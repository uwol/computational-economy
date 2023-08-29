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

package io.github.uwol.compecon.engine.factory.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.uwol.compecon.economy.agent.Agent;
import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.sectors.financial.CentralBank;
import io.github.uwol.compecon.economy.sectors.financial.CreditBank;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.economy.sectors.financial.impl.CentralBankImpl;
import io.github.uwol.compecon.economy.sectors.financial.impl.CreditBankImpl;
import io.github.uwol.compecon.economy.sectors.household.Household;
import io.github.uwol.compecon.economy.sectors.household.impl.HouseholdImpl;
import io.github.uwol.compecon.economy.sectors.industry.Factory;
import io.github.uwol.compecon.economy.sectors.industry.impl.FactoryImpl;
import io.github.uwol.compecon.economy.sectors.state.State;
import io.github.uwol.compecon.economy.sectors.state.impl.StateImpl;
import io.github.uwol.compecon.economy.sectors.trading.Trader;
import io.github.uwol.compecon.economy.sectors.trading.impl.TraderImpl;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.factory.AgentFactory;

public class AgentImplFactoryImpl implements AgentFactory {

	protected final List<Class<? extends Agent>> agentTypes = new ArrayList<Class<? extends Agent>>();

	public AgentImplFactoryImpl() {
		agentTypes.add(HouseholdImpl.class);
		agentTypes.add(CreditBankImpl.class);
		agentTypes.add(CentralBankImpl.class);
		agentTypes.add(StateImpl.class);
		agentTypes.add(FactoryImpl.class);
		agentTypes.add(TraderImpl.class);
	}

	@Override
	public void constructAgentsFromConfiguration() {
		for (final Currency currency : Currency.values()) {
			if (ApplicationContext.getInstance().getConfiguration().stateConfig.getNumber(currency) == 1) {
				// initialize states
				ApplicationContext.getInstance().getAgentService().findState(currency);
			}
		}

		for (final Currency currency : Currency.values()) {
			if (ApplicationContext.getInstance().getConfiguration().centralBankConfig.getNumber(currency) == 1) {
				// initialize central banks
				ApplicationContext.getInstance().getAgentService().findCentralBank(currency);
			}
		}

		for (final Currency currency : Currency.values()) {
			final Set<Currency> offeredCurrencies = new HashSet<Currency>();
			offeredCurrencies.add(currency);

			// initialize credit banks
			for (int i = 0; i < ApplicationContext.getInstance().getConfiguration().creditBankConfig
					.getNumber(currency); i++) {
				ApplicationContext.getInstance().getCreditBankFactory().newInstanceCreditBank(offeredCurrencies,
						currency);
			}
		}

		for (final Currency currency : Currency.values()) {
			// initialize factories
			for (final GoodType goodType : GoodType.values()) {
				if (!GoodType.LABOURHOUR.equals(goodType)) {
					for (int i = 0; i < ApplicationContext.getInstance().getConfiguration().factoryConfig
							.getNumber(currency, goodType); i++) {
						ApplicationContext.getInstance().getFactoryFactory().newInstanceFactory(goodType, currency);
					}
				}
			}
		}

		for (final Currency currency : Currency.values()) {
			// initialize traders
			for (int i = 0; i < ApplicationContext.getInstance().getConfiguration().traderConfig
					.getNumber(currency); i++) {
				ApplicationContext.getInstance().getTraderFactory().newInstanceTrader(currency);
			}
		}

		final int householdLifeSpanInDays = ApplicationContext.getInstance().getConfiguration().householdConfig
				.getLifespanInDays();
		// division by 2, so that households have time left until
		// retirement
		final int householdAgeLimit = householdLifeSpanInDays / 2;

		for (final Currency currency : Currency.values()) {
			// initialize households
			for (int i = 0; i < ApplicationContext.getInstance().getConfiguration().householdConfig
					.getNumber(currency); i++) {
				final int ageInDays = ApplicationContext.getInstance().getRandomNumberGenerator()
						.nextInt(householdAgeLimit);
				ApplicationContext.getInstance().getHouseholdFactory().newInstanceHousehold(currency, ageInDays);
			}
		}
	}

	@Override
	public void deconstructAgents() {
		for (final Household household : ApplicationContext.getInstance().getHouseholdDAO().findAll()) {
			household.deconstruct();
		}

		for (final Trader trader : ApplicationContext.getInstance().getTraderDAO().findAll()) {
			trader.deconstruct();
		}

		for (final Factory factory : ApplicationContext.getInstance().getFactoryDAO().findAll()) {
			factory.deconstruct();
		}

		for (final CreditBank creditBank : ApplicationContext.getInstance().getCreditBankDAO().findAll()) {
			creditBank.deconstruct();
		}

		for (final CentralBank centralBank : ApplicationContext.getInstance().getCentralBankDAO().findAll()) {
			centralBank.deconstruct();
		}

		for (final State state : ApplicationContext.getInstance().getStateDAO().findAll()) {
			state.deconstruct();
		}
	}

	@Override
	public List<Class<? extends Agent>> getAgentTypes() {
		return agentTypes;
	}
}
