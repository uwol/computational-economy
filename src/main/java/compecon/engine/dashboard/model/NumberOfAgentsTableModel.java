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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import compecon.engine.Agent;

public class NumberOfAgentsTableModel extends AbstractTableModel {

	String columnNames[] = { "Agent Type", "#" };

	Map<Class<? extends Agent>, Integer> numberOfAgents = new HashMap<Class<? extends Agent>, Integer>();
	List<Class<? extends Agent>> rowNumbersOfAgents = new ArrayList<Class<? extends Agent>>();

	public void agent_onConstruct(Class<? extends Agent> agentType) {
		// store number of agents
		int numberOfAgentsForAgentType = 0;
		if (this.numberOfAgents.containsKey(agentType))
			numberOfAgentsForAgentType = this.numberOfAgents.get(agentType);
		numberOfAgentsForAgentType++;
		this.numberOfAgents.put(agentType, numberOfAgentsForAgentType);

		// store position in table
		if (!this.rowNumbersOfAgents.contains(agentType))
			this.rowNumbersOfAgents.add(agentType);

		fireTableCellUpdated(this.getRowNumberOfAgentType(agentType), 0);
		fireTableCellUpdated(this.getRowNumberOfAgentType(agentType), 1);
	}

	public void agent_onDeconstruct(Class<? extends Agent> agentType) {
		int numberOfAgentsForAgentType = 0;
		if (this.numberOfAgents.containsKey(agentType))
			numberOfAgentsForAgentType = this.numberOfAgents.get(agentType);
		numberOfAgentsForAgentType--;
		this.numberOfAgents.put(agentType, numberOfAgentsForAgentType);

		fireTableCellUpdated(this.getRowNumberOfAgentType(agentType), 0);
		fireTableCellUpdated(this.getRowNumberOfAgentType(agentType), 1);
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return this.numberOfAgents.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (col == 0)
			return this.getAgentTypeAtRowNumber(row).getSimpleName();
		else
			return this.numberOfAgents.get(this.getAgentTypeAtRowNumber(row));
	}

	@Override
	public String getColumnName(int column) {
		return this.columnNames[column];
	}

	private int getRowNumberOfAgentType(Class<? extends Agent> agentType) {
		return this.rowNumbersOfAgents.indexOf(agentType);
	}

	private Class<? extends Agent> getAgentTypeAtRowNumber(int rowNumber) {
		return this.rowNumbersOfAgents.get(rowNumber);
	}
}
