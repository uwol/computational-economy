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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import compecon.economy.sectors.Agent;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.engine.AgentFactory;
import compecon.engine.Simulation;
import compecon.engine.dashboard.panel.BalanceSheetPanel.BalanceSheetTableModel;
import compecon.engine.statistics.model.NotificationListenerModel.IModelListener;
import compecon.engine.statistics.model.accumulator.PeriodDataAccumulator;

public class NationalAccountsPanel extends AbstractChartsPanel implements
		IModelListener {

	public class NationalAccountsPanelForCurrency extends JPanel implements
			IModelListener {

		public class MonetaryTransactionsTableModel extends AbstractTableModel
				implements IModelListener {

			public final Currency referenceCurrency;

			protected Object[][] transientTableData = new Object[AgentFactory.agentTypes
					.size()][AgentFactory.agentTypes.size() + 1];

			public MonetaryTransactionsTableModel(Currency referenceCurrency) {
				this.referenceCurrency = referenceCurrency;
				Simulation.getInstance().getModelRegistry()
						.getNationalEconomyModel(referenceCurrency).monetaryTransactionsModel
						.registerListener(this);
			}

			@Override
			public int getColumnCount() {
				return transientTableData[0].length;
			}

			@Override
			public int getRowCount() {
				return transientTableData.length;
			}

			@Override
			public Object getValueAt(int rowIndex, int colIndex) {
				return this.transientTableData[rowIndex][colIndex];
			}

			@Override
			public String getColumnName(int columnIndex) {
				if (columnIndex == 0)
					return "from / to";
				return AgentFactory.agentTypes.get(columnIndex - 1)
						.getSimpleName();
			}

			@Override
			public void notifyListener() {
				// source data model
				Map<Class<? extends Agent>, Map<Class<? extends Agent>, PeriodDataAccumulator>> adjacencyMatrixForCurrency = Simulation
						.getInstance().getModelRegistry()
						.getNationalEconomyModel(referenceCurrency).monetaryTransactionsModel
						.getAdjacencyMatrix();

				// for all agent types as sources of monetary transactions
				// -> rows
				for (int i = 0; i < AgentFactory.agentTypes.size(); i++) {
					Class<? extends Agent> agentTypeFrom = AgentFactory.agentTypes
							.get(i);
					Map<Class<? extends Agent>, PeriodDataAccumulator> adjacencyMatrixForCurrencyAndFromAgentType = adjacencyMatrixForCurrency
							.get(agentTypeFrom);

					// row name
					this.transientTableData[i][0] = agentTypeFrom
							.getSimpleName();

					// for all agent types as destinations of monetary
					// transactions
					// ->
					// columns
					for (int j = 0; j < AgentFactory.agentTypes.size(); j++) {
						Class<? extends Agent> agentTypeTo = AgentFactory.agentTypes
								.get(j);
						PeriodDataAccumulator periodTransactionVolume = adjacencyMatrixForCurrencyAndFromAgentType
								.get(agentTypeTo);
						this.transientTableData[i][j + 1] = Currency
								.formatMoneySum(periodTransactionVolume
										.getAmount());
						periodTransactionVolume.reset();
					}
				}
				this.fireTableDataChanged();
			}
		}

		protected final Currency currency;

		// stores transaction values compatible to JTables, is filled each
		// period from adjacencyMatrix
		protected final MonetaryTransactionsTableModel monetaryTransactionsTableModel;

		public NationalAccountsPanelForCurrency(Currency currency) {
			this.currency = currency;
			this.monetaryTransactionsTableModel = new MonetaryTransactionsTableModel(
					currency);

			this.setLayout(new GridLayout(0, 3));

			// balance sheets
			this.add(createNationalBalanceSheetPanel(currency));
			this.add(createHouseholdBalanceSheetPanel(currency));
			this.add(createFactoryBalanceSheetPanel(currency));
			this.add(createTraderBalanceSheetPanel(currency));
			this.add(createCreditBankBalanceSheetPanel(currency));
			this.add(createCentralBankBalanceSheetPanel(currency));
			this.add(createStateBalanceSheetPanel(currency));

			// panel for monetary transactions
			JTable monetaryTransactionsTable = new JTable(
					monetaryTransactionsTableModel);
			JScrollPane monetaryTransactionsJScrollPane = new JScrollPane(
					monetaryTransactionsTable);
			monetaryTransactionsJScrollPane.setPreferredSize(new Dimension(-1,
					250));

			JPanel monetaryTransactionsTablePanel = new JPanel();
			monetaryTransactionsTablePanel.setBorder(BorderFactory
					.createTitledBorder(
							BorderFactory.createEtchedBorder(),
							"Monetary Transactions in "
									+ currency.getIso4217Code(),
							TitledBorder.CENTER, TitledBorder.TOP));
			monetaryTransactionsTablePanel.setLayout(new BorderLayout());
			monetaryTransactionsTablePanel.add(monetaryTransactionsJScrollPane);
			this.add(monetaryTransactionsTablePanel);
		}

		@Override
		public void notifyListener() {
		}
	}

	protected JTabbedPane jTabbedPaneCurrency = new JTabbedPane();

	public NationalAccountsPanel() {
		this.setLayout(new BorderLayout());

		for (Currency currency : Currency.values()) {
			NationalAccountsPanelForCurrency panelForCurrency = new NationalAccountsPanelForCurrency(
					currency);
			jTabbedPaneCurrency.addTab(currency.getIso4217Code(),
					panelForCurrency);
			panelForCurrency.setBackground(Color.lightGray);
		}

		jTabbedPaneCurrency.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (e.getSource() instanceof JTabbedPane) {
					JTabbedPane pane = (JTabbedPane) e.getSource();
					NationalAccountsPanelForCurrency selectedComponent = (NationalAccountsPanelForCurrency) pane
							.getSelectedComponent();
					selectedComponent.notifyListener();
				}
			}
		});

		add(jTabbedPaneCurrency, BorderLayout.CENTER);
	}

	protected JPanel createNationalBalanceSheetPanel(final Currency currency) {
		final BalanceSheetTableModel balanceSheetTableModel = new BalanceSheetTableModel(
				currency) {
			@Override
			protected BalanceSheet getBalanceSheet() {
				return Simulation.getInstance().getModelRegistry()
						.getNationalEconomyModel(currency).balanceSheetsModel
						.getNationalAccountsBalanceSheet();
			}
		};
		return new BalanceSheetPanel(currency, balanceSheetTableModel,
				"National Balance Sheet for " + currency.getIso4217Code());
	}

	@Override
	public void notifyListener() {
		if (this.isShowing()) {
			NationalAccountsPanelForCurrency accountsPanel = (NationalAccountsPanelForCurrency) jTabbedPaneCurrency
					.getSelectedComponent();
			accountsPanel.notifyListener();
		}
	}
}
