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
			panelForCurrency.setLayout(new GridLayout(0, 2));
			jTabbedPane.addTab(currency.getIso4217Code(), panelForCurrency);

			panelForCurrency.setLayout(new GridLayout(0, 2));
			panelForCurrency.setBackground(Color.lightGray);

			panelForCurrency.add(createLabourPanel(currency));

			for (GoodType goodType : ModelRegistry
					.getEffectiveProductionOutputModel(currency).getTypes())
				if (!goodType.equals(GoodType.LABOURHOUR)) {
					panelForCurrency.add(createProductionPanel(currency,
							goodType));
				}
		}

		add(jTabbedPane, BorderLayout.CENTER);
	}

	protected ChartPanel createLabourPanel(Currency currency) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ModelRegistry
				.getEffectiveProductionOutputModel(currency).getTimeSeries(
						GoodType.LABOURHOUR));
		timeSeriesCollection.addSeries(ModelRegistry.getCapacityModel(currency)
				.getTimeSeries(GoodType.LABOURHOUR));

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				GoodType.LABOURHOUR.toString() + " Output", "Date",
				"Capacity & Output", (XYDataset) timeSeriesCollection, true,
				true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createProductionPanel(Currency currency,
			GoodType goodType) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ModelRegistry
				.getEffectiveProductionOutputModel(currency).getTimeSeries(
						goodType));

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				goodType.toString() + " Output", "Date", "Output",
				(XYDataset) timeSeriesCollection, true, true, false);
		configureChart(chart);
		chart.addSubtitle(new TextTitle("Inputs: "
				+ InputOutputModel.getProductionFunction(goodType)
						.getInputGoodTypes().toString()));
		return new ChartPanel(chart);
	}
}
