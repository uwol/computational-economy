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

package compecon.engine.timesystem;

import java.util.Date;

import compecon.engine.timesystem.impl.DayType;
import compecon.engine.timesystem.impl.HourType;
import compecon.engine.timesystem.impl.MonthType;

public interface TimeSystem {

	public Date getCurrentDate();

	public int getCurrentYear();

	public int getCurrentMonthNumberInYear();

	public int getCurrentDayNumberInMonth();

	public int getStartYear();

	public MonthType getCurrentMonthType();

	public DayType getCurrentDayType();

	public HourType getCurrentHourType();

	public HourType suggestRandomHourType();

	public HourType suggestRandomHourType(final HourType minHourType,
			final HourType maxHourType);

	public boolean isInitializationPhase();

	public void addEvent(final TimeSystemEvent event, final int year,
			final MonthType monthType, final DayType dayType,
			final HourType hourType);

	public void addEventEvery(final TimeSystemEvent event, final int year,
			final MonthType monthType, final DayType dayType,
			final HourType excepthourType);

	public void addEventForEveryDay(final TimeSystemEvent event);

	public void addEventForEveryMorning(final TimeSystemEvent event);

	public void addEventForEveryEvening(final TimeSystemEvent event);

	public void addEventForEveryHour(final TimeSystemEvent event);

	public void addExternalEvent(final TimeSystemEvent timeSystemEvent);

	public void removeEvent(final TimeSystemEvent event);

	public void nextHour();

	public void cleanRemovedEvents();

}
