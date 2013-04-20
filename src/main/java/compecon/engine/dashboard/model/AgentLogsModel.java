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

package compecon.engine.dashboard.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.swing.AbstractListModel;
import javax.swing.table.AbstractTableModel;

import compecon.engine.Agent;

public class AgentLogsModel {

	public class AgentLogsTableModel extends AbstractTableModel {

		protected final String columnNames[] = { "Date", "Message" };

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			if (AgentLogsModel.this.messages
					.containsKey(AgentLogsModel.this.currentAgent)) {
				Queue<Object[]> messages = AgentLogsModel.this.messages
						.get(AgentLogsModel.this.currentAgent);
				return messages.size();
			}
			return 0;
		}

		@Override
		public Object getValueAt(int rowIndex, int colIndex) {
			if (AgentLogsModel.this.messages
					.containsKey(AgentLogsModel.this.currentAgent)) {
				ArrayList<Object[]> messagesOfAgent = new ArrayList<Object[]>(
						AgentLogsModel.this.messages
								.get(AgentLogsModel.this.currentAgent));
				Object[] rowContent = (Object[]) messagesOfAgent.get(rowIndex);
				return rowContent[colIndex];
			}
			return null;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return this.columnNames[columnIndex];
		}
	}

	public class AgentLogsListModel extends AbstractListModel {
		@Override
		public Object getElementAt(int index) {
			return AgentLogsModel.this.agents.get(index);
		}

		@Override
		public int getSize() {
			return Math.min(MESSAGES_TO_STORE,
					AgentLogsModel.this.agents.size());
		}

		public void onListModification(int id) {
			this.fireContentsChanged(this, 0, AgentLogsModel.this.agents.size());
		}
	}

	protected final int MESSAGES_TO_STORE = 100;

	protected Map<Agent, Queue<Object[]>> messages = new HashMap<Agent, Queue<Object[]>>();

	protected ArrayList<Agent> agents = new ArrayList<Agent>();

	protected Agent currentAgent;

	protected AgentLogsListModel agentLogsListModel = new AgentLogsListModel();

	protected AgentLogsTableModel agentLogsTableModel = new AgentLogsTableModel();

	protected boolean blockRefresh = true;

	public void agent_onDeconstruct(Agent agent) {
		int index = this.agents.indexOf(agent);
		this.messages.remove(agent);
		this.agents.remove(agent);
		this.agentLogsListModel.onListModification(index);
	}

	public void logAgentEvent(Date date, Agent agent, String message) {
		if (!this.messages.containsKey(agent)) {
			this.messages.put(agent, new LinkedList<Object[]>());
			this.agents.add(agent);
			this.agentLogsListModel.onListModification(this.agents
					.indexOf(agent));
		}
		Queue<Object[]> messagesForAgent = this.messages.get(agent);
		messagesForAgent.add(new Object[] { date, message });
		if (messagesForAgent.size() > MESSAGES_TO_STORE)
			messagesForAgent.poll();
	}

	public void setCurrentAgent(Agent agent) {
		this.currentAgent = agent;
		this.signalizeContentModification();
	}

	public void setCurrentAgentId(Integer agentId) {
		this.currentAgent = this.agents.get(agentId);
		this.signalizeContentModification();
	}

	public void signalizeContentModification() {
		if (!this.blockRefresh)
			if (this.currentAgent != null)
				this.agentLogsTableModel.fireTableDataChanged();
	}

	public AgentLogsListModel getAgentLogsListModel() {
		return this.agentLogsListModel;
	}

	public AgentLogsTableModel getAgentLogsTableModel() {
		return this.agentLogsTableModel;
	}

	public void blockRefresh(boolean blockRefresh) {
		this.blockRefresh = blockRefresh;
	}
}
