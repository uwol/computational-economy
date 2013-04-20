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

package compecon.nature.production;

import compecon.nature.materia.GoodType;

public class LinearProductionFunction implements IProductionFunction,
		Comparable<LinearProductionFunction> {

	protected final double MAX_OUTPUT_PER_PRODUCTION_CYCLE;

	protected final double MAX_LABOURHOUR_INPUT_PER_PRODUCTION_CYCLE = 24;

	public LinearProductionFunction(double maxOutputPerProductionCycle) {
		MAX_OUTPUT_PER_PRODUCTION_CYCLE = maxOutputPerProductionCycle;
	}

	public double calculateMaxOutputPerProductionCycle() {
		return MAX_OUTPUT_PER_PRODUCTION_CYCLE;
	}

	public double calculateMaxLabourHourInputPerProductionCycle() {
		return MAX_LABOURHOUR_INPUT_PER_PRODUCTION_CYCLE;
	}

	public double calculateProductionCoefficient(GoodType goodType) {
		if (GoodType.LABOURHOUR.equals(goodType))
			return MAX_OUTPUT_PER_PRODUCTION_CYCLE
					/ MAX_LABOURHOUR_INPUT_PER_PRODUCTION_CYCLE;
		return 0;
	}

	public double calculateOutput(double inputNumberOfLabourHours) {
		return Math.min(MAX_OUTPUT_PER_PRODUCTION_CYCLE,
				calculateProductionCoefficient(GoodType.LABOURHOUR)
						* inputNumberOfLabourHours);
	}

	public double calculateMarginalOutput(double inputNumberOfLabourHours) {
		if (inputNumberOfLabourHours > MAX_LABOURHOUR_INPUT_PER_PRODUCTION_CYCLE)
			return 0;
		return calculateProductionCoefficient(GoodType.LABOURHOUR);
	}

	@Override
	public int compareTo(LinearProductionFunction linearProductionFunction) {
		if (this == linearProductionFunction)
			return 0;
		if (this.calculateProductionCoefficient(GoodType.LABOURHOUR) > linearProductionFunction
				.calculateProductionCoefficient(GoodType.LABOURHOUR))
			return -1;
		if (this.calculateProductionCoefficient(GoodType.LABOURHOUR) < linearProductionFunction
				.calculateProductionCoefficient(GoodType.LABOURHOUR))
			return 1;
		return super.hashCode() - linearProductionFunction.hashCode();
	}
}
