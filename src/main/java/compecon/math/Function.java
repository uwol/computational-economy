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
import compecon.math.price.IPriceFunction;

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
			Map<T, IPriceFunction> priceFunctionsOfInputTypes) {
		T optimalInputType = null;
		double highestPartialDerivatePerPrice = 0.0;
		for (T inputType : this.getInputTypes()) {
			double partialDerivative = this.partialDerivative(bundleOfInputs,
					inputType);
			double pricePerUnit = priceFunctionsOfInputTypes.get(inputType)
					.getMarginalPrice(bundleOfInputs.get(inputType));
			if (!Double.isNaN(pricePerUnit)) {
				double partialDerivativePerPrice = partialDerivative
						/ pricePerUnit;
				if (Double.isNaN(partialDerivativePerPrice))
					throw new RuntimeException();
				if (partialDerivativePerPrice > highestPartialDerivatePerPrice) {
					optimalInputType = inputType;
					highestPartialDerivatePerPrice = partialDerivativePerPrice;
				}
			}
		}
		return optimalInputType;
	}

	public Map<T, Double> partialDerivatives(Map<T, Double> forBundleOfInputs) {
		Map<T, Double> partialDerivatives = new HashMap<T, Double>();
		for (T inputType : this.getInputTypes())
			partialDerivatives.put(inputType,
					this.partialDerivative(forBundleOfInputs, inputType));
		return partialDerivatives;
	}

	public Map<T, Double> calculateOutputMaximizingInputs(
			final Map<T, IPriceFunction> priceFunctionsOfInputs,
			final double budget) {
		return this.calculateOutputMaximizingInputsByRangeScan(
				priceFunctionsOfInputs, budget);
	}

	/**
	 * finds the optimal bundle of inputs under the budget restriction by a
	 * discrete brute force search on the domain of the function -> slow
	 */
	public Map<T, Double> calculateOutputMaximizingInputsByRangeScan(
			final Map<T, IPriceFunction> priceFunctionsOfInputTypes,
			final double budget) {
		Map<T, Double> optimalBundleOfInputs = this
				.calculateOutputMaximizingInputsByRangeScan(
						priceFunctionsOfInputTypes, budget, 0.0,
						new HashMap<T, Double>());
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
	private Map<T, Double> calculateOutputMaximizingInputsByRangeScan(
			final Map<T, IPriceFunction> priceFunctionsOfInputTypes,
			final double budgetLeft, final double minOutput,
			final Map<T, Double> currentBundleOfInputs) {
		T currentInputType = identifyNextUnsetInputType(currentBundleOfInputs);
		// if a bundle of inputs has been chosen
		if (currentInputType == null) {
			if (this.f(currentBundleOfInputs) > minOutput)
				return currentBundleOfInputs;
			else
				return new HashMap<T, Double>();
		}
		// if at least one input good type has not been chosen
		else {
			// the amount of this input type is limited by the remaining budget
			double maxInputOfCurrentInputType;
			double initialPriceOfCurrentInputType = priceFunctionsOfInputTypes
					.get(currentInputType).getPrice(0.0);

			if (Double.isNaN(initialPriceOfCurrentInputType)) {
				maxInputOfCurrentInputType = 0.0;
			} else {
				maxInputOfCurrentInputType = budgetLeft
						/ initialPriceOfCurrentInputType;
			}

			double bestOutput = minOutput;
			Map<T, Double> bestBundleOfInputs = new HashMap<T, Double>();

			for (double i = 0.0; i <= maxInputOfCurrentInputType; i += 0.01) {
				currentBundleOfInputs.put(currentInputType, i);

				final double priceSumOfCurrentInputType;
				if (i == 0.0) {
					/*
					 * if there is no input, the price sum is always 0.0; this
					 * makes sure that in the case of
					 * currentPriceOfCurrentInputType = Double.NaN this
					 * iteration is executed at least once
					 */
					priceSumOfCurrentInputType = 0.0;
				} else {
					final double currentPriceOfCurrentInputType = priceFunctionsOfInputTypes
							.get(currentInputType)
							.getPrice(
									currentBundleOfInputs.get(currentInputType));
					priceSumOfCurrentInputType = currentPriceOfCurrentInputType
							* i;
				}

				if (Double.isNaN(priceSumOfCurrentInputType)
						|| priceSumOfCurrentInputType > budgetLeft) {
					break;
				}

				Map<T, Double> betterBundleOfInputs = calculateOutputMaximizingInputsByRangeScan(
						priceFunctionsOfInputTypes, budgetLeft
								- priceSumOfCurrentInputType, bestOutput,
						currentBundleOfInputs);
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
			currentBundleOfInputs.remove(currentInputType);
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
