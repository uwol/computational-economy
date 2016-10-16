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

import java.util.HashSet;
import java.util.Set;

import io.github.uwol.compecon.engine.timesystem.Hour;
import io.github.uwol.compecon.engine.timesystem.TimeSystemEvent;

public class HourImpl implements Hour {

	private final Set<TimeSystemEvent> events = new HashSet<TimeSystemEvent>();

	private final HourType hourType;

	public HourImpl(final HourType hourType) {
		this.hourType = hourType;
	}

	@Override
	public void addEvent(final TimeSystemEvent event) {
		events.add(event);
	}

	@Override
	public Set<TimeSystemEvent> getEvents() {
		return events;
	}

	@Override
	public HourType getHourType() {
		return hourType;
	}

	@Override
	public void removeEvents(final Set<TimeSystemEvent> events) {
		this.events.removeAll(events);
	}
}
