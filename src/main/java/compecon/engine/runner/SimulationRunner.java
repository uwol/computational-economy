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

package compecon.engine.runner;

import java.util.Date;

/**
 * Central runner for the simulation and its time system.
 */
public interface SimulationRunner {

	/**
	 * start the simulation loop without an end date.
	 */
	public void run();

	/**
	 * start the simulation loop
	 *
	 * @param endDate
	 *            end date
	 */
	public void run(final Date endDate);

	public void setMillisecondsToSleepPerHourType(
			final int millisecondsToSleepPerHourType);

	/**
	 * pauses the simulation loop.
	 */
	public void setPaused(boolean paused);

	/**
	 * set a flag that the simulation loop should advance one day.
	 */
	public void stepSingleDay();

	/**
	 * set a flag that the simulation loop should advance one hour.
	 */
	public void stepSingleHour();

	/**
	 * shuts down the simulation loop.
	 */
	public void stop();
}