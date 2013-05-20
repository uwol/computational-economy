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

package compecon.engine.jmx.model;

import java.util.HashMap;
import java.util.Map;

import compecon.engine.Agent;

public class NumberOfAgentsModel extends Model {
	Map<Class<? extends Agent>, Integer> numberOfAgents = new HashMap<Class<? extends Agent>, Integer>();

	public void agent_onConstruct(Class<? extends Agent> agentType) {
		// store number of agents
		int numberOfAgentsForAgentType = 0;
		if (this.numberOfAgents.containsKey(agentType))
			numberOfAgentsForAgentType = this.numberOfAgents.get(agentType);
		numberOfAgentsForAgentType++;
		this.numberOfAgents.put(agentType, numberOfAgentsForAgentType);

		this.notifyListeners();
	}

	public void agent_onDeconstruct(Class<? extends Agent> agentType) {
		int numberOfAgentsForAgentType = 0;
		if (this.numberOfAgents.containsKey(agentType))
			numberOfAgentsForAgentType = this.numberOfAgents.get(agentType);
		numberOfAgentsForAgentType--;
		this.numberOfAgents.put(agentType, numberOfAgentsForAgentType);

		this.notifyListeners();
	}

	public Map<Class<? extends Agent>, Integer> getNumberOfAgents() {
		return numberOfAgents;
	}
}
