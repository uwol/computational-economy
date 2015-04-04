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

import compecon.economy.materia.GoodType;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.statistics.NotificationListenerModel.ModelListener;
import compecon.engine.statistics.PricesModel;
import compecon.engine.statistics.PricesModel.PriceModel;
import compecon.math.production.ConvexProductionFunction.ConvexProductionFunctionTerminationCause;

public class IndustriesPanel extends AbstractChartsPanel implements
		ModelListener {

	public class IndustriesPanelForCurrency extends JPanel implements
			ModelListener {

		public class IndustriesPanelForGoodTypeInCurrency extends JPanel
				implements ModelListener {

			protected final Currency currency;

			protected final GoodType goodType;

			protected JPanel marketDepthPanel;

			protected JPanel priceTimeSeriesPanel;

			public IndustriesPanelForGoodTypeInCurrency(
					final Currency currency, final GoodType goodType) {
				this.currency = currency;
				this.goodType = goodType;

				setLayout(new GridLayout(0, 2));

				this.add(createProductionPanel(currency, goodType));
				this.add(createProductionFunctionMechanicsPanel(currency,
						goodType));
				this.add(createFactoryBalanceSheetPanel(currency, goodType));
				this.add(createGoodTypeSupplyPanel(currency, goodType));
				this.add(createPricingBehaviourMechanicsPanel(currency,
						goodType));

				// only capital goods are depreciated
				if (goodType.isDurable()) {
					this.add(createCapitalDepreciationPanel(currency, goodType));
				}

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
					// prices panel
					if (priceTimeSeriesPanel != null) {
						this.remove(priceTimeSeriesPanel);
					}
					priceTimeSeriesPanel = createPriceTimeSeriesChartPanel(
							currency, goodType);
					this.add(priceTimeSeriesPanel);

					// market depth panel
					if (marketDepthPanel != null) {
						this.remove(marketDepthPanel);
					}
					marketDepthPanel = createMarketDepthPanel(currency,
							goodType);
					this.add(marketDepthPanel);

					validate();
					repaint();
				}
			}
		}

		protected final Currency currency;

		protected final JTabbedPane jTabbedPaneGoodType = new JTabbedPane();

		public IndustriesPanelForCurrency(final Currency currency) {
			setLayout(new BorderLayout());

			this.currency = currency;
			this.add(jTabbedPaneGoodType, BorderLayout.CENTER);
			for (final GoodType outputGoodType : GoodType.values()) {
				if (!GoodType.LABOURHOUR.equals(outputGoodType)) {
					// a model has to exist for this panel; might be not the
					// case when certain good types are unused in simulation
					if (ApplicationContext.getInstance().getModelRegistry()
							.getNationalEconomyModel(currency)
							.getIndustryModel(outputGoodType) != null) {
						final IndustriesPanelForGoodTypeInCurrency panelForGoodType = new IndustriesPanelForGoodTypeInCurrency(
								currency, outputGoodType);
						jTabbedPaneGoodType.addTab(outputGoodType.name(),
								panelForGoodType);
					}
				}
			}

			jTabbedPaneGoodType.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(final ChangeEvent e) {
					if (e.getSource() instanceof JTabbedPane) {
						final JTabbedPane pane = (JTabbedPane) e.getSource();
						final IndustriesPanelForGoodTypeInCurrency selectedComponent = (IndustriesPanelForGoodTypeInCurrency) pane
								.getSelectedComponent();
						selectedComponent.notifyListener();
					}
				}
			});
		}

		@Override
		public void notifyListener() {
			if (isShowing()) {
				final IndustriesPanelForGoodTypeInCurrency industryPanel = (IndustriesPanelForGoodTypeInCurrency) jTabbedPaneGoodType
						.getSelectedComponent();
				industryPanel.notifyListener();
			}
		}
	}

	protected final JTabbedPane jTabbedPaneCurrency = new JTabbedPane();

	public IndustriesPanel() {
		setLayout(new BorderLayout());
		for (final Currency currency : Currency.values()) {
			final IndustriesPanelForCurrency panelForCurrency = new IndustriesPanelForCurrency(
					currency);
			jTabbedPaneCurrency.addTab(currency.getIso4217Code(),
					panelForCurrency);
		}

		jTabbedPaneCurrency.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (e.getSource() instanceof JTabbedPane) {
					final JTabbedPane pane = (JTabbedPane) e.getSource();
					final IndustriesPanelForCurrency selectedComponent = (IndustriesPanelForCurrency) pane
							.getSelectedComponent();
					selectedComponent.notifyListener();
				}
			}
		});

		add(jTabbedPaneCurrency, BorderLayout.CENTER);
	}

	protected ChartPanel createCapitalDepreciationPanel(
			final Currency currency, final GoodType outputGoodType) {
		final TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(currency)
				.getIndustryModel(outputGoodType).capitalDepreciationModel
				.getTimeSeries());

		final JFreeChart chart = ChartFactory
				.createTimeSeriesChart(outputGoodType.toString()
						+ " Capital Depreciation", "Date",
						"Capital Depreciation", timeSeriesCollection, true,
						true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createGoodTypeSupplyPanel(final Currency currency,
			final GoodType outputGoodType) {
		final TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

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

		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
				outputGoodType.toString() + " Supply", "Date", "Supply",
				timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createMarketDepthPanel(final Currency currency,
			final GoodType goodType) {
		final XYDataset dataset = ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(currency).marketDepthModel
				.getMarketDepthDataset(currency, goodType);
		final JFreeChart chart = ChartFactory.createXYStepAreaChart(goodType
				+ " Market Depth", "Price", "Volume", dataset,
				PlotOrientation.VERTICAL, true, true, false);
		return new ChartPanel(chart);
	}

	protected ChartPanel createPriceTimeSeriesChartPanel(
			final Currency currency, final GoodType goodType) {
		final JFreeChart priceChart = ChartFactory.createCandlestickChart(
				goodType + " Prices", "Time",
				"Price in " + currency.getIso4217Code(),
				getDefaultHighLowDataset(currency, goodType), false);
		final ChartPanel chartPanel = new ChartPanel(priceChart);
		chartPanel.setDomainZoomable(true);
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 400));
		return chartPanel;
	}

	protected ChartPanel createProductionFunctionMechanicsPanel(
			final Currency currency, final GoodType outputGoodType) {
		final TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(currency)
				.getIndustryModel(outputGoodType).budgetModel.getTimeSeries());
		for (final ConvexProductionFunctionTerminationCause terminationCause : ConvexProductionFunctionTerminationCause
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
		final JFreeChart chart = ChartFactory
				.createTimeSeriesChart(outputGoodType.toString()
						+ " Production Function Mechanics", "Date",
						"Budget Spent", timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}

	protected ChartPanel createProductionPanel(final Currency currency,
			final GoodType outputGoodType) {
		final TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(currency)
				.getIndustryModel(outputGoodType).outputModel.getTimeSeries());
		for (final GoodType inputGoodType : ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(currency)
				.getIndustryModel(outputGoodType).inputModels.keySet()) {
			timeSeriesCollection.addSeries(ApplicationContext.getInstance()
					.getModelRegistry().getNationalEconomyModel(currency)
					.getIndustryModel(outputGoodType).inputModels.get(
					inputGoodType).getTimeSeries());
		}

		final JFreeChart chart = ChartFactory.createTimeSeriesChart(
				outputGoodType.toString() + " Production", "Date", "Output",
				timeSeriesCollection, true, true, false);
		configureChart(chart);
		chart.addSubtitle(new TextTitle("Inputs: "
				+ ApplicationContext.getInstance().getInputOutputModel()
						.getProductionFunction(outputGoodType)
						.getInputGoodTypes().toString()));
		return new ChartPanel(chart);
	}

	protected DefaultHighLowDataset getDefaultHighLowDataset(
			final Currency currency, final GoodType goodType) {
		final PricesModel pricesModel = ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(currency).pricesModel;

		final Map<GoodType, PriceModel> priceModelsForGoodType = pricesModel
				.getPriceModelsForGoodTypes();
		final PriceModel priceModel = priceModelsForGoodType.get(goodType);

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
			final IndustriesPanelForCurrency industryPanel = (IndustriesPanelForCurrency) jTabbedPaneCurrency
					.getSelectedComponent();
			industryPanel.notifyListener();
		}
	}
}