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

package compecon.math.production;

import java.util.Map;
import java.util.Set;

import compecon.economy.materia.GoodType;
import compecon.math.price.PriceFunction;

public interface ProductionFunction {

	public double calculateMarginalOutput(
			final Map<GoodType, Double> bundleOfProductionFactors,
			final GoodType differentialGoodType);

	public double calculateOutput(
			final Map<GoodType, Double> bundleOfProductionFactors);

	/**
	 * returns the optimal production plan for this production function under a
	 * given price of produced good type, prices, capital goods, budget, maximal
	 * output and minimal margin.
	 *
	 * @param capital
	 *            Capital goods; have to be durable good types.
	 * @return Key: Input good type, Value: Amount of good type input
	 */
	public Map<GoodType, Double> calculateProfitMaximizingProductionFactors(
			final double priceOfProducedGoodType,
			final Map<GoodType, PriceFunction> priceFunctionsOfInputGoods,
			final Map<GoodType, Double> capital, final double budget,
			final double maxOutput, final double margin);

	public Set<GoodType> getInputGoodTypes();

	public double getProductivity();

	public void setProductivity(final double productivity);
}
