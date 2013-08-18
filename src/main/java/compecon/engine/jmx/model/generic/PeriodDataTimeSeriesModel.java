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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;

import compecon.engine.time.TimeSystem;

public class PeriodDataTimeSeriesModel<T> {

	protected final int NUMBER_OF_DAYS = 180;

	protected final Map<T, TimeSeries> timeSeries = new HashMap<T, TimeSeries>();

	protected String titleSuffix;

	public Set<T> getTypes() {
		return this.timeSeries.keySet();
	}

	public TimeSeries getTimeSeries(T type) {
		assureTimeSeries(type);
		return this.timeSeries.get(type);
	}

	protected void assureTimeSeries(T type) {
		if (!this.timeSeries.containsKey(type)) {
			TimeSeries amountTimeSeries = createTimeSeries(type);
			this.timeSeries.put(type, amountTimeSeries);
		}
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
