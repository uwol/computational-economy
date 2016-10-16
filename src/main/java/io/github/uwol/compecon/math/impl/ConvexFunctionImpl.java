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
import java.util.LinkedHashMap;
import java.util.Map;

import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.log.Log;
import io.github.uwol.compecon.math.ConvexFunction;
import io.github.uwol.compecon.math.price.PriceFunction;
import io.github.uwol.compecon.math.util.MathUtil;

public abstract class ConvexFunctionImpl<T> extends FunctionImpl<T> implements
		ConvexFunction<T> {

	protected ConvexFunctionImpl(
			final boolean needsAllInputFactorsNonZeroForPartialDerivate) {
		super(needsAllInputFactorsNonZeroForPartialDerivate);
	}

	@Override
	public Map<T, Double> calculateOutputMaximizingInputs(
			final Map<T, PriceFunction> priceFunctionsOfInputGoods,
			final double budget) {
		return this.calculateOutputMaximizingInputsIterative(
				priceFunctionsOfInputGoods, budget, ApplicationContext
						.getInstance().getConfiguration().mathConfig
						.getNumberOfIterations(), ApplicationContext
						.getInstance().getConfiguration().mathConfig
						.getInitializationValue());
	}

	/**
	 * calculates the output maximizing bundle of inputs under a budget
	 * restriction and given fixed markets prices of inputs.
	 *
	 * @param budget
	 *            determines the granularity of the output, as the budget is
	 *            divided by the numberOfIterations -> large budget leads to
	 *            large chunks
	 */
	public Map<T, Double> calculateOutputMaximizingInputsIterative(
			final Map<T, PriceFunction> priceFunctionsOfInputTypes,
			final double budget, final int numberOfIterations) {
		return this.calculateOutputMaximizingInputsIterative(
				priceFunctionsOfInputTypes, budget, numberOfIterations,
				ApplicationContext.getInstance().getConfiguration().mathConfig
						.getInitializationValue());
	}

	protected Map<T, Double> calculateOutputMaximizingInputsIterative(
			final Map<T, PriceFunction> priceFunctionsOfInputTypes,
			final double budget, final int numberOfIterations,
			final double initializationValue) {

		assert (numberOfIterations > 0);

		// ------ preparation -----------------------------------------

		// initialize inventory
		final Map<T, Double> inventoryNullSafe = new HashMap<T, Double>();

		for (final T inputType : getInputTypes()) {
			inventoryNullSafe.put(inputType, 0.0);
		}

		// check, whether inputs have NaN prices
		boolean inputsAreUnavailable = false;

		for (final T inputType : getInputTypes()) {
			final double inventoryAmount = inventoryNullSafe.get(inputType);
			// if the input type is not available in the inventory
			if (inventoryAmount <= 0.0) {
				// if the good type is not available on markets
				if (Double.isNaN(priceFunctionsOfInputTypes.get(inputType)
						.getPrice(0.0))) {
					inputsAreUnavailable = true;
					break;
				}
			}
		}

		/*
		 * special cases
		 */

		// special case: if some prices are NaN, then not all inputs can be set.
		// This becomes a problem, if all inputs have to be set -> return zero
		// input
		if (inputsAreUnavailable
				&& needsAllInputFactorsNonZeroForPartialDerivate) {
			getLog().log(
					"at least one of the prices is Double.NaN, but the function needs all inputs set -> no calculation");
			getLog().agent_onCalculateOutputMaximizingInputsIterative(budget,
					0.0,
					ConvexFunctionTerminationCause.INPUT_FACTOR_UNAVAILABLE);

			final Map<T, Double> bundleOfInputs = new LinkedHashMap<T, Double>();

			for (final T inputType : getInputTypes()) {
				bundleOfInputs.put(inputType, 0.0);
			}

			return bundleOfInputs;
		}

		// special case: check for budget
		if (MathUtil.lesserEqual(budget, 0.0)) {
			getLog().log("budget is %s -> no calculation", budget);
			getLog().agent_onCalculateOutputMaximizingInputsIterative(budget,
					0.0, ConvexFunctionTerminationCause.BUDGET_PLANNED);

			final Map<T, Double> bundleOfInputs = new LinkedHashMap<T, Double>();

			for (final T inputType : getInputTypes()) {
				bundleOfInputs.put(inputType, 0.0);
			}

			return bundleOfInputs;
		}

		/*
		 * initialization
		 */
		final Map<T, Double> bundleOfInputs = new HashMap<T, Double>(
				inventoryNullSafe);

		// determine initialization value
		final double initializationValueForInputs;

		if (needsAllInputFactorsNonZeroForPartialDerivate) {
			initializationValueForInputs = initializationValue;
		} else {
			initializationValueForInputs = 0.0;
		}

		// set initialization value
		for (final T inputType : getInputTypes()) {
			bundleOfInputs.put(inputType,
					MathUtil.nullSafeValue(bundleOfInputs.get(inputType))
							+ initializationValueForInputs);
		}

		// ------ calculation -----------------------------------------

		/*
		 * iterative calculation of maximizing inputs
		 */
		double budgetSpent = 0.0;

		// maximize output
		final int NUMBER_OF_ITERATIONS = bundleOfInputs.size()
				* numberOfIterations;
		final double budgetPerIteration = budget / NUMBER_OF_ITERATIONS;

		while (true) {
			// would this iteration lead to overspending of the budget?
			if (MathUtil.greater(budgetSpent + budgetPerIteration, budget)) {
				getLog().log("budget planned completely");
				getLog().agent_onCalculateOutputMaximizingInputsIterative(
						budget, budgetSpent,
						ConvexFunctionTerminationCause.BUDGET_PLANNED);
				break;
			}

			final T optimalInputType = findHighestPartialDerivatePerPrice(
					bundleOfInputs, priceFunctionsOfInputTypes,
					inventoryNullSafe);

			// no optimal input type could be found, i. e. markets are sold out
			if (optimalInputType == null) {
				getLog().log("no optimal input found -> terminating");
				getLog().agent_onCalculateOutputMaximizingInputsIterative(
						budget, budgetSpent,
						ConvexFunctionTerminationCause.NO_INPUT_AVAILABLE);
				break;
			} else {
				final double oldAmountOfOptimalInputType = bundleOfInputs
						.get(optimalInputType);
				final double priceRelevantAmountOfOptimalInputType = Math.max(
						bundleOfInputs.get(optimalInputType)
								- inventoryNullSafe.get(optimalInputType), 0.0);
				final double marginalPriceOfOptimalInputType = priceFunctionsOfInputTypes
						.get(optimalInputType).getMarginalPrice(
								priceRelevantAmountOfOptimalInputType);

				// additional amounts have to grow slowly, so that the solution
				// space is not left
				final double additionalAmountOfInputType = Math.min(
						budgetPerIteration / marginalPriceOfOptimalInputType,
						Math.max(priceRelevantAmountOfOptimalInputType,
								initializationValue));
				bundleOfInputs.put(optimalInputType,
						oldAmountOfOptimalInputType
								+ additionalAmountOfInputType);

				// constraints

				budgetSpent += marginalPriceOfOptimalInputType
						* additionalAmountOfInputType;
			}
		}

		// ------ cleanup -----------------------------------------

		// reset initialization values
		for (final T inputType : getInputTypes()) {
			bundleOfInputs.put(inputType, bundleOfInputs.get(inputType)
					- initializationValueForInputs);
		}

		return bundleOfInputs;
	}

	private Log getLog() {
		return ApplicationContext.getInstance().getLog();
	}
}
