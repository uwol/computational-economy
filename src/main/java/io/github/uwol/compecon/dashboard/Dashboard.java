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

package io.github.uwol.compecon.dashboard;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import io.github.uwol.compecon.dashboard.panel.AgentsPanel;
import io.github.uwol.compecon.dashboard.panel.BanksPanel;
import io.github.uwol.compecon.dashboard.panel.ControlPanel;
import io.github.uwol.compecon.dashboard.panel.HouseholdsPanel;
import io.github.uwol.compecon.dashboard.panel.IndustriesPanel;
import io.github.uwol.compecon.dashboard.panel.LogPanel;
import io.github.uwol.compecon.dashboard.panel.MoneyPanel;
import io.github.uwol.compecon.dashboard.panel.NationalAccountsPanel;
import io.github.uwol.compecon.dashboard.panel.StatesPanel;
import io.github.uwol.compecon.dashboard.panel.TradersPanel;
import io.github.uwol.compecon.engine.statistics.NotificationListenerModel.ModelListener;

public class Dashboard extends JFrame implements ModelListener {

	private static final long serialVersionUID = 1L;

	protected final AgentsPanel agentsPanel = new AgentsPanel();

	protected final BanksPanel banksPanel = new BanksPanel();

	protected final ControlPanel controlPanel = new ControlPanel();

	protected final HouseholdsPanel householdsPanel = new HouseholdsPanel();

	protected final IndustriesPanel industriesPanel = new IndustriesPanel();

	protected final JTabbedPane jTabbedPane;

	protected final LogPanel logPanel = new LogPanel();

	protected final MoneyPanel moneyPanel = new MoneyPanel();

	protected final NationalAccountsPanel nationalAccountsPanel = new NationalAccountsPanel();

	protected final StatesPanel statesPanel = new StatesPanel();

	protected final TradersPanel tradersPanel = new TradersPanel();

	public Dashboard() {
		/*
		 * panels
		 */
		setLayout(new BorderLayout());
		setTitle("Computational Economy");

		/*
		 * border panel
		 */
		this.add(controlPanel, BorderLayout.WEST);

		/*
		 * tabbed content panel
		 */
		jTabbedPane = new JTabbedPane();
		jTabbedPane.addTab("Agents", agentsPanel);
		jTabbedPane.addTab("Households", householdsPanel);
		jTabbedPane.addTab("Industries", industriesPanel);
		jTabbedPane.addTab("Traders", tradersPanel);
		jTabbedPane.addTab("Banks", banksPanel);
		jTabbedPane.addTab("States", statesPanel);
		jTabbedPane.addTab("Money", moneyPanel);
		jTabbedPane.addTab("National Accounts", nationalAccountsPanel);
		jTabbedPane.addTab("Logs", logPanel);

		jTabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (e.getSource() instanceof JTabbedPane) {
					final JTabbedPane pane = (JTabbedPane) e.getSource();
					final ModelListener selectedComponent = (ModelListener) pane.getSelectedComponent();
					selectedComponent.notifyListener();
				}
			}
		});

		add(jTabbedPane, BorderLayout.CENTER);

		/*
		 * Pack
		 */

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	public ControlPanel getControlPanel() {
		return controlPanel;
	}

	@Override
	public void notifyListener() {
		if (isShowing()) {
			final ModelListener panel = (ModelListener) jTabbedPane.getSelectedComponent();
			panel.notifyListener();
		}
	}
}
