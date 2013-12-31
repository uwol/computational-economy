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

package compecon.engine.runner.impl;

import java.util.Date;

import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.runner.SimulationRunner;
import compecon.engine.timesystem.impl.HourType;

public class SimulationRunnerImpl implements SimulationRunner {

	protected boolean killFlag = false;

	protected boolean paused = false;

	protected boolean singleDayStep = false;

	protected boolean singleHourStep = false;

	protected int millisecondsToSleepPerHourType = 0;

	@Override
	public void run(final Date endDate) {
		try {
			// start simulation
			while (true) {
				// simulation ends
				if (killFlag) {
					break;
				} else if (endDate != null
						&& ApplicationContext.getInstance().getTimeSystem()
								.getCurrentDate().after(endDate)) {
					break;
				}
				// normal mode
				else if (!paused) {
					// step hour-wise; triggers events in time system
					ApplicationContext.getInstance().getTimeSystem().nextHour();
					Thread.sleep(millisecondsToSleepPerHourType);
				}
				// paused mode, only proceeding with singleDayStep interaction
				// by user
				else if (paused && singleDayStep) {
					ApplicationContext.getInstance().getTimeSystem().nextHour();
					if (HourType.HOUR_00
							.equals(ApplicationContext.getInstance()
									.getTimeSystem().getCurrentHourType())) {
						singleDayStep = false;
					}
				}
				// paused mode, only proceeding with singleHourStep interaction
				// by user
				else if (paused && singleHourStep) {
					singleHourStep = false;
					ApplicationContext.getInstance().getTimeSystem().nextHour();
				}
				// wait until next iteration
				else {
					Thread.sleep(50);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setMillisecondsToSleepPerHourType(
			final int millisecondsToSleepPerHourType) {
		this.millisecondsToSleepPerHourType = millisecondsToSleepPerHourType;
	}

	@Override
	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	@Override
	public void setSingleDayStep() {
		this.singleDayStep = true;
	}

	@Override
	public void setSingleHourStep() {
		this.singleHourStep = true;
	}

	@Override
	public void stop() {
		this.killFlag = true;
	}
}
