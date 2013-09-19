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
import java.util.Set;

import compecon.math.price.IPriceFunction;

public interface IFunction<T> {

	public Map<T, Double> calculateOutputMaximizingInputs(
			final Map<T, IPriceFunction> priceFunctionsOfInputTypes,
			final double budget);

	public Map<T, Double> calculateOutputMaximizingInputsByRangeScan(
			final Map<T, IPriceFunction> priceFunctionsOfInputTypes,
			final double budget);

	public Set<T> getInputTypes();

	public boolean getNeedsAllInputFactorsNonZeroForPartialDerivate();

	public double f(Map<T, Double> bundleOfInputs);

	public double partialDerivative(Map<T, Double> forBundleOfInputs,
			T withRespectToInput);

	public Map<T, Double> partialDerivatives(Map<T, Double> forBundleOfInputs);

	public T findLargestPartialDerivate(Map<T, Double> forBundleOfInputs);

	public T findHighestPartialDerivatePerPrice(Map<T, Double> bundleOfInputs,
			Map<T, IPriceFunction> priceFunctionsOfInputTypes);
}
