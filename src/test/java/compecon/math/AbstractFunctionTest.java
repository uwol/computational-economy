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

package compecon.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Map.Entry;

import compecon.materia.GoodType;

public abstract class AbstractFunctionTest {

	final double epsilon = 0.01;

	public void assertOutputIsOptimalUnderBudget(
			final Function<GoodType> function, final double budgetRestriction,
			final Map<GoodType, Double> costsOfInputs,
			final Map<GoodType, Double> referenceBundleOfInputs) {

		Map<GoodType, Double> rangeScanBundleOfInputs = function
				.calculateOutputMaximizingInputsUnderBudgetRestrictionByRangeScan(
						costsOfInputs, budgetRestriction);

		// check that budget restriction is not violated
		double sumOfCostsOfOptimalBundleOfInputs = 0.0;
		for (Entry<GoodType, Double> inputEntry : rangeScanBundleOfInputs
				.entrySet()) {
			sumOfCostsOfOptimalBundleOfInputs += costsOfInputs.get(inputEntry
					.getKey()) * inputEntry.getValue();
		}
		if (sumOfCostsOfOptimalBundleOfInputs > budgetRestriction) {
			throw new RuntimeException(
					"optimalBundleOfInputs violates the budget restriction");
		}

		// check that budget restriction is not violated
		double sumOfCostsOfReferenceBundleOfInputs = 0.0;
		for (Entry<GoodType, Double> inputEntry : referenceBundleOfInputs
				.entrySet()) {
			sumOfCostsOfReferenceBundleOfInputs += costsOfInputs.get(inputEntry
					.getKey()) * inputEntry.getValue();
		}
		if (sumOfCostsOfReferenceBundleOfInputs > budgetRestriction) {
			throw new RuntimeException(
					"referenceBundleOfInputs violates the budget restriction");
		}

		assertTrue(function.f(rangeScanBundleOfInputs) <= function
				.f(referenceBundleOfInputs));
	}

	/**
	 * in an optimum partial derivatives per price have to be identical
	 */
	public void assertPartialDerivativesPerPriceAreEqual(
			final Function<GoodType> function,
			final Map<GoodType, Double> bundleOfInputs,
			final Map<GoodType, Double> costsOfInputs) {
		Map<GoodType, Double> partialDerivatives = function
				.partialDerivatives(bundleOfInputs);
		for (Entry<GoodType, Double> outerPartialDerivativeEntry : partialDerivatives
				.entrySet()) {
			if (!Double.isNaN(costsOfInputs.get(outerPartialDerivativeEntry
					.getKey()))) {
				for (Entry<GoodType, Double> innerPartialDerivativeEntry : partialDerivatives
						.entrySet()) {
					if (!Double.isNaN(costsOfInputs
							.get(innerPartialDerivativeEntry.getKey()))) {
						double innerPartialDerivativePerPrice = innerPartialDerivativeEntry
								.getValue()
								/ costsOfInputs.get(innerPartialDerivativeEntry
										.getKey());
						double outerPartialDerivativePerPrice = outerPartialDerivativeEntry
								.getValue()
								/ costsOfInputs.get(outerPartialDerivativeEntry
										.getKey());
						assertEquals(innerPartialDerivativePerPrice,
								outerPartialDerivativePerPrice, epsilon);
					}
				}
			}
		}
	}
}
