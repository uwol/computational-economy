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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import compecon.math.Function;

public class RootFunctionImpl<T> extends ConvexFunctionImpl<T> implements
		Function<T> {

	protected double coefficient;

	protected T inputType;

	public RootFunctionImpl(final T inputType, final double coefficient) {
		super(false);

		this.inputType = inputType;
		this.coefficient = coefficient;
	}

	@Override
	public double f(final Map<T, Double> bundleOfInputs) {
		return this.coefficient
				* Math.pow(bundleOfInputs.get(this.inputType), 0.5);
	}

	public double getCoefficient() {
		return this.coefficient;
	}

	@Override
	public Set<T> getInputTypes() {
		final Set<T> inputTypes = new HashSet<T>();
		inputTypes.add(this.inputType);
		return inputTypes;
	}

	@Override
	public double partialDerivative(final Map<T, Double> forBundleOfInputs,
			final T withRespectToInputType) {
		if (this.inputType.equals(withRespectToInputType)) {
			return this.coefficient * 0.5
					* Math.pow(forBundleOfInputs.get(this.inputType), -0.5);
		}
		return 0.0;
	}

	public void setCoefficient(final double coefficient) {
		this.coefficient = coefficient;
	}
}
