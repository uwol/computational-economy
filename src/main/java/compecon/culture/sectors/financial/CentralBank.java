/*
This file is part of ComputationalEconomy.

ComputationalEconomy is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

ComputationalEconomy is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ComputationalEconomy. If not, see <http://www.gnu.org/licenses/>.
 */

package compecon.culture.sectors.financial;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import compecon.culture.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.culture.sectors.state.law.property.IProperty;
import compecon.culture.sectors.state.law.property.PropertyRegister;
import compecon.culture.sectors.state.law.security.debt.Bond;
import compecon.culture.sectors.state.law.security.debt.FixedRateBond;
import compecon.engine.Log;
import compecon.engine.MarketFactory;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.HourType;
import compecon.engine.time.calendar.MonthType;
import compecon.nature.materia.GoodType;

/**
 * Agent type central bank adjusts key interest rates based on price indices.
 */
@Entity
// @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@Table(name = "CentralBank")
public class CentralBank extends Bank {

	// constants
	@Transient
	protected final double RESERVE_RATIO = 0.1;

	@Transient
	protected final double INFLATION_TARGET = 0.02;

	@Transient
	protected final double MAX_EFFECTIVE_KEY_INTEREST_RATE = 0.2;

	@Transient
	protected final double MIN_EFFECTIVE_KEY_INTEREST_RATE = 0;

	@Transient
	protected int NUMBER_OF_MARGINAL_PRICE_SNAPSHOTS_PER_DAY;

	@Transient
	protected StatisticalOffice statisticalOffice;

	// state

	@Column(name = "effectiveKeyInterestRate")
	protected double effectiveKeyInterestRate = 0.03;

	@Override
	public void initialize() {
		super.initialize();

		// calculate interest
		ITimeSystemEvent interestCalculationEvent = new DailyInterestCalculationEvent();
		this.timeSystemEvents.add(interestCalculationEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				interestCalculationEvent, -1, MonthType.EVERY, DayType.EVERY,
				HourType.HOUR_01);

		// take snapshots of marginal prices multiple times a day
		// -> market situation differs over the day !!!
		ITimeSystemEvent recalculateAveragePriceIndexEvent = new MarginalPriceSnapshotEvent();
		this.timeSystemEvents.add(recalculateAveragePriceIndexEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				recalculateAveragePriceIndexEvent, -1, MonthType.EVERY,
				DayType.EVERY, HourType.HOUR_09);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				recalculateAveragePriceIndexEvent, -1, MonthType.EVERY,
				DayType.EVERY, HourType.HOUR_15);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				recalculateAveragePriceIndexEvent, -1, MonthType.EVERY,
				DayType.EVERY, HourType.HOUR_21);

		// recalculate key interest rate every day
		ITimeSystemEvent keyInterestRateCalculationEvent = new KeyInterestRateCalculationEvent();
		this.timeSystemEvents.add(keyInterestRateCalculationEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				keyInterestRateCalculationEvent, -1, MonthType.EVERY,
				DayType.EVERY, HourType.HOUR_01);

		// balance sheet publication
		ITimeSystemEvent balanceSheetPublicationEvent = new BalanceSheetPublicationEvent();
		this.timeSystemEvents.add(balanceSheetPublicationEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				balanceSheetPublicationEvent, -1, MonthType.EVERY,
				DayType.EVERY, BALANCE_SHEET_PUBLICATION_HOUR_TYPE);

		// count number of snapshots that are taken per day
		int numberOfSnapshotsPerDay = 0;
		for (ITimeSystemEvent event : CentralBank.this.timeSystemEvents)
			if (event instanceof MarginalPriceSnapshotEvent)
				numberOfSnapshotsPerDay++;
		this.NUMBER_OF_MARGINAL_PRICE_SNAPSHOTS_PER_DAY = numberOfSnapshotsPerDay;

		// statistical office; has to be initialized after calculating
		// NUMBER_OF_SNAPSHOTS_PER_DAY
		this.statisticalOffice = new StatisticalOffice();
	}

	/*
	 * Accessors
	 */

	public Currency getPrimaryCurrency() {
		return this.primaryCurrency;
	}

	public double getEffectiveKeyInterestRate() {
		return this.effectiveKeyInterestRate;
	}

	public void setPrimaryCurrency(final Currency primaryCurrency) {
		this.primaryCurrency = primaryCurrency;
	}

	public void setEffectiveKeyInterestRate(double effectiveKeyInterestRate) {
		this.effectiveKeyInterestRate = effectiveKeyInterestRate;
	}

	/*
	 * Assertions
	 */

	@Transient
	@Override
	protected void assertTransactionsBankAccount() {
		if (this.primaryBank == null) {
			this.primaryBank = this;
		}
		if (this.transactionsBankAccount == null) {
			// initialize the banks own bank account and open a customer account
			// at
			// this new bank, so that this bank can transfer money from its own
			// bank account
			String bankPassword = this.openCustomerAccount(this);
			this.transactionsBankAccount = new BankAccount(this, true,
					this.primaryCurrency, this);
			this.bankPasswords.put(this, bankPassword);
		}
	}

	@Transient
	protected void assertCurrencyIsOffered(Currency currency) {
		if (this.primaryCurrency != currency)
			throw new RuntimeException(currency
					+ " are not offered at this bank");
	}

	/*
	 * Business logic
	 */

	@Transient
	public void transferMoney(BankAccount from, BankAccount to, double amount,
			String password, String subject) {
		this.transferMoney(from, to, amount, password, subject, false);
	}

	@Transient
	protected void transferMoney(BankAccount from, BankAccount to,
			double amount, String password, String subject,
			boolean negativeAmountOK) {

		if (!negativeAmountOK && amount < 0)
			throw new RuntimeException("amount must be larger than 0");

		if (from.getManagingBank() instanceof CentralBank
				&& to.getManagingBank() instanceof CentralBank) {
			this.transferMoneyInternally(from, to, amount, password);
			Log.bank_onTransfer(from.getOwner(), to.getOwner(),
					from.getCurrency(), amount, subject);
		} else if (from.getManagingBank() instanceof CreditBank
				&& to.getManagingBank() instanceof CentralBank)
			this.transferMoneyFromCreditBankAccountToCentralBankAccount(from,
					to, amount, password);
		else if (from.getManagingBank() instanceof CentralBank
				&& to.getManagingBank() instanceof CreditBank) {
			this.transferMoneyFromCentralBankAccountToCreditBankAccount(from,
					to, amount, password);
		}
	}

	@Transient
	private void transferMoneyInternally(BankAccount from, BankAccount to,
			double amount, String password) {
		this.assertPasswordOk(from.getOwner(), password);
		this.assertBankAccountIsManagedByThisBank(from);
		this.assertBankAccountIsManagedByThisBank(to);

		if (amount < 0)
			throw new RuntimeException("amount must be larger than 0");

		if (from.getCurrency() != to.getCurrency())
			throw new RuntimeException(
					"both bank accounts must have the same currency");

		// unusual at the central bank
		if (from.getBalance() - amount < 0 && !from.getOverdraftPossible())
			throw new RuntimeException(
					"amount is too high and bank account cannot be overdraft");

		// transfer money internally
		from.withdraw(amount);
		to.deposit(amount);
	}

	@Transient
	private void transferMoneyFromCentralBankAccountToCreditBankAccount(
			BankAccount from, BankAccount to, double amount, String password) {
		/*
		 * Checks
		 */
		this.assertPasswordOk(from.getOwner(), password);
		this.assertBankAccountIsManagedByThisBank(from);

		if (amount < 0)
			throw new RuntimeException("amount must be larger than 0");

		if (from.getCurrency() != to.getCurrency())
			throw new RuntimeException(
					"both bank accounts must have the same currency");

		// unusual at the central bank
		if (from.getBalance() - amount < 0 && !from.getOverdraftPossible())
			throw new RuntimeException(
					"amount is too high and bank account cannot be overdraft");

		if (!(to.getManagingBank() instanceof CreditBank))
			throw new RuntimeException(
					"managing bank of target bank account is not a credit bank");

		/*
		 * Transaction
		 */
		CreditBank creditBank = (CreditBank) to.getManagingBank();
		from.withdraw(amount);
		creditBank.assertCentralBankAccount();
		creditBank.deposit(this, this.customerPasswords.get(creditBank), to,
				amount);
	}

	@Transient
	private void transferMoneyFromCreditBankAccountToCentralBankAccount(
			BankAccount from, BankAccount to, double amount, String password) {
		/*
		 * Checks
		 */
		this.assertBankAccountIsManagedByThisBank(to);

		if (amount < 0)
			throw new RuntimeException("amount must be larger than 0");

		if (from.getCurrency() != to.getCurrency())
			throw new RuntimeException(
					"both bank accounts must have the same currency");

		// unusual at the central bank
		if (from.getBalance() - amount < 0 && !from.getOverdraftPossible())
			throw new RuntimeException(
					"amount is too high and bank account cannot be overdraft");

		if (!(from.getManagingBank() instanceof CreditBank))
			throw new RuntimeException(
					"managing bank of source bank account is not a credit bank");

		/*
		 * Transaction
		 */
		CreditBank creditBank = (CreditBank) from.getManagingBank();
		creditBank.withdraw(this, this.customerPasswords.get(creditBank), from,
				amount);
		to.deposit(amount);
	}

	@Transient
	public void obtainTender(CreditBank creditBank, List<FixedRateBond> bonds,
			String password) {
		this.assertPasswordOk(creditBank, password);
		this.assertIsClientAtThisBank(creditBank);

		for (Bond bond : bonds) {
			this.customerBankAccounts.get(creditBank).deposit(
					bond.getFaceValue());
			Log.centralBank_onObtainTender(this, bond.getFaceValue(),
					creditBank);
		}
	}

	@Transient
	protected double calculateEffectiveKeyInterestRate() {
		double monthlyNominalInflationTarget = this
				.calculateMonthlyNominalInterestRate(INFLATION_TARGET);
		double dailyNominalInflationTarget = monthlyNominalInflationTarget / 30;

		// prices have risen?
		if (this.statisticalOffice.getPriceIndex()
				* (1 + dailyNominalInflationTarget) > 1)
			// raise key interest rate -> contractive monetary policy
			return Math.min(this.effectiveKeyInterestRate + 0.001,
					MAX_EFFECTIVE_KEY_INTEREST_RATE);
		// prices have fallen?
		else if (this.statisticalOffice.getPriceIndex()
				* (1 + dailyNominalInflationTarget) < 1)
			// lower key interest rate -> expansive monetary policy
			return Math.max(this.effectiveKeyInterestRate - 0.001,
					MIN_EFFECTIVE_KEY_INTEREST_RATE);
		else
			return this.effectiveKeyInterestRate;
	}

	@Transient
	public double getAverageMarginalPriceForGoodType(GoodType goodType) {
		return this.statisticalOffice
				.getAverageMarginalPriceForGoodType(goodType);
	}

	@Transient
	public double getMaxEffectiveKeyInterestRate() {
		return this.MAX_EFFECTIVE_KEY_INTEREST_RATE;
	}

	@Override
	@Transient
	protected double calculateTotalDividend() {
		assertTransactionsBankAccount();
		return Math.max(0.0, this.transactionsBankAccount.getBalance()
				- MONEY_TO_RETAIN);
	}

	@Transient
	public double getReserveRatio() {
		return RESERVE_RATIO;
	}

	protected class DailyInterestCalculationEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			for (BankAccount bankAccount : CentralBank.this.customerBankAccounts
					.values()) {
				if (bankAccount.getBalance() > 0) { // liability account
					double monthlyInterest = bankAccount.getBalance()
							* CentralBank.this
									.calculateMonthlyNominalInterestRate(CentralBank.this.effectiveKeyInterestRate);
					double dailyInterest = monthlyInterest / 30;
					try {
						CentralBank.this.transferMoneyInternally(
								CentralBank.this.transactionsBankAccount,
								bankAccount, dailyInterest,
								CentralBank.this.customerPasswords
										.get(CentralBank.this));
					} catch (Exception e) {
						throw new RuntimeException(e.getMessage());
					}
				} else if (bankAccount.getBalance() < 0) { // asset account
					double monthlyInterest = -1
							* bankAccount.getBalance()
							* CentralBank.this
									.calculateMonthlyNominalInterestRate(CentralBank.this.effectiveKeyInterestRate);
					double dailyInterest = monthlyInterest / 30;
					try {
						CentralBank.this.transferMoneyInternally(bankAccount,
								CentralBank.this.transactionsBankAccount,
								dailyInterest,
								CentralBank.this.customerPasswords
										.get(bankAccount.getOwner()));
					} catch (Exception e) {
						throw new RuntimeException(e.getMessage());
					}
				}
			}
		}
	}

	protected class MarginalPriceSnapshotEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			CentralBank.this.statisticalOffice.takeSnapshotOfMarginalPrices();
		}
	}

	protected class KeyInterestRateCalculationEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			// calculate price index
			CentralBank.this.statisticalOffice.recalculateAveragePrices();
			CentralBank.this.statisticalOffice.recalculatePriceIndex();
			double priceIndex = CentralBank.this.statisticalOffice
					.getPriceIndex();
			Log.centralBank_PriceIndex(CentralBank.this.primaryCurrency,
					priceIndex);

			// calculate key interest rate
			CentralBank.this.effectiveKeyInterestRate = CentralBank.this
					.calculateEffectiveKeyInterestRate();
			Log.centralBank_KeyInterestRate(CentralBank.this.primaryCurrency,
					CentralBank.this.effectiveKeyInterestRate);
		}
	}

	protected class BalanceSheetPublicationEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			BalanceSheet balanceSheet = CentralBank.this
					.issueBasicBalanceSheet();

			// bank accounts of customers
			for (BankAccount bankAccount : CentralBank.this.customerBankAccounts
					.values()) {
				// TODO compare with referenceCurrency of balance sheet
				if (bankAccount.getBalance() > 0) // passive account
					balanceSheet.bankBorrowings += bankAccount.getBalance();
				else
					// active account
					balanceSheet.bankLoans += bankAccount.getBalance() * -1;
			}

			// bonds
			for (IProperty property : PropertyRegister.getInstance()
					.getIProperties(CentralBank.this)) {
				if (property instanceof Bond)
					balanceSheet.bonds += ((Bond) property).getFaceValue();
			}

			// --------------

			// publish
			Log.agent_onPublishBalanceSheet(CentralBank.this, balanceSheet);
		}
	}

	/**
	 * the statistical office takes snapshots of marginal market prices not only
	 * for the purpose of calculating the price index, but generally for
	 * offering information about markets to agents
	 */
	protected class StatisticalOffice {
		// constants

		protected final int NUMBER_OF_LOGGED_PERIODS = 180;

		protected final Map<GoodType, Double> priceIndexWeights = new HashMap<GoodType, Double>();

		// state

		protected Map<GoodType, double[]> monitoredMarginalPricesForGoodTypesAndPeriods = new HashMap<GoodType, double[]>();

		// pre-calculated values

		protected Map<GoodType, Double> averageMarginalPricesForGoodTypes = new HashMap<GoodType, Double>();

		protected double priceIndex = 0;

		public StatisticalOffice() {
			/*
			 * set price index weights, must sum up to 1
			 */
			this.priceIndexWeights.put(GoodType.MEGACALORIE, 0.5);
			this.priceIndexWeights.put(GoodType.KILOWATT, 0.5);
			/*
			 * GoodType LABOURHOUR is not monitored, as its market price is not
			 * influenced by the key interest rate over a transmission mechanism
			 * in the buying behaviour, but instead by comparison of marginal
			 * costs and prices in the production behaviour
			 */

			/*
			 * initialize monitoredMarginalPrices and
			 * averageMarginalPricesForGoodTypes; prices should be stores for
			 * all GoodTypes, not only those in the price index
			 */
			for (GoodType goodType : GoodType.values()) {
				this.monitoredMarginalPricesForGoodTypesAndPeriods
						.put(goodType,
								new double[NUMBER_OF_LOGGED_PERIODS
										* CentralBank.this.NUMBER_OF_MARGINAL_PRICE_SNAPSHOTS_PER_DAY]);

				this.averageMarginalPricesForGoodTypes
						.put(goodType, Double.NaN);
			}
		}

		protected void takeSnapshotOfMarginalPrices() {
			// store marginal prices of monitored good types for this period
			for (Entry<GoodType, double[]> entry : this.monitoredMarginalPricesForGoodTypesAndPeriods
					.entrySet()) {
				double[] pricesForGoodType = entry.getValue();

				// fetch and store current price for this good type
				double marginalPriceForGoodType = MarketFactory.getInstance(
						CentralBank.this.primaryCurrency).getMarginalPrice(
						entry.getKey(), CentralBank.this.primaryCurrency)
						/ CentralBank.this.NUMBER_OF_MARGINAL_PRICE_SNAPSHOTS_PER_DAY;

				if (!Double.isNaN(marginalPriceForGoodType)
						&& !Double.isInfinite(marginalPriceForGoodType)) {
					// shift prices of older periods for this good type
					System.arraycopy(pricesForGoodType, 0, pricesForGoodType,
							1, pricesForGoodType.length - 1);
					pricesForGoodType[0] = marginalPriceForGoodType;
				}
			}
		}

		protected void recalculateAveragePrices() {
			// for each monitored good type
			for (Entry<GoodType, double[]> entry : this.monitoredMarginalPricesForGoodTypesAndPeriods
					.entrySet()) {
				double[] monitoredMarginalPricesForGoodType = entry.getValue();

				double priceSumForGoodType = 0;
				int totalWeight = 0;

				// recalculate average price
				for (int i = 0; i < monitoredMarginalPricesForGoodType.length; i++) {
					double marginalPriceForGoodType = monitoredMarginalPricesForGoodType[i];
					if (marginalPriceForGoodType != 0
							&& !Double.isNaN(marginalPriceForGoodType)
							&& !Double.isInfinite(marginalPriceForGoodType)) {
						// weight period by age
						int weight = monitoredMarginalPricesForGoodType.length
								- i;
						priceSumForGoodType += marginalPriceForGoodType
								* weight;
						totalWeight += weight;
					}
				}

				double averagePriceForGoodType = priceSumForGoodType
						/ totalWeight;
				if (!Double.isNaN(totalWeight)
						&& !Double.isInfinite(totalWeight))
					this.averageMarginalPricesForGoodTypes.put(entry.getKey(),
							averagePriceForGoodType);
			}
		}

		protected void recalculatePriceIndex() {
			double newPriceIndex = Double.NaN;

			// for basket of good types
			for (Entry<GoodType, Double> entry : this.priceIndexWeights
					.entrySet()) {
				GoodType goodType = entry.getKey();
				Double weight = entry.getValue();
				double[] monitoredMarginalPrices = this.monitoredMarginalPricesForGoodTypesAndPeriods
						.get(entry.getKey());
				// add marginal price for good type to price index, weighted by
				// defined weight and average marginal price of this good type
				double weightedMarginalPrice = weight
						* monitoredMarginalPrices[0];
				double averageMarginalPrice = this.averageMarginalPricesForGoodTypes
						.get(goodType);
				double newPriceIndexForGoodType = weightedMarginalPrice
						/ averageMarginalPrice;

				if (!Double.isNaN(newPriceIndexForGoodType)
						&& !Double.isInfinite(newPriceIndexForGoodType)) {
					if (Double.isNaN(newPriceIndex)
							|| Double.isInfinite(newPriceIndex))
						newPriceIndex = newPriceIndexForGoodType;
					else
						newPriceIndex += newPriceIndexForGoodType;
				}
			}

			// store average price index
			this.priceIndex = newPriceIndex;
		}

		protected double getPriceIndex() {
			return this.priceIndex;
		}

		protected double getAverageMarginalPriceForGoodType(GoodType goodType) {
			return this.averageMarginalPricesForGoodTypes.get(goodType);
		}
	}
}
