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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.engine.Agent;
import compecon.engine.AgentFactory;
import compecon.engine.jmx.model.Model.IModelListener;
import compecon.engine.jmx.model.ModelRegistry;
import compecon.engine.jmx.model.generic.PeriodDataAccumulator;
import compecon.engine.jmx.model.generic.PeriodDataAccumulatorSet;
import compecon.nature.materia.GoodType;

public class NationalAccountsPanel extends JPanel {

	public class NationalAccountsTableModel extends AbstractTableModel
			implements IModelListener {

		public final Currency referenceCurrency;

		public final static int SIDE_ACTIVE = 0;
		public final static int SIDE_PASSIVE = 5;

		public final static int POSITION_HARD_CASH = 0;
		public final static int POSITION_CASH_SHORT_TERM = 1;
		public final static int POSITION_CASH_LONG_TERM = 2;
		public final static int POSITION_BONDS = 3;
		public final static int POSITION_BANK_LOANS = 4;
		public final static int STARTPOSITION_GOODTYPES = 6;

		public final static int POSITION_LOANS = 1;
		public final static int POSITION_FIN_LIABLITIES = 3;
		public final static int POSITION_BANK_BORROWINGS = 4;

		protected final String columnNames[] = { "Active Account",
				"Agent Type", "Value", "Currency", "", "Passive Account",
				"Agent Type", "Value", "Currency" };

		protected String[] activePositionNames;

		protected String[] passivePositionNames;

		protected Object[][] cells;

		public NationalAccountsTableModel(Currency referenceCurrency) {
			this.referenceCurrency = referenceCurrency;
			ModelRegistry.getBalanceSheetsModel().registerListener(this);

			this.activePositionNames = new String[STARTPOSITION_GOODTYPES
					+ GoodType.values().length];
			this.passivePositionNames = new String[STARTPOSITION_GOODTYPES
					+ GoodType.values().length];

			this.activePositionNames[POSITION_HARD_CASH] = "Hard Cash";
			this.activePositionNames[POSITION_CASH_SHORT_TERM] = "Cash Short Term";
			this.activePositionNames[POSITION_CASH_LONG_TERM] = "Cash Long Term";
			this.activePositionNames[POSITION_BONDS] = "Fin. Assets (Bonds)";
			this.activePositionNames[POSITION_BANK_LOANS] = "Bank Loans";

			this.passivePositionNames[POSITION_LOANS] = "Loans";
			this.passivePositionNames[POSITION_FIN_LIABLITIES] = "Fin. Liabilities (Bonds)";
			this.passivePositionNames[POSITION_BANK_BORROWINGS] = "Bank Borrowings";

			for (GoodType goodType : GoodType.values()) {
				int position = STARTPOSITION_GOODTYPES + goodType.ordinal();
				this.activePositionNames[position] = goodType.name();
			}

			this.cells = new Object[this.getRowCount()][this.getColumnCount()];

			for (int i = 0; i < Math.max(this.activePositionNames.length,
					this.passivePositionNames.length); i++) {
				// position name
				cells[i * AgentFactory.agentTypes.size()][SIDE_ACTIVE] = this.activePositionNames[i];
				cells[i * AgentFactory.agentTypes.size()][SIDE_PASSIVE] = this.passivePositionNames[i];

				for (int j = 0; j < AgentFactory.agentTypes.size(); j++) {
					if (this.activePositionNames[i] != null) {
						// agent type name
						cells[i * AgentFactory.agentTypes.size() + j][SIDE_ACTIVE + 1] = AgentFactory.agentTypes
								.get(j).getSimpleName();

						if (i < STARTPOSITION_GOODTYPES)
							// currency name
							cells[i * AgentFactory.agentTypes.size() + j][SIDE_ACTIVE + 3] = this.referenceCurrency
									.getIso4217Code();
						else
							// unit
							cells[i * AgentFactory.agentTypes.size() + j][SIDE_ACTIVE + 3] = "Units";
					}
					if (this.passivePositionNames[i] != null) {
						// agent type name
						cells[i * AgentFactory.agentTypes.size() + j][SIDE_PASSIVE + 1] = AgentFactory.agentTypes
								.get(j).getSimpleName();
						// currency name
						cells[i * AgentFactory.agentTypes.size() + j][SIDE_PASSIVE + 3] = this.referenceCurrency
								.getIso4217Code();
					}
				}
			}
		}

		@Override
		public int getColumnCount() {
			return this.columnNames.length;
		}

		@Override
		public int getRowCount() {
			return AgentFactory.agentTypes.size()
					* Math.max(this.activePositionNames.length,
							this.passivePositionNames.length);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return this.cells[rowIndex][columnIndex];
		}

		@Override
		public String getColumnName(int columnIndex) {
			return this.columnNames[columnIndex];
		}

		public void setValue(final int sideNr, final int positionTypeNr,
				final int agentTypeNr, final Currency currency,
				final double value) {
			if (!currency.equals(this.referenceCurrency))
				return;

			int rowNumber = calculateRowNumber(positionTypeNr, agentTypeNr);
			this.cells[rowNumber][sideNr + 2] = Currency.round(value);
			fireTableCellUpdated(rowNumber, sideNr + 2);
		}

		public int calculateRowNumber(final int positionTypeNr,
				final int agentTypeNr) {
			return (positionTypeNr * AgentFactory.agentTypes.size())
					+ agentTypeNr;
		}

		@Override
		public void notifyListener() {
			Map<Class<? extends Agent>, BalanceSheet> balanceSheetsForAgentTypes = ModelRegistry
					.getBalanceSheetsModel().getNationalAccountsBalanceSheets()
					.get(this.referenceCurrency);

			for (Entry<Class<? extends Agent>, BalanceSheet> balanceSheetEntry : balanceSheetsForAgentTypes
					.entrySet()) {
				Class<? extends Agent> agentType = balanceSheetEntry.getKey();
				BalanceSheet balanceSheet = balanceSheetEntry.getValue();

				int agentTypeNr = AgentFactory.agentTypes.indexOf(agentType);

				// active
				this.setValue(NationalAccountsTableModel.SIDE_ACTIVE,
						NationalAccountsTableModel.POSITION_HARD_CASH,
						agentTypeNr, referenceCurrency,
						Currency.round(balanceSheet.hardCash));
				this.setValue(NationalAccountsTableModel.SIDE_ACTIVE,
						NationalAccountsTableModel.POSITION_CASH_SHORT_TERM,
						agentTypeNr, referenceCurrency,
						Currency.round(balanceSheet.cashShortTerm));
				this.setValue(NationalAccountsTableModel.SIDE_ACTIVE,
						NationalAccountsTableModel.POSITION_CASH_LONG_TERM,
						agentTypeNr, referenceCurrency,
						Currency.round(balanceSheet.cashLongTerm));
				this.setValue(NationalAccountsTableModel.SIDE_ACTIVE,
						NationalAccountsTableModel.POSITION_BONDS, agentTypeNr,
						referenceCurrency, Currency.round(balanceSheet.bonds));
				this.setValue(NationalAccountsTableModel.SIDE_ACTIVE,
						NationalAccountsTableModel.POSITION_BANK_LOANS,
						agentTypeNr, referenceCurrency,
						Currency.round(balanceSheet.bankLoans));

				for (GoodType goodType : GoodType.values()) {
					if (balanceSheet.inventory.containsKey(goodType))
						this.setValue(
								NationalAccountsTableModel.SIDE_ACTIVE,
								NationalAccountsTableModel.STARTPOSITION_GOODTYPES
										+ goodType.ordinal(), agentTypeNr,
								referenceCurrency,
								balanceSheet.inventory.get(goodType));
				}

				// passive
				this.setValue(NationalAccountsTableModel.SIDE_PASSIVE,
						NationalAccountsTableModel.POSITION_LOANS, agentTypeNr,
						referenceCurrency, Currency.round(balanceSheet.loans));
				this.setValue(NationalAccountsTableModel.SIDE_PASSIVE,
						NationalAccountsTableModel.POSITION_FIN_LIABLITIES,
						agentTypeNr, referenceCurrency,
						Currency.round(balanceSheet.financialLiabilities));
				this.setValue(NationalAccountsTableModel.SIDE_PASSIVE,
						NationalAccountsTableModel.POSITION_BANK_BORROWINGS,
						agentTypeNr, referenceCurrency,
						Currency.round(balanceSheet.bankBorrowings));
			}
		}
	}

	public class MonetaryTransactionsTableModel extends AbstractTableModel
			implements IModelListener {

		public final Currency referenceCurrency;

		protected Object[][] transientTableData = new Object[AgentFactory.agentTypes
				.size()][AgentFactory.agentTypes.size() + 1];

		public MonetaryTransactionsTableModel(Currency referenceCurrency) {
			this.referenceCurrency = referenceCurrency;
			ModelRegistry.getMonetaryTransactionsModel().registerListener(this);
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
			return AgentFactory.agentTypes.get(columnIndex - 1).getSimpleName();
		}

		@Override
		public void notifyListener() {
			if (ModelRegistry.getMonetaryTransactionsModel()
					.getAdjacencyMatrix().containsKey(referenceCurrency)) {
				// source data model
				Map<Class<? extends Agent>, PeriodDataAccumulatorSet<Class<? extends Agent>>> adjacencyMatrixForCurrency = ModelRegistry
						.getMonetaryTransactionsModel().getAdjacencyMatrix()
						.get(referenceCurrency);

				// for all agent types as sources of monetary transactions ->
				// rows
				for (int i = 0; i < AgentFactory.agentTypes.size(); i++) {
					Class<? extends Agent> agentTypeFrom = AgentFactory.agentTypes
							.get(i);
					PeriodDataAccumulatorSet<Class<? extends Agent>> adjacencyMatrixForCurrencyAndFromAgentType = adjacencyMatrixForCurrency
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
						PeriodDataAccumulator periodTransactionVolum = adjacencyMatrixForCurrencyAndFromAgentType
								.getPeriodDataAccumulators().get(agentTypeTo);
						this.transientTableData[i][j + 1] = Currency
								.round(periodTransactionVolum.getAmount());
						periodTransactionVolum.reset();
					}
				}
				this.fireTableDataChanged();
			}
		}
	}

	// stores transaction values compatible to JTables, is filled each period by
	// nextPeriod from adjacencyMatrix
	protected Map<Currency, MonetaryTransactionsTableModel> monetaryTransactionsTableModels = new HashMap<Currency, MonetaryTransactionsTableModel>();

	protected final Map<Currency, NationalAccountsTableModel> nationalAccountsTableModels = new HashMap<Currency, NationalAccountsTableModel>();

	public NationalAccountsPanel() {

		/*
		 * initialize models
		 */

		for (Currency currency : Currency.values()) {
			this.nationalAccountsTableModels.put(currency,
					new NationalAccountsTableModel(currency));
			this.monetaryTransactionsTableModels.put(currency,
					new MonetaryTransactionsTableModel(currency));
		}

		/*
		 * Balance sheets
		 */

		this.setLayout(new BorderLayout());
		JTabbedPane jTabbedPane = new JTabbedPane();

		for (Currency currency : Currency.values()) {
			JPanel currencyJPanel = new JPanel();
			currencyJPanel.setLayout(new BoxLayout(currencyJPanel,
					BoxLayout.PAGE_AXIS));

			// panel for national accounts
			if (nationalAccountsTableModels.containsKey(currency)) {
				JTable nationalAccountsTable = new JTable(
						nationalAccountsTableModels.get(currency));
				currencyJPanel.add(new JScrollPane(nationalAccountsTable));
			}

			// panel for monetary transactions
			if (monetaryTransactionsTableModels.containsKey(currency)) {
				JTable monetaryTransactionsTable = new JTable(
						monetaryTransactionsTableModels.get(currency));
				JScrollPane monetaryTransactionsJScrollPane = new JScrollPane(
						monetaryTransactionsTable);
				monetaryTransactionsJScrollPane.setPreferredSize(new Dimension(
						-1, 250));
				currencyJPanel.add(monetaryTransactionsJScrollPane);
			}

			jTabbedPane.addTab(currency.getIso4217Code(), currencyJPanel);
		}
		add(jTabbedPane, BorderLayout.CENTER);
	}
}
