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

package compecon.nature.math;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import compecon.nature.materia.GoodType;

public class RootFunction extends Function implements IFunction {

	protected GoodType inputGoodType;

	protected double coefficient;

	public RootFunction(GoodType inputGoodType, double coefficient) {
		this.inputGoodType = inputGoodType;
		this.coefficient = coefficient;
	}

	@Override
	public Set<GoodType> getInputGoodTypes() {
		Set<GoodType> inputGoodTypes = new HashSet<GoodType>();
		inputGoodTypes.add(this.inputGoodType);
		return inputGoodTypes;
	}

	@Override
	public double f(Map<GoodType, Double> bundleOfInputGoods) {
		return this.coefficient
				* Math.pow(bundleOfInputGoods.get(this.inputGoodType), 0.5);
	}

	@Override
	public double partialDerivative(
			Map<GoodType, Double> forBundleOfInputGoods,
			GoodType withRespectToInputGoodType) {
		if (withRespectToInputGoodType == this.inputGoodType)
			return this.coefficient
					* 0.5
					* Math.pow(forBundleOfInputGoods.get(this.inputGoodType),
							-0.5);
		return 0;
	}
}
