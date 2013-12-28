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

package compecon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Map.Entry;

import compecon.economy.materia.GoodType;
import compecon.economy.sectors.financial.CentralBank;
import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.economy.sectors.industry.Factory;
import compecon.economy.sectors.state.State;
import compecon.economy.sectors.trading.Trader;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.applicationcontext.ApplicationContextFactory;
import compecon.engine.util.HibernateUtil;
import compecon.math.impl.FunctionImpl;
import compecon.math.price.PriceFunction;

public abstract class CompEconTestSupport {

	protected final double epsilon = 0.01;

	public void assertOutputIsOptimalUnderBudget(
			final FunctionImpl<GoodType> function,
			final double budgetRestriction,
			final Map<GoodType, PriceFunction> priceFunctions,
			final Map<GoodType, Double> referenceBundleOfInputs) {

		Map<GoodType, Double> rangeScanBundleOfInputs = function
				.calculateOutputMaximizingInputsByRangeScan(priceFunctions,
						budgetRestriction);

		// check that budget restriction is not violated
		double sumOfCostsOfOptimalBundleOfInputs = 0.0;
		for (Entry<GoodType, Double> inputEntry : rangeScanBundleOfInputs
				.entrySet()) {
			double priceOfGoodType = priceFunctions.get(inputEntry.getKey())
					.getPrice(inputEntry.getValue());
			if (!Double.isNaN(priceOfGoodType)) {
				sumOfCostsOfOptimalBundleOfInputs += priceFunctions.get(
						inputEntry.getKey()).getPrice(inputEntry.getValue())
						* inputEntry.getValue();
			}
		}

		// optimalBundleOfInputs violates the budget restriction
		assert (sumOfCostsOfOptimalBundleOfInputs <= budgetRestriction);

		// check that budget restriction is not violated
		double sumOfCostsOfReferenceBundleOfInputs = 0.0;
		for (Entry<GoodType, Double> inputEntry : referenceBundleOfInputs
				.entrySet()) {
			double priceOfGoodType = priceFunctions.get(inputEntry.getKey())
					.getPrice(inputEntry.getValue());
			if (!Double.isNaN(priceOfGoodType)) {
				sumOfCostsOfReferenceBundleOfInputs += priceOfGoodType
						* inputEntry.getValue();
			}
		}

		// referenceBundleOfInputs violates the budget restriction?
		assert (sumOfCostsOfReferenceBundleOfInputs <= budgetRestriction);

		assertTrue(function.f(rangeScanBundleOfInputs) <= function
				.f(referenceBundleOfInputs));
	}

	/**
	 * in an optimum partial derivatives per price have to be identical
	 */
	public void assertPartialDerivativesPerPriceAreEqual(
			final FunctionImpl<GoodType> function,
			final Map<GoodType, Double> bundleOfInputs,
			final Map<GoodType, PriceFunction> priceFunctions) {
		Map<GoodType, Double> partialDerivatives = function
				.partialDerivatives(bundleOfInputs);
		for (Entry<GoodType, Double> outerPartialDerivativeEntry : partialDerivatives
				.entrySet()) {
			PriceFunction outerPriceFunction = priceFunctions
					.get(outerPartialDerivativeEntry.getKey());
			double outerMarginalPrice = outerPriceFunction
					.getMarginalPrice(bundleOfInputs
							.get(outerPartialDerivativeEntry.getKey()));
			if (!Double.isNaN(outerMarginalPrice)) {
				for (Entry<GoodType, Double> innerPartialDerivativeEntry : partialDerivatives
						.entrySet()) {
					PriceFunction innerPriceFunction = priceFunctions
							.get(innerPartialDerivativeEntry.getKey());
					double innerMarginalPrice = innerPriceFunction
							.getMarginalPrice(bundleOfInputs
									.get(innerPartialDerivativeEntry.getKey()));
					if (!Double.isNaN(innerMarginalPrice)) {
						double innerPartialDerivativePerPrice = innerPartialDerivativeEntry
								.getValue() / innerMarginalPrice;
						double outerPartialDerivativePerPrice = outerPartialDerivativeEntry
								.getValue() / outerMarginalPrice;
						assertEquals(innerPartialDerivativePerPrice,
								outerPartialDerivativePerPrice, epsilon);
					}
				}
			}
		}
	}

	protected void setUpApplicationContext() {
		if (HibernateUtil.isActive()) {
			ApplicationContextFactory.configureHibernateApplicationContext();
		} else {
			ApplicationContextFactory.configureInMemoryApplicationContext();
		}

		// init database connection
		HibernateUtil.openSession();
	}

	protected void setUpApplicationContextWithAgents() {
		this.setUpApplicationContext();

		for (Currency currency : Currency.values()) {
			ApplicationContext.getInstance().getAgentService()
					.findCentralBank(currency);

			ApplicationContext.getInstance().getCreditBankFactory()
					.newInstanceCreditBank(currency);
			ApplicationContext.getInstance().getCreditBankFactory()
					.newInstanceCreditBank(currency);

			ApplicationContext.getInstance().getFactoryFactory()
					.newInstanceFactory(GoodType.WHEAT, currency);

			ApplicationContext.getInstance().getHouseholdFactory()
					.newInstanceHousehold(currency, 0);
			ApplicationContext.getInstance().getHouseholdFactory()
					.newInstanceHousehold(currency, 0);

			ApplicationContext.getInstance().getTraderFactory()
					.newInstanceTrader(currency);
		}

		HibernateUtil.flushSession();
	}

	protected void tearDown() {
		for (Household household : ApplicationContext.getInstance()
				.getHouseholdDAO().findAll()) {
			household.deconstruct();
		}

		for (Trader trader : ApplicationContext.getInstance().getTraderDAO()
				.findAll()) {
			trader.deconstruct();
		}

		for (Factory factory : ApplicationContext.getInstance().getFactoryDAO()
				.findAll()) {
			factory.deconstruct();
		}

		for (State state : ApplicationContext.getInstance().getStateDAO()
				.findAll()) {
			state.deconstruct();
		}

		for (CreditBank creditBank : ApplicationContext.getInstance()
				.getCreditBankDAO().findAll()) {
			creditBank.deconstruct();
		}

		for (CentralBank centralBank : ApplicationContext.getInstance()
				.getCentralBankDAO().findAll()) {
			centralBank.deconstruct();
		}

		HibernateUtil.flushSession();
		HibernateUtil.closeSession();

		ApplicationContext.getInstance().reset();
	}
}
