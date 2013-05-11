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
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.AbstractListModel;
import javax.swing.table.AbstractTableModel;

import compecon.engine.Agent;
import compecon.engine.Log;

public class AgentLogsModel {

	public class AgentLogsTableModel extends AbstractTableModel {

		protected final String columnNames[] = { "Date", "Message" };

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return AgentLogsModel.this.messages.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int colIndex) {
			Object[] rowContent = (Object[]) new ArrayList<Object[]>(
					AgentLogsModel.this.messages).get(rowIndex);
			return rowContent[colIndex];
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

	protected Queue<Object[]> messages = new LinkedList<Object[]>();

	protected ArrayList<Agent> agents = new ArrayList<Agent>();

	protected AgentLogsListModel agentLogsListModel = new AgentLogsListModel();

	protected AgentLogsTableModel agentLogsTableModel = new AgentLogsTableModel();

	protected boolean noRefresh = true;

	public void agent_onConstruct(Agent agent) {
		this.agents.add(agent);
		this.agentLogsListModel.onListModification(this.agents.indexOf(agent));
	}

	public void agent_onDeconstruct(Agent agent) {
		int index = this.agents.indexOf(agent);
		this.messages.remove(agent);
		this.agents.remove(agent);
		this.agentLogsListModel.onListModification(index);
	}

	public void logAgentEvent(Date date, String message) {
		messages.add(new Object[] { date, message });
		if (messages.size() > MESSAGES_TO_STORE)
			messages.poll();
	}

	public void setCurrentAgent(Agent agent) {
		Log.setAgentSelectedByClient(agent);
		this.messages.clear();
		this.signalizeContentModification();
	}

	public void setCurrentAgent(Integer agentId) {
		this.setCurrentAgent(this.agents.get(agentId));
	}

	public void signalizeContentModification() {
		if (!this.noRefresh) {
			if (Log.getAgentSelectedByClient() != null)
				this.agentLogsTableModel.fireTableDataChanged();
		}
	}

	public AgentLogsListModel getAgentLogsListModel() {
		return this.agentLogsListModel;
	}

	public AgentLogsTableModel getAgentLogsTableModel() {
		return this.agentLogsTableModel;
	}

	public void noRefresh(boolean noRefresh) {
		this.noRefresh = noRefresh;
	}
}
