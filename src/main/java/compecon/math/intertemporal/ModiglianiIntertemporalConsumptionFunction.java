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

package compecon.math.intertemporal;

import java.util.HashMap;
import java.util.Map;

import compecon.math.intertemporal.IrvingFisherIntertemporalConsumptionFunction.Period;

public class ModiglianiIntertemporalConsumptionFunction implements
		IntertemporalConsumptionFunction {

	@Override
	public Map<Period, Double> calculateUtilityMaximizingConsumptionPlan(
			double averageIncomePerPeriod, double currentAssets,
			double keyInterestRate, int ageInDays, int retirementAgeInDays,
			int averageRemainingLifeDays) {
		int remainingDaysUntilRetirement = Math.max(retirementAgeInDays
				- ageInDays, 0);

		double consumption;
		if (averageRemainingLifeDays > 0) {
			consumption = (currentAssets + remainingDaysUntilRetirement
					* averageIncomePerPeriod)
					/ (float) averageRemainingLifeDays;
		} else {
			consumption = averageIncomePerPeriod + currentAssets;
		}

		assert (!Double.isNaN(consumption));

		Map<Period, Double> optimalConsumptionPlan = new HashMap<Period, Double>();
		for (Period period : Period.values()) {
			optimalConsumptionPlan.put(period, consumption);
		}
		return optimalConsumptionPlan;
	}
}
