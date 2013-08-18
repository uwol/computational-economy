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

package compecon.engine.jmx.model.generic;

import java.util.Map.Entry;

import org.jfree.data.time.Day;

import compecon.engine.jmx.model.generic.accumulator.PeriodDataAccumulator;
import compecon.engine.jmx.model.generic.accumulator.PeriodDataAccumulatorSet;
import compecon.engine.time.TimeSystem;

public class PeriodDataAccumulatorTimeSeriesModel<T> extends
		PeriodDataTimeSeriesModel<T> {

	protected final PeriodDataAccumulatorSet<T> periodDataAccumulatorSet;

	public PeriodDataAccumulatorTimeSeriesModel(T[] initialTypes) {
		this.periodDataAccumulatorSet = new PeriodDataAccumulatorSet<T>(
				initialTypes);
		for (T type : periodDataAccumulatorSet.getPeriodDataAccumulators()
				.keySet())
			assureTimeSeries(type);
	}

	public PeriodDataAccumulatorTimeSeriesModel(T[] initialTypes,
			String titleSuffix) {
		this.periodDataAccumulatorSet = new PeriodDataAccumulatorSet<T>(
				initialTypes);
		this.titleSuffix = titleSuffix;
		for (T type : periodDataAccumulatorSet.getPeriodDataAccumulators()
				.keySet())
			assureTimeSeries(type);
	}

	public void add(T type, double amount) {
		if (this.timeSeries.containsKey(type)) {
			this.periodDataAccumulatorSet.add(type, amount);
		}
	}

	public double getAccumulatorSum() {
		double accumulatorSum = 0;
		for (PeriodDataAccumulator periodDataAccumulator : this.periodDataAccumulatorSet
				.getPeriodDataAccumulators().values()) {
			accumulatorSum += periodDataAccumulator.getAmount();
		}
		return accumulatorSum;
	}

	public void nextPeriod() {
		nextPeriod(1);
	}

	public void nextPeriod(double divisor) {
		// write amount for each type into corresponding time series
		for (Entry<T, PeriodDataAccumulator> entry : this.periodDataAccumulatorSet
				.getPeriodDataAccumulators().entrySet()) {
			if (this.timeSeries.containsKey(entry.getKey())) {
				this.timeSeries.get(entry.getKey()).addOrUpdate(
						new Day(TimeSystem.getInstance().getCurrentDate()),
						entry.getValue().getAmount() / divisor);
			}
		}

		this.periodDataAccumulatorSet.reset();
	}
}
