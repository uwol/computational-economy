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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import compecon.engine.time.ITimeSystemEvent;

public class Hour {

	private HourType hourType;

	private List<ITimeSystemEvent> events = new ArrayList<ITimeSystemEvent>();

	public Hour(HourType hourType) {
		this.hourType = hourType;
	}

	public HourType getHourType() {
		return this.hourType;
	}

	public void addEvent(ITimeSystemEvent event) {
		this.events.add(event);
	}

	public List<ITimeSystemEvent> getEvents() {
		// every time this method is called, events have to be shuffled, so
		// that
		// each day gives each agent a new chance of being first
		Collections.shuffle(this.events);
		return this.events;
	}

	public void removeEvents(Set<ITimeSystemEvent> events) {
		this.events.removeAll(events);
	}
}
