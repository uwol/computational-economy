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

package io.github.uwol.compecon.math.production;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.github.uwol.compecon.CompEconTestSupport;
import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.economy.sectors.household.Household;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.service.impl.FixedPriceFunctionImpl;
import io.github.uwol.compecon.math.price.PriceFunction;
import io.github.uwol.compecon.math.production.impl.CobbDouglasProductionFunctionImpl;

public class CobbDouglasProductionFunctionTest extends CompEconTestSupport {

	final int numberOfIterations = 2000;

	@Before
	public void setup() throws IOException {
		super.setUpApplicationContext(testConfigurationPropertiesFilename);
		super.setUpTestAgents();
	}

	@Override
	@After
	public void tearDown() {
		super.tearDown();
	}

	@Test
	public void testCalculateProductionOutputWithFixedPrices() {
		/*
		 * prepare function
		 */
		final Map<GoodType, Double> exponents = new HashMap<GoodType, Double>();
		exponents.put(GoodType.KILOWATT, 0.4);
		exponents.put(GoodType.WHEAT, 0.6);
		final CobbDouglasProductionFunctionImpl cobbDouglasProductionFunction = new CobbDouglasProductionFunctionImpl(
				1.0, exponents);

		/*
		 * maximize output under budget restriction
		 */
		final Map<GoodType, PriceFunction> priceFunctions = new HashMap<GoodType, PriceFunction>();
		priceFunctions.put(GoodType.KILOWATT, new FixedPriceFunctionImpl(1.0));
		priceFunctions.put(GoodType.WHEAT, new FixedPriceFunctionImpl(2.0));

		final double budget = 10.0;

		final Map<GoodType, Double> optimalInputsIterative = cobbDouglasProductionFunction
				.calculateProfitMaximizingProductionFactorsIterative(10.0, priceFunctions, null, budget, Double.NaN,
						0.0, numberOfIterations);

		/*
		 * assert inputs
		 */
		assertEquals(4.0, optimalInputsIterative.get(GoodType.KILOWATT), epsilon);
		assertEquals(3.0, optimalInputsIterative.get(GoodType.WHEAT), epsilon);

		/*
		 * assert output
		 */
		assertEquals(3.36586, cobbDouglasProductionFunction.calculateOutput(optimalInputsIterative), epsilon);

		/*
		 * assert marginal outputs
		 */
		assertEquals(0.336586,
				cobbDouglasProductionFunction.calculateMarginalOutput(optimalInputsIterative, GoodType.KILOWATT),
				epsilon);
		assertEquals(0.673173,
				cobbDouglasProductionFunction.calculateMarginalOutput(optimalInputsIterative, GoodType.WHEAT), epsilon);
	}

	@Test
	public void testCalculateProductionOutputWithMarketPrices() {
		final Currency currency = Currency.EURO;

		final Household household1_EUR = ApplicationContext.getInstance().getAgentService().findHouseholds(currency)
				.get(0);
		final Household household2_EUR = ApplicationContext.getInstance().getAgentService().findHouseholds(currency)
				.get(1);

		ApplicationContext.getInstance().getMarketService().placeSellingOffer(GoodType.KILOWATT, household1_EUR,
				household1_EUR.getBankAccountTransactionsDelegate(), 10, 2);
		ApplicationContext.getInstance().getMarketService().placeSellingOffer(GoodType.KILOWATT, household2_EUR,
				household2_EUR.getBankAccountTransactionsDelegate(), 10, 1);
		ApplicationContext.getInstance().getMarketService().placeSellingOffer(GoodType.WHEAT, household1_EUR,
				household1_EUR.getBankAccountTransactionsDelegate(), 10, 1);

		/*
		 * prepare function
		 */
		final Map<GoodType, Double> exponents = new HashMap<GoodType, Double>();
		exponents.put(GoodType.KILOWATT, 0.4);
		exponents.put(GoodType.WHEAT, 0.6);
		final CobbDouglasProductionFunctionImpl cobbDouglasProductionFunction = new CobbDouglasProductionFunctionImpl(
				1.0, exponents);

		/*
		 * maximize output under budget restriction
		 */
		final Map<GoodType, PriceFunction> priceFunctions = new HashMap<GoodType, PriceFunction>();
		priceFunctions.put(GoodType.KILOWATT, ApplicationContext.getInstance().getMarketService()
				.getMarketPriceFunction(currency, GoodType.KILOWATT));
		priceFunctions.put(GoodType.WHEAT,
				ApplicationContext.getInstance().getMarketService().getMarketPriceFunction(currency, GoodType.WHEAT));

		final double budget = 50.0;

		final Map<GoodType, Double> optimalInputsIterative = cobbDouglasProductionFunction
				.calculateProfitMaximizingProductionFactorsIterative(10.0, priceFunctions, null, budget, Double.NaN,
						0.0, numberOfIterations);

		/*
		 * assert inputs
		 */
		assertEquals(20.0, optimalInputsIterative.get(GoodType.KILOWATT), epsilon * 2.0);
		assertEquals(10.0, optimalInputsIterative.get(GoodType.WHEAT), epsilon * 2.0);

		/*
		 * assert output
		 */
		assertEquals(13.2, cobbDouglasProductionFunction.calculateOutput(optimalInputsIterative), epsilon);

		/*
		 * assert marginal outputs
		 */
		assertEquals(0.264,
				cobbDouglasProductionFunction.calculateMarginalOutput(optimalInputsIterative, GoodType.KILOWATT),
				epsilon);
		assertEquals(0.7914,
				cobbDouglasProductionFunction.calculateMarginalOutput(optimalInputsIterative, GoodType.WHEAT), epsilon);
	}

	@Test
	public void testCalculateProductionOutputWithMarketPricesAndCapital() {
		final Currency currency = Currency.EURO;

		final Household household1_EUR = ApplicationContext.getInstance().getAgentService().findHouseholds(currency)
				.get(0);
		final Household household2_EUR = ApplicationContext.getInstance().getAgentService().findHouseholds(currency)
				.get(1);

		ApplicationContext.getInstance().getMarketService().placeSellingOffer(GoodType.KILOWATT, household1_EUR,
				household1_EUR.getBankAccountTransactionsDelegate(), 10, 2);
		ApplicationContext.getInstance().getMarketService().placeSellingOffer(GoodType.KILOWATT, household2_EUR,
				household2_EUR.getBankAccountTransactionsDelegate(), 10, 1);
		ApplicationContext.getInstance().getMarketService().placeSellingOffer(GoodType.WHEAT, household1_EUR,
				household1_EUR.getBankAccountTransactionsDelegate(), 10, 1);

		/*
		 * capital
		 */
		final Map<GoodType, Double> capital = new HashMap<GoodType, Double>();
		capital.put(GoodType.MACHINE, 5.0);

		/*
		 * prepare function
		 */
		final Map<GoodType, Double> exponents = new HashMap<GoodType, Double>();
		exponents.put(GoodType.KILOWATT, 0.4);
		exponents.put(GoodType.WHEAT, 0.5);
		exponents.put(GoodType.MACHINE, 0.1);
		final CobbDouglasProductionFunctionImpl cobbDouglasProductionFunction = new CobbDouglasProductionFunctionImpl(
				1.0, exponents);

		/*
		 * maximize output under budget restriction
		 */
		final Map<GoodType, PriceFunction> priceFunctions = new HashMap<GoodType, PriceFunction>();
		priceFunctions.put(GoodType.KILOWATT, ApplicationContext.getInstance().getMarketService()
				.getMarketPriceFunction(currency, GoodType.KILOWATT));
		priceFunctions.put(GoodType.WHEAT,
				ApplicationContext.getInstance().getMarketService().getMarketPriceFunction(currency, GoodType.WHEAT));
		priceFunctions.put(GoodType.MACHINE,
				ApplicationContext.getInstance().getMarketService().getMarketPriceFunction(currency, GoodType.MACHINE));

		final double budget = 50.0;

		final Map<GoodType, Double> optimalInputsIterative = cobbDouglasProductionFunction
				.calculateProfitMaximizingProductionFactorsIterative(10.0, priceFunctions, capital, budget, Double.NaN,
						0.0, numberOfIterations);

		/*
		 * assert inputs
		 */
		assertEquals(20.0, optimalInputsIterative.get(GoodType.KILOWATT), epsilon * 2.0);
		assertEquals(10.0, optimalInputsIterative.get(GoodType.WHEAT), epsilon * 2.0);
		assertEquals(5.0, optimalInputsIterative.get(GoodType.MACHINE), epsilon * 2.0);

		/*
		 * assert output
		 */
		assertEquals(12.31144413, cobbDouglasProductionFunction.calculateOutput(optimalInputsIterative), epsilon);

		/*
		 * assert marginal outputs
		 */
		assertEquals(0.246228883,
				cobbDouglasProductionFunction.calculateMarginalOutput(optimalInputsIterative, GoodType.KILOWATT),
				epsilon);
		assertEquals(0.615572207,
				cobbDouglasProductionFunction.calculateMarginalOutput(optimalInputsIterative, GoodType.WHEAT), epsilon);
		assertEquals(0.246228883,
				cobbDouglasProductionFunction.calculateMarginalOutput(optimalInputsIterative, GoodType.MACHINE),
				epsilon);
	}
}
