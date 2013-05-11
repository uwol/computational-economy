package compecon.nature.math;

import java.util.Map;

import compecon.engine.util.MathUtil;
import compecon.nature.materia.GoodType;

public abstract class Function implements IFunction {

	public GoodType findLargestPartialDerivate(
			Map<GoodType, Double> bundleOfInputGoods) {
		GoodType optimalGoodType = (GoodType) this.getInputGoodTypes()
				.toArray()[0];
		double optimalPartialDerivate = 0;

		for (GoodType goodType : this.getInputGoodTypes()) {
			double partialDerivate = this.partialDerivative(bundleOfInputGoods,
					goodType);
			if (optimalGoodType == null
					|| MathUtil
							.greater(partialDerivate, optimalPartialDerivate)) {
				optimalGoodType = goodType;
				optimalPartialDerivate = partialDerivate;
			}
		}
		return optimalGoodType;
	}

	public GoodType findLargestPartialDerivatePerPrice(
			Map<GoodType, Double> bundleOfInputGoods,
			Map<GoodType, Double> pricesOfInputGoods) {
		GoodType optimalInput = (GoodType) this.getInputGoodTypes().toArray()[0];
		double highestPartialDerivatePerPrice = 0.0;

		for (GoodType goodType : this.getInputGoodTypes()) {
			double partialDerivative = this.partialDerivative(
					bundleOfInputGoods, goodType);
			double pricePerUnit = pricesOfInputGoods.get(goodType);
			if (!Double.isNaN(pricePerUnit)) {
				double partialDerivativePerPrice = partialDerivative
						/ pricePerUnit;
				/*
				 * the check for bundleOfInputGoods.get(goodType) == 0 is a
				 * heuristic for certain functions (e.g. Cobb-Douglas): if for
				 * all alternatives the partial derivative is 0, choose an
				 * input, that has not been chosen before -> in case of
				 * Coob-Douglas partial derivative becomes > 0, as soon as all
				 * inputs > 0
				 */
				if (MathUtil.greater(partialDerivativePerPrice,
						highestPartialDerivatePerPrice)
						|| (MathUtil.equal(partialDerivativePerPrice,
								highestPartialDerivatePerPrice) && bundleOfInputGoods
								.get(goodType) == 0)) {
					optimalInput = goodType;
					highestPartialDerivatePerPrice = partialDerivativePerPrice;
				}
			}
		}
		return optimalInput;
	}
}
