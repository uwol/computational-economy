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

import java.util.Map;
import java.util.Set;

import compecon.nature.materia.GoodType;
import compecon.nature.math.IFunction;

public abstract class UtilityFunction implements IUtilityFunction {

	protected IFunction delegate;

	protected UtilityFunction(IFunction delegate) {
		this.delegate = delegate;
	}

	@Override
	public Set<GoodType> getInputGoodTypes() {
		return this.delegate.getInputGoodTypes();
	}

	@Override
	public double calculateUtility(Map<GoodType, Double> bundleOfInputGoods) {
		return this.delegate.f(bundleOfInputGoods);
	}

	@Override
	public double calculateMarginalUtility(
			Map<GoodType, Double> bundleOfInputGoods,
			GoodType differentialInputGoodType) {
		return this.delegate.partialDerivative(bundleOfInputGoods,
				differentialInputGoodType);
	}

	protected GoodType selectInputWithHighestMarginalUtilityPerPrice(
			Map<GoodType, Double> bundleOfInputGoods,
			Map<GoodType, Double> pricesOfInputGoods) {
		return this.delegate.findLargestPartialDerivatePerPrice(
				bundleOfInputGoods, pricesOfInputGoods);
	}
}
