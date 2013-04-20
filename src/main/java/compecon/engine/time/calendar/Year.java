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

package compecon.engine.time.calendar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import compecon.engine.time.ITimeSystemEvent;

public class Year {
	private HashMap<MonthType, Month> months = new HashMap<MonthType, Month>();

	public void addEvent(ITimeSystemEvent event, MonthType monthType,
			DayType dayType, HourType hourType) {
		if (!this.months.containsKey(monthType))
			this.months.put(monthType, new Month(monthType));
		this.months.get(monthType).addEvent(event, dayType, hourType);
	}

	public List<ITimeSystemEvent> getEvents(MonthType monthType,
			DayType dayType, HourType hourType) {
		Month monthExact = this.months.get(monthType);
		Month monthEvery = this.months.get(MonthType.EVERY);

		List<ITimeSystemEvent> events = new ArrayList<ITimeSystemEvent>();

		if (monthExact != null)
			events.addAll(monthExact.getEvents(dayType, hourType));

		if (monthEvery != null)
			events.addAll(monthEvery.getEvents(dayType, hourType));

		return events;
	}

	public void removeEvents(Set<ITimeSystemEvent> events) {
		for (Month month : this.months.values())
			month.removeEvents(events);
	}
}
