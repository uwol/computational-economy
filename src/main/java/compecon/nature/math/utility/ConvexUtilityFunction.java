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

package compecon.nature.math.utility;

import java.util.LinkedHashMap;
import java.util.Map;

import compecon.engine.util.MathUtil;
import compecon.nature.materia.GoodType;
import compecon.nature.math.IFunction;

/**
 * A convex function -> any local optimum must be a global optimum<br />
 * http://en.wikipedia.org/wiki/Convex_optimization
 */
public abstract class ConvexUtilityFunction extends UtilityFunction {

	protected ConvexUtilityFunction(IFunction delegate) {
		super(delegate);
	}

	/**
	 * iterative implementation for calculating an optimal consumption plan
	 */
	public Map<GoodType, Double> calculateUtilityMaximizingInputsUnderBudgetRestriction(
			Map<GoodType, Double> pricesOfGoods, double budget) {
		// order of exponents is preserved, so that important GoodTypes
		// will be bought first
		Map<GoodType, Double> bundleOfGoods = new LinkedHashMap<GoodType, Double>();
		for (GoodType goodType : this.getInputGoodTypes())
			bundleOfGoods.put(goodType, 0.0);

		if (MathUtil.equal(budget, 0))
			return bundleOfGoods;

		double NUMBER_OF_ITERATIONS = bundleOfGoods.size() * 5.0;
		double moneySpent = 0.0;

		while (MathUtil.greater(budget, moneySpent)) {
			GoodType optimalInput = this
					.selectInputWithHighestMarginalUtilityPerPrice(
							bundleOfGoods, pricesOfGoods);
			if (optimalInput != null) {
				double priceOfGoodType = pricesOfGoods.get(optimalInput);
				double amount = (budget / NUMBER_OF_ITERATIONS)
						/ priceOfGoodType;
				bundleOfGoods.put(optimalInput, bundleOfGoods.get(optimalInput)
						+ amount);
				moneySpent += priceOfGoodType * amount;
			} else
				break;
		}

		return bundleOfGoods;
	}

	private GoodType selectInputWithHighestMarginalUtilityPerPrice(
			Map<GoodType, Double> bundleOfGoods,
			Map<GoodType, Double> pricesOfGoods) {
		GoodType optimalInput = (GoodType) this.getInputGoodTypes().toArray()[0];
		double highestMarginalUtilityPerPrice = 0.0;

		for (GoodType goodType : this.getInputGoodTypes()) {
			double partialDerivative = this.calculateMarginalUtility(
					bundleOfGoods, goodType);
			double pricePerUnit = pricesOfGoods.get(goodType);
			if (!Double.isNaN(pricePerUnit)) {
				double marginalUtilityPerPrice = partialDerivative
						/ pricePerUnit;
				if (MathUtil.greater(marginalUtilityPerPrice,
						highestMarginalUtilityPerPrice)
						|| (MathUtil.equal(marginalUtilityPerPrice,
								highestMarginalUtilityPerPrice) && bundleOfGoods
								.get(goodType) == 0)) {
					optimalInput = goodType;
					highestMarginalUtilityPerPrice = marginalUtilityPerPrice;
				}
			}
		}
		return optimalInput;
	}
}
