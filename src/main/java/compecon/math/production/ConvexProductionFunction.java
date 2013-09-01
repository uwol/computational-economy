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
import compecon.engine.util.MathUtil;
import compecon.materia.GoodType;
import compecon.math.IFunction;

public abstract class ConvexProductionFunction extends ProductionFunction {

	protected ConvexProductionFunction(IFunction<GoodType> delegate) {
		super(delegate);
	}

	public Map<GoodType, Double> calculateProfitMaximizingBundleOfProductionFactorsUnderBudgetRestriction(
			double priceOfProducedGoodType,
			Map<GoodType, Double> pricesOfProductionFactors,
			final double budget, final double maxOutput) {

		// check if inputs have NaN prices
		boolean costsAreNaN = false;
		for (GoodType inputType : this.getInputGoodTypes()) {
			if (Double.isNaN(pricesOfProductionFactors.get(inputType))) {
				costsAreNaN = true;
				break;
			}
		}

		// define estimated revenue per unit
		final double estMarginalRevenue = priceOfProducedGoodType;

		/*
		 * special cases
		 */

		// special case: if some prices are NaN, then not all inputs can be set.
		// This becomes a problem, if all inputs have to be set -> return zero
		// input
		if (costsAreNaN
				&& this.delegate
						.getNeedsAllInputFactorsNonZeroForPartialDerivate()) {
			Log.log("at least one of the prices is Double.NaN, but the production function needs all inputs set");
			final Map<GoodType, Double> bundleOfInputs = new LinkedHashMap<GoodType, Double>();
			for (GoodType inputType : this.getInputGoodTypes())
				bundleOfInputs.put(inputType, 0.0);
			return bundleOfInputs;
		}

		// special case: check for budget
		if (MathUtil.equal(budget, 0.0)) {
			Log.log("budget is " + budget);
			final Map<GoodType, Double> bundleOfInputs = new LinkedHashMap<GoodType, Double>();
			for (GoodType inputType : this.getInputGoodTypes())
				bundleOfInputs.put(inputType, 0.0);
			return bundleOfInputs;
		}

		// special case: check for estimated revenue per unit
		if (MathUtil.equal(estMarginalRevenue, 0.0)) {
			Log.log("estMarginalRevenue = " + estMarginalRevenue
					+ " -> no production");
			final Map<GoodType, Double> bundleOfInputs = new LinkedHashMap<GoodType, Double>();
			for (GoodType inputType : this.getInputGoodTypes())
				bundleOfInputs.put(inputType, 0.0);
			return bundleOfInputs;
		}

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
						* pricesOfProductionFactors.get(inputType);
			}
		} else {
			for (GoodType inputType : this.getInputGoodTypes())
				bundleOfInputFactors.put(inputType, 0.0);
		}

		// maximize profit
		final int NUMBER_OF_ITERATIONS = this.getInputGoodTypes().size() * 50;

		double lastProfitableMarginalCost = 0.0;
		while (MathUtil.greater(budget, moneySpent)) {
			GoodType optimalInput = this
					.calculateProfitMaximizingBundleOfProductionFactorsUnderBudgetRestriction(
							bundleOfInputFactors, pricesOfProductionFactors);

			double marginalCost = pricesOfProductionFactors.get(optimalInput)
					/ this.calculateMarginalOutput(bundleOfInputFactors,
							optimalInput);
			double priceOfGoodType = pricesOfProductionFactors
					.get(optimalInput);
			double amount = (budget / NUMBER_OF_ITERATIONS) / priceOfGoodType;
			bundleOfInputFactors.put(optimalInput,
					bundleOfInputFactors.get(optimalInput) + amount);
			double newOutput = this.calculateOutput(bundleOfInputFactors);

			if (!Double.isNaN(estMarginalRevenue)
					&& !Double.isInfinite(estMarginalRevenue)) {
				// a polypoly is assumed -> price = marginal revenue
				if (MathUtil.lesser(estMarginalRevenue, marginalCost)) {
					Log.log(MathUtil.round(lastProfitableMarginalCost)
							+ " deltaCost" + " <= "
							+ MathUtil.round(estMarginalRevenue)
							+ " deltaEstRevenue" + " < "
							+ MathUtil.round(marginalCost) + " deltaCost"
							+ " -> "
							+ bundleOfInputFactors.entrySet().toString());
					break;
				}
			}

			if (!Double.isNaN(maxOutput)
					&& MathUtil.greater(newOutput, maxOutput)) {
				bundleOfInputFactors.put(optimalInput,
						bundleOfInputFactors.get(optimalInput) - amount);
				Log.log("output " + newOutput + " > maxOutput " + maxOutput
						+ " -> " + bundleOfInputFactors.entrySet().toString());
				break;
			}

			lastProfitableMarginalCost = marginalCost;
			moneySpent += priceOfGoodType * amount;
		}

		return bundleOfInputFactors;
	}
}
