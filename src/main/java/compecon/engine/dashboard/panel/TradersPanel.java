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
import compecon.engine.statistics.model.NotificationListenerModel.IModelListener;

public class TradersPanel extends AbstractChartsPanel implements IModelListener {

	public class TraderPanelForCurrency extends JPanel implements
			IModelListener {
		protected final Currency currency;

		public TraderPanelForCurrency(Currency currency) {
			this.currency = currency;

			this.setLayout(new GridLayout(0, 2));

			this.add(createTraderBalanceSheetPanel(currency));

			notifyListener();
		}

		@Override
		public void notifyListener() {
			if (this.isShowing()) {

			}
		}
	}

	protected final JTabbedPane jTabbedPaneCurrency = new JTabbedPane();

	public TradersPanel() {
		this.setLayout(new BorderLayout());

		for (Currency currency : Currency.values()) {
			JPanel panelForCurrency = new TraderPanelForCurrency(currency);
			jTabbedPaneCurrency.addTab(currency.getIso4217Code(),
					panelForCurrency);
		}

		jTabbedPaneCurrency.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (e.getSource() instanceof JTabbedPane) {
					JTabbedPane pane = (JTabbedPane) e.getSource();
					TraderPanelForCurrency selectedComponent = (TraderPanelForCurrency) pane
							.getSelectedComponent();
					selectedComponent.notifyListener();
				}
			}
		});

		add(jTabbedPaneCurrency, BorderLayout.CENTER);
	}

	@Override
	public synchronized void notifyListener() {
		if (this.isShowing()) {
			TraderPanelForCurrency traderPanelForCurrency = (TraderPanelForCurrency) jTabbedPaneCurrency
					.getSelectedComponent();
			traderPanelForCurrency.notifyListener();
		}
	}
}
