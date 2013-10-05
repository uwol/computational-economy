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
import compecon.economy.sectors.state.law.property.PropertyRegister;
import compecon.economy.sectors.state.law.security.debt.Bond;
import compecon.economy.sectors.state.law.security.debt.FixedRateBond;
import compecon.engine.MarketFactory;
import compecon.engine.Simulation;
import compecon.engine.dao.DAOFactory;
import compecon.engine.statistics.Log;
import compecon.engine.time.ITimeSystemEvent;
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
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(interestCalculationEvent, -1, MonthType.EVERY,
						DayType.EVERY, HourType.HOUR_01);

		// take snapshots of marginal prices multiple times a day
		// -> market situation differs over the day !!!
		ITimeSystemEvent recalculateAveragePriceIndexEvent = new MarginalPriceSnapshotEvent();
		this.timeSystemEvents.add(recalculateAveragePriceIndexEvent);
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(recalculateAveragePriceIndexEvent, -1,
						MonthType.EVERY, DayType.EVERY, HourType.HOUR_03);
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(recalculateAveragePriceIndexEvent, -1,
						MonthType.EVERY, DayType.EVERY, HourType.HOUR_09);
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(recalculateAveragePriceIndexEvent, -1,
						MonthType.EVERY, DayType.EVERY, HourType.HOUR_15);
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(recalculateAveragePriceIndexEvent, -1,
						MonthType.EVERY, DayType.EVERY, HourType.HOUR_21);

		// recalculate key interest rate every day
		ITimeSystemEvent keyInterestRateCalculationEvent = new KeyInterestRateCalculationEvent();
		this.timeSystemEvents.add(keyInterestRateCalculationEvent);
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(keyInterestRateCalculationEvent, -1, MonthType.EVERY,
						DayType.EVERY, HourType.HOUR_01);

		// balance sheet publication
		ITimeSystemEvent balanceSheetPublicationEvent = new BalanceSheetPublicationEvent();
		this.timeSystemEvents.add(balanceSheetPublicationEvent);
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(balanceSheetPublicationEvent, -1, MonthType.EVERY,
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
					this, this.primaryCurrency, "transactions account",
					BankAccountType.GIRO);
		}
	}

	@Transient
	protected void assertCurrencyIsOffered(Currency currency) {
		assert (this.primaryCurrency == currency);
	}

	/*
	 * business logic
	 */

	@Transient
	public void transferMoney(BankAccount from, BankAccount to, double amount,
			String subject) {
		this.transferMoney(from, to, amount, subject, false);
	}

	@Transient
	protected void transferMoney(BankAccount from, BankAccount to,
			double amount, String subject, boolean negativeAmountOK) {

		assert (amount >= 0.0 || negativeAmountOK);
		assert (from != to);

		if (from.getManagingBank() instanceof CentralBank
				&& to.getManagingBank() instanceof CentralBank) {
			Log.bank_onTransfer(from, to, from.getCurrency(), amount, subject);
			this.transferMoneyInternally(from, to, amount);
		} else if (from.getManagingBank() instanceof CreditBank
				&& to.getManagingBank() instanceof CentralBank)
			this.transferMoneyFromCreditBankAccountToCentralBankAccount(from,
					to, amount);
		else if (from.getManagingBank() instanceof CentralBank
				&& to.getManagingBank() instanceof CreditBank) {
			Log.bank_onTransfer(from, to, from.getCurrency(), amount, subject);
			this.transferMoneyFromCentralBankAccountToCreditBankAccount(from,
					to, amount);
		} else
			throw new RuntimeException("uncovered case");
	}

	@Transient
	private void transferMoneyInternally(BankAccount from, BankAccount to,
			double amount) {
		this.assertBankAccountIsManagedByThisBank(from);
		this.assertBankAccountIsManagedByThisBank(to);

		assert (amount >= 0);
		assert (from.getCurrency().equals(to.getCurrency()));
		// unusual at the central bank
		assert (from.getBalance() - amount >= 0 || from.getOverdraftPossible());

		// transfer money internally
		from.withdraw(amount);
		to.deposit(amount);
	}

	@Transient
	private void transferMoneyFromCentralBankAccountToCreditBankAccount(
			BankAccount from, BankAccount to, double amount) {
		/*
		 * Checks
		 */
		this.assertBankAccountIsManagedByThisBank(from);

		assert (amount >= 0.0);
		assert (from.getCurrency().equals(to.getCurrency()));

		// unusual at the central bank
		assert (from.getBalance() - amount >= 0.0 || from
				.getOverdraftPossible());

		assert (to.getManagingBank() instanceof CreditBank);

		/*
		 * Transaction
		 */
		CreditBank creditBank = (CreditBank) to.getManagingBank();
		from.withdraw(amount);
		creditBank.assureCentralBankAccount();
		creditBank.deposit(this, to, amount);
	}

	@Transient
	private void transferMoneyFromCreditBankAccountToCentralBankAccount(
			BankAccount from, BankAccount to, double amount) {
		/*
		 * Checks
		 */
		this.assertBankAccountIsManagedByThisBank(to);

		assert (amount >= 0);
		assert (from.getCurrency().equals(to.getCurrency()));
		// unusual at the central bank
		assert (from.getBalance() - amount >= 0 || from.getOverdraftPossible());
		assert (from.getManagingBank() instanceof CreditBank);

		/*
		 * Transaction
		 */
		CreditBank creditBank = (CreditBank) from.getManagingBank();
		creditBank.withdraw(this, from, amount);
		to.deposit(amount);
	}

	@Transient
	public void obtainTender(CreditBank creditBank, List<FixedRateBond> bonds) {
		this.assertIsCustomerOfThisBank(creditBank);

		for (Bond bond : bonds) {
			BankAccount bankAccount = DAOFactory.getBankAccountDAO()
					.findAll(this, creditBank).get(0);

			bankAccount.deposit(bond.getFaceValue());
			PropertyRegister.getInstance().transferProperty(creditBank, this,
					bond);

			if (Log.isAgentSelectedByClient(creditBank))
				Log.log(creditBank,
						"obtained a tender of "
								+ Currency.formatMoneySum(bond.getFaceValue())
								+ " " + this.getPrimaryCurrency()
								+ " of central bank money from " + this);
		}
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
		/**
		 * central banks transfer their profit to the state, not arbitrary
		 * agents -> deactivate dividends, transfer money in separate event
		 * 
		 * @see DailyInterestCalculationEvent
		 */
		return 0.0;
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
				double dailyInterest = monthlyInterest / 30.0;

				// liability account + positive interest rate or asset account +
				// negative interest rate
				if (dailyInterest > 0.0) {
					CentralBank.this.transferMoneyInternally(
							CentralBank.this.transactionsBankAccount,
							bankAccount, dailyInterest);
				}
				// asset account + positive interest rate or liability
				// account + negative interest rate
				else if (dailyInterest < 0.0) {
					dailyInterest = -1.0 * dailyInterest;
					CentralBank.this.transferMoneyInternally(bankAccount,
							CentralBank.this.transactionsBankAccount,
							dailyInterest);
				}
			}

			if (CentralBank.this.transactionsBankAccount.getBalance() > 0.0) {
				State state = DAOFactory.getStateDAO().findByCurrency(
						primaryCurrency);
				CentralBank.this.transferMoney(
						CentralBank.this.transactionsBankAccount,
						state.getTransactionsBankAccount(),
						CentralBank.this.transactionsBankAccount.getBalance(),
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
			CentralBank.this.effectiveKeyInterestRate = calculateEffectiveKeyInterestRate();
			Log.centralBank_KeyInterestRate(CentralBank.this.primaryCurrency,
					CentralBank.this.effectiveKeyInterestRate);
		}

		@Transient
		protected double calculateEffectiveKeyInterestRate() {
			double targetPriceIndexForCurrentPeriod = this
					.calculateTargetPriceIndexForPeriod();
			double currentPriceIndex = CentralBank.this.statisticalOffice
					.getPriceIndex();
			double newEffectiveKeyInterestRate = 0.03 + (((currentPriceIndex - targetPriceIndexForCurrentPeriod) / currentPriceIndex) / 10.0);

			if (!Double.isNaN(newEffectiveKeyInterestRate)
					&& !Double.isInfinite(newEffectiveKeyInterestRate)) {
				if (ConfigurationUtil.CentralBankConfig
						.getAllowNegativeKeyInterestRate()) {
					return newEffectiveKeyInterestRate;
				} else {
					return Math.max(0.0, newEffectiveKeyInterestRate);
				}
			} else {
				return CentralBank.this.effectiveKeyInterestRate;
			}
		}

		@Transient
		protected double calculateTargetPriceIndexForPeriod() {
			int yearNumber = Simulation.getInstance().getTimeSystem()
					.getCurrentYear()
					- Simulation.getInstance().getTimeSystem().getStartYear();
			double targetPriceLevelForYear = Math.pow(
					(1 + ConfigurationUtil.CentralBankConfig
							.getInflationTarget()), yearNumber);

			double monthlyNominalInflationTarget = CentralBank.this
					.calculateMonthlyNominalInterestRate(ConfigurationUtil.CentralBankConfig
							.getInflationTarget());

			double targetPriceLevelForMonth = Math.pow(
					1.0 + monthlyNominalInflationTarget, (double) Simulation
							.getInstance().getTimeSystem()
							.getCurrentMonthNumberInYear() - 1.0) - 1.0;
			double targetPriceLevelForDay = (monthlyNominalInflationTarget / 30.0)
					* Simulation.getInstance().getTimeSystem()
							.getCurrentDayNumberInMonth();

			double combinedTargetPriceLevel = (targetPriceLevelForYear
					+ targetPriceLevelForMonth + targetPriceLevelForDay);
			return ConfigurationUtil.CentralBankConfig.getTargetPriceIndex()
					* combinedTargetPriceLevel;
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
				if (bankAccount.getBalance() > 0.0) // passive account
					balanceSheet.bankBorrowings += bankAccount.getBalance();
				else
					// active account
					balanceSheet.bankLoans += bankAccount.getBalance() * -1.0;
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

		protected double priceIndex = 0.0;

		public StatisticalOffice() {
			/*
			 * set price index weights, must sum up to 1
			 */

			this.priceIndexWeights.put(GoodType.FOOD, 0.25);
			this.priceIndexWeights.put(GoodType.CLOTHING, 0.25);
			this.priceIndexWeights.put(GoodType.KILOWATT, 0.25);
			this.priceIndexWeights.put(GoodType.REALESTATE, 0.25);

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
						.getPrice(CentralBank.this.primaryCurrency,
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

				double priceSumForGoodType = 0.0;
				double totalWeight = 0;

				// recalculate average price
				for (int i = 0; i < monitoredMarginalPricesForGoodType.length; i++) {
					double marginalPriceForGoodType = monitoredMarginalPricesForGoodType[i];
					if (marginalPriceForGoodType != 0.0
							&& !Double.isNaN(marginalPriceForGoodType)
							&& !Double.isInfinite(marginalPriceForGoodType)) {
						// weight period by age
						double weight = monitoredMarginalPricesForGoodType.length
								- i;
						priceSumForGoodType += marginalPriceForGoodType
								* weight;
						totalWeight += weight;
					}
				}

				double averagePriceForGoodType = priceSumForGoodType
						/ (double) totalWeight;
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
