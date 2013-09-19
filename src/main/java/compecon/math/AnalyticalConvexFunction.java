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
import java.util.Map.Entry;

import compecon.math.price.IPriceFunction;
import compecon.math.price.IPriceFunction.PriceFunctionConfig;

public abstract class AnalyticalConvexFunction<T> extends ConvexFunction<T> {

	protected AnalyticalConvexFunction(
			boolean needsAllInputFactorsNonZeroForPartialDerivate) {
		super(needsAllInputFactorsNonZeroForPartialDerivate);
	}

	/**
	 * finds the optimal bundle of inputs under budget constraints and a step
	 * price function.
	 */
	public Map<T, Double> calculateOutputMaximizingInputsAnalyticalWithPriceFunctions(
			final Map<T, IPriceFunction> priceFunctionsOfInputTypes,
			final double budget) {
		/*
		 * retrieve parameters/configs of price step functions
		 */
		final Map<T, PriceFunctionConfig[]> priceConfigsOfInputTypes = new HashMap<T, PriceFunctionConfig[]>();
		for (Entry<T, IPriceFunction> priceConfigEntry : priceFunctionsOfInputTypes
				.entrySet()) {
			priceConfigsOfInputTypes.put(priceConfigEntry.getKey(),
					priceConfigEntry.getValue()
							.getAnalyticalPriceFunctionParameters(budget));
		}

		// select valid constellation of price function configs
		Map<T, PriceFunctionConfig> validPriceFunctionConfigConstellation = this
				.searchValidPriceFunctionConfigConstellation(
						priceConfigsOfInputTypes, budget,
						new HashMap<T, PriceFunctionConfig>());
		return this
				.calculatePossiblyValidOutputMaximizingInputsAnalyticalWithMarketPrices(
						validPriceFunctionConfigConstellation, budget);
	}

	public Map<T, PriceFunctionConfig> searchValidPriceFunctionConfigConstellation(
			final Map<T, PriceFunctionConfig[]> priceConfigsOfInputs,
			final double budget,
			final Map<T, PriceFunctionConfig> currentPriceFunctionConfigConstellation) {
		T currentInputType = identifyNextUnsetInputType(currentPriceFunctionConfigConstellation);
		// if a constellation of price function configs has been chosen
		if (currentInputType == null) {
			final Map<T, Double> optimalBundleOfInputs = this
					.calculatePossiblyValidOutputMaximizingInputsAnalyticalWithMarketPrices(
							currentPriceFunctionConfigConstellation, budget);
			for (Entry<T, PriceFunctionConfig> priceFunctionConfig : currentPriceFunctionConfigConstellation
					.entrySet()) {
				double optimalAmountOfInputType = optimalBundleOfInputs
						.get(priceFunctionConfig.getKey());
				double intervalLeftBoundary = priceFunctionConfig.getValue().intervalLeftBoundary;
				double intervalRightBoundary = priceFunctionConfig.getValue().intervalRightBoundary;

				// if the optimalBundleOfInputs is not valid under the
				// restrictions of the step price function
				if (intervalLeftBoundary > optimalAmountOfInputType)
					return new HashMap<T, PriceFunctionConfig>();

				PriceFunctionConfig[] priceFunctionConfigs = priceConfigsOfInputs
						.get(priceFunctionConfig.getKey());
				PriceFunctionConfig lastPriceFunctionConfig = priceFunctionConfigs[priceFunctionConfigs.length - 1];
				if (intervalRightBoundary < optimalAmountOfInputType) {
					if (priceFunctionConfig != lastPriceFunctionConfig)
						return new HashMap<T, PriceFunctionConfig>();
				}
			}
			return currentPriceFunctionConfigConstellation;
		}
		// if at least one price function config has not been chosen
		else {
			// iterate over steps of price step function/configs of this input
			// type
			Map<T, PriceFunctionConfig> validPriceFunctionConfigConstellation = new HashMap<T, PriceFunctionConfig>();
			for (PriceFunctionConfig currentPriceFunctionConfig : priceConfigsOfInputs
					.get(currentInputType)) {
				currentPriceFunctionConfigConstellation.put(currentInputType,
						currentPriceFunctionConfig);
				Map<T, PriceFunctionConfig> priceFunctionConfigConstellation = searchValidPriceFunctionConfigConstellation(
						priceConfigsOfInputs, budget,
						currentPriceFunctionConfigConstellation);
				// if the constellation of price function configs is valid
				if (!priceFunctionConfigConstellation.isEmpty()) {
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

	protected abstract Map<T, Double> calculatePossiblyValidOutputMaximizingInputsAnalyticalWithMarketPrices(
			Map<T, PriceFunctionConfig> priceFunctionConfigs, double budget);

	private T identifyNextUnsetInputType(
			Map<T, PriceFunctionConfig> priceFunctionConfigs) {
		for (T inputType : this.getInputTypes()) {
			if (!priceFunctionConfigs.containsKey(inputType))
				return inputType;
		}
		return null;
	}
}
