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

public class Day {

	private DayType dayType;

	private HashMap<HourType, Hour> hours = new HashMap<HourType, Hour>();

	public Day(DayType dayType) {
		this.dayType = dayType;
	}

	public DayType getDayType() {
		return this.dayType;
	}

	public void addEvent(ITimeSystemEvent event, HourType hourType) {
		if (!this.hours.containsKey(hourType))
			this.hours.put(hourType, new Hour(hourType));
		this.hours.get(hourType).addEvent(event);
	}

	public List<ITimeSystemEvent> getEvents(HourType hourType) {
		Hour hourExact = this.hours.get(hourType);
		Hour hourEvery = this.hours.get(HourType.EVERY);

		List<ITimeSystemEvent> events = new ArrayList<ITimeSystemEvent>();

		if (hourExact != null)
			events.addAll(hourExact.getEvents());

		if (hourEvery != null)
			events.addAll(hourEvery.getEvents());

		return events;
	}

	public void removeEvents(Set<ITimeSystemEvent> events) {
		for (Hour hour : this.hours.values())
			hour.removeEvents(events);
	}
}
