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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.timesystem.ITimeSystem;
import compecon.engine.timesystem.ITimeSystemEvent;
import compecon.engine.util.HibernateUtil;

/**
 * Agents register their actions as events in the time system (observer
 * pattern).
 */
public class TimeSystem implements ITimeSystem {

	private final int startYear;

	private int dayNumber = 0;

	private Random random = new Random();

	private GregorianCalendar gregorianCalendar = new GregorianCalendar();

	private SimpleDateFormat dayFormat = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm");

	private Map<Integer, Year> years = new HashMap<Integer, Year>();

	private Set<ITimeSystemEvent> eventsToBeRemoved = new HashSet<ITimeSystemEvent>();

	private List<ITimeSystemEvent> externalEvents = new ArrayList<ITimeSystemEvent>();

	public TimeSystem(int year) {
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
	public void addEvent(final ITimeSystemEvent event, final int year,
			final MonthType monthType, final DayType dayType,
			final HourType hourType) {
		if (!this.years.containsKey(year))
			this.years.put(year, new Year());
		this.years.get(year).addEvent(event, monthType, dayType, hourType);
	}

	/**
	 * @param year
	 *            -1 for every year
	 */
	public void addEventEvery(final ITimeSystemEvent event, final int year,
			final MonthType monthType, DayType dayType,
			final HourType excepthourType) {
		assert (excepthourType != null);

		if (!this.years.containsKey(year))
			this.years.put(year, new Year());
		for (HourType hourType : HourType.values()) {
			if (!HourType.EVERY.equals(hourType)
					&& !excepthourType.equals(hourType)) {
				this.years.get(year).addEvent(event, monthType, dayType,
						hourType);
			}
		}
	}

	public void addEventForEveryDay(final ITimeSystemEvent event) {
		this.addEvent(event, -1, MonthType.EVERY, DayType.EVERY,
				HourType.HOUR_12);
	}

	public void addEventForEveryMorning(final ITimeSystemEvent event) {
		this.addEvent(event, -1, MonthType.EVERY, DayType.EVERY,
				HourType.HOUR_07);
	}

	public void addEventForEveryEvening(final ITimeSystemEvent event) {
		this.addEvent(event, -1, MonthType.EVERY, DayType.EVERY,
				HourType.HOUR_18);
	}

	public void addEventForEveryHour(final ITimeSystemEvent event) {
		this.addEvent(event, -1, MonthType.EVERY, DayType.EVERY, HourType.EVERY);
	}

	/*
	 * methods for events induced by the dashboard
	 */

	public synchronized void addExternalEvent(
			final ITimeSystemEvent timeSystemEvent) {
		this.externalEvents.add(timeSystemEvent);
	}

	/*
	 * methods for removing ITimeSystemEvents
	 */

	public void removeEvent(final ITimeSystemEvent event) {
		this.eventsToBeRemoved.add(event);
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
		Year yearExact = this.years.get(this.getCurrentYear());
		Year yearEvery = this.years.get(-1);

		MonthType currentMonthType = this.getCurrentMonthType();
		DayType currentDayType = this.getCurrentDayType();
		HourType currentHourType = this.getCurrentHourType();

		// select events for this date
		List<ITimeSystemEvent> events = new ArrayList<ITimeSystemEvent>();

		if (yearExact != null)
			events.addAll(yearExact.getEvents(currentMonthType, currentDayType,
					currentHourType));

		if (yearEvery != null)
			events.addAll(yearEvery.getEvents(currentMonthType, currentDayType,
					currentHourType));

		for (ITimeSystemEvent event : events) {
			try {
				event.onEvent();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (HourType.HOUR_00.equals(currentHourType)) {
			// potential external events from GUI
			for (ITimeSystemEvent event : this.externalEvents) {
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

		if (false && DayType.DAY_01.equals(currentDayType)
				&& HourType.HOUR_00.equals(currentHourType)) {
			HibernateUtil.closeSession();
			HibernateUtil.openSession();
		}

		// remove events that have been marked as removable
		if (this.eventsToBeRemoved.size() > 0) {
			for (Year year : this.years.values())
				year.removeEvents(this.eventsToBeRemoved);
			this.eventsToBeRemoved.clear();
		}
	}

	public String toString() {
		return this.dayFormat.format(gregorianCalendar.getTime());
	}
}