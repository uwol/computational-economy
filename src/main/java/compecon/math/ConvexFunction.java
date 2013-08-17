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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import compecon.engine.util.MathUtil;

public abstract class ConvexFunction<T> extends Function<T> {

	protected ConvexFunction(
			boolean needsAllInputFactorsNonZeroForPartialDerivate) {
		super(needsAllInputFactorsNonZeroForPartialDerivate);
	}

	/**
	 * iterative implementation for calculating an optimal consumption plan
	 */
	public Map<T, Double> calculateOutputMaximizingInputsUnderBudgetRestriction(
			Map<T, Double> costsOfInputs, double budgetRestriction) {

		// order of exponents is preserved, so that important InputTypes
		// will be chosen first
		Map<T, Double> bundleOfInputs = new LinkedHashMap<T, Double>();

		// initialize
		for (T inputType : this.getInputTypes())
			bundleOfInputs.put(inputType, 0.0);

		// NaN costs / prices should be initialized to a large number, so that
		// they do
		// not disturb Cobb-Douglas functions
		for (T inputType : this.getInputTypes()) {
			if (Double.isNaN(costsOfInputs.get(inputType)))
				costsOfInputs.put(inputType, 9999999999999.);
		}

		// check for budget
		if (MathUtil.equal(budgetRestriction, 0))
			return bundleOfInputs;

		// maximize output
		double NUMBER_OF_ITERATIONS = bundleOfInputs.size() * 20.0;

		double moneySpent = 0.0;
		while (MathUtil.greater(budgetRestriction, moneySpent)) {
			T optimalInput = this.findLargestPartialDerivatePerPrice(
					bundleOfInputs, costsOfInputs);
			if (optimalInput != null) {
				double costOfInputType = costsOfInputs.get(optimalInput);
				if (!Double.isNaN(costOfInputType)) {
					double amount = (budgetRestriction / NUMBER_OF_ITERATIONS)
							/ costOfInputType;
					bundleOfInputs.put(optimalInput,
							bundleOfInputs.get(optimalInput) + amount);
					moneySpent += costOfInputType * amount;
				} else
					break;
			} else
				break;
		}

		// NaN prices result in minimal deviations from 0.0 -> reset to 0.0
		for (Entry<T, Double> entry : bundleOfInputs.entrySet()) {
			if (MathUtil.equal(entry.getValue(), 0.0))
				entry.setValue(0.0);
		}

		return bundleOfInputs;
	}
}
