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

		if (!MathUtil.lesser(substitutionFactor, 0.0))
			throw new RuntimeException("substitutionFactor is not < 0");

		if (!MathUtil.greater(homogenityFactor, 0.0))
			throw new RuntimeException("homogenityFactor is not > 0");

		if (!MathUtil.greater(mainCoefficient, 0.0))
			throw new RuntimeException("mainCoefficient is not > 0");

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
			double input = bundleOfInputs.containsKey(inputType) ? bundleOfInputs
					.get(inputType) : 0.0;
			sum += Math.pow(coefficient * input, -this.substitutionFactor);
		}
		return this.mainCoefficient
				* Math.pow(sum, -this.homogenityFactor
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
		 * Exterior derivative
		 */

		double sum = 0.0;
		for (Entry<T, Double> entry : this.coefficients.entrySet()) {
			T inputType = entry.getKey();
			double coefficient = entry.getValue();
			double input = forBundleOfInputs.containsKey(inputType) ? forBundleOfInputs
					.get(inputType) : 0.0;
			sum += Math.pow(coefficient * input, -this.substitutionFactor);
		}
		double exteriorDerivative = (-this.homogenityFactor / this.substitutionFactor)
				* this.mainCoefficient
				* Math.pow(
						sum,
						(-this.homogenityFactor / this.substitutionFactor) - 1.0);

		/*
		 * interior derivative
		 */
		double coefficient = this.coefficients
				.containsKey(withRespectToInputType) ? this.coefficients
				.get(withRespectToInputType) : 0.0;
		double differentialInput = forBundleOfInputs
				.containsKey(withRespectToInputType) ? forBundleOfInputs
				.get(withRespectToInputType) : 0.0;
		double interiorDerivative = -this.substitutionFactor * coefficient
				* Math.pow(differentialInput, -this.substitutionFactor - 1.0);
		return exteriorDerivative * interiorDerivative;
	}

	public double getMainCoefficient() {
		return this.mainCoefficient;
	}

	public void setMainCoefficient(double mainCoefficient) {
		this.mainCoefficient = mainCoefficient;
	}
}
