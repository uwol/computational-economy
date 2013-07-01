package compecon.nature.math;

import java.util.LinkedHashMap;
import java.util.Map;

import compecon.engine.util.MathUtil;

public abstract class ConvexFunction<T> extends Function<T> {

	protected ConvexFunction(
			boolean needsAllInputFactorsNonZeroForPartialDerivate) {
		super(needsAllInputFactorsNonZeroForPartialDerivate);
	}

	/**
	 * iterative implementation for calculating an optimal consumption plan
	 */
	public Map<T, Double> calculateOutputMaximizingInputsUnderBudgetRestriction(
			Map<T, Double> pricesOfInputs, double budget) {

		// order of exponents is preserved, so that important InputTypes
		// will be chosen first
		Map<T, Double> bundleOfInputs = new LinkedHashMap<T, Double>();

		// initialize
		for (T inputType : this.getInputTypes())
			bundleOfInputs.put(inputType, 0.0);

		// check for budget
		if (MathUtil.equal(budget, 0))
			return bundleOfInputs;

		// maximize output
		double NUMBER_OF_ITERATIONS = bundleOfInputs.size() * 10.0;

		double moneySpent = 0.0;
		while (MathUtil.greater(budget, moneySpent)) {
			T optimalInput = this.findLargestPartialDerivatePerPrice(
					bundleOfInputs, pricesOfInputs);
			if (optimalInput != null) {
				double priceOfInputType = pricesOfInputs.get(optimalInput);
				if (!Double.isNaN(priceOfInputType)) {
					double amount = (budget / NUMBER_OF_ITERATIONS)
							/ priceOfInputType;
					bundleOfInputs.put(optimalInput,
							bundleOfInputs.get(optimalInput) + amount);
					moneySpent += priceOfInputType * amount;
				} else
					break;
			} else
				break;
		}

		return bundleOfInputs;
	}
}
