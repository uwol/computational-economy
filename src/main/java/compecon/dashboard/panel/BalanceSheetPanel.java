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

package compecon.dashboard.panel;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import compecon.dashboard.model.BalanceSheetTableModel;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.statistics.NotificationListenerModel.ModelListener;

public class BalanceSheetPanel extends JPanel implements ModelListener {

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
