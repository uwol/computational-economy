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

package compecon.math;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import compecon.CompEconTestSupport;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.engine.MarketFactory;
import compecon.engine.dao.DAOFactory;
import compecon.materia.GoodType;
import compecon.math.price.FixedPriceFunction;
import compecon.math.price.IPriceFunction;

public class CobbDouglasFunctionTest extends CompEconTestSupport {

	final int numberOfIterations = 500;

	@Before
	public void setUp() {
		super.setUp();
	}

	@After
	public void tearDown() {
		super.tearDown();
	}

	@Test
	public void testCalculateForTwoGoodsWithFixedPrices() {
		/*
		 * prepare function
		 */
		Map<GoodType, Double> exponents = new HashMap<GoodType, Double>();
		exponents.put(GoodType.KILOWATT, 0.4);
		exponents.put(GoodType.WHEAT, 0.6);
		CobbDouglasFunction<GoodType> cobbDouglasFunction = new CobbDouglasFunction<GoodType>(
				1.0, exponents);

		/*
		 * maximize output under budget restriction
		 */
		Map<GoodType, Double> prices = new HashMap<GoodType, Double>();
		prices.put(GoodType.COAL, Double.NaN);
		prices.put(GoodType.KILOWATT, 1.0);
		prices.put(GoodType.WHEAT, 2.0);

		Map<GoodType, IPriceFunction> priceFunctions = new HashMap<GoodType, IPriceFunction>();
		priceFunctions.put(GoodType.COAL, new FixedPriceFunction(Double.NaN));
		priceFunctions.put(GoodType.KILOWATT, new FixedPriceFunction(1.0));
		priceFunctions.put(GoodType.WHEAT, new FixedPriceFunction(2.0));

		double budget = 10.0;

		Map<GoodType, Double> optimalInputsAnalyticalFixedPrices = cobbDouglasFunction
				.calculateOutputMaximizingInputsAnalyticalWithFixedPrices(
						prices, budget);
		Map<GoodType, Double> optimalInputsAnalyticalPriceFunctions = cobbDouglasFunction
				.calculateOutputMaximizingInputsAnalyticalWithPriceFunctions(
						priceFunctions, budget);
		Map<GoodType, Double> optimalInputsIterative = cobbDouglasFunction
				.calculateOutputMaximizingInputsIterative(priceFunctions,
						budget, numberOfIterations);
		Map<GoodType, Double> optimalInputsBruteForce = cobbDouglasFunction
				.calculateOutputMaximizingInputsByRangeScan(priceFunctions,
						budget);

		/*
		 * assert inputs
		 */
		assertEquals(4.,
				optimalInputsAnalyticalFixedPrices.get(GoodType.KILOWATT),
				epsilon);
		assertEquals(3.,
				optimalInputsAnalyticalFixedPrices.get(GoodType.WHEAT), epsilon);

		for (GoodType goodType : optimalInputsAnalyticalFixedPrices.keySet()) {
			assertEquals(optimalInputsAnalyticalFixedPrices.get(goodType),
					optimalInputsAnalyticalPriceFunctions.get(goodType),
					epsilon);
			assertEquals(optimalInputsAnalyticalFixedPrices.get(goodType),
					optimalInputsIterative.get(goodType), epsilon);
			assertEquals(optimalInputsAnalyticalFixedPrices.get(goodType),
					optimalInputsBruteForce.get(goodType), epsilon);
		}

		/*
		 * assert output
		 */
		assertEquals(3.36586,
				cobbDouglasFunction.f(optimalInputsAnalyticalFixedPrices),
				epsilon);

		assertOutputIsOptimalUnderBudget(cobbDouglasFunction, budget,
				priceFunctions, optimalInputsAnalyticalFixedPrices);

		/*
		 * assert marginal outputs
		 */
		assertEquals(0.336586, cobbDouglasFunction.partialDerivative(
				optimalInputsAnalyticalFixedPrices, GoodType.KILOWATT), epsilon);
		assertEquals(0.673173, cobbDouglasFunction.partialDerivative(
				optimalInputsAnalyticalFixedPrices, GoodType.WHEAT), epsilon);

		assertPartialDerivativesPerPriceAreEqual(cobbDouglasFunction,
				optimalInputsAnalyticalFixedPrices, priceFunctions);
		assertPartialDerivativesPerPriceAreEqual(cobbDouglasFunction,
				optimalInputsAnalyticalPriceFunctions, priceFunctions);
		assertPartialDerivativesPerPriceAreEqual(cobbDouglasFunction,
				optimalInputsIterative, priceFunctions);
		assertPartialDerivativesPerPriceAreEqual(cobbDouglasFunction,
				optimalInputsBruteForce, priceFunctions);
	}

	@Test
	public void testCalculateForThreeGoodsWithNaNFixedPrices() {
		/*
		 * prepare function
		 */
		Map<GoodType, Double> exponents = new HashMap<GoodType, Double>();
		exponents.put(GoodType.COAL, 0.1);
		exponents.put(GoodType.KILOWATT, 0.3);
		exponents.put(GoodType.WHEAT, 0.6);
		CobbDouglasFunction<GoodType> cobbDouglasFunction = new CobbDouglasFunction<GoodType>(
				1.0, exponents);

		/*
		 * maximize output under budget restriction
		 */
		Map<GoodType, Double> prices = new HashMap<GoodType, Double>();
		prices.put(GoodType.COAL, Double.NaN);
		prices.put(GoodType.KILOWATT, 1.0);
		prices.put(GoodType.WHEAT, 2.0);

		Map<GoodType, IPriceFunction> priceFunctions = new HashMap<GoodType, IPriceFunction>();
		priceFunctions.put(GoodType.COAL, new FixedPriceFunction(Double.NaN));
		priceFunctions.put(GoodType.KILOWATT, new FixedPriceFunction(1.0));
		priceFunctions.put(GoodType.WHEAT, new FixedPriceFunction(2.0));

		double budget = 10.0;

		Map<GoodType, Double> optimalInputsAnalyticalFixedPrices = cobbDouglasFunction
				.calculateOutputMaximizingInputsAnalyticalWithFixedPrices(
						prices, budget);
		Map<GoodType, Double> optimalInputsAnalyticalPriceFunctions = cobbDouglasFunction
				.calculateOutputMaximizingInputsAnalyticalWithPriceFunctions(
						priceFunctions, budget);
		Map<GoodType, Double> optimalInputsIterative = cobbDouglasFunction
				.calculateOutputMaximizingInputsIterative(priceFunctions,
						budget, numberOfIterations);
		Map<GoodType, Double> optimalInputsBruteForce = cobbDouglasFunction
				.calculateOutputMaximizingInputsByRangeScan(priceFunctions,
						budget);

		/*
		 * assert inputs
		 */
		assertEquals(0.0,
				optimalInputsAnalyticalFixedPrices.get(GoodType.COAL), epsilon);
		assertEquals(0.0,
				optimalInputsAnalyticalFixedPrices.get(GoodType.KILOWATT),
				epsilon);
		assertEquals(0.0,
				optimalInputsAnalyticalFixedPrices.get(GoodType.WHEAT), epsilon);

		for (GoodType goodType : optimalInputsAnalyticalFixedPrices.keySet()) {
			assertEquals(optimalInputsAnalyticalFixedPrices.get(goodType),
					optimalInputsAnalyticalPriceFunctions.get(goodType),
					epsilon);
			assertEquals(optimalInputsAnalyticalFixedPrices.get(goodType),
					optimalInputsIterative.get(goodType), epsilon);
			assertEquals(optimalInputsAnalyticalFixedPrices.get(goodType),
					optimalInputsBruteForce.get(goodType), epsilon);
		}

		/*
		 * assert output
		 */
		assertEquals(0.0,
				cobbDouglasFunction.f(optimalInputsAnalyticalFixedPrices),
				epsilon);

		assertOutputIsOptimalUnderBudget(cobbDouglasFunction, budget,
				priceFunctions, optimalInputsAnalyticalFixedPrices);

		/*
		 * assert marginal outputs
		 */
		assertEquals(0.0, cobbDouglasFunction.partialDerivative(
				optimalInputsAnalyticalFixedPrices, GoodType.KILOWATT), epsilon);
		assertEquals(0.0, cobbDouglasFunction.partialDerivative(
				optimalInputsAnalyticalFixedPrices, GoodType.WHEAT), epsilon);

		assertPartialDerivativesPerPriceAreEqual(cobbDouglasFunction,
				optimalInputsAnalyticalFixedPrices, priceFunctions);
		assertPartialDerivativesPerPriceAreEqual(cobbDouglasFunction,
				optimalInputsAnalyticalPriceFunctions, priceFunctions);
		assertPartialDerivativesPerPriceAreEqual(cobbDouglasFunction,
				optimalInputsIterative, priceFunctions);
		assertPartialDerivativesPerPriceAreEqual(cobbDouglasFunction,
				optimalInputsBruteForce, priceFunctions);
	}

	@Test
	public void testCalculateForTwoGoodsWithMarketPrices() {

		/*
		 * prepare market
		 */

		Currency currency = Currency.EURO;

		Household household1_EUR = DAOFactory.getHouseholdDAO()
				.findAllByCurrency(currency).get(0);
		Household household2_EUR = DAOFactory.getHouseholdDAO()
				.findAllByCurrency(currency).get(1);

		assertEquals(
				Double.NaN,
				MarketFactory.getInstance().getPrice(currency,
						GoodType.KILOWATT), epsilon);
		assertEquals(Double.NaN,
				MarketFactory.getInstance().getPrice(currency, GoodType.WHEAT),
				epsilon);

		MarketFactory.getInstance().placeSellingOffer(GoodType.KILOWATT,
				household1_EUR, household1_EUR.getTransactionsBankAccount(),
				5.0, 2.0);
		MarketFactory.getInstance().placeSellingOffer(GoodType.KILOWATT,
				household2_EUR, household2_EUR.getTransactionsBankAccount(),
				5.0, 1.0);

		MarketFactory.getInstance().placeSellingOffer(GoodType.WHEAT,
				household1_EUR, household1_EUR.getTransactionsBankAccount(),
				3.0, 2.0);
		MarketFactory.getInstance().placeSellingOffer(GoodType.WHEAT,
				household2_EUR, household2_EUR.getTransactionsBankAccount(),
				2.0, 1.0);

		/*
		 * prepare function
		 */
		Map<GoodType, Double> exponents = new HashMap<GoodType, Double>();
		exponents.put(GoodType.KILOWATT, 0.4);
		exponents.put(GoodType.WHEAT, 0.6);
		CobbDouglasFunction<GoodType> cobbDouglasFunction = new CobbDouglasFunction<GoodType>(
				1.0, exponents);

		/*
		 * maximize output under budget restriction
		 */
		Map<GoodType, IPriceFunction> priceFunctions = MarketFactory
				.getInstance().getPriceFunctions(currency,
						new GoodType[] { GoodType.KILOWATT, GoodType.WHEAT });

		double budget = 10.0;

		Map<GoodType, Double> optimalInputsAnalytical = cobbDouglasFunction
				.calculateOutputMaximizingInputsAnalyticalWithPriceFunctions(
						priceFunctions, budget);
		Map<GoodType, Double> optimalInputsIterative = cobbDouglasFunction
				.calculateOutputMaximizingInputsIterative(priceFunctions,
						budget, numberOfIterations);
		Map<GoodType, Double> optimalInputsBruteForce = cobbDouglasFunction
				.calculateOutputMaximizingInputsByRangeScan(priceFunctions,
						budget);

		/*
		 * assert inputs
		 */
		for (GoodType goodType : optimalInputsAnalytical.keySet()) {
			assertEquals(optimalInputsAnalytical.get(goodType),
					optimalInputsIterative.get(goodType), epsilon);
			assertEquals(optimalInputsAnalytical.get(goodType),
					optimalInputsBruteForce.get(goodType), epsilon);
		}

		/*
		 * assert output
		 */

		/*
		 * assert marginal outputs
		 */
		assertPartialDerivativesPerPriceAreEqual(cobbDouglasFunction,
				optimalInputsAnalytical, priceFunctions);
		assertPartialDerivativesPerPriceAreEqual(cobbDouglasFunction,
				optimalInputsIterative, priceFunctions);
		assertPartialDerivativesPerPriceAreEqual(cobbDouglasFunction,
				optimalInputsBruteForce, priceFunctions);
	}

	@Test
	public void testCalculateForTwoGoodsWithNaNMarketPrices() {

		/*
		 * prepare market
		 */

		Currency currency = Currency.EURO;

		Household household1_EUR = DAOFactory.getHouseholdDAO()
				.findAllByCurrency(currency).get(0);
		Household household2_EUR = DAOFactory.getHouseholdDAO()
				.findAllByCurrency(currency).get(1);

		assertEquals(
				Double.NaN,
				MarketFactory.getInstance().getPrice(currency,
						GoodType.KILOWATT), epsilon);
		assertEquals(Double.NaN,
				MarketFactory.getInstance().getPrice(currency, GoodType.WHEAT),
				epsilon);

		MarketFactory.getInstance().placeSellingOffer(GoodType.KILOWATT,
				household1_EUR, household1_EUR.getTransactionsBankAccount(),
				20.0, 2.0);
		MarketFactory.getInstance().placeSellingOffer(GoodType.KILOWATT,
				household2_EUR, household2_EUR.getTransactionsBankAccount(),
				5.0, 1.0);

		MarketFactory.getInstance().placeSellingOffer(GoodType.WHEAT,
				household1_EUR, household1_EUR.getTransactionsBankAccount(),
				20.0, 2.0);
		MarketFactory.getInstance().placeSellingOffer(GoodType.WHEAT,
				household2_EUR, household2_EUR.getTransactionsBankAccount(),
				4.0, 1.0);

		/*
		 * prepare function
		 */
		Map<GoodType, Double> exponents = new HashMap<GoodType, Double>();
		exponents.put(GoodType.KILOWATT, 0.4);
		exponents.put(GoodType.WHEAT, 0.6);
		CobbDouglasFunction<GoodType> cobbDouglasFunction = new CobbDouglasFunction<GoodType>(
				1.0, exponents);

		/*
		 * maximize output under budget restriction
		 */
		Map<GoodType, IPriceFunction> priceFunctions = MarketFactory
				.getInstance().getPriceFunctions(currency,
						new GoodType[] { GoodType.KILOWATT, GoodType.WHEAT });

		// FIXME: problematic with 6.7 < budget < 15.8 as
		// partialDerivativesPerPrice
		// are not equal -> analytical solution is not found
		double budget = 21;

		Map<GoodType, Double> optimalInputsAnalytical = cobbDouglasFunction
				.calculateOutputMaximizingInputsAnalyticalWithPriceFunctions(
						priceFunctions, budget);
		Map<GoodType, Double> optimalInputsIterative = cobbDouglasFunction
				.calculateOutputMaximizingInputsIterative(priceFunctions,
						budget, numberOfIterations);
		Map<GoodType, Double> optimalInputsBruteForce = cobbDouglasFunction
				.calculateOutputMaximizingInputsByRangeScan(priceFunctions,
						budget);

		/*
		 * assert inputs
		 */
		for (GoodType goodType : optimalInputsAnalytical.keySet()) {
			assertEquals(optimalInputsAnalytical.get(goodType),
					optimalInputsIterative.get(goodType), epsilon);
			assertEquals(optimalInputsAnalytical.get(goodType),
					optimalInputsBruteForce.get(goodType), epsilon);
		}

		/*
		 * assert output
		 */

		/*
		 * assert marginal outputs
		 */
		assertPartialDerivativesPerPriceAreEqual(cobbDouglasFunction,
				optimalInputsAnalytical, priceFunctions);
		assertPartialDerivativesPerPriceAreEqual(cobbDouglasFunction,
				optimalInputsIterative, priceFunctions);
		assertPartialDerivativesPerPriceAreEqual(cobbDouglasFunction,
				optimalInputsBruteForce, priceFunctions);
	}
}
