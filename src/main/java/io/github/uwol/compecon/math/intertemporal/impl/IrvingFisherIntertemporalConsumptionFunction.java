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

package io.github.uwol.compecon.math.intertemporal.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.github.uwol.compecon.engine.service.impl.FixedPriceFunctionImpl;
import io.github.uwol.compecon.math.impl.ConvexFunctionImpl;
import io.github.uwol.compecon.math.intertemporal.IntertemporalConsumptionFunction;
import io.github.uwol.compecon.math.price.PriceFunction;

public abstract class IrvingFisherIntertemporalConsumptionFunction implements IntertemporalConsumptionFunction {

	public enum Period {
		CURRENT, NEXT;
	}

	protected final ConvexFunctionImpl<Period> delegate;

	protected IrvingFisherIntertemporalConsumptionFunction(final ConvexFunctionImpl<Period> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Map<Period, Double> calculateUtilityMaximizingConsumptionPlan(final double averageIncomePerPeriod,
			final double currentAssets, final double keyInterestRate, final int ageInDays, final int lifeSpanInDays,
			final int retirementAgeInDays) {

		// price levels
		final Map<Period, PriceFunction> priceLevelsOfPeriods = new HashMap<Period, PriceFunction>();

		for (final Period period : Period.values()) {
			// price levels in periods
			priceLevelsOfPeriods.put(period, new FixedPriceFunctionImpl(1.0));
		}

		// income cash flow discounted to current period -> intertemporal budget
		double discountedBudget = 0.0;

		for (final Period period : Period.values()) {
			// budget, discounted to current period
			final double periodIncome = averageIncomePerPeriod;
			final int periodNumber = period.ordinal();
			discountedBudget += periodIncome / Math.pow(1.0 + keyInterestRate, periodNumber);
		}

		// resulting consumption plan
		final Map<Period, Double> intermediateResult = delegate.calculateOutputMaximizingInputs(priceLevelsOfPeriods,
				discountedBudget);

		for (final Entry<Period, Double> entry : intermediateResult.entrySet()) {
			// add interest to consumption plan
			final double periodConsumption = entry.getValue();
			final int periodNumber = entry.getKey().ordinal();
			final double discountedValue = periodConsumption * Math.pow(1.0 + keyInterestRate, periodNumber);
			intermediateResult.put(entry.getKey(), discountedValue);
		}

		return intermediateResult;
	}
}