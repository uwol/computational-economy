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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import compecon.culture.sectors.financial.CentralBank;
import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.household.Household;
import compecon.culture.sectors.industry.Factory;
import compecon.culture.sectors.state.State;
import compecon.culture.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.culture.sectors.trading.Trader;
import compecon.engine.Agent;
import compecon.engine.jmx.model.Model.IModelListener;
import compecon.engine.jmx.model.ModelRegistry;
import compecon.engine.jmx.model.PeriodDataAccumulator;
import compecon.engine.jmx.model.PeriodDataAccumulatorSet;
import compecon.nature.materia.GoodType;

public class NationalAccountsPanel extends JPanel {

	public class NationalAccountsTableModel extends AbstractTableModel
			implements IModelListener {

		public final Currency referenceCurrency;

		public final static int SIDE_ACTIVE = 0;
		public final static int SIDE_PASSIVE = 5;

		public final static int POSITION_HARD_CASH = 0;
		public final static int POSITION_CASH = 1;
		public final static int POSITION_BONDS = 2;
		public final static int POSITION_BANK_LOANS = 3;
		public final static int STARTPOSITION_GOODTYPES = 5;

		public final static int POSITION_LOANS = 1;
		public final static int POSITION_FIN_LIABLITIES = 2;
		public final static int POSITION_BANK_BORROWINGS = 3;

		public final static int AGENTTYPE_HOUSEHOLD = 0;
		public final static int AGENTTYPE_FACTORY = 1;
		public final static int AGENTTYPE_CREDITBANK = 2;
		public final static int AGENTTYPE_CENTRALBANK = 3;
		public final static int AGENTTYPE_STATE = 4;
		public final static int AGENTTYPE_TRADER = 5;

		protected final String columnNames[] = { "Active Account",
				"Agent Type", "Value", "Currency", "", "Passive Account",
				"Agent Type", "Value", "Currency" };

		protected final String[] agentTypeNames = { "Household", "Factory",
				"Credit Bank", "Central Bank", "State", "Trader" };

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
			this.activePositionNames[POSITION_CASH] = "Cash";
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
				cells[i * this.agentTypeNames.length][SIDE_ACTIVE] = this.activePositionNames[i];
				cells[i * this.agentTypeNames.length][SIDE_PASSIVE] = this.passivePositionNames[i];

				for (int j = 0; j < this.agentTypeNames.length; j++) {
					if (this.activePositionNames[i] != null) {
						// agent type name
						cells[i * this.agentTypeNames.length + j][SIDE_ACTIVE + 1] = this.agentTypeNames[j];

						if (i < STARTPOSITION_GOODTYPES)
							// currency name
							cells[i * this.agentTypeNames.length + j][SIDE_ACTIVE + 3] = this.referenceCurrency
									.getIso4217Code();
						else
							// unit
							cells[i * this.agentTypeNames.length + j][SIDE_ACTIVE + 3] = "Units";
					}
					if (this.passivePositionNames[i] != null) {
						// agent type name
						cells[i * this.agentTypeNames.length + j][SIDE_PASSIVE + 1] = this.agentTypeNames[j];
						// currency name
						cells[i * this.agentTypeNames.length + j][SIDE_PASSIVE + 3] = this.referenceCurrency
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
			return this.agentTypeNames.length
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
			return (positionTypeNr * agentTypeNames.length) + agentTypeNr;
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

				int agentTypeNr = -1;
				if (agentType.equals(Household.class))
					agentTypeNr = NationalAccountsTableModel.AGENTTYPE_HOUSEHOLD;
				else if (agentType.equals(CreditBank.class))
					agentTypeNr = NationalAccountsTableModel.AGENTTYPE_CREDITBANK;
				else if (agentType.equals(CentralBank.class))
					agentTypeNr = NationalAccountsTableModel.AGENTTYPE_CENTRALBANK;
				else if (agentType.equals(State.class))
					agentTypeNr = NationalAccountsTableModel.AGENTTYPE_STATE;
				else if (agentType.equals(Factory.class))
					agentTypeNr = NationalAccountsTableModel.AGENTTYPE_FACTORY;
				else if (agentType.equals(Trader.class))
					agentTypeNr = NationalAccountsTableModel.AGENTTYPE_TRADER;

				// active
				this.setValue(NationalAccountsTableModel.SIDE_ACTIVE,
						NationalAccountsTableModel.POSITION_HARD_CASH,
						agentTypeNr, referenceCurrency,
						Currency.round(balanceSheet.hardCash));
				this.setValue(NationalAccountsTableModel.SIDE_ACTIVE,
						NationalAccountsTableModel.POSITION_CASH, agentTypeNr,
						referenceCurrency, Currency.round(balanceSheet.cash));
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

		protected Object[][] transientTableData = new Object[ModelRegistry
				.getMonetaryTransactionsModel().getAgentTypes().size()][ModelRegistry
				.getMonetaryTransactionsModel().getAgentTypes().size() + 1];

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
			return ModelRegistry.getMonetaryTransactionsModel().getAgentTypes()
					.get(columnIndex - 1).getSimpleName();
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
				for (int i = 0; i < ModelRegistry
						.getMonetaryTransactionsModel().getAgentTypes().size(); i++) {
					Class<? extends Agent> agentTypeFrom = ModelRegistry
							.getMonetaryTransactionsModel().getAgentTypes()
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
					for (int j = 0; j < ModelRegistry
							.getMonetaryTransactionsModel().getAgentTypes()
							.size(); j++) {
						Class<? extends Agent> agentTypeTo = ModelRegistry
								.getMonetaryTransactionsModel().getAgentTypes()
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
		JTabbedPane jTabbedPane_BalanceSheets = new JTabbedPane();

		// panels for national accounts
		for (Currency currency : Currency.values()) {
			if (nationalAccountsTableModels.containsKey(currency)) {
				JTable nationalAccountsTable = new JTable(
						nationalAccountsTableModels.get(currency));
				jTabbedPane_BalanceSheets.addTab(currency.getIso4217Code(),
						new JScrollPane(nationalAccountsTable));
			}
		}
		add(jTabbedPane_BalanceSheets, BorderLayout.CENTER);

		/*
		 * Monetary Transactions
		 */

		JTabbedPane jTabbedPane_MonetaryTransactions = new JTabbedPane();
		// panels for national accounts
		for (Currency currency : Currency.values()) {
			if (monetaryTransactionsTableModels.containsKey(currency)) {
				JTable monetaryTransactionsTable = new JTable(
						monetaryTransactionsTableModels.get(currency));
				jTabbedPane_MonetaryTransactions.addTab(currency
						.getIso4217Code(), new JScrollPane(
						monetaryTransactionsTable));
			}
		}
		jTabbedPane_MonetaryTransactions
				.setPreferredSize(new Dimension(-1, 250));
		add(jTabbedPane_MonetaryTransactions, BorderLayout.SOUTH);
	}

}
