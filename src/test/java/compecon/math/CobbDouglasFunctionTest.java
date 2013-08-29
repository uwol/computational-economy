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

import org.junit.Test;

import compecon.materia.GoodType;

public class CobbDouglasFunctionTest extends AbstractFunctionTest {

	final double epsilon = 0.01;

	@Test
	public void calculateForTwoGoods() {
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
		double budget = 10.0;

		Map<GoodType, Double> optimalInputsIterative = cobbDouglasFunction
				.calculateOutputMaximizingInputsUnderBudgetRestrictionIterative(
						prices, budget);
		Map<GoodType, Double> optimalInputsAnalytical = cobbDouglasFunction
				.calculateOutputMaximizingInputsUnderBudgetRestrictionAnalytical(
						prices, budget);
		Map<GoodType, Double> optimalInputsBruteForce = cobbDouglasFunction
				.calculateOutputMaximizingInputsUnderBudgetRestrictionByRangeScan(
						prices, budget);

		/*
		 * assert inputs
		 */
		assertEquals(4., optimalInputsAnalytical.get(GoodType.KILOWATT),
				epsilon);
		assertEquals(3., optimalInputsAnalytical.get(GoodType.WHEAT), epsilon);

		for (GoodType goodType : optimalInputsAnalytical.keySet()) {
			assertEquals(optimalInputsAnalytical.get(goodType),
					optimalInputsIterative.get(goodType), epsilon);
			assertEquals(optimalInputsAnalytical.get(goodType),
					optimalInputsBruteForce.get(goodType), epsilon);
		}

		/*
		 * assert output
		 */
		assertEquals(3.36586, cobbDouglasFunction.f(optimalInputsAnalytical),
				epsilon);

		assertOutputIsOptimalUnderBudget(cobbDouglasFunction, budget, prices,
				optimalInputsAnalytical);

		/*
		 * assert marginal outputs
		 */
		assertEquals(0.336586, cobbDouglasFunction.partialDerivative(
				optimalInputsAnalytical, GoodType.KILOWATT), epsilon);
		assertEquals(0.673173, cobbDouglasFunction.partialDerivative(
				optimalInputsAnalytical, GoodType.WHEAT), epsilon);

		assertPartialDerivativesPerPriceAreEqual(cobbDouglasFunction,
				optimalInputsIterative, prices);
		assertPartialDerivativesPerPriceAreEqual(cobbDouglasFunction,
				optimalInputsAnalytical, prices);
		assertPartialDerivativesPerPriceAreEqual(cobbDouglasFunction,
				optimalInputsBruteForce, prices);
	}

	@Test
	public void calculateForThreeGoodsWithNaNPrices() {
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
		double budget = 10.0;

		Map<GoodType, Double> optimalInputsAnalytical = cobbDouglasFunction
				.calculateOutputMaximizingInputsUnderBudgetRestrictionAnalytical(
						prices, budget);
		Map<GoodType, Double> optimalInputsIterative = cobbDouglasFunction
				.calculateOutputMaximizingInputsUnderBudgetRestrictionIterative(
						prices, budget);
		Map<GoodType, Double> optimalInputsBruteForce = cobbDouglasFunction
				.calculateOutputMaximizingInputsUnderBudgetRestrictionByRangeScan(
						prices, budget);

		/*
		 * assert inputs
		 */
		assertEquals(0.0, optimalInputsAnalytical.get(GoodType.COAL), epsilon);
		assertEquals(0.0, optimalInputsAnalytical.get(GoodType.KILOWATT),
				epsilon);
		assertEquals(0.0, optimalInputsAnalytical.get(GoodType.WHEAT), epsilon);

		for (GoodType goodType : optimalInputsAnalytical.keySet()) {
			assertEquals(optimalInputsAnalytical.get(goodType),
					optimalInputsIterative.get(goodType), epsilon);
			assertEquals(optimalInputsAnalytical.get(goodType),
					optimalInputsBruteForce.get(goodType), epsilon);
		}

		/*
		 * assert output
		 */
		assertEquals(0.0, cobbDouglasFunction.f(optimalInputsAnalytical),
				epsilon);

		assertOutputIsOptimalUnderBudget(cobbDouglasFunction, budget, prices,
				optimalInputsAnalytical);

		/*
		 * assert marginal outputs
		 */
		assertEquals(0.0, cobbDouglasFunction.partialDerivative(
				optimalInputsAnalytical, GoodType.KILOWATT), epsilon);
		assertEquals(0.0, cobbDouglasFunction.partialDerivative(
				optimalInputsAnalytical, GoodType.WHEAT), epsilon);

		assertPartialDerivativesPerPriceAreEqual(cobbDouglasFunction,
				optimalInputsIterative, prices);
		assertPartialDerivativesPerPriceAreEqual(cobbDouglasFunction,
				optimalInputsAnalytical, prices);
		assertPartialDerivativesPerPriceAreEqual(cobbDouglasFunction,
				optimalInputsBruteForce, prices);
	}
}
