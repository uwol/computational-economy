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

import java.awt.Color;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

import compecon.culture.sectors.financial.Currency;
import compecon.engine.dashboard.model.BalanceSheetsModel;
import compecon.engine.dashboard.model.PeriodDataAccumulatorTimeSeriesModel;
import compecon.engine.dashboard.model.PricesModel;
import compecon.engine.dashboard.model.TimeSeriesModel;
import compecon.nature.materia.GoodType;

public class DiagramPanel extends JPanel {

	protected final PricesModel pricesModel;

	protected boolean blockRedraw = false;

	protected final TimeSeriesModel<Currency> priceIndexModel;

	protected final TimeSeriesModel<Currency> keyInterestRateModel;

	protected final PeriodDataAccumulatorTimeSeriesModel<GoodType> effectiveAmountModel;

	protected final PeriodDataAccumulatorTimeSeriesModel<GoodType> capacityModel;

	protected final BalanceSheetsModel balanceSheetsModel;

	protected final PeriodDataAccumulatorTimeSeriesModel<Currency> moneySupplyM0Model;

	protected final PeriodDataAccumulatorTimeSeriesModel<Currency> moneySupplyM1Model;

	protected final Map<GoodType, ChartPanel> priceCharts = new HashMap<GoodType, ChartPanel>();

	protected final PeriodDataAccumulatorTimeSeriesModel<Currency> utilityModel;

	public DiagramPanel(
			final PricesModel pricesModel,
			final TimeSeriesModel<Currency> priceIndexModel,
			final TimeSeriesModel<Currency> keyInterestRateModel,
			final PeriodDataAccumulatorTimeSeriesModel<GoodType> effectiveAmountModel,
			final PeriodDataAccumulatorTimeSeriesModel<GoodType> capacityModel,
			final BalanceSheetsModel balanceSheetsModel,
			final PeriodDataAccumulatorTimeSeriesModel<Currency> moneySupplyM0Model,
			final PeriodDataAccumulatorTimeSeriesModel<Currency> moneySupplyM1Model,
			final PeriodDataAccumulatorTimeSeriesModel<Currency> utilityModel) {
		this.pricesModel = pricesModel;
		this.priceIndexModel = priceIndexModel;
		this.keyInterestRateModel = keyInterestRateModel;
		this.effectiveAmountModel = effectiveAmountModel;
		this.capacityModel = capacityModel;
		this.balanceSheetsModel = balanceSheetsModel;
		this.moneySupplyM0Model = moneySupplyM0Model;
		this.moneySupplyM1Model = moneySupplyM1Model;
		this.utilityModel = utilityModel;

		this.setLayout(new GridLayout(0, 2));

		this.add(this.createKeyInterestRatesChart());
		this.add(this.createPriceIndicesChart());
		this.add(this.createMoneySupplyChart());

		this.add(this.createProductionCapacityChart());
		this.add(this.createLabourCapacityChart());
	}

	private void configureChart(JFreeChart chart) {
		chart.setBackgroundPaint(Color.white);
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

		DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
		NumberAxis valueAxis = (NumberAxis) plot.getRangeAxis();

		dateAxis.setDateFormatOverride(new SimpleDateFormat("dd-MMM"));
		valueAxis.setAutoRangeIncludesZero(true);
		valueAxis.setUpperMargin(0.15);
		valueAxis.setLowerMargin(0.15);
	}

	private ChartPanel createKeyInterestRatesChart() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : this.keyInterestRateModel.getTypes())
			timeSeriesCollection.addSeries(this.keyInterestRateModel
					.getTimeSeries(currency));

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Key Interest Rate", "Date", "Key Interest Rate",
				timeSeriesCollection, true, true, false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}

	private ChartPanel createPriceIndicesChart() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : this.priceIndexModel.getTypes())
			timeSeriesCollection.addSeries(this.priceIndexModel
					.getTimeSeries(currency));

		JFreeChart chart = ChartFactory.createTimeSeriesChart("Price Index",
				"Date", "Price Index", timeSeriesCollection, true, true, false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}

	private ChartPanel createProductionCapacityChart() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (GoodType goodType : this.effectiveAmountModel.getTypes())
			if (!goodType.equals(GoodType.LABOURHOUR))
				timeSeriesCollection.addSeries(this.effectiveAmountModel
						.getTimeSeries(goodType));

		for (GoodType goodType : this.capacityModel.getTypes())
			if (!goodType.equals(GoodType.LABOURHOUR))
				timeSeriesCollection.addSeries(this.capacityModel
						.getTimeSeries(goodType));

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Production Capacity", "Date", "Capacity & Output",
				(XYDataset) timeSeriesCollection, true, true, false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}

	private ChartPanel createLabourCapacityChart() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(this.effectiveAmountModel
				.getTimeSeries(GoodType.LABOURHOUR));
		timeSeriesCollection.addSeries(this.capacityModel
				.getTimeSeries(GoodType.LABOURHOUR));

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Labour Hour Capacity", "Date", "Capacity & Utilization",
				(XYDataset) timeSeriesCollection, true, true, false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}

	private ChartPanel createMoneySupplyChart() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : this.moneySupplyM0Model.getTypes())
			timeSeriesCollection.addSeries(this.moneySupplyM0Model
					.getTimeSeries(currency));

		for (Currency currency : this.moneySupplyM1Model.getTypes())
			timeSeriesCollection.addSeries(this.moneySupplyM1Model
					.getTimeSeries(currency));

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Money Supply to Non-Banks", "Date", "Money Supply",
				(XYDataset) timeSeriesCollection, true, true, false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}

	public ChartPanel createUtilityChart() {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (Currency currency : this.utilityModel.getTypes())
			timeSeriesCollection.addSeries(this.utilityModel
					.getTimeSeries(currency));

		JFreeChart chart = ChartFactory.createTimeSeriesChart("Utility",
				"Date", "Utility", (XYDataset) timeSeriesCollection, true,
				true, false);
		this.configureChart(chart);
		return new ChartPanel(chart);
	}

	private ChartPanel createPriceChartPanel(GoodType goodType) {
		ChartPanel chartPanel = new ChartPanel(createPriceChart(goodType));
		chartPanel.setDomainZoomable(true);
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 400));
		return chartPanel;
	}

	private JFreeChart createPriceChart(GoodType goodType) {
		return ChartFactory.createCandlestickChart("Price Chart for "
				+ goodType, "Time", "Price in " + Currency.EURO,
				this.pricesModel.getDefaultHighLowDataset(goodType), false);
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