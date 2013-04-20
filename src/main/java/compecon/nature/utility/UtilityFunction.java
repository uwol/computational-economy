package compecon.nature.utility;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import compecon.engine.util.MathUtil;
import compecon.nature.materia.GoodType;

public abstract class UtilityFunction implements IUtilityFunction {

	@Override
	public abstract Set<GoodType> getGoodTypes();

	@Override
	public abstract double calculateUtility(Map<GoodType, Double> bundleOfGoods);

	@Override
	public abstract double calculateMarginalUtility(
			Map<GoodType, Double> bundleOfGoods, GoodType differentialGoodType);

	/**
	 * iterative implementation for calculation of an optimal consumption plan
	 */
	@Override
	public Map<GoodType, Double> calculateOptimalBundleOfGoods(
			Map<GoodType, Double> pricesOfGoods, double budget) {
		// order of exponents is preserved, so that important GoodTypes
		// will be bought first
		Map<GoodType, Double> bundleOfGoods = new LinkedHashMap<GoodType, Double>();
		for (GoodType goodType : this.getGoodTypes())
			// has to be initialized with values > 0; else, marginal utility of
			// all good types is 0, as the total utility is a
			// product (multiplication)
			bundleOfGoods.put(goodType, 0.001);

		double NUMBER_OF_ITERATIONS = bundleOfGoods.size() * 5.0;

		double moneySpent = 0.0;
		while (!MathUtil.equal(budget, moneySpent) && moneySpent < budget) {
			GoodType optimalGoodType = this.calculateOptimalMarginalGoodType(
					bundleOfGoods, pricesOfGoods);
			if (optimalGoodType != null) {
				double priceOfGoodType = pricesOfGoods.get(optimalGoodType);
				double amount = (budget / NUMBER_OF_ITERATIONS)
						/ priceOfGoodType;
				bundleOfGoods.put(optimalGoodType,
						bundleOfGoods.get(optimalGoodType) + amount);
				moneySpent += priceOfGoodType * amount;
			} else
				break;
		}

		return bundleOfGoods;
	}

	protected GoodType calculateOptimalMarginalGoodType(
			Map<GoodType, Double> bundleOfGoods,
			Map<GoodType, Double> pricesOfGoods) {
		GoodType optimalMarginalGoodType = null;
		double highestMarginalUtilityPerPrice = 0.0;

		for (GoodType differentialGoodType : this.getGoodTypes()) {
			double marginalUtility = this.calculateMarginalUtility(
					bundleOfGoods, differentialGoodType);
			double pricePerUnit = pricesOfGoods.get(differentialGoodType);
			if (!Double.isNaN(pricePerUnit)) {
				double marginalUtilityPerPrice = marginalUtility / pricePerUnit;
				if (optimalMarginalGoodType == null
						|| marginalUtilityPerPrice > highestMarginalUtilityPerPrice) {
					optimalMarginalGoodType = differentialGoodType;
					highestMarginalUtilityPerPrice = marginalUtilityPerPrice;
				}
			}
		}
		return optimalMarginalGoodType;
	}
}
