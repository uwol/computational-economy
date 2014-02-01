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

package compecon.engine.timesystem;

import java.util.Set;

import compecon.engine.timesystem.impl.HourType;

public interface Hour {

	public HourType getHourType();

	public void addEvent(final TimeSystemEvent event);

	public Set<TimeSystemEvent> getEvents();

	public void removeEvents(final Set<TimeSystemEvent> events);
}
