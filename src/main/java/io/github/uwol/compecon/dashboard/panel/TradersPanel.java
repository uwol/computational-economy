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

package io.github.uwol.compecon.dashboard.panel;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.engine.statistics.NotificationListenerModel.ModelListener;

public class TradersPanel extends AbstractChartsPanel implements ModelListener {

	private static final long serialVersionUID = 1L;

	public class TraderPanelForCurrency extends JPanel implements ModelListener {

		private static final long serialVersionUID = 1L;

		protected final Currency currency;

		public TraderPanelForCurrency(final Currency currency) {
			this.currency = currency;

			setLayout(new GridLayout(0, 2));

			this.add(createTraderBalanceSheetPanel(currency));

			notifyListener();
		}

		@Override
		public void notifyListener() {
			if (isShowing()) {

			}
		}
	}

	protected final JTabbedPane jTabbedPaneCurrency = new JTabbedPane();

	public TradersPanel() {
		setLayout(new BorderLayout());

		for (final Currency currency : Currency.values()) {
			final JPanel panelForCurrency = new TraderPanelForCurrency(currency);
			jTabbedPaneCurrency.addTab(currency.getIso4217Code(), panelForCurrency);
		}

		jTabbedPaneCurrency.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (e.getSource() instanceof JTabbedPane) {
					final JTabbedPane pane = (JTabbedPane) e.getSource();
					final TraderPanelForCurrency selectedComponent = (TraderPanelForCurrency) pane
							.getSelectedComponent();
					selectedComponent.notifyListener();
				}
			}
		});

		add(jTabbedPaneCurrency, BorderLayout.CENTER);
	}

	@Override
	public synchronized void notifyListener() {
		if (isShowing()) {
			final TraderPanelForCurrency traderPanelForCurrency = (TraderPanelForCurrency) jTabbedPaneCurrency
					.getSelectedComponent();
			traderPanelForCurrency.notifyListener();
		}
	}
}
