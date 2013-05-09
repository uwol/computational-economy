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

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import compecon.engine.Log;
import compecon.engine.dashboard.model.AgentLogsModel;

public class AgentsPanel extends JPanel {
	protected final AgentLogsModel agentLogsModel;

	protected final JList agentsList;

	public AgentsPanel(AgentLogsModel agentLogsModel) {
		setLayout(new BorderLayout());

		this.agentLogsModel = agentLogsModel;

		/*
		 * agents list
		 */
		agentsList = new JList(this.agentLogsModel.getAgentLogsListModel());
		agentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		agentsList.setLayoutOrientation(JList.VERTICAL);
		agentsList.setVisibleRowCount(-1);
		agentsList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					AgentsPanel.this.agentLogsModel
							.setCurrentAgent(agentsList.getSelectedIndex());
				}
			}
		});
		JScrollPane agentsListScroller = new JScrollPane(agentsList);
		agentsListScroller.setPreferredSize(new Dimension(250, 80));
		this.add(agentsListScroller, BorderLayout.WEST);

		/*
		 * agent details table
		 */
		JTable agentDetailsTable = new JTable(
				this.agentLogsModel.getAgentLogsTableModel());
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
}
