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

import compecon.economy.sectors.financial.BankAccount.BankAccountType;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.engine.Simulation;
import compecon.engine.statistics.model.NotificationListenerModel.IModelListener;

public class BalanceSheetPanel extends JPanel implements IModelListener {

	public abstract static class BalanceSheetTableModel extends
			AbstractTableModel implements IModelListener {

		protected BalanceSheet balanceSheet;

		protected final String columnNames[] = { "Active Account", "Value",
				"Passive Account", "Value" };

		public BalanceSheetTableModel(Currency currency) {
			Simulation.getInstance().getModelRegistry()
					.getNationalEconomyModel(currency).balanceSheetsModel
					.registerListener(this);
		}

		@Override
		public int getColumnCount() {
			return this.columnNames.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return this.columnNames[columnIndex];
		}

		@Override
		public int getRowCount() {
			return 11;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {

			// active accounts
			if (columnIndex == 0) {
				switch (rowIndex) {
				case 0:
					return "Hard Cash";

				case 1:
					return "Cash " + BankAccountType.SHORT_TERM;
				case 2:
					return "Cash " + BankAccountType.LONG_TERM;
				case 3:
					return "Cash " + BankAccountType.CENTRAL_BANK_TRANSACTION;
				case 4:
					return "Cash "
							+ BankAccountType.CENTRAL_BANK_MONEY_RESERVES;

				case 5:
					return "Cash Foreign Currency";
				case 6:
					return "Fin. Assets (Bonds)";
				case 7:
					return "Bank Loans";
				case 8:
					return "Inventory";
				case 9:
					return "--------";
				case 10:
					return "Balance";
				default:
					return null;
				}
			}
			// active values
			else if (columnIndex == 1) {
				switch (rowIndex) {
				case 0:
					return Currency.formatMoneySum(balanceSheet.hardCash);

				case 1:
					return Currency.formatMoneySum(balanceSheet.cash
							.get(BankAccountType.SHORT_TERM));
				case 2:
					return Currency.formatMoneySum(balanceSheet.cash
							.get(BankAccountType.LONG_TERM));
				case 3:
					return Currency.formatMoneySum(balanceSheet.cash
							.get(BankAccountType.CENTRAL_BANK_TRANSACTION));
				case 4:
					return Currency.formatMoneySum(balanceSheet.cash
							.get(BankAccountType.CENTRAL_BANK_MONEY_RESERVES));

				case 5:
					return Currency
							.formatMoneySum(balanceSheet.cashForeignCurrency);
				case 6:
					return Currency.formatMoneySum(balanceSheet.bonds);
				case 7:
					return Currency.formatMoneySum(balanceSheet.bankLoans);
				case 8:
					return Currency.formatMoneySum(balanceSheet.inventoryValue);
				case 10:
					return Currency.formatMoneySum(balanceSheet
							.getBalanceActive());
				default:
					return null;
				}
			}
			// passive accounts
			else if (columnIndex == 2) {
				switch (rowIndex) {
				case 1:
					return "Loans " + BankAccountType.SHORT_TERM;
				case 2:
					return "Loans " + BankAccountType.LONG_TERM;
				case 3:
					return "Loans " + BankAccountType.CENTRAL_BANK_TRANSACTION;
				case 4:
					return "Loans "
							+ BankAccountType.CENTRAL_BANK_MONEY_RESERVES;

				case 6:
					return "Fin. Liabilities (Bonds)";
				case 7:
					return "Bank Borrowings";
				case 8:
					return "Equity";
				case 9:
					return "--------";
				case 10:
					return "Balance";
				default:
					return null;
				}
			}
			// passive values
			else if (columnIndex == 3) {
				switch (rowIndex) {
				case 1:
					return Currency.formatMoneySum(balanceSheet.loans
							.get(BankAccountType.SHORT_TERM));
				case 2:
					return Currency.formatMoneySum(balanceSheet.loans
							.get(BankAccountType.LONG_TERM));
				case 3:
					return Currency.formatMoneySum(balanceSheet.loans
							.get(BankAccountType.CENTRAL_BANK_TRANSACTION));
				case 4:
					return Currency.formatMoneySum(balanceSheet.loans
							.get(BankAccountType.CENTRAL_BANK_MONEY_RESERVES));

				case 6:
					return Currency
							.formatMoneySum(balanceSheet.financialLiabilities);
				case 7:
					return Currency.formatMoneySum(balanceSheet.bankBorrowings);
				case 8:
					return Currency.formatMoneySum(balanceSheet.getEquity());
				case 10:
					return Currency.formatMoneySum(balanceSheet
							.getBalancePassive());
				default:
					return null;
				}
			} else
				return null;
		}

		protected abstract BalanceSheet getBalanceSheet();

		@Override
		public void notifyListener() {
			this.balanceSheet = getBalanceSheet();
			this.fireTableDataChanged();
		}
	}

	protected final BalanceSheetTableModel balanceSheetTableModel;

	public BalanceSheetPanel(Currency referenceCurrency,
			final BalanceSheetTableModel balanceSheetTableModel, String title) {
		this.balanceSheetTableModel = balanceSheetTableModel;
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		TitledBorder titleBorder = BorderFactory.createTitledBorder(title);
		titleBorder.setTitleJustification(TitledBorder.CENTER);
		this.setBorder(titleBorder);
		JTable balanceSheetTable = new JTable(balanceSheetTableModel);
		this.add(new JScrollPane(balanceSheetTable));
	}

	@Override
	public void notifyListener() {
		this.balanceSheetTableModel.notifyListener();
	}
}
