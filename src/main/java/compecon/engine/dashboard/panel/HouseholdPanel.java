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

import compecon.culture.sectors.financial.Currency;
import compecon.engine.jmx.model.Model.IModelListener;
import compecon.engine.jmx.model.ModelRegistry;
import compecon.engine.jmx.model.ModelRegistry.IncomeSource;
import compecon.engine.jmx.model.generic.DistributionModel.SummaryStatisticalData;

public class HouseholdPanel extends ChartsPanel implements IModelListener {

	protected final Map<Currency, JPanel> panelsForCurrencies = new HashMap<Currency, JPanel>();

	protected Map<Currency, JFreeChart> incomeDistributionCharts = new HashMap<Currency, JFreeChart>();

	public HouseholdPanel() {
		ModelRegistry.getIncomeDistributionModel().registerListener(this);

		this.setLayout(new BorderLayout());

		JTabbedPane jTabbedPane_Households = new JTabbedPane();

		// for each currency a panel is added that contains sub-panels with
		// prices for good types and currencies in this currency
		for (Currency currency : Currency.values()) {
			JPanel panelForCurrency = new JPanel();
			panelForCurrency.setLayout(new GridLayout(0, 2));
			this.panelsForCurrencies.put(currency, panelForCurrency);
			jTabbedPane_Households.addTab(currency.getIso4217Code(),
					panelForCurrency);

			panelForCurrency.setLayout(new GridLayout(0, 2));

			panelForCurrency.setBackground(Color.lightGray);

			panelForCurrency.add(createIncomeConsumptionSavingPanel(currency));
			panelForCurrency.add(createConsumptionSavingRatePanel(currency));
			panelForCurrency.add(createWageDividendPanel(currency));
			panelForCurrency.add(createIncomeSourcePanel(currency));
			panelForCurrency.add(createIncomeDistributionPanel(currency));
			panelForCurrency.add(createLorenzCurvePanel(currency));
		}

		add(jTabbedPane_Households, BorderLayout.CENTER);

	}

	protected ChartPanel createIncomeConsumptionSavingPanel(Currency currency) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ModelRegistry.getIncomeModel()
				.getTimeSeries(currency));
		timeSeriesCollection.addSeries(ModelRegistry.getConsumptionModel()
				.getTimeSeries(currency));
		timeSeriesCollection.addSeries(ModelRegistry.getSavingModel()
				.getTimeSeries(currency));

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Consumption & Saving", "Date", "Consumption & Saving",
				timeSeriesCollection, true, true, false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createConsumptionSavingRatePanel(Currency currency) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ModelRegistry.getConsumptionRateModel()
				.getTimeSeries(currency));
		timeSeriesCollection.addSeries(ModelRegistry.getSavingRateModel()
				.getTimeSeries(currency));

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Consumption & Saving Rate", "Date",
				"Consumption & Saving Rate", timeSeriesCollection, true, true,
				false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createWageDividendPanel(Currency currency) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ModelRegistry.getWageModel()
				.getTimeSeries(currency));
		timeSeriesCollection.addSeries(ModelRegistry.getDividendModel()
				.getTimeSeries(currency));

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Wage & Dividend", "Date", "Wage & Dividend",
				timeSeriesCollection, true, true, false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createIncomeSourcePanel(Currency currency) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (IncomeSource incomeSource : ModelRegistry.getIncomeSourceModel()
				.getCategories()) {
			timeSeriesCollection.addSeries(ModelRegistry.getIncomeSourceModel()
					.getTimeSeries(currency, incomeSource));
		}

		JFreeChart chart = ChartFactory.createTimeSeriesChart("Income Source",
				"Date", "Income Source", timeSeriesCollection, true, true,
				false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createIncomeDistributionPanel(Currency currency) {
		IntervalXYDataset dataset = ModelRegistry.getIncomeDistributionModel()
				.getHistogramDataset(currency);
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
		XYDataset dataset = ModelRegistry.getIncomeDistributionModel()
				.getLorenzCurveDataset(currency);
		JFreeChart lorenzCurveChart = ChartFactory.createXYLineChart(
				"Lorenz Curve", "% of Households", "% of Income", dataset,
				PlotOrientation.VERTICAL, true, true, false);
		return new ChartPanel(lorenzCurveChart);
	}

	@Override
	public void notifyListener() {
		for (Entry<Currency, JFreeChart> entry : this.incomeDistributionCharts
				.entrySet()) {
			Currency currency = entry.getKey();
			JFreeChart chart = entry.getValue();
			XYPlot plot = ((XYPlot) chart.getPlot());
			plot.setDataset(ModelRegistry.getIncomeDistributionModel()
					.getHistogramDataset(currency));

			plot.clearDomainMarkers();
			SummaryStatisticalData summaryStatisticalData = ModelRegistry
					.getIncomeDistributionModel().getSummaryStatisticalData(
							entry.getKey());
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
