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

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeriesCollection;

import io.github.uwol.compecon.economy.agent.Agent;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.economy.sectors.household.Household;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.statistics.NotificationListenerModel.ModelListener;

public class AgentsPanel extends AbstractChartsPanel implements ModelListener {

	private static final long serialVersionUID = 1L;

	public AgentsPanel() {

		setLayout(new BorderLayout());

		final JTabbedPane jTabbedPane = new JTabbedPane();

		for (final Currency currency : Currency.values()) {
			final JPanel panelForCurrency = new JPanel();
			panelForCurrency.setLayout(new GridLayout(0, 2));
			jTabbedPane.addTab(currency.getIso4217Code(), panelForCurrency);

			panelForCurrency.setBackground(Color.lightGray);

			for (final Class<? extends Agent> agentType : ApplicationContext.getInstance().getAgentFactory()
					.getAgentTypes()) {
				panelForCurrency.add(createAgentNumberPanel(currency, agentType));
			}
		}

		add(jTabbedPane, BorderLayout.CENTER);
	}

	protected ChartPanel createAgentNumberPanel(final Currency currency, final Class<? extends Agent> agentType) {
		final TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).numberOfAgentsModels.get(agentType).getTimeSeries());

		// in case of households
		if (Household.class.isAssignableFrom(agentType)) {
			// show retired households
			timeSeriesCollection.addSeries(ApplicationContext.getInstance().getModelRegistry()
					.getNationalEconomyModel(currency).householdsModel.retiredModel.getTimeSeries());
		}

		final JFreeChart chart = ChartFactory.createTimeSeriesChart("# " + agentType.getSimpleName() + " Agents",
				"Date", "# Agents", timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	@Override
	public void notifyListener() {
	}
}
