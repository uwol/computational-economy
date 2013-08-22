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

public class CobbDouglasFunctionTest {

	final double epsilon = 0.001;

	@Test
	public void calculateProductionOutputForGoodsWithRegularPrices() {
		Map<GoodType, Double> prices = new HashMap<GoodType, Double>();
		prices.put(GoodType.COAL, Double.NaN);
		prices.put(GoodType.KILOWATT, 1.0);
		prices.put(GoodType.WHEAT, 2.0);
		double budget = 10;

		Map<GoodType, Double> preferences1 = new HashMap<GoodType, Double>();
		preferences1.put(GoodType.KILOWATT, 0.4);
		preferences1.put(GoodType.WHEAT, 0.6);
		CobbDouglasFunction<GoodType> cobbDouglasFunction1 = new CobbDouglasFunction<GoodType>(
				1.0, preferences1);

		Map<GoodType, Double> amount = cobbDouglasFunction1
				.calculateOutputMaximizingInputsUnderBudgetRestriction(prices,
						budget);
		assertEquals(4., amount.get(GoodType.KILOWATT), epsilon);
		assertEquals(3., amount.get(GoodType.WHEAT), epsilon);

		amount = cobbDouglasFunction1
				.calculateOutputMaximizingInputsUnderBudgetRestrictionIterative(
						prices, budget);
		assertEquals(4., amount.get(GoodType.KILOWATT), epsilon);
		assertEquals(3., amount.get(GoodType.WHEAT), epsilon);
	}

	@Test
	public void calculateProductionOutputForGoodsWithNaNPrices() {
		Map<GoodType, Double> prices = new HashMap<GoodType, Double>();
		prices.put(GoodType.COAL, Double.NaN);
		prices.put(GoodType.KILOWATT, 1.0);
		prices.put(GoodType.WHEAT, 2.0);
		double budget = 10;

		Map<GoodType, Double> preferences2 = new HashMap<GoodType, Double>();
		preferences2.put(GoodType.COAL, 0.1);
		preferences2.put(GoodType.KILOWATT, 0.3);
		preferences2.put(GoodType.WHEAT, 0.6);
		CobbDouglasFunction<GoodType> cobbDouglasFunction2 = new CobbDouglasFunction<GoodType>(
				1.0, preferences2);

		Map<GoodType, Double> amount = cobbDouglasFunction2
				.calculateOutputMaximizingInputsUnderBudgetRestriction(prices,
						budget);
		assertEquals(0., amount.get(GoodType.COAL), epsilon);
		assertEquals(3., amount.get(GoodType.KILOWATT), epsilon);
		assertEquals(3., amount.get(GoodType.WHEAT), epsilon);

		amount = cobbDouglasFunction2
				.calculateOutputMaximizingInputsUnderBudgetRestrictionIterative(
						prices, budget);
		assertEquals(0., amount.get(GoodType.COAL), epsilon);
		assertEquals(3., amount.get(GoodType.KILOWATT), epsilon);
		assertEquals(3., amount.get(GoodType.WHEAT), epsilon);
	}
}
