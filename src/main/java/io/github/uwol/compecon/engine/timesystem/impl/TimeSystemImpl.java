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

package io.github.uwol.compecon.engine.timesystem.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.timesystem.TimeSystem;
import io.github.uwol.compecon.engine.timesystem.TimeSystemEvent;
import io.github.uwol.compecon.engine.util.HibernateUtil;

/**
 * Agents register their actions as events in the time system (observer
 * pattern).
 */
public class TimeSystemImpl implements TimeSystem {

	private final SimpleDateFormat dayFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

	private int dayNumber = 0;

	private final List<TimeSystemEvent> externalEvents = new ArrayList<TimeSystemEvent>();

	private GregorianCalendar gregorianCalendar = new GregorianCalendar();

	private final int startYear;

	private final Map<Integer, YearImpl> years = new HashMap<Integer, YearImpl>();

	public TimeSystemImpl(final int year) {
		gregorianCalendar = new GregorianCalendar(year, MonthType.JANUARY.getMonthNumber(),
				DayType.DAY_01.getDayNumber());
		startYear = year;
	}

	/**
	 * @param year -1 for every year
	 */
	@Override
	public void addEvent(final TimeSystemEvent event, final int year, final MonthType monthType, final DayType dayType,
			final HourType hourType) {
		if (!years.containsKey(year)) {
			years.put(year, new YearImpl());
		}

		years.get(year).addEvent(event, monthType, dayType, hourType);
	}

	/**
	 * @param year -1 for every year
	 */
	@Override
	public void addEventEvery(final TimeSystemEvent event, final int year, final MonthType monthType,
			final DayType dayType, final HourType exceptHourType) {
		assert (exceptHourType != null);

		if (!years.containsKey(year)) {
			years.put(year, new YearImpl());
		}

		for (final HourType hourType : HourType.values()) {
			if (!HourType.EVERY.equals(hourType) && !exceptHourType.equals(hourType)) {
				years.get(year).addEvent(event, monthType, dayType, hourType);
			}
		}
	}

	@Override
	public void addEventForEveryDay(final TimeSystemEvent event) {
		addEvent(event, -1, MonthType.EVERY, DayType.EVERY, HourType.HOUR_12);
	}

	@Override
	public void addEventForEveryEvening(final TimeSystemEvent event) {
		addEvent(event, -1, MonthType.EVERY, DayType.EVERY, HourType.HOUR_18);
	}

	@Override
	public void addEventForEveryHour(final TimeSystemEvent event) {
		addEvent(event, -1, MonthType.EVERY, DayType.EVERY, HourType.EVERY);
	}

	@Override
	public void addEventForEveryMorning(final TimeSystemEvent event) {
		addEvent(event, -1, MonthType.EVERY, DayType.EVERY, HourType.HOUR_07);
	}

	@Override
	public synchronized void addExternalEvent(final TimeSystemEvent timeSystemEvent) {
		externalEvents.add(timeSystemEvent);
	}

	@Override
	public Date getCurrentDate() {
		return gregorianCalendar.getTime();
	}

	@Override
	public int getCurrentDayNumberInMonth() {
		return gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH);
	}

	@Override
	public DayType getCurrentDayType() {
		return DayType.getDayType(gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
	}

	@Override
	public HourType getCurrentHourType() {
		return HourType.getHourType(gregorianCalendar.get(GregorianCalendar.HOUR_OF_DAY));
	}

	/*
	 * methods for adding ITimeSystemEvents
	 */

	@Override
	public int getCurrentMonthNumberInYear() {
		return gregorianCalendar.get(GregorianCalendar.MONTH) + 1;
	}

	@Override
	public MonthType getCurrentMonthType() {
		return MonthType.getMonthType(gregorianCalendar.get(GregorianCalendar.MONTH));
	}

	@Override
	public int getCurrentYear() {
		return gregorianCalendar.get(GregorianCalendar.YEAR);
	}

	@Override
	public int getStartYear() {
		return startYear;
	}

	@Override
	public boolean isInitializationPhase() {
		return dayNumber < ApplicationContext.getInstance().getConfiguration().timeSystemConfig
				.getInitializationPhaseInDays();
	}

	@Override
	public void nextHour() {
		gregorianCalendar.add(GregorianCalendar.HOUR_OF_DAY, 1);
		ApplicationContext.getInstance().getLog().notifyTimeSystem_nextHour(getCurrentDate());

		if (HourType.getHourType(gregorianCalendar.get(GregorianCalendar.HOUR_OF_DAY)) == HourType.HOUR_00) {
			ApplicationContext.getInstance().getLog().notifyTimeSystem_nextDay(getCurrentDate());
			dayNumber++;
		}

		triggerEvents();
	}

	/*
	 * methods for events induced by the dashboard
	 */

	@Override
	public void removeEvents(final Set<TimeSystemEvent> events) {
		for (final YearImpl year : years.values()) {
			year.removeEvents(events);
		}
	}

	/*
	 * methods for removing ITimeSystemEvents
	 */

	@Override
	public HourType suggestRandomHourType() {
		// HourType.HOUR_23 and HourType.HOUR_00 are reserved for balance sheet
		// publication, interest calculation, ...
		return this.suggestRandomHourType(HourType.HOUR_01, HourType.HOUR_22);
	}

	/*
	 * methods for proceeding in time
	 */

	@Override
	public HourType suggestRandomHourType(final HourType minHourType, final HourType maxHourType) {
		final int limit = maxHourType.getHourNumber() + 1 - minHourType.getHourNumber();
		final int randomNumber = ApplicationContext.getInstance().getRandomNumberGenerator().nextInt(limit);
		return HourType.getHourType(minHourType.getHourNumber() + randomNumber);
	}

	@Override
	public String toString() {
		return dayFormat.format(gregorianCalendar.getTime());
	}

	private synchronized void triggerEvents() {
		// determine current date
		final YearImpl yearExact = years.get(getCurrentYear());
		final YearImpl yearEvery = years.get(-1);

		final MonthType currentMonthType = getCurrentMonthType();
		final DayType currentDayType = getCurrentDayType();
		final HourType currentHourType = getCurrentHourType();

		// select events for this date
		final List<TimeSystemEvent> events = new ArrayList<TimeSystemEvent>();

		if (yearExact != null) {
			events.addAll(yearExact.getEvents(currentMonthType, currentDayType, currentHourType));
		}

		if (yearEvery != null) {
			events.addAll(yearEvery.getEvents(currentMonthType, currentDayType, currentHourType));
		}

		/*
		 * important: every time this method is called, events have to be shuffled, so
		 * that each day gives each agent a new chance of being first
		 */
		final Random random = ApplicationContext.getInstance().getRandomNumberGenerator().getRandom();
		Collections.shuffle(events, random);

		for (final TimeSystemEvent event : events) {
			try {
				/*
				 * it may happen, that an event deconstructs an agent, and that agent has
				 * registered other events for the same point in time -> they are contained in
				 * the events-list -> check for deconstruction
				 */
				if (!event.isDeconstructed()) {
					event.onEvent();
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		if (HourType.HOUR_00.equals(currentHourType)) {
			// potential external events from GUI
			for (final TimeSystemEvent event : externalEvents) {
				try {
					event.onEvent();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			externalEvents.clear();
		}

		// flush state to database
		HibernateUtil.flushSession();
	}
}