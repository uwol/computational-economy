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
import java.util.Map.Entry;
import java.util.Set;

import compecon.engine.util.MathUtil;

public class CESFunction<T> extends ConvexFunction<T> implements IFunction<T> {

	protected double mainCoefficient;

	protected final Map<T, Double> coefficients;

	protected final double substitutionFactor;

	protected final double homogenityFactor;

	public CESFunction(double mainCoefficient, Map<T, Double> coefficients,
			double substitutionFactor, double homogenityFactor) {
		super(false);

		if (!MathUtil.greater(mainCoefficient, 0.0))
			throw new RuntimeException("mainCoefficient has to be > 0");

		if (substitutionFactor <= -1.0 || substitutionFactor >= 0.0)
			throw new RuntimeException(
					"interior exponent has to be in interval ]-1, 0[ for a convex maximization problem");

		if (!MathUtil.greater(homogenityFactor, 0.0))
			throw new RuntimeException("homogenityFactor has to be > 0");

		double exteriorExponent = -homogenityFactor / substitutionFactor;
		if (exteriorExponent <= 0.0 || exteriorExponent >= 1.0)
			throw new RuntimeException(
					"exterior exponent has to be in interval ]0, 1[ for a convex function");

		this.mainCoefficient = mainCoefficient;
		this.coefficients = coefficients;
		this.substitutionFactor = substitutionFactor;
		this.homogenityFactor = homogenityFactor;
	}

	@Override
	public Set<T> getInputTypes() {
		return this.coefficients.keySet();
	}

	/**
	 * y = a * [c_1 * (x_1)^(-r) + ... + c_n * (x_n)^(r)]^(-h/r)
	 */
	@Override
	public double f(Map<T, Double> bundleOfInputs) {
		double sum = 0.0;
		for (Entry<T, Double> entry : this.coefficients.entrySet()) {
			T inputType = entry.getKey();
			double coefficient = entry.getValue();
			double input = bundleOfInputs.get(inputType);
			sum += coefficient * Math.pow(input, -this.substitutionFactor);
		}
		return this.mainCoefficient
				* Math.pow(sum, (-1.0 * this.homogenityFactor)
						/ this.substitutionFactor);
	}

	/**
	 * dy/d(x_1) = [(-h/r) * a * [c_1 * (x_1)^(r) + ... + c_n * (x_n)^(r)]^(-h/r
	 * - 1)] * [-r * c_1 * (x_1)^(-r-1)]
	 */
	@Override
	public double partialDerivative(Map<T, Double> forBundleOfInputs,
			T withRespectToInputType) {

		/*
		 * exterior derivative
		 */
		double sum = 0.0;
		for (Entry<T, Double> entry : this.coefficients.entrySet()) {
			T inputType = entry.getKey();
			double coefficient = entry.getValue();
			double input = forBundleOfInputs.get(inputType);
			sum += coefficient
					* Math.pow(input, -1.0 * this.substitutionFactor);
		}
		double exponent = (-1.0 * this.homogenityFactor / this.substitutionFactor) - 1.0;
		double exteriorDerivative = (-1.0 * this.homogenityFactor / this.substitutionFactor)
				* this.mainCoefficient * Math.pow(sum, exponent);

		/*
		 * interior derivative
		 */
		double coefficient = this.coefficients.get(withRespectToInputType);
		double differentialInput = forBundleOfInputs
				.get(withRespectToInputType);
		double interiorDerivative = -1.0
				* this.substitutionFactor
				* coefficient
				* Math.pow(differentialInput,
						(-1.0 * this.substitutionFactor) - 1.0);

		// Java returns Double.NaN for 0 * Double.INFINITE -> return 0
		if (exteriorDerivative == 0.0 && Double.isInfinite(interiorDerivative))
			return 0.0;
		if (interiorDerivative == 0.0 && Double.isInfinite(exteriorDerivative))
			return 0.0;
		return exteriorDerivative * interiorDerivative;
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
	 * general solution for convex functions because of performance reasons. <br />
	 * <br />
	 * L(x_1, ..., x_n, l) = a * [c_1 * (x_1)^(-r) + ... + c_n *
	 * (x_n)^(r)]^(-h/r) + l(p_1 * x_1 + ... + p_n * x_n - b) <br />
	 * <br />
	 * => <br />
	 * dL(x_1, ..., x_n, l) / dx_1 = [(-h/r) * a * [c_1 * (x_1)^(r) + ... + c_n
	 * * (x_n)^(r)]^(-h/r - 1)] * [-r * c_1 * (x_1)^(-r-1)] + l * p_1 <br />
	 * dL(x_1, ..., x_n, l) / dx_2 = [(-h/r) * a * [c_1 * (x_1)^(r) + ... + c_n
	 * * (x_n)^(r)]^(-h/r - 1)] * [-r * c_2 * (x_2)^(-r-1)] + l * p_2 <br />
	 * dL(x_1, ..., x_n, l) / dl = p_1 * x_1 + ... + p_n * x_n - b <br />
	 * <br />
	 * <br />
	 * dL(x_1, ..., x_n, l) / dx_1 = 0 <br />
	 * dL(x_1, ..., x_n, l) / dx_2 = 0 <br />
	 * dL(x_1, ..., x_n, l) / dl = 0 <br />
	 * <br />
	 * => <br />
	 * l = [(-h/r) * a * [c_1 * (x_1)^(r) + ... + c_n * (x_n)^(r)]^(-h/r - 1)] *
	 * [-r * c_1 * (x_1)^(-r-1)] / p_1 <br />
	 * l = [(-h/r) * a * [c_1 * (x_1)^(r) + ... + c_n * (x_n)^(r)]^(-h/r - 1)] *
	 * [-r * c_2 * (x_2)^(-r-1)] / p_2 <br />
	 * p_1 * x_1 + ... + p_n * x_n = b <br />
	 * <br />
	 * => (1) = (2)<br />
	 * p_1 * c_2 * (x_2)^(-r-1) = p_2 * c_1 * (x_1)^(-r-1) <br />
	 * => <br />
	 * x_2 = [(p_1 * c_2) / (p_2 * c_1)]^(1/(r+1)) * x_1 <br />
	 * x_n = [(p_1 * c_n) / (p_n * c_1)]^(1/(r+1)) * x_1 <br />
	 * <br />
	 * b = p_1 * x_1 + p_2 * [(p_1 * c_2) / (p_2 * c_1)]^(1/(r+1)) * x_1 + ... +
	 * p_n * [(p_1 * c_n) / (p_n * c_1)]^(1/(r+1)) * x_1 <br />
	 * <br />
	 * x_1 = b / [p_1 + p_2 * [(p_1 * c_2) / (p_2 * c_1)]^(1/(r+1)) + p_n [(p_1
	 * * c_n) / (p_n * c_1)]^(1/(r+1))]
	 */
	public Map<T, Double> calculateOutputMaximizingInputsUnderBudgetRestrictionAnalytical(
			Map<T, Double> costsOfInputs, double budget) {
		Map<T, Double> bundleOfInputs = new LinkedHashMap<T, Double>();

		/*
		 * analytical formula for the optimal solution of a CES function under
		 * given budget restriction -> lagrange function
		 */
		for (T referenceInputType : this.getInputTypes()) {
			double divisor = costsOfInputs.get(referenceInputType);
			for (T inputType : this.getInputTypes()) {
				if (!referenceInputType.equals(inputType)) {
					if (!Double.isNaN(costsOfInputs.get(referenceInputType))
							&& !Double.isNaN(costsOfInputs.get(inputType))) {
						double base = costsOfInputs.get(referenceInputType)
								* this.coefficients.get(inputType)
								/ (costsOfInputs.get(inputType) * this.coefficients
										.get(referenceInputType));
						double exponent = 1.0 / (this.substitutionFactor + 1.0);
						divisor += costsOfInputs.get(inputType)
								* Math.pow(base, exponent);
					}
				}
			}

			double optimalAmount = budget / divisor;
			if (Double.isNaN(optimalAmount))
				optimalAmount = 0.0;
			bundleOfInputs.put(referenceInputType, optimalAmount);
		}

		return bundleOfInputs;
	}

	public double getMainCoefficient() {
		return this.mainCoefficient;
	}

	public void setMainCoefficient(double mainCoefficient) {
		this.mainCoefficient = mainCoefficient;
	}
}
