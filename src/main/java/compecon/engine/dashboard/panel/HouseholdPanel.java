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
import java.awt.Color;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;

import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.engine.dashboard.panel.BalanceSheetPanel.BalanceSheetTableModel;
import compecon.engine.jmx.model.ModelRegistry;
import compecon.engine.jmx.model.ModelRegistry.IncomeSource;
import compecon.engine.jmx.model.NotificationListenerModel.IModelListener;
import compecon.engine.jmx.model.PeriodDataDistributionModel.SummaryStatisticalData;
import compecon.materia.GoodType;

public class HouseholdPanel extends AbstractChartsPanel implements
		IModelListener {

	protected Map<Currency, JFreeChart> incomeDistributionCharts = new HashMap<Currency, JFreeChart>();

	public HouseholdPanel() {
		for (Currency currency : Currency.values())
			ModelRegistry.getIncomeDistributionModel(currency)
					.registerListener(this);

		this.setLayout(new BorderLayout());

		JTabbedPane jTabbedPane = new JTabbedPane();

		for (Currency currency : Currency.values()) {
			JPanel panelForCurrency = new JPanel();
			jTabbedPane.addTab(currency.getIso4217Code(), panelForCurrency);
			panelForCurrency.setLayout(new GridLayout(0, 3));
			panelForCurrency.setBackground(Color.lightGray);

			// panelForCurrency.add(AgentsPanel.createAgentNumberPanel(currency,
			// Household.class));
			panelForCurrency.add(createLabourPanel(currency));
			panelForCurrency.add(createUtilityPanel(currency));
			panelForCurrency.add(createIncomeConsumptionSavingPanel(currency));
			panelForCurrency.add(createConsumptionSavingRatePanel(currency));
			panelForCurrency.add(createWageDividendPanel(currency));
			panelForCurrency.add(createIncomeSourcePanel(currency));
			panelForCurrency.add(createIncomeDistributionPanel(currency));
			panelForCurrency.add(createLorenzCurvePanel(currency));
			panelForCurrency.add(createHouseholdBalanceSheetPanel(currency));
		}

		add(jTabbedPane, BorderLayout.CENTER);
	}

	protected ChartPanel createLabourPanel(Currency currency) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ModelRegistry
				.getGoodTypeProductionModel(currency, GoodType.LABOURHOUR)
				.getOutputModel().getTimeSeries());
		timeSeriesCollection.addSeries(ModelRegistry
				.getLabourHourCapacityModel(currency).getTimeSeries());

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				GoodType.LABOURHOUR.toString() + " Output", "Date",
				"Capacity & Output", (XYDataset) timeSeriesCollection, true,
				true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createUtilityPanel(Currency currency) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ModelRegistry.getUtilityModel(currency)
				.getOutputModel().getTimeSeries());

		for (GoodType inputGoodType : ModelRegistry.getUtilityModel(currency)
				.getInputGoodTypes()) {
			timeSeriesCollection.addSeries(ModelRegistry
					.getUtilityModel(currency).getInputModel(inputGoodType)
					.getTimeSeries());
		}

		JFreeChart chart = ChartFactory.createTimeSeriesChart("Utility",
				"Date", "Total Utility", (XYDataset) timeSeriesCollection,
				true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createIncomeConsumptionSavingPanel(Currency currency) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ModelRegistry.getIncomeModel(currency)
				.getTimeSeries());
		timeSeriesCollection.addSeries(ModelRegistry.getConsumptionModel(
				currency).getTimeSeries());
		timeSeriesCollection.addSeries(ModelRegistry.getSavingModel(currency)
				.getTimeSeries());

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Consumption & Saving", "Date", "Consumption & Saving",
				timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createConsumptionSavingRatePanel(Currency currency) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ModelRegistry.getConsumptionRateModel(
				currency).getTimeSeries());
		timeSeriesCollection.addSeries(ModelRegistry.getSavingRateModel(
				currency).getTimeSeries());

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Consumption & Saving Rate", "Date",
				"Consumption & Saving Rate", timeSeriesCollection, true, true,
				false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createWageDividendPanel(Currency currency) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ModelRegistry.getWageModel(currency)
				.getTimeSeries());
		timeSeriesCollection.addSeries(ModelRegistry.getDividendModel(currency)
				.getTimeSeries());

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Wage & Dividend", "Date", "Wage & Dividend",
				timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createIncomeSourcePanel(Currency currency) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (IncomeSource incomeSource : ModelRegistry.getIncomeSourceModel(
				currency).getIndexTypes()) {
			timeSeriesCollection.addSeries(ModelRegistry.getIncomeSourceModel(
					currency).getTimeSeries(incomeSource));
		}

		JFreeChart chart = ChartFactory.createTimeSeriesChart("Income Source",
				"Date", "Income Source", timeSeriesCollection, true, true,
				false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createIncomeDistributionPanel(Currency currency) {
		IntervalXYDataset dataset = ModelRegistry.getIncomeDistributionModel(
				currency).getHistogramDataset();
		JFreeChart incomeDistributionChart = ChartFactory.createHistogram(
				"Income Distribution", "Income", "% Households at Income",
				dataset, PlotOrientation.VERTICAL, true, false, false);
		this.incomeDistributionCharts.put(currency, incomeDistributionChart);
		return new ChartPanel(incomeDistributionChart);
	}

	protected void addValueMarker(JFreeChart chart, double position,
			String label) {
		ValueMarker marker = new ValueMarker(position);
		marker.setPaint(Color.black);
		marker.setLabel(label);
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.addDomainMarker(marker);
	}

	protected ChartPanel createLorenzCurvePanel(Currency currency) {
		XYDataset dataset = ModelRegistry.getIncomeDistributionModel(currency)
				.getLorenzCurveDataset();
		JFreeChart lorenzCurveChart = ChartFactory.createXYLineChart(
				"Lorenz Curve", "% of Households", "% of Income", dataset,
				PlotOrientation.VERTICAL, true, true, false);
		return new ChartPanel(lorenzCurveChart);
	}

	protected JPanel createHouseholdBalanceSheetPanel(Currency currency) {
		final BalanceSheetTableModel balanceSheetTableModel = new BalanceSheetTableModel(
				currency) {
			@Override
			protected BalanceSheet getModelData() {
				return ModelRegistry.getBalanceSheetsModel(referenceCurrency)
						.getHouseholdNationalAccountsBalanceSheet();
			}
		};
		return new BalanceSheetPanel(currency, balanceSheetTableModel,
				"Balance Sheet for " + currency.getIso4217Code()
						+ " Households");
	}

	@Override
	public void notifyListener() {
		for (Entry<Currency, JFreeChart> entry : this.incomeDistributionCharts
				.entrySet()) {
			Currency currency = entry.getKey();
			JFreeChart chart = entry.getValue();
			XYPlot plot = ((XYPlot) chart.getPlot());
			plot.setDataset(ModelRegistry.getIncomeDistributionModel(currency)
					.getHistogramDataset());

			plot.clearDomainMarkers();
			SummaryStatisticalData summaryStatisticalData = ModelRegistry
					.getIncomeDistributionModel(currency)
					.getSummaryStatisticalData();
			if (summaryStatisticalData.originalValues != null) {
				addValueMarker(
						chart,
						summaryStatisticalData.originalValues[summaryStatisticalData.xWith10PercentY],
						"10 %");
				addValueMarker(
						chart,
						summaryStatisticalData.originalValues[summaryStatisticalData.xWith20PercentY],
						"20 %");
				addValueMarker(
						chart,
						summaryStatisticalData.originalValues[summaryStatisticalData.xWith30PercentY],
						"30 %");
				addValueMarker(
						chart,
						summaryStatisticalData.originalValues[summaryStatisticalData.xWith40PercentY],
						"40 %");
				addValueMarker(
						chart,
						summaryStatisticalData.originalValues[summaryStatisticalData.xWith50PercentY],
						"50 %");
				addValueMarker(
						chart,
						summaryStatisticalData.originalValues[summaryStatisticalData.xWith60PercentY],
						"60 %");
				addValueMarker(
						chart,
						summaryStatisticalData.originalValues[summaryStatisticalData.xWith70PercentY],
						"70 %");
				addValueMarker(
						chart,
						summaryStatisticalData.originalValues[summaryStatisticalData.xWith80PercentY],
						"80 %");
				addValueMarker(
						chart,
						summaryStatisticalData.originalValues[summaryStatisticalData.xWith90PercentY],
						"90 %");
			}
		}
	}
}
