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

import java.util.HashSet;
import java.util.Set;

import compecon.economy.sectors.financial.CentralBank;
import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.economy.sectors.industry.Factory;
import compecon.economy.sectors.state.State;
import compecon.economy.sectors.trading.Trader;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.util.HibernateUtil;
import compecon.materia.GoodType;

public abstract class AbstractConfigurationRunnerImpl extends
		AbstractRunnerImpl {

	protected void setUp() {
		for (Currency currency : Currency.values()) {
			if (ApplicationContext.getInstance().getConfiguration().stateConfig
					.getNumber(currency) == 1) {
				// initialize states
				ApplicationContext.getInstance().getAgentService()
						.getInstanceState(currency);
			}
		}

		for (Currency currency : Currency.values()) {
			if (ApplicationContext.getInstance().getConfiguration().centralBankConfig
					.getNumber(currency) == 1) {
				// initialize central banks
				ApplicationContext.getInstance().getAgentService()
						.getInstanceCentralBank(currency);
			}
		}

		for (Currency currency : Currency.values()) {
			Set<Currency> offeredCurrencies = new HashSet<Currency>();
			offeredCurrencies.add(currency);

			// initialize credit banks
			for (int i = 0; i < ApplicationContext.getInstance()
					.getConfiguration().creditBankConfig.getNumber(currency); i++) {
				ApplicationContext.getInstance().getAgentService()
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
						ApplicationContext.getInstance().getAgentService()
								.newInstanceFactory(goodType, currency);
					}
				}
			}
		}

		for (Currency currency : Currency.values()) {
			// initialize traders
			for (int i = 0; i < ApplicationContext.getInstance()
					.getConfiguration().traderConfig.getNumber(currency); i++) {
				ApplicationContext.getInstance().getAgentService()
						.newInstanceTrader(currency);
			}
		}

		for (Currency currency : Currency.values()) {
			// initialize households
			for (int i = 0; i < ApplicationContext.getInstance()
					.getConfiguration().householdConfig.getNumber(currency); i++) {
				Household household = ApplicationContext.getInstance()
						.getAgentService().newInstanceHousehold(currency);
				// division, so that households have time left until
				// retirement
				household
						.setAgeInDays((household.hashCode() % ApplicationContext
								.getInstance().getConfiguration().householdConfig
								.getLifespanInDays()) / 2);
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
