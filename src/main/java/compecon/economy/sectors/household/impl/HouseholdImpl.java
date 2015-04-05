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

package compecon.economy.sectors.household.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.economy.agent.impl.AgentImpl;
import compecon.economy.behaviour.PricingBehaviour;
import compecon.economy.behaviour.impl.PricingBehaviourImpl;
import compecon.economy.bookkeeping.impl.BalanceSheetDTO;
import compecon.economy.materia.GoodType;
import compecon.economy.materia.Refreshable;
import compecon.economy.property.Property;
import compecon.economy.property.PropertyOwner;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.financial.impl.BankAccountImpl;
import compecon.economy.sectors.household.Household;
import compecon.economy.security.equity.Share;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.timesystem.TimeSystemEvent;
import compecon.engine.timesystem.impl.DayType;
import compecon.engine.timesystem.impl.MonthType;
import compecon.math.intertemporal.IntertemporalConsumptionFunction;
import compecon.math.intertemporal.impl.IrvingFisherIntertemporalConsumptionFunction.Period;
import compecon.math.price.PriceFunction;
import compecon.math.util.MathUtil;
import compecon.math.utility.UtilityFunction;

/**
 * Agent type Household offers labour hours and consumes goods.
 */
@Entity
public class HouseholdImpl extends AgentImpl implements Household {

	// configuration constants ------------------------------

	public class DailyLifeEvent implements TimeSystemEvent {

		private double buyGoods(final Map<GoodType, Double> goodsToBuy,
				final Map<GoodType, PriceFunction> priceFunctions,
				final double budget) {
			/*
			 * buy production factors; maxPricePerUnit is significantly
			 * important for price equilibrium
			 */
			double budgetSpent = 0.0;

			for (final Entry<GoodType, Double> entry : goodsToBuy.entrySet()) {
				final GoodType goodTypeToBuy = entry.getKey();
				final double amountToBuy = entry.getValue();

				if (MathUtil.greater(amountToBuy, 0.0)) {
					final double marginalPrice = priceFunctions.get(
							goodTypeToBuy).getMarginalPrice(0.0);

					// maxPricePerUnit is significantly important for price
					// equilibrium; also budget, as in the depth of the markets,
					// prices can rise, leading to overspending
					final double[] priceAndAmount = ApplicationContext
							.getInstance()
							.getMarketService()
							.buy(goodTypeToBuy,
									amountToBuy,
									budget,
									marginalPrice
											* ApplicationContext.getInstance()
													.getConfiguration().householdConfig
													.getMaxPricePerUnitMultiplier(),
									HouseholdImpl.this,
									getBankAccountTransactionsDelegate());
					budgetSpent += priceAndAmount[0];
				}
			}

			return budgetSpent;
		}

		protected double buyOptimalGoodsForBudget(final double budget) {
			double numberOfLabourHoursToConsume = 0.0;

			if (MathUtil.greater(budget, 0.0)) {
				// get prices for good types
				final Map<GoodType, PriceFunction> priceFunctions = ApplicationContext
						.getInstance()
						.getMarketService()
						.getFixedPriceFunctions(
								HouseholdImpl.this.primaryCurrency,
								utilityFunction.getInputGoodTypes());

				// calculate optimal consumption plan
				getLog().setAgentCurrentlyActive(HouseholdImpl.this);
				final Map<GoodType, Double> utilityMaximizingGoodsBundle = utilityFunction
						.calculateUtilityMaximizingInputs(priceFunctions,
								budget);
				numberOfLabourHoursToConsume = utilityMaximizingGoodsBundle
						.get(GoodType.LABOURHOUR);

				// no labour hours should be bought on markets
				utilityMaximizingGoodsBundle.remove(GoodType.LABOURHOUR);

				// buy goods
				final double budgetSpent = buyGoods(
						utilityMaximizingGoodsBundle, priceFunctions, budget);

				assert (MathUtil.lesserEqual(budgetSpent, budget * 1.1));
			}

			return numberOfLabourHoursToConsume;
		}

		protected void buyShares() {
			/*
			 * buy shares / capital -> equity savings
			 */
			ApplicationContext
					.getInstance()
					.getMarketService()
					.buy(Share.class, 1.0, 0.0, 0.0, HouseholdImpl.this,
							getBankAccountTransactionsDelegate());
		}

		protected void checkCallDestructor() {
			/*
			 * potentially, call destructor
			 */
			if (daysWithoutUtility > DAYS_WITHOUT_UTILITY_UNTIL_DESTRUCTOR) {
				if (!ApplicationContext.getInstance().getTimeSystem()
						.isInitializationPhase()) {
					deconstruct();
				}
			}
		}

		protected void checkDeriveNewHousehold() {
			/*
			 * potentially, derive new household
			 */
			final int NEW_HOUSEHOLD_FROM_X_DAYS = ApplicationContext
					.getInstance().getConfiguration().householdConfig
					.getNewHouseholdFromAgeInDays();

			if (ageInDays >= NEW_HOUSEHOLD_FROM_X_DAYS) {
				if ((ageInDays - NEW_HOUSEHOLD_FROM_X_DAYS)
						% ApplicationContext.getInstance().getConfiguration().householdConfig
								.getNewHouseholdEveryXDays() == 0) {
					ApplicationContext
							.getInstance()
							.getHouseholdFactory()
							.newInstanceHousehold(
									HouseholdImpl.this.primaryCurrency, 0);
				}
			}
		}

		protected void checkRequiredUtilityPerDay(final double utility) {
			/*
			 * check for required utility
			 */
			if (utility < ApplicationContext.getInstance().getConfiguration().householdConfig
					.getRequiredUtilityPerDay()) {
				daysWithoutUtility++;
				continuousDaysWithUtility = 0;
				if (getLog().isAgentSelectedByClient(HouseholdImpl.this)) {
					getLog().log(
							HouseholdImpl.this,
							DailyLifeEvent.class,
							"does not have required utility of %s",
							ApplicationContext.getInstance().getConfiguration().householdConfig
									.getRequiredUtilityPerDay());
				}
			} else {
				if (daysWithoutUtility > 0) {
					daysWithoutUtility--;
				}
				continuousDaysWithUtility++;
			}
		}

		protected double consumeGoods(final double numberOfLabourHoursToConsume) {
			/*
			 * consume goods
			 */
			final Map<GoodType, Double> effectiveConsumptionGoodsBundle = new HashMap<GoodType, Double>();

			for (final GoodType goodType : utilityFunction.getInputGoodTypes()) {
				// only non-durable consumption goods should be consumed
				assert (!goodType.isDurable());

				final double balance = ApplicationContext.getInstance()
						.getPropertyService()
						.getGoodTypeBalance(HouseholdImpl.this, goodType);
				final double amountToConsume;

				if (GoodType.LABOURHOUR.equals(goodType)) {
					amountToConsume = Math.min(numberOfLabourHoursToConsume,
							balance);
				} else {
					amountToConsume = balance;
				}

				effectiveConsumptionGoodsBundle.put(goodType, amountToConsume);
				ApplicationContext
						.getInstance()
						.getPropertyService()
						.decrementGoodTypeAmount(HouseholdImpl.this, goodType,
								amountToConsume);
			}

			final double utility = utilityFunction
					.calculateUtility(effectiveConsumptionGoodsBundle);
			getLog().household_onUtility(HouseholdImpl.this,
					HouseholdImpl.this.bankAccountTransactions.getCurrency(),
					effectiveConsumptionGoodsBundle, utility);
			return utility;
		}

		@Override
		public boolean isDeconstructed() {
			return HouseholdImpl.this.isDeconstructed;
		}

		protected void offerLabourHours() {
			/*
			 * remove labour hour offers
			 */
			ApplicationContext
					.getInstance()
					.getMarketService()
					.removeAllSellingOffers(
							HouseholdImpl.this,
							HouseholdImpl.this.bankAccountTransactions
									.getCurrency(), GoodType.LABOURHOUR);

			// if not retired
			if (ageInDays < ApplicationContext.getInstance().getConfiguration().householdConfig
					.getRetirementAgeInDays()) {
				/*
				 * offer labour hours
				 */
				final double amountOfLabourHours = ApplicationContext
						.getInstance()
						.getPropertyService()
						.getGoodTypeBalance(HouseholdImpl.this,
								GoodType.LABOURHOUR);
				final double prices[] = pricingBehaviour.getCurrentPriceArray();

				for (final double price : prices) {
					ApplicationContext
							.getInstance()
							.getMarketService()
							.placeSellingOffer(GoodType.LABOURHOUR,
									HouseholdImpl.this,
									getBankAccountTransactionsDelegate(),
									amountOfLabourHours / (prices.length),
									price);
				}
				pricingBehaviour.registerOfferedAmount(amountOfLabourHours);

				getLog().household_onOfferResult(
						HouseholdImpl.this.primaryCurrency,
						pricingBehaviour.getLastOfferedAmount(),
						ApplicationContext.getInstance().getConfiguration().householdConfig
								.getNumberOfLabourHoursPerDay());
			}
		}

		@Override
		public void onEvent() {
			assert (!HouseholdImpl.this.isDeconstructed);

			/*
			 * potentially call destructor
			 */
			if (ageInDays > ApplicationContext.getInstance().getConfiguration().householdConfig
					.getLifespanInDays()) {
				deconstruct();
				return;
			}

			getLog().household_AmountSold(HouseholdImpl.this.primaryCurrency,
					pricingBehaviour.getLastSoldAmount());

			/*
			 * simulation mechanics
			 */
			ageInDays++;
			labourPower.refresh();
			pricingBehaviour.nextPeriod();

			assureBankAccountTransactions();
			assureBankAccountSavings();

			/*
			 * economic actions
			 */
			final double budget = saveMoney();

			final double numberOfLabourHoursToConsume = buyOptimalGoodsForBudget(budget);

			final double utility = consumeGoods(numberOfLabourHoursToConsume);

			offerLabourHours();

			buyShares();

			// households make no debt; safety epsilon due to iterative
			// deviations
			assert (MathUtil.greaterEqual(
					HouseholdImpl.this.bankAccountTransactions.getBalance(),
					0.0));
			assert (MathUtil.greaterEqual(bankAccountSavings.getBalance(), 0.0));

			checkRequiredUtilityPerDay(utility);

			checkDeriveNewHousehold();

			checkCallDestructor();
		}

		protected double saveMoney() {
			/*
			 * calculate budget
			 */
			final double keyInterestRate = ApplicationContext.getInstance()
					.getAgentService()
					.findCentralBank(HouseholdImpl.this.primaryCurrency)
					.getEffectiveKeyInterestRate();
			final double income = HouseholdImpl.this.bankAccountTransactions
					.getBalance();

			final double budget;

			// do households save for retirement?
			if (ApplicationContext.getInstance().getConfiguration().householdConfig
					.getRetirementSaving()) {
				final Map<Period, Double> intertemporalConsumptionPlan = intertemporalConsumptionFunction
						.calculateUtilityMaximizingConsumptionPlan(income,
								bankAccountSavings.getBalance(),
								keyInterestRate, ageInDays,
								ApplicationContext.getInstance()
										.getConfiguration().householdConfig
										.getLifespanInDays(),
								ApplicationContext.getInstance()
										.getConfiguration().householdConfig
										.getRetirementAgeInDays());
				budget = intertemporalConsumptionPlan.get(Period.CURRENT);
			} else {
				budget = income;
			}

			final double moneySumToSave = income - budget;
			final double moneySumToConsume = budget;

			/*
			 * logging
			 */
			getLog().household_onIncomeWageDividendTransfersConsumptionSaving(
					primaryCurrency, income, moneySumToConsume, moneySumToSave,
					pricingBehaviour.getLastSoldValue(),
					dividendSinceLastPeriod, governmentTransfersSinceLastPeriod);

			dividendSinceLastPeriod = 0;
			governmentTransfersSinceLastPeriod = 0;

			if (moneySumToSave > 0.0) {
				/*
				 * save money for retirement
				 */
				HouseholdImpl.this.bankAccountTransactions.getManagingBank()
						.transferMoney(
								HouseholdImpl.this.bankAccountTransactions,
								bankAccountSavings, moneySumToSave,
								"retirement savings");
				if (getLog().isAgentSelectedByClient(HouseholdImpl.this)) {
					getLog().log(
							HouseholdImpl.this,
							DailyLifeEvent.class,
							"saving %s %s of %s %s income",
							Currency.formatMoneySum(moneySumToSave),
							HouseholdImpl.this.bankAccountTransactions
									.getCurrency(),
							Currency.formatMoneySum(HouseholdImpl.this.bankAccountTransactions
									.getBalance()),
							HouseholdImpl.this.bankAccountTransactions
									.getCurrency());
				}
			} else if (moneySumToSave < 0.0) {
				/*
				 * dissavings can happen also when not retired, yet, in case of
				 * no income due to average conmption premise (e. g. Modigliani
				 * intertemporal consumption function)
				 */

				getLog().household_onRetired(HouseholdImpl.this);

				/*
				 * spend saved retirement money
				 */
				HouseholdImpl.this.bankAccountTransactions.getManagingBank()
						.transferMoney(bankAccountSavings,
								HouseholdImpl.this.bankAccountTransactions,
								-1.0 * moneySumToSave, "retirement dissavings");
				if (getLog().isAgentSelectedByClient(HouseholdImpl.this)) {
					getLog().log(HouseholdImpl.this, "unsaving %s %s",
							Currency.formatMoneySum(-1.0 * moneySumToSave),
							bankAccountSavings.getCurrency());
				}
			}

			return budget;
		}
	}

	// dynamic state ------------------------------

	protected class LabourPower implements Refreshable {

		@Override
		public void exhaust() {
			ApplicationContext
					.getInstance()
					.getPropertyService()
					.resetGoodTypeAmount(HouseholdImpl.this,
							GoodType.LABOURHOUR);
		}

		public double getNumberOfLabourHoursAvailable() {
			return ApplicationContext
					.getInstance()
					.getPropertyService()
					.getGoodTypeBalance(HouseholdImpl.this, GoodType.LABOURHOUR);
		}

		@Override
		public boolean isExhausted() {
			return ApplicationContext
					.getInstance()
					.getPropertyService()
					.getGoodTypeBalance(HouseholdImpl.this, GoodType.LABOURHOUR) <= 0;
		}

		@Override
		public void refresh() {
			exhaust();
			ApplicationContext
					.getInstance()
					.getPropertyService()
					.incrementGoodTypeAmount(
							HouseholdImpl.this,
							GoodType.LABOURHOUR,
							ApplicationContext.getInstance().getConfiguration().householdConfig
									.getNumberOfLabourHoursPerDay());
		}
	}

	@Column(name = "ageInDays")
	protected int ageInDays = 0;

	@Transient
	protected final BankAccountDelegate bankAccountDividendDelegate = new BankAccountDelegate() {
		@Override
		public BankAccount getBankAccount() {
			HouseholdImpl.this.assureBankAccountTransactions();
			return HouseholdImpl.this.bankAccountTransactions;
		}

		@Override
		public void onTransfer(final double amount) {
			dividendSinceLastPeriod += amount;
		}
	};

	@Transient
	protected final BankAccountDelegate bankAccountGovernmentTransfersDelegate = new BankAccountDelegate() {
		@Override
		public BankAccount getBankAccount() {
			HouseholdImpl.this.assureBankAccountTransactions();
			return HouseholdImpl.this.bankAccountTransactions;
		}

		@Override
		public void onTransfer(final double amount) {
			governmentTransfersSinceLastPeriod += amount;
		}
	};

	@OneToOne(targetEntity = BankAccountImpl.class)
	@JoinColumn(name = "bankAccountSavings_id")
	@Index(name = "IDX_A_BA_SAVINGS")
	// bank account for savings
	protected BankAccount bankAccountSavings;

	@Column(name = "continuousDaysWithUtility")
	protected int continuousDaysWithUtility = 0;

	@Transient
	protected int DAYS_WITHOUT_UTILITY_UNTIL_DESTRUCTOR;

	@Column(name = "daysWithoutUtility")
	protected int daysWithoutUtility = 0;

	@Transient
	protected double dividendSinceLastPeriod;

	@Transient
	protected double governmentTransfersSinceLastPeriod;

	@Transient
	protected IntertemporalConsumptionFunction intertemporalConsumptionFunction;

	@Transient
	protected LabourPower labourPower = new LabourPower();

	@Transient
	protected PricingBehaviour pricingBehaviour;

	@Transient
	protected UtilityFunction utilityFunction;

	/*
	 * accessors
	 */

	@Transient
	protected void assureBankAccountSavings() {
		if (isDeconstructed) {
			return;
		}

		// initialize bank account
		if (bankAccountSavings == null) {
			bankAccountSavings = getPrimaryBank().openBankAccount(this,
					primaryCurrency, false, "savings", TermType.LONG_TERM,
					MoneyType.DEPOSITS);
		}
	}

	@Transient
	protected void assureDividendBankAccount() {
		assureBankAccountTransactions();
	}

	@Override
	public void deconstruct() {
		super.deconstruct();

		ApplicationContext.getInstance().getHouseholdFactory()
				.deleteHousehold(this);
	}

	@Override
	public int getAgeInDays() {
		return ageInDays;
	}

	@Transient
	public BankAccountDelegate getBankAccountDividendDelegate() {
		return bankAccountDividendDelegate;
	}

	@Override
	@Transient
	public BankAccountDelegate getBankAccountGovernmentTransfersDelegate() {
		return bankAccountGovernmentTransfersDelegate;
	}

	public BankAccount getBankAccountSavings() {
		return bankAccountSavings;
	}

	@Override
	public int getContinuousDaysWithUtility() {
		return continuousDaysWithUtility;
	}

	@Override
	public int getDaysWithoutUtility() {
		return daysWithoutUtility;
	}

	public IntertemporalConsumptionFunction getIntertemporalConsumptionFunction() {
		return intertemporalConsumptionFunction;
	}

	public PricingBehaviour getPricingBehaviour() {
		return pricingBehaviour;
	}

	public UtilityFunction getUtilityFunction() {
		return utilityFunction;
	}

	@Override
	public void initialize() {
		super.initialize();

		DAYS_WITHOUT_UTILITY_UNTIL_DESTRUCTOR = ApplicationContext
				.getInstance().getConfiguration().householdConfig
				.getDaysWithoutUtilityUntilDestructor()
				+ hashCode()
				% ApplicationContext.getInstance().getConfiguration().householdConfig
						.getDaysWithoutUtilityUntilDestructor();

		// daily life at random HourType
		final TimeSystemEvent dailyLifeEvent = new DailyLifeEvent();
		timeSystemEvents.add(dailyLifeEvent);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(
						dailyLifeEvent,
						-1,
						MonthType.EVERY,
						DayType.EVERY,
						ApplicationContext.getInstance().getTimeSystem()
								.suggestRandomHourType());

		final double marketPrice = ApplicationContext.getInstance()
				.getMarketService()
				.getMarginalMarketPrice(primaryCurrency, GoodType.LABOURHOUR);
		pricingBehaviour = new PricingBehaviourImpl(this, GoodType.LABOURHOUR,
				primaryCurrency, marketPrice);
		labourPower.refresh();
	}

	@Override
	@Transient
	protected BalanceSheetDTO issueBalanceSheet() {
		assureBankAccountSavings();

		final BalanceSheetDTO balanceSheet = super.issueBalanceSheet();

		// retirement savings
		balanceSheet.addBankAccountBalance(bankAccountSavings);

		return balanceSheet;
	}

	/*
	 * assertions
	 */

	@Override
	@Transient
	public void onBankCloseBankAccount(final BankAccount bankAccount) {
		if (bankAccountSavings != null && bankAccountSavings == bankAccount) {
			bankAccountSavings = null;
		}

		super.onBankCloseBankAccount(bankAccount);
	}

	@Override
	public void onMarketSettlement(final Currency commodityCurrency,
			final double amount, final double pricePerUnit,
			final Currency currency) {
	}

	/*
	 * business logic
	 */

	@Override
	public void onMarketSettlement(final GoodType goodType,
			final double amount, final double pricePerUnit,
			final Currency currency) {
		if (GoodType.LABOURHOUR.equals(goodType)) {
			HouseholdImpl.this.pricingBehaviour.registerSelling(amount, amount
					* pricePerUnit);
		}
	}

	@Override
	public void onMarketSettlement(final Property property,
			final double pricePerUnit, final Currency currency) {
	}

	@Override
	@Transient
	public void onPropertyTransferred(final Property property,
			final PropertyOwner oldOwner, final PropertyOwner newOwner) {
		super.onPropertyTransferred(property, oldOwner, newOwner);

		if (newOwner == this && property instanceof Share) {
			final Share share = (Share) property;
			share.setDividendBankAccountDelegate(getBankAccountDividendDelegate());

			/*
			 * check that shares have correct bank account delegate, and sell
			 * shares that are denominated in an incorrect currency
			 */
			if (!HouseholdImpl.this.primaryCurrency.equals(share.getIssuer()
					.getPrimaryCurrency())) {
				ApplicationContext
						.getInstance()
						.getMarketService()
						.placeSellingOffer(share, HouseholdImpl.this,
								getBankAccountTransactionsDelegate(), 0.0);
			}
		}
	}

	public void setAgeInDays(final int ageInDays) {
		this.ageInDays = ageInDays;
	}

	public void setBankAccountSavings(final BankAccount bankAccountSavings) {
		this.bankAccountSavings = bankAccountSavings;
	}

	public void setContinuousDaysWithUtility(final int continuousDaysWithUtility) {
		this.continuousDaysWithUtility = continuousDaysWithUtility;
	}

	public void setDaysWithoutUtility(final int daysWithoutUtility) {
		this.daysWithoutUtility = daysWithoutUtility;
	}

	public void setIntertemporalConsumptionFunction(
			final IntertemporalConsumptionFunction intertemporalConsumptionFunction) {
		this.intertemporalConsumptionFunction = intertemporalConsumptionFunction;
	}

	public void setPricingBehaviour(final PricingBehaviour pricingBehaviour) {
		this.pricingBehaviour = pricingBehaviour;
	}

	public void setUtilityFunction(final UtilityFunction utilityFunction) {
		this.utilityFunction = utilityFunction;
	}

	@Override
	public String toString() {
		return super.toString() + ", ageInYears=[" + ageInDays / 365 + "]";
	}
}
