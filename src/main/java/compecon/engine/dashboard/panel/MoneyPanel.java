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

import java.awt.GridLayout;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import compecon.economy.sectors.financial.Currency;
import compecon.engine.jmx.model.ModelRegistry;

public class MoneyPanel extends AbstractChartsPanel {

	public MoneyPanel() {
		this.setLayout(new GridLayout(0, 2));

		this.add(createKeyInterestRatesPanel());
		this.add(createPriceIndicesPanel());
		this.add(createMoneySupplyPanel());
	}

	protected ChartPanel createKeyInterestRatesPanel() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : ModelRegistry.getKeyInterestRateModel()
				.getTypes())
			timeSeriesCollection.addSeries(ModelRegistry
					.getKeyInterestRateModel().getTimeSeries(currency));

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Key Interest Rate", "Date", "Key Interest Rate",
				timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createPriceIndicesPanel() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : ModelRegistry.getPriceIndexModel().getTypes())
			timeSeriesCollection.addSeries(ModelRegistry.getPriceIndexModel()
					.getTimeSeries(currency));

		JFreeChart chart = ChartFactory.createTimeSeriesChart("Price Index",
				"Date", "Price Index", timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createMoneySupplyPanel() {
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
		configureChart(chart);
		return new ChartPanel(chart);
	}
}