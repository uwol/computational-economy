package compecon.nature.math;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

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

		// NaN prices should be initialized to a large number, so that they do
		// not disturb Cobb-Douglas functions
		for (T inputType : this.getInputTypes()) {
			if (Double.isNaN(pricesOfInputs.get(inputType)))
				pricesOfInputs.put(inputType, 9999999999999.);
		}

		// check for budget
		if (MathUtil.equal(budget, 0))
			return bundleOfInputs;

		// maximize output
		double NUMBER_OF_ITERATIONS = bundleOfInputs.size() * 20.0;

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

		// NaN prices result in minimal deviations from 0.0 -> reset to 0.0
		for (Entry<T, Double> entry : bundleOfInputs.entrySet()) {
			if (MathUtil.equal(entry.getValue(), 0.0))
				entry.setValue(0.0);
		}

		return bundleOfInputs;
	}
}
