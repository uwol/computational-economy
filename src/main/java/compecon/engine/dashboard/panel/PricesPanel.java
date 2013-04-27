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

import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import compecon.culture.sectors.financial.Currency;
import compecon.engine.dashboard.model.PricesModel;
import compecon.nature.materia.GoodType;

public class PricesPanel extends JPanel {

	protected boolean blockRedraw = true;

	protected final PricesModel pricesModel;

	protected final Map<GoodType, ChartPanel> priceCharts = new HashMap<GoodType, ChartPanel>();

	public PricesPanel(final PricesModel pricesModel) {
		this.pricesModel = pricesModel;

		this.setLayout(new GridLayout(0, 2));
	}

	private JFreeChart createPriceChart(GoodType goodType) {
		return ChartFactory.createCandlestickChart("Price Chart for "
				+ goodType, "Time", "Price in " + Currency.EURO,
				this.pricesModel.getDefaultHighLowDataset(goodType), false);
	}

	private ChartPanel createPriceChartPanel(GoodType goodType) {
		ChartPanel chartPanel = new ChartPanel(createPriceChart(goodType));
		chartPanel.setDomainZoomable(true);
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 400));
		return chartPanel;
	}

	public void redrawPriceCharts() {
		if (!this.blockRedraw) {
			// redraw price charts
			for (GoodType goodType : GoodType.values()) {
				if (!this.priceCharts.containsKey(goodType)) {
					ChartPanel chartPanel = this
							.createPriceChartPanel(goodType);
					this.priceCharts.put(goodType, chartPanel);
					this.add(chartPanel);
				} else
					this.priceCharts.get(goodType).setChart(
							createPriceChart(goodType));
			}
			this.revalidate();
			setVisible(true);
		}
	}

	public void blockRedraw(boolean blockRedraw) {
		this.blockRedraw = blockRedraw;
	}
}
