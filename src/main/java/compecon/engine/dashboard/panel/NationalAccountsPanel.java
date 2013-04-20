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
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import compecon.culture.sectors.financial.Currency;
import compecon.engine.dashboard.model.BalanceSheetsModel;
import compecon.engine.dashboard.model.MonetaryTransactionsModel.MonetaryTransactionsTableModel;

public class NationalAccountsPanel extends JPanel {

	public NationalAccountsPanel(
			Map<Currency, BalanceSheetsModel.NationalAccountsTableModel> nationalAccountsTableModels,
			Map<Currency, MonetaryTransactionsTableModel> monetaryTransactionsTableModels) {

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
