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

package compecon.engine.dao.inmemory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import compecon.engine.Agent;

public abstract class AgentIndexedInMemoryDAO<T> extends InMemoryDAO<T> {

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

	protected synchronized List<T> getInstancesForAgent(Agent agent) {
		if (this.agentIndexedInstances.containsKey(agent))
			return this.agentIndexedInstances.get(agent);
		// has to return null, as the calling DAO method should return a new
		// collection anyway, not this one
		return null;
	}

	protected synchronized List<Agent> getAgentsForInstance(T instance) {
		if (this.instanceIndexedAgents.containsKey(instance))
			return this.instanceIndexedAgents.get(instance);
		// has to return null, as the calling DAO method should return a new
		// collection anyway, not this one
		return null;
	}

	/*
	 * actions
	 */

	protected synchronized void save(Agent agent, T instance) {
		this.assureInitializedDataStructure(agent, instance);

		if (agent != null) {
			this.agentIndexedInstances.get(agent).add(instance);
			this.instanceIndexedAgents.get(instance).add(agent);
		}
		super.save(instance);
	}

	public synchronized void delete(T instance) {
		List<Agent> agents = getAgentsForInstance(instance);
		if (agents != null) {
			for (Agent agent : new ArrayList<Agent>(agents)) {
				if (this.agentIndexedInstances.containsKey(agent)) {
					this.agentIndexedInstances.get(agent).remove(instance);
					if (this.agentIndexedInstances.get(agent).isEmpty())
						this.agentIndexedInstances.remove(agent);
				}

				if (this.instanceIndexedAgents.containsKey(instance)) {
					this.instanceIndexedAgents.get(instance).remove(agent);
					if (this.instanceIndexedAgents.get(instance).isEmpty())
						this.instanceIndexedAgents.remove(instance);
				}
			}
		}

		super.delete(instance);
	}
}
