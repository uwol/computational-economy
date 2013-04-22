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

package compecon.culture.sectors.industry;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

import compecon.culture.EconomicalBehaviour;
import compecon.culture.markets.SettlementMarket.ISettlementEvent;
import compecon.culture.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.culture.sectors.state.law.property.IProperty;
import compecon.culture.sectors.state.law.property.PropertyRegister;
import compecon.culture.sectors.state.law.security.equity.JointStockCompany;
import compecon.engine.Log;
import compecon.engine.MarketFactory;
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
 * Agent type factory produces arbitrary goods by combining production factors
 * machine and labour hour.
 */
@Entity
@Table(name = "Factory")
public class Factory extends JointStockCompany {

	@Enumerated(EnumType.STRING)
	protected GoodType producedGoodType;

	// maxCredit limits the demand for money when buying production input
	// factors, thus limiting M1 in the monetary system
	@Transient
	protected final int MAX_CREDIT = 10000;

	// state of farm
	@Transient
	protected SortedSet<Machine> machines = new TreeSet<Machine>();

	@Transient
	protected CompositeProductionFunction compositeProductionFunction;

	@Transient
	protected EconomicalBehaviour economicalBehaviour;

	@Override
	public void initialize() {
		super.initialize();

		this.compositeProductionFunction = new CompositeProductionFunction();

		for (int i = 0; i < 100; i++) {
			Machine machine = new Machine(i * 0.25);
			this.machines.add(machine);
			this.compositeProductionFunction
					.addProductionFunction(machine.productionFunction);
		}

		// production event at random HourType
		ITimeSystemEvent productionEvent = new ProductionEvent();
		this.timeSystemEvents.add(productionEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(productionEvent,
				-1, MonthType.EVERY, DayType.EVERY,
				TimeSystem.getInstance().suggestRandomHourType());

		// refresh machines at 00:00
		ITimeSystemEvent machineRefreshEvent = new MachineRefreshEvent();
		this.timeSystemEvents.add(machineRefreshEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				machineRefreshEvent, -1, MonthType.EVERY, DayType.EVERY,
				HourType.HOUR_00);

		// balance sheet publication
		ITimeSystemEvent balanceSheetPublicationEvent = new BalanceSheetPublicationEvent();
		this.timeSystemEvents.add(balanceSheetPublicationEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				balanceSheetPublicationEvent, -1, MonthType.EVERY,
				DayType.EVERY, BALANCE_SHEET_PUBLICATION_HOUR_TYPE);

		this.economicalBehaviour = new EconomicalBehaviour(this,
				GoodType.MEGACALORIE, this.compositeProductionFunction,
				this.primaryCurrency);
	}

	/*
	 * Accessors
	 */

	public GoodType getProducedGoodType() {
		return producedGoodType;
	}

	public void setProducedGoodType(GoodType producedGoodType) {
		this.producedGoodType = producedGoodType;
	}

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
			Factory.this.assertTransactionsBankAccount();
			if (Factory.this.producedGoodType.equals(goodType)) {
				Factory.this.economicalBehaviour.registerSelling(amount);
			}
		}

		@Override
		public void onEvent(IProperty property, double amount, double totalPrice) {
		}
	}

	protected class ProductionEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			Factory.this.assertTransactionsBankAccount();
			Factory.this.economicalBehaviour.nextPeriod();

			/*
			 * Buy labour hours
			 */
			double requiredLabourHours = Factory.this.economicalBehaviour
					.getProductionBehaviour()
					.calculateProfitableAmountOfLabourHourInput();
			double budget = Factory.this.economicalBehaviour
					.getBudgetingBehaviour()
					.calculateTransmissionBasedBudgetForPeriod(
							Factory.this.transactionsBankAccount.getCurrency(),
							Factory.this.transactionsBankAccount.getBalance(),
							MAX_CREDIT);
			MarketFactory.getInstance(
					Factory.this.transactionsBankAccount.getCurrency()).buy(
					GoodType.LABOURHOUR,
					-1,
					requiredLabourHours,
					budget,
					-1,
					-1,
					Factory.this,
					Factory.this.transactionsBankAccount,
					Factory.this.bankPasswords
							.get(Factory.this.transactionsBankAccount
									.getManagingBank()));

			/*
			 * Report production capacity
			 */
			double productionCapacity = 0;
			for (Machine machine : Factory.this.machines)
				productionCapacity += machine.productionFunction
						.calculateMaxOutputPerProductionCycle();

			Log.factory_ProductionCapacity(Factory.this,
					Factory.this.producedGoodType, productionCapacity);

			/*
			 * Produce with production factors machine and labour hour
			 */
			double producedProducts = 0;

			for (Machine machine : Factory.this.machines) {
				if (!machine.isExhausted()) {
					producedProducts += machine.produce();
				}
			}

			Log.factory_onProduction(Factory.this,
					Factory.this.producedGoodType, producedProducts);

			/*
			 * Refresh prices / offer
			 */
			Factory.this.economicalBehaviour.getPricingBehaviour()
					.setNewPrice();
			MarketFactory.getInstance(
					Factory.this.transactionsBankAccount.getCurrency())
					.removeAllSellingOffers(Factory.this,
							Factory.this.producedGoodType);
			double amount = PropertyRegister.getInstance().getBalance(
					Factory.this, Factory.this.producedGoodType);
			MarketFactory.getInstance(
					Factory.this.transactionsBankAccount.getCurrency())
					.placeSettlementSellingOffer(
							Factory.this.producedGoodType,
							Factory.this,
							Factory.this.transactionsBankAccount.getCurrency(),
							Factory.this.transactionsBankAccount,
							amount,
							Factory.this.economicalBehaviour
									.getPricingBehaviour().getCurrentPrice(),
							new SettlementMarketEvent());
			Factory.this.economicalBehaviour.registerOfferedAmount(amount);

			// ToDo Remove
			Factory.this.payDividend();
		}
	}

	protected class MachineRefreshEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			for (Machine machine : Factory.this.machines)
				machine.refresh();
		}
	}

	protected class BalanceSheetPublicationEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			Factory.this.assertTransactionsBankAccount();
			BalanceSheet balanceSheet = Factory.this.issueBasicBalanceSheet();
			balanceSheet.issuedCapital = Factory.this.issuedShares;
			Log.agent_onPublishBalanceSheet(Factory.this, balanceSheet);
		}
	}

	protected class Machine implements Refreshable, Comparable<Machine> {

		protected boolean isExhausted = false;

		protected LinearProductionFunction productionFunction;

		public Machine(double maxOutputPerProductionCycle) {
			this.productionFunction = new LinearProductionFunction(
					maxOutputPerProductionCycle);
		}

		public double produce() {
			double output = 0;

			double numberOfLabourHoursToUse = Math.min(
					this.productionFunction
							.calculateMaxLabourHourInputPerProductionCycle(),
					PropertyRegister.getInstance().getBalance(Factory.this,
							GoodType.LABOURHOUR));

			if (!this.isExhausted()) {
				output = this.productionFunction
						.calculateOutput(numberOfLabourHoursToUse);
				this.exhaust();
			}

			// deregister labour hours
			PropertyRegister.getInstance().decrement(Factory.this,
					GoodType.LABOURHOUR, numberOfLabourHoursToUse);

			// notify statistical service of exhausted labour hours
			Log.factory_onLabourHourExhaust(Factory.this,
					numberOfLabourHoursToUse);

			// register output
			PropertyRegister.getInstance().increment(Factory.this,
					Factory.this.producedGoodType, output);

			return output;
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
		public int compareTo(Machine machine) {
			return this.productionFunction
					.compareTo(machine.productionFunction);
		}

		public String toString() {
			return "machine productivity "
					+ this.productionFunction
							.calculateMaxOutputPerProductionCycle();
		}
	}
}
