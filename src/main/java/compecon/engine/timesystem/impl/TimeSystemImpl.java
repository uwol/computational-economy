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

package compecon.engine.timesystem.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.timesystem.TimeSystem;
import compecon.engine.timesystem.TimeSystemEvent;
import compecon.engine.util.HibernateUtil;

/**
 * Agents register their actions as events in the time system (observer
 * pattern).
 */
public class TimeSystemImpl implements TimeSystem {

	private final int startYear;

	private int dayNumber = 0;

	private Random random = new Random();

	private GregorianCalendar gregorianCalendar = new GregorianCalendar();

	private SimpleDateFormat dayFormat = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm");

	private Map<Integer, YearImpl> years = new HashMap<Integer, YearImpl>();

	private List<TimeSystemEvent> externalEvents = new ArrayList<TimeSystemEvent>();

	public TimeSystemImpl(int year) {
		gregorianCalendar = new GregorianCalendar(year,
				MonthType.JANUARY.getMonthNumber(),
				DayType.DAY_01.getDayNumber());
		startYear = year;
	}

	public Date getCurrentDate() {
		return this.gregorianCalendar.getTime();
	}

	public int getCurrentYear() {
		return this.gregorianCalendar.get(GregorianCalendar.YEAR);
	}

	public int getCurrentMonthNumberInYear() {
		return this.gregorianCalendar.get(GregorianCalendar.MONTH) + 1;
	}

	public int getCurrentDayNumberInMonth() {
		return this.gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH);
	}

	public int getStartYear() {
		return this.startYear;
	}

	public MonthType getCurrentMonthType() {
		return MonthType.getMonthType(this.gregorianCalendar
				.get(GregorianCalendar.MONTH));
	}

	public DayType getCurrentDayType() {
		return DayType.getDayType(this.gregorianCalendar
				.get(GregorianCalendar.DAY_OF_MONTH));
	}

	public HourType getCurrentHourType() {
		return HourType.getHourType(this.gregorianCalendar
				.get(GregorianCalendar.HOUR_OF_DAY));
	}

	public HourType suggestRandomHourType() {
		// HourType.HOUR_23 and HourType.HOUR_00 are reserved for balance sheet
		// publication, interest calculation, ...
		return this.suggestRandomHourType(HourType.HOUR_01, HourType.HOUR_22);
	}

	public HourType suggestRandomHourType(final HourType minHourType,
			final HourType maxHourType) {
		int randomNumber = this.random.nextInt(maxHourType.getHourNumber() + 1
				- minHourType.getHourNumber());
		return HourType.getHourType(minHourType.getHourNumber() + randomNumber);
	}

	public boolean isInitializationPhase() {
		return this.dayNumber < ApplicationContext.getInstance()
				.getConfiguration().timeSystemConfig
				.getInitializationPhaseInDays();
	}

	/*
	 * methods for adding ITimeSystemEvents
	 */

	/**
	 * @param year
	 *            -1 for every year
	 */
	public void addEvent(final TimeSystemEvent event, final int year,
			final MonthType monthType, final DayType dayType,
			final HourType hourType) {
		if (!this.years.containsKey(year))
			this.years.put(year, new YearImpl());
		this.years.get(year).addEvent(event, monthType, dayType, hourType);
	}

	/**
	 * @param year
	 *            -1 for every year
	 */
	public void addEventEvery(final TimeSystemEvent event, final int year,
			final MonthType monthType, DayType dayType,
			final HourType excepthourType) {
		assert (excepthourType != null);

		if (!this.years.containsKey(year))
			this.years.put(year, new YearImpl());
		for (HourType hourType : HourType.values()) {
			if (!HourType.EVERY.equals(hourType)
					&& !excepthourType.equals(hourType)) {
				this.years.get(year).addEvent(event, monthType, dayType,
						hourType);
			}
		}
	}

	public void addEventForEveryDay(final TimeSystemEvent event) {
		this.addEvent(event, -1, MonthType.EVERY, DayType.EVERY,
				HourType.HOUR_12);
	}

	public void addEventForEveryMorning(final TimeSystemEvent event) {
		this.addEvent(event, -1, MonthType.EVERY, DayType.EVERY,
				HourType.HOUR_07);
	}

	public void addEventForEveryEvening(final TimeSystemEvent event) {
		this.addEvent(event, -1, MonthType.EVERY, DayType.EVERY,
				HourType.HOUR_18);
	}

	public void addEventForEveryHour(final TimeSystemEvent event) {
		this.addEvent(event, -1, MonthType.EVERY, DayType.EVERY, HourType.EVERY);
	}

	/*
	 * methods for events induced by the dashboard
	 */

	public synchronized void addExternalEvent(
			final TimeSystemEvent timeSystemEvent) {
		this.externalEvents.add(timeSystemEvent);
	}

	/*
	 * methods for removing ITimeSystemEvents
	 */

	public void removeEvents(final Set<TimeSystemEvent> events) {
		for (YearImpl year : this.years.values()) {
			year.removeEvents(events);
		}
	}

	/*
	 * methods for proceeding in time
	 */

	public void nextHour() {
		this.gregorianCalendar.add(GregorianCalendar.HOUR_OF_DAY, 1);
		ApplicationContext.getInstance().getLog()
				.notifyTimeSystem_nextHour(getCurrentDate());
		if (HourType.getHourType(this.gregorianCalendar
				.get(GregorianCalendar.HOUR_OF_DAY)) == HourType.HOUR_00) {
			ApplicationContext.getInstance().getLog()
					.notifyTimeSystem_nextDay(getCurrentDate());
			this.dayNumber++;
		}
		this.triggerEvents();
	}

	private synchronized void triggerEvents() {
		// determine current date
		final YearImpl yearExact = this.years.get(this.getCurrentYear());
		final YearImpl yearEvery = this.years.get(-1);

		final MonthType currentMonthType = this.getCurrentMonthType();
		final DayType currentDayType = this.getCurrentDayType();
		final HourType currentHourType = this.getCurrentHourType();

		// select events for this date
		final List<TimeSystemEvent> events = new ArrayList<TimeSystemEvent>();

		if (yearExact != null) {
			events.addAll(yearExact.getEvents(currentMonthType, currentDayType,
					currentHourType));
		}

		if (yearEvery != null) {
			events.addAll(yearEvery.getEvents(currentMonthType, currentDayType,
					currentHourType));
		}

		for (TimeSystemEvent event : events) {
			try {
				/*
				 * it may happen, that an event deconstructs an agent, and that
				 * agent has registered other events for the same point in time
				 * -> they are contained in the events-list -> check for
				 * deconstruction
				 */
				if (!event.isDeconstructed()) {
					event.onEvent();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (HourType.HOUR_00.equals(currentHourType)) {
			// potential external events from GUI
			for (TimeSystemEvent event : this.externalEvents) {
				try {
					event.onEvent();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			this.externalEvents.clear();
		}

		// flush state to database
		HibernateUtil.flushSession();
	}

	public String toString() {
		return this.dayFormat.format(gregorianCalendar.getTime());
	}
}