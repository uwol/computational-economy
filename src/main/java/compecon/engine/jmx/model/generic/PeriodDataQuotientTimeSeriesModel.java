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

import org.jfree.data.time.Day;

import compecon.engine.time.TimeSystem;

public class PeriodDataQuotientTimeSeriesModel<T> extends
		PeriodDataAbstractTimeSeriesModel<T> {

	protected final PeriodDataAccumulatorSet<T> periodDataDividendAccumulatorSet;

	protected final PeriodDataAccumulatorSet<T> periodDataDivisorAccumulatorSet;

	public PeriodDataQuotientTimeSeriesModel(T[] initialTypes) {
		this.periodDataDividendAccumulatorSet = new PeriodDataAccumulatorSet<T>(
				initialTypes);
		this.periodDataDivisorAccumulatorSet = new PeriodDataAccumulatorSet<T>(
				initialTypes);
		for (T type : initialTypes)
			assureTimeSeries(type);
	}

	public PeriodDataQuotientTimeSeriesModel(T[] initialTypes,
			String titleSuffix) {
		this.titleSuffix = titleSuffix;
		this.periodDataDividendAccumulatorSet = new PeriodDataAccumulatorSet<T>(
				initialTypes);
		this.periodDataDivisorAccumulatorSet = new PeriodDataAccumulatorSet<T>(
				initialTypes);
		for (T type : initialTypes)
			assureTimeSeries(type);
	}

	public void add(T type, double dividendAamount, double divisorAamount) {
		if (this.timeSeries.containsKey(type)) {
			this.periodDataDividendAccumulatorSet.add(type, dividendAamount);
			this.periodDataDivisorAccumulatorSet.add(type, divisorAamount);
		}
	}

	public void nextPeriod() {
		// write amount for each type into corresponding time series
		for (T key : this.periodDataDividendAccumulatorSet
				.getPeriodDataAccumulators().keySet()) {
			PeriodDataAccumulator dividend = this.periodDataDividendAccumulatorSet
					.getPeriodDataAccumulators().get(key);
			PeriodDataAccumulator divisor = this.periodDataDivisorAccumulatorSet
					.getPeriodDataAccumulators().get(key);

			if (this.timeSeries.containsKey(key)) {
				this.timeSeries.get(key).addOrUpdate(
						new Day(TimeSystem.getInstance().getCurrentDate()),
						dividend.getAmount() / divisor.getAmount());
			}
		}

		this.periodDataDividendAccumulatorSet.reset();
		this.periodDataDivisorAccumulatorSet.reset();
	}

}
