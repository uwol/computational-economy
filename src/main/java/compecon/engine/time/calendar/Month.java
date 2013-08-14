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

package compecon.engine.time.calendar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import compecon.engine.time.ITimeSystemEvent;

public class Month {

	private MonthType monthType;

	private HashMap<DayType, Day> days = new HashMap<DayType, Day>();

	public Month(MonthType monthType) {
		this.monthType = monthType;
	}

	public MonthType getMonthType() {
		return this.monthType;
	}

	public void addEvent(ITimeSystemEvent event, DayType dayType,
			HourType hourType) {
		if (!this.days.containsKey(dayType))
			this.days.put(dayType, new Day(dayType));
		this.days.get(dayType).addEvent(event, hourType);
	}

	public List<ITimeSystemEvent> getEvents(DayType dayType, HourType hourType) {
		Day dayExact = this.days.get(dayType);
		Day dayEvery = this.days.get(DayType.EVERY);

		List<ITimeSystemEvent> events = new ArrayList<ITimeSystemEvent>();

		if (dayExact != null)
			events.addAll(dayExact.getEvents(hourType));

		if (dayEvery != null)
			events.addAll(dayEvery.getEvents(hourType));

		return events;
	}

	public void removeEvents(Set<ITimeSystemEvent> events) {
		for (Day day : this.days.values())
			day.removeEvents(events);
	}
}
