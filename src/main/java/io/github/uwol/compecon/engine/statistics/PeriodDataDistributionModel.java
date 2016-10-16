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

package io.github.uwol.compecon.engine.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.google.common.primitives.Doubles;

import io.github.uwol.compecon.economy.sectors.financial.Currency;

public class PeriodDataDistributionModel extends NotificationListenerModel {

	public class SummaryStatisticalData {

		public double[] originalValues;

		// median, y-value
		public double quantil50Percent;

		// y-value
		public double quantil5Percent;

		// y-value
		public double quantil95Percent;

		// y-value
		public double quantil99Percent;

		public int xWith10PercentY;

		public int xWith20PercentY;

		public int xWith30PercentY;

		public int xWith40PercentY;

		public int xWith50PercentY;

		public int xWith60PercentY;

		public int xWith70PercentY;

		public int xWith80PercentY;

		public int xWith90PercentY;

		// F(X)
		public double[] ySumAtPercentOfX = new double[20];

		// F(infinite)
		public double yTotalSum;
	}

	protected HistogramDataset datasetsHistogram = new HistogramDataset();

	protected XYSeriesCollection datasetsLorenzCurve = new XYSeriesCollection();

	protected final int NUMBER_OF_BINS = 30;

	protected final Currency referenceCurrency;

	protected SummaryStatisticalData summaryStatisticalData = new SummaryStatisticalData();

	protected List<Double> values = new ArrayList<Double>();

	public PeriodDataDistributionModel(final Currency referenceCurrency) {
		this.referenceCurrency = referenceCurrency;
	}

	public void add(final double value) {
		values.add(value);
	}

	public HistogramDataset getHistogramDataset() {
		return datasetsHistogram;
	}

	public XYDataset getLorenzCurveDataset() {
		return datasetsLorenzCurve;
	}

	public SummaryStatisticalData getSummaryStatisticalData() {
		return summaryStatisticalData;
	}

	public void nextPeriod() {
		final double[] valuesAsArray = Doubles.toArray(values);
		values.clear();
		Arrays.sort(valuesAsArray);

		/*
		 * precalculate summary statistical data
		 */
		final SummaryStatisticalData summaryStatisticalData = new SummaryStatisticalData();
		this.summaryStatisticalData = summaryStatisticalData;

		summaryStatisticalData.originalValues = valuesAsArray;

		if (valuesAsArray.length > 0) {
			summaryStatisticalData.quantil5Percent = Math.max(0.0,
					valuesAsArray[(int) (valuesAsArray.length * 0.05)]);
			summaryStatisticalData.quantil50Percent = Math.max(0.0,
					valuesAsArray[(int) (valuesAsArray.length * 0.5)]);
			summaryStatisticalData.quantil95Percent = Math.max(0.0,
					valuesAsArray[(int) (valuesAsArray.length * 0.95)]);
			summaryStatisticalData.quantil99Percent = Math.max(0.0,
					valuesAsArray[(int) (valuesAsArray.length * 0.99)]);
		}

		for (final double value : valuesAsArray) {
			summaryStatisticalData.yTotalSum += value;
		}

		final int bucketWidth = valuesAsArray.length
				/ summaryStatisticalData.ySumAtPercentOfX.length;

		double sum = 0;

		if (bucketWidth != 0) {
			for (int i = 0; i < valuesAsArray.length; i++) {
				/*
				 * xWith..PercentY
				 */
				sum += valuesAsArray[i];
				if (sum > (summaryStatisticalData.yTotalSum * 0.9)
						&& summaryStatisticalData.xWith90PercentY == 0) {
					summaryStatisticalData.xWith90PercentY = i;
				} else if (sum > (summaryStatisticalData.yTotalSum * 0.8)
						&& summaryStatisticalData.xWith80PercentY == 0) {
					summaryStatisticalData.xWith80PercentY = i;
				} else if (sum > (summaryStatisticalData.yTotalSum * 0.7)
						&& summaryStatisticalData.xWith70PercentY == 0) {
					summaryStatisticalData.xWith70PercentY = i;
				} else if (sum > (summaryStatisticalData.yTotalSum * 0.6)
						&& summaryStatisticalData.xWith60PercentY == 0) {
					summaryStatisticalData.xWith60PercentY = i;
				} else if (sum > (summaryStatisticalData.yTotalSum * 0.5)
						&& summaryStatisticalData.xWith50PercentY == 0) {
					summaryStatisticalData.xWith50PercentY = i;
				} else if (sum > (summaryStatisticalData.yTotalSum * 0.4)
						&& summaryStatisticalData.xWith40PercentY == 0) {
					summaryStatisticalData.xWith40PercentY = i;
				} else if (sum > (summaryStatisticalData.yTotalSum * 0.3)
						&& summaryStatisticalData.xWith30PercentY == 0) {
					summaryStatisticalData.xWith30PercentY = i;
				} else if (sum > (summaryStatisticalData.yTotalSum * 0.2)
						&& summaryStatisticalData.xWith20PercentY == 0) {
					summaryStatisticalData.xWith20PercentY = i;
				} else if (sum > (summaryStatisticalData.yTotalSum * 0.1)
						&& summaryStatisticalData.xWith10PercentY == 0) {
					summaryStatisticalData.xWith10PercentY = i;
				}

				/*
				 * ySumAtPercentOfX
				 */
				if (i % bucketWidth == 0) {
					final int position = i / bucketWidth;
					if (position < summaryStatisticalData.ySumAtPercentOfX.length) {
						summaryStatisticalData.ySumAtPercentOfX[position] = sum;
					}
				}
			}
		}

		/*
		 * create dataset for histogram
		 */
		final HistogramDataset datasetHistogram = new HistogramDataset();
		datasetHistogram.addSeries(referenceCurrency.getIso4217Code(),
				valuesAsArray, NUMBER_OF_BINS, 0,
				summaryStatisticalData.quantil99Percent);
		datasetsHistogram = datasetHistogram;

		/*
		 * create dataset for lorenz curve
		 */
		final XYSeries seriesLorenzCurve = new XYSeries(
				referenceCurrency.getIso4217Code() + " lorenz curve");

		for (int i = 0; i < summaryStatisticalData.ySumAtPercentOfX.length; i++) {
			final double x = i
					/ (double) summaryStatisticalData.ySumAtPercentOfX.length;
			final double y = summaryStatisticalData.ySumAtPercentOfX[i]
					/ summaryStatisticalData.yTotalSum;
			seriesLorenzCurve.add(x, y);
		}

		seriesLorenzCurve.add(1, 1);

		final XYSeries seriesLine = new XYSeries("line of equality "
				+ referenceCurrency.getIso4217Code());
		seriesLine.add(0, 0);
		seriesLine.add(1, 1);

		final XYSeriesCollection datasetLorenzCurve = datasetsLorenzCurve;
		datasetLorenzCurve.removeAllSeries();
		datasetLorenzCurve.addSeries(seriesLorenzCurve);
		datasetLorenzCurve.addSeries(seriesLine);

		notifyListeners();
	}
}
