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

import java.util.LinkedHashMap;
import java.util.Map;

import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.log.Log;
import compecon.engine.util.MathUtil;
import compecon.math.ConvexFunction;
import compecon.math.price.PriceFunction;

public abstract class ConvexFunctionImpl<T> extends FunctionImpl<T> implements
		ConvexFunction<T> {

	protected ConvexFunctionImpl(
			boolean needsAllInputFactorsNonZeroForPartialDerivate) {
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

		// check, whether inputs have NaN prices
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
			getLog().log(
					"at least one of the prices is Double.NaN, but the function needs all inputs set -> no calculation");
			getLog().agent_onCalculateOutputMaximizingInputsIterative(budget,
					0.0,
					ConvexFunctionTerminationCause.INPUT_FACTOR_UNAVAILABLE);

			final Map<T, Double> bundleOfInputs = new LinkedHashMap<T, Double>();
			for (T inputType : this.getInputTypes())
				bundleOfInputs.put(inputType, 0.0);
			return bundleOfInputs;
		}

		// special case: check for budget
		if (MathUtil.lesserEqual(budget, 0.0)) {
			getLog().log("budget is " + budget + " -> no calculation");
			getLog().agent_onCalculateOutputMaximizingInputsIterative(budget,
					0.0, ConvexFunctionTerminationCause.BUDGET_PLANNED);

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

		final double initializationValueForInputs;
		if (this.needsAllInputFactorsNonZeroForPartialDerivate) {
			initializationValueForInputs = initializationValue;
		} else {
			initializationValueForInputs = 0.0;
		}

		// initialize
		for (T inputType : this.getInputTypes()) {
			bundleOfInputs.put(inputType, initializationValueForInputs);
		}

		// maximize output
		final int NUMBER_OF_ITERATIONS = bundleOfInputs.size()
				* numberOfIterations;
		final double budgetPerIteration = budget
				/ (double) NUMBER_OF_ITERATIONS;

		while (true) {
			// would this iteration lead to overspending of the budget?
			if (MathUtil.greater(moneySpent + budgetPerIteration, budget)) {
				getLog().log("budget planned completely");
				getLog().agent_onCalculateOutputMaximizingInputsIterative(
						budget, moneySpent,
						ConvexFunctionTerminationCause.BUDGET_PLANNED);
				break;
			}

			T optimalInputType = this.findHighestPartialDerivatePerPrice(
					bundleOfInputs, priceFunctionsOfInputTypes);

			// no optimal input type could be found, i. e. markets are sold out
			if (optimalInputType == null) {
				getLog().log("no optimal input found -> terminating");
				getLog().agent_onCalculateOutputMaximizingInputsIterative(
						budget, moneySpent,
						ConvexFunctionTerminationCause.NO_INPUT_AVAILABLE);
				break;
			} else {
				double marginalPriceOfOptimalInputType = priceFunctionsOfInputTypes
						.get(optimalInputType).getMarginalPrice(
								bundleOfInputs.get(optimalInputType));
				// additional amounts have to grow slowly, so that the solution
				// space is not left
				double additionalAmountOfInputType = Math.min(
						budgetPerIteration / marginalPriceOfOptimalInputType,
						Math.max(bundleOfInputs.get(optimalInputType),
								initializationValue));
				bundleOfInputs.put(optimalInputType,
						bundleOfInputs.get(optimalInputType)
								+ additionalAmountOfInputType);
				moneySpent += marginalPriceOfOptimalInputType
						* additionalAmountOfInputType;
			}
		}

		// reset initialization values
		for (T inputType : this.getInputTypes()) {
			bundleOfInputs.put(inputType, bundleOfInputs.get(inputType)
					- initializationValueForInputs);

		}

		return bundleOfInputs;
	}

	private Log getLog() {
		return ApplicationContext.getInstance().getLog();
	}
}
