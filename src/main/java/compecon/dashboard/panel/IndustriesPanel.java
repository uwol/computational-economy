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
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.XYDataset;

import compecon.economy.sectors.financial.Currency;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.statistics.NotificationListenerModel.ModelListener;
import compecon.engine.statistics.PricesModel;
import compecon.engine.statistics.PricesModel.PriceModel;
import compecon.materia.GoodType;
import compecon.math.production.ConvexProductionFunction.ConvexProductionFunctionTerminationCause;

public class IndustriesPanel extends AbstractChartsPanel implements
		ModelListener {

	public class IndustriesPanelForCurrency extends JPanel implements
			ModelListener {

		public class IndustriesPanelForGoodTypeInCurrency extends JPanel
				implements ModelListener {

			protected final Currency currency;

			protected final GoodType goodType;

			protected JPanel priceTimeSeriesPanel;

			protected JPanel marketDepthPanel;

			public IndustriesPanelForGoodTypeInCurrency(Currency currency,
					GoodType goodType) {
				this.currency = currency;
				this.goodType = goodType;

				this.setLayout(new GridLayout(0, 2));

				this.add(createProductionPanel(currency, goodType));
				this.add(createProductionFunctionMechanicsPanel(currency,
						goodType));
				this.add(createFactoryBalanceSheetPanel(currency, goodType));
				this.add(createGoodTypeSupplyPanel(currency, goodType));
				this.add(createPricingBehaviourMechanicsPanel(currency,
						goodType));

				ApplicationContext.getInstance().getModelRegistry()
						.getNationalEconomyModel(currency).pricesModel
						.registerListener(this);
				// no registration with the market depth model, as they call
				// listeners synchronously

				notifyListener();
			}

			@Override
			public synchronized void notifyListener() {
				if (this.isShowing()) {
					// prices panel
					if (this.priceTimeSeriesPanel != null)
						this.remove(this.priceTimeSeriesPanel);
					this.priceTimeSeriesPanel = createPriceTimeSeriesChartPanel(
							currency, goodType);
					this.add(priceTimeSeriesPanel);

					// market depth panel
					if (this.marketDepthPanel != null)
						this.remove(this.marketDepthPanel);
					this.marketDepthPanel = createMarketDepthPanel(currency,
							goodType);
					this.add(this.marketDepthPanel);

					validate();
					repaint();
				}
			}
		}

		protected final Currency currency;

		protected final JTabbedPane jTabbedPaneGoodType = new JTabbedPane();

		public IndustriesPanelForCurrency(Currency currency) {
			this.setLayout(new BorderLayout());

			this.currency = currency;
			this.add(jTabbedPaneGoodType, BorderLayout.CENTER);
			for (GoodType outputGoodType : GoodType.values()) {
				if (!GoodType.LABOURHOUR.equals(outputGoodType)) {
					// a model has to exist for this panel; might be not the
					// case when certain good types are unused in simulation
					if (ApplicationContext.getInstance().getModelRegistry()
							.getNationalEconomyModel(currency)
							.getIndustryModel(outputGoodType) != null) {
						IndustriesPanelForGoodTypeInCurrency panelForGoodType = new IndustriesPanelForGoodTypeInCurrency(
								currency, outputGoodType);
						jTabbedPaneGoodType.addTab(outputGoodType.name(),
								panelForGoodType);
					}
				}
			}

			jTabbedPaneGoodType.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					if (e.getSource() instanceof JTabbedPane) {
						JTabbedPane pane = (JTabbedPane) e.getSource();
						IndustriesPanelForGoodTypeInCurrency selectedComponent = (IndustriesPanelForGoodTypeInCurrency) pane
								.getSelectedComponent();
						selectedComponent.notifyListener();
					}
				}
			});
		}

		@Override
		public void notifyListener() {
			if (this.isShowing()) {
				IndustriesPanelForGoodTypeInCurrency industryPanel = (IndustriesPanelForGoodTypeInCurrency) jTabbedPaneGoodType
						.getSelectedComponent();
				industryPanel.notifyListener();
			}
		}
	}

	protected final JTabbedPane jTabbedPaneCurrency = new JTabbedPane();

	public IndustriesPanel() {
		this.setLayout(new BorderLayout());
		for (Currency currency : Currency.values()) {
			IndustriesPanelForCurrency panelForCurrency = new IndustriesPanelForCurrency(
					currency);
			jTabbedPaneCurrency.addTab(currency.getIso4217Code(),
					panelForCurrency);
		}

		jTabbedPaneCurrency.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (e.getSource() instanceof JTabbedPane) {
					JTabbedPane pane = (JTabbedPane) e.getSource();
					IndustriesPanelForCurrency selectedComponent = (IndustriesPanelForCurrency) pane
							.getSelectedComponent();
					selectedComponent.notifyListener();
				}
			}
		});

		add(jTabbedPaneCurrency, BorderLayout.CENTER);
	}

	protected ChartPanel createProductionPanel(Currency currency,
			GoodType outputGoodType) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(currency)
				.getIndustryModel(outputGoodType).outputModel.getTimeSeries());
		for (GoodType inputGoodType : ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(currency)
				.getIndustryModel(outputGoodType).inputModels.keySet()) {
			timeSeriesCollection.addSeries(ApplicationContext.getInstance()
					.getModelRegistry().getNationalEconomyModel(currency)
					.getIndustryModel(outputGoodType).inputModels.get(
					inputGoodType).getTimeSeries());
		}

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				outputGoodType.toString() + " Production", "Date", "Output",
				(XYDataset) timeSeriesCollection, true, true, false);
		configureChart(chart);
		chart.addSubtitle(new TextTitle("Inputs: "
				+ ApplicationContext.getInstance().getInputOutputModel()
						.getProductionFunction(outputGoodType)
						.getInputGoodTypes().toString()));
		return new ChartPanel(chart);
	}

	protected ChartPanel createGoodTypeSupplyPanel(Currency currency,
			GoodType outputGoodType) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(currency)
				.getPricingBehaviourModel(outputGoodType).offerModel
				.getTimeSeries());
		timeSeriesCollection.addSeries(ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(currency)
				.getPricingBehaviourModel(outputGoodType).soldModel
				.getTimeSeries());
		timeSeriesCollection.addSeries(ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(currency)
				.getIndustryModel(outputGoodType).inventoryModel
				.getTimeSeries());
		timeSeriesCollection.addSeries(ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(currency)
				.getIndustryModel(outputGoodType).outputModel.getTimeSeries());

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				outputGoodType.toString() + " Supply", "Date", "Supply",
				(XYDataset) timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createPriceTimeSeriesChartPanel(Currency currency,
			GoodType goodType) {
		JFreeChart priceChart = ChartFactory.createCandlestickChart(goodType
				+ " Prices", "Time", "Price in " + currency.getIso4217Code(),
				this.getDefaultHighLowDataset(currency, goodType), false);
		ChartPanel chartPanel = new ChartPanel(priceChart);
		chartPanel.setDomainZoomable(true);
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 400));
		return chartPanel;
	}

	protected ChartPanel createMarketDepthPanel(Currency currency,
			GoodType goodType) {
		XYDataset dataset = ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).marketDepthModel
				.getMarketDepthDataset(currency, goodType);
		JFreeChart chart = ChartFactory.createXYStepAreaChart(goodType
				+ " Market Depth", "Price", "Volume", dataset,
				PlotOrientation.VERTICAL, true, true, false);
		return new ChartPanel(chart);
	}

	protected ChartPanel createProductionFunctionMechanicsPanel(
			Currency currency, GoodType outputGoodType) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(currency)
				.getIndustryModel(outputGoodType).budgetModel.getTimeSeries());
		for (ConvexProductionFunctionTerminationCause terminationCause : ConvexProductionFunctionTerminationCause
				.values()) {
			timeSeriesCollection
					.addSeries(ApplicationContext.getInstance()
							.getModelRegistry()
							.getNationalEconomyModel(currency)
							.getIndustryModel(outputGoodType).convexProductionFunctionTerminationCauseModels
							.get(terminationCause).getTimeSeries());
		}

		// budget is correct here, as the chart illustrates budget
		// emergence from these causes
		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				outputGoodType.toString() + " Production Function Mechanics",
				"Date", "Budget Spent", (XYDataset) timeSeriesCollection, true,
				true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected DefaultHighLowDataset getDefaultHighLowDataset(Currency currency,
			GoodType goodType) {
		PricesModel pricesModel = ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(currency).pricesModel;
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

	@Override
	public void notifyListener() {
		if (this.isShowing()) {
			IndustriesPanelForCurrency industryPanel = (IndustriesPanelForCurrency) jTabbedPaneCurrency
					.getSelectedComponent();
			industryPanel.notifyListener();
		}
	}
}