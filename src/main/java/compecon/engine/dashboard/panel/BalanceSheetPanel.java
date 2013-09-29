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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.engine.Simulation;
import compecon.engine.statistics.model.NotificationListenerModel.IModelListener;

public class BalanceSheetPanel extends JPanel {

	public static abstract class BalanceSheetTableModel extends
			AbstractTableModel implements IModelListener {
		public final Currency referenceCurrency;

		public final static int SIDE_ACTIVE = 0;
		public final static int SIDE_PASSIVE = 2;

		public final static int POSITION_HARD_CASH = 0;
		public final static int POSITION_CASH_SHORT_TERM = 1;
		public final static int POSITION_CASH_LONG_TERM = 2;
		public final static int POSITION_BONDS = 3;
		public final static int POSITION_BANK_LOANS = 4;
		public final static int POSITION_INVENTORY = 5;

		public final static int POSITION_LOANS = 1;
		public final static int POSITION_FIN_LIABLITIES = 3;
		public final static int POSITION_BANK_BORROWINGS = 4;
		public final static int POSITION_EQUITY = 5;

		public final static int POSITION_BALANCE = 7;

		protected final String columnNames[] = { "Active Account", "Value",
				"Passive Account", "Value" };

		protected String[] activePositionNames;

		protected String[] passivePositionNames;

		protected Object[][] cells;

		public BalanceSheetTableModel(Currency referenceCurrency) {
			this.referenceCurrency = referenceCurrency;
			Simulation.getInstance().getModelRegistry()
					.getBalanceSheetsModel(referenceCurrency)
					.registerListener(this);

			this.activePositionNames = new String[8];
			this.passivePositionNames = new String[8];

			this.activePositionNames[POSITION_HARD_CASH] = "Hard Cash";
			this.activePositionNames[POSITION_CASH_SHORT_TERM] = "Cash Short Term";
			this.activePositionNames[POSITION_CASH_LONG_TERM] = "Cash Long Term";
			this.activePositionNames[POSITION_BONDS] = "Fin. Assets (Bonds)";
			this.activePositionNames[POSITION_BANK_LOANS] = "Bank Loans";
			this.activePositionNames[POSITION_INVENTORY] = "Inventory";
			this.activePositionNames[POSITION_BALANCE - 1] = "--------";
			this.activePositionNames[POSITION_BALANCE] = "Balance";

			this.passivePositionNames[POSITION_LOANS] = "Loans";
			this.passivePositionNames[POSITION_FIN_LIABLITIES] = "Fin. Liabilities (Bonds)";
			this.passivePositionNames[POSITION_BANK_BORROWINGS] = "Bank Borrowings";
			this.passivePositionNames[POSITION_EQUITY] = "Equity";
			this.passivePositionNames[POSITION_BALANCE - 1] = "--------";
			this.passivePositionNames[POSITION_BALANCE] = "Balance";

			this.cells = new Object[this.getRowCount()][this.getColumnCount()];

			for (int i = 0; i < Math.max(this.activePositionNames.length,
					this.passivePositionNames.length); i++) {
				// position name
				cells[i][SIDE_ACTIVE] = this.activePositionNames[i];
				cells[i][SIDE_PASSIVE] = this.passivePositionNames[i];
			}
		}

		@Override
		public int getColumnCount() {
			return this.columnNames.length;
		}

		@Override
		public int getRowCount() {
			return Math.max(this.activePositionNames.length,
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
				final Currency currency, final double value) {
			if (!currency.equals(this.referenceCurrency))
				return;

			int rowNumber = positionTypeNr;
			this.cells[rowNumber][sideNr + 1] = Currency.formatMoneySum(value);
			fireTableCellUpdated(rowNumber, sideNr + 1);
		}

		protected abstract BalanceSheet getModelData();

		@Override
		public void notifyListener() {
			BalanceSheet balanceSheet = getModelData();

			if (balanceSheet != null) {
				// active
				this.setValue(SIDE_ACTIVE, POSITION_HARD_CASH,
						referenceCurrency,
						Currency.round(balanceSheet.hardCash));
				this.setValue(SIDE_ACTIVE, POSITION_CASH_SHORT_TERM,
						referenceCurrency,
						Currency.round(balanceSheet.cashShortTerm));
				this.setValue(SIDE_ACTIVE, POSITION_CASH_LONG_TERM,
						referenceCurrency,
						Currency.round(balanceSheet.cashLongTerm));
				this.setValue(SIDE_ACTIVE, POSITION_BONDS, referenceCurrency,
						Currency.round(balanceSheet.bonds));
				this.setValue(SIDE_ACTIVE, POSITION_BANK_LOANS,
						referenceCurrency,
						Currency.round(balanceSheet.bankLoans));
				this.setValue(SIDE_ACTIVE, POSITION_INVENTORY,
						referenceCurrency,
						Currency.round(balanceSheet.inventoryValue));
				this.setValue(SIDE_ACTIVE, POSITION_BALANCE, referenceCurrency,
						Currency.round(balanceSheet.getBalanceActive()));

				// passive
				this.setValue(SIDE_PASSIVE, POSITION_LOANS, referenceCurrency,
						Currency.round(balanceSheet.loans));
				this.setValue(SIDE_PASSIVE, POSITION_FIN_LIABLITIES,
						referenceCurrency,
						Currency.round(balanceSheet.financialLiabilities));
				this.setValue(SIDE_PASSIVE, POSITION_BANK_BORROWINGS,
						referenceCurrency,
						Currency.round(balanceSheet.bankBorrowings));
				this.setValue(SIDE_PASSIVE, POSITION_EQUITY, referenceCurrency,
						Currency.round(balanceSheet.getEquity()));
				this.setValue(SIDE_PASSIVE, POSITION_BALANCE,
						referenceCurrency,
						Currency.round(balanceSheet.getBalancePassive()));
			}
		}
	}

	public final BalanceSheetTableModel balanceSheetTableModel;

	public BalanceSheetPanel(Currency referenceCurrency,
			BalanceSheetTableModel balanceSheetTableModel, String title) {
		this.balanceSheetTableModel = balanceSheetTableModel;
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		TitledBorder titleBorder = BorderFactory.createTitledBorder(title);
		titleBorder.setTitleJustification(TitledBorder.CENTER);
		this.setBorder(titleBorder);
		JTable balanceSheetTable = new JTable(balanceSheetTableModel);
		this.add(new JScrollPane(balanceSheetTable));
	}
}
