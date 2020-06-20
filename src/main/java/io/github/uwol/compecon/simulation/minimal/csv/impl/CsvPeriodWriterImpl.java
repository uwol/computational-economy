/*
Copyright (C) 2015 u.wol@wwu.de

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

package io.github.uwol.compecon.simulation.minimal.csv.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;

public abstract class CsvPeriodWriterImpl extends CsvWriterImpl {

	public CsvPeriodWriterImpl(final String csvFileName) {
		super(csvFileName);
	}

	private Date getCurrentDate() {
		final Date date = ApplicationContext.getInstance().getTimeSystem().getCurrentDate();
		return date;
	}

	private String getDateString() {
		final Date currentDate = getCurrentDate();

		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(currentDate);
	}

	@SuppressWarnings("unused")
	private String getMonthString() {
		final Date currentDate = getCurrentDate();

		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
		return dateFormat.format(currentDate);
	}

	protected String getPeriodLabel() {
		return getDateString();
	}

	@SuppressWarnings("unused")
	private String getWeekString() {
		final Date currentDate = getCurrentDate();

		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-ww");
		return dateFormat.format(currentDate);
	}

	@SuppressWarnings("unused")
	private boolean isEndOfMonth() {
		final Date currentDate = ApplicationContext.getInstance().getTimeSystem().getCurrentDate();

		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);

		final int maxDayInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		final int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

		final boolean result = dayOfMonth == maxDayInMonth;
		return result;
	}

	private boolean isEndOfWeek() {
		final Date currentDate = ApplicationContext.getInstance().getTimeSystem().getCurrentDate();

		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);

		final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		final boolean result = dayOfWeek == Calendar.SUNDAY;
		return result;
	}

	protected boolean isPeriodEnd() {
		return isEndOfWeek();
	}
}
