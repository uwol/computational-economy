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

import java.util.HashSet;
import java.util.Set;

import compecon.engine.timesystem.Hour;
import compecon.engine.timesystem.TimeSystemEvent;

public class HourImpl implements Hour {

	private HourType hourType;

	private Set<TimeSystemEvent> events = new HashSet<TimeSystemEvent>();

	public HourImpl(final HourType hourType) {
		this.hourType = hourType;
	}

	public HourType getHourType() {
		return this.hourType;
	}

	public void addEvent(final TimeSystemEvent event) {
		this.events.add(event);
	}

	public Set<TimeSystemEvent> getEvents() {
		return this.events;
	}

	public void removeEvents(final Set<TimeSystemEvent> events) {
		this.events.removeAll(events);
	}
}
