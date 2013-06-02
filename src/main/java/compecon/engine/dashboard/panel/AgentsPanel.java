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

package compecon.engine.dashboard.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.AbstractListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import compecon.engine.jmx.Log;
import compecon.engine.jmx.model.AgentLogsModel;
import compecon.engine.jmx.model.Model.IModelListener;
import compecon.engine.jmx.model.ModelRegistry;

public class AgentsPanel extends JPanel {

	public class AgentLogsTableModel extends AbstractTableModel implements
			IModelListener {

		protected final String columnNames[] = { "Date", "Message" };

		public AgentLogsTableModel() {
			ModelRegistry.getAgentLogsModel().registerListener(this);
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return AgentsPanel.this.agentLogsModel.getMessages().size();
		}

		@Override
		public Object getValueAt(int rowIndex, int colIndex) {
			Object[] rowContent = (Object[]) new ArrayList<Object[]>(
					AgentsPanel.this.agentLogsModel.getMessages())
					.get(rowIndex);
			return rowContent[colIndex];
		}

		@Override
		public String getColumnName(int columnIndex) {
			return this.columnNames[columnIndex];
		}

		@Override
		public void notifyListener() {
			if (!AgentsPanel.this.noRefresh) {
				if (Log.getAgentSelectedByClient() != null)
					this.fireTableDataChanged();
			}
		}
	}

	public class AgentLogsListModel extends AbstractListModel implements
			IModelListener {

		public AgentLogsListModel() {
			ModelRegistry.getAgentLogsModel().registerListener(this);
		}

		@Override
		public Object getElementAt(int index) {
			return AgentsPanel.this.agentLogsModel.getAgents().get(index);
		}

		@Override
		public int getSize() {
			return Math.min(NUMBER_OF_AGENTS_TO_SHOW,
					AgentsPanel.this.agentLogsModel.getAgents().size());
		}

		@Override
		public void notifyListener() {
			this.fireContentsChanged(this, 0, AgentsPanel.this.agentLogsModel
					.getAgents().size());
		}
	}

	protected final int NUMBER_OF_AGENTS_TO_SHOW = 200;

	protected final AgentLogsModel agentLogsModel = ModelRegistry
			.getAgentLogsModel();

	protected AgentLogsListModel agentLogsListModel = new AgentLogsListModel();

	protected AgentLogsTableModel agentLogsTableModel = new AgentLogsTableModel();

	protected boolean noRefresh = true;

	protected final JList agentsList;

	public AgentsPanel() {
		setLayout(new BorderLayout());

		/*
		 * agents list
		 */
		agentsList = new JList(this.agentLogsListModel);
		agentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		agentsList.setLayoutOrientation(JList.VERTICAL);
		agentsList.setVisibleRowCount(-1);
		agentsList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					AgentsPanel.this.agentLogsModel.setCurrentAgent(agentsList
							.getSelectedIndex());
				}
			}
		});
		JScrollPane agentsListScroller = new JScrollPane(agentsList);
		agentsListScroller.setPreferredSize(new Dimension(250, 80));
		this.add(agentsListScroller, BorderLayout.WEST);

		/*
		 * agent details table
		 */
		JTable agentDetailsTable = new JTable(this.agentLogsTableModel);
		JScrollPane agentDetailsTablePane = new JScrollPane(agentDetailsTable);
		this.add(agentDetailsTablePane, BorderLayout.CENTER);

		setVisible(true);

		/*
		 * controls
		 */
		JPanel controlPanel = new JPanel();
		JCheckBox showTransactionsCheckBox = new JCheckBox(
				"Log Monetary Transactions", false);
		showTransactionsCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent
						.getSource();
				boolean selected = abstractButton.getModel().isSelected();
				Log.setLogTransactions(selected);
			}
		});
		controlPanel.add(showTransactionsCheckBox);
		this.add(controlPanel, BorderLayout.NORTH);
	}

	public void setNoRefresh(boolean noRefresh) {
		this.noRefresh = noRefresh;
	}
}
