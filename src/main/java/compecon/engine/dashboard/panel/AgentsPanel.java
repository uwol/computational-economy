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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractListModel;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.Currency;
import compecon.engine.Agent;
import compecon.engine.jmx.Log;
import compecon.engine.jmx.model.AgentDetailModel;
import compecon.engine.jmx.model.AgentDetailModel.AgentLog;
import compecon.engine.jmx.model.Model.IModelListener;
import compecon.engine.jmx.model.ModelRegistry;
import compecon.engine.util.MathUtil;
import compecon.nature.materia.GoodType;

public class AgentsPanel extends JPanel {

	public class AgentListModel extends AbstractListModel<Agent> implements
			IModelListener {

		public AgentListModel() {
			ModelRegistry.getAgentDetailModel().registerListener(this);
		}

		@Override
		public Agent getElementAt(int index) {
			return AgentsPanel.this.agentDetailModel.getAgents().get(index);
		}

		@Override
		public int getSize() {
			return Math.min(NUMBER_OF_AGENTS_TO_SHOW,
					AgentsPanel.this.agentDetailModel.getAgents().size());
		}

		@Override
		public void notifyListener() {
			this.fireContentsChanged(this, 0, AgentsPanel.this.agentDetailModel
					.getAgents().size());
		}
	}

	public class AgentLogSelectionModel extends DefaultComboBoxModel<AgentLog> {
		@Override
		public int getSize() {
			return ModelRegistry.getAgentDetailModel().getLogsOfCurrentAgent()
					.size();
		}

		@Override
		public AgentLog getElementAt(int i) {
			return ModelRegistry.getAgentDetailModel().getLogsOfCurrentAgent()
					.get(i);
		}
	}

	public class AgentLogsTableModel extends AbstractTableModel implements
			IModelListener {

		protected final String columnNames[] = { "Message" };

		public AgentLogsTableModel() {
			ModelRegistry.getAgentDetailModel().registerListener(this);
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			// -5, so that concurrent changes in the messages queue do not
			// produce exceptions
			return Math.max(AgentsPanel.this.agentDetailModel.getCurrentLog()
					.getRows().size() - 5, 0);
		}

		@Override
		public Object getValueAt(int rowIndex, int colIndex) {
			return AgentsPanel.this.agentDetailModel.getCurrentLog().getRows()
					.get(rowIndex);
		}

		@Override
		public String getColumnName(int columnIndex) {
			return this.columnNames[columnIndex];
		}

		@Override
		public void notifyListener() {
			if (AgentsPanel.this.refresh) {
				if (Log.getAgentSelectedByClient() != null)
					this.fireTableDataChanged();
			}
		}
	}

	public class AgentBankAccountsTableModel extends AbstractTableModel
			implements IModelListener {

		protected final String columnNames[] = { "Name", "Balance", "Currency" };

		public AgentBankAccountsTableModel() {
			ModelRegistry.getAgentDetailModel().registerListener(this);
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return AgentsPanel.this.agentDetailModel
					.getBankAccountsOfCurrentAgent().size();
		}

		@Override
		public Object getValueAt(int rowIndex, int colIndex) {
			BankAccount bankAccount = AgentsPanel.this.agentDetailModel
					.getBankAccountsOfCurrentAgent().get(rowIndex);

			switch (colIndex) {
			case 0:
				return bankAccount.getName() + " [" + bankAccount.getId() + "]";
			case 1:
				return Currency.formatMoneySum(bankAccount.getBalance());
			case 2:
				return bankAccount.getCurrency();
			default:
				return null;
			}
		}

		@Override
		public String getColumnName(int columnIndex) {
			return this.columnNames[columnIndex];
		}

		@Override
		public void notifyListener() {
			if (AgentsPanel.this.refresh) {
				if (Log.getAgentSelectedByClient() != null)
					this.fireTableDataChanged();
			}
		}
	}

	public class AgentGoodsTableModel extends AbstractTableModel implements
			IModelListener {

		protected final String columnNames[] = { "Name", "Balance" };

		public AgentGoodsTableModel() {
			ModelRegistry.getAgentDetailModel().registerListener(this);
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return AgentsPanel.this.agentDetailModel.getGoodsOfCurrentAgent()
					.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int colIndex) {
			GoodType goodType = GoodType.values()[rowIndex];
			double balance = AgentsPanel.this.agentDetailModel
					.getGoodsOfCurrentAgent().get(goodType);

			switch (colIndex) {
			case 0:
				return goodType.toString();
			case 1:
				return MathUtil.round(balance);
			default:
				return null;
			}
		}

		@Override
		public String getColumnName(int columnIndex) {
			return this.columnNames[columnIndex];
		}

		@Override
		public void notifyListener() {
			if (AgentsPanel.this.refresh) {
				if (Log.getAgentSelectedByClient() != null)
					this.fireTableDataChanged();
			}
		}
	}

	public class AgentPropertyTableModel extends AbstractTableModel implements
			IModelListener {

		protected final String columnNames[] = { "Name" };

		public AgentPropertyTableModel() {
			ModelRegistry.getAgentDetailModel().registerListener(this);
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public int getRowCount() {
			return AgentsPanel.this.agentDetailModel
					.getPropertiesOfCurrentAgent().size();
		}

		@Override
		public Object getValueAt(int rowIndex, int colIndex) {
			switch (colIndex) {
			case 0:
				return AgentsPanel.this.agentDetailModel
						.getPropertiesOfCurrentAgent().get(rowIndex);
			default:
				return null;
			}
		}

		@Override
		public String getColumnName(int columnIndex) {
			return this.columnNames[columnIndex];
		}

		@Override
		public void notifyListener() {
			if (AgentsPanel.this.refresh) {
				if (Log.getAgentSelectedByClient() != null)
					this.fireTableDataChanged();
			}
		}
	}

	protected final int NUMBER_OF_AGENTS_TO_SHOW = 300;

	protected final AgentDetailModel agentDetailModel = ModelRegistry
			.getAgentDetailModel();

	protected AgentListModel agentLogsListModel = new AgentListModel();

	protected AgentLogSelectionModel agentLogSelectionModel = new AgentLogSelectionModel();

	protected AgentLogsTableModel agentLogsTableModel = new AgentLogsTableModel();

	protected AgentBankAccountsTableModel agentBankAccountsTableModel = new AgentBankAccountsTableModel();

	protected AgentGoodsTableModel agentGoodsTableModel = new AgentGoodsTableModel();

	protected AgentPropertyTableModel agentPropertyTableModel = new AgentPropertyTableModel();

	protected boolean refresh = false;

	protected final JList<Agent> agentsList;

	public AgentsPanel() {
		setLayout(new BorderLayout());

		/*
		 * controls
		 */
		JPanel controlPanel = new JPanel(new FlowLayout());
		final JComboBox<AgentLog> logSelection = new JComboBox<AgentLog>(
				agentLogSelectionModel);
		logSelection.setPreferredSize(new Dimension(400, 30));
		logSelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ModelRegistry.getAgentDetailModel().setCurrentLog(
						(AgentLog) logSelection.getSelectedItem());
			}
		});
		controlPanel.add(logSelection);
		this.add(controlPanel, BorderLayout.NORTH);

		/*
		 * agents list
		 */
		agentsList = new JList<Agent>(this.agentLogsListModel);
		agentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		agentsList.setLayoutOrientation(JList.VERTICAL);
		agentsList.setVisibleRowCount(-1);
		agentsList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					AgentsPanel.this.agentDetailModel
							.setCurrentAgent(agentsList.getSelectedIndex());
				}
			}
		});
		JScrollPane agentsListScroller = new JScrollPane(agentsList);
		agentsListScroller.setPreferredSize(new Dimension(250, 80));
		this.add(agentsListScroller, BorderLayout.WEST);

		/*
		 * agent detail panel
		 */
		JPanel agentDetailPanel = new JPanel();
		agentDetailPanel.setLayout(new BoxLayout(agentDetailPanel,
				BoxLayout.PAGE_AXIS));
		this.add(agentDetailPanel, BorderLayout.CENTER);

		// table with log entries for the agent
		JTable agentLogTable = new JTable(this.agentLogsTableModel);
		JScrollPane agentLogTablePane = new JScrollPane(agentLogTable);
		agentDetailPanel.add(agentLogTablePane);

		// panel for money and goods owned by this agent
		JPanel agentBankAccountsAndGoodsPanel = new JPanel(new GridLayout(0, 3));
		agentBankAccountsAndGoodsPanel.setPreferredSize(new Dimension(-1, 150));
		agentDetailPanel.add(agentBankAccountsAndGoodsPanel);

		// agent bank account table
		final JTable agentBankAccountsTable = new JTable(
				this.agentBankAccountsTableModel);
		JScrollPane agentBankAccountsTablePane = new JScrollPane(
				agentBankAccountsTable);
		agentBankAccountsAndGoodsPanel.add(agentBankAccountsTablePane);

		// agent goods table
		JTable agentGoodsTable = new JTable(this.agentGoodsTableModel);
		JScrollPane agentGoodsTablePane = new JScrollPane(agentGoodsTable);
		agentBankAccountsAndGoodsPanel.add(agentGoodsTablePane);

		// agent property table
		JTable agentPropertyTable = new JTable(this.agentPropertyTableModel);
		JScrollPane agentPropertyTablePane = new JScrollPane(agentPropertyTable);
		agentBankAccountsAndGoodsPanel.add(agentPropertyTablePane);

		setVisible(true);
	}

	public void setRefresh(boolean refresh) {
		this.refresh = refresh;
	}
}
