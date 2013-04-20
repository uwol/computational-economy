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

import java.util.SortedSet;
import java.util.TreeSet;

public class CompositeProductionFunction implements IProductionFunction {
	// production functions ordered by productivity; highest first
	protected final SortedSet<LinearProductionFunction> productionFunctions = new TreeSet<LinearProductionFunction>();

	public void addProductionFunction(
			LinearProductionFunction productionFunction) {
		this.productionFunctions.add(productionFunction);
	}

	@Override
	public double calculateMaxOutputPerProductionCycle() {
		double maxOutputPerProductionCycle = 0;
		for (LinearProductionFunction productionFunction : this.productionFunctions) {
			maxOutputPerProductionCycle += productionFunction
					.calculateMaxOutputPerProductionCycle();
		}
		return maxOutputPerProductionCycle;
	}

	@Override
	public double calculateMaxLabourHourInputPerProductionCycle() {
		double maxLabourHourInputPerProductionCycle = 0;
		for (LinearProductionFunction productionFunction : this.productionFunctions)
			maxLabourHourInputPerProductionCycle += productionFunction
					.calculateMaxLabourHourInputPerProductionCycle();
		return maxLabourHourInputPerProductionCycle;
	}

	@Override
	public double calculateOutput(double inputNumberOfLabourHours) {
		double output = 0;
		double remainingLabourHours = inputNumberOfLabourHours;
		for (LinearProductionFunction productionFunction : this.productionFunctions) {
			double maxLabourHourInput = productionFunction
					.calculateMaxLabourHourInputPerProductionCycle();
			double usedLabourHours = Math.min(remainingLabourHours,
					maxLabourHourInput);
			output += productionFunction.calculateOutput(usedLabourHours);
			remainingLabourHours -= usedLabourHours;
		}
		return output;
	}

	@Override
	public double calculateMarginalOutput(double inputNumberOfLabourHours) {
		double remainingLabourHours = inputNumberOfLabourHours;
		for (LinearProductionFunction productionFunction : this.productionFunctions) {
			double maxLabourHourInputPerProductionCycle = productionFunction
					.calculateMaxLabourHourInputPerProductionCycle();
			if (remainingLabourHours > maxLabourHourInputPerProductionCycle)
				remainingLabourHours -= maxLabourHourInputPerProductionCycle;
			else
				return productionFunction
						.calculateMarginalOutput(remainingLabourHours);
		}
		return 0;
	}
}
