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

package compecon.engine.dao.inmemory.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import compecon.economy.agent.Agent;

public abstract class AgentDoubleIndexedInMemoryDAOImpl<T> extends
		AgentIndexedInMemoryDAOImpl<T> {

	private Map<Agent, List<T>> agentIndexedInstances = new HashMap<Agent, List<T>>();

	private Map<T, List<Agent>> instanceIndexedAgents = new HashMap<T, List<Agent>>();

	private synchronized void assureInitializedDataStructure(Agent agent,
			T instance) {
		if (agent != null && instance != null) {
			if (!this.agentIndexedInstances.containsKey(agent))
				this.agentIndexedInstances.put(agent, new ArrayList<T>());

			if (!this.instanceIndexedAgents.containsKey(instance))
				this.instanceIndexedAgents
						.put(instance, new ArrayList<Agent>());
		}
	}

	/*
	 * get instances for agent
	 */

	protected synchronized List<T> getInstancesForFirstAgent(Agent firstAgent) {
		return super.getInstancesForAgent(firstAgent);
	}

	protected synchronized List<T> getInstancesForSecondAgent(Agent secondAgent) {
		if (this.agentIndexedInstances.containsKey(secondAgent))
			return this.agentIndexedInstances.get(secondAgent);
		// has to return null, as the calling DAO method should return a new
		// collection anyway, not this one
		return null;
	}

	protected synchronized List<Agent> getFirstAgentsForInstance(T instance) {
		return super.getAgentsForInstance(instance);
	}

	protected synchronized List<Agent> getSecondAgentsForInstance(T instance) {
		if (this.instanceIndexedAgents.containsKey(instance))
			return this.instanceIndexedAgents.get(instance);
		// has to return null, as the calling DAO method should return a new
		// collection anyway, not this one
		return null;
	}

	/*
	 * actions
	 */

	protected synchronized void save(Agent firstAgent, Agent secondAgent,
			T instance) {
		this.assureInitializedDataStructure(secondAgent, instance);

		if (secondAgent != null) {
			this.agentIndexedInstances.get(secondAgent).add(instance);
			this.instanceIndexedAgents.get(instance).add(secondAgent);
		}
		super.save(firstAgent, instance);
	}

	public synchronized void delete(T instance) {
		List<Agent> secondAgents = getSecondAgentsForInstance(instance);
		if (secondAgents != null) {
			for (Agent secondAgent : new ArrayList<Agent>(secondAgents)) {
				if (this.agentIndexedInstances.containsKey(secondAgent)) {
					this.agentIndexedInstances.get(secondAgent)
							.remove(instance);
					if (this.agentIndexedInstances.get(secondAgent).isEmpty())
						this.agentIndexedInstances.remove(secondAgent);
				}

				if (this.instanceIndexedAgents.containsKey(instance)) {
					this.instanceIndexedAgents.get(instance)
							.remove(secondAgent);
					if (this.instanceIndexedAgents.get(instance).isEmpty())
						this.instanceIndexedAgents.remove(instance);
				}
			}
		}

		super.delete(instance);
	}
}
