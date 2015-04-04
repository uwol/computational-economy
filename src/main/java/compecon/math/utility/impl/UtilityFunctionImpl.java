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

package compecon.math.utility.impl;

import java.util.Map;
import java.util.Set;

import compecon.economy.materia.GoodType;
import compecon.math.Function;
import compecon.math.price.PriceFunction;
import compecon.math.utility.UtilityFunction;

public abstract class UtilityFunctionImpl implements UtilityFunction {

	protected Function<GoodType> delegate;

	protected UtilityFunctionImpl(final Function<GoodType> delegate) {
		this.delegate = delegate;
	}

	@Override
	public double calculateMarginalUtility(
			final Map<GoodType, Double> bundleOfInputGoods,
			final GoodType differentialInputGoodType) {
		return delegate.partialDerivative(bundleOfInputGoods,
				differentialInputGoodType);
	}

	@Override
	public double calculateUtility(
			final Map<GoodType, Double> bundleOfInputGoods) {
		return delegate.f(bundleOfInputGoods);
	}

	@Override
	public Map<GoodType, Double> calculateUtilityMaximizingInputs(
			final Map<GoodType, PriceFunction> priceFunctionsOfInputGoods,
			final double budget) {
		return delegate.calculateOutputMaximizingInputs(
				priceFunctionsOfInputGoods, budget);
	}

	@Override
	public Set<GoodType> getInputGoodTypes() {
		return delegate.getInputTypes();
	}

	protected GoodType selectInputWithHighestMarginalUtilityPerPrice(
			final Map<GoodType, Double> bundleOfInputGoods,
			final Map<GoodType, PriceFunction> priceFunctionsOfInputGoods) {
		return delegate.findHighestPartialDerivatePerPrice(bundleOfInputGoods,
				priceFunctionsOfInputGoods, null);
	}
}
