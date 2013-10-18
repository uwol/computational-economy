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
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.XYDataset;

import compecon.economy.sectors.financial.Currency;
import compecon.engine.Simulation;
import compecon.engine.statistics.model.NotificationListenerModel.IModelListener;
import compecon.engine.statistics.model.PricesModel;
import compecon.engine.statistics.model.PricesModel.PriceModel;

public class BanksPanel extends AbstractChartsPanel implements IModelListener {

	public class BanksPanelForCurrency extends JPanel implements IModelListener {

		protected final Currency currency;

		protected Map<Currency, JPanel> priceTimeSeriesPanels = new HashMap<Currency, JPanel>();

		protected Map<Currency, JPanel> marketDepthPanel = new HashMap<Currency, JPanel>();

		public BanksPanelForCurrency(Currency currency) {
			this.currency = currency;

			this.setLayout(new GridLayout(0, 2));

			this.add(createCreditBankBalanceSheetPanel(currency));
			this.add(createCentralBankBalanceSheetPanel(currency));

			Simulation.getInstance().getModelRegistry()
					.getNationalEconomyModel(currency).pricesModel
					.registerListener(this);
			// no registration with the market depth model, as they call
			// listeners synchronously

			notifyListener();
		}

		@Override
		public void notifyListener() {
			if (this.isShowing()) {
				for (Entry<Currency, JPanel> pricePanel : priceTimeSeriesPanels
						.entrySet()) {
					this.remove(pricePanel.getValue());
				}

				for (Entry<Currency, JPanel> priceFunctionPanel : marketDepthPanel
						.entrySet()) {
					this.remove(priceFunctionPanel.getValue());
				}

				for (Currency commodityCurrency : Currency.values()) {
					if (!commodityCurrency.equals(currency)) {
						priceTimeSeriesPanels.put(
								commodityCurrency,
								createPriceTimeSeriesChartPanel(currency,
										commodityCurrency));
						this.add(priceTimeSeriesPanels.get(commodityCurrency));

						marketDepthPanel.put(
								commodityCurrency,
								createMarketDepthPanel(currency,
										commodityCurrency));
						this.add(marketDepthPanel.get(commodityCurrency));
					}
				}

				validate();
				repaint();
			}
		}
	}

	protected final JTabbedPane jTabbedPaneCurrency = new JTabbedPane();

	public BanksPanel() {
		this.setLayout(new BorderLayout());

		for (Currency currency : Currency.values()) {
			JPanel panelForCurrency = new BanksPanelForCurrency(currency);
			jTabbedPaneCurrency.addTab(currency.getIso4217Code(),
					panelForCurrency);
		}

		jTabbedPaneCurrency.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (e.getSource() instanceof JTabbedPane) {
					JTabbedPane pane = (JTabbedPane) e.getSource();
					BanksPanelForCurrency selectedComponent = (BanksPanelForCurrency) pane
							.getSelectedComponent();
					selectedComponent.notifyListener();
				}
			}
		});

		add(jTabbedPaneCurrency, BorderLayout.CENTER);
	}

	protected ChartPanel createPriceTimeSeriesChartPanel(Currency currency,
			Currency commodityCurrency) {
		JFreeChart priceChart = ChartFactory.createCandlestickChart(
				commodityCurrency.getIso4217Code() + " Prices", "Time",
				"Price in " + currency.getIso4217Code(),
				this.getDefaultHighLowDataset(currency, commodityCurrency),
				false);
		ChartPanel chartPanel = new ChartPanel(priceChart);
		chartPanel.setDomainZoomable(true);
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 400));
		return chartPanel;
	}

	protected ChartPanel createMarketDepthPanel(Currency currency,
			Currency commodityCurrency) {
		XYDataset dataset = Simulation.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).marketDepthModel
				.getMarketDepthDataset(currency, commodityCurrency);
		JFreeChart chart = ChartFactory.createXYStepAreaChart(
				commodityCurrency.getIso4217Code() + " Market Depth", "Price",
				"Volume", dataset, PlotOrientation.VERTICAL, true, true, false);
		return new ChartPanel(chart);
	}

	protected DefaultHighLowDataset getDefaultHighLowDataset(Currency currency,
			Currency commodityCurrency) {
		PricesModel pricesModel = Simulation.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).pricesModel;
		if (pricesModel.getPriceModelsForCurrencies().containsKey(currency)) {
			Map<Currency, PriceModel> priceModelsForCurrencies = pricesModel
					.getPriceModelsForCurrencies().get(currency);
			PriceModel priceModel = priceModelsForCurrencies
					.get(commodityCurrency);
			if (priceModel != null)
				return new DefaultHighLowDataset("", priceModel.getDate(),
						priceModel.getHigh(), priceModel.getLow(),
						priceModel.getOpen(), priceModel.getClose(),
						priceModel.getVolume());
		}
		return null;
	}

	@Override
	public void notifyListener() {
		if (this.isShowing()) {
			BanksPanelForCurrency banksPanelForCurrency = (BanksPanelForCurrency) jTabbedPaneCurrency
					.getSelectedComponent();
			banksPanelForCurrency.notifyListener();
		}
	}
}