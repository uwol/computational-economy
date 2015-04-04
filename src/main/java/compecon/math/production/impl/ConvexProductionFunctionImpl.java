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

package compecon.math.production.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import compecon.economy.materia.GoodType;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.log.Log;
import compecon.math.Function;
import compecon.math.price.PriceFunction;
import compecon.math.production.ConvexProductionFunction;
import compecon.math.util.MathUtil;

public abstract class ConvexProductionFunctionImpl extends
		ProductionFunctionImpl implements ConvexProductionFunction {

	protected ConvexProductionFunctionImpl(final Function<GoodType> delegate) {
		super(delegate);
	}

	@Override
	public Map<GoodType, Double> calculateProfitMaximizingProductionFactors(
			final double priceOfProducedGoodType,
			final Map<GoodType, PriceFunction> priceFunctionsOfInputTypes,
			final Map<GoodType, Double> capital, final double budget,
			final double maxOutput, final double margin) {
		return this.calculateProfitMaximizingProductionFactorsIterative(
				priceOfProducedGoodType, priceFunctionsOfInputTypes, capital,
				budget, maxOutput, margin, ApplicationContext.getInstance()
						.getConfiguration().mathConfig.getNumberOfIterations());
	}

	public Map<GoodType, Double> calculateProfitMaximizingProductionFactorsIterative(
			final double priceOfProducedGoodType,
			final Map<GoodType, PriceFunction> priceFunctionsOfInputTypes,
			final Map<GoodType, Double> capital, final double budget,
			final double maxOutput, final double margin,
			final int numberOfIterations) {
		return this.calculateProfitMaximizingProductionFactorsIterative(
				priceOfProducedGoodType, priceFunctionsOfInputTypes, capital,
				budget, maxOutput, margin, numberOfIterations,
				ApplicationContext.getInstance().getConfiguration().mathConfig
						.getInitializationValue());
	}

	/**
	 * Calculates the profit maximizing production plan based on the common
	 * microeconomical marginal calculus. This function has a time complexity of
	 * O(inputGoodTypes.length), as each input type has to be evaluated in each
	 * iteration.
	 *
	 */
	protected Map<GoodType, Double> calculateProfitMaximizingProductionFactorsIterative(
			final double priceOfProducedGoodType,
			final Map<GoodType, PriceFunction> priceFunctionsOfInputTypes,
			final Map<GoodType, Double> capital, final double budget,
			final double maxOutput, final double margin,
			final int numberOfIterations, final double initializationValue) {

		assert (numberOfIterations > 0);

		if (capital != null) {
			for (final Entry<GoodType, Double> entry : capital.entrySet()) {
				assert (entry.getKey().isDurable()) : "capital good "
						+ entry.getKey() + " is not durable";
				assert (entry.getValue() != null);
			}
		}

		// ------ preparation -----------------------------------------

		// initialize capital
		final Map<GoodType, Double> capitalNullSafe = new HashMap<GoodType, Double>();
		for (final GoodType inputType : getInputGoodTypes()) {
			if (capital != null) {
				capitalNullSafe.put(inputType,
						MathUtil.nullSafeValue(capital.get(inputType)));
			} else {
				capitalNullSafe.put(inputType, 0.0);
			}
		}

		// check, whether inputs have NaN prices
		boolean inputsAreUnavailable = false;
		for (final GoodType inputType : getInputGoodTypes()) {
			final double capitalAmount = capitalNullSafe.get(inputType);
			// if the good type is not available as capital
			if (capitalAmount <= 0.0) {
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

		// special case: if some input prices are NaN, then not all inputs can
		// be set. This becomes a problem, if all inputs have to be set ->
		// return zero input
		if (inputsAreUnavailable
				&& delegate.getNeedsAllInputFactorsNonZeroForPartialDerivate()) {
			getLog().log(
					"at least one of the prices is Double.NaN, but the production function needs all inputs set -> no calculation");
			getLog().factory_onCalculateProfitMaximizingProductionFactorsIterative(
					budget,
					0.0,
					ConvexProductionFunctionTerminationCause.INPUT_FACTOR_UNAVAILABLE);

			final Map<GoodType, Double> bundleOfInputs = new LinkedHashMap<GoodType, Double>();
			for (final GoodType inputType : getInputGoodTypes()) {
				bundleOfInputs.put(inputType, 0.0);
			}
			return bundleOfInputs;
		}

		// special case: check for budget
		if (MathUtil.lesserEqual(budget, 0.0)) {
			getLog().log("budget is " + budget + " -> no calculation");
			getLog().factory_onCalculateProfitMaximizingProductionFactorsIterative(
					budget, 0.0,
					ConvexProductionFunctionTerminationCause.BUDGET_PLANNED);

			final Map<GoodType, Double> bundleOfInputs = new LinkedHashMap<GoodType, Double>();
			for (final GoodType inputType : getInputGoodTypes()) {
				bundleOfInputs.put(inputType, 0.0);
			}
			return bundleOfInputs;
		}

		// special case: check for estimated revenue per unit being 0.0
		if (MathUtil.lesserEqual(priceOfProducedGoodType, 0.0)) {
			getLog().log(
					"priceOfProducedGoodType = " + priceOfProducedGoodType
							+ " -> no production");
			getLog().factory_onCalculateProfitMaximizingProductionFactorsIterative(
					budget,
					0.0,
					ConvexProductionFunctionTerminationCause.ESTIMATED_REVENUE_PER_UNIT_ZERO);

			final Map<GoodType, Double> bundleOfInputs = new LinkedHashMap<GoodType, Double>();
			for (final GoodType inputType : getInputGoodTypes()) {
				bundleOfInputs.put(inputType, 0.0);
			}
			return bundleOfInputs;
		}

		// special case: check for estimated revenue per unit being NaN ->
		// needed for bootstrapping markets
		// problem: producing this little leads to a complete sold amount ->
		// rising prices because of pricingBehaviour; again, Double.NaN price,
		// as nothing is offered
		//
		// if (Double.isNaN(priceOfProducedGoodType)) {
		// getLog().log("priceOfProducedGoodType = " +
		// priceOfProducedGoodType
		// + " -> cautious production");
		// final Map<GoodType, Double> bundleOfInputs = new
		// LinkedHashMap<GoodType, Double>();
		// for (GoodType inputType : this.getInputGoodTypes())
		// bundleOfInputs.put(inputType, 0.001);
		// return bundleOfInputs;
		// }

		/*
		 * initialization
		 */
		final Map<GoodType, Double> bundleOfInputFactors = new HashMap<GoodType, Double>(
				capitalNullSafe);

		// determine initialization value
		final double initializationValueForInputs;
		if (delegate.getNeedsAllInputFactorsNonZeroForPartialDerivate()) {
			initializationValueForInputs = initializationValue;
		} else {
			initializationValueForInputs = 0.0;
		}

		// set initialization value
		for (final GoodType inputType : getInputGoodTypes()) {
			bundleOfInputFactors.put(inputType,
					MathUtil.nullSafeValue(bundleOfInputFactors.get(inputType))
							+ initializationValueForInputs);
		}

		// ------ calculation -----------------------------------------

		/*
		 * iterative calculation of maximizing inputs
		 */
		double budgetSpent = 0.0;

		// maximize profit
		final int NUMBER_OF_ITERATIONS = getInputGoodTypes().size()
				* numberOfIterations;
		final double budgetPerIteration = budget / NUMBER_OF_ITERATIONS;

		while (true) {
			// would this iteration lead to overspending of the budget?
			if (MathUtil.greater(budgetSpent + budgetPerIteration, budget)) {
				getLog().log("budget planned completely");
				getLog().factory_onCalculateProfitMaximizingProductionFactorsIterative(
						budget, budgetSpent,
						ConvexProductionFunctionTerminationCause.BUDGET_PLANNED);
				break;
			}

			final GoodType optimalInputType = selectProductionFactorWithHighestMarginalOutputPerPrice(
					bundleOfInputFactors, priceFunctionsOfInputTypes,
					capitalNullSafe);

			// no optimal input type could be found, i. e. markets are sold out
			if (optimalInputType == null) {
				getLog().log("no optimal input found -> terminating");
				getLog().factory_onCalculateProfitMaximizingProductionFactorsIterative(
						budget,
						budgetSpent,
						ConvexProductionFunctionTerminationCause.NO_INPUT_AVAILABLE);
				break;
			} else {
				final double oldAmountOfOptimalInputType = bundleOfInputFactors
						.get(optimalInputType);
				final double priceRelevantAmountOfOptimalInputType = Math.max(
						bundleOfInputFactors.get(optimalInputType)
								- capitalNullSafe.get(optimalInputType), 0.0);
				final double marginalPriceOfOptimalInputType = priceFunctionsOfInputTypes
						.get(optimalInputType).getMarginalPrice(
								priceRelevantAmountOfOptimalInputType);

				// additional amounts have to grow slowly, so that the solution
				// space is not left
				final double additionalAmountOfInputType = Math.min(
						budgetPerIteration / marginalPriceOfOptimalInputType,
						Math.max(priceRelevantAmountOfOptimalInputType,
								initializationValue));
				bundleOfInputFactors.put(optimalInputType,
						oldAmountOfOptimalInputType
								+ additionalAmountOfInputType);

				{
					final double estimatedMarginalRevenueOfGoodType = priceOfProducedGoodType
							/ (1.0 + margin);
					final double marginalOutputOfOptimalInputType = calculateMarginalOutput(
							bundleOfInputFactors, optimalInputType);
					final double marginalPriceOfOptimalInputTypePerOutput = marginalPriceOfOptimalInputType
							/ marginalOutputOfOptimalInputType;

					/*
					 * ensure that marginal revenue > marginal cost with NEW
					 * amount; calculation strictly has to happen with NEW
					 * amount, so that no inputs at all are chosen in case that
					 * marginal costs per unit > marginal revenue per unit ->
					 * effect on pricing behaviour, as nothing is bought
					 */
					if (!Double.isNaN(estimatedMarginalRevenueOfGoodType)
							&& !Double
									.isInfinite(estimatedMarginalRevenueOfGoodType)) {
						// a polypoly is assumed -> price = marginal revenue
						if (MathUtil.lesser(estimatedMarginalRevenueOfGoodType,
								marginalPriceOfOptimalInputTypePerOutput)) {
							// revert amount of optimal good type
							// important (see above)
							bundleOfInputFactors.put(optimalInputType,
									oldAmountOfOptimalInputType);
							getLog().log(
									MathUtil.round(estimatedMarginalRevenueOfGoodType)
											+ " estimatedMarginalRevenue"
											+ " < "
											+ MathUtil
													.round(marginalPriceOfOptimalInputTypePerOutput)
											+ " currentMarginalPriceOfInputPerOutput"
											+ " -> "
											+ bundleOfInputFactors.entrySet()
													.toString());
							getLog().factory_onCalculateProfitMaximizingProductionFactorsIterative(
									budget,
									budgetSpent,
									ConvexProductionFunctionTerminationCause.MARGINAL_REVENUE_EXCEEDED);
							break;
						}
					}
				}

				{
					// check maxOutput
					final double newOutput = calculateOutput(bundleOfInputFactors);
					if (!Double.isNaN(maxOutput)
							&& MathUtil.greater(newOutput, maxOutput)) {
						// revert amount of optimal good type
						// important (see above)
						bundleOfInputFactors.put(optimalInputType,
								oldAmountOfOptimalInputType);
						getLog().log(
								"output "
										+ newOutput
										+ " > maxOutput "
										+ maxOutput
										+ " -> "
										+ bundleOfInputFactors.entrySet()
												.toString());
						getLog().factory_onCalculateProfitMaximizingProductionFactorsIterative(
								budget,
								budgetSpent,
								ConvexProductionFunctionTerminationCause.MAX_OUTPUT_EXCEEDED);
						break;
					}
				}

				budgetSpent += marginalPriceOfOptimalInputType
						* additionalAmountOfInputType;
			}
		}

		// ------ cleanup -----------------------------------------

		// reset initialization values
		for (final GoodType inputType : getInputGoodTypes()) {
			bundleOfInputFactors.put(inputType,
					bundleOfInputFactors.get(inputType)
							- initializationValueForInputs);
		}

		return bundleOfInputFactors;
	}

	private Log getLog() {
		return ApplicationContext.getInstance().getLog();
	}
}
