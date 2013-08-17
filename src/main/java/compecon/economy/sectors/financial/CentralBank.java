/*
Copyright (C) 2013 u.wol@wwu.de 
 
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

package compecon.economy.sectors.financial;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import compecon.economy.sectors.financial.BankAccount.BankAccountType;
import compecon.economy.sectors.state.State;
import compecon.economy.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.economy.sectors.state.law.security.debt.Bond;
import compecon.economy.sectors.state.law.security.debt.FixedRateBond;
import compecon.engine.MarketFactory;
import compecon.engine.dao.DAOFactory;
import compecon.engine.jmx.Log;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.TimeSystem;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.HourType;
import compecon.engine.time.calendar.MonthType;
import compecon.engine.util.ConfigurationUtil;
import compecon.materia.GoodType;

/**
 * Agent type central bank adjusts key interest rates based on price indices.
 */
@Entity
public class CentralBank extends Bank {

	// constants

	@Transient
	protected int NUMBER_OF_MARGINAL_PRICE_SNAPSHOTS_PER_DAY;

	@Transient
	protected StatisticalOffice statisticalOffice;

	// state

	@Column(name = "effectiveKeyInterestRate")
	protected double effectiveKeyInterestRate = 0.1;

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
				DayType.EVERY, HourType.HOUR_03);
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
	 * accessors
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
	 * assertions
	 */

	@Transient
	@Override
	public void assureTransactionsBankAccount() {
		if (this.isDeconstructed)
			return;

		this.assureSelfCustomerAccount();

		if (this.transactionsBankAccount == null) {
			/*
			 * initialize the banks own bank account and open a customer account
			 * at this new bank, so that this bank can transfer money from its
			 * own bank account
			 */
			this.transactionsBankAccount = this.primaryBank.openBankAccount(
					this, this.primaryCurrency,
					this.bankPasswords.get(this.primaryBank),
					"transactions account", BankAccountType.GIRO);
		}
	}

	@Transient
	protected void assertCurrencyIsOffered(Currency currency) {
		if (this.primaryCurrency != currency)
			throw new RuntimeException(currency
					+ " are not offered at this bank");
	}

	/*
	 * business logic
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

		if (from == to)
			throw new RuntimeException("the bank accounts are identical");

		if (from.getManagingBank() instanceof CentralBank
				&& to.getManagingBank() instanceof CentralBank) {
			Log.bank_onTransfer(from, to, from.getCurrency(), amount, subject);
			this.transferMoneyInternally(from, to, amount, password);
		} else if (from.getManagingBank() instanceof CreditBank
				&& to.getManagingBank() instanceof CentralBank)
			this.transferMoneyFromCreditBankAccountToCentralBankAccount(from,
					to, amount, password);
		else if (from.getManagingBank() instanceof CentralBank
				&& to.getManagingBank() instanceof CreditBank) {
			Log.bank_onTransfer(from, to, from.getCurrency(), amount, subject);
			this.transferMoneyFromCentralBankAccountToCreditBankAccount(from,
					to, amount, password);
		} else
			throw new RuntimeException("uncovered case");
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
		creditBank.assureCentralBankAccount();
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
		this.assertIsCustomerOfThisBank(creditBank);

		for (Bond bond : bonds) {
			BankAccount bankAccount = this.getBankAccounts(creditBank,
					this.customerPasswords.get(creditBank)).get(0);
			bankAccount.deposit(bond.getFaceValue());
			if (Log.isAgentSelectedByClient(creditBank))
				Log.log(creditBank,
						"obtained a tender of "
								+ Currency.formatMoneySum(bond.getFaceValue())
								+ " " + this.getPrimaryCurrency()
								+ " of central bank money from " + this);
		}
	}

	@Transient
	protected double calculateEffectiveKeyInterestRate() {
		double targetPriceIndexForCurrentPeriod = this
				.calculateTargetPriceIndexForPeriod();
		double currentPriceIndex = this.statisticalOffice.getPriceIndex();
		double newEffectiveKeyInterestRate = 0.03 + (((currentPriceIndex - targetPriceIndexForCurrentPeriod) / currentPriceIndex) / 10);

		if (!Double.isNaN(newEffectiveKeyInterestRate)
				&& !Double.isInfinite(newEffectiveKeyInterestRate)) {
			return newEffectiveKeyInterestRate;
		}
		return this.effectiveKeyInterestRate;
	}

	@Transient
	protected double calculateTargetPriceIndexForPeriod() {
		int yearNumber = TimeSystem.getInstance().getCurrentYear()
				- TimeSystem.getInstance().getStartYear();
		double targetPriceLevelForYear = Math.pow(
				(1 + ConfigurationUtil.CentralBankConfig.getInflationTarget()),
				yearNumber);

		double monthlyNominalInflationTarget = this
				.calculateMonthlyNominalInterestRate(ConfigurationUtil.CentralBankConfig
						.getInflationTarget());

		double targetPriceLevelForMonth = Math.pow(
				1.0 + monthlyNominalInflationTarget, TimeSystem.getInstance()
						.getCurrentMonthNumberInYear() - 1) - 1.0;
		double targetPriceLevelForDay = (monthlyNominalInflationTarget / 30)
				* TimeSystem.getInstance().getCurrentDayNumberInMonth();

		double combinedTargetPriceLevel = (targetPriceLevelForYear
				+ targetPriceLevelForMonth + targetPriceLevelForDay);
		return ConfigurationUtil.CentralBankConfig.getTargetPriceIndex()
				* combinedTargetPriceLevel;
	}

	@Transient
	public double getAverageMarginalPriceForGoodType(GoodType goodType) {
		return this.statisticalOffice
				.getAverageMarginalPriceForGoodType(goodType);
	}

	@Transient
	public double getReserveRatio() {
		return ConfigurationUtil.CentralBankConfig.getReserveRatio();
	}

	protected double calculateTotalDividend() {
		// central banks transfer their profit to the state, not arbitrary
		// agents -> deactivate dividends, transfer money in separate event
		return 0;
	}

	public class DailyInterestCalculationEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			assureTransactionsBankAccount();

			for (BankAccount bankAccount : DAOFactory.getBankAccountDAO()
					.findAllBankAccountsManagedByBank(CentralBank.this)) {
				double monthlyInterest = bankAccount.getBalance()
						* CentralBank.this
								.calculateMonthlyNominalInterestRate(CentralBank.this.effectiveKeyInterestRate);
				double dailyInterest = monthlyInterest / 30;

				// liability account + positive interest rate or asset account +
				// negative interest rate
				if (dailyInterest > 0) {
					try {
						CentralBank.this.transferMoneyInternally(
								CentralBank.this.transactionsBankAccount,
								bankAccount, dailyInterest,
								CentralBank.this.customerPasswords
										.get(CentralBank.this));
					} catch (Exception e) {
						throw new RuntimeException(e.getMessage());
					}
				}
				// asset account + positive interest rate or liability
				// account + negative interest rate
				else if (dailyInterest < 0) {
					try {
						dailyInterest = -1 * dailyInterest;
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

			if (CentralBank.this.transactionsBankAccount.getBalance() > 0) {
				State state = DAOFactory.getStateDAO().findByCurrency(
						primaryCurrency);
				CentralBank.this.transferMoney(
						CentralBank.this.transactionsBankAccount,
						state.getTransactionsBankAccount(),
						CentralBank.this.transactionsBankAccount.getBalance(),
						CentralBank.this.bankPasswords.get(CentralBank.this),
						"national interest");
			}
		}
	}

	public class MarginalPriceSnapshotEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			CentralBank.this.statisticalOffice.takeSnapshotOfMarginalPrices();
		}
	}

	public class KeyInterestRateCalculationEvent implements ITimeSystemEvent {
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

	public class BalanceSheetPublicationEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			BalanceSheet balanceSheet = CentralBank.this
					.issueBasicBalanceSheet();

			// bank accounts of customers
			for (BankAccount bankAccount : DAOFactory.getBankAccountDAO()
					.findAllBankAccountsManagedByBank(CentralBank.this)) {
				// TODO compare with referenceCurrency of balance sheet
				if (bankAccount.getBalance() > 0) // passive account
					balanceSheet.bankBorrowings += bankAccount.getBalance();
				else
					// active account
					balanceSheet.bankLoans += bankAccount.getBalance() * -1;
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

		protected final int NUMBER_OF_LOGGED_PERIODS = 3;

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

			this.priceIndexWeights.put(GoodType.WHEAT, 0.2);
			this.priceIndexWeights.put(GoodType.KILOWATT, 0.2);
			this.priceIndexWeights.put(GoodType.CAR, 0.2);
			this.priceIndexWeights.put(GoodType.REALESTATE, 0.2);
			this.priceIndexWeights.put(GoodType.GOLD, 0.2);

			/*
			 * GoodType LABOURHOUR is not monitored, as its market price is not
			 * influenced by the key interest rate over a transmission mechanism
			 * in the buying behaviour, but instead by comparison of marginal
			 * costs and prices in the production behaviour
			 */

			/*
			 * initialize monitoredMarginalPrices and
			 * averageMarginalPricesForGoodTypes; prices should be stored for
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
				double marginalPriceForGoodType = MarketFactory.getInstance()
						.getMarginalPrice(CentralBank.this.primaryCurrency,
								entry.getKey());

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

				// average marginal price of the good type
				double averageMarginalPrice = this.averageMarginalPricesForGoodTypes
						.get(goodType);

				// add marginal price for good type to price index, weighted by
				// defined weight
				double newPriceIndexForGoodType = weight * averageMarginalPrice;

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
