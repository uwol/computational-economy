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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartPanel;

import compecon.engine.dashboard.panel.AgentsPanel;
import compecon.engine.dashboard.panel.AggregatesPanel;
import compecon.engine.dashboard.panel.ControlPanel;
import compecon.engine.dashboard.panel.HouseholdPanel;
import compecon.engine.dashboard.panel.NationalAccountsPanel;
import compecon.engine.dashboard.panel.NumberOfAgentsPanel;
import compecon.engine.dashboard.panel.PricesPanel;

public class Dashboard extends JFrame {
	private static Dashboard instance;

	protected final JTabbedPane jTabbedPane;

	protected final AggregatesPanel aggregatesPanel = new AggregatesPanel();

	protected final PricesPanel pricesPanel = new PricesPanel();

	protected final HouseholdPanel householdPanel = new HouseholdPanel();

	protected final NationalAccountsPanel nationalAccountsPanel = new NationalAccountsPanel();

	protected final AgentsPanel agentsPanel = new AgentsPanel();

	protected final JPanel borderPanel;

	private Dashboard() {

		/*
		 * panels
		 */
		setLayout(new BorderLayout());
		setTitle("Computational Economy");

		/*
		 * bottom panel
		 */
		JPanel bottomPanel = new JPanel(new GridLayout(0, 2));
		bottomPanel.setPreferredSize(new Dimension(-1, 150));
		bottomPanel.add(new NumberOfAgentsPanel());

		ChartPanel utilityChart = this.aggregatesPanel.createUtilityChart();
		utilityChart.setPreferredSize(new Dimension(-1, 150));
		bottomPanel.add(utilityChart);

		add(bottomPanel, BorderLayout.SOUTH);

		/*
		 * tabbed content panel
		 */
		this.jTabbedPane = new JTabbedPane();
		this.jTabbedPane.addTab("Aggregates", this.aggregatesPanel);
		this.jTabbedPane.addTab("Prices", this.pricesPanel);
		this.jTabbedPane.addTab("Households", this.householdPanel);
		this.jTabbedPane.addTab("National Accounts", nationalAccountsPanel);
		this.jTabbedPane.addTab("Agents", this.agentsPanel);

		jTabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (e.getSource() instanceof JTabbedPane) {
					JTabbedPane pane = (JTabbedPane) e.getSource();

					if (pane.getSelectedComponent().equals(
							Dashboard.this.agentsPanel))
						Dashboard.this.agentsPanel.setRefresh(true);
					else
						Dashboard.this.agentsPanel.setRefresh(false);

					if (pane.getSelectedComponent().equals(
							Dashboard.this.pricesPanel)) {
						Dashboard.this.pricesPanel.setRefresh(true);
						Dashboard.this.pricesPanel.redrawPriceCharts();
					} else
						Dashboard.this.pricesPanel.setRefresh(false);
				}
			}
		});

		add(this.jTabbedPane, BorderLayout.CENTER);

		borderPanel = new JPanel();
		borderPanel.setLayout(new BoxLayout(borderPanel, BoxLayout.PAGE_AXIS));
		borderPanel.add(new ControlPanel());
		this.add(borderPanel, BorderLayout.WEST);

		/*
		 * Pack
		 */

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		setExtendedState(Frame.MAXIMIZED_BOTH);
	}

	public static Dashboard getInstance() {
		if (instance == null)
			instance = new Dashboard();
		return instance;
	}
}
