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

package compecon.math.utility;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import compecon.materia.GoodType;
import compecon.math.utility.CobbDouglasUtilityFunction;

public class CobbDouglasUtilityFunctionTest {

	public final double epsilon = 0.001;

	@Test
	public void calculateUtility() {
		Map<GoodType, Double> preferences = new HashMap<GoodType, Double>();
		preferences.put(GoodType.KILOWATT, 0.4);
		preferences.put(GoodType.WHEAT, 0.6);
		CobbDouglasUtilityFunction cobbDouglasUtilityFunction = new CobbDouglasUtilityFunction(
				preferences, 1);

		Map<GoodType, Double> prices = new HashMap<GoodType, Double>();
		prices.put(GoodType.KILOWATT, 1.0);
		prices.put(GoodType.WHEAT, 2.0);
		double budget = 10;

		Map<GoodType, Double> amount = cobbDouglasUtilityFunction
				.calculateUtilityMaximizingInputsUnderBudgetRestriction(prices,
						budget);
		assertEquals(4., amount.get(GoodType.KILOWATT), epsilon);
		assertEquals(3., amount.get(GoodType.WHEAT), epsilon);
	}
}
