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

import java.util.HashMap;
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

	public T findHighestPartialDerivatePerPrice(Map<T, Double> bundleOfInputs,
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
				if (Double.isNaN(partialDerivativePerPrice))
					throw new RuntimeException();
				if (partialDerivativePerPrice > highestPartialDerivatePerPrice) {
					optimalInput = inputType;
					highestPartialDerivatePerPrice = partialDerivativePerPrice;
				}
			}
		}
		return optimalInput;
	}

	public Map<T, Double> partialDerivatives(Map<T, Double> forBundleOfInputs) {
		Map<T, Double> partialDerivatives = new HashMap<T, Double>();
		for (T inputType : this.getInputTypes())
			partialDerivatives.put(inputType,
					this.partialDerivative(forBundleOfInputs, inputType));
		return partialDerivatives;
	}

	public Map<T, Double> calculateOutputMaximizingInputsUnderBudgetRestriction(
			final Map<T, Double> costsOfInputs, final double budget) {
		return this
				.calculateOutputMaximizingInputsUnderBudgetRestrictionByRangeScan(
						costsOfInputs, budget);
	}

	/**
	 * finds the optimal bundle of inputs under the budget restriction by a
	 * discrete brute force search on the domain of the function -> slow
	 */
	public Map<T, Double> calculateOutputMaximizingInputsUnderBudgetRestrictionByRangeScan(
			final Map<T, Double> costsOfInputs, final double budget) {
		Map<T, Double> optimalBundleOfInputs = this
				.calculateOutputMaximizingInputsUnderBudgetRestrictionByRangeScan(
						costsOfInputs, budget, 0.0, new HashMap<T, Double>());
		if (optimalBundleOfInputs.isEmpty()) {
			for (T inputType : this.getInputTypes()) {
				optimalBundleOfInputs.put(inputType, 0.0);
			}
		}
		return optimalBundleOfInputs;
	}

	/**
	 * @return the optimal bundleOfInputs under the budget restriction; empty,
	 *         if there is no bundleOfInputs that returns an output exceeding
	 *         minOutput
	 */
	private Map<T, Double> calculateOutputMaximizingInputsUnderBudgetRestrictionByRangeScan(
			final Map<T, Double> costsOfInputs, final double budgetLeft,
			final double minOutput, final Map<T, Double> bundleOfInputs) {
		T currentInputType = identifyNextUnsetInputType(bundleOfInputs);
		// if a bundle of inputs has been chosen
		if (currentInputType == null) {
			if (this.f(bundleOfInputs) > minOutput)
				return bundleOfInputs;
			else
				return new HashMap<T, Double>();
		}
		// if at least one input good type has not been chosen
		else {
			// the amount of this input type is limited by the remaining budget
			double maxInputOfCurrentInputType;
			if (Double.isNaN(costsOfInputs.get(currentInputType))) {
				maxInputOfCurrentInputType = 0.0;
			} else {
				maxInputOfCurrentInputType = budgetLeft
						/ costsOfInputs.get(currentInputType);
			}

			double bestOutput = minOutput;
			Map<T, Double> bestBundleOfInputs = new HashMap<T, Double>();

			for (double i = 0.0; i <= maxInputOfCurrentInputType; i += 0.01) {
				bundleOfInputs.put(currentInputType, i);
				double costs = Double
						.isNaN(costsOfInputs.get(currentInputType)) ? 0.0
						: costsOfInputs.get(currentInputType) * i;
				Map<T, Double> betterBundleOfInputs = calculateOutputMaximizingInputsUnderBudgetRestrictionByRangeScan(
						costsOfInputs, budgetLeft - costs, bestOutput,
						bundleOfInputs);
				// if a better bundle of inputs has been found
				if (!betterBundleOfInputs.isEmpty()) {
					double betterOutput = this.f(betterBundleOfInputs);
					bestOutput = betterOutput;
					// clone the map, as the original map will be modified
					// in next iteration
					bestBundleOfInputs = new HashMap<T, Double>(
							betterBundleOfInputs);
				}
			}
			// remove this input, so that it will be set in next outer iteration
			bundleOfInputs.remove(currentInputType);
			return bestBundleOfInputs;
		}
	}

	private T identifyNextUnsetInputType(Map<T, Double> bundleOfInputs) {
		for (T inputType : this.getInputTypes()) {
			if (!bundleOfInputs.containsKey(inputType))
				return inputType;
		}
		return null;
	}
}
