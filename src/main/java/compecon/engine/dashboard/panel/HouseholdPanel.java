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

import compecon.culture.sectors.financial.Currency;
import compecon.engine.jmx.model.ModelRegistry;
import compecon.engine.jmx.model.ModelRegistry.IncomeSource;

public class HouseholdPanel extends ChartsPanel {

	public HouseholdPanel() {
		this.setLayout(new GridLayout(0, 2));

		this.add(createConsumptionPanel());
		this.add(createConsumptionRatePanel());
		this.add(createSavingPanel());
		this.add(createWageDividendPanel());
		this.add(createIncomeSourcePanel());
	}

	protected ChartPanel createConsumptionPanel() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : ModelRegistry.getConsumptionModel().getTypes())
			timeSeriesCollection.addSeries(ModelRegistry.getConsumptionModel()
					.getTimeSeries(currency));

		for (Currency currency : ModelRegistry.getIncomeModel().getTypes())
			timeSeriesCollection.addSeries(ModelRegistry.getIncomeModel()
					.getTimeSeries(currency));

		JFreeChart chart = ChartFactory.createTimeSeriesChart("Consumption",
				"Date", "Consumption", timeSeriesCollection, true, true, false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createConsumptionRatePanel() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : ModelRegistry.getConsumptionRateModel()
				.getTypes())
			timeSeriesCollection.addSeries(ModelRegistry
					.getConsumptionRateModel().getTimeSeries(currency));

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Consumption Rate", "Date", "Consumption Rate",
				timeSeriesCollection, true, true, false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createSavingPanel() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : ModelRegistry.getSavingModel().getTypes())
			timeSeriesCollection.addSeries(ModelRegistry.getSavingModel()
					.getTimeSeries(currency));

		JFreeChart chart = ChartFactory.createTimeSeriesChart("Saving", "Date",
				"Saving", timeSeriesCollection, true, true, false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createWageDividendPanel() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : ModelRegistry.getWageModel().getTypes())
			timeSeriesCollection.addSeries(ModelRegistry.getWageModel()
					.getTimeSeries(currency));

		for (Currency currency : ModelRegistry.getDividendModel().getTypes())
			timeSeriesCollection.addSeries(ModelRegistry.getDividendModel()
					.getTimeSeries(currency));

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Wage / Dividend", "Date", "Wage / Dividend",
				timeSeriesCollection, true, true, false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createIncomeSourcePanel() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : ModelRegistry.getIncomeSourceModel()
				.getTypes()) {
			for (IncomeSource incomeSource : ModelRegistry
					.getIncomeSourceModel().getCategories()) {
				timeSeriesCollection.addSeries(ModelRegistry
						.getIncomeSourceModel().getTimeSeries(currency,
								incomeSource));
			}
		}

		JFreeChart chart = ChartFactory.createTimeSeriesChart("Income Source",
				"Date", "Income Source", timeSeriesCollection, true, true,
				false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}
}
