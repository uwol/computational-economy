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
import java.awt.Component;
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
import org.jfree.data.xy.DefaultHighLowDataset;

import compecon.economy.sectors.financial.Currency;
import compecon.engine.Simulation;
import compecon.engine.statistics.model.PricesModel;
import compecon.engine.statistics.model.NotificationListenerModel.IModelListener;
import compecon.engine.statistics.model.PricesModel.PriceModel;
import compecon.materia.GoodType;

public class PricesPanel extends AbstractChartsPanel implements IModelListener {

	protected boolean refresh = false;

	protected Currency selectedCurrency = Currency.EURO;

	protected final Map<Currency, JPanel> panelsForCurrencies = new HashMap<Currency, JPanel>();

	public PricesPanel() {
		this.setLayout(new BorderLayout());

		JTabbedPane jTabbedPane_Prices = new JTabbedPane();

		// for each currency a panel is added that contains sub-panels with
		// prices for good types and currencies in this currency
		for (Currency currency : Currency.values()) {
			JPanel panelForPricesInCurrency = new JPanel();
			panelForPricesInCurrency.setLayout(new GridLayout(0, 3));
			this.panelsForCurrencies.put(currency, panelForPricesInCurrency);
			jTabbedPane_Prices.addTab(currency.getIso4217Code(),
					panelForPricesInCurrency);
			Simulation.getInstance().getModelRegistry()
					.getPricesModel(currency).registerListener(this);
		}

		jTabbedPane_Prices.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (e.getSource() instanceof JTabbedPane) {
					JTabbedPane pane = (JTabbedPane) e.getSource();
					Component selectecComponent = pane.getSelectedComponent();

					// log panel
					for (Entry<Currency, JPanel> panelForCurrency : panelsForCurrencies
							.entrySet()) {
						if (panelForCurrency.getValue().equals(
								selectecComponent)) {
							selectedCurrency = panelForCurrency.getKey();
						}
					}
				}
			}
		});

		add(jTabbedPane_Prices, BorderLayout.CENTER);
	}

	public void redrawPriceCharts() {
		if (this.refresh) {
			// redraw price charts
			for (Currency currency : Currency.values()) {
				if (currency.equals(this.selectedCurrency)) {
					JPanel panelForCurrency = this.panelsForCurrencies
							.get(currency);
					panelForCurrency.removeAll();

					for (Currency commodityCurrency : Currency.values()) {
						if (!currency.equals(commodityCurrency)) {
							// initially, add a new price chart panel for this
							// good
							// type
							ChartPanel chartPanel = this.createPriceChartPanel(
									currency, commodityCurrency);
							panelForCurrency.add(chartPanel);
						}
					}

					for (GoodType goodType : GoodType.values()) {
						ChartPanel chartPanel = this.createPriceChartPanel(
								currency, goodType);
						panelForCurrency.add(chartPanel);
					}
				}
			}
		}
		repaint();
	}

	protected JFreeChart createPriceChart(Currency currency, GoodType goodType) {
		return ChartFactory.createCandlestickChart("Price Chart for "
				+ goodType, "Time", "Price in " + currency.getIso4217Code(),
				PricesPanel.this.getDefaultHighLowDataset(currency, goodType),
				false);
	}

	protected JFreeChart createPriceChart(Currency currency,
			Currency commodityCurrency) {
		return ChartFactory.createCandlestickChart("Price Chart for "
				+ commodityCurrency, "Time",
				"Price in " + currency.getIso4217Code(), PricesPanel.this
						.getDefaultHighLowDataset(currency, commodityCurrency),
				false);
	}

	protected ChartPanel createPriceChartPanel(Currency currency,
			GoodType goodType) {
		ChartPanel chartPanel = new ChartPanel(createPriceChart(currency,
				goodType));
		chartPanel.setDomainZoomable(true);
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 400));
		return chartPanel;
	}

	protected ChartPanel createPriceChartPanel(Currency currency,
			Currency commodityCurrency) {
		ChartPanel chartPanel = new ChartPanel(createPriceChart(currency,
				commodityCurrency));
		chartPanel.setDomainZoomable(true);
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 400));
		return chartPanel;
	}

	public DefaultHighLowDataset getDefaultHighLowDataset(Currency currency,
			GoodType goodType) {
		PricesModel pricesModel = Simulation.getInstance().getModelRegistry()
				.getPricesModel(currency);
		if (pricesModel.getPriceModelsForGoodTypes().containsKey(currency)) {
			Map<GoodType, PriceModel> priceModelsForGoodType = pricesModel
					.getPriceModelsForGoodTypes().get(currency);
			PriceModel priceModel = priceModelsForGoodType.get(goodType);
			if (priceModel != null)
				return new DefaultHighLowDataset("", priceModel.getDate(),
						priceModel.getHigh(), priceModel.getLow(),
						priceModel.getOpen(), priceModel.getClose(),
						priceModel.getVolume());
		}
		return null;
	}

	public DefaultHighLowDataset getDefaultHighLowDataset(Currency currency,
			Currency commodityCurrency) {
		PricesModel pricesModel = Simulation.getInstance().getModelRegistry()
				.getPricesModel(currency);
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
		this.redrawPriceCharts();
	}

	/**
	 * disables / enables redrawing of panels for performance reasons
	 */
	public void setRefresh(boolean refresh) {
		this.refresh = refresh;
	}

}
