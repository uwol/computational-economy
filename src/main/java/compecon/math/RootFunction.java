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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RootFunction<T> extends ConvexFunction<T> implements IFunction<T> {

	protected T inputType;

	protected double coefficient;

	public RootFunction(T inputType, double coefficient) {
		super(false);

		this.inputType = inputType;
		this.coefficient = coefficient;
	}

	@Override
	public Set<T> getInputTypes() {
		Set<T> inputTypes = new HashSet<T>();
		inputTypes.add(this.inputType);
		return inputTypes;
	}

	@Override
	public double f(Map<T, Double> bundleOfInputs) {
		return this.coefficient
				* Math.pow(bundleOfInputs.get(this.inputType), 0.5);
	}

	@Override
	public double partialDerivative(Map<T, Double> forBundleOfInputs,
			T withRespectToInputType) {
		if (withRespectToInputType == this.inputType)
			return this.coefficient * 0.5
					* Math.pow(forBundleOfInputs.get(this.inputType), -0.5);
		return 0;
	}

	public double getCoefficient() {
		return this.coefficient;
	}

	public void setCoefficient(double coefficient) {
		this.coefficient = coefficient;
	}
}
