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

package compecon.engine;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import compecon.economy.sectors.financial.CentralBank;
import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.economy.sectors.industry.Factory;
import compecon.economy.sectors.state.State;
import compecon.economy.sectors.trading.Trader;
import compecon.engine.dao.DAOFactory;
import compecon.engine.dashboard.Dashboard;
import compecon.engine.statistics.jmx.JMXRegistration;
import compecon.engine.statistics.model.ModelRegistry;
import compecon.engine.time.TimeSystem;
import compecon.engine.time.calendar.HourType;
import compecon.engine.util.ConfigurationUtil;
import compecon.engine.util.HibernateUtil;
import compecon.materia.GoodType;

public class Simulation {

	protected static Simulation instance;

	protected final Dashboard dashboard;

	protected final ModelRegistry modelRegistry;

	protected final TimeSystem timeSystem;

	protected boolean killFlag = false;

	protected boolean paused = false;

	protected boolean singleDayStep = false;

	protected boolean singleHourStep = false;

	protected final Date endDate;

	protected int lastId = 0;

	protected int millisecondsToSleepPerHourType = 0;

	public Simulation(boolean showDashboard, Date endDate) {
		instance = this;
		this.timeSystem = new TimeSystem(2000);
		this.modelRegistry = new ModelRegistry();
		this.endDate = endDate;

		if (showDashboard) {
			this.dashboard = new Dashboard();
		} else {
			this.dashboard = null;
		}
	}

	public void run() {
		setUp();
		try {
			// start simulation
			while (true) {
				// simulation ends
				if (killFlag) {
					break;
				} else if (endDate != null
						&& timeSystem.getCurrentDate().after(endDate)) {
					break;
				}
				// normal mode
				else if (!paused) {
					// step hour-wise; triggers events in time system
					timeSystem.nextHour();
					if (HourType.HOUR_00
							.equals(timeSystem.getCurrentHourType())) {
						if (this.dashboard != null) {
							this.dashboard.getControlPanel().refreshDateTime();
						}
					}
					Thread.sleep(millisecondsToSleepPerHourType);
				}
				// paused mode, only proceeding with singleDayStep interaction
				// by user
				else if (paused && singleDayStep) {
					timeSystem.nextHour();
					if (HourType.HOUR_00
							.equals(timeSystem.getCurrentHourType())) {
						singleDayStep = false;
						if (this.dashboard != null) {
							this.dashboard.getControlPanel().refreshDateTime();
						}
					}
				}
				// paused mode, only proceeding with singleHourStep interaction
				// by user
				else if (paused && singleHourStep) {
					singleHourStep = false;
					timeSystem.nextHour();
					if (this.dashboard != null) {
						this.dashboard.getControlPanel().refreshDateTime();
						// normally, the dashboard is redrawn at HOUR_00 only ->
						// trigger redrawing
						this.dashboard.notifyListener();
					}
				}
				// wait until next iteration
				else {
					Thread.sleep(50);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			tearDown();
		}
	}

	protected void setUp() {
		// init database connection
		HibernateUtil.openSession();

		// initialize the time system, so that agents can register their
		// events

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
			for (int i = 0; i < ConfigurationUtil.CreditBankConfig
					.getNumber(currency); i++) {
				AgentFactory.newInstanceCreditBank(offeredCurrencies, currency);
			}
		}

		for (Currency currency : Currency.values()) {
			// initialize factories
			for (GoodType goodType : GoodType.values()) {
				if (!GoodType.LABOURHOUR.equals(goodType)) {
					for (int i = 0; i < ConfigurationUtil.FactoryConfig
							.getNumberPerGoodType(currency); i++) {
						AgentFactory.newInstanceFactory(goodType, currency);
					}
				}
			}
		}

		for (Currency currency : Currency.values()) {
			// initialize traders
			for (int i = 0; i < ConfigurationUtil.TraderConfig
					.getNumber(currency); i++) {
				AgentFactory.newInstanceTrader(currency);
			}
		}

		for (Currency currency : Currency.values()) {
			// initialize households
			for (int i = 0; i < ConfigurationUtil.HouseholdConfig
					.getNumber(currency); i++) {
				Household household = AgentFactory
						.newInstanceHousehold(currency);
				// division, so that households have time left until
				// retirement
				household
						.setAgeInDays((household.hashCode() % ConfigurationUtil.HouseholdConfig
								.getLifespanInDays()) / 2);
			}
		}

		HibernateUtil.flushSession();

		// init JMX
		JMXRegistration.init();
	}

	protected void tearDown() {
		for (Household household : DAOFactory.getHouseholdDAO().findAll()) {
			household.deconstruct();
		}

		for (Trader trader : DAOFactory.getTraderDAO().findAll()) {
			trader.deconstruct();
		}

		for (Factory factory : DAOFactory.getFactoryDAO().findAll()) {
			factory.deconstruct();
		}

		for (CreditBank creditBank : DAOFactory.getCreditBankDAO().findAll()) {
			creditBank.deconstruct();
		}

		for (CentralBank centralBank : DAOFactory.getCentralBankDAO().findAll()) {
			centralBank.deconstruct();
		}

		for (State state : DAOFactory.getStateDAO().findAll()) {
			state.deconstruct();
		}

		HibernateUtil.flushSession();

		// close database conenction
		HibernateUtil.closeSession();

		JMXRegistration.close();

		instance = null;
	}

	public void stop() {
		this.killFlag = true;
	}

	// accessors

	public static Simulation getInstance() {
		return instance;
	}

	public Dashboard getDashboard() {
		return this.dashboard;
	}

	public TimeSystem getTimeSystem() {
		return this.timeSystem;
	}

	public ModelRegistry getModelRegistry() {
		return this.modelRegistry;
	}

	public int getNextId() {
		this.lastId++;
		return this.lastId;
	}

	public void setMillisecondsToSleepPerHourType(
			int millisecondsToSleepPerHourType) {
		this.millisecondsToSleepPerHourType = millisecondsToSleepPerHourType;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public void setSingleDayStep() {
		this.singleDayStep = true;
	}

	public void setSingleHourStep() {
		this.singleHourStep = true;
	}
}
