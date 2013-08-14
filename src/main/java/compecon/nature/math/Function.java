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

package compecon.nature.math;

import java.util.Map;

import compecon.engine.util.MathUtil;

public abstract class Function<T> implements IFunction<T> {

	protected final boolean needsAllInputFactorsNonZeroForPartialDerivate;

	public boolean getNeedsAllInputFactorsNonZeroForPartialDerivate() {
		return this.needsAllInputFactorsNonZeroForPartialDerivate;
	}

	public Function(boolean needsAllInputFactorsNonZeroForPartialDerivate) {
		this.needsAllInputFactorsNonZeroForPartialDerivate = needsAllInputFactorsNonZeroForPartialDerivate;
	}

	public T findLargestPartialDerivate(Map<T, Double> bundleOfInputs) {
		@SuppressWarnings("unchecked")
		T optimalInputType = (T) this.getInputTypes().toArray()[0];
		double optimalPartialDerivate = 0;

		for (T inputType : this.getInputTypes()) {
			double partialDerivate = this.partialDerivative(bundleOfInputs,
					inputType);
			if (optimalInputType == null
					|| MathUtil
							.greater(partialDerivate, optimalPartialDerivate)) {
				optimalInputType = inputType;
				optimalPartialDerivate = partialDerivate;
			}
		}
		return optimalInputType;
	}

	public T findLargestPartialDerivatePerPrice(Map<T, Double> bundleOfInputs,
			Map<T, Double> pricesOfInputs) {
		@SuppressWarnings("unchecked")
		T optimalInput = (T) this.getInputTypes().toArray()[0];
		double highestPartialDerivatePerPrice = 0.0;

		for (T inputType : this.getInputTypes()) {
			double partialDerivative = this.partialDerivative(bundleOfInputs,
					inputType);
			double pricePerUnit = pricesOfInputs.get(inputType);
			if (!Double.isNaN(pricePerUnit)) {
				double partialDerivativePerPrice = partialDerivative
						/ pricePerUnit;
				/*
				 * the check for bundleOfInputs.get(inputType) == 0 is a
				 * heuristic for certain functions (e.g. Cobb-Douglas): if for
				 * all alternatives the partial derivative is 0, choose an
				 * input, that has not been chosen before -> in case of
				 * Coob-Douglas partial derivative becomes > 0, as soon as all
				 * inputs > 0
				 */
				if (!Double.isNaN(partialDerivativePerPrice)
						&& MathUtil.greater(partialDerivativePerPrice,
								highestPartialDerivatePerPrice)
						|| (MathUtil.equal(partialDerivativePerPrice,
								highestPartialDerivatePerPrice) && bundleOfInputs
								.get(inputType) == 0)) {
					optimalInput = inputType;
					highestPartialDerivatePerPrice = partialDerivativePerPrice;
				}
			}
		}
		return optimalInput;
	}
}
