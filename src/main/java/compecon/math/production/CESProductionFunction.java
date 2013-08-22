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

import compecon.materia.GoodType;
import compecon.math.CESFunction;

public class CESProductionFunction extends ConvexProductionFunction {
	public CESProductionFunction(double factorProductivity,
			Map<GoodType, Double> coefficients, double substitutionFactor,
			double homogenityFactor) {
		super(new CESFunction<GoodType>(factorProductivity, coefficients,
				substitutionFactor, homogenityFactor));
	}

	@Override
	public double getProductivity() {
		return ((CESFunction<GoodType>) this.delegate).getMainCoefficient();
	}

	@Override
	public void setProductivity(double productivity) {
		((CESFunction<GoodType>) this.delegate)
				.setMainCoefficient(productivity);
	}
}
