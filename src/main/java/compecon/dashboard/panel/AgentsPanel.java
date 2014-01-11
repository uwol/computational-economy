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

package compecon.dashboard.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import compecon.economy.agent.Agent;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.statistics.NotificationListenerModel.ModelListener;

public class AgentsPanel extends AbstractChartsPanel implements ModelListener {

	public AgentsPanel() {

		this.setLayout(new BorderLayout());

		JTabbedPane jTabbedPane = new JTabbedPane();

		for (Currency currency : Currency.values()) {
			JPanel panelForCurrency = new JPanel();
			panelForCurrency.setLayout(new GridLayout(0, 2));
			jTabbedPane.addTab(currency.getIso4217Code(), panelForCurrency);

			panelForCurrency.setBackground(Color.lightGray);

			for (Class<? extends Agent> agentType : ApplicationContext
					.getInstance().getAgentFactory().getAgentTypes()) {
				panelForCurrency
						.add(createAgentNumberPanel(currency, agentType));
			}
		}

		add(jTabbedPane, BorderLayout.CENTER);
	}

	protected ChartPanel createAgentNumberPanel(Currency currency,
			Class<? extends Agent> agentType) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection
				.addSeries(ApplicationContext.getInstance().getModelRegistry()
						.getNationalEconomyModel(currency).numberOfAgentsModels
						.get(agentType).getTimeSeries());

		// in case of households
		if (Household.class.isAssignableFrom(agentType)) {
			// show retired households
			timeSeriesCollection
					.addSeries(ApplicationContext.getInstance()
							.getModelRegistry()
							.getNationalEconomyModel(currency).householdsModel.retiredModel
							.getTimeSeries());
		}

		JFreeChart chart = ChartFactory
				.createTimeSeriesChart("# " + agentType.getSimpleName()
						+ " Agents", "Date", "# Agents",
						(XYDataset) timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	@Override
	public void notifyListener() {
	}
}
