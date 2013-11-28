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
import org.jfree.data.xy.XYDataset;

import compecon.economy.materia.GoodType;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.statistics.NotificationListenerModel.ModelListener;

public class StatesPanel extends AbstractChartsPanel implements ModelListener {

	public class StatePanelForCurrency extends JPanel implements ModelListener {
		protected final Currency currency;

		public StatePanelForCurrency(Currency currency) {
			this.currency = currency;

			this.add(createStateBalanceSheetPanel(currency));
			this.add(createUtilityPanel(currency));

			this.setLayout(new GridLayout(0, 2));
		}

		@Override
		public void notifyListener() {
		}
	}

	protected final JTabbedPane jTabbedPaneCurrency = new JTabbedPane();

	public StatesPanel() {
		this.setLayout(new BorderLayout());

		for (Currency currency : Currency.values()) {
			JPanel panelForCurrency = new StatePanelForCurrency(currency);
			jTabbedPaneCurrency.addTab(currency.getIso4217Code(),
					panelForCurrency);
		}

		jTabbedPaneCurrency.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (e.getSource() instanceof JTabbedPane) {
					JTabbedPane pane = (JTabbedPane) e.getSource();
					StatePanelForCurrency selectedComponent = (StatePanelForCurrency) pane
							.getSelectedComponent();
					selectedComponent.notifyListener();
				}
			}
		});

		add(jTabbedPaneCurrency, BorderLayout.CENTER);
	}

	protected ChartPanel createUtilityPanel(Currency currency) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection
				.addSeries(ApplicationContext.getInstance().getModelRegistry()
						.getNationalEconomyModel(currency).stateModel.utilityModel.utilityOutputModel
						.getTimeSeries());

		for (GoodType inputGoodType : ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(currency).stateModel.utilityModel.utilityInputModels
				.keySet()) {
			timeSeriesCollection
					.addSeries(ApplicationContext.getInstance()
							.getModelRegistry()
							.getNationalEconomyModel(currency).stateModel.utilityModel.utilityInputModels
							.get(inputGoodType).getTimeSeries());
		}

		JFreeChart chart = ChartFactory.createTimeSeriesChart("State Utility",
				"Date", "Utility", (XYDataset) timeSeriesCollection, true,
				true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	@Override
	public synchronized void notifyListener() {
		if (this.isShowing()) {
			StatePanelForCurrency statePanelForCurrency = (StatePanelForCurrency) jTabbedPaneCurrency
					.getSelectedComponent();
			statePanelForCurrency.notifyListener();
		}
	}
}
