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

package compecon.engine.dashboard.panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import compecon.engine.Agent;
import compecon.engine.jmx.model.Model.IModelListener;
import compecon.engine.jmx.model.ModelRegistry;

public class NumberOfAgentsPanel extends JPanel {
	public class NumberOfAgentsTableModel extends AbstractTableModel implements
			IModelListener {

		String columnNames[] = { "Agent Type", "#" };

		List<Class<? extends Agent>> rowNumbersOfAgents = new ArrayList<Class<? extends Agent>>();

		public NumberOfAgentsTableModel() {
			ModelRegistry.getNumberOfAgentsModel().registerListener(this);
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return ModelRegistry.getNumberOfAgentsModel().getNumberOfAgents()
					.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (col == 0)
				return this.getAgentTypeAtRowNumber(row).getSimpleName();
			else
				return ModelRegistry.getNumberOfAgentsModel()
						.getNumberOfAgents()
						.get(this.getAgentTypeAtRowNumber(row));
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

		@Override
		public void notifyListener() {
			for (Entry<Class<? extends Agent>, Integer> entry : ModelRegistry
					.getNumberOfAgentsModel().getNumberOfAgents().entrySet()) {
				// store position in table
				if (!this.rowNumbersOfAgents.contains(entry.getKey()))
					this.rowNumbersOfAgents.add(entry.getKey());
			}

			this.fireTableDataChanged();
		}
	}

	public NumberOfAgentsPanel() {
		JScrollPane numberOfAgentsPane = new JScrollPane(new JTable(
				new NumberOfAgentsTableModel()));
		this.add(numberOfAgentsPane);
	}
}
