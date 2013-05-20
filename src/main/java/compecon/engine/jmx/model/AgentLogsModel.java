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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import compecon.engine.Agent;
import compecon.engine.jmx.Log;

public class AgentLogsModel extends Model {

	protected final int MESSAGES_TO_STORE = 100;

	protected Queue<Object[]> messages = new LinkedList<Object[]>();

	protected ArrayList<Agent> agents = new ArrayList<Agent>();

	public void agent_onConstruct(Agent agent) {
		this.agents.add(agent);
		this.notifyListeners();
	}

	public void agent_onDeconstruct(Agent agent) {
		this.messages.remove(agent);
		this.agents.remove(agent);
		this.notifyListeners();
	}

	public void logAgentEvent(Date date, String message) {
		messages.add(new Object[] { date, message });
		if (messages.size() > MESSAGES_TO_STORE)
			messages.poll();
	}

	public void setCurrentAgent(Agent agent) {
		Log.setAgentSelectedByClient(agent);
		this.messages.clear();
		this.notifyListeners();
	}

	public void setCurrentAgent(Integer agentId) {
		this.setCurrentAgent(this.agents.get(agentId));
	}

	public Queue<Object[]> getMessages() {
		return this.messages;
	}

	public ArrayList<Agent> getAgents() {
		return this.agents;
	}
}
