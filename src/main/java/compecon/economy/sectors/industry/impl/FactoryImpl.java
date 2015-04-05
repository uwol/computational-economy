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

	public class ProductionEvent implements TimeSystemEvent {
		protected void buyOptimalProductionFactorsForBudget(final double budget) {
			if (MathUtil.greater(budget, 0.0)) {
				// get prices for production factors
				final Map<GoodType, PriceFunction> priceFunctionsOfProductionFactors = ApplicationContext
						.getInstance()
						.getMarketService()
						.getMarketPriceFunctions(
								FactoryImpl.this.primaryCurrency,
								productionFunction.getInputGoodTypes());

				/*
				 * calculate optimal production plan
				 */
				final double priceOfProducedGoodType = ApplicationContext
						.getInstance()
						.getMarketService()
						.getMarginalMarketPrice(
								FactoryImpl.this.primaryCurrency,
								producedGoodType);

				final Map<GoodType, Double> capital = ApplicationContext
						.getInstance().getPropertyService()
						.getCapitalBalances(FactoryImpl.this);

				getLog().setAgentCurrentlyActive(FactoryImpl.this);
				final Map<GoodType, Double> profitMaximizingProductionFactors = productionFunction
						.calculateProfitMaximizingProductionFactors(
								priceOfProducedGoodType,
								priceFunctionsOfProductionFactors, capital,
								budget, Double.NaN,
								ApplicationContext.getInstance()
										.getConfiguration().factoryConfig
										.getMargin());

				final Map<GoodType, Double> profitMaximizingProductionFactorsToBuy = new HashMap<GoodType, Double>(
						profitMaximizingProductionFactors);

				/*
				 * the optimal production plan includes capital goods. Only
				 * surplus capital goods should be bought!
				 */
				for (final Entry<GoodType, Double> entry : profitMaximizingProductionFactorsToBuy
						.entrySet()) {
					final GoodType goodType = entry.getKey();
					final double profitMaximizingAmountOfGoodType = entry
							.getValue();
					final double ownedAmountOfGoodType = MathUtil
							.nullSafeValue(capital.get(goodType));
					final double amountOfCapitalGoodTypeToBuy = Math.max(
							profitMaximizingAmountOfGoodType
									- ownedAmountOfGoodType, 0.0);
					profitMaximizingProductionFactorsToBuy.put(goodType,
							amountOfCapitalGoodTypeToBuy);
				}

				// buy production factors
				final double budgetSpent = buyProductionFactors(profitMaximizingProductionFactorsToBuy);

				assert (MathUtil.lesserEqual(budgetSpent, budget * 1.2));

				// log credit capacity utilization
				final double creditBudgetCapacity = budgetingBehaviour
						.getCreditBasedBudgetCapacity();
				final double creditUtilization = -1.0
						* getBankAccountTransactions().getBalance();
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
			for (final Entry<GoodType, Double> entry : productionFactorsToBuy
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

		/**
		 * capital depreciation according to the Solow–Swan model <br />
		 * <br />
		 * http://en.wikipedia.org/wiki/Solow%E2%80%93Swan_model
		 */
		protected void capitalDepreciation() {
			final Map<GoodType, Double> capital = ApplicationContext
					.getInstance().getPropertyService()
					.getCapitalBalances(FactoryImpl.this);
			final double depreciationRatio = ApplicationContext.getInstance()
					.getConfiguration().factoryConfig
					.getCapitalDepreciationRatioPerPeriod();

			for (final Entry<GoodType, Double> entry : capital.entrySet()) {
				final GoodType capitalGoodType = entry.getKey();
				final double capitalGoodTypeAmount = entry.getValue();
				final double depreciation = depreciationRatio
						* capitalGoodTypeAmount;

				if (depreciation > 0) {
					ApplicationContext
							.getInstance()
							.getPropertyService()
							.decrementGoodTypeAmount(FactoryImpl.this,
									capitalGoodType, depreciation);

					ApplicationContext
							.getInstance()
							.getLog()
							.factory_onCapitalDepreciation(FactoryImpl.this,
									capitalGoodType, depreciation);
				}
			}
		}

		@Override
		public boolean isDeconstructed() {
			return FactoryImpl.this.isDeconstructed;
		}

		protected void offerProducedGoodType(final double producedOutput) {
			/*
			 * refresh prices / offer
			 */
			ApplicationContext
					.getInstance()
					.getMarketService()
					.removeAllSellingOffers(FactoryImpl.this,
							FactoryImpl.this.primaryCurrency, producedGoodType);
			final double amountInInventory = ApplicationContext.getInstance()
					.getPropertyService()
					.getGoodTypeBalance(FactoryImpl.this, producedGoodType);
			final double[] prices = pricingBehaviour.getCurrentPriceArray();
			for (final double price : prices) {
				ApplicationContext
						.getInstance()
						.getMarketService()
						.placeSellingOffer(producedGoodType, FactoryImpl.this,
								getBankAccountTransactionsDelegate(),
								amountInInventory / (prices.length), price);
			}
			pricingBehaviour.registerOfferedAmount(amountInInventory);

			getLog().factory_onOfferGoodType(FactoryImpl.this.primaryCurrency,
					producedGoodType, amountInInventory, amountInInventory);
		}

		@Override
		public void onEvent() {
			assureBankAccountTransactions();

			getLog().factory_AmountSold(FactoryImpl.this.primaryCurrency,
					producedGoodType, pricingBehaviour.getLastSoldAmount());

			/*
			 * simulation mechanics
			 */
			pricingBehaviour.nextPeriod();

			/*
			 * economic actions
			 */
			transferBankAccountBalanceToDividendBankAccount(FactoryImpl.this.bankAccountTransactions);

			/*
			 * has to happen before offering good on market; otherwise there is
			 * offered more than owned.
			 */
			capitalDepreciation();

			final double budget = budgetingBehaviour
					.calculateTransmissionBasedBudgetForPeriod(
							FactoryImpl.this.bankAccountTransactions
									.getCurrency(),
							FactoryImpl.this.bankAccountTransactions
									.getBalance(),
							FactoryImpl.this.referenceCredit);

			buyOptimalProductionFactorsForBudget(budget);

			final double producedOutput = produce();

			offerProducedGoodType(producedOutput);
		}

		protected double produce() {
			/*
			 * produce with production factors
			 */
			final Map<GoodType, Double> productionFactorsOwned = new HashMap<GoodType, Double>();
			for (final GoodType productionFactor : productionFunction
					.getInputGoodTypes()) {
				productionFactorsOwned.put(
						productionFactor,
						ApplicationContext
								.getInstance()
								.getPropertyService()
								.getGoodTypeBalance(FactoryImpl.this,
										productionFactor));
			}

			final double producedOutput = productionFunction
					.calculateOutput(productionFactorsOwned);
			ApplicationContext
					.getInstance()
					.getPropertyService()
					.incrementGoodTypeAmount(FactoryImpl.this,
							producedGoodType, producedOutput);

			getLog().factory_onProduction(FactoryImpl.this,
					FactoryImpl.this.primaryCurrency, producedGoodType,
					producedOutput, productionFactorsOwned);
			if (getLog().isAgentSelectedByClient(FactoryImpl.this)) {
				getLog().log(FactoryImpl.this, ProductionEvent.class,
						"produced %s %s", MathUtil.round(producedOutput),
						producedGoodType);
			}

			/*
			 * deregister production factors from property register
			 */
			for (final Entry<GoodType, Double> entry : productionFactorsOwned
					.entrySet()) {
				final GoodType productionFactor = entry.getKey();

				// only non-durable production inputs are exhausted; durable
				// production inputs are capital goods
				if (!productionFactor.isDurable()) {
					ApplicationContext
							.getInstance()
							.getPropertyService()
							.decrementGoodTypeAmount(FactoryImpl.this,
									entry.getKey(), entry.getValue());
				}
			}

			return producedOutput;
		}
	}

	@Transient
	protected BudgetingBehaviour budgetingBehaviour;

	@Transient
	protected PricingBehaviour pricingBehaviour;

	@Enumerated(EnumType.STRING)
	protected GoodType producedGoodType;

	@Transient
	protected ProductionFunction productionFunction;

	@Override
	public void deconstruct() {
		super.deconstruct();

		ApplicationContext.getInstance().getFactoryFactory()
				.deleteFactory(this);
	}

	/*
	 * accessors
	 */

	@Override
	public GoodType getProducedGoodType() {
		return producedGoodType;
	}

	@Override
	@Transient
	public ProductionFunction getProductionFunction() {
		return productionFunction;
	}

	@Override
	public void initialize() {
		super.initialize();

		// production event at random HourType
		final TimeSystemEvent productionEvent = new ProductionEvent();
		timeSystemEvents.add(productionEvent);
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

		final double marketPrice = ApplicationContext.getInstance()
				.getMarketService()
				.getMarginalMarketPrice(primaryCurrency, producedGoodType);
		pricingBehaviour = new PricingBehaviourImpl(this, producedGoodType,
				primaryCurrency, marketPrice);
		budgetingBehaviour = new BudgetingBehaviourImpl(this);
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
		FactoryImpl.this.assureBankAccountTransactions();
		if (FactoryImpl.this.producedGoodType.equals(goodType)) {
			FactoryImpl.this.pricingBehaviour.registerSelling(amount, amount
					* pricePerUnit);
		}
	}

	@Override
	public void onMarketSettlement(final Property property,
			final double totalPrice, final Currency currency) {
	}

	public void setProducedGoodType(final GoodType producedGoodType) {
		this.producedGoodType = producedGoodType;
	}

	@Transient
	public void setProductionFunction(
			final ProductionFunction productionFunction) {
		this.productionFunction = productionFunction;
	}

	@Override
	public String toString() {
		return super.toString() + ", producedGoodType=[" + producedGoodType
				+ "]";
	}
}
