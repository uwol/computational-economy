/*
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

package compecon.nature.math.utility;

import java.util.LinkedHashMap;
import java.util.Map;

import compecon.nature.materia.GoodType;
import compecon.nature.math.CobbDouglasFunction;

public class CobbDouglasUtilityFunction extends ConvexUtilityFunction {

	public CobbDouglasUtilityFunction(Map<GoodType, Double> exponents,
			double coefficient) {
		super(new CobbDouglasFunction<GoodType>(exponents, coefficient));
	}

	/**
	 * This method implements the analytical solution for the lagrange function
	 * of an optimization problem under budget constraints. It overwrites the
	 * general solution for convex functions because of performance reasons.
	 */
	@Override
	public Map<GoodType, Double> calculateUtilityMaximizingInputsUnderBudgetRestriction(
			Map<GoodType, Double> pricesOfInputGoods, double budget) {
		Map<GoodType, Double> bundleOfGoods = new LinkedHashMap<GoodType, Double>();
		Map<GoodType, Double> exponents = ((CobbDouglasFunction<GoodType>) this.delegate)
				.getExponents();

		/*
		 * analytical formula for the optimal solution of a Cobb-Douglas utility
		 * function under given budget restriction -> Lagrange function
		 */
		for (GoodType goodType : this.getInputGoodTypes()) {
			double optimalAmount = exponents.get(goodType) * budget
					/ pricesOfInputGoods.get(goodType);
			if (Double.isNaN(optimalAmount))
				optimalAmount = 0.0;
			bundleOfGoods.put(goodType, optimalAmount);
		}

		return bundleOfGoods;
	}
}
