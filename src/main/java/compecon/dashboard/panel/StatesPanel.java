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
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeriesCollection;

import compecon.economy.materia.GoodType;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.statistics.NotificationListenerModel.ModelListener;

public class StatesPanel extends AbstractChartsPanel implements ModelListener {

	public class StatePanelForCurrency extends JPanel implements ModelListener {
		protected final Currency currency;

		public StatePanelForCurrency(final Currency currency) {
			this.currency = currency;

			this.add(createStateBalanceSheetPanel(currency));
			this.add(createUtilityPanel(currency));
			this.add(createGovernmentTransfersPanel(currency));

			setLayout(new GridLayout(0, 2));
		}

		@Override
		public void notifyListener() {
		}
	}

	protected final JTabbedPane jTabbedPaneCurrency = new JTabbedPane();

	public StatesPanel() {
		setLayout(new BorderLayout());

		for (final Currency currency : Currency.values()) {
			final JPanel panelForCurrency = new StatePanelForCurrency(currency);
			jTabbedPaneCurrency.addTab(currency.getIso4217Code(),
					panelForCurrency);
		}

		jTabbedPaneCurrency.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (e.getSource() instanceof JTabbedPane) {
					final JTabbedPane pane = (JTabbedPane) e.getSource();
					final StatePanelForCurrency selectedComponent = (StatePanelForCurrency) pane
							.getSelectedComponent();
					selectedComponent.notifyListener();
				}
			}
		});

		add(jTabbedPaneCurrency, BorderLayout.CENTER);
	}

	protected ChartPanel createGovernmentTransfersPanel(final Currency currency) {
		final TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection
				.addSeries(ApplicationContext.getInstance().getModelRegistry()
						.getNationalEconomyModel(currency).householdsModel.governmentTransfersModel
						.getTimeSeries());

		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Government Transfers", "Date", "Government Transfers",
				timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createUtilityPanel(final Currency currency) {
		final TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection
				.addSeries(ApplicationContext.getInstance().getModelRegistry()
						.getNationalEconomyModel(currency).stateModel.utilityModel.utilityOutputModel
						.getTimeSeries());

		for (final GoodType inputGoodType : ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(currency).stateModel.utilityModel.utilityInputModels
				.keySet()) {
			timeSeriesCollection
					.addSeries(ApplicationContext.getInstance()
							.getModelRegistry()
							.getNationalEconomyModel(currency).stateModel.utilityModel.utilityInputModels
							.get(inputGoodType).getTimeSeries());
		}

		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"State Utility", "Date", "Utility", timeSeriesCollection, true,
				true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	@Override
	public synchronized void notifyListener() {
		if (isShowing()) {
			final StatePanelForCurrency statePanelForCurrency = (StatePanelForCurrency) jTabbedPaneCurrency
					.getSelectedComponent();
			statePanelForCurrency.notifyListener();
		}
	}
}
