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

public class CESFunctionTest extends AbstractFunctionTest {

	final double epsilon = 0.01;

	@Test
	public void calculateForTwoGoods() {
		/*
		 * prepare function
		 */
		Map<GoodType, Double> coefficients = new HashMap<GoodType, Double>();
		coefficients.put(GoodType.KILOWATT, 0.4);
		coefficients.put(GoodType.WHEAT, 0.6);
		CESFunction<GoodType> cesFunction = new CESFunction<GoodType>(1.0,
				coefficients, -0.5, 0.4);

		/*
		 * maximize output under budget restriction
		 */
		Map<GoodType, Double> prices = new HashMap<GoodType, Double>();
		prices.put(GoodType.COAL, Double.NaN);
		prices.put(GoodType.KILOWATT, 1.0);
		prices.put(GoodType.WHEAT, 1.0);
		double budget = 10.0;

		Map<GoodType, Double> optimalInputsIterative = cesFunction
				.calculateOutputMaximizingInputsUnderBudgetRestrictionIterative(
						prices, budget);
		Map<GoodType, Double> optimalInputsAnalytical = cesFunction
				.calculateOutputMaximizingInputsUnderBudgetRestrictionAnalytical(
						prices, budget);
		Map<GoodType, Double> optimalInputsBruteForce = cesFunction
				.calculateOutputMaximizingInputsUnderBudgetRestrictionByRangeScan(
						prices, budget);

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
		assertOutputIsOptimalUnderBudget(cesFunction, budget, prices,
				optimalInputsAnalytical);

		/*
		 * assert marginal outputs
		 */
		assertPartialDerivativesPerPriceAreEqual(cesFunction,
				optimalInputsIterative, prices);
		assertPartialDerivativesPerPriceAreEqual(cesFunction,
				optimalInputsAnalytical, prices);
		assertPartialDerivativesPerPriceAreEqual(cesFunction,
				optimalInputsBruteForce, prices);
	}

	@Test
	public void calculateForThreeGoods() {
		/*
		 * prepare function
		 */
		Map<GoodType, Double> coefficients = new HashMap<GoodType, Double>();
		coefficients.put(GoodType.KILOWATT, 0.1);
		coefficients.put(GoodType.URANIUM, 0.2);
		coefficients.put(GoodType.WHEAT, 0.7);
		CESFunction<GoodType> cesFunction = new CESFunction<GoodType>(1.0,
				coefficients, -0.5, 0.4);

		/*
		 * maximize output under budget restriction
		 */
		Map<GoodType, Double> prices = new HashMap<GoodType, Double>();
		prices.put(GoodType.COAL, Double.NaN);
		prices.put(GoodType.KILOWATT, 1.0);
		prices.put(GoodType.URANIUM, 3.0);
		prices.put(GoodType.WHEAT, 2.0);
		double budget = 10.0;

		Map<GoodType, Double> optimalInputsIterative = cesFunction
				.calculateOutputMaximizingInputsUnderBudgetRestrictionIterative(
						prices, budget);
		Map<GoodType, Double> optimalInputsAnalytical = cesFunction
				.calculateOutputMaximizingInputsUnderBudgetRestrictionAnalytical(
						prices, budget);
		Map<GoodType, Double> optimalInputsBruteForce = cesFunction
				.calculateOutputMaximizingInputsUnderBudgetRestrictionByRangeScan(
						prices, budget);

		/*
		 * assert inputs
		 */
		assertEquals(0.373, optimalInputsAnalytical.get(GoodType.KILOWATT),
				epsilon);
		assertEquals(4.565, optimalInputsAnalytical.get(GoodType.WHEAT),
				epsilon);
		assertEquals(0.166, optimalInputsAnalytical.get(GoodType.URANIUM),
				epsilon);

		for (GoodType goodType : optimalInputsAnalytical.keySet()) {
			assertEquals(optimalInputsAnalytical.get(goodType),
					optimalInputsIterative.get(goodType), epsilon);
			assertEquals(optimalInputsAnalytical.get(goodType),
					optimalInputsBruteForce.get(goodType), epsilon);
		}

		/*
		 * assert output
		 */
		assertOutputIsOptimalUnderBudget(cesFunction, budget, prices,
				optimalInputsAnalytical);

		/*
		 * assert marginal outputs
		 */
		assertPartialDerivativesPerPriceAreEqual(cesFunction,
				optimalInputsIterative, prices);
		assertPartialDerivativesPerPriceAreEqual(cesFunction,
				optimalInputsAnalytical, prices);
		assertPartialDerivativesPerPriceAreEqual(cesFunction,
				optimalInputsBruteForce, prices);

	}

	@Test
	public void calculateForThreeGoodsWithNaNPrices() {
		/*
		 * prepare function
		 */
		Map<GoodType, Double> coefficients = new HashMap<GoodType, Double>();
		coefficients.put(GoodType.COAL, 0.1);
		coefficients.put(GoodType.KILOWATT, 0.3);
		coefficients.put(GoodType.WHEAT, 0.6);
		CESFunction<GoodType> cesFunction = new CESFunction<GoodType>(1.0,
				coefficients, -0.5, 0.4);

		/*
		 * maximize output under budget restriction
		 */
		Map<GoodType, Double> prices = new HashMap<GoodType, Double>();
		prices.put(GoodType.COAL, Double.NaN);
		prices.put(GoodType.KILOWATT, 1.0);
		prices.put(GoodType.WHEAT, 2.0);
		double budget = 10.0;

		Map<GoodType, Double> optimalInputsIterative = cesFunction
				.calculateOutputMaximizingInputsUnderBudgetRestrictionIterative(
						prices, budget);
		Map<GoodType, Double> optimalInputsAnalytical = cesFunction
				.calculateOutputMaximizingInputsUnderBudgetRestrictionAnalytical(
						prices, budget);
		Map<GoodType, Double> optimalInputsBruteForce = cesFunction
				.calculateOutputMaximizingInputsUnderBudgetRestrictionByRangeScan(
						prices, budget);

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
		assertOutputIsOptimalUnderBudget(cesFunction, budget, prices,
				optimalInputsAnalytical);

		/*
		 * assert marginal outputs
		 */
		assertPartialDerivativesPerPriceAreEqual(cesFunction,
				optimalInputsIterative, prices);
		assertPartialDerivativesPerPriceAreEqual(cesFunction,
				optimalInputsAnalytical, prices);
		assertPartialDerivativesPerPriceAreEqual(cesFunction,
				optimalInputsBruteForce, prices);
	}
}
