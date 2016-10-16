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

package io.github.uwol.compecon.engine.statistics.timeseries;

import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;

import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;

public abstract class AbstractPeriodDataTimeSeriesModel {

	protected final int NUMBER_OF_DAYS = 180;

	protected TimeSeries createTimeSeries(final String title) {
		final TimeSeries timeSeries = new TimeSeries(title, Day.class);
		timeSeries.setMaximumItemAge(NUMBER_OF_DAYS);
		timeSeries.add(new Day(ApplicationContext.getInstance().getTimeSystem()
				.getCurrentDate()), 0);
		return timeSeries;
	}

	public abstract void nextPeriod();
}
