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

package compecon.culture.sectors.household;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import compecon.culture.EconomicalBehaviour;
import compecon.culture.markets.SettlementMarket.ISettlementEvent;
import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.property.Property;
import compecon.culture.sectors.state.law.property.PropertyRegister;
import compecon.culture.sectors.state.law.security.equity.IShareOwner;
import compecon.culture.sectors.state.law.security.equity.Share;
import compecon.engine.Agent;
import compecon.engine.AgentFactory;
import compecon.engine.Log;
import compecon.engine.MarketFactory;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.TimeSystem;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.MonthType;
import compecon.nature.materia.GoodType;
import compecon.nature.materia.Refreshable;
import compecon.nature.math.utility.IUtilityFunction;

/**
 * Agent type Household offers labour hours and consumes goods.
 */
@Entity
public class Household extends Agent implements IShareOwner {

	// configuration constants
	@Transient
	protected final int NEW_HOUSEHOLD_FROM_X_DAYS = 360 + this.hashCode() % 360;

	@Transient
	protected final int NEW_HOUSEHOLD_EVERY_X_DAYS = 360;

	@Transient
	protected final int LIFESPAN = 360 * 10;

	// maxCredit limits the demand for money when buying production input
	// factors, thus limiting M1 in the monetary system
	@Transient
	protected final int MAX_CREDIT = 0;

	@Transient
	protected final int DAYS_WITHOUT_UTILITY_UNTIL_DESTRUCTOR = 14 + this
			.hashCode() % 14;

	@Transient
	protected final double REQUIRED_UTILITY = 1.0;

	// state
	@Column(name = "ageInDays")
	protected int ageInDays = 0;

	@Column(name = "daysWithoutUtility")
	protected int daysWithoutUtility = 0;

	@Column(name = "continuousDaysWithUtility")
	protected int continuousDaysWithUtility = 0;

	@Transient
	protected LabourPower labourPower = new LabourPower();

	@Transient
	protected EconomicalBehaviour economicalBehaviour;

	@Transient
	protected IUtilityFunction utilityFunction;

	@Override
	public void initialize() {
		super.initialize();

		// daily life at random HourType
		ITimeSystemEvent dailyLifeEvent = new DailyLifeEvent();
		this.timeSystemEvents.add(dailyLifeEvent);
		TimeSystem.getInstance()
				.addEvent(dailyLifeEvent, -1, MonthType.EVERY, DayType.EVERY,
						TimeSystem.getInstance().suggestRandomHourType());

		// balance sheet publication
		ITimeSystemEvent balanceSheetPublicationEvent = new BalanceSheetPublicationEvent();
		this.timeSystemEvents.add(balanceSheetPublicationEvent);
		TimeSystem.getInstance().addEvent(balanceSheetPublicationEvent, -1,
				MonthType.EVERY, DayType.EVERY,
				BALANCE_SHEET_PUBLICATION_HOUR_TYPE);

		this.economicalBehaviour = new EconomicalBehaviour(this,
				GoodType.LABOURHOUR, this.primaryCurrency);
	}

	/*
	 * accessors
	 */

	public int getAgeInDays() {
		return ageInDays;
	}

	public int getDaysWithoutUtility() {
		return daysWithoutUtility;
	}

	public int getContinuousDaysWithUtility() {
		return continuousDaysWithUtility;
	}

	public void setAgeInDays(int ageInDays) {
		this.ageInDays = ageInDays;
	}

	public void setDaysWithoutUtility(int daysWithoutUtility) {
		this.daysWithoutUtility = daysWithoutUtility;
	}

	public void setContinuousDaysWithUtility(int continuousDaysWithUtility) {
		this.continuousDaysWithUtility = continuousDaysWithUtility;
	}

	/*
	 * business logic
	 */

	public void setUtilityFunction(IUtilityFunction utilityFunction) {
		this.utilityFunction = utilityFunction;
	}

	@Override
	@Transient
	public BankAccount getDividendBankAccount(Currency currency) {
		this.assertTransactionsBankAccount();
		if (this.transactionsBankAccount.getCurrency() == currency)
			return this.transactionsBankAccount;
		return null;
	}

	protected class SettlementMarketEvent implements ISettlementEvent {
		@Override
		public void onEvent(GoodType goodType, double amount,
				double pricePerUnit, Currency currency) {
			Household.this.assertTransactionsBankAccount();
			if (goodType.equals(GoodType.LABOURHOUR)) {
				Household.this.economicalBehaviour.registerSelling(amount);

				/*
				 * no exhaust of labour hours, as the offered labour hours have
				 * been transfered by the SettelementMarket via PropertyRegister
				 * and will be exhausted by the buyer
				 */
			}
		}

		@Override
		public void onEvent(Property property, double pricePerUnit,
				Currency currency) {
		}
	}

	protected class BalanceSheetPublicationEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			Household.this.assertTransactionsBankAccount();
			Log.agent_onPublishBalanceSheet(Household.this,
					Household.this.issueBasicBalanceSheet());
		}
	}

	protected class DailyLifeEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			if (Household.this.isDeconstructed)
				throw new RuntimeException(Household.this
						+ " is deconstructed, but not removed from TimeSystem");

			Household.this.assertTransactionsBankAccount();

			/*
			 * simulation mechanics
			 */
			Household.this.ageInDays++;
			Household.this.labourPower.refresh();
			Household.this.economicalBehaviour.nextPeriod();

			/*
			 * maximize utility
			 */
			Map<GoodType, Double> prices = MarketFactory.getInstance()
					.getMarginalPrices(
							Household.this.transactionsBankAccount
									.getCurrency());
			double transmissionBasedBudget = Household.this.economicalBehaviour
					.getBudgetingBehaviour()
					.calculateTransmissionBasedBudgetForPeriod(
							Household.this.transactionsBankAccount
									.getCurrency(),
							Household.this.transactionsBankAccount.getBalance(),
							Household.this.MAX_CREDIT);
			Map<GoodType, Double> optimalBundleOfGoods = Household.this.utilityFunction
					.calculateUtilityMaximizingInputsUnderBudgetRestriction(
							prices, transmissionBasedBudget);

			/*
			 * buy goods
			 */
			for (Entry<GoodType, Double> entry : optimalBundleOfGoods
					.entrySet()) {
				GoodType goodType = entry.getKey();
				if (!GoodType.LABOURHOUR.equals(goodType)) {
					double maxAmount = entry.getValue();
					double maxTotalPrice = Household.this.transactionsBankAccount
							.getBalance();
					if (!Double.isInfinite(maxAmount)) {
						maxTotalPrice = Math.min(
								maxAmount * prices.get(goodType),
								Household.this.transactionsBankAccount
										.getBalance());
					}
					MarketFactory.getInstance().buy(
							goodType,
							Household.this.transactionsBankAccount
									.getCurrency(),
							maxAmount,
							maxTotalPrice,
							-1,
							Household.this,
							Household.this.transactionsBankAccount,
							Household.this.bankPasswords
									.get(Household.this.transactionsBankAccount
											.getManagingBank()));
				}
			}

			/*
			 * consume goods
			 */
			Map<GoodType, Double> bundleOfGoodsToConsume = new HashMap<GoodType, Double>();
			for (GoodType goodType : Household.this.utilityFunction
					.getInputGoodTypes()) {
				double balance = PropertyRegister.getInstance().getBalance(
						Household.this, goodType);
				double amountToConsume = Math.min(balance,
						optimalBundleOfGoods.get(goodType));
				bundleOfGoodsToConsume.put(goodType, amountToConsume);
				PropertyRegister.getInstance().decrement(Household.this,
						goodType, amountToConsume);
			}
			double utility = Household.this.utilityFunction
					.calculateUtility(bundleOfGoodsToConsume);
			Log.household_onUtility(Household.this,
					Household.this.transactionsBankAccount.getCurrency(),
					bundleOfGoodsToConsume, utility);

			/*
			 * buy shares / capital -> equity savings
			 */
			MarketFactory.getInstance().buy(
					Share.class,
					Household.this.transactionsBankAccount.getCurrency(),
					1,
					0,
					0,
					Household.this,
					Household.this.transactionsBankAccount,
					Household.this.bankPasswords
							.get(Household.this.primaryBank));

			/*
			 * check for required utility
			 */
			if (utility < Household.this.REQUIRED_UTILITY) {
				Household.this.daysWithoutUtility++;
				Household.this.continuousDaysWithUtility = 0;
				Log.household_NotEnoughUtility(Household.this,
						Household.this.REQUIRED_UTILITY);
			} else {
				if (Household.this.daysWithoutUtility > 0)
					daysWithoutUtility--;
				Household.this.continuousDaysWithUtility++;
			}

			/*
			 * potentially, derive new household
			 */
			if (Household.this.ageInDays >= NEW_HOUSEHOLD_FROM_X_DAYS) {
				if ((Household.this.ageInDays - NEW_HOUSEHOLD_FROM_X_DAYS)
						% NEW_HOUSEHOLD_EVERY_X_DAYS == 0) {
					AgentFactory.newInstanceHousehold();
				}
			}

			/*
			 * offer remaining labour hours
			 */
			Household.this.economicalBehaviour.getPricingBehaviour()
					.setNewPrice();
			MarketFactory.getInstance().removeAllSellingOffers(Household.this,
					Household.this.transactionsBankAccount.getCurrency(),
					GoodType.LABOURHOUR);
			double amountOfLabourHours = PropertyRegister.getInstance()
					.getBalance(Household.this, GoodType.LABOURHOUR);
			MarketFactory.getInstance().placeSettlementSellingOffer(
					GoodType.LABOURHOUR,
					Household.this,
					Household.this.transactionsBankAccount,
					amountOfLabourHours,
					Household.this.economicalBehaviour.getPricingBehaviour()
							.getCurrentPrice(),
					Household.this.transactionsBankAccount.getCurrency(),
					new SettlementMarketEvent());
			Household.this.economicalBehaviour
					.registerOfferedAmount(amountOfLabourHours);

			/*
			 * potentially, call destructor
			 */
			if (Household.this.daysWithoutUtility > Household.this.DAYS_WITHOUT_UTILITY_UNTIL_DESTRUCTOR
					|| Household.this.ageInDays > LIFESPAN)
				Household.this.deconstruct();
		}
	}

	protected class LabourPower implements Refreshable {
		protected final int NUMBER_OF_DAILY_LABOUR_HOURS = 16;

		public LabourPower() {
			this.refresh();
		}

		public double getNumberOfLabourHoursAvailable() {
			return PropertyRegister.getInstance().getBalance(Household.this,
					GoodType.LABOURHOUR);
		}

		@Override
		public boolean isExhausted() {
			return PropertyRegister.getInstance().getBalance(Household.this,
					GoodType.LABOURHOUR) <= 0;
		}

		@Override
		public void exhaust() {
			PropertyRegister.getInstance().reset(Household.this,
					GoodType.LABOURHOUR);
		}

		@Override
		public void refresh() {
			exhaust();
			PropertyRegister.getInstance().increment(Household.this,
					GoodType.LABOURHOUR, this.NUMBER_OF_DAILY_LABOUR_HOURS);
			Log.household_LabourHourCapacity(Household.this,
					this.NUMBER_OF_DAILY_LABOUR_HOURS);
		}
	}
}
