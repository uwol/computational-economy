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
import java.util.Set;

import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;

import compecon.engine.time.TimeSystem;

public class TimeSeriesModel<T> {

	protected final int NUMBER_OF_DAYS = 180;

	protected final Map<T, TimeSeries> timeSeries = new HashMap<T, TimeSeries>();

	protected String titleSuffix;

	public TimeSeriesModel(T[] initialTypes) {
		for (T type : initialTypes)
			this.assertTimeSeries(type);
	}

	public TimeSeriesModel(T[] initialTypes, String titleSuffix) {
		for (T type : initialTypes)
			this.assertTimeSeries(type);
		this.titleSuffix = titleSuffix;
	}

	public void add(T type, double amount) {
		if (this.timeSeries.containsKey(type))
			this.timeSeries.get(type).addOrUpdate(
					new Day(TimeSystem.getInstance().getCurrentDate()), amount);
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

	protected TimeSeries createTimeSeries(T type) {
		String title = type.toString();
		if (titleSuffix != null)
			title += titleSuffix;

		TimeSeries timeSeries = new TimeSeries(title, Day.class);
		timeSeries.setMaximumItemAge(this.NUMBER_OF_DAYS);
		timeSeries.add(new Day(TimeSystem.getInstance().getCurrentDate()), 0);
		return timeSeries;
	}
}
