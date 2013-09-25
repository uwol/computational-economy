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

package compecon.economy.sectors.industry;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import compecon.economy.BudgetingBehaviour;
import compecon.economy.PricingBehaviour;
import compecon.economy.markets.SettlementMarket.ISettlementEvent;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.economy.sectors.state.law.property.Property;
import compecon.economy.sectors.state.law.property.PropertyRegister;
import compecon.economy.sectors.state.law.security.equity.JointStockCompany;
import compecon.engine.MarketFactory;
import compecon.engine.jmx.Log;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.TimeSystem;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.MonthType;
import compecon.engine.util.ConfigurationUtil;
import compecon.engine.util.MathUtil;
import compecon.materia.GoodType;
import compecon.math.price.IPriceFunction;
import compecon.math.production.IProductionFunction;

/**
 * Agent type factory produces arbitrary goods by combining production factors
 * machine and labour hour.
 */
@Entity
public class Factory extends JointStockCompany {

	@Enumerated(EnumType.STRING)
	protected GoodType producedGoodType;

	@Transient
	protected IProductionFunction productionFunction;

	@Transient
	protected PricingBehaviour pricingBehaviour;

	@Transient
	protected BudgetingBehaviour budgetingBehaviour;

	@Override
	public void initialize() {
		super.initialize();

		// production event at random HourType
		ITimeSystemEvent productionEvent = new ProductionEvent();
		this.timeSystemEvents.add(productionEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(productionEvent,
				-1, MonthType.EVERY, DayType.EVERY,
				TimeSystem.getInstance().suggestRandomHourType());

		// balance sheet publication
		ITimeSystemEvent balanceSheetPublicationEvent = new BalanceSheetPublicationEvent();
		this.timeSystemEvents.add(balanceSheetPublicationEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				balanceSheetPublicationEvent, -1, MonthType.EVERY,
				DayType.EVERY, BALANCE_SHEET_PUBLICATION_HOUR_TYPE);

		double marketPrice = MarketFactory.getInstance().getPrice(
				this.primaryCurrency, this.producedGoodType);
		this.pricingBehaviour = new PricingBehaviour(this,
				this.producedGoodType, this.primaryCurrency, marketPrice);
		this.budgetingBehaviour = new BudgetingBehaviour(this);
	}

	/*
	 * accessors
	 */

	public GoodType getProducedGoodType() {
		return producedGoodType;
	}

	@Transient
	public IProductionFunction getProductionFunction() {
		return this.productionFunction;
	}

	public void setProducedGoodType(GoodType producedGoodType) {
		this.producedGoodType = producedGoodType;
	}

	@Transient
	public void setProductionFunction(IProductionFunction productionFunction) {
		this.productionFunction = productionFunction;
	}

	/*
	 * business logic
	 */

	protected class SettlementMarketEvent implements ISettlementEvent {
		@Override
		public void onEvent(GoodType goodType, double amount,
				double pricePerUnit, Currency currency) {
			Factory.this.assureTransactionsBankAccount();
			if (Factory.this.producedGoodType.equals(goodType)) {
				Factory.this.pricingBehaviour.registerSelling(amount, amount
						* pricePerUnit);
			}
		}

		@Override
		public void onEvent(Currency commodityCurrency, double amount,
				double pricePerUnit, Currency currency) {
		}

		@Override
		public void onEvent(Property property, double totalPrice,
				Currency currency) {
		}
	}

	public class ProductionEvent implements ITimeSystemEvent {

		@Override
		public void onEvent() {
			Factory.this.assureTransactionsBankAccount();
			Factory.this.pricingBehaviour.nextPeriod();

			/*
			 * economic actions
			 */
			double budget = Factory.this.budgetingBehaviour
					.calculateTransmissionBasedBudgetForPeriod(
							Factory.this.transactionsBankAccount.getCurrency(),
							Factory.this.transactionsBankAccount.getBalance(),
							Factory.this.referenceCredit);

			this.buyOptimalProductionFactorsForBudget(budget);

			this.produce();

			this.offerProducedGoodType();
		}

		protected void buyOptimalProductionFactorsForBudget(final double budget) {
			if (MathUtil.greater(budget, 0.0)) {
				// get prices for production factors
				Map<GoodType, IPriceFunction> priceFunctionsOfProductionFactors = MarketFactory
						.getInstance().getMarketPriceFunctions(
								Factory.this.primaryCurrency,
								Factory.this.productionFunction
										.getInputGoodTypes());

				// calculate optimal production plan
				double priceOfProducedGoodType = MarketFactory.getInstance()
						.getPrice(Factory.this.primaryCurrency,
								Factory.this.producedGoodType);

				Log.setAgentCurrentlyActive(Factory.this);
				Map<GoodType, Double> productionFactorsToBuy = Factory.this.productionFunction
						.calculateProfitMaximizingProductionFactors(
								priceOfProducedGoodType,
								priceFunctionsOfProductionFactors, budget,
								Double.NaN,
								ConfigurationUtil.FactoryConfig.getMargin());

				// buy production factors
				double budgetSpent = this
						.buyProductionFactors(productionFactorsToBuy);
			}
		}

		protected double buyProductionFactors(
				final Map<GoodType, Double> productionFactorsToBuy) {
			/*
			 * buy production factors; maxPricePerUnit is significantly
			 * important for price equilibrium
			 */
			double budgetSpent = 0.0;
			for (Entry<GoodType, Double> entry : productionFactorsToBuy
					.entrySet()) {
				GoodType goodTypeToBuy = entry.getKey();
				double amountToBuy = entry.getValue();
				if (MathUtil.greater(amountToBuy, 0.0)) {
					double[] priceAndAmount = MarketFactory.getInstance().buy(
							goodTypeToBuy,
							amountToBuy,
							Double.NaN,
							Double.NaN,
							Factory.this,
							Factory.this.transactionsBankAccount,
							Factory.this.bankPasswords
									.get(Factory.this.transactionsBankAccount
											.getManagingBank()));
					budgetSpent += priceAndAmount[0];
				}
			}
			return budgetSpent;
		}

		protected void produce() {
			/*
			 * produce with production factors
			 */
			Map<GoodType, Double> productionFactorsOwned = new HashMap<GoodType, Double>();
			for (GoodType productionFactor : Factory.this.productionFunction
					.getInputGoodTypes()) {
				productionFactorsOwned.put(
						productionFactor,
						PropertyRegister.getInstance().getBalance(Factory.this,
								productionFactor));
			}

			double producedOutput = Factory.this.productionFunction
					.calculateOutput(productionFactorsOwned);
			Log.factory_onProduction(Factory.this,
					Factory.this.primaryCurrency,
					Factory.this.producedGoodType, producedOutput,
					productionFactorsOwned);
			PropertyRegister.getInstance()
					.incrementGoodTypeAmount(Factory.this,
							Factory.this.producedGoodType, producedOutput);
			if (Log.isAgentSelectedByClient(Factory.this))
				Log.log(Factory.this, ProductionEvent.class, "produced "
						+ MathUtil.round(producedOutput) + " "
						+ Factory.this.producedGoodType);

			/*
			 * deregister production factors from property register
			 */
			for (Entry<GoodType, Double> entry : productionFactorsOwned
					.entrySet()) {
				PropertyRegister.getInstance().decrementGoodTypeAmount(
						Factory.this, entry.getKey(), entry.getValue());
			}
		}

		protected void offerProducedGoodType() {
			/*
			 * refresh prices / offer
			 */
			MarketFactory.getInstance()
					.removeAllSellingOffers(Factory.this,
							Factory.this.primaryCurrency,
							Factory.this.producedGoodType);
			double amount = PropertyRegister.getInstance().getBalance(
					Factory.this, Factory.this.producedGoodType);
			double[] prices = Factory.this.pricingBehaviour
					.getCurrentPriceArray();
			for (double price : prices) {
				MarketFactory.getInstance().placeSettlementSellingOffer(
						Factory.this.producedGoodType, Factory.this,
						Factory.this.transactionsBankAccount,
						amount / ((double) prices.length), price,
						new SettlementMarketEvent());
			}
			Factory.this.pricingBehaviour.registerOfferedAmount(amount);
		}
	}

	public class BalanceSheetPublicationEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			Factory.this.assureTransactionsBankAccount();
			BalanceSheet balanceSheet = Factory.this.issueBasicBalanceSheet();
			balanceSheet.issuedCapital = Factory.this.issuedShares;
			Log.agent_onPublishBalanceSheet(Factory.this, balanceSheet);
		}
	}

	@Override
	public String toString() {
		return super.toString() + " [" + this.producedGoodType + "]";
	}

}
