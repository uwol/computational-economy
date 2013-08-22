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

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import compecon.economy.sectors.financial.Currency;
import compecon.engine.jmx.model.ModelRegistry;
import compecon.materia.GoodType;
import compecon.materia.InputOutputModel;

public class IndustryPanel extends AbstractChartsPanel {

	public IndustryPanel() {
		this.setLayout(new BorderLayout());

		JTabbedPane jTabbedPane = new JTabbedPane();

		for (Currency currency : Currency.values()) {
			JPanel panelForCurrency = new JPanel();
			panelForCurrency.setLayout(new GridLayout(0, 3));
			jTabbedPane.addTab(currency.getIso4217Code(), panelForCurrency);
			panelForCurrency.setBackground(Color.lightGray);

			panelForCurrency.add(createLabourPanel(currency));

			for (GoodType outputGoodType : GoodType.values()) {
				if (!outputGoodType.equals(GoodType.LABOURHOUR)) {
					panelForCurrency.add(createProductionPanel(currency,
							outputGoodType));
				}
			}
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

	protected ChartPanel createProductionPanel(Currency currency,
			GoodType outputGoodType) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ModelRegistry
				.getGoodTypeProductionModel(currency, outputGoodType)
				.getOutputModel().getTimeSeries());
		for (GoodType inputGoodType : ModelRegistry.getGoodTypeProductionModel(
				currency, outputGoodType).getInputGoodTypes()) {
			timeSeriesCollection.addSeries(ModelRegistry
					.getGoodTypeProductionModel(currency, outputGoodType)
					.getInputModel(inputGoodType).getTimeSeries());
		}

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				outputGoodType.toString() + " Output", "Date", "Output",
				(XYDataset) timeSeriesCollection, true, true, false);
		configureChart(chart);
		chart.addSubtitle(new TextTitle("Inputs: "
				+ InputOutputModel.getProductionFunction(outputGoodType)
						.getInputGoodTypes().toString()));
		return new ChartPanel(chart);
	}
}
