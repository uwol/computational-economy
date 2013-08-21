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

package compecon.engine.dashboard;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import compecon.engine.dashboard.panel.AgentsPanel;
import compecon.engine.dashboard.panel.ControlPanel;
import compecon.engine.dashboard.panel.HouseholdPanel;
import compecon.engine.dashboard.panel.IndustryPanel;
import compecon.engine.dashboard.panel.LogPanel;
import compecon.engine.dashboard.panel.MoneyPanel;
import compecon.engine.dashboard.panel.NationalAccountsPanel;
import compecon.engine.dashboard.panel.PricesPanel;

public class Dashboard extends JFrame {
	private static Dashboard instance;

	protected final JTabbedPane jTabbedPane;

	protected final AgentsPanel agentsPanel = new AgentsPanel();

	protected final HouseholdPanel householdPanel = new HouseholdPanel();

	protected final IndustryPanel industryPanel = new IndustryPanel();

	protected final MoneyPanel moneyPanel = new MoneyPanel();

	protected final PricesPanel pricesPanel = new PricesPanel();

	protected final NationalAccountsPanel nationalAccountsPanel = new NationalAccountsPanel();

	protected final LogPanel logPanel = new LogPanel();

	private Dashboard() {

		/*
		 * panels
		 */
		setLayout(new BorderLayout());
		setTitle("Computational Economy");

		/*
		 * border panel
		 */
		this.add(new ControlPanel(), BorderLayout.WEST);

		/*
		 * tabbed content panel
		 */
		this.jTabbedPane = new JTabbedPane();
		this.jTabbedPane.addTab("Agents", this.agentsPanel);
		this.jTabbedPane.addTab("Households", this.householdPanel);
		this.jTabbedPane.addTab("Industries", this.industryPanel);
		this.jTabbedPane.addTab("Money", this.moneyPanel);
		this.jTabbedPane.addTab("Prices", this.pricesPanel);
		this.jTabbedPane.addTab("National Accounts", nationalAccountsPanel);
		this.jTabbedPane.addTab("Logs", this.logPanel);

		jTabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (e.getSource() instanceof JTabbedPane) {
					JTabbedPane pane = (JTabbedPane) e.getSource();
					Component selectecComponent = pane.getSelectedComponent();

					// log panel
					if (selectecComponent.equals(Dashboard.this.logPanel))
						Dashboard.this.logPanel.setRefresh(true);
					else
						Dashboard.this.logPanel.setRefresh(false);

					// prices panel
					if (selectecComponent.equals(Dashboard.this.pricesPanel)) {
						Dashboard.this.pricesPanel.setRefresh(true);
					} else
						Dashboard.this.pricesPanel.setRefresh(false);
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

	public static Dashboard getInstance() {
		if (instance == null)
			instance = new Dashboard();
		return instance;
	}
}
