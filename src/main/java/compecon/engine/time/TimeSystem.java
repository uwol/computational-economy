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

package compecon.engine.time;

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

import compecon.engine.Log;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.HourType;
import compecon.engine.time.calendar.MonthType;
import compecon.engine.time.calendar.Year;
import compecon.engine.util.HibernateUtil;

/**
 * Agents register their actions as events in the time system (observer
 * pattern).
 */
public class TimeSystem {
	private static TimeSystem timeSystem;

	private Random random = new Random();

	private GregorianCalendar gregorianCalendar = new GregorianCalendar();

	private SimpleDateFormat dayFormat = new SimpleDateFormat(
			"dd.MM.yyyy HH:mm");

	private Map<Integer, Year> years = new HashMap<Integer, Year>();

	private Set<ITimeSystemEvent> eventsToBeRemoved = new HashSet<ITimeSystemEvent>();

	private List<ITimeSystemEvent> externalEvents = new ArrayList<ITimeSystemEvent>();

	protected TimeSystem(int year, MonthType monthType, DayType dayType) {
		gregorianCalendar = new GregorianCalendar(year,
				monthType.getMonthNumber(), dayType.getDayNumber());
	}

	protected TimeSystem() {
		gregorianCalendar = new GregorianCalendar(2000,
				MonthType.JANUARY.getMonthNumber(),
				DayType.DAY_01.getDayNumber());
	}

	public static TimeSystem getInstance() {
		if (TimeSystem.timeSystem == null)
			timeSystem = new TimeSystem();
		return timeSystem;
	}

	public static TimeSystem getInstance(int year, MonthType monthType,
			DayType dayType) {
		if (TimeSystem.timeSystem == null)
			timeSystem = new TimeSystem(year, monthType, dayType);
		return timeSystem;
	}

	public Date getCurrentDate() {
		return this.gregorianCalendar.getTime();
	}

	public int getCurrentYear() {
		return this.gregorianCalendar.get(GregorianCalendar.YEAR);
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

	public HourType suggestRandomHourType(HourType minHourType,
			HourType maxHourType) {
		int randomNumber = this.random.nextInt(maxHourType.getHourNumber() + 1
				- minHourType.getHourNumber());
		return HourType.getHourType(minHourType.getHourNumber() + randomNumber);
	}

	/*
	 * methods for adding ITimeSystemEvents
	 */

	public void addEvent(ITimeSystemEvent event, int year, MonthType monthType,
			DayType dayType, HourType hourType) {
		if (!this.years.containsKey(year))
			this.years.put(year, new Year());
		this.years.get(year).addEvent(event, monthType, dayType, hourType);
	}

	public void addEventForEveryDay(ITimeSystemEvent event) {
		this.addEvent(event, -1, MonthType.EVERY, DayType.EVERY,
				HourType.HOUR_12);
	}

	public void addEventForEveryMorning(ITimeSystemEvent event) {
		this.addEvent(event, -1, MonthType.EVERY, DayType.EVERY,
				HourType.HOUR_07);
	}

	public void addEventForEveryEvening(ITimeSystemEvent event) {
		this.addEvent(event, -1, MonthType.EVERY, DayType.EVERY,
				HourType.HOUR_18);
	}

	public void addEventForEveryHour(ITimeSystemEvent event) {
		this.addEvent(event, -1, MonthType.EVERY, DayType.EVERY, HourType.EVERY);
	}

	/*
	 * methods for events induced by the dashboard
	 */

	public synchronized void addExternalEvent(ITimeSystemEvent timeSystemEvent) {
		this.externalEvents.add(timeSystemEvent);
	}

	/*
	 * methods for removing ITimeSystemEvents
	 */

	public void removeEvent(ITimeSystemEvent event) {
		this.eventsToBeRemoved.add(event);
	}

	/*
	 * methods for proceeding in time
	 */

	public void nextHour() {
		this.gregorianCalendar.add(GregorianCalendar.HOUR_OF_DAY, 1);
		if (HourType.getHourType(this.gregorianCalendar
				.get(GregorianCalendar.HOUR_OF_DAY)) == HourType.HOUR_00)
			Log.notifyTimeSystem_nextDay(timeSystem.getCurrentDate());
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

		if (MonthType.JANUARY.equals(currentMonthType)
				&& DayType.DAY_01.equals(currentDayType)
				&& HourType.HOUR_00.equals(currentHourType)) {
			// store state in database
			HibernateUtil.getSession().flush();
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