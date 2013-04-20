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

package compecon.culture.sectors.agriculture;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import compecon.culture.EconomicalBehaviour;
import compecon.culture.markets.PrimaryMarket;
import compecon.culture.markets.SettlementMarket.ISettlementEvent;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.culture.sectors.state.law.property.IProperty;
import compecon.culture.sectors.state.law.property.PropertyRegister;
import compecon.culture.sectors.state.law.security.equity.JointStockCompany;
import compecon.engine.Log;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.TimeSystem;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.HourType;
import compecon.engine.time.calendar.MonthType;
import compecon.nature.materia.GoodType;
import compecon.nature.materia.Refreshable;
import compecon.nature.production.CompositeProductionFunction;
import compecon.nature.production.LinearProductionFunction;

/**
 * Agent type farm produces mega calories by combining the production factors
 * acre and labour hour.
 */
@Entity
@Table(name = "Farm")
public class Farm extends JointStockCompany {

	// maxCredit limits the demand for money when buying production input
	// factors, thus limiting M1 in the monetary system
	@Transient
	protected final int MAX_CREDIT = 10000;

	// state of farm
	@Transient
	protected SortedSet<Acre> acres = new TreeSet<Acre>();

	@Transient
	protected CompositeProductionFunction compositeProductionFunction;

	@Transient
	protected EconomicalBehaviour economicalBehaviour;

	public void initialize() {
		super.initialize();

		this.compositeProductionFunction = new CompositeProductionFunction();

		// acres
		int landSize = 10;
		for (int i = 0; i < landSize; i++) {
			Acre acre = new Acre(i * 5);
			this.acres.add(acre);
			this.compositeProductionFunction
					.addProductionFunction(acre.productionFunction);
		}

		// harvest at random HourType
		ITimeSystemEvent productionEvent = new ProductionEvent();
		this.timeSystemEvents.add(productionEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(productionEvent,
				-1, MonthType.EVERY, DayType.EVERY,
				TimeSystem.getInstance().suggestRandomHourType());

		// refresh acres at 00:00
		ITimeSystemEvent acreRefreshEvent = new AcreRefreshEvent();
		this.timeSystemEvents.add(acreRefreshEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				acreRefreshEvent, -1, MonthType.EVERY, DayType.EVERY,
				HourType.HOUR_00);

		// balance sheet publication
		ITimeSystemEvent balanceSheetPublicationEvent = new BalanceSheetPublicationEvent();
		this.timeSystemEvents.add(balanceSheetPublicationEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				balanceSheetPublicationEvent, -1, MonthType.EVERY,
				DayType.EVERY, BALANCE_SHEET_PUBLICATION_HOUR_TYPE);

		this.economicalBehaviour = new EconomicalBehaviour(this,
				GoodType.MEGACALORIE, this.compositeProductionFunction);
	}

	/*
	 * Accessors
	 */

	/*
	 * Business logic
	 */

	@Override
	@Transient
	protected double calculateTotalDividend() {
		this.assertTransactionsBankAccount();
		return Math.max(0.0, this.transactionsBankAccount.getBalance()
				- MONEY_TO_RETAIN);
	}

	protected class SettlementMarketEvent implements ISettlementEvent {
		@Override
		public void onEvent(GoodType goodType, double amount,
				double pricePerUnit) {
			Farm.this.assertTransactionsBankAccount();
			if (goodType.equals(GoodType.MEGACALORIE)) {
				Farm.this.economicalBehaviour.registerSelling(amount);
			}
		}

		@Override
		public void onEvent(IProperty property, double amount,
				double pricePerUnit) {
		}
	}

	protected class ProductionEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			Farm.this.assertTransactionsBankAccount();

			Farm.this.economicalBehaviour.nextPeriod();

			/*
			 * Buy labour hours
			 */
			double requiredLabourHours = Farm.this.economicalBehaviour
					.getProductionBehaviour()
					.calculateProfitableAmountOfLabourHourInput();
			double budget = Farm.this.economicalBehaviour
					.getBudgetingBehaviour()
					.calculateTransmissionBasedBudgetForPeriod(
							Farm.this.transactionsBankAccount.getCurrency(),
							Farm.this.transactionsBankAccount.getBalance(),
							MAX_CREDIT);
			PrimaryMarket.getInstance().buy(
					GoodType.LABOURHOUR,
					-1,
					requiredLabourHours,
					budget,
					-1,
					-1,
					Farm.this,
					Farm.this.transactionsBankAccount,
					Farm.this.bankPasswords
							.get(Farm.this.transactionsBankAccount
									.getManagingBank()));

			/*
			 * Report production capacity
			 */
			double productionCapacity = 0;
			for (Acre acre : Farm.this.acres)
				productionCapacity += acre.productionFunction
						.calculateMaxOutputPerProductionCycle();

			Log.farm_ProductionCapacity(Farm.this, productionCapacity);

			/*
			 * Harvest with production factors acre and labour hour
			 */
			double harvestedMegaCalories = 0;

			for (Acre acre : Farm.this.acres) {
				if (!acre.isExhausted()) {
					harvestedMegaCalories += acre.harvest();
				}
			}

			Log.farm_onProduction(Farm.this, harvestedMegaCalories);

			/*
			 * Refresh prices / offer
			 */
			Farm.this.economicalBehaviour.getPricingBehaviour().setNewPrice();
			PrimaryMarket.getInstance().removeAllSellingOffers(Farm.this,
					GoodType.MEGACALORIE);
			double amount = PropertyRegister.getInstance().getBalance(
					Farm.this, GoodType.MEGACALORIE);
			PrimaryMarket.getInstance().placeSettlementSellingOffer(
					GoodType.MEGACALORIE,
					Farm.this,
					Currency.EURO,
					Farm.this.transactionsBankAccount,
					amount,
					Farm.this.economicalBehaviour.getPricingBehaviour()
							.getCurrentPrice(), new SettlementMarketEvent());
			Farm.this.economicalBehaviour.registerOfferedAmount(amount);

			// ToDo Remove
			Farm.this.payDividend();
		}
	}

	protected class AcreRefreshEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			for (Acre acre : Farm.this.acres)
				acre.refresh();
		}
	}

	protected class BalanceSheetPublicationEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			Farm.this.assertTransactionsBankAccount();
			BalanceSheet balanceSheet = Farm.this.issueBasicBalanceSheet();
			balanceSheet.issuedCapital = Farm.this.issuedShares;
			Log.agent_onPublishBalanceSheet(Farm.this, balanceSheet);
		}
	}

	protected class Acre implements Refreshable, Comparable<Acre> {

		protected boolean isExhausted = false;

		protected LinearProductionFunction productionFunction;

		public Acre(double maxOutputPerProductionCycle) {
			this.productionFunction = new LinearProductionFunction(
					maxOutputPerProductionCycle);
		}

		public double harvest() {
			double harvestedMegaCalories = 0;

			double numberOfLabourHoursToUse = Math.min(
					this.productionFunction
							.calculateMaxLabourHourInputPerProductionCycle(),
					PropertyRegister.getInstance().getBalance(Farm.this,
							GoodType.LABOURHOUR));

			if (!this.isExhausted()) {
				harvestedMegaCalories = this.productionFunction
						.calculateOutput(numberOfLabourHoursToUse);
				this.exhaust();
			}

			// deregister labour hours
			PropertyRegister.getInstance().decrement(Farm.this,
					GoodType.LABOURHOUR, numberOfLabourHoursToUse);

			// notify statistical service of exhausted labour hours
			Log.farm_onLabourHourExhaust(Farm.this, numberOfLabourHoursToUse);

			// register harvest
			PropertyRegister.getInstance().increment(Farm.this,
					GoodType.MEGACALORIE, harvestedMegaCalories);

			return harvestedMegaCalories;
		}

		@Override
		public void refresh() {
			this.isExhausted = false;
		}

		@Override
		public void exhaust() {
			this.isExhausted = true;
		}

		@Override
		public boolean isExhausted() {
			return this.isExhausted;
		}

		@Override
		public int compareTo(Acre acre) {
			return this.productionFunction.compareTo(acre.productionFunction);
		}

		public String toString() {
			return "acre productivity "
					+ this.productionFunction
							.calculateMaxOutputPerProductionCycle();
		}
	}

}
