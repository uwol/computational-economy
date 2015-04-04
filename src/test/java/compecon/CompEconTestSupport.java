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

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import compecon.economy.materia.GoodType;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.applicationcontext.ApplicationContextFactory;
import compecon.engine.util.HibernateUtil;
import compecon.math.impl.FunctionImpl;
import compecon.math.price.PriceFunction;

public abstract class CompEconTestSupport {

	protected final double epsilon = 0.01;

	protected final String testConfigurationPropertiesFilename = "testing.configuration.properties";

	protected void assertOutputIsOptimalUnderBudget(
			final FunctionImpl<GoodType> function,
			final double budgetRestriction,
			final Map<GoodType, PriceFunction> priceFunctions,
			final Map<GoodType, Double> referenceBundleOfInputs) {

		final Map<GoodType, Double> rangeScanBundleOfInputs = function
				.calculateOutputMaximizingInputsByRangeScan(priceFunctions,
						budgetRestriction);

		// check that budget restriction is not violated
		double sumOfCostsOfOptimalBundleOfInputs = 0.0;
		for (final Entry<GoodType, Double> inputEntry : rangeScanBundleOfInputs
				.entrySet()) {
			final double priceOfGoodType = priceFunctions.get(
					inputEntry.getKey()).getPrice(inputEntry.getValue());
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
		for (final Entry<GoodType, Double> inputEntry : referenceBundleOfInputs
				.entrySet()) {
			final double priceOfGoodType = priceFunctions.get(
					inputEntry.getKey()).getPrice(inputEntry.getValue());
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
	 * check that partial derivatives per price are identical. Criterion for an
	 * optimum.
	 */
	protected void assertPartialDerivativesPerPriceAreEqual(
			final FunctionImpl<GoodType> function,
			final Map<GoodType, Double> bundleOfInputs,
			final Map<GoodType, PriceFunction> priceFunctions) {
		final Map<GoodType, Double> partialDerivatives = function
				.partialDerivatives(bundleOfInputs);
		for (final Entry<GoodType, Double> outerPartialDerivativeEntry : partialDerivatives
				.entrySet()) {
			final PriceFunction outerPriceFunction = priceFunctions
					.get(outerPartialDerivativeEntry.getKey());
			final double outerMarginalPrice = outerPriceFunction
					.getMarginalPrice(bundleOfInputs
							.get(outerPartialDerivativeEntry.getKey()));
			if (!Double.isNaN(outerMarginalPrice)) {
				for (final Entry<GoodType, Double> innerPartialDerivativeEntry : partialDerivatives
						.entrySet()) {
					final PriceFunction innerPriceFunction = priceFunctions
							.get(innerPartialDerivativeEntry.getKey());
					final double innerMarginalPrice = innerPriceFunction
							.getMarginalPrice(bundleOfInputs
									.get(innerPartialDerivativeEntry.getKey()));
					if (!Double.isNaN(innerMarginalPrice)) {
						final double innerPartialDerivativePerPrice = innerPartialDerivativeEntry
								.getValue() / innerMarginalPrice;
						final double outerPartialDerivativePerPrice = outerPartialDerivativeEntry
								.getValue() / outerMarginalPrice;
						assertEquals(innerPartialDerivativePerPrice,
								outerPartialDerivativePerPrice, epsilon);
					}
				}
			}
		}
	}

	protected void setUpApplicationContext(
			final String configurationPropertiesFilename) throws IOException {
		if (HibernateUtil.isActive()) {
			ApplicationContextFactory
					.configureHibernateApplicationContext(configurationPropertiesFilename);
		} else {
			ApplicationContextFactory
					.configureInMemoryApplicationContext(configurationPropertiesFilename);
		}

		// init database connection
		HibernateUtil.openSession();
	}

	protected void setUpTestAgents() {
		for (final Currency currency : Currency.values()) {
			ApplicationContext.getInstance().getAgentService()
					.findCentralBank(currency);

			ApplicationContext.getInstance().getCreditBankFactory()
					.newInstanceCreditBank(currency);
			ApplicationContext.getInstance().getCreditBankFactory()
					.newInstanceCreditBank(currency);

			ApplicationContext.getInstance().getFactoryFactory()
					.newInstanceFactory(GoodType.WHEAT, currency);
			ApplicationContext.getInstance().getFactoryFactory()
					.newInstanceFactory(GoodType.COAL, currency);

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
		ApplicationContext.getInstance().getAgentFactory().deconstructAgents();

		HibernateUtil.flushSession();
		HibernateUtil.closeSession();

		ApplicationContext.getInstance().reset();
	}
}
