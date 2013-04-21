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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartPanel;

import compecon.culture.sectors.financial.Currency;
import compecon.engine.dashboard.model.AgentLogsModel;
import compecon.engine.dashboard.model.BalanceSheetsModel;
import compecon.engine.dashboard.model.MonetaryTransactionsModel;
import compecon.engine.dashboard.model.NumberOfAgentsTableModel;
import compecon.engine.dashboard.model.PeriodDataAccumulatorTimeSeriesModel;
import compecon.engine.dashboard.model.PricesModel;
import compecon.engine.dashboard.model.TimeSeriesModel;
import compecon.engine.dashboard.panel.AgentsPanel;
import compecon.engine.dashboard.panel.AggregatesPanel;
import compecon.engine.dashboard.panel.ControlPanel;
import compecon.engine.dashboard.panel.NationalAccountsPanel;
import compecon.engine.dashboard.panel.PricesPanel;
import compecon.nature.materia.GoodType;

public class Dashboard extends JFrame {
	private static Dashboard instance;

	// models

	protected final AgentLogsModel agentLogsModel = new AgentLogsModel();

	protected final BalanceSheetsModel balanceSheetsModel;

	protected final PeriodDataAccumulatorTimeSeriesModel<GoodType> capacityModel = new PeriodDataAccumulatorTimeSeriesModel<GoodType>(
			GoodType.values(), " cap.");

	protected final PeriodDataAccumulatorTimeSeriesModel<GoodType> effectiveProductionOutputModel = new PeriodDataAccumulatorTimeSeriesModel<GoodType>(
			GoodType.values());

	protected final TimeSeriesModel<Currency> keyInterestRateModel = new TimeSeriesModel<Currency>(
			new Currency[] { Currency.EURO });

	protected final MonetaryTransactionsModel monetaryTransactionsModel = new MonetaryTransactionsModel();

	protected final PeriodDataAccumulatorTimeSeriesModel<Currency> moneySupplyM0Model = new PeriodDataAccumulatorTimeSeriesModel<Currency>(
			new Currency[] { Currency.EURO }, " M0");

	protected final PeriodDataAccumulatorTimeSeriesModel<Currency> moneySupplyM1Model = new PeriodDataAccumulatorTimeSeriesModel<Currency>(
			new Currency[] { Currency.EURO }, " M1");

	protected final PeriodDataAccumulatorTimeSeriesModel<Currency> utilityModel = new PeriodDataAccumulatorTimeSeriesModel<Currency>(
			new Currency[] { Currency.EURO }, " utility");

	protected final NumberOfAgentsTableModel numberOfAgentsTableModel = new NumberOfAgentsTableModel();

	protected final PricesModel pricesModel = new PricesModel();

	protected final TimeSeriesModel<Currency> priceIndexModel = new TimeSeriesModel<Currency>(
			new Currency[] { Currency.EURO });

	// panels

	protected final AgentsPanel agentsPanel;

	protected final JPanel borderPanel;

	protected final AggregatesPanel aggregatesPanel;

	protected final PricesPanel pricesPanel;

	protected final NationalAccountsPanel nationalAccountsPanel;

	protected final JTabbedPane jTabbedPane;

	private Dashboard() {
		/*
		 * models
		 */
		this.balanceSheetsModel = new BalanceSheetsModel(
				this.moneySupplyM0Model, this.moneySupplyM1Model);
		this.aggregatesPanel = new AggregatesPanel(this.priceIndexModel,
				this.keyInterestRateModel, this.effectiveProductionOutputModel,
				this.capacityModel, this.balanceSheetsModel,
				this.moneySupplyM0Model, this.moneySupplyM1Model,
				this.utilityModel);
		this.pricesPanel = new PricesPanel(this.pricesModel);
		this.nationalAccountsPanel = new NationalAccountsPanel(
				this.balanceSheetsModel.getNationalAccountsTableModels(),
				this.monetaryTransactionsModel
						.getMonetaryTransactionsTableModels());
		this.agentsPanel = new AgentsPanel(this.agentLogsModel);

		/*
		 * panels
		 */
		setLayout(new BorderLayout());
		setTitle("Computational Economy");

		/*
		 * bottom panel
		 */
		JPanel bottomPanel = new JPanel(new GridLayout(0, 2));
		JTable numberOfAgentsTable = new JTable(numberOfAgentsTableModel);
		JScrollPane numberOfAgentsPane = new JScrollPane(numberOfAgentsTable);
		numberOfAgentsPane.setPreferredSize(new Dimension(-1, 150));
		bottomPanel.add(numberOfAgentsPane);
		// ToDo method in wrong class
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
						Dashboard.this.agentLogsModel.blockRefresh(false);
					else
						Dashboard.this.agentLogsModel.blockRefresh(true);

					if (pane.getSelectedComponent().equals(
							Dashboard.this.pricesPanel))
						Dashboard.this.pricesPanel.blockRedraw(false);
					else
						Dashboard.this.pricesPanel.blockRedraw(true);
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

	public AgentLogsModel getAgentLogsModel() {
		return this.agentLogsModel;
	}

	public BalanceSheetsModel getBalanceSheetsModel() {
		return this.balanceSheetsModel;
	}

	public PeriodDataAccumulatorTimeSeriesModel<GoodType> getCapacityModel() {
		return this.capacityModel;
	}

	public PeriodDataAccumulatorTimeSeriesModel<GoodType> getEffectiveProductionOutputModel() {
		return this.effectiveProductionOutputModel;
	}

	public TimeSeriesModel<Currency> getKeyInterestRateModel() {
		return this.keyInterestRateModel;
	}

	public MonetaryTransactionsModel getMonetaryTransactionsModel() {
		return this.monetaryTransactionsModel;
	}

	public PeriodDataAccumulatorTimeSeriesModel<Currency> getMoneySupplyM0Model() {
		return this.moneySupplyM0Model;
	}

	public PeriodDataAccumulatorTimeSeriesModel<Currency> getMoneySupplyM1Model() {
		return this.moneySupplyM1Model;
	}

	public NumberOfAgentsTableModel getNumberOfAgentsTableModel() {
		return this.numberOfAgentsTableModel;
	}

	public PricesModel getPricesModel() {
		return this.pricesModel;
	}

	public TimeSeriesModel<Currency> getPriceIndexModel() {
		return this.priceIndexModel;
	}

	public PeriodDataAccumulatorTimeSeriesModel<Currency> getUtilityModel() {
		return this.utilityModel;
	}

	public void nextPeriod() {
		this.pricesPanel.redrawPriceCharts();
	}
}
