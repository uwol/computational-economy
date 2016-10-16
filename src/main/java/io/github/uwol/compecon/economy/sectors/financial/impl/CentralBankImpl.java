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

package io.github.uwol.compecon.economy.sectors.financial.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import io.github.uwol.compecon.economy.bookkeeping.impl.BalanceSheetDTO;
import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.sectors.financial.BankAccount;
import io.github.uwol.compecon.economy.sectors.financial.BankAccountDelegate;
import io.github.uwol.compecon.economy.sectors.financial.BankCustomer;
import io.github.uwol.compecon.economy.sectors.financial.CentralBank;
import io.github.uwol.compecon.economy.sectors.financial.CreditBank;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.economy.sectors.financial.BankAccount.MoneyType;
import io.github.uwol.compecon.economy.sectors.financial.BankAccount.TermType;
import io.github.uwol.compecon.economy.sectors.state.State;
import io.github.uwol.compecon.economy.security.debt.FixedRateBond;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.timesystem.TimeSystemEvent;
import io.github.uwol.compecon.engine.timesystem.impl.DayType;
import io.github.uwol.compecon.engine.timesystem.impl.HourType;
import io.github.uwol.compecon.engine.timesystem.impl.MonthType;
import io.github.uwol.compecon.math.util.MathUtil;

/**
 * Agent type central bank adjusts key interest rates based on price indices.
 */
@Entity
public class CentralBankImpl extends BankImpl implements CentralBank {

	public class DailyInterestCalculationEvent implements TimeSystemEvent {
		@Override
		public boolean isDeconstructed() {
			return CentralBankImpl.this.isDeconstructed;
		}

		@Override
		public void onEvent() {
			assureBankAccountTransactions();

			final double monthlyInterestRate = MathUtil
					.calculateMonthlyNominalInterestRate(effectiveKeyInterestRate);
			final double dailyInterestRate = monthlyInterestRate / 30.0;

			for (final BankAccount bankAccount : ApplicationContext
					.getInstance().getBankAccountDAO()
					.findAllBankAccountsManagedByBank(CentralBankImpl.this)) {
				if (bankAccount.getOwner() != CentralBankImpl.this) {
					assert (CentralBankImpl.this.primaryCurrency
							.equals(bankAccount.getCurrency()));

					final double dailyInterest = bankAccount.getBalance()
							* dailyInterestRate;

					// liability account & positive interest rate or asset
					// account & negative interest rate
					if (dailyInterest > 0.0) {
						transferMoneyInternally(
								CentralBankImpl.this.bankAccountTransactions,
								bankAccount, dailyInterest);
					}
					// asset account & positive interest rate or liability
					// account & negative interest rate
					else if (dailyInterest < 0.0) {
						final double absDailyInterest = -1.0 * dailyInterest;
						transferMoneyInternally(bankAccount,
								CentralBankImpl.this.bankAccountTransactions,
								absDailyInterest);
					}
				}
			}

			// profits are transferred to the state, instead of dividends to
			// share holders etc. (seigniorage)
			if (CentralBankImpl.this.bankAccountTransactions.getBalance() > 0.0) {
				final State state = ApplicationContext.getInstance()
						.getAgentService().findState(primaryCurrency);

				CentralBankImpl.this.transferMoney(
						CentralBankImpl.this.bankAccountTransactions, state
								.getBankAccountTransactionsDelegate()
								.getBankAccount(),
						CentralBankImpl.this.bankAccountTransactions
								.getBalance(), "national interest");
			}
		}
	}

	public class KeyInterestRateCalculationEvent implements TimeSystemEvent {
		@Transient
		protected double calculateEffectiveKeyInterestRate() {
			final double targetPriceIndexForCurrentPeriod = calculateTargetPriceIndexForPeriod();
			final double currentPriceIndex = statisticalOffice.getPriceIndex();

			final double defaultEffectiveKeyInterestRate = ApplicationContext
					.getInstance().getConfiguration().centralBankConfig
					.getDefaultEffectiveKeyInterestRate();
			final double maxEffectiveKeyInterestRate = ApplicationContext
					.getInstance().getConfiguration().centralBankConfig
					.getMaxEffectiveKeyInterestRate();
			final double minEffectiveKeyInterestRate = ApplicationContext
					.getInstance().getConfiguration().centralBankConfig
					.getMinEffectiveKeyInterestRate();

			final double newEffectiveKeyInterestRate = defaultEffectiveKeyInterestRate
					+ (((currentPriceIndex - targetPriceIndexForCurrentPeriod) / currentPriceIndex) / 10.0);

			if (Double.isNaN(newEffectiveKeyInterestRate)
					|| Double.isInfinite(newEffectiveKeyInterestRate)) {
				return defaultEffectiveKeyInterestRate;
			} else {
				return Math.max(Math.min(newEffectiveKeyInterestRate,
						maxEffectiveKeyInterestRate),
						minEffectiveKeyInterestRate);
			}
		}

		@Transient
		protected double calculateTargetPriceIndexForPeriod() {
			final int yearNumber = ApplicationContext.getInstance()
					.getTimeSystem().getCurrentYear()
					- ApplicationContext.getInstance().getTimeSystem()
							.getStartYear();
			final double targetPriceLevelForYear = Math
					.pow((1.0 + ApplicationContext.getInstance()
							.getConfiguration().centralBankConfig
							.getInflationTarget()), yearNumber);

			final double monthlyNominalInflationTarget = MathUtil
					.calculateMonthlyNominalInterestRate(ApplicationContext
							.getInstance().getConfiguration().centralBankConfig
							.getInflationTarget());

			final double targetPriceLevelForMonth = Math.pow(
					1.0 + monthlyNominalInflationTarget, ApplicationContext
							.getInstance().getTimeSystem()
							.getCurrentMonthNumberInYear() - 1.0) - 1.0;
			final double targetPriceLevelForDay = (monthlyNominalInflationTarget / 30.0)
					* ApplicationContext.getInstance().getTimeSystem()
							.getCurrentDayNumberInMonth();

			final double combinedTargetPriceLevel = (targetPriceLevelForYear
					+ targetPriceLevelForMonth + targetPriceLevelForDay);
			return ApplicationContext.getInstance().getConfiguration().centralBankConfig
					.getTargetPriceIndex() * combinedTargetPriceLevel;
		}

		@Override
		public boolean isDeconstructed() {
			return CentralBankImpl.this.isDeconstructed;
		}

		@Override
		public void onEvent() {
			// calculate price index
			statisticalOffice.recalculateAveragePrices();
			statisticalOffice.recalculatePriceIndex();

			final double priceIndex = statisticalOffice.getPriceIndex();
			getLog().centralBank_PriceIndex(
					CentralBankImpl.this.primaryCurrency, priceIndex);

			// calculate key interest rate
			effectiveKeyInterestRate = calculateEffectiveKeyInterestRate();
			getLog().centralBank_KeyInterestRate(
					CentralBankImpl.this.primaryCurrency,
					effectiveKeyInterestRate);
		}
	}

	public class MarginalPriceSnapshotEvent implements TimeSystemEvent {
		@Override
		public boolean isDeconstructed() {
			return CentralBankImpl.this.isDeconstructed;
		}

		@Override
		public void onEvent() {
			statisticalOffice.takeSnapshotOfMarginalPrices();
		}
	}

	/**
	 * the statistical office takes snapshots of marginal market prices not only
	 * for the purpose of calculating the price index, but generally for
	 * offering information about markets to agents
	 */
	protected class StatisticalOffice {

		protected Map<GoodType, Double> averageMarginalPricesForGoodTypes = new HashMap<GoodType, Double>();

		protected Map<GoodType, double[]> monitoredMarginalPricesForGoodTypesAndPeriods = new HashMap<GoodType, double[]>();

		protected final int NUMBER_OF_LOGGED_PERIODS = 3;

		protected double priceIndex = 0.0;

		protected final Map<GoodType, Double> priceIndexWeights = new HashMap<GoodType, Double>();

		public StatisticalOffice() {
			/*
			 * set price index weights, must sum up to 1.0
			 */
			double priceIndexWeightSum = 0.0;

			for (final GoodType goodType : GoodType.values()) {
				final double priceIndexWeight = ApplicationContext
						.getInstance().getConfiguration().centralBankConfig.statisticalOfficeConfig
						.getPriceIndexWeight(goodType);

				priceIndexWeights.put(goodType, priceIndexWeight);
				priceIndexWeightSum += priceIndexWeight;
			}

			assert (priceIndexWeightSum == 1.0);

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
			for (final GoodType goodType : GoodType.values()) {
				monitoredMarginalPricesForGoodTypesAndPeriods.put(goodType,
						new double[NUMBER_OF_LOGGED_PERIODS
								* NUMBER_OF_MARGINAL_PRICE_SNAPSHOTS_PER_DAY]);

				averageMarginalPricesForGoodTypes.put(goodType, Double.NaN);
			}
		}

		protected double getAverageMarginalPriceForGoodType(
				final GoodType goodType) {
			return averageMarginalPricesForGoodTypes.get(goodType);
		}

		protected double getPriceIndex() {
			return priceIndex;
		}

		protected void recalculateAveragePrices() {
			// for each monitored good type
			for (final Entry<GoodType, double[]> entry : monitoredMarginalPricesForGoodTypesAndPeriods
					.entrySet()) {
				final double[] monitoredMarginalPricesForGoodType = entry
						.getValue();

				double priceSumForGoodType = 0.0;
				double totalWeight = 0;

				// recalculate average price
				for (int i = 0; i < monitoredMarginalPricesForGoodType.length; i++) {
					final double marginalPriceForGoodType = monitoredMarginalPricesForGoodType[i];

					if (marginalPriceForGoodType != 0.0
							&& !Double.isNaN(marginalPriceForGoodType)
							&& !Double.isInfinite(marginalPriceForGoodType)) {
						// weight period by age
						final double weight = monitoredMarginalPricesForGoodType.length
								- i;
						priceSumForGoodType += marginalPriceForGoodType
								* weight;
						totalWeight += weight;
					}
				}

				final double averagePriceForGoodType = priceSumForGoodType
						/ totalWeight;

				if (!Double.isNaN(totalWeight)
						&& !Double.isInfinite(totalWeight)) {
					averageMarginalPricesForGoodTypes.put(entry.getKey(),
							averagePriceForGoodType);
				}
			}
		}

		protected void recalculatePriceIndex() {
			double newPriceIndex = Double.NaN;

			// for basket of good types
			for (final Entry<GoodType, Double> entry : priceIndexWeights
					.entrySet()) {
				final GoodType goodType = entry.getKey();
				final Double weight = entry.getValue();

				// average marginal price of the good type
				final double averageMarginalPrice = averageMarginalPricesForGoodTypes
						.get(goodType);

				// add marginal price for good type to price index, weighted by
				// defined weight
				final double newPriceIndexForGoodType = weight
						* averageMarginalPrice;

				if (!Double.isNaN(newPriceIndexForGoodType)
						&& !Double.isInfinite(newPriceIndexForGoodType)) {
					if (Double.isNaN(newPriceIndex)
							|| Double.isInfinite(newPriceIndex)) {
						newPriceIndex = newPriceIndexForGoodType;
					} else {
						newPriceIndex += newPriceIndexForGoodType;
					}
				}
			}

			// store average price index
			priceIndex = newPriceIndex;
		}

		protected void takeSnapshotOfMarginalPrices() {
			// store marginal prices of monitored good types for this period
			for (final Entry<GoodType, double[]> entry : monitoredMarginalPricesForGoodTypesAndPeriods
					.entrySet()) {
				final double[] pricesForGoodType = entry.getValue();

				// fetch and store current price for this good type
				final double marginalPriceForGoodType = ApplicationContext
						.getInstance()
						.getMarketService()
						.getMarginalMarketPrice(
								CentralBankImpl.this.primaryCurrency,
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
	}

	@OneToOne(targetEntity = BankAccountImpl.class)
	@JoinColumn(name = "bankAccountCentralBankMoney_id")
	@Index(name = "IDX_A_BA_CENTRALBANKMONEY")
	// bank account for central bank money
	protected BankAccount bankAccountCentralBankMoney;

	@Transient
	protected final BankAccountDelegate bankAccountCentralBankMoneyDelegate = new BankAccountDelegate() {
		@Override
		public BankAccount getBankAccount() {
			CentralBankImpl.this.assureBankAccountCentralBankMoney();
			return bankAccountCentralBankMoney;
		}

		@Override
		public void onTransfer(final double amount) {
		}
	};

	@Column(name = "effectiveKeyInterestRate")
	protected double effectiveKeyInterestRate;

	@Transient
	protected int NUMBER_OF_MARGINAL_PRICE_SNAPSHOTS_PER_DAY;

	@Transient
	protected StatisticalOffice statisticalOffice;

	@Override
	@Transient
	protected void assertCurrencyIsOffered(final Currency currency) {
		assert (primaryCurrency == currency);
	}

	@Transient
	public void assureBankAccountCentralBankMoney() {
		if (isDeconstructed) {
			return;
		}

		if (bankAccountCentralBankMoney == null) {
			/*
			 * initialize the banks own bank account and open a customer account
			 * at this new bank, so that this bank can transfer money from its
			 * own bank account
			 */
			bankAccountCentralBankMoney = getPrimaryBank().openBankAccount(
					this, primaryCurrency, true, "central bank money",
					TermType.LONG_TERM, MoneyType.CENTRALBANK_MONEY);
		}
	}

	@Transient
	@Override
	public void assureBankAccountTransactions() {
		if (isDeconstructed) {
			return;
		}

		if (bankAccountTransactions == null) {
			/*
			 * initialize the banks own bank account and open a customer account
			 * at this new bank, so that this bank can transfer money from its
			 * own bank account
			 */
			bankAccountTransactions = getPrimaryBank().openBankAccount(this,
					primaryCurrency, true, "transactions", TermType.SHORT_TERM,
					MoneyType.DEPOSITS);
		}
	}

	@Override
	@Transient
	public void closeCustomerAccount(final BankCustomer customer) {
		assureBankAccountCentralBankMoney();

		// each customer bank account ...
		for (final BankAccount bankAccount : ApplicationContext.getInstance()
				.getBankAccountDAO().findAll(this, customer)) {
			// on closing has to be evened up to 0, so that no money is
			// lost in the monetary system
			switch (bankAccount.getMoneyType()) {
			case DEPOSITS:
				if (bankAccountTransactions != null
						&& bankAccount != bankAccountTransactions) {
					if (bankAccount.getBalance() >= 0) {
						this.transferMoney(bankAccount,
								bankAccountTransactions,
								bankAccount.getBalance(),
								"evening-up of closed bank account", true);
					} else {
						this.transferMoney(bankAccountTransactions,
								bankAccount, -1.0 * bankAccount.getBalance(),
								"evening-up of closed bank account", true);
					}
				}
				break;
			case CENTRALBANK_MONEY:
				if (bankAccountCentralBankMoney != null
						&& bankAccount != bankAccountCentralBankMoney) {

					if (bankAccount.getBalance() >= 0) {
						this.transferMoney(bankAccount,
								bankAccountCentralBankMoney,
								bankAccount.getBalance(),
								"evening-up of closed bank account", true);
					} else {
						this.transferMoney(bankAccountCentralBankMoney,
								bankAccount, -1.0 * bankAccount.getBalance(),
								"evening-up of closed bank account", true);
					}
				}
				break;
			}
			customer.onBankCloseBankAccount(bankAccount);
		}

		ApplicationContext.getInstance().getBankAccountFactory()
				.deleteAllBankAccounts(this, customer);
	}

	@Override
	public void deconstruct() {
		super.deconstruct();

		ApplicationContext.getInstance().getCentralBankFactory()
				.deleteCentralBank(this);
	}

	@Override
	@Transient
	public double getAverageMarginalPriceForGoodType(final GoodType goodType) {
		return statisticalOffice.getAverageMarginalPriceForGoodType(goodType);
	}

	public BankAccount getBankAccountCentralBankMoney() {
		return bankAccountCentralBankMoney;
	}

	@Override
	@Transient
	public BankAccountDelegate getBankAccountCentralBankMoneyDelegate() {
		return bankAccountCentralBankMoneyDelegate;
	}

	@Override
	public double getEffectiveKeyInterestRate() {
		return effectiveKeyInterestRate;
	}

	@Override
	public Currency getPrimaryCurrency() {
		return primaryCurrency;
	}

	@Override
	@Transient
	public double getReserveRatio() {
		return ApplicationContext.getInstance().getConfiguration().centralBankConfig
				.getReserveRatio();
	}

	@Override
	public void initialize() {
		super.initialize();

		// calculate interest
		final TimeSystemEvent interestCalculationEvent = new DailyInterestCalculationEvent();
		timeSystemEvents.add(interestCalculationEvent);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(interestCalculationEvent, -1, MonthType.EVERY,
						DayType.EVERY, HourType.HOUR_01);

		// take snapshots of marginal prices multiple times a day
		// -> market situation differs over the day !!!
		final TimeSystemEvent recalculateAveragePriceIndexEvent = new MarginalPriceSnapshotEvent();
		timeSystemEvents.add(recalculateAveragePriceIndexEvent);

		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(recalculateAveragePriceIndexEvent, -1,
						MonthType.EVERY, DayType.EVERY, HourType.HOUR_03);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(recalculateAveragePriceIndexEvent, -1,
						MonthType.EVERY, DayType.EVERY, HourType.HOUR_09);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(recalculateAveragePriceIndexEvent, -1,
						MonthType.EVERY, DayType.EVERY, HourType.HOUR_15);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(recalculateAveragePriceIndexEvent, -1,
						MonthType.EVERY, DayType.EVERY, HourType.HOUR_21);

		// recalculate key interest rate every day
		final TimeSystemEvent keyInterestRateCalculationEvent = new KeyInterestRateCalculationEvent();
		timeSystemEvents.add(keyInterestRateCalculationEvent);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(keyInterestRateCalculationEvent, -1, MonthType.EVERY,
						DayType.EVERY, HourType.HOUR_01);

		// count number of snapshots that are taken per day
		int numberOfSnapshotsPerDay = 0;

		for (final TimeSystemEvent event : CentralBankImpl.this.timeSystemEvents) {
			if (event instanceof MarginalPriceSnapshotEvent) {
				numberOfSnapshotsPerDay++;
			}
		}

		NUMBER_OF_MARGINAL_PRICE_SNAPSHOTS_PER_DAY = numberOfSnapshotsPerDay;

		// statistical office; has to be initialized after calculating
		// NUMBER_OF_SNAPSHOTS_PER_DAY
		statisticalOffice = new StatisticalOffice();

		effectiveKeyInterestRate = ApplicationContext.getInstance()
				.getConfiguration().centralBankConfig
				.getDefaultEffectiveKeyInterestRate();
	}

	@Override
	@Transient
	protected BalanceSheetDTO issueBalanceSheet() {
		assureBankAccountCentralBankMoney();

		final BalanceSheetDTO balanceSheet = super.issueBalanceSheet();

		// bank account for interactions with central bank money accounts of
		// credit banks
		balanceSheet.addBankAccountBalance(bankAccountCentralBankMoney);

		return balanceSheet;
	}

	@Override
	@Transient
	public void obtainTender(final BankAccount moneyReservesBankAccount,
			final List<FixedRateBond> bonds) {
		assureBankAccountCentralBankMoney();

		assertIsCustomerOfThisBank(moneyReservesBankAccount.getOwner());

		for (final FixedRateBond bond : bonds) {
			// bank money creation; fiat money!
			assert (MoneyType.CENTRALBANK_MONEY.equals(moneyReservesBankAccount
					.getMoneyType()));

			// transfer money
			moneyReservesBankAccount.deposit(bond.getFaceValue());

			// transfer bond
			ApplicationContext.getInstance().getPropertyService()
					.transferProperty(bond, bond.getOwner(), this);

			assert (bond.getOwner() == this);

			bond.setFaceValueToBankAccountDelegate(getBankAccountCentralBankMoneyDelegate());
			bond.setCouponToBankAccountDelegate(getBankAccountCentralBankMoneyDelegate());

			if (getLog().isAgentSelectedByClient(
					moneyReservesBankAccount.getOwner())) {
				getLog().log(
						moneyReservesBankAccount.getOwner(),
						"obtained a tender of %s %s of central bank money from %s",
						Currency.formatMoneySum(bond.getFaceValue()),
						getPrimaryCurrency(), this);
			}
		}
	}

	@Override
	@Transient
	public void onBankCloseBankAccount(final BankAccount bankAccount) {
		if (bankAccountCentralBankMoney != null
				&& bankAccountCentralBankMoney == bankAccount) {
			bankAccountCentralBankMoney = null;
		}

		super.onBankCloseBankAccount(bankAccount);
	}

	public void setBankAccountCentralBankMoney(
			final BankAccount bankAccountCentralBankMoney) {
		this.bankAccountCentralBankMoney = bankAccountCentralBankMoney;
	}

	public void setEffectiveKeyInterestRate(
			final double effectiveKeyInterestRate) {
		this.effectiveKeyInterestRate = effectiveKeyInterestRate;
	}

	@Override
	public void setPrimaryCurrency(final Currency primaryCurrency) {
		this.primaryCurrency = primaryCurrency;
	}

	@Override
	@Transient
	public void transferMoney(final BankAccount from, final BankAccount to,
			final double amount, final String subject) {
		this.transferMoney(from, to, amount, subject, false);
	}

	@Transient
	protected void transferMoney(final BankAccount from, final BankAccount to,
			final double amount, final String subject,
			final boolean negativeAmountOK) {

		assert (amount >= 0.0 || negativeAmountOK);
		assert (from != null);
		assert (to != null);
		assert (from != to);

		final double fromBalanceBefore = from.getBalance();
		final double toBalanceBefore = to.getBalance();

		assertIdenticalMoneyType(from, to);

		if (from.getManagingBank() instanceof CentralBankImpl
				&& to.getManagingBank() instanceof CentralBankImpl) {
			getLog().bank_onTransfer(from, to, from.getCurrency(), amount,
					subject);
			transferMoneyInternally(from, to, amount);
		} else if (from.getManagingBank() instanceof CreditBank
				&& to.getManagingBank() instanceof CentralBankImpl) {
			transferMoneyFromCreditBankAccountToCentralBankAccount(from, to,
					amount);
		} else if (from.getManagingBank() instanceof CentralBankImpl
				&& to.getManagingBank() instanceof CreditBank) {
			transferMoneyFromCentralBankAccountToCreditBankAccount(from, to,
					amount);
		} else {
			throw new RuntimeException("uncovered case");
		}

		assert (fromBalanceBefore - amount == from.getBalance());
		assert (toBalanceBefore + amount == to.getBalance());
	}

	@Transient
	private void transferMoneyFromCentralBankAccountToCreditBankAccount(
			final BankAccount from, final BankAccount to, final double amount) {
		/*
		 * Checks
		 */
		assertBankAccountIsManagedByThisBank(from);

		assert (amount >= 0.0);
		assert (from.getCurrency().equals(to.getCurrency()));

		// unusual at the central bank
		assert (from.getBalance() - amount >= 0.0 || from
				.getOverdraftPossible());

		assert (to.getManagingBank() instanceof CreditBank);

		/*
		 * Transaction
		 */
		final double fromBalanceBefore = from.getBalance();
		final double toBalanceBefore = to.getBalance();

		final CreditBank creditBank = (CreditBank) to.getManagingBank();
		from.withdraw(amount);
		creditBank.deposit(to, amount);

		assert (fromBalanceBefore - amount == from.getBalance());
		assert (toBalanceBefore + amount == to.getBalance());
	}

	@Transient
	private void transferMoneyFromCreditBankAccountToCentralBankAccount(
			final BankAccount from, final BankAccount to, final double amount) {
		/*
		 * Checks
		 */
		assertBankAccountIsManagedByThisBank(to);

		assert (amount >= 0);
		assert (from.getCurrency().equals(to.getCurrency()));
		// unusual at the central bank
		assert (from.getBalance() - amount >= 0 || from.getOverdraftPossible());
		assert (from.getManagingBank() instanceof CreditBank);

		/*
		 * Transaction
		 */
		final double fromBalanceBefore = from.getBalance();
		final double toBalanceBefore = to.getBalance();

		final CreditBank creditBank = (CreditBank) from.getManagingBank();
		creditBank.withdraw(from, amount);
		to.deposit(amount);

		assert (fromBalanceBefore - amount == from.getBalance());
		assert (toBalanceBefore + amount == to.getBalance());
	}

	@Transient
	private void transferMoneyInternally(final BankAccount from,
			final BankAccount to, final double amount) {
		assertBankAccountIsManagedByThisBank(from);
		assertBankAccountIsManagedByThisBank(to);

		assert (amount >= 0);
		assert (from.getCurrency().equals(to.getCurrency()));
		// unusual at the central bank
		assert (from.getBalance() - amount >= 0 || from.getOverdraftPossible());

		final double fromBalanceBefore = from.getBalance();
		final double toBalanceBefore = to.getBalance();

		// transfer money internally
		from.withdraw(amount);
		to.deposit(amount);

		// from and to can be the same
		assert (MathUtil.equal(fromBalanceBefore + toBalanceBefore,
				from.getBalance() + to.getBalance()));
	}
}
