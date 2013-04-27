/*
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

package compecon.nature.math.production;

import java.util.LinkedHashMap;
import java.util.Map;

import compecon.engine.Log;
import compecon.engine.util.MathUtil;
import compecon.nature.materia.GoodType;
import compecon.nature.math.IFunction;

public class ConvexProductionFunction extends ProductionFunction {

	protected ConvexProductionFunction(IFunction delegate) {
		super(delegate);
	}

	public Map<GoodType, Double> calculateProfitMaximizingBundleOfProductionFactorsUnderBudgetRestriction(
			double priceOfProducedGoodType,
			Map<GoodType, Double> pricesOfProductionFactors,
			final double budget, final double maxOutput) {

		// order of exponents is preserved, so that important GoodTypes
		// will be bought first
		Map<GoodType, Double> bundleOfInputFactors = new LinkedHashMap<GoodType, Double>();

		// define estimated revenue per unit
		double estMarginalRevenue = priceOfProducedGoodType;

		// checks
		for (GoodType goodType : this.getInputGoodTypes())
			bundleOfInputFactors.put(goodType, 0.0);

		// check for estimated revenue per unit
		if (MathUtil.equal(estMarginalRevenue, 0.0)) {
			Log.log(agent, "estMarginalRevenue = " + estMarginalRevenue
					+ " -> no production");
			return bundleOfInputFactors;
		}

		// check for budget
		if (MathUtil.equal(budget, 0)) {
			Log.log(agent, "budget is " + budget);
			return bundleOfInputFactors;
		}

		// maximize profit
		final int NUMBER_OF_ITERATIONS = this.getInputGoodTypes().size() * 20;

		double moneySpent = 0.0;
		double lastProfitableMarginalCost = 0;
		while (MathUtil.greater(budget, moneySpent)) {
			GoodType optimalInput = this
					.selectInputWithHighestMarginalOutputPerPrice(
							bundleOfInputFactors, pricesOfProductionFactors);
			double marginalCost = pricesOfProductionFactors.get(optimalInput)
					/ this.calculateMarginalOutput(bundleOfInputFactors,
							optimalInput);
			double priceOfGoodType = pricesOfProductionFactors
					.get(optimalInput);
			double amount = (budget / NUMBER_OF_ITERATIONS) / priceOfGoodType;
			bundleOfInputFactors.put(optimalInput,
					bundleOfInputFactors.get(optimalInput) + amount);
			double newOutput = this.calculateOutput(pricesOfProductionFactors);

			if (!Double.isNaN(estMarginalRevenue)
					&& !Double.isInfinite(estMarginalRevenue)) {
				if (MathUtil.lesser(estMarginalRevenue, marginalCost)) {
					Log.log(agent,
							MathUtil.round(lastProfitableMarginalCost)
									+ " deltaCost"
									+ " <= "
									+ MathUtil.round(estMarginalRevenue)
									+ " deltaEstRevenue"
									+ " < "
									+ MathUtil.round(marginalCost)
									+ " deltaCost"
									+ " -> "
									+ bundleOfInputFactors.entrySet()
											.toString());
					break;
				}
			}

			if (maxOutput != -1 && MathUtil.greater(newOutput, maxOutput)) {
				bundleOfInputFactors.put(optimalInput,
						bundleOfInputFactors.get(optimalInput) - amount);
				Log.log(agent, "output " + newOutput + " > maxOutput "
						+ maxOutput + " -> "
						+ bundleOfInputFactors.entrySet().toString());
				break;
			}

			lastProfitableMarginalCost = marginalCost;
			moneySpent += priceOfGoodType * amount;
		}

		return bundleOfInputFactors;
	}

	private GoodType selectInputWithHighestMarginalOutputPerPrice(
			Map<GoodType, Double> bundleOfGoods,
			Map<GoodType, Double> pricesOfGoods) {
		GoodType optimalInput = (GoodType) this.getInputGoodTypes().toArray()[0];
		double highestMarginalOutputPerPrice = 0.0;

		for (GoodType goodType : this.getInputGoodTypes()) {
			double partialDerivative = this.calculateMarginalOutput(
					bundleOfGoods, goodType);
			double pricePerUnit = pricesOfGoods.get(goodType);
			if (!Double.isNaN(pricePerUnit)) {
				double marginalOutputPerPrice = partialDerivative
						/ pricePerUnit;
				if (MathUtil.greater(marginalOutputPerPrice,
						highestMarginalOutputPerPrice)
						|| (MathUtil.equal(marginalOutputPerPrice,
								highestMarginalOutputPerPrice) && bundleOfGoods
								.get(goodType) == 0)) {
					optimalInput = goodType;
					highestMarginalOutputPerPrice = marginalOutputPerPrice;
				}
			}
		}
		return optimalInput;
	}
}
