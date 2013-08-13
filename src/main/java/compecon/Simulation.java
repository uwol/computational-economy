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

package compecon;

import java.util.HashSet;
import java.util.Set;

import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.household.Household;
import compecon.engine.AgentFactory;
import compecon.engine.dashboard.Dashboard;
import compecon.engine.jmx.JMXRegistration;
import compecon.engine.time.TimeSystem;
import compecon.engine.time.calendar.HourType;
import compecon.engine.util.ConfigurationUtil;
import compecon.engine.util.HibernateUtil;
import compecon.nature.materia.GoodType;

public class Simulation {

	protected static int lastId = 0;

	protected static int millisecondsToSleepPerHourType = 0;

	protected static boolean paused = false;

	protected static boolean singleStep = false;

	public static int getNextId() {
		lastId++;
		return lastId;
	}

	public static void setMillisecondsToSleepPerHourType(
			int millisecondsToSleepPerHourType) {
		Simulation.millisecondsToSleepPerHourType = millisecondsToSleepPerHourType;
	}

	public static void setPaused(boolean paused) {
		Simulation.paused = paused;
	}

	public static void setSingleStep() {
		Simulation.singleStep = true;
	}

	public static void main(String[] args) {
		try {
			// open dashboard
			Dashboard.getInstance();

			// init database connection
			HibernateUtil.openSession();

			// init JMX
			JMXRegistration.init();

			// configure simulation
			final int NUMBER_OF_CREDITBANKSPERCURRENCY = 5;

			int NUMBER_OF_HOUSEHOLDSPERCURRENCY;
			int NUMBER_OF_FACTORIES_PER_GOODTYPE_AND_CURRENCY;
			int NUMBER_OF_TRADERSPERCURRENCY;

			if (!ConfigurationUtil.getActivateDb()) {
				NUMBER_OF_HOUSEHOLDSPERCURRENCY = 2000;
				NUMBER_OF_FACTORIES_PER_GOODTYPE_AND_CURRENCY = 5;
				NUMBER_OF_TRADERSPERCURRENCY = 10;
			} else {
				NUMBER_OF_HOUSEHOLDSPERCURRENCY = 10;
				NUMBER_OF_FACTORIES_PER_GOODTYPE_AND_CURRENCY = 1;
				NUMBER_OF_TRADERSPERCURRENCY = 2;
			}

			// initialize the time system, so that agents can register their
			// events
			TimeSystem timeSystem = TimeSystem.getInstance();

			for (Currency currency : Currency.values()) {
				// initialize states
				AgentFactory.getInstanceState(currency);
			}

			for (Currency currency : Currency.values()) {
				// initialize central banks
				AgentFactory.getInstanceCentralBank(currency);
			}

			for (Currency currency : Currency.values()) {
				Set<Currency> offeredCurrencies = new HashSet<Currency>();
				offeredCurrencies.add(currency);

				// initialize credit banks
				for (int i = 0; i < NUMBER_OF_CREDITBANKSPERCURRENCY; i++) {
					AgentFactory.newInstanceCreditBank(offeredCurrencies,
							currency);
				}
			}

			for (Currency currency : Currency.values()) {
				// initialize factories
				for (GoodType goodType : GoodType.values()) {
					if (!GoodType.LABOURHOUR.equals(goodType)) {
						for (int i = 0; i < NUMBER_OF_FACTORIES_PER_GOODTYPE_AND_CURRENCY; i++) {
							AgentFactory.newInstanceFactory(goodType, currency);
						}
					}
				}
			}

			for (Currency currency : Currency.values()) {
				// initialize traders
				for (int i = 0; i < NUMBER_OF_TRADERSPERCURRENCY; i++) {
					AgentFactory.newInstanceTrader(currency);
				}
			}

			for (Currency currency : Currency.values()) {
				// initialize households
				for (int i = 0; i < NUMBER_OF_HOUSEHOLDSPERCURRENCY; i++) {
					Household household = AgentFactory
							.newInstanceHousehold(currency);
					// division, so that households have time left to retirement
					household.setAgeInDays((household.hashCode() % household
							.getLifespanInDays()) / 2);
				}
			}

			HibernateUtil.flushSession();

			// start simulation
			while (true) {
				if (!paused) {
					// step hour-wise; trigger events in time system
					timeSystem.nextHour();
					Thread.sleep(millisecondsToSleepPerHourType);
				} else {
					if (singleStep) {
						timeSystem.nextHour();
						if (HourType.HOUR_00.equals(TimeSystem.getInstance()
								.getCurrentHourType()))
							singleStep = false;
					} else
						Thread.sleep(50);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			HibernateUtil.closeSession();
		}
	}
}
