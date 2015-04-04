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

package compecon.math.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import compecon.math.price.PriceFunction;
import compecon.math.price.PriceFunction.PriceFunctionConfig;

public abstract class AnalyticalConvexFunctionImpl<T> extends
		ConvexFunctionImpl<T> {

	protected AnalyticalConvexFunctionImpl(
			final boolean needsAllInputFactorsNonZeroForPartialDerivate) {
		super(needsAllInputFactorsNonZeroForPartialDerivate);
	}

	// commented out for performance reasons: TODO find optimal number and
	// market depth of price functions for
	// calculateOutputMaximizingInputsAnalyticalWithPriceFunctions to be
	// efficient
	//
	// @Override
	// public Map<T, Double> calculateOutputMaximizingInputs(
	// final Map<T, IPriceFunction> priceFunctionsOfInputs,
	// final double budget) {
	// Map<T, Double> validPriceFunctionConfigConstellation = this
	// .calculateOutputMaximizingInputsAnalyticalWithPriceFunctions(
	// priceFunctionsOfInputs, budget);
	// if (validPriceFunctionConfigConstellation != null) {
	// return validPriceFunctionConfigConstellation;
	// } else {
	// // if no analytical solution can be found -> iterative algorithm
	// return super.calculateOutputMaximizingInputs(
	// priceFunctionsOfInputs, budget);
	// }
	// }

	/**
	 * finds the optimal bundle of inputs under budget constraints and a step
	 * price function. premise for calculation of a solution is that the
	 * marginal output per price is equal for all input types; this requirement
	 * is not fulfilled in cases, when the solution lies near to points of
	 * discontinuity of the price step function, as an immediate price change
	 * causes gaps between marginal output per marginal price. In these cases
	 * null is returned.
	 */
	public Map<T, Double> calculateOutputMaximizingInputsAnalyticalWithPriceFunctions(
			final Map<T, PriceFunction> priceFunctionsOfInputTypes,
			final double budget) {
		/*
		 * retrieve parameters/configs of price step functions
		 */
		final Map<T, PriceFunctionConfig[]> priceConfigsOfInputTypes = new HashMap<T, PriceFunctionConfig[]>();

		for (final Entry<T, PriceFunction> priceConfigEntry : priceFunctionsOfInputTypes
				.entrySet()) {
			priceConfigsOfInputTypes.put(priceConfigEntry.getKey(),
					priceConfigEntry.getValue()
							.getAnalyticalPriceFunctionParameters(budget));
		}

		// select valid constellation of price function configs
		final Map<T, PriceFunctionConfig> validPriceFunctionConfigConstellation = this
				.searchValidPriceFunctionConfigConstellation(
						priceConfigsOfInputTypes, budget,
						new HashMap<T, PriceFunctionConfig>());
		if (validPriceFunctionConfigConstellation == null) {
			return null;
		}

		return this
				.calculatePossiblyValidOutputMaximizingInputsAnalyticalWithMarketPrices(
						validPriceFunctionConfigConstellation, budget);
	}

	protected abstract Map<T, Double> calculatePossiblyValidOutputMaximizingInputsAnalyticalWithMarketPrices(
			Map<T, PriceFunctionConfig> priceFunctionConfigs, double budget);

	private T identifyNextUnsetInputType(
			final Map<T, PriceFunctionConfig> priceFunctionConfigs) {
		for (final T inputType : getInputTypes()) {
			if (!priceFunctionConfigs.containsKey(inputType)) {
				return inputType;
			}
		}

		return null;
	}

	/**
	 * @see #calculateOutputMaximizingInputsAnalyticalWithPriceFunctions(Map,
	 *      double)
	 * @return null if no valid constellation is found
	 */
	protected Map<T, PriceFunctionConfig> searchValidPriceFunctionConfigConstellation(
			final Map<T, PriceFunctionConfig[]> priceConfigsOfInputs,
			final double budget,
			final Map<T, PriceFunctionConfig> currentPriceFunctionConfigConstellation) {
		final T currentInputType = identifyNextUnsetInputType(currentPriceFunctionConfigConstellation);

		// if a constellation of price function configs has been chosen
		if (currentInputType == null) {
			final Map<T, Double> optimalBundleOfInputs = this
					.calculatePossiblyValidOutputMaximizingInputsAnalyticalWithMarketPrices(
							currentPriceFunctionConfigConstellation, budget);
			for (final Entry<T, PriceFunctionConfig> priceFunctionConfig : currentPriceFunctionConfigConstellation
					.entrySet()) {
				final double optimalAmountOfInputType = optimalBundleOfInputs
						.get(priceFunctionConfig.getKey());
				final double intervalLeftBoundary = priceFunctionConfig
						.getValue().intervalLeftBoundary;
				final double intervalRightBoundary = priceFunctionConfig
						.getValue().intervalRightBoundary;

				// if the optimalBundleOfInputs is not valid under the
				// restrictions of the price function
				if (intervalLeftBoundary > optimalAmountOfInputType
						|| (!Double.isInfinite(intervalRightBoundary) && intervalRightBoundary < optimalAmountOfInputType)) {
					return null;
				}
			}
			return currentPriceFunctionConfigConstellation;
		}
		// if at least one price function config has not been chosen
		else {
			// iterate over steps of price step function/configs of this input
			// type
			Map<T, PriceFunctionConfig> validPriceFunctionConfigConstellation = null;

			for (final PriceFunctionConfig currentPriceFunctionConfig : priceConfigsOfInputs
					.get(currentInputType)) {
				currentPriceFunctionConfigConstellation.put(currentInputType,
						currentPriceFunctionConfig);
				final Map<T, PriceFunctionConfig> priceFunctionConfigConstellation = searchValidPriceFunctionConfigConstellation(
						priceConfigsOfInputs, budget,
						currentPriceFunctionConfigConstellation);

				// if the constellation of price function configs is valid
				if (priceFunctionConfigConstellation != null) {
					// clone the map, as the original map will be modified
					// in next iteration
					validPriceFunctionConfigConstellation = new HashMap<T, PriceFunctionConfig>(
							priceFunctionConfigConstellation);
					// there should only be one valid constellation -> break
					break;
				}
			}

			// remove this input, so that it will be set in next outer iteration
			currentPriceFunctionConfigConstellation.remove(currentInputType);
			return validPriceFunctionConfigConstellation;
		}
	}
}
