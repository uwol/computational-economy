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

package compecon.engine.jmx.model;

import java.util.HashMap;
import java.util.Map;

public class PeriodDataAccumulatorSet<T> {
	final Map<T, PeriodDataAccumulator> periodDataAccumulators = new HashMap<T, PeriodDataAccumulator>();

	public PeriodDataAccumulatorSet() {

	}

	public PeriodDataAccumulatorSet(T[] initialTypes) {
		for (T type : initialTypes) {
			this.periodDataAccumulators.put(type, new PeriodDataAccumulator());
		}
	}

	public void add(T type, double amount) {
		if (!this.periodDataAccumulators.containsKey(type)) {
			this.periodDataAccumulators.put(type, new PeriodDataAccumulator());
		}

		this.periodDataAccumulators.get(type).add(amount);
	}

	public Map<T, PeriodDataAccumulator> getPeriodDataAccumulators() {
		return this.periodDataAccumulators;
	}

	/**
	 * Reset values to zero
	 */
	public void reset() {
		for (PeriodDataAccumulator periodDataAccumulator : this.periodDataAccumulators
				.values())
			periodDataAccumulator.reset();
	}
}
