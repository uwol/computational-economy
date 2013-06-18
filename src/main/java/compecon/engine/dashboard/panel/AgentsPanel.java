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
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
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
import compecon.engine.jmx.model.Model.IModelListener;
import compecon.engine.jmx.model.ModelRegistry;

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
			return AgentsPanel.this.agentDetailModel
					.getMessagesOfCurrentAgent().size() - 5;
		}

		@Override
		public Object getValueAt(int rowIndex, int colIndex) {
			return new ArrayList<String>(
					AgentsPanel.this.agentDetailModel
							.getMessagesOfCurrentAgent()).get(rowIndex);
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
				return bankAccount.getName();
			case 1:
				return Currency.round(bankAccount.getBalance());
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
			if (!AgentsPanel.this.noRefresh) {
				if (Log.getAgentSelectedByClient() != null)
					this.fireTableDataChanged();
			}
		}
	}

	protected final int NUMBER_OF_AGENTS_TO_SHOW = 200;

	protected final AgentDetailModel agentDetailModel = ModelRegistry
			.getAgentDetailModel();

	protected AgentListModel agentLogsListModel = new AgentListModel();

	protected AgentLogsTableModel agentLogsTableModel = new AgentLogsTableModel();

	protected AgentBankAccountsTableModel agentBankAccountsTableModel = new AgentBankAccountsTableModel();

	protected boolean noRefresh = true;

	protected final JList<Agent> agentsList;

	public AgentsPanel() {
		setLayout(new BorderLayout());

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

		// agent log table
		JTable agentLogTable = new JTable(this.agentLogsTableModel);
		JScrollPane agentLogTablePane = new JScrollPane(agentLogTable);
		agentDetailPanel.add(agentLogTablePane);

		// agent bank account table
		JTable agentBankAccountsTable = new JTable(
				this.agentBankAccountsTableModel);
		JScrollPane agentBankAccountsTablePane = new JScrollPane(
				agentBankAccountsTable);
		agentBankAccountsTablePane.setPreferredSize(new Dimension(-1, 150));
		agentDetailPanel.add(agentBankAccountsTablePane);

		setVisible(true);
	}

	public void setNoRefresh(boolean noRefresh) {
		this.noRefresh = noRefresh;
	}
}
