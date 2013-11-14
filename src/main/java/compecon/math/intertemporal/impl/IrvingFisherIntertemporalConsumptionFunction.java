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
import java.util.Map.Entry;

import compecon.math.impl.ConvexFunctionImpl;
import compecon.math.intertemporal.IntertemporalConsumptionFunction;
import compecon.math.price.PriceFunction;
import compecon.math.price.impl.FixedPriceFunction;

public abstract class IrvingFisherIntertemporalConsumptionFunction implements
		IntertemporalConsumptionFunction {

	public enum Period {
		CURRENT, NEXT;
	}

	protected final ConvexFunctionImpl<Period> delegate;

	protected IrvingFisherIntertemporalConsumptionFunction(
			ConvexFunctionImpl<Period> delegate) {
		this.delegate = delegate;
	}

	public Map<Period, Double> calculateUtilityMaximizingConsumptionPlan(
			double averageIncomePerPeriod, double currentAssets,
			double keyInterestRate, int ageInDays, int retirementAgeInDays,
			int averageRemainingLifeDays) {

		// price levels
		Map<Period, PriceFunction> priceLevelsOfPeriods = new HashMap<Period, PriceFunction>();
		for (Period period : Period.values()) {
			// price levels in periods
			priceLevelsOfPeriods.put(period, new FixedPriceFunction(1.0));
		}

		// income cash flow discounted to current period -> intertemporal budget
		double discountedBudget = 0.0;
		for (Period period : Period.values()) {
			// budget, discounted to current period
			double periodIncome = averageIncomePerPeriod;
			int periodNumber = period.ordinal();
			discountedBudget += periodIncome
					/ Math.pow(1.0 + keyInterestRate, periodNumber);
		}

		// resulting consumption plan
		Map<Period, Double> intermediateResult = delegate
				.calculateOutputMaximizingInputs(priceLevelsOfPeriods,
						discountedBudget);
		for (Entry<Period, Double> entry : intermediateResult.entrySet()) {
			// add interest to consumption plan
			double periodConsumption = entry.getValue();
			int periodNumber = entry.getKey().ordinal();
			double discountedValue = periodConsumption
					* Math.pow(1.0 + keyInterestRate, periodNumber);
			intermediateResult.put(entry.getKey(), discountedValue);
		}

		return intermediateResult;
	}
}