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

package compecon.math.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import compecon.engine.service.impl.FixedPriceFunctionImpl;
import compecon.math.price.PriceFunction;
import compecon.math.price.PriceFunction.PriceFunctionConfig;
import compecon.math.util.MathUtil;

public class CobbDouglasFunctionImpl<T> extends AnalyticalConvexFunctionImpl<T> {

	protected double coefficient;

	protected final Map<T, Double> exponents;

	public CobbDouglasFunctionImpl(final double coefficient,
			final Map<T, Double> exponents) {
		super(true);

		// coefficient has to be > 0
		assert (MathUtil.greater(coefficient, 0.0));

		double sumOfExponents = 0.0;
		for (final Double exponent : exponents.values()) {
			// each exponent has to be in interval ]0, 1[
			assert (exponent < 1.0 && exponent > 0.0);
			sumOfExponents += exponent;
		}

		// sum of exponents must equal 1
		assert (MathUtil.equal(sumOfExponents, 1.0));

		this.exponents = exponents;
		this.coefficient = coefficient;
	}

	@Override
	public Map<T, Double> calculateOutputMaximizingInputs(
			final Map<T, PriceFunction> priceFunctionsOfInputs,
			final double budget) {
		// check whether the analytical solution is viable
		final Map<T, Double> fixedPrices = new HashMap<T, Double>();

		for (final Entry<T, PriceFunction> priceFunctionEntry : priceFunctionsOfInputs
				.entrySet()) {
			if (priceFunctionEntry.getValue() instanceof FixedPriceFunctionImpl) {
				fixedPrices.put(priceFunctionEntry.getKey(), priceFunctionEntry
						.getValue().getPrice(0.0));
			} else {
				break;
			}
		}

		// dispatch
		if (fixedPrices.size() == priceFunctionsOfInputs.size()) {
			return this
					.calculateOutputMaximizingInputsAnalyticalWithFixedPrices(
							fixedPrices, budget);
		} else {
			return super.calculateOutputMaximizingInputs(
					priceFunctionsOfInputs, budget);
		}
	}

	/**
	 * This method implements the analytical solution for the lagrange function
	 * of an optimization problem under budget constraints. It overwrites the
	 * general solution for convex functions for performance reasons.<br />
	 * <br />
	 * L(x_1, ..., x_n, l) = a * (x_1)^(e_1) * ... * (x_n)^(e_n) + l(p_1 * x_1 +
	 * ... + p_n * x_n - b) <br />
	 * 1 = e_1 + e_2 + ... + e_n <br />
	 * <br />
	 * => <br />
	 * dL(x_1, ..., x_n, l) / dx_1 = a * e_1 * (x_1)^(e_1 - 1) * ... *
	 * (x_n)^(e_n) + l * p_1 <br />
	 * dL(x_1, ..., x_n, l) / dx_2 = a * e_2 * (x_2)^(e_2 - 1) * ... *
	 * (x_n)^(e_n) + l * p_2 <br />
	 * ... <br />
	 * dL(x_1, ..., x_n, l) / dx_n = a * e_n * (x_n)^(e_n - 1) * (x_1)^(e_1) *
	 * ... + l * p_n <br />
	 * dL(x_1, ..., x_n, l) / dl = p_1 * x_1 + ... + p_n * x_n - b <br />
	 * <br />
	 * <br />
	 * dL(x_1, ..., x_n, l) / dx_1 = 0 <br />
	 * ... <br />
	 * dL(x_1, ..., x_n, l) / dx_n = 0 <br />
	 * dL(x_1, ..., x_n, l) / dl = 0 <br />
	 * <br />
	 * => <br />
	 * l = a * e_1 * (x_1)^(e_1 - 1) * (x_2)^(e_2) * ... * (x_n)^(e_n) / -p_1 <br />
	 * l = a * (x_1)^(e_1) * e_2 * (x_2)^(e_2 - 1) * ... * (x_n)^(e_n) / -p_2 <br />
	 * <br />
	 * => (1) = (2)<br />
	 * e_1 * (x_1)^(e_1 - 1) * (x_2)^(e_2) * ... * (x_n)^(e_n) / -p_1 =
	 * (x_1)^(e_1) * e_2 * (x_2)^(e_2 - 1) * ... * (x_n)^(e_n) / -p_2 <br />
	 * => <br />
	 * e_1 * (x_1)^(e_1 - 1) * (x_2)^(e_2) / p_1 = (x_1)^(e_1) * e_2 *
	 * (x_2)^(e_2 - 1) / p_2 <br />
	 * <br />
	 * e_1 * (x_2)^(e_2) / (x_2)^(e_2 - 1) / p_1 = e_2 * (x_1)^(e_1) /
	 * (x_1)^(e_1 - 1) / p_2 <br />
	 * <br />
	 * e_1 * x_2 / p_1 = e_2 * x_1 / p_2 <br />
	 * <br />
	 * x_2 = [e_2 * x_1 * p_1] / [p_2 * e_1] <br />
	 * <br />
	 * <br />
	 * b = p_1 * x_1 + p_2 * [e_2 * x_1 * p_1] / [p_2 * e_1] + ... + p_n * [e_n
	 * * x_1 * p_1] / [p_n * e_1] <br />
	 * => <br />
	 * b = x_1 * [p_1 + p_1 * e_2 / e_1 + ... + p_1 * e_n / e_1] <br />
	 * => <br />
	 * b = x_1 * p_1 * [1 + e_2 / e_1 + ... + e_n / e_1] <br />
	 * <br />
	 * x_1 = b / p_1 * [1 + e_2 / e_1 + ... + e_n / e_1] <br />
	 * <br />
	 * x_1 = b / p_1 * [e_1 / e_1 + e_2 / e_1 + ... + e_n / e_1] <br />
	 * <br />
	 * x_1 = b / p_1 * (e_1 + e_2 + ... + e_n) / e_1 <br />
	 * e_1 + e_2 + ... + e_n = 1 => <br />
	 * <br />
	 * x_1 = b * e_1 / p_1
	 */
	public Map<T, Double> calculateOutputMaximizingInputsAnalyticalWithFixedPrices(
			final Map<T, Double> pricesOfInputs, final double budget) {
		final Map<T, Double> bundleOfInputs = new LinkedHashMap<T, Double>();
		final Map<T, Double> exponents = this.getExponents();

		boolean pricesAreNaN = false;

		for (final T inputType : this.getInputTypes()) {
			if (Double.isNaN(pricesOfInputs.get(inputType))) {
				pricesAreNaN = true;
				break;
			}
		}

		/*
		 * analytical formula for the optimal solution of a Cobb-Douglas
		 * function under given budget restriction -> lagrange function
		 */
		for (final T inputType : this.getInputTypes()) {
			double optimalAmount = exponents.get(inputType) * budget
					/ pricesOfInputs.get(inputType);

			if (pricesAreNaN || Double.isNaN(optimalAmount)) {
				optimalAmount = 0.0;
			}

			bundleOfInputs.put(inputType, optimalAmount);
		}

		return bundleOfInputs;
	}

	/**
	 * This method implements the analytical solution for the lagrange function
	 * of an optimization problem under budget constraints and a step price
	 * function. It overwrites the general solution for convex functions for
	 * performance reasons.<br />
	 * <br />
	 * L(x_1, ..., x_n, l) = a * (x_1)^(e_1) * ... * (x_n)^(e_n) + l(p_1(x_1) *
	 * x_1 + ... + p_n(x_n) * x_n - b) <br />
	 * 1 = e_1 + e_2 + ... + e_n <br />
	 * <br />
	 * p_1(x_1) = c_x0_(x_1) + c_xMinus1_(x_1) / x_1 <br />
	 * ... <br />
	 * p_n(x_n) = c_x0_(x_n) + c_xMinus1_(x_n) / x_n <br />
	 * <br />
	 * => <br />
	 * <br />
	 * L(x_1, ..., x_n, l) = a * (x_1)^(e_1) * ... * (x_n)^(e_n) + l((c_x0_(x_1)
	 * + c_xMinus1_(x_1) / x_1) * x_1 + ... + (c_x0_(x_n) + c_xMinus1_(x_n) /
	 * x_n) * x_n - b) <br />
	 * => <br />
	 * L(x_1, ..., x_n, l) = a * (x_1)^(e_1) * ... * (x_n)^(e_n) + l((c_x0_(x_1)
	 * * x_1 + c_xMinus1_(x_1) + ... + c_x0_(x_n) * x_n + c_xMinus1_(x_n) - b) <br />
	 * <br />
	 * <br />
	 * dL(x_1, ..., x_n, l) / dx_1 = a * e_1 * (x_1)^(e_1 - 1) * ... *
	 * (x_n)^(e_n) + l * c_x0_(x_1) <br />
	 * dL(x_1, ..., x_n, l) / dx_2 = a * e_2 * (x_2)^(e_2 - 1) * ... *
	 * (x_n)^(e_n) + l * c_x0_(x_2) <br />
	 * ... <br />
	 * dL(x_1, ..., x_n, l) / dx_n = a * e_n * (x_n)^(e_n - 1) * (x_1)^(e_1) *
	 * ... + l * c_x0_(x_n) <br />
	 * <br />
	 * dL(x_1, ..., x_n, l) / dl = c_x0_(x_1) * x_1 + c_xMinus1_(x_1) + ... +
	 * c_x0_(x_n) * x_n + c_xMinus1_(x_n) - b <br />
	 * <br />
	 * <br />
	 * dL(x_1, ..., x_n, l) / dx_1 = 0 <br />
	 * ... <br />
	 * dL(x_1, ..., x_n, l) / dx_n = 0 <br />
	 * dL(x_1, ..., x_n, l) / dl = 0 <br />
	 * <br />
	 * => <br />
	 * l = a * e_1 * (x_1)^(e_1 - 1) * (x_2)^(e_2) * ... * (x_n)^(e_n) /
	 * -c_x0_(x_1) <br />
	 * l = a * (x_1)^(e_1) * e_2 * (x_2)^(e_2 - 1) * ... * (x_n)^(e_n) /
	 * -c_x0_(x_2) <br />
	 * <br />
	 * => (1) = (2)<br />
	 * e_1 * (x_1)^(e_1 - 1) * (x_2)^(e_2) * ... * (x_n)^(e_n) / -c_x0_(x_1) =
	 * (x_1)^(e_1) * e_2 * (x_2)^(e_2 - 1) * ... * (x_n)^(e_n) / -c_x0_(x_2) <br />
	 * => <br />
	 * e_1 * (x_1)^(e_1 - 1) * (x_2)^(e_2) / c_x0_(x_1) = (x_1)^(e_1) * e_2 *
	 * (x_2)^(e_2 - 1) / c_x0_(x_2) <br />
	 * <br />
	 * e_1 * (x_2)^(e_2) / (x_2)^(e_2 - 1) / c_x0_(x_1) = e_2 * (x_1)^(e_1) /
	 * (x_1)^(e_1 - 1) / c_x0_(x_2) <br />
	 * <br />
	 * e_1 * x_2 / c_x0_(x_2) = e_2 * x_1 / c_x0_(x_2) <br />
	 * <br />
	 * x_2 = [e_2 * x_1 * c_x0_(x_1)] / [c_x0_(x_2) * e_1] <br />
	 * <br />
	 * <br />
	 * b = p_1(x_1) * x_1 + ... + p_n(x_n) * x_n <br />
	 * <br />
	 * b = [c_x0_(x_1) + c_xMinus1_(x_1) / x_1] * x_1 + ... + [c_x0_(x_n) +
	 * c_xMinus1_(x_n) / x_n] * x_n <br />
	 * => <br />
	 * b = c_x0_(x_1) * x_1 + c_xMinus1_(x_1) + ... + c_x0_(x_n) * x_n +
	 * c_xMinus1_(x_n) <br />
	 * b = c_x0_(x_1) * x_1 + c_xMinus1_(x_1) + ... + c_x0_(x_n) * [e_n * x_1 *
	 * c_x0_(x_1)] / [c_x0_(x_n) * e_1] + c_xMinus1_(x_n) <br />
	 * => <br />
	 * b = c_xMinus1_(x_1) + ... + c_xMinus1_(x_n) + c_x0_(x_1) * x_1 +
	 * c_x0_(x_2) * [e_2 * x_1 * c_x0_(x_1)] / [c_x0_(x_2) * e_1] + ...
	 * c_x0_(x_n) * [e_n * x_1 * c_x0_(x_1)] / [c_x0_(x_n) * e_1] <br />
	 * => <br />
	 * b = c_xMinus1_(x_1) + ... + c_xMinus1_(x_n) + x_1 * c_x0_(x_1) + x_1 *
	 * [c_x0_(x_1) * e_2 * c_x0_(x_2)] / [c_x0_(x_2) * e_1] + ... + x_1 *
	 * [c_x0_(x_1) * e_n * c_x0_(x_n) ] / [c_x0_(x_n) * e_1] <br />
	 * => <br />
	 * b = c_xMinus1_(x_1) + ... + c_xMinus1_(x_n) + x_1 * c_x0_(x_1) * [1 + e_2
	 * / e_1 + e_2 / e_1 + ... + e_n / e_1] <br />
	 * => <br />
	 * b = c_xMinus1_(x_1) + c_xMinus1_(x_2) + ... + c_xMinus1_(x_n) + x_1 *
	 * c_x0_(x_1) * [e_1 / e_1 + e_2 / e_1 + ... + e_n / e_1] <br />
	 * e_1 + e_2 + ... + e_n = 1 => <br />
	 * <br />
	 * b = c_xMinus1_(x_1) + c_xMinus1_(x_2) + ... + c_xMinus1_(x_n) + x_1 *
	 * c_x0_(x_1) * 1 / e_1 <br />
	 * <br />
	 * x_1 = [b - c_xMinus1_(x_1) - c_xMinus1_(x_2) - ... - c_xMinus1_(x_n)] *
	 * e_1 / c_x0_(x_1) <br />
	 */
	@Override
	protected Map<T, Double> calculatePossiblyValidOutputMaximizingInputsAnalyticalWithMarketPrices(
			final Map<T, PriceFunctionConfig> priceFunctionConfigs,
			final double budget) {
		final Map<T, Double> bundleOfInputs = new LinkedHashMap<T, Double>();
		final Map<T, Double> exponents = this.getExponents();

		boolean pricesAreNaN = false;

		for (final T inputType : this.getInputTypes()) {
			if (!priceFunctionConfigs.containsKey(inputType)
					|| Double
							.isNaN(priceFunctionConfigs.get(inputType).coefficientXPower0)) {
				pricesAreNaN = true;
				break;
			}
		}

		/*
		 * analytical formula for the optimal solution of a Cobb-Douglas
		 * function under given budget restriction -> lagrange function
		 */
		double sumOfCoefficientXPowerMinus1 = 0.0;

		for (final PriceFunctionConfig priceFunctionConfig : priceFunctionConfigs
				.values()) {
			sumOfCoefficientXPowerMinus1 += priceFunctionConfig.coefficientXPowerMinus1;
		}

		for (final T inputType : this.getInputTypes()) {
			double optimalAmount = exponents.get(inputType)
					* (budget - sumOfCoefficientXPowerMinus1)
					/ priceFunctionConfigs.get(inputType).coefficientXPower0;

			if (pricesAreNaN || Double.isNaN(optimalAmount)) {
				optimalAmount = 0.0;
			}

			bundleOfInputs.put(inputType, optimalAmount);
		}

		return bundleOfInputs;
	}

	/**
	 * y = (x_1)^(e_1) * (x_2)^(e_2) * ... * (x_n)^(e_n) <br />
	 * | e_1 + e_2 + ... + e_n = 1
	 */
	@Override
	public double f(final Map<T, Double> bundleOfInputs) {
		double output = this.coefficient;

		for (final T inputType : this.getInputTypes()) {
			final double exponent = this.exponents.get(inputType);
			final double input = bundleOfInputs.get(inputType);
			output = output * Math.pow(input, exponent);
		}

		return output;
	}

	public double getCoefficient() {
		return this.coefficient;
	}

	public Map<T, Double> getExponents() {
		return this.exponents;
	}

	@Override
	public Set<T> getInputTypes() {
		return this.exponents.keySet();
	}

	/**
	 * dy/d(x_1) = e_1 * (x_1)^(e_1 - 1) * (x_2)^(e_2) * ... * (x_n)^(e_n) <br />
	 * | e_1 + e_2 + ... + e_n = 1
	 */
	@Override
	public double partialDerivative(final Map<T, Double> forBundleOfInputs,
			final T withRespectToInputType) {
		/*
		 * constant
		 */
		double constant = this.coefficient;

		for (final T inputType : this.getInputTypes()) {
			if (!inputType.equals(withRespectToInputType)) {
				final double base = forBundleOfInputs.get(inputType);
				final double exponent = this.exponents.get(inputType);
				constant = constant * Math.pow(base, exponent);
			}
		}

		/*
		 * differential factor
		 */
		final double differentialInput = forBundleOfInputs
				.get(withRespectToInputType);
		final double differentialExponent = this.exponents
				.get(withRespectToInputType) - 1.0;
		final double differentialCoefficient = this.exponents
				.get(withRespectToInputType);
		final double differentialFactor = differentialCoefficient
				* Math.pow(differentialInput, differentialExponent);

		// Java returns Double.NaN for 0 * Double.INFINITE -> return 0
		if (constant == 0.0 && Double.isInfinite(differentialFactor)) {
			return 0.0;
		}

		return constant * differentialFactor;
	}

	public void setCoefficient(final double coefficient) {
		this.coefficient = coefficient;
	}

}
