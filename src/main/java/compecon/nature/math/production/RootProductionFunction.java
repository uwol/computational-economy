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

package compecon.nature.math.production;

import compecon.nature.materia.GoodType;
import compecon.nature.math.RootFunction;

public class RootProductionFunction extends ConvexProductionFunction {
	public RootProductionFunction(GoodType inputGoodType, double coefficient) {
		super(new RootFunction(inputGoodType, coefficient));
	}

	@Override
	public double getProductivity() {
		return ((RootFunction) this.delegate).getCoefficient();
	}

	@Override
	public void setProductivity(double productivity) {
		((RootFunction) this.delegate).setCoefficient(productivity);
	}
}
