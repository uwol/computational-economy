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
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import compecon.economy.materia.GoodType;
import compecon.economy.sectors.financial.CentralBank;
import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.economy.sectors.industry.Factory;
import compecon.economy.sectors.state.State;
import compecon.economy.sectors.trading.Trader;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.util.HibernateUtil;

public class ConfigurationSimulationRunnerImpl extends SimulationRunnerImpl {

	protected final Random random = new Random();

	@Override
	public void run(final Date endDate) {
		this.setUp();
		super.run(endDate);
		this.tearDown();
	}

	protected void setUp() {
		for (Currency currency : Currency.values()) {
			if (ApplicationContext.getInstance().getConfiguration().stateConfig
					.getNumber(currency) == 1) {
				// initialize states
				ApplicationContext.getInstance().getAgentService()
						.findState(currency);
			}
		}

		for (Currency currency : Currency.values()) {
			if (ApplicationContext.getInstance().getConfiguration().centralBankConfig
					.getNumber(currency) == 1) {
				// initialize central banks
				ApplicationContext.getInstance().getAgentService()
						.findCentralBank(currency);
			}
		}

		for (Currency currency : Currency.values()) {
			Set<Currency> offeredCurrencies = new HashSet<Currency>();
			offeredCurrencies.add(currency);

			// initialize credit banks
			for (int i = 0; i < ApplicationContext.getInstance()
					.getConfiguration().creditBankConfig.getNumber(currency); i++) {
				ApplicationContext.getInstance().getCreditBankFactory()
						.newInstanceCreditBank(offeredCurrencies, currency);
			}
		}

		for (Currency currency : Currency.values()) {
			// initialize factories
			for (GoodType goodType : GoodType.values()) {
				if (!GoodType.LABOURHOUR.equals(goodType)) {
					for (int i = 0; i < ApplicationContext.getInstance()
							.getConfiguration().factoryConfig.getNumber(
							currency, goodType); i++) {
						ApplicationContext.getInstance().getFactoryFactory()
								.newInstanceFactory(goodType, currency);
					}
				}
			}
		}

		for (Currency currency : Currency.values()) {
			// initialize traders
			for (int i = 0; i < ApplicationContext.getInstance()
					.getConfiguration().traderConfig.getNumber(currency); i++) {
				ApplicationContext.getInstance().getTraderFactory()
						.newInstanceTrader(currency);
			}
		}

		for (Currency currency : Currency.values()) {
			// initialize households
			for (int i = 0; i < ApplicationContext.getInstance()
					.getConfiguration().householdConfig.getNumber(currency); i++) {
				// division, so that households have time left until
				// retirement
				int ageInDays = this.random.nextInt(ApplicationContext
						.getInstance().getConfiguration().householdConfig
						.getLifespanInDays()) / 2;
				ApplicationContext.getInstance().getHouseholdFactory()
						.newInstanceHousehold(currency, ageInDays);
			}
		}

		HibernateUtil.flushSession();
	}

	protected void tearDown() {
		for (Household household : ApplicationContext.getInstance()
				.getHouseholdDAO().findAll()) {
			household.deconstruct();
		}

		for (Trader trader : ApplicationContext.getInstance().getTraderDAO()
				.findAll()) {
			trader.deconstruct();
		}

		for (Factory factory : ApplicationContext.getInstance().getFactoryDAO()
				.findAll()) {
			factory.deconstruct();
		}

		for (CreditBank creditBank : ApplicationContext.getInstance()
				.getCreditBankDAO().findAll()) {
			creditBank.deconstruct();
		}

		for (CentralBank centralBank : ApplicationContext.getInstance()
				.getCentralBankDAO().findAll()) {
			centralBank.deconstruct();
		}

		for (State state : ApplicationContext.getInstance().getStateDAO()
				.findAll()) {
			state.deconstruct();
		}
	}
}
