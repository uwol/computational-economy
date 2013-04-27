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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import compecon.engine.util.MathUtil;
import compecon.nature.materia.GoodType;

/**
 * U = (g_1)^(e_1) * (g_2)^(e_2) * ... * (g_n)^(e_n) | g_1 + g_2 + ... + g_n = 1
 */
public class CobbDouglasFunction implements IFunction {

	protected final Map<GoodType, Double> exponents;

	@Override
	public Set<GoodType> getInputGoodTypes() {
		return this.exponents.keySet();
	}

	public CobbDouglasFunction(Map<GoodType, Double> exponents) {
		/*
		 * constraint on exponents
		 */
		double sumOfExponents = 0.0;
		for (Double exponent : exponents.values()) {
			if (exponent == 1)
				throw new RuntimeException("exponent is 1");
			sumOfExponents += exponent;
		}
		if (!MathUtil.equal(sumOfExponents, 1))
			throw new RuntimeException("sum of exponents not 1");

		this.exponents = exponents;
	}

	@Override
	public double f(Map<GoodType, Double> bundleOfGoods) {
		double utility = 1.0;
		for (Entry<GoodType, Double> entry : this.exponents.entrySet()) {
			GoodType goodType = entry.getKey();
			double exponent = entry.getValue();
			double base = bundleOfGoods.containsKey(goodType) ? bundleOfGoods
					.get(goodType) : 0.0;
			utility = utility * Math.pow(base, exponent);
		}
		return utility;
	}

	@Override
	public double partialDerivative(Map<GoodType, Double> forBundleOfGoods,
			GoodType withRespectToGoodType) {
		/*
		 * Constant
		 */
		double constant = 1;
		for (Entry<GoodType, Double> entry : this.exponents.entrySet()) {
			GoodType goodType = entry.getKey();
			double exponent = entry.getValue();
			double base = forBundleOfGoods.containsKey(goodType) ? forBundleOfGoods
					.get(goodType) : 0;
			if (goodType != withRespectToGoodType) {
				constant = constant * Math.pow(base, exponent);
			}
		}

		/*
		 * Differential factor
		 */
		double differentialBase = forBundleOfGoods
				.containsKey(withRespectToGoodType) ? forBundleOfGoods
				.get(withRespectToGoodType) : 0;
		double differentialExponent = this.exponents
				.containsKey(withRespectToGoodType) ? this.exponents
				.get(withRespectToGoodType) - 1 : 0;
		double differentialCoefficient = this.exponents
				.containsKey(withRespectToGoodType) ? this.exponents
				.get(withRespectToGoodType) : 0;
		double differentialFactor = differentialCoefficient
				* Math.pow(differentialBase, differentialExponent);

		return constant * differentialFactor;
	}

	public Map<GoodType, Double> getExponents() {
		return this.exponents;
	}
}
