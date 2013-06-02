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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultHighLowDataset;

import compecon.culture.sectors.financial.Currency;
import compecon.engine.jmx.model.Model.IModelListener;
import compecon.engine.jmx.model.ModelRegistry;
import compecon.engine.jmx.model.PricesModel;
import compecon.engine.jmx.model.PricesModel.PriceModel;
import compecon.nature.materia.GoodType;

public class PricesPanel extends JPanel implements IModelListener {

	protected boolean noRefresh = true;

	protected final Map<Currency, JPanel> panelsForCurrencies = new HashMap<Currency, JPanel>();

	protected final Map<Currency, Map<GoodType, ChartPanel>> priceChartPanelsForGoodTypes = new HashMap<Currency, Map<GoodType, ChartPanel>>();

	protected final Map<Currency, Map<Currency, ChartPanel>> priceChartPanelsForCurrencies = new HashMap<Currency, Map<Currency, ChartPanel>>();

	public PricesPanel() {
		ModelRegistry.getPricesModel().registerListener(this);

		this.setLayout(new BorderLayout());

		JTabbedPane jTabbedPane_Prices = new JTabbedPane();

		// initialize maps for storing price panels, which are contained in
		// following panels
		for (Currency currency : Currency.values()) {
			this.priceChartPanelsForGoodTypes.put(currency,
					new HashMap<GoodType, ChartPanel>());
			this.priceChartPanelsForCurrencies.put(currency,
					new HashMap<Currency, ChartPanel>());
		}

		// for each currency a panel is added that contains sub-panels with
		// prices for good types and currencies in this currency
		for (Currency currency : Currency.values()) {
			JPanel panelForPricesOfGoodTypes = new JPanel();
			panelForPricesOfGoodTypes.setLayout(new GridLayout(0, 2));
			this.panelsForCurrencies.put(currency, panelForPricesOfGoodTypes);
			jTabbedPane_Prices.addTab(currency.getIso4217Code(),
					panelForPricesOfGoodTypes);
		}

		add(jTabbedPane_Prices, BorderLayout.CENTER);
	}

	public void redrawPriceCharts() {
		if (!this.noRefresh) {
			// redraw price charts
			for (Currency currency : Currency.values()) {
				for (Currency commodityCurrency : Currency.values()) {
					if (currency != commodityCurrency) {
						// initially, add a new price chart panel for this good
						// type
						if (!this.priceChartPanelsForCurrencies.get(currency)
								.containsKey(commodityCurrency)) {
							ChartPanel chartPanel = this.createPriceChartPanel(
									currency, commodityCurrency);
							this.priceChartPanelsForCurrencies.get(currency)
									.put(commodityCurrency, chartPanel);
							this.panelsForCurrencies.get(currency).add(
									chartPanel);
						}
						// or refresh the chart of an existing chart panel
						else
							this.priceChartPanelsForCurrencies
									.get(currency)
									.get(commodityCurrency)
									.setChart(
											createPriceChart(currency,
													commodityCurrency));
					}
				}

				for (GoodType goodType : GoodType.values()) {
					// initially, add a new price chart panel for this good type
					if (!this.priceChartPanelsForGoodTypes.get(currency)
							.containsKey(goodType)) {
						ChartPanel chartPanel = this.createPriceChartPanel(
								currency, goodType);
						this.priceChartPanelsForGoodTypes.get(currency).put(
								goodType, chartPanel);
						this.panelsForCurrencies.get(currency).add(chartPanel);
					}
					// or refresh the chart of an existing chart panel
					else
						this.priceChartPanelsForGoodTypes.get(currency)
								.get(goodType)
								.setChart(createPriceChart(currency, goodType));
				}
			}
			this.revalidate();
			setVisible(true);
		}
	}

	private JFreeChart createPriceChart(Currency currency, GoodType goodType) {
		return ChartFactory.createCandlestickChart("Price Chart for "
				+ goodType, "Time", "Price in " + currency.getIso4217Code(),
				PricesPanel.this.getDefaultHighLowDataset(currency, goodType),
				false);
	}

	private JFreeChart createPriceChart(Currency currency,
			Currency commodityCurrency) {
		return ChartFactory.createCandlestickChart("Price Chart for "
				+ commodityCurrency, "Time",
				"Price in " + currency.getIso4217Code(), PricesPanel.this
						.getDefaultHighLowDataset(currency, commodityCurrency),
				false);
	}

	private ChartPanel createPriceChartPanel(Currency currency,
			GoodType goodType) {
		ChartPanel chartPanel = new ChartPanel(createPriceChart(currency,
				goodType));
		chartPanel.setDomainZoomable(true);
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 400));
		return chartPanel;
	}

	private ChartPanel createPriceChartPanel(Currency currency,
			Currency commodityCurrency) {
		ChartPanel chartPanel = new ChartPanel(createPriceChart(currency,
				commodityCurrency));
		chartPanel.setDomainZoomable(true);
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 400));
		return chartPanel;
	}

	public DefaultHighLowDataset getDefaultHighLowDataset(Currency currency,
			GoodType goodType) {
		PricesModel pricesModel = ModelRegistry.getPricesModel();
		if (pricesModel.getPriceModelsForGoodTypes().containsKey(currency)) {
			Map<GoodType, PriceModel> priceModelsForGoodType = pricesModel
					.getPriceModelsForGoodTypes().get(currency);
			PriceModel priceModel = priceModelsForGoodType.get(goodType);
			return new DefaultHighLowDataset("", priceModel.getDate(),
					priceModel.getHigh(), priceModel.getLow(),
					priceModel.getOpen(), priceModel.getClose(),
					priceModel.getVolume());
		}
		return null;
	}

	public DefaultHighLowDataset getDefaultHighLowDataset(Currency currency,
			Currency commodityCurrency) {
		PricesModel pricesModel = ModelRegistry.getPricesModel();
		if (pricesModel.getPriceModelsForCurrencies().containsKey(currency)) {
			Map<Currency, PriceModel> priceModelsForCurrencies = pricesModel
					.getPriceModelsForCurrencies().get(currency);
			PriceModel priceModel = priceModelsForCurrencies
					.get(commodityCurrency);
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
	 * disables / enables redrawing of price panels for performance reasons
	 */
	public void setNoRefresh(boolean noRefresh) {
		this.noRefresh = noRefresh;
	}
}
