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

package io.github.uwol.compecon.math;

import java.util.Map;
import java.util.Set;

import io.github.uwol.compecon.math.price.PriceFunction;

public interface Function<T> {

	public Map<T, Double> calculateOutputMaximizingInputs(final Map<T, PriceFunction> priceFunctionsOfInputTypes,
			final double budget);

	public Map<T, Double> calculateOutputMaximizingInputsByRangeScan(
			final Map<T, PriceFunction> priceFunctionsOfInputTypes, final double budget);

	public double f(final Map<T, Double> bundleOfInputs);

	/**
	 * @param bundleOfInputs             has to contain all elements from
	 *                                   {@link #getInputTypes()} as keys.
	 * @param priceFunctionsOfInputTypes has to contain all elements from
	 *                                   {@link #getInputTypes()} as keys.
	 * @param inventory                  null allowed.
	 * @return null, if markets are sold out.
	 */
	public T findHighestPartialDerivatePerPrice(final Map<T, Double> bundleOfInputs,
			final Map<T, PriceFunction> priceFunctionsOfInputTypes, final Map<T, Double> inventory);

	public T findLargestPartialDerivate(final Map<T, Double> forBundleOfInputs);

	public Set<T> getInputTypes();

	public boolean getNeedsAllInputFactorsNonZeroForPartialDerivate();

	public double partialDerivative(final Map<T, Double> forBundleOfInputs, final T withRespectToInput);

	public Map<T, Double> partialDerivatives(final Map<T, Double> forBundleOfInputs);
}
