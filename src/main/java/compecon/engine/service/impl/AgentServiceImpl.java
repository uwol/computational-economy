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

package compecon.engine.service.impl;

import java.util.ArrayList;
import java.util.List;

import compecon.economy.agent.Agent;
import compecon.economy.sectors.financial.CentralBank;
import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.financial.impl.CentralBankImpl;
import compecon.economy.sectors.financial.impl.CreditBankImpl;
import compecon.economy.sectors.household.impl.HouseholdImpl;
import compecon.economy.sectors.industry.Factory;
import compecon.economy.sectors.industry.impl.FactoryImpl;
import compecon.economy.sectors.state.State;
import compecon.economy.sectors.state.impl.StateImpl;
import compecon.economy.sectors.trading.impl.TraderImpl;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.service.AgentService;

public class AgentServiceImpl implements AgentService {

	public final List<Class<? extends Agent>> agentTypes = new ArrayList<Class<? extends Agent>>();

	public AgentServiceImpl() {
		this.agentTypes.add(HouseholdImpl.class);
		this.agentTypes.add(CreditBankImpl.class);
		this.agentTypes.add(CentralBankImpl.class);
		this.agentTypes.add(StateImpl.class);
		this.agentTypes.add(FactoryImpl.class);
		this.agentTypes.add(TraderImpl.class);
	}

	public List<Class<? extends Agent>> getAgentTypes() {
		return this.agentTypes;
	}

	public State getInstanceState(final Currency currency) {
		State state = ApplicationContext.getInstance().getStateDAO()
				.findByCurrency(currency);
		if (state == null) {
			return ApplicationContext.getInstance().getStateFactory()
					.newInstanceState(currency);
		}
		return state;
	}

	public CentralBank getInstanceCentralBank(final Currency currency) {
		CentralBank centralBank = ApplicationContext.getInstance()
				.getCentralBankDAO().findByCurrency(currency);
		if (centralBank == null) {
			return ApplicationContext.getInstance().getCentralBankFactory()
					.newInstanceCentralBank(currency);
		}
		return centralBank;
	}

	public CreditBank getRandomInstanceCreditBank(final Currency currency) {
		return ApplicationContext.getInstance().getCreditBankDAO()
				.findRandom(currency);
	}

	public List<CreditBank> getAllCreditBanks(Currency currency) {
		return ApplicationContext.getInstance().getCreditBankDAO()
				.findAllByCurrency(currency);
	}

	public List<Factory> getAllFactories() {
		return ApplicationContext.getInstance().getFactoryDAO().findAll();
	}
}
