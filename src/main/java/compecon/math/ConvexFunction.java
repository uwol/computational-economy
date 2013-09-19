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

import compecon.engine.util.ConfigurationUtil;
import compecon.engine.util.MathUtil;
import compecon.math.price.IPriceFunction;

public abstract class ConvexFunction<T> extends Function<T> {

	protected ConvexFunction(
			boolean needsAllInputFactorsNonZeroForPartialDerivate) {
		super(needsAllInputFactorsNonZeroForPartialDerivate);
	}

	@Override
	public Map<T, Double> calculateOutputMaximizingInputs(
			final Map<T, IPriceFunction> priceFunctionsOfInputGoods,
			final double budget) {
		return this.calculateOutputMaximizingInputsIterative(
				priceFunctionsOfInputGoods, budget,
				ConfigurationUtil.MathConfig.getNumberOfIterations());
	}

	/**
	 * calculates the output maximizing bundle of inputs under a budget
	 * restriction and given static markets prices of inputs.
	 */
	public Map<T, Double> calculateOutputMaximizingInputsIterative(
			final Map<T, IPriceFunction> priceFunctionsOfInputTypes,
			final double budget, final int numberOfIterations) {

		// check if inputs have NaN prices
		boolean pricesAreNaN = false;
		for (T inputType : this.getInputTypes()) {
			if (Double.isNaN(priceFunctionsOfInputTypes.get(inputType)
					.getPrice(0.0))) {
				pricesAreNaN = true;
				break;
			}
		}

		/*
		 * special cases
		 */

		// special case: if some prices are NaN, then not all inputs can be set.
		// This becomes a problem, if all inputs have to be set -> return zero
		// input
		if (pricesAreNaN && this.needsAllInputFactorsNonZeroForPartialDerivate) {
			final Map<T, Double> bundleOfInputs = new LinkedHashMap<T, Double>();
			for (T inputType : this.getInputTypes())
				bundleOfInputs.put(inputType, 0.0);
			return bundleOfInputs;
		}

		// special case: check for budget
		if (MathUtil.equal(budget, 0.0)) {
			final Map<T, Double> bundleOfInputs = new LinkedHashMap<T, Double>();
			for (T inputType : this.getInputTypes())
				bundleOfInputs.put(inputType, 0.0);
			return bundleOfInputs;
		}

		/*
		 * regular calculation
		 */
		double moneySpent = 0.0;
		final Map<T, Double> bundleOfInputs = new LinkedHashMap<T, Double>();

		// initialize
		if (this.needsAllInputFactorsNonZeroForPartialDerivate) {
			for (T inputType : this.getInputTypes()) {
				bundleOfInputs.put(inputType, 0.0000001);
				moneySpent += bundleOfInputs.get(inputType)
						* priceFunctionsOfInputTypes.get(inputType).getPrice(
								bundleOfInputs.get(inputType));
			}
		} else {
			for (T inputType : this.getInputTypes())
				bundleOfInputs.put(inputType, 0.0);
		}

		// maximize output
		double NUMBER_OF_ITERATIONS = bundleOfInputs.size()
				* numberOfIterations;

		while (MathUtil.greater(budget, moneySpent)) {
			T optimalInput = this.findHighestPartialDerivatePerPrice(
					bundleOfInputs, priceFunctionsOfInputTypes);
			if (optimalInput != null) {
				double priceOfOptimalInputType = priceFunctionsOfInputTypes
						.get(optimalInput).getMarginalPrice(
								bundleOfInputs.get(optimalInput));
				double amount = (budget / NUMBER_OF_ITERATIONS)
						/ priceOfOptimalInputType;
				bundleOfInputs.put(optimalInput,
						bundleOfInputs.get(optimalInput) + amount);
				moneySpent += priceOfOptimalInputType * amount;
			} else
				break;
		}

		return bundleOfInputs;
	}
}
