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

package compecon.engine.jmx.model.generic;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;

import compecon.engine.time.TimeSystem;

public class PeriodDataPercentageTimeSeriesModel<T, C> {

	protected final int NUMBER_OF_DAYS = 180;

	protected final C[] categories;

	protected final Map<T, PeriodDataAccumulatorSet<C>> periodDataAccumulatorSetsPerType = new HashMap<T, PeriodDataAccumulatorSet<C>>();

	protected final Map<T, Map<C, TimeSeries>> timeSeries = new HashMap<T, Map<C, TimeSeries>>();

	public PeriodDataPercentageTimeSeriesModel(T[] initialTypes,
			C[] initialCategories) {
		this.categories = initialCategories;
		for (T type : initialTypes) {
			PeriodDataAccumulatorSet<C> periodDataAccumulatorSet = new PeriodDataAccumulatorSet<C>(
					initialCategories);
			this.periodDataAccumulatorSetsPerType.put(type,
					periodDataAccumulatorSet);
			for (C category : initialCategories) {
				assureTimeSeries(type, category);
			}
		}
	}

	protected void assureTimeSeries(T type, C category) {
		if (!this.timeSeries.containsKey(type)) {
			this.timeSeries.put(type, new HashMap<C, TimeSeries>());
		}

		if (!this.timeSeries.get(type).containsKey(category)) {
			TimeSeries amountTimeSeries = createTimeSeries(type, category);
			this.timeSeries.get(type).put(category, amountTimeSeries);
		}
	}

	protected TimeSeries createTimeSeries(T type, C category) {
		String title = type.toString() + "-" + category.toString();
		TimeSeries timeSeries = new TimeSeries(title, Day.class);
		timeSeries.setMaximumItemAge(this.NUMBER_OF_DAYS);
		timeSeries.add(new Day(TimeSystem.getInstance().getCurrentDate()), 0);
		return timeSeries;
	}

	public void add(T type, C category, double amount) {
		if (this.timeSeries.containsKey(type)
				&& this.timeSeries.get(type).containsKey(category)) {
			this.periodDataAccumulatorSetsPerType.get(type).add(category,
					amount);
		}
	}

	public TimeSeries getTimeSeries(T type, C category) {
		return this.timeSeries.get(type).get(category);
	}

	public Set<T> getTypes() {
		return this.timeSeries.keySet();
	}

	public C[] getCategories() {
		return this.categories;
	}

	public void nextPeriod() {
		// write amount for each type into corresponding time series
		for (Entry<T, PeriodDataAccumulatorSet<C>> entry : this.periodDataAccumulatorSetsPerType
				.entrySet()) {
			T type = entry.getKey();
			PeriodDataAccumulatorSet<C> periodDataAccumulatorSet = entry
					.getValue();

			double categoriesSumForType = 0;
			for (PeriodDataAccumulator periodDataAccumulator : periodDataAccumulatorSet
					.getPeriodDataAccumulators().values()) {
				categoriesSumForType += periodDataAccumulator.getAmount();
			}

			for (Entry<C, PeriodDataAccumulator> entry2 : periodDataAccumulatorSet
					.getPeriodDataAccumulators().entrySet()) {
				C category = entry2.getKey();
				PeriodDataAccumulator periodDataAccumulator = entry2.getValue();

				if (this.timeSeries.containsKey(type)) {
					if (this.timeSeries.get(type).containsKey(category)) {
						this.timeSeries
								.get(type)
								.get(category)
								.addOrUpdate(
										new Day(TimeSystem.getInstance()
												.getCurrentDate()),
										periodDataAccumulator.getAmount()
												/ categoriesSumForType);
					}
				}
			}

			periodDataAccumulatorSet.reset();
		}
	}
}
