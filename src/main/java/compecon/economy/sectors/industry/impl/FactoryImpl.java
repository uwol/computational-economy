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

package compecon.economy.sectors.industry.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import compecon.economy.behaviour.BudgetingBehaviour;
import compecon.economy.behaviour.PricingBehaviour;
import compecon.economy.behaviour.impl.BudgetingBehaviourImpl;
import compecon.economy.behaviour.impl.PricingBehaviourImpl;
import compecon.economy.materia.GoodType;
import compecon.economy.property.Property;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.industry.Factory;
import compecon.economy.security.equity.impl.JointStockCompanyImpl;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.timesystem.TimeSystemEvent;
import compecon.engine.timesystem.impl.DayType;
import compecon.engine.timesystem.impl.MonthType;
import compecon.math.price.PriceFunction;
import compecon.math.production.ProductionFunction;
import compecon.math.util.MathUtil;

/**
 * Agent type factory produces arbitrary goods by combining production factors
 * machine and labour hour.
 */
@Entity
public class FactoryImpl extends JointStockCompanyImpl implements Factory {

	@Transient
	protected BudgetingBehaviour budgetingBehaviour;

	@Enumerated(EnumType.STRING)
	protected GoodType producedGoodType;

	@Transient
	protected ProductionFunction productionFunction;

	@Transient
	protected PricingBehaviour pricingBehaviour;

	@Override
	public void initialize() {
		super.initialize();

		// production event at random HourType
		final TimeSystemEvent productionEvent = new ProductionEvent();
		this.timeSystemEvents.add(productionEvent);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(
						productionEvent,
						-1,
						MonthType.EVERY,
						DayType.EVERY,
						ApplicationContext.getInstance().getTimeSystem()
								.suggestRandomHourType());

		final double marketPrice = ApplicationContext
				.getInstance()
				.getMarketService()
				.getMarginalMarketPrice(this.primaryCurrency,
						this.producedGoodType);
		this.pricingBehaviour = new PricingBehaviourImpl(this,
				this.producedGoodType, this.primaryCurrency, marketPrice);
		this.budgetingBehaviour = new BudgetingBehaviourImpl(this);
	}

	@Override
	public void deconstruct() {
		super.deconstruct();

		ApplicationContext.getInstance().getFactoryFactory()
				.deleteFactory(this);
	}

	/*
	 * accessors
	 */

	public GoodType getProducedGoodType() {
		return producedGoodType;
	}

	@Transient
	public ProductionFunction getProductionFunction() {
		return this.productionFunction;
	}

	public void setProducedGoodType(GoodType producedGoodType) {
		this.producedGoodType = producedGoodType;
	}

	@Transient
	public void setProductionFunction(ProductionFunction productionFunction) {
		this.productionFunction = productionFunction;
	}

	/*
	 * business logic
	 */

	@Override
	public void onMarketSettlement(GoodType goodType, double amount,
			double pricePerUnit, Currency currency) {
		FactoryImpl.this.assureBankAccountTransactions();
		if (FactoryImpl.this.producedGoodType.equals(goodType)) {
			FactoryImpl.this.pricingBehaviour.registerSelling(amount, amount
					* pricePerUnit);
		}
	}

	@Override
	public void onMarketSettlement(Currency commodityCurrency, double amount,
			double pricePerUnit, Currency currency) {
	}

	@Override
	public void onMarketSettlement(Property property, double totalPrice,
			Currency currency) {
	}

	@Override
	public String toString() {
		return super.toString() + ", producedGoodType=["
				+ this.producedGoodType + "]";
	}

	public class ProductionEvent implements TimeSystemEvent {
		@Override
		public boolean isDeconstructed() {
			return FactoryImpl.this.isDeconstructed;
		}

		@Override
		public void onEvent() {
			FactoryImpl.this.assureBankAccountTransactions();

			getLog().factory_AmountSold(FactoryImpl.this.primaryCurrency,
					FactoryImpl.this.producedGoodType,
					FactoryImpl.this.pricingBehaviour.getLastSoldAmount());

			/*
			 * simulation mechanics
			 */
			FactoryImpl.this.pricingBehaviour.nextPeriod();

			/*
			 * economic actions
			 */
			FactoryImpl.this
					.transferBankAccountBalanceToDividendBankAccount(FactoryImpl.this.bankAccountTransactions);

			final double budget = FactoryImpl.this.budgetingBehaviour
					.calculateTransmissionBasedBudgetForPeriod(
							FactoryImpl.this.bankAccountTransactions
									.getCurrency(),
							FactoryImpl.this.bankAccountTransactions
									.getBalance(),
							FactoryImpl.this.referenceCredit);

			this.buyOptimalProductionFactorsForBudget(budget);

			final double producedOutput = this.produce();

			this.offerProducedGoodType(producedOutput);
		}

		protected void buyOptimalProductionFactorsForBudget(final double budget) {
			if (MathUtil.greater(budget, 0.0)) {
				// get prices for production factors
				final Map<GoodType, PriceFunction> priceFunctionsOfProductionFactors = ApplicationContext
						.getInstance()
						.getMarketService()
						.getMarketPriceFunctions(
								FactoryImpl.this.primaryCurrency,
								FactoryImpl.this.productionFunction
										.getInputGoodTypes());

				// calculate optimal production plan
				final double priceOfProducedGoodType = ApplicationContext
						.getInstance()
						.getMarketService()
						.getMarginalMarketPrice(
								FactoryImpl.this.primaryCurrency,
								FactoryImpl.this.producedGoodType);

				getLog().setAgentCurrentlyActive(FactoryImpl.this);
				final Map<GoodType, Double> productionFactorsToBuy = FactoryImpl.this.productionFunction
						.calculateProfitMaximizingProductionFactors(
								priceOfProducedGoodType,
								priceFunctionsOfProductionFactors, budget,
								Double.NaN, ApplicationContext.getInstance()
										.getConfiguration().factoryConfig
										.getMargin());

				// buy production factors
				final double budgetSpent = this
						.buyProductionFactors(productionFactorsToBuy);

				assert (MathUtil.lesserEqual(budgetSpent, budget * 1.2));

				// log credit capacity utilization
				double creditBudgetCapacity = FactoryImpl.this.budgetingBehaviour
						.getCreditBasedBudgetCapacity();
				double creditUtilization = -1.0
						* FactoryImpl.this.getBankAccountTransactions()
								.getBalance();
				getLog().agent_CreditUtilization(FactoryImpl.this,
						creditUtilization, creditBudgetCapacity);
				assert (MathUtil.lesserEqual(creditUtilization,
						creditBudgetCapacity * 1.1));
			}
		}

		private double buyProductionFactors(
				final Map<GoodType, Double> productionFactorsToBuy) {
			/*
			 * buy production factors; maxPricePerUnit is significantly
			 * important for price equilibrium
			 */
			double budgetSpent = 0.0;
			for (Entry<GoodType, Double> entry : productionFactorsToBuy
					.entrySet()) {
				final GoodType goodTypeToBuy = entry.getKey();
				final double amountToBuy = entry.getValue();
				if (MathUtil.greater(amountToBuy, 0.0)) {
					final double[] priceAndAmount = ApplicationContext
							.getInstance()
							.getMarketService()
							.buy(goodTypeToBuy, amountToBuy, Double.NaN,
									Double.NaN, FactoryImpl.this,
									getBankAccountTransactionsDelegate());
					budgetSpent += priceAndAmount[0];
				}
			}
			return budgetSpent;
		}

		protected double produce() {
			/*
			 * produce with production factors
			 */
			final Map<GoodType, Double> productionFactorsOwned = new HashMap<GoodType, Double>();
			for (GoodType productionFactor : FactoryImpl.this.productionFunction
					.getInputGoodTypes()) {
				productionFactorsOwned
						.put(productionFactor,
								ApplicationContext
										.getInstance()
										.getPropertyService()
										.getBalance(FactoryImpl.this,
												productionFactor));
			}

			final double producedOutput = FactoryImpl.this.productionFunction
					.calculateOutput(productionFactorsOwned);
			ApplicationContext
					.getInstance()
					.getPropertyService()
					.incrementGoodTypeAmount(FactoryImpl.this,
							FactoryImpl.this.producedGoodType, producedOutput);

			getLog().factory_onProduction(FactoryImpl.this,
					FactoryImpl.this.primaryCurrency,
					FactoryImpl.this.producedGoodType, producedOutput,
					productionFactorsOwned);
			if (getLog().isAgentSelectedByClient(FactoryImpl.this)) {
				getLog().log(
						FactoryImpl.this,
						ProductionEvent.class,
						"produced " + MathUtil.round(producedOutput) + " "
								+ FactoryImpl.this.producedGoodType);
			}

			/*
			 * deregister production factors from property register
			 */
			for (Entry<GoodType, Double> entry : productionFactorsOwned
					.entrySet()) {
				ApplicationContext
						.getInstance()
						.getPropertyService()
						.decrementGoodTypeAmount(FactoryImpl.this,
								entry.getKey(), entry.getValue());
			}

			return producedOutput;
		}

		protected void offerProducedGoodType(double producedOutput) {
			/*
			 * refresh prices / offer
			 */
			ApplicationContext
					.getInstance()
					.getMarketService()
					.removeAllSellingOffers(FactoryImpl.this,
							FactoryImpl.this.primaryCurrency,
							FactoryImpl.this.producedGoodType);
			final double amountInInventory = ApplicationContext
					.getInstance()
					.getPropertyService()
					.getBalance(FactoryImpl.this,
							FactoryImpl.this.producedGoodType);
			final double[] prices = FactoryImpl.this.pricingBehaviour
					.getCurrentPriceArray();
			for (double price : prices) {
				ApplicationContext
						.getInstance()
						.getMarketService()
						.placeSellingOffer(FactoryImpl.this.producedGoodType,
								FactoryImpl.this,
								getBankAccountTransactionsDelegate(),
								amountInInventory / ((double) prices.length),
								price);
			}
			FactoryImpl.this.pricingBehaviour
					.registerOfferedAmount(amountInInventory);

			getLog().factory_onOfferGoodType(FactoryImpl.this.primaryCurrency,
					FactoryImpl.this.producedGoodType, amountInInventory,
					amountInInventory);
		}
	}

}
