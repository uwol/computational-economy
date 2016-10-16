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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import io.github.uwol.compecon.engine.timesystem.Day;
import io.github.uwol.compecon.engine.timesystem.TimeSystemEvent;

public class DayImpl implements Day {

	private final DayType dayType;

	private final HashMap<HourType, HourImpl> hours = new HashMap<HourType, HourImpl>();

	public DayImpl(final DayType dayType) {
		this.dayType = dayType;
	}

	@Override
	public void addEvent(final TimeSystemEvent event, final HourType hourType) {
		if (!hours.containsKey(hourType)) {
			hours.put(hourType, new HourImpl(hourType));
		}
		hours.get(hourType).addEvent(event);
	}

	@Override
	public DayType getDayType() {
		return dayType;
	}

	@Override
	public Set<TimeSystemEvent> getEvents(final HourType hourType) {
		final HourImpl hourExact = hours.get(hourType);
		final HourImpl hourEvery = hours.get(HourType.EVERY);

		final Set<TimeSystemEvent> events = new HashSet<TimeSystemEvent>();

		if (hourExact != null) {
			events.addAll(hourExact.getEvents());
		}

		if (hourEvery != null) {
			events.addAll(hourEvery.getEvents());
		}

		return events;
	}

	@Override
	public void removeEvents(final Set<TimeSystemEvent> events) {
		for (final HourImpl hour : hours.values()) {
			hour.removeEvents(events);
		}
	}
}
