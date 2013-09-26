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

package compecon.math.production;

import java.util.LinkedHashMap;
import java.util.Map;

import compecon.engine.jmx.Log;
import compecon.engine.util.ConfigurationUtil;
import compecon.engine.util.MathUtil;
import compecon.materia.GoodType;
import compecon.math.IFunction;
import compecon.math.price.IPriceFunction;

public abstract class ConvexProductionFunction extends ProductionFunction {

	protected ConvexProductionFunction(IFunction<GoodType> delegate) {
		super(delegate);
	}

	public Map<GoodType, Double> calculateProfitMaximizingProductionFactors(
			double priceOfProducedGoodType,
			Map<GoodType, IPriceFunction> priceFunctionsOfInputTypes,
			final double budget, final double maxOutput, final double margin) {
		return this.calculateProfitMaximizingProductionFactorsIterative(
				priceOfProducedGoodType, priceFunctionsOfInputTypes, budget,
				maxOutput,
				ConfigurationUtil.MathConfig.getNumberOfIterations(), margin);
	}

	public Map<GoodType, Double> calculateProfitMaximizingProductionFactorsIterative(
			double priceOfProducedGoodType,
			Map<GoodType, IPriceFunction> priceFunctionsOfInputTypes,
			final double budget, final double maxOutput,
			final int numberOfIterations, final double margin) {

		// check if inputs have NaN prices
		boolean pricesAreNaN = false;
		for (GoodType inputType : this.getInputGoodTypes()) {
			if (Double.isNaN(priceFunctionsOfInputTypes.get(inputType)
					.getPrice(0.0))) {
				pricesAreNaN = true;
				break;
			}
		}

		/*
		 * special cases
		 */

		// special case: if some input prices are NaN, then not all inputs can
		// be set. This becomes a problem, if all inputs have to be set ->
		// return zero input
		if (pricesAreNaN
				&& this.delegate
						.getNeedsAllInputFactorsNonZeroForPartialDerivate()) {
			Log.log("at least one of the prices is Double.NaN, but the production function needs all inputs set");
			final Map<GoodType, Double> bundleOfInputs = new LinkedHashMap<GoodType, Double>();
			for (GoodType inputType : this.getInputGoodTypes())
				bundleOfInputs.put(inputType, 0.0);
			return bundleOfInputs;
		}

		// special case: check for budget
		if (MathUtil.lesserEqual(budget, 0.0)) {
			Log.log("budget is " + budget);
			final Map<GoodType, Double> bundleOfInputs = new LinkedHashMap<GoodType, Double>();
			for (GoodType inputType : this.getInputGoodTypes())
				bundleOfInputs.put(inputType, 0.0);
			return bundleOfInputs;
		}

		// special case: check for estimated revenue per unit being 0
		if (MathUtil.equal(priceOfProducedGoodType, 0.0)) {
			Log.log("priceOfProducedGoodType = " + priceOfProducedGoodType
					+ " -> no production");
			final Map<GoodType, Double> bundleOfInputs = new LinkedHashMap<GoodType, Double>();
			for (GoodType inputType : this.getInputGoodTypes())
				bundleOfInputs.put(inputType, 0.0);
			return bundleOfInputs;
		}

		// special case: check for estimated revenue per unit being NaN ->
		// needed for bootstrapping markets
		// problem: producing this little leads to a complete sold amount ->
		// rising prices because of pricingBehaviour; again, Double.NaN price,
		// as nothing is offered
		//
		// if (Double.isNaN(priceOfProducedGoodType)) {
		// Log.log("priceOfProducedGoodType = " + priceOfProducedGoodType
		// + " -> cautious production");
		// final Map<GoodType, Double> bundleOfInputs = new
		// LinkedHashMap<GoodType, Double>();
		// for (GoodType inputType : this.getInputGoodTypes())
		// bundleOfInputs.put(inputType, 0.001);
		// return bundleOfInputs;
		// }

		/*
		 * regular calculation
		 */
		double moneySpent = 0.0;
		final Map<GoodType, Double> bundleOfInputFactors = new LinkedHashMap<GoodType, Double>();

		// initialize
		if (this.delegate.getNeedsAllInputFactorsNonZeroForPartialDerivate()) {
			for (GoodType inputType : this.getInputGoodTypes()) {
				bundleOfInputFactors.put(inputType, 0.0000001);
				moneySpent += bundleOfInputFactors.get(inputType)
						* priceFunctionsOfInputTypes.get(inputType).getPrice(
								bundleOfInputFactors.get(inputType));
			}
		} else {
			for (GoodType inputType : this.getInputGoodTypes())
				bundleOfInputFactors.put(inputType, 0.0);
		}

		// maximize profit
		final int NUMBER_OF_ITERATIONS = this.getInputGoodTypes().size()
				* numberOfIterations;

		// determine estimatedMarginalRevenueOfGoodType
		double estimatedMarginalRevenueOfGoodType = priceOfProducedGoodType
				/ (1.0 + margin);

		while (MathUtil.greater(budget, moneySpent)) {
			GoodType optimalInputType = this
					.selectProductionFactorWithHighestMarginalOutputPerPrice(
							bundleOfInputFactors, priceFunctionsOfInputTypes);

			if (optimalInputType == null) {
				break;
			} else {
				double marginalPriceOfOptimalInputType = priceFunctionsOfInputTypes
						.get(optimalInputType).getMarginalPrice(
								bundleOfInputFactors.get(optimalInputType));
				double marginalOutputOfOptimalInputType = this
						.calculateMarginalOutput(bundleOfInputFactors,
								optimalInputType);
				double marginalPriceOfOptimalInputTypePerOutput = marginalPriceOfOptimalInputType
						/ marginalOutputOfOptimalInputType;

				// ensure that marginal revenue > marginal cost
				if (!Double.isInfinite(estimatedMarginalRevenueOfGoodType)) {
					// a polypoly is assumed -> price = marginal revenue
					if (MathUtil.lesser(estimatedMarginalRevenueOfGoodType,
							marginalPriceOfOptimalInputTypePerOutput)) {
						Log.log(MathUtil
								.round(estimatedMarginalRevenueOfGoodType)
								+ " estimatedMarginalRevenue"
								+ " < "
								+ MathUtil
										.round(marginalPriceOfOptimalInputTypePerOutput)
								+ " currentMarginalPriceOfInputPerOutput"
								+ " -> "
								+ bundleOfInputFactors.entrySet().toString());
						break;
					}
				}

				double amount = (budget / (double) NUMBER_OF_ITERATIONS)
						/ marginalPriceOfOptimalInputType;
				bundleOfInputFactors.put(optimalInputType,
						bundleOfInputFactors.get(optimalInputType) + amount);
				double newOutput = this.calculateOutput(bundleOfInputFactors);

				// check maxOutput
				if (!Double.isNaN(maxOutput)
						&& MathUtil.greater(newOutput, maxOutput)) {
					bundleOfInputFactors
							.put(optimalInputType,
									bundleOfInputFactors.get(optimalInputType)
											- amount);
					Log.log("output " + newOutput + " > maxOutput " + maxOutput
							+ " -> "
							+ bundleOfInputFactors.entrySet().toString());
					break;
				}

				moneySpent += marginalPriceOfOptimalInputType * amount;
			}
		}

		return bundleOfInputFactors;
	}
}
