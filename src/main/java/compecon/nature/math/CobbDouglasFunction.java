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

package compecon.nature.math;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import compecon.engine.util.MathUtil;

/**
 * U = (g_1)^(e_1) * (g_2)^(e_2) * ... * (g_n)^(e_n) | g_1 + g_2 + ... + g_n = 1
 */
public class CobbDouglasFunction<T> extends ConvexFunction<T> implements
		IFunction<T> {

	protected final Map<T, Double> exponents;

	protected double coefficient;

	public CobbDouglasFunction(Map<T, Double> exponents, double coefficient) {
		super(true);

		/*
		 * constraint on exponents
		 */
		double sumOfExponents = 0.0;
		for (Double exponent : exponents.values()) {
			if (exponent == 1)
				throw new RuntimeException("exponent is 1");
			sumOfExponents += exponent;
		}
		if (!MathUtil.equal(sumOfExponents, 1))
			throw new RuntimeException("sum of exponents not 1");

		if (!MathUtil.greater(coefficient, 0))
			throw new RuntimeException("coefficient is not > 0");

		this.exponents = exponents;
		this.coefficient = coefficient;
	}

	@Override
	public Set<T> getInputTypes() {
		return this.exponents.keySet();
	}

	@Override
	public double f(Map<T, Double> bundleOfInputs) {
		double output = this.coefficient;
		for (Entry<T, Double> entry : this.exponents.entrySet()) {
			T inputType = entry.getKey();
			double exponent = entry.getValue();
			double base = bundleOfInputs.containsKey(inputType) ? bundleOfInputs
					.get(inputType) : 0.0;
			output = output * Math.pow(base, exponent);
		}
		return output;
	}

	@Override
	public double partialDerivative(Map<T, Double> forBundleOfInputs,
			T withRespectToInputType) {
		/*
		 * Constant
		 */
		double constant = this.coefficient;
		for (Entry<T, Double> entry : this.exponents.entrySet()) {
			T inputType = entry.getKey();
			if (inputType != withRespectToInputType) {
				double base = forBundleOfInputs.containsKey(inputType) ? forBundleOfInputs
						.get(inputType) : 0;
				double exponent = entry.getValue();
				constant = constant * Math.pow(base, exponent);
			}
		}

		/*
		 * Differential factor
		 */
		double differentialBase = forBundleOfInputs
				.containsKey(withRespectToInputType) ? forBundleOfInputs
				.get(withRespectToInputType) : 0;
		double differentialExponent = this.exponents
				.containsKey(withRespectToInputType) ? this.exponents
				.get(withRespectToInputType) - 1 : 0;
		double differentialCoefficient = this.exponents
				.containsKey(withRespectToInputType) ? this.exponents
				.get(withRespectToInputType) : 0;
		double differentialFactor = differentialCoefficient
				* Math.pow(differentialBase, differentialExponent);

		// Java returns Double.NaN for 0 * Double.INFINITE -> return 0
		if (constant == 0.0 && Double.isInfinite(differentialFactor))
			return 0.0;

		return constant * differentialFactor;
	}

	/**
	 * This method implements the analytical solution for the lagrange function
	 * of an optimization problem under budget constraints. It overwrites the
	 * general solution for convex functions because of performance reasons.
	 */
	@Override
	public Map<T, Double> calculateOutputMaximizingInputsUnderBudgetRestriction(
			Map<T, Double> costsOfInputs, double budgetRestriction) {
		Map<T, Double> bundleOfInputs = new LinkedHashMap<T, Double>();
		Map<T, Double> exponents = this.getExponents();

		/*
		 * analytical formula for the optimal solution of a Cobb-Douglas
		 * function under given budget restriction -> lagrange function
		 */
		for (T inputType : this.getInputTypes()) {
			double optimalAmount = exponents.get(inputType) * budgetRestriction
					/ costsOfInputs.get(inputType);
			if (Double.isNaN(optimalAmount))
				optimalAmount = 0.0;
			bundleOfInputs.put(inputType, optimalAmount);
		}

		return bundleOfInputs;
	}

	public Map<T, Double> calculateOutputMaximizingInputsUnderBudgetRestrictionIterative(
			Map<T, Double> costsOfInputs, double budgetRestriction) {
		return super.calculateOutputMaximizingInputsUnderBudgetRestriction(
				costsOfInputs, budgetRestriction);
	}

	public Map<T, Double> getExponents() {
		return this.exponents;
	}

	public double getCoefficient() {
		return this.coefficient;
	}

	public void setCoefficient(double coefficient) {
		this.coefficient = coefficient;
	}
}
