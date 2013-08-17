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

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import compecon.economy.sectors.financial.Currency;
import compecon.engine.jmx.model.ModelRegistry;
import compecon.materia.GoodType;

public class IndustryPanel extends ChartsPanel {

	protected final Map<Currency, JPanel> panelsForCurrencies = new HashMap<Currency, JPanel>();

	public IndustryPanel() {
		this.setLayout(new BorderLayout());

		JTabbedPane jTabbedPane_Industries = new JTabbedPane();

		for (Currency currency : Currency.values()) {
			JPanel panelForCurrency = new JPanel();
			panelForCurrency.setLayout(new GridLayout(0, 2));
			this.panelsForCurrencies.put(currency, panelForCurrency);
			jTabbedPane_Industries.addTab(currency.getIso4217Code(),
					panelForCurrency);

			panelForCurrency.setLayout(new GridLayout(0, 2));

			panelForCurrency.setBackground(Color.lightGray);

			panelForCurrency.add(createProductionPanel(currency));
			panelForCurrency.add(createLabourPanel(currency));

		}

		add(jTabbedPane_Industries, BorderLayout.CENTER);
	}

	private ChartPanel createProductionPanel(Currency currency) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (GoodType goodType : ModelRegistry
				.getEffectiveProductionOutputModel(currency).getTypes())
			if (!goodType.equals(GoodType.LABOURHOUR))
				timeSeriesCollection.addSeries(ModelRegistry
						.getEffectiveProductionOutputModel(currency)
						.getTimeSeries(goodType));

		JFreeChart chart = ChartFactory.createTimeSeriesChart("Production",
				"Date", "Output", (XYDataset) timeSeriesCollection, true, true,
				false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}

	private ChartPanel createLabourPanel(Currency currency) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ModelRegistry
				.getEffectiveProductionOutputModel(currency).getTimeSeries(
						GoodType.LABOURHOUR));
		timeSeriesCollection.addSeries(ModelRegistry.getCapacityModel(currency)
				.getTimeSeries(GoodType.LABOURHOUR));

		JFreeChart chart = ChartFactory.createTimeSeriesChart("Labour", "Date",
				"Capacity & Utilization", (XYDataset) timeSeriesCollection,
				true, true, false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}
}
