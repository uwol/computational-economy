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

package compecon.engine.dashboard.panel;

import java.awt.GridLayout;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import compecon.culture.sectors.financial.Currency;
import compecon.engine.jmx.model.ModelRegistry;
import compecon.nature.materia.GoodType;

public class AggregatesPanel extends ChartsPanel {

	public AggregatesPanel() {
		this.setLayout(new GridLayout(0, 2));

		this.add(this.createKeyInterestRatesChart());
		this.add(this.createPriceIndicesChart());
		this.add(this.createMoneySupplyChart());
		this.add(this.createProductionChart());
		this.add(this.createLabourChart());
	}

	private ChartPanel createKeyInterestRatesChart() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : ModelRegistry.getKeyInterestRateModel()
				.getTypes())
			timeSeriesCollection.addSeries(ModelRegistry
					.getKeyInterestRateModel().getTimeSeries(currency));

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Key Interest Rate", "Date", "Key Interest Rate",
				timeSeriesCollection, true, true, false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}

	private ChartPanel createPriceIndicesChart() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : ModelRegistry.getPriceIndexModel().getTypes())
			timeSeriesCollection.addSeries(ModelRegistry.getPriceIndexModel()
					.getTimeSeries(currency));

		JFreeChart chart = ChartFactory.createTimeSeriesChart("Price Index",
				"Date", "Price Index", timeSeriesCollection, true, true, false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}

	private ChartPanel createProductionChart() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (GoodType goodType : ModelRegistry
				.getEffectiveProductionOutputModel().getTypes())
			if (!goodType.equals(GoodType.LABOURHOUR))
				timeSeriesCollection.addSeries(ModelRegistry
						.getEffectiveProductionOutputModel().getTimeSeries(
								goodType));

		JFreeChart chart = ChartFactory.createTimeSeriesChart("Production",
				"Date", "Output", (XYDataset) timeSeriesCollection, true, true,
				false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}

	private ChartPanel createLabourChart() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ModelRegistry
				.getEffectiveProductionOutputModel().getTimeSeries(
						GoodType.LABOURHOUR));
		timeSeriesCollection.addSeries(ModelRegistry.getCapacityModel()
				.getTimeSeries(GoodType.LABOURHOUR));

		JFreeChart chart = ChartFactory.createTimeSeriesChart("Labour", "Date",
				"Capacity & Utilization", (XYDataset) timeSeriesCollection,
				true, true, false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}

	private ChartPanel createMoneySupplyChart() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : ModelRegistry.getMoneySupplyM0Model()
				.getTypes())
			timeSeriesCollection.addSeries(ModelRegistry
					.getMoneySupplyM0Model().getTimeSeries(currency));

		for (Currency currency : ModelRegistry.getMoneySupplyM1Model()
				.getTypes())
			timeSeriesCollection.addSeries(ModelRegistry
					.getMoneySupplyM1Model().getTimeSeries(currency));

		for (Currency currency : ModelRegistry.getMoneySupplyM2Model()
				.getTypes())
			timeSeriesCollection.addSeries(ModelRegistry
					.getMoneySupplyM2Model().getTimeSeries(currency));

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Money Supply to Non-Banks", "Date", "Money Supply",
				(XYDataset) timeSeriesCollection, true, true, false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}

	public ChartPanel createUtilityChart() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : ModelRegistry.getUtilityModel().getTypes())
			timeSeriesCollection.addSeries(ModelRegistry.getUtilityModel()
					.getTimeSeries(currency));

		JFreeChart chart = ChartFactory.createTimeSeriesChart("Utility",
				"Date", "Total Utility", (XYDataset) timeSeriesCollection,
				true, true, false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}
}