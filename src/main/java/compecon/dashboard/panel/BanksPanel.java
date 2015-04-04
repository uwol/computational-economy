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
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.statistics.NotificationListenerModel.ModelListener;
import compecon.engine.statistics.PricesModel;
import compecon.engine.statistics.PricesModel.PriceModel;

public class BanksPanel extends AbstractChartsPanel implements ModelListener {

	public class BanksPanelForCurrency extends JPanel implements ModelListener {

		protected final Currency currency;

		protected Map<Currency, JPanel> marketDepthPanel = new HashMap<Currency, JPanel>();

		protected Map<Currency, JPanel> priceTimeSeriesPanels = new HashMap<Currency, JPanel>();

		public BanksPanelForCurrency(final Currency currency) {
			this.currency = currency;

			setLayout(new GridLayout(0, 2));

			this.add(createCreditBankBalanceSheetPanel(currency));
			this.add(createCentralBankBalanceSheetPanel(currency));

			ApplicationContext.getInstance().getModelRegistry()
					.getNationalEconomyModel(currency).pricesModel
					.registerListener(this);
			// no registration with the market depth model, as they call
			// listeners synchronously

			notifyListener();
		}

		@Override
		public synchronized void notifyListener() {
			if (isShowing()) {
				// remove prices panels
				for (final Entry<Currency, JPanel> pricePanel : priceTimeSeriesPanels
						.entrySet()) {
					this.remove(pricePanel.getValue());
				}

				// remove market depth panels
				for (final Entry<Currency, JPanel> priceFunctionPanel : marketDepthPanel
						.entrySet()) {
					this.remove(priceFunctionPanel.getValue());
				}

				// add prices & market depth panels
				for (final Currency commodityCurrency : Currency.values()) {
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
		setLayout(new BorderLayout());

		for (final Currency currency : Currency.values()) {
			final JPanel panelForCurrency = new BanksPanelForCurrency(currency);
			jTabbedPaneCurrency.addTab(currency.getIso4217Code(),
					panelForCurrency);
		}

		jTabbedPaneCurrency.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (e.getSource() instanceof JTabbedPane) {
					final JTabbedPane pane = (JTabbedPane) e.getSource();
					final BanksPanelForCurrency selectedComponent = (BanksPanelForCurrency) pane
							.getSelectedComponent();
					selectedComponent.notifyListener();
				}
			}
		});

		add(jTabbedPaneCurrency, BorderLayout.CENTER);
	}

	protected ChartPanel createMarketDepthPanel(final Currency currency,
			final Currency commodityCurrency) {
		final XYDataset dataset = ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(currency).marketDepthModel
				.getMarketDepthDataset(currency, commodityCurrency);
		final JFreeChart chart = ChartFactory.createXYStepAreaChart(
				commodityCurrency.getIso4217Code() + " Market Depth", "Price",
				"Volume", dataset, PlotOrientation.VERTICAL, true, true, false);
		return new ChartPanel(chart);
	}

	protected ChartPanel createPriceTimeSeriesChartPanel(
			final Currency currency, final Currency commodityCurrency) {
		final JFreeChart priceChart = ChartFactory.createCandlestickChart(
				commodityCurrency.getIso4217Code() + " Prices", "Time",
				"Price in " + currency.getIso4217Code(),
				getDefaultHighLowDataset(currency, commodityCurrency), false);
		final ChartPanel chartPanel = new ChartPanel(priceChart);
		chartPanel.setDomainZoomable(true);
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 400));
		return chartPanel;
	}

	protected DefaultHighLowDataset getDefaultHighLowDataset(
			final Currency currency, final Currency commodityCurrency) {
		final PricesModel pricesModel = ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(currency).pricesModel;

		final Map<Currency, PriceModel> priceModelsForCurrencies = pricesModel
				.getPriceModelsForCurrencies();
		final PriceModel priceModel = priceModelsForCurrencies
				.get(commodityCurrency);

		if (priceModel != null) {
			return new DefaultHighLowDataset("", priceModel.getDate(),
					priceModel.getHigh(), priceModel.getLow(),
					priceModel.getOpen(), priceModel.getClose(),
					priceModel.getVolume());
		}

		return null;
	}

	@Override
	public void notifyListener() {
		if (isShowing()) {
			final BanksPanelForCurrency banksPanelForCurrency = (BanksPanelForCurrency) jTabbedPaneCurrency
					.getSelectedComponent();
			banksPanelForCurrency.notifyListener();
		}
	}
}