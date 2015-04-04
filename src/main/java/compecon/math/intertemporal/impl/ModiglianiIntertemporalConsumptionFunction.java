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

package compecon.math.intertemporal.impl;

import java.util.HashMap;
import java.util.Map;

import compecon.math.intertemporal.IntertemporalConsumptionFunction;
import compecon.math.intertemporal.impl.IrvingFisherIntertemporalConsumptionFunction.Period;

public class ModiglianiIntertemporalConsumptionFunction implements
		IntertemporalConsumptionFunction {

	@Override
	public Map<Period, Double> calculateUtilityMaximizingConsumptionPlan(
			final double averageIncomePerPeriod, final double currentAssets,
			final double keyInterestRate, final int ageInDays,
			final int lifeSpanInDays, final int retirementAgeInDays) {
		final int averageRemainingLifeDays = lifeSpanInDays - ageInDays;
		final int remainingDaysUntilRetirement = Math.max(retirementAgeInDays
				- ageInDays, 0);

		final double dailyConsumption;

		if (averageRemainingLifeDays > 0) {
			final double lifeConsumption = currentAssets
					+ averageIncomePerPeriod * remainingDaysUntilRetirement;
			dailyConsumption = lifeConsumption / averageRemainingLifeDays;
		} else {
			// household is deconstructed -> spend everything
			dailyConsumption = averageIncomePerPeriod + currentAssets;
		}

		// TODO: check, whether dailyConsumption > 0 when not retired and income
		// = 0 is valid according to Modigliani

		assert (!Double.isNaN(dailyConsumption));

		final Map<Period, Double> optimalConsumptionPlan = new HashMap<Period, Double>();

		for (final Period period : Period.values()) {
			optimalConsumptionPlan.put(period, dailyConsumption);
		}

		return optimalConsumptionPlan;
	}
}
