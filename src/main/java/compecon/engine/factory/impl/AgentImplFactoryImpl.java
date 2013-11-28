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

package compecon.engine.factory.impl;

import java.util.ArrayList;
import java.util.List;

import compecon.economy.agent.Agent;
import compecon.economy.sectors.financial.impl.CentralBankImpl;
import compecon.economy.sectors.financial.impl.CreditBankImpl;
import compecon.economy.sectors.household.impl.HouseholdImpl;
import compecon.economy.sectors.industry.impl.FactoryImpl;
import compecon.economy.sectors.state.impl.StateImpl;
import compecon.economy.sectors.trading.impl.TraderImpl;
import compecon.engine.factory.AgentFactory;

public class AgentImplFactoryImpl implements AgentFactory {

	protected final List<Class<? extends Agent>> agentTypes = new ArrayList<Class<? extends Agent>>();

	public AgentImplFactoryImpl() {
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
}
