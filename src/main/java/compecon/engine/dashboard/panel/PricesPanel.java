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
import compecon.engine.jmx.model.ModelRegistry;
import compecon.engine.jmx.model.PricesModel;
import compecon.engine.jmx.model.PricesModel.PriceModel;
import compecon.nature.materia.GoodType;

public class PricesPanel extends JPanel {

	protected boolean noRedraw = true;

	protected final Map<Currency, JPanel> panelsForCurrencies = new HashMap<Currency, JPanel>();

	protected final Map<Currency, Map<GoodType, ChartPanel>> priceChartPanels = new HashMap<Currency, Map<GoodType, ChartPanel>>();

	public PricesPanel() {
		this.setLayout(new BorderLayout());

		JTabbedPane jTabbedPane_Prices = new JTabbedPane();

		for (Currency currency : Currency.values()) {
			JPanel panelForPricesOfGoodTypes = new JPanel();
			panelForPricesOfGoodTypes.setLayout(new GridLayout(0, 2));
			this.panelsForCurrencies.put(currency, panelForPricesOfGoodTypes);
			this.priceChartPanels.put(currency,
					new HashMap<GoodType, ChartPanel>());
			jTabbedPane_Prices.addTab(currency.getIso4217Code(),
					panelForPricesOfGoodTypes);
		}
		add(jTabbedPane_Prices, BorderLayout.CENTER);
	}

	private JFreeChart createPriceChart(Currency currency, GoodType goodType) {
		return ChartFactory.createCandlestickChart("Price Chart for "
				+ goodType, "Time", "Price in " + currency.getIso4217Code(),
				PricesPanel.this.getDefaultHighLowDataset(currency, goodType),
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

	public void redrawPriceCharts() {
		if (!this.noRedraw) {
			// redraw price charts
			for (Currency currency : Currency.values()) {
				for (GoodType goodType : GoodType.values()) {
					if (!this.priceChartPanels.get(currency).containsKey(
							goodType)) {
						ChartPanel chartPanel = this.createPriceChartPanel(
								currency, goodType);
						this.priceChartPanels.get(currency).put(goodType,
								chartPanel);
						this.panelsForCurrencies.get(currency).add(chartPanel);
					} else
						this.priceChartPanels.get(currency).get(goodType)
								.setChart(createPriceChart(currency, goodType));
				}
			}
			this.revalidate();
			setVisible(true);
		}
	}

	public void noRedraw(boolean noRedraw) {
		this.noRedraw = noRedraw;
	}

	public DefaultHighLowDataset getDefaultHighLowDataset(Currency currency,
			GoodType goodType) {
		PricesModel pricesModel = ModelRegistry.getPricesModel();
		if (pricesModel.getPriceModels().containsKey(currency)) {
			Map<GoodType, PriceModel> priceModelsForGoodType = pricesModel
					.getPriceModels().get(currency);
			PriceModel priceModel = priceModelsForGoodType.get(goodType);
			return new DefaultHighLowDataset("", priceModel.getDate(),
					priceModel.getHigh(), priceModel.getLow(),
					priceModel.getOpen(), priceModel.getClose(),
					priceModel.getVolume());
		}
		return null;
	}
}
