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

package compecon.engine.dao.inmemory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import compecon.engine.Agent;

public abstract class AgentIndexedInMemoryDAO<T> extends InMemoryDAO<T> {

	private Map<Agent, Set<T>> agentIndexedInstances = new HashMap<Agent, Set<T>>();

	private synchronized void assertInitializedDataStructure(Agent agent) {
		if (!this.agentIndexedInstances.containsKey(agent))
			this.agentIndexedInstances.put(agent, new HashSet<T>());
	}

	/*
	 * get instances for agent
	 */

	protected synchronized Set<T> getInstancesForAgent(Agent agent) {
		if (this.agentIndexedInstances.containsKey(agent))
			return this.agentIndexedInstances.get(agent);
		return null;
	}

	/*
	 * actions
	 */

	protected synchronized void save(Agent agent, T instance) {
		this.assertInitializedDataStructure(agent);

		this.agentIndexedInstances.get(agent).add(instance);
		super.save(instance);
	}

	protected synchronized void delete(Agent agent, T instance) {
		if (this.agentIndexedInstances.containsKey(agent)) {
			this.agentIndexedInstances.get(agent).remove(instance);
			if (this.agentIndexedInstances.get(agent).isEmpty())
				this.agentIndexedInstances.remove(agent);
		}

		super.delete(instance);
	}
}
