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

package io.github.uwol.compecon.math.intertemporal;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.github.uwol.compecon.CompEconTestSupport;
import io.github.uwol.compecon.math.intertemporal.IntertemporalConsumptionFunction;
import io.github.uwol.compecon.math.intertemporal.impl.ModiglianiIntertemporalConsumptionFunction;
import io.github.uwol.compecon.math.intertemporal.impl.IrvingFisherIntertemporalConsumptionFunction.Period;

public class ModiglianiIntertemporalConsumptionFunctionTest extends
		CompEconTestSupport {

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
	public void testCalculateUtilityMaximizingConsumptionPlanAfterRetirement() {
		/*
		 * prepare function
		 */
		final IntertemporalConsumptionFunction consumptionFunction = new ModiglianiIntertemporalConsumptionFunction();

		/*
		 * calculate distribution of income
		 */
		final double income = 0;
		final double assets = 100000;
		final double keyInterestRate = 0.01;
		final int ageInDays = 70 * 365;
		final int lifeSpanInDays = 80 * 365;
		final int retirementAgeInDays = 65 * 365;

		final Map<Period, Double> consumptionPlan = consumptionFunction
				.calculateUtilityMaximizingConsumptionPlan(income, assets,
						keyInterestRate, ageInDays, lifeSpanInDays,
						retirementAgeInDays);

		assertEquals(27.397260273972602, consumptionPlan.get(Period.CURRENT),
				epsilon);
		assertEquals(27.397260273972602, consumptionPlan.get(Period.NEXT),
				epsilon);
	}

	@Test
	public void testCalculateUtilityMaximizingConsumptionPlanBeforeRetirement() {
		/*
		 * prepare function
		 */
		final IntertemporalConsumptionFunction consumptionFunction = new ModiglianiIntertemporalConsumptionFunction();

		/*
		 * calculate distribution of income
		 */
		final double income = 100;
		final double assets = 0;
		final double keyInterestRate = 0.01;
		final int ageInDays = 20 * 365;
		final int lifeSpanInDays = 80 * 365;
		final int retirementAgeInDays = 65 * 365;

		final Map<Period, Double> consumptionPlan = consumptionFunction
				.calculateUtilityMaximizingConsumptionPlan(income, assets,
						keyInterestRate, ageInDays, lifeSpanInDays,
						retirementAgeInDays);

		assertEquals(75.0, consumptionPlan.get(Period.CURRENT), epsilon);
		assertEquals(75.0, consumptionPlan.get(Period.NEXT), epsilon);
	}
}
