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
import java.util.Set;

import org.jfree.data.time.TimeSeries;

public class PeriodDataPercentageTimeSeriesModel<T, C> {

	protected final C[] categories;

	protected final Map<T, PeriodDataAccumulatorTimeSeriesModel<C>> periodDataAccumulatorTimeSeriesModels = new HashMap<T, PeriodDataAccumulatorTimeSeriesModel<C>>();

	public PeriodDataPercentageTimeSeriesModel(T[] initialTypes,
			C[] initialCategories) {
		this.categories = initialCategories;
		for (T type : initialTypes) {
			PeriodDataAccumulatorTimeSeriesModel<C> periodDataAccumulatorTimeSeriesModel = new PeriodDataAccumulatorTimeSeriesModel<C>(
					initialCategories, "-" + type.toString());
			this.periodDataAccumulatorTimeSeriesModels.put(type,
					periodDataAccumulatorTimeSeriesModel);
		}
	}

	public void add(T type, C category, double amount) {
		this.periodDataAccumulatorTimeSeriesModels.get(type).add(category,
				amount);
	}

	public TimeSeries getTimeSeries(T type, C category) {
		return this.periodDataAccumulatorTimeSeriesModels.get(type)
				.getTimeSeries(category);
	}

	public Set<T> getTypes() {
		return this.periodDataAccumulatorTimeSeriesModels.keySet();
	}

	public C[] getCategories() {
		return this.categories;
	}

	public void nextPeriod() {
		// write amount for each type into corresponding time series
		for (PeriodDataAccumulatorTimeSeriesModel<C> periodDataAccumulatorTimeSeriesModel : this.periodDataAccumulatorTimeSeriesModels
				.values()) {
			double categorySum = periodDataAccumulatorTimeSeriesModel
					.getAccumulatorSum();
			periodDataAccumulatorTimeSeriesModel.nextPeriod(categorySum);
		}
	}
}
