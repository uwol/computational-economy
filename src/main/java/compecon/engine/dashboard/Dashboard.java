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
import compecon.engine.dashboard.panel.NationalAccountsPanel;
import compecon.engine.dashboard.panel.NumberOfAgentsPanel;
import compecon.engine.dashboard.panel.PricesPanel;

public class Dashboard extends JFrame {
	private static Dashboard instance;

	// panels

	protected final AgentsPanel agentsPanel = new AgentsPanel();

	protected final JPanel borderPanel;

	protected final AggregatesPanel aggregatesPanel = new AggregatesPanel();

	protected final PricesPanel pricesPanel = new PricesPanel();

	protected final NationalAccountsPanel nationalAccountsPanel = new NationalAccountsPanel();

	protected final JTabbedPane jTabbedPane;

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
		this.jTabbedPane.addTab("Agents", this.agentsPanel);
		this.jTabbedPane.addTab("National Accounts", nationalAccountsPanel);

		jTabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (e.getSource() instanceof JTabbedPane) {
					JTabbedPane pane = (JTabbedPane) e.getSource();

					if (pane.getSelectedComponent().equals(
							Dashboard.this.agentsPanel))
						Dashboard.this.agentsPanel.noRefresh(false);
					else
						Dashboard.this.agentsPanel.noRefresh(true);

					if (pane.getSelectedComponent().equals(
							Dashboard.this.pricesPanel))
						Dashboard.this.pricesPanel.noRedraw(false);
					else
						Dashboard.this.pricesPanel.noRedraw(true);
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

	public void nextPeriod() {
		this.pricesPanel.redrawPriceCharts();
	}
}
