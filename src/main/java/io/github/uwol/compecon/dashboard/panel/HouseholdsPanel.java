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
import java.awt.Color;
import java.awt.GridLayout;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;

import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.statistics.ModelRegistry.IncomeSource;
import io.github.uwol.compecon.engine.statistics.NotificationListenerModel.ModelListener;
import io.github.uwol.compecon.engine.statistics.PeriodDataDistributionModel.SummaryStatisticalData;
import io.github.uwol.compecon.engine.statistics.PricesModel;
import io.github.uwol.compecon.engine.statistics.PricesModel.PriceModel;
import io.github.uwol.compecon.math.ConvexFunction.ConvexFunctionTerminationCause;

public class HouseholdsPanel extends AbstractChartsPanel implements ModelListener {

	private static final long serialVersionUID = 1L;

	public class HouseholdsPanelForCurrency extends JPanel implements ModelListener {

		private static final long serialVersionUID = 1L;

		protected final Currency currency;

		protected JFreeChart incomeDistributionChart;

		protected JPanel marketDepthPanel;

		protected JPanel priceTimeSeriesPanel;

		public HouseholdsPanelForCurrency(final Currency currency) {
			this.currency = currency;

			setLayout(new GridLayout(0, 3));

			this.add(createUtilityPanel(currency));
			// this.add(createUtilityFunctionMechanicsPanel(currency));
			this.add(createIncomeConsumptionSavingPanel(currency));
			this.add(createConsumptionSavingRatePanel(currency));
			this.add(createWageDividendPanel(currency));
			this.add(createIncomeSourcePanel(currency));
			incomeDistributionChart = createIncomeDistributionPanel(currency);
			this.add(new ChartPanel(incomeDistributionChart));
			this.add(createLorenzCurvePanel(currency));
			this.add(createHouseholdBalanceSheetPanel(currency));
			this.add(createLabourHourSupplyPanel(currency));
			this.add(createPricingBehaviourMechanicsPanel(currency, GoodType.LABOURHOUR));

			ApplicationContext.getInstance().getModelRegistry()
					.getNationalEconomyModel(currency).householdsModel.incomeDistributionModel.registerListener(this);
			// no registration with the price and market depth model, as they
			// call listeners synchronously

			notifyListener();
		}

		@Override
		public synchronized void notifyListener() {
			if (isShowing()) {
				/*
				 * income distribution chart
				 */

				final XYPlot plot = ((XYPlot) incomeDistributionChart.getPlot());
				plot.setDataset(ApplicationContext.getInstance().getModelRegistry()
						.getNationalEconomyModel(currency).householdsModel.incomeDistributionModel
								.getHistogramDataset());

				plot.clearDomainMarkers();
				final SummaryStatisticalData summaryStatisticalData = ApplicationContext.getInstance()
						.getModelRegistry().getNationalEconomyModel(currency).householdsModel.incomeDistributionModel
								.getSummaryStatisticalData();
				if (summaryStatisticalData.originalValues != null && summaryStatisticalData.originalValues.length > 0) {
					addValueMarker(incomeDistributionChart,
							summaryStatisticalData.originalValues[summaryStatisticalData.xWith10PercentY], "10 %");
					addValueMarker(incomeDistributionChart,
							summaryStatisticalData.originalValues[summaryStatisticalData.xWith20PercentY], "20 %");
					addValueMarker(incomeDistributionChart,
							summaryStatisticalData.originalValues[summaryStatisticalData.xWith30PercentY], "30 %");
					addValueMarker(incomeDistributionChart,
							summaryStatisticalData.originalValues[summaryStatisticalData.xWith40PercentY], "40 %");
					addValueMarker(incomeDistributionChart,
							summaryStatisticalData.originalValues[summaryStatisticalData.xWith50PercentY], "50 %");
					addValueMarker(incomeDistributionChart,
							summaryStatisticalData.originalValues[summaryStatisticalData.xWith60PercentY], "60 %");
					addValueMarker(incomeDistributionChart,
							summaryStatisticalData.originalValues[summaryStatisticalData.xWith70PercentY], "70 %");
					addValueMarker(incomeDistributionChart,
							summaryStatisticalData.originalValues[summaryStatisticalData.xWith80PercentY], "80 %");
					addValueMarker(incomeDistributionChart,
							summaryStatisticalData.originalValues[summaryStatisticalData.xWith90PercentY], "90 %");
				}

				// prices panel
				if (priceTimeSeriesPanel != null) {
					this.remove(priceTimeSeriesPanel);
				}
				priceTimeSeriesPanel = createPriceTimeSeriesChartPanel(currency);
				this.add(priceTimeSeriesPanel);

				// market depth panel
				if (marketDepthPanel != null) {
					this.remove(marketDepthPanel);
				}
				marketDepthPanel = createMarketDepthPanel(currency);
				this.add(marketDepthPanel);

				validate();
				repaint();
			}
		}
	}

	protected JTabbedPane jTabbedPaneCurrency = new JTabbedPane();

	public HouseholdsPanel() {
		setLayout(new BorderLayout());

		for (final Currency currency : Currency.values()) {
			final HouseholdsPanelForCurrency panelForCurrency = new HouseholdsPanelForCurrency(currency);
			jTabbedPaneCurrency.addTab(currency.getIso4217Code(), panelForCurrency);
			panelForCurrency.setBackground(Color.lightGray);
		}

		jTabbedPaneCurrency.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (e.getSource() instanceof JTabbedPane) {
					final JTabbedPane pane = (JTabbedPane) e.getSource();
					final HouseholdsPanelForCurrency selectedComponent = (HouseholdsPanelForCurrency) pane
							.getSelectedComponent();
					selectedComponent.notifyListener();
				}
			}
		});

		add(jTabbedPaneCurrency, BorderLayout.CENTER);
	}

	protected void addValueMarker(final JFreeChart chart, final double position, final String label) {
		final ValueMarker marker = new ValueMarker(position);
		marker.setPaint(Color.black);
		marker.setLabel(label);
		final XYPlot plot = (XYPlot) chart.getPlot();
		plot.addDomainMarker(marker);
	}

	protected ChartPanel createConsumptionSavingRatePanel(final Currency currency) {
		final TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.consumptionRateModel.getTimeSeries());
		timeSeriesCollection.addSeries(ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.savingRateModel.getTimeSeries());

		final JFreeChart chart = ChartFactory.createTimeSeriesChart("Consumption & Saving Rate", "Date",
				"Consumption & Saving Rate", timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createIncomeConsumptionSavingPanel(final Currency currency) {
		final TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.incomeModel.getTimeSeries());
		timeSeriesCollection.addSeries(ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.consumptionModel.getTimeSeries());
		timeSeriesCollection.addSeries(ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.savingModel.getTimeSeries());

		final JFreeChart chart = ChartFactory.createTimeSeriesChart("Consumption & Saving", "Date",
				"Consumption & Saving", timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected JFreeChart createIncomeDistributionPanel(final Currency currency) {
		final IntervalXYDataset dataset = ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.incomeDistributionModel.getHistogramDataset();
		final JFreeChart incomeDistributionChart = ChartFactory.createHistogram("Income Distribution", "Income",
				"% Households at Income", dataset, PlotOrientation.VERTICAL, true, false, false);
		return incomeDistributionChart;
	}

	protected ChartPanel createIncomeSourcePanel(final Currency currency) {
		final TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (final IncomeSource incomeSource : ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.incomeSourceModel.getIndexTypes()) {
			timeSeriesCollection.addSeries(ApplicationContext.getInstance().getModelRegistry()
					.getNationalEconomyModel(currency).householdsModel.incomeSourceModel.getTimeSeries(incomeSource));
		}

		final JFreeChart chart = ChartFactory.createTimeSeriesChart("Income Source", "Date", "Income Source",
				timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createLabourHourSupplyPanel(final Currency currency) {
		final TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.labourHourCapacityModel.getTimeSeries());
		timeSeriesCollection
				.addSeries(ApplicationContext.getInstance().getModelRegistry().getNationalEconomyModel(currency)
						.getPricingBehaviourModel(GoodType.LABOURHOUR).offerModel.getTimeSeries());
		timeSeriesCollection
				.addSeries(ApplicationContext.getInstance().getModelRegistry().getNationalEconomyModel(currency)
						.getPricingBehaviourModel(GoodType.LABOURHOUR).soldModel.getTimeSeries());

		final JFreeChart chart = ChartFactory.createTimeSeriesChart(GoodType.LABOURHOUR.toString() + " Supply", "Date",
				"Capacity & Output", timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createLorenzCurvePanel(final Currency currency) {
		final XYDataset dataset = ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.incomeDistributionModel.getLorenzCurveDataset();
		final JFreeChart lorenzCurveChart = ChartFactory.createXYLineChart("Lorenz Curve", "% of Households",
				"% of Income", dataset, PlotOrientation.VERTICAL, true, true, false);
		return new ChartPanel(lorenzCurveChart);
	}

	protected ChartPanel createMarketDepthPanel(final Currency currency) {
		final XYDataset dataset = ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).marketDepthModel.getMarketDepthDataset(currency,
						GoodType.LABOURHOUR);
		final JFreeChart chart = ChartFactory.createXYStepAreaChart(GoodType.LABOURHOUR + " Market Depth", "Price",
				"Volume", dataset, PlotOrientation.VERTICAL, true, true, false);
		return new ChartPanel(chart);
	}

	protected ChartPanel createPriceTimeSeriesChartPanel(final Currency currency) {
		final JFreeChart priceChart = ChartFactory.createCandlestickChart(GoodType.LABOURHOUR + " Prices", "Time",
				"Price in " + currency.getIso4217Code(), getDefaultHighLowDataset(currency), false);
		final ChartPanel chartPanel = new ChartPanel(priceChart);
		chartPanel.setDomainZoomable(true);
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 400));
		return chartPanel;
	}

	protected ChartPanel createUtilityFunctionMechanicsPanel(final Currency currency) {
		final TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.budgetModel.getTimeSeries());
		for (final ConvexFunctionTerminationCause terminationCause : ConvexFunctionTerminationCause.values()) {
			timeSeriesCollection.addSeries(ApplicationContext.getInstance().getModelRegistry()
					.getNationalEconomyModel(currency).householdsModel.convexFunctionTerminationCauseModels
							.get(terminationCause).getTimeSeries());
		}

		// budget is correct here
		final JFreeChart chart = ChartFactory.createTimeSeriesChart("Utility Function Mechanics", "Date",
				"Budget Spent", timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createUtilityPanel(final Currency currency) {
		final TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.utilityModel.utilityOutputModel.getTimeSeries());

		for (final GoodType inputGoodType : ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.utilityModel.utilityInputModels.keySet()) {
			timeSeriesCollection.addSeries(ApplicationContext.getInstance().getModelRegistry()
					.getNationalEconomyModel(currency).householdsModel.utilityModel.utilityInputModels
							.get(inputGoodType).getTimeSeries());
		}

		final JFreeChart chart = ChartFactory.createTimeSeriesChart("Households Utility", "Date", "Utility",
				timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createWageDividendPanel(final Currency currency) {
		final TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.wageModel.getTimeSeries());
		timeSeriesCollection.addSeries(ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.dividendModel.getTimeSeries());
		timeSeriesCollection.addSeries(ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.governmentTransfersModel.getTimeSeries());

		final JFreeChart chart = ChartFactory.createTimeSeriesChart("Wage, Dividend & Transfers", "Date",
				"Wage, Dividend & Transfers", timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected DefaultHighLowDataset getDefaultHighLowDataset(final Currency currency) {
		final PricesModel pricesModel = ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).pricesModel;

		final Map<GoodType, PriceModel> priceModelsForGoodType = pricesModel.getPriceModelsForGoodTypes();
		final PriceModel priceModel = priceModelsForGoodType.get(GoodType.LABOURHOUR);

		if (priceModel != null) {
			return new DefaultHighLowDataset("", priceModel.getDate(), priceModel.getHigh(), priceModel.getLow(),
					priceModel.getOpen(), priceModel.getClose(), priceModel.getVolume());
		}

		return null;
	}

	@Override
	public void notifyListener() {
		if (isShowing()) {
			final HouseholdsPanelForCurrency householdPanel = (HouseholdsPanelForCurrency) jTabbedPaneCurrency
					.getSelectedComponent();
			householdPanel.notifyListener();
		}
	}
}
