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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import compecon.engine.timesystem.Month;
import compecon.engine.timesystem.TimeSystemEvent;

public class MonthImpl implements Month {

	private MonthType monthType;

	private HashMap<DayType, DayImpl> days = new HashMap<DayType, DayImpl>();

	public MonthImpl(final MonthType monthType) {
		this.monthType = monthType;
	}

	public MonthType getMonthType() {
		return this.monthType;
	}

	public void addEvent(final TimeSystemEvent event, final DayType dayType,
			HourType hourType) {
		if (!this.days.containsKey(dayType))
			this.days.put(dayType, new DayImpl(dayType));
		this.days.get(dayType).addEvent(event, hourType);
	}

	public List<TimeSystemEvent> getEvents(final DayType dayType,
			final HourType hourType) {
		final DayImpl dayExact = this.days.get(dayType);
		final DayImpl dayEvery = this.days.get(DayType.EVERY);

		final List<TimeSystemEvent> events = new ArrayList<TimeSystemEvent>();

		if (dayExact != null)
			events.addAll(dayExact.getEvents(hourType));

		if (dayEvery != null)
			events.addAll(dayEvery.getEvents(hourType));

		return events;
	}

	public void removeEvents(final Set<TimeSystemEvent> events) {
		for (DayImpl day : this.days.values()) {
			day.removeEvents(events);
		}
	}
}
