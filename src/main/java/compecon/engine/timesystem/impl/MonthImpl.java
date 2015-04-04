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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import compecon.engine.timesystem.Month;
import compecon.engine.timesystem.TimeSystemEvent;

public class MonthImpl implements Month {

	private final HashMap<DayType, DayImpl> days = new HashMap<DayType, DayImpl>();

	private final MonthType monthType;

	public MonthImpl(final MonthType monthType) {
		this.monthType = monthType;
	}

	@Override
	public void addEvent(final TimeSystemEvent event, final DayType dayType,
			final HourType hourType) {
		if (!days.containsKey(dayType)) {
			days.put(dayType, new DayImpl(dayType));
		}
		days.get(dayType).addEvent(event, hourType);
	}

	@Override
	public Set<TimeSystemEvent> getEvents(final DayType dayType,
			final HourType hourType) {
		final DayImpl dayExact = days.get(dayType);
		final DayImpl dayEvery = days.get(DayType.EVERY);

		final Set<TimeSystemEvent> events = new HashSet<TimeSystemEvent>();

		if (dayExact != null) {
			events.addAll(dayExact.getEvents(hourType));
		}

		if (dayEvery != null) {
			events.addAll(dayEvery.getEvents(hourType));
		}

		return events;
	}

	@Override
	public MonthType getMonthType() {
		return monthType;
	}

	public void removeEvents(final Set<TimeSystemEvent> events) {
		for (final DayImpl day : days.values()) {
			day.removeEvents(events);
		}
	}
}
