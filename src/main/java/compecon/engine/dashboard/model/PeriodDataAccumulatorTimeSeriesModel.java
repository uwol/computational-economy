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

package compecon.engine.dashboard.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;

import compecon.engine.time.TimeSystem;

public class PeriodDataAccumulatorTimeSeriesModel<T> {

	protected final int NUMBER_OF_DAYS = 180;

	protected final PeriodDataAccumulatorSet<T> periodDataAccumulatorSet;

	protected final Map<T, TimeSeries> timeSeries = new HashMap<T, TimeSeries>();

	protected String titleSuffix;

	public PeriodDataAccumulatorTimeSeriesModel(T[] initialTypes) {
		this.periodDataAccumulatorSet = new PeriodDataAccumulatorSet<T>(
				initialTypes);
		for (T type : initialTypes)
			assertTimeSeries(type);
	}

	public PeriodDataAccumulatorTimeSeriesModel(T[] initialTypes,
			String titleSuffix) {
		this.titleSuffix = titleSuffix;
		this.periodDataAccumulatorSet = new PeriodDataAccumulatorSet<T>(
				initialTypes);
		for (T type : initialTypes)
			assertTimeSeries(type);
	}

	public void add(T type, double amount) {
		if (this.timeSeries.containsKey(type))
			this.periodDataAccumulatorSet.add(type, amount);
	}

	public Set<T> getTypes() {
		return this.timeSeries.keySet();
	}

	public TimeSeries getTimeSeries(T type) {
		return this.timeSeries.get(type);
	}

	private void assertTimeSeries(T type) {
		if (!this.timeSeries.containsKey(type)) {
			TimeSeries amountTimeSeries = createTimeSeries(type);
			this.timeSeries.put(type, amountTimeSeries);
		}
	}

	public void nextPeriod() {
		// write amount for each good type into corresponding time
		// series
		for (Entry<T, PeriodDataAccumulator> entry : this.periodDataAccumulatorSet
				.getPeriodDataAccumulators().entrySet()) {
			if (this.timeSeries.containsKey(entry.getKey())) {
				this.timeSeries.get(entry.getKey()).addOrUpdate(
						new Day(TimeSystem.getInstance().getCurrentDate()),
						entry.getValue().getAmount());
			}
		}

		this.periodDataAccumulatorSet.reset();
	}

	protected TimeSeries createTimeSeries(T type) {
		String title = type.toString();
		if (this.titleSuffix != null)
			title += this.titleSuffix;

		TimeSeries timeSeries = new TimeSeries(title, Day.class);
		timeSeries.setMaximumItemAge(this.NUMBER_OF_DAYS);
		timeSeries.add(new Day(TimeSystem.getInstance().getCurrentDate()), 0);
		return timeSeries;
	}
}
