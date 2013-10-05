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
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.engine.Simulation;
import compecon.engine.dashboard.panel.BalanceSheetPanel.BalanceSheetTableModel;
import compecon.engine.statistics.model.NotificationListenerModel.IModelListener;

public class StatesPanel extends AbstractChartsPanel implements IModelListener {

	public class StatePanelForCurrency extends JPanel implements IModelListener {
		protected final Currency currency;

		public StatePanelForCurrency(Currency currency) {
			this.currency = currency;

			this.add(createStateBalanceSheetPanel(currency));

			this.setLayout(new GridLayout(0, 2));
		}

		@Override
		public void notifyListener() {
		}
	}

	protected final JTabbedPane jTabbedPaneCurrency = new JTabbedPane();

	public StatesPanel() {
		this.setLayout(new BorderLayout());

		for (Currency currency : Currency.values()) {
			JPanel panelForCurrency = new StatePanelForCurrency(currency);
			jTabbedPaneCurrency.addTab(currency.getIso4217Code(),
					panelForCurrency);
		}

		jTabbedPaneCurrency.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (e.getSource() instanceof JTabbedPane) {
					JTabbedPane pane = (JTabbedPane) e.getSource();
					StatePanelForCurrency selectedComponent = (StatePanelForCurrency) pane
							.getSelectedComponent();
					selectedComponent.notifyListener();
				}
			}
		});

		add(jTabbedPaneCurrency, BorderLayout.CENTER);
	}

	protected JPanel createStateBalanceSheetPanel(Currency currency) {
		final BalanceSheetTableModel balanceSheetTableModel = new BalanceSheetTableModel(
				currency) {
			@Override
			protected BalanceSheet getModelData() {
				return Simulation.getInstance().getModelRegistry()
						.getBalanceSheetsModel(referenceCurrency)
						.getStateNationalAccountsBalanceSheet();
			}
		};
		return new BalanceSheetPanel(currency, balanceSheetTableModel,
				"Balance Sheet for " + currency.getIso4217Code() + " State");
	}

	@Override
	public void notifyListener() {
		if (this.isShowing()) {
			StatePanelForCurrency statePanelForCurrency = (StatePanelForCurrency) jTabbedPaneCurrency
					.getSelectedComponent();
			statePanelForCurrency.notifyListener();
		}
	}
}
