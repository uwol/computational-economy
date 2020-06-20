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

import io.github.uwol.compecon.engine.timesystem.TimeSystemEvent;
import io.github.uwol.compecon.engine.timesystem.Year;

public class YearImpl implements Year {
	private final HashMap<MonthType, MonthImpl> months = new HashMap<MonthType, MonthImpl>();

	@Override
	public void addEvent(final TimeSystemEvent event, final MonthType monthType, final DayType dayType,
			final HourType hourType) {
		if (!months.containsKey(monthType)) {
			months.put(monthType, new MonthImpl(monthType));
		}

		months.get(monthType).addEvent(event, dayType, hourType);
	}

	@Override
	public Set<TimeSystemEvent> getEvents(final MonthType monthType, final DayType dayType, final HourType hourType) {
		final MonthImpl monthExact = months.get(monthType);
		final MonthImpl monthEvery = months.get(MonthType.EVERY);

		final Set<TimeSystemEvent> events = new HashSet<TimeSystemEvent>();

		if (monthExact != null) {
			events.addAll(monthExact.getEvents(dayType, hourType));
		}

		if (monthEvery != null) {
			events.addAll(monthEvery.getEvents(dayType, hourType));
		}

		return events;
	}

	@Override
	public void removeEvents(final Set<TimeSystemEvent> events) {
		for (final MonthImpl month : months.values()) {
			month.removeEvents(events);
		}
	}
}
