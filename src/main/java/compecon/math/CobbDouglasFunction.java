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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import compecon.engine.util.MathUtil;

public class CobbDouglasFunction<T> extends ConvexFunction<T> implements
		IFunction<T> {

	protected double coefficient;

	protected final Map<T, Double> exponents;

	public CobbDouglasFunction(double coefficient, Map<T, Double> exponents) {
		super(true);

		if (!MathUtil.greater(coefficient, 0.0))
			throw new RuntimeException("coefficient has to be > 0");

		double sumOfExponents = 0.0;
		for (Double exponent : exponents.values()) {
			if (exponent >= 1.0 || exponent <= 0.0)
				throw new RuntimeException(
						"each exponent has to be in interval ]0, 1[");
			sumOfExponents += exponent;
		}

		if (!MathUtil.equal(sumOfExponents, 1.0))
			throw new RuntimeException("sum of exponents must equal 1");

		this.exponents = exponents;
		this.coefficient = coefficient;
	}

	@Override
	public Set<T> getInputTypes() {
		return this.exponents.keySet();
	}

	/**
	 * y = (x_1)^(e_1) * (x_2)^(e_2) * ... * (x_n)^(e_n) <br />
	 * | e_1 + e_2 + ... + e_n = 1
	 */
	@Override
	public double f(Map<T, Double> bundleOfInputs) {
		double output = this.coefficient;
		for (T inputType : this.getInputTypes()) {
			double exponent = this.exponents.get(inputType);
			double input = bundleOfInputs.get(inputType);
			output = output * Math.pow(input, exponent);
		}
		return output;
	}

	/**
	 * dy/d(x_1) = e_1 * (x_1)^(e_1 - 1) * (x_2)^(e_2) * ... * (x_n)^(e_n) <br />
	 * | e_1 + e_2 + ... + e_n = 1
	 */
	@Override
	public double partialDerivative(Map<T, Double> forBundleOfInputs,
			T withRespectToInputType) {
		/*
		 * Constant
		 */
		double constant = this.coefficient;
		for (T inputType : this.getInputTypes()) {
			if (!inputType.equals(withRespectToInputType)) {
				double base = forBundleOfInputs.get(inputType);
				double exponent = this.exponents.get(inputType);
				constant = constant * Math.pow(base, exponent);
			}
		}

		/*
		 * Differential factor
		 */
		double differentialInput = forBundleOfInputs
				.get(withRespectToInputType);
		double differentialExponent = this.exponents
				.get(withRespectToInputType) - 1.0;
		double differentialCoefficient = this.exponents
				.get(withRespectToInputType);
		double differentialFactor = differentialCoefficient
				* Math.pow(differentialInput, differentialExponent);

		// Java returns Double.NaN for 0 * Double.INFINITE -> return 0
		if (constant == 0.0 && Double.isInfinite(differentialFactor))
			return 0.0;

		return constant * differentialFactor;
	}

	@Override
	public Map<T, Double> calculateOutputMaximizingInputsUnderBudgetRestriction(
			final Map<T, Double> costsOfInputs, final double budget) {
		return this
				.calculateOutputMaximizingInputsUnderBudgetRestrictionAnalytical(
						costsOfInputs, budget);
	}

	/**
	 * This method implements the analytical solution for the lagrange function
	 * of an optimization problem under budget constraints. It overwrites the
	 * general solution for convex functions because of performance reasons.
	 */
	public Map<T, Double> calculateOutputMaximizingInputsUnderBudgetRestrictionAnalytical(
			Map<T, Double> costsOfInputs, double budgetRestriction) {
		Map<T, Double> bundleOfInputs = new LinkedHashMap<T, Double>();
		Map<T, Double> exponents = this.getExponents();

		boolean costsAreNaN = false;
		for (T inputType : this.getInputTypes()) {
			if (Double.isNaN(costsOfInputs.get(inputType))) {
				costsAreNaN = true;
				break;
			}
		}

		/*
		 * analytical formula for the optimal solution of a Cobb-Douglas
		 * function under given budget restriction -> lagrange function
		 */
		for (T inputType : this.getInputTypes()) {
			double optimalAmount = exponents.get(inputType) * budgetRestriction
					/ costsOfInputs.get(inputType);
			if (costsAreNaN || Double.isNaN(optimalAmount))
				optimalAmount = 0.0;
			bundleOfInputs.put(inputType, optimalAmount);
		}

		return bundleOfInputs;
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
