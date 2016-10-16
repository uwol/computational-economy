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

package io.github.uwol.compecon.math.impl;

import java.util.HashMap;
import java.util.Map;

import io.github.uwol.compecon.math.Function;
import io.github.uwol.compecon.math.price.PriceFunction;
import io.github.uwol.compecon.math.util.MathUtil;

public abstract class FunctionImpl<T> implements Function<T> {

	protected final boolean needsAllInputFactorsNonZeroForPartialDerivate;

	public FunctionImpl(
			final boolean needsAllInputFactorsNonZeroForPartialDerivate) {
		this.needsAllInputFactorsNonZeroForPartialDerivate = needsAllInputFactorsNonZeroForPartialDerivate;
	}

	@Override
	public Map<T, Double> calculateOutputMaximizingInputs(
			final Map<T, PriceFunction> priceFunctionsOfInputs,
			final double budget) {
		return this.calculateOutputMaximizingInputsByRangeScan(
				priceFunctionsOfInputs, budget);
	}

	/**
	 * finds the optimal bundle of inputs under the budget restriction by a
	 * discrete brute force search on the domain of the function -> slow
	 */
	@Override
	public Map<T, Double> calculateOutputMaximizingInputsByRangeScan(
			final Map<T, PriceFunction> priceFunctionsOfInputTypes,
			final double budget) {
		final Map<T, Double> optimalBundleOfInputs = this
				.calculateOutputMaximizingInputsByRangeScan(
						priceFunctionsOfInputTypes, budget, 0.0,
						new HashMap<T, Double>());

		if (optimalBundleOfInputs.isEmpty()) {
			for (final T inputType : getInputTypes()) {
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
			final Map<T, PriceFunction> priceFunctionsOfInputTypes,
			final double budgetLeft, final double minOutput,
			final Map<T, Double> currentBundleOfInputs) {
		final T currentInputType = identifyNextUnsetInputType(currentBundleOfInputs);

		// if a bundle of inputs has been chosen
		if (currentInputType == null) {
			if (f(currentBundleOfInputs) > minOutput) {
				return currentBundleOfInputs;
			} else {
				return new HashMap<T, Double>();
			}
		}
		// if at least one input good type has not been chosen
		else {
			// the amount of this input type is limited by the remaining budget
			final double maxInputOfCurrentInputType;
			final double initialPriceOfCurrentInputType = priceFunctionsOfInputTypes
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

				final Map<T, Double> betterBundleOfInputs = calculateOutputMaximizingInputsByRangeScan(
						priceFunctionsOfInputTypes, budgetLeft
								- priceSumOfCurrentInputType, bestOutput,
						currentBundleOfInputs);
				// if a better bundle of inputs has been found
				if (!betterBundleOfInputs.isEmpty()) {
					final double betterOutput = f(betterBundleOfInputs);
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

	@Override
	public T findHighestPartialDerivatePerPrice(
			final Map<T, Double> bundleOfInputs,
			final Map<T, PriceFunction> priceFunctionsOfInputTypes,
			final Map<T, Double> inventory) {
		T optimalInputType = null;
		double highestPartialDerivatePerPrice = 0.0;

		for (final T inputType : getInputTypes()) {
			final double partialDerivative = partialDerivative(bundleOfInputs,
					inputType);
			final double amountToBuy = Math.max(bundleOfInputs.get(inputType)
					- inventory.get(inputType), 0.0);
			final double marginalPrice = priceFunctionsOfInputTypes.get(
					inputType).getMarginalPrice(amountToBuy);
			if (!Double.isNaN(marginalPrice)) {
				final double partialDerivativePerPrice = partialDerivative
						/ marginalPrice;

				assert (!Double.isNaN(partialDerivativePerPrice));

				if (partialDerivativePerPrice > highestPartialDerivatePerPrice) {
					optimalInputType = inputType;
					highestPartialDerivatePerPrice = partialDerivativePerPrice;
				}
			}
		}

		return optimalInputType;
	}

	@Override
	public T findLargestPartialDerivate(final Map<T, Double> bundleOfInputs) {
		@SuppressWarnings("unchecked")
		T optimalInputType = (T) getInputTypes().toArray()[0];
		double optimalPartialDerivate = 0;

		for (final T inputType : getInputTypes()) {
			final double partialDerivate = partialDerivative(bundleOfInputs,
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

	@Override
	public boolean getNeedsAllInputFactorsNonZeroForPartialDerivate() {
		return this.needsAllInputFactorsNonZeroForPartialDerivate;
	}

	private T identifyNextUnsetInputType(final Map<T, Double> bundleOfInputs) {
		for (final T inputType : getInputTypes()) {
			if (!bundleOfInputs.containsKey(inputType)) {
				return inputType;
			}
		}

		return null;
	}

	@Override
	public Map<T, Double> partialDerivatives(
			final Map<T, Double> forBundleOfInputs) {
		final Map<T, Double> partialDerivatives = new HashMap<T, Double>();

		for (final T inputType : getInputTypes()) {
			partialDerivatives.put(inputType,
					partialDerivative(forBundleOfInputs, inputType));
		}

		return partialDerivatives;
	}
}
