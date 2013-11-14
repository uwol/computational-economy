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

package compecon.dashboard;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import compecon.dashboard.panel.AgentsPanel;
import compecon.dashboard.panel.BanksPanel;
import compecon.dashboard.panel.ControlPanel;
import compecon.dashboard.panel.HouseholdsPanel;
import compecon.dashboard.panel.IndustriesPanel;
import compecon.dashboard.panel.LogPanel;
import compecon.dashboard.panel.MoneyPanel;
import compecon.dashboard.panel.NationalAccountsPanel;
import compecon.dashboard.panel.StatesPanel;
import compecon.dashboard.panel.TradersPanel;
import compecon.engine.statistics.NotificationListenerModel.ModelListener;

public class Dashboard extends JFrame implements ModelListener {

	protected final JTabbedPane jTabbedPane;

	protected final AgentsPanel agentsPanel = new AgentsPanel();

	protected final ControlPanel controlPanel = new ControlPanel();

	protected final HouseholdsPanel householdsPanel = new HouseholdsPanel();

	protected final IndustriesPanel industriesPanel = new IndustriesPanel();

	protected final TradersPanel tradersPanel = new TradersPanel();

	protected final BanksPanel banksPanel = new BanksPanel();

	protected final StatesPanel statesPanel = new StatesPanel();

	protected final MoneyPanel moneyPanel = new MoneyPanel();

	protected final NationalAccountsPanel nationalAccountsPanel = new NationalAccountsPanel();

	protected final LogPanel logPanel = new LogPanel();

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
		this.jTabbedPane = new JTabbedPane();
		this.jTabbedPane.addTab("Agents", this.agentsPanel);
		this.jTabbedPane.addTab("Households", this.householdsPanel);
		this.jTabbedPane.addTab("Industries", this.industriesPanel);
		this.jTabbedPane.addTab("Traders", this.tradersPanel);
		this.jTabbedPane.addTab("Banks", this.banksPanel);
		this.jTabbedPane.addTab("States", this.statesPanel);
		this.jTabbedPane.addTab("Money", this.moneyPanel);
		this.jTabbedPane.addTab("National Accounts", nationalAccountsPanel);
		this.jTabbedPane.addTab("Logs", this.logPanel);

		jTabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (e.getSource() instanceof JTabbedPane) {
					JTabbedPane pane = (JTabbedPane) e.getSource();
					ModelListener selectedComponent = (ModelListener) pane
							.getSelectedComponent();
					selectedComponent.notifyListener();
				}
			}
		});

		add(this.jTabbedPane, BorderLayout.CENTER);

		/*
		 * Pack
		 */

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	public ControlPanel getControlPanel() {
		return this.controlPanel;
	}

	@Override
	public void notifyListener() {
		if (this.isShowing()) {
			ModelListener panel = (ModelListener) jTabbedPane
					.getSelectedComponent();
			panel.notifyListener();
		}
	}
}
