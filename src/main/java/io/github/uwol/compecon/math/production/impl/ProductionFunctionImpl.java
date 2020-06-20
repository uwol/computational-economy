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

package io.github.uwol.compecon.math.production.impl;

import java.util.Map;
import java.util.Set;

import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.math.Function;
import io.github.uwol.compecon.math.price.PriceFunction;
import io.github.uwol.compecon.math.production.ProductionFunction;

public abstract class ProductionFunctionImpl implements ProductionFunction {

	protected Function<GoodType> delegate;

	protected ProductionFunctionImpl(final Function<GoodType> delegate) {
		this.delegate = delegate;
	}

	@Override
	public double calculateMarginalOutput(final Map<GoodType, Double> bundleOfProductionFactors,
			final GoodType differentialGoodType) {
		return delegate.partialDerivative(bundleOfProductionFactors, differentialGoodType);
	}

	@Override
	public double calculateOutput(final Map<GoodType, Double> bundleOfProductionFactors) {
		return delegate.f(bundleOfProductionFactors);
	}

	@Override
	public Set<GoodType> getInputGoodTypes() {
		return delegate.getInputTypes();
	}

	/**
	 * @return null, if markets are sold out
	 */
	protected GoodType selectProductionFactorWithHighestMarginalOutputPerPrice(
			final Map<GoodType, Double> bundleOfInputGoods,
			final Map<GoodType, PriceFunction> priceFunctionsOfInputGoods, final Map<GoodType, Double> capital) {
		return delegate.findHighestPartialDerivatePerPrice(bundleOfInputGoods, priceFunctionsOfInputGoods, capital);
	}
}
