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

package io.github.uwol.compecon.math.utility;

import java.util.Map;
import java.util.Set;

import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.math.price.PriceFunction;

public interface UtilityFunction {

	public double calculateMarginalUtility(
			Map<GoodType, Double> bundleOfInputGoods,
			GoodType differentialInputGoodType);

	public double calculateUtility(Map<GoodType, Double> bundleOfInputGoods);

	/**
	 * This method implements the analytical solution for the lagrange function
	 * of an optimization problem under budget constraints. It overwrites the
	 * general solution for convex functions because of performance reasons.
	 */
	public Map<GoodType, Double> calculateUtilityMaximizingInputs(
			Map<GoodType, PriceFunction> priceFunctionsOfInputGoods,
			double budget);

	public Set<GoodType> getInputGoodTypes();
}
