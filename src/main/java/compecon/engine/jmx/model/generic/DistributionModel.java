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

package compecon.engine.jmx.model.generic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.google.common.primitives.Doubles;
import compecon.engine.jmx.model.Model;

public class DistributionModel<T> extends Model {

	public class SummaryStatisticalData {

		public double[] originalValues;

		// F(X)
		public double[] ySumAtPercentOfX = new double[20];

		// F(infinite)
		public double yTotalSum;

		// y-value
		public double quantil5Percent;

		// median, y-value
		public double quantil50Percent;

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
	}

	protected T[] types;

	protected Map<T, List<Double>> values = new HashMap<T, List<Double>>();

	protected Map<T, SummaryStatisticalData> summaryStatisticalData = new HashMap<T, SummaryStatisticalData>();

	protected final int NUMBER_OF_BINS = 30;

	protected Map<T, HistogramDataset> datasetsHistogram = new HashMap<T, HistogramDataset>();

	protected Map<T, XYSeriesCollection> datasetsLorenzCurve = new HashMap<T, XYSeriesCollection>();

	public DistributionModel(T[] initialTypes) {
		this.types = initialTypes;

		for (T type : initialTypes) {
			this.values.put(type, new ArrayList<Double>());
			this.datasetsHistogram.put(type, new HistogramDataset());
			this.datasetsLorenzCurve.put(type, new XYSeriesCollection());
		}
	}

	public void add(T type, double value) {
		this.values.get(type).add(value);
	}

	public T[] getTypes() {
		return this.types;
	}

	public SummaryStatisticalData getSummaryStatisticalData(T type) {
		return this.summaryStatisticalData.get(type);
	}

	public XYDataset getLorenzCurveDataset(T type) {
		return this.datasetsLorenzCurve.get(type);
	}

	public HistogramDataset getHistogramDataset(T type) {
		return this.datasetsHistogram.get(type);
	}

	public void nextPeriod() {
		for (Entry<T, List<Double>> entry : this.values.entrySet()) {
			T type = entry.getKey();
			List<Double> valuesAsList = entry.getValue();
			double[] valuesAsArray = Doubles.toArray(valuesAsList);
			valuesAsList.clear();
			Arrays.sort(valuesAsArray);

			/*
			 * precalculate summary statistical data
			 */
			SummaryStatisticalData summaryStatisticalData = new SummaryStatisticalData();
			this.summaryStatisticalData.put(entry.getKey(),
					summaryStatisticalData);

			summaryStatisticalData.originalValues = valuesAsArray;
			summaryStatisticalData.quantil5Percent = valuesAsArray[(int) (valuesAsArray.length * 0.05)];
			summaryStatisticalData.quantil50Percent = valuesAsArray[(int) (valuesAsArray.length * 0.5)];
			summaryStatisticalData.quantil95Percent = valuesAsArray[(int) (valuesAsArray.length * 0.95)];
			summaryStatisticalData.quantil99Percent = valuesAsArray[(int) (valuesAsArray.length * 0.99)];
			for (double value : valuesAsArray)
				summaryStatisticalData.yTotalSum += value;

			int bucketWidth = valuesAsArray.length
					/ summaryStatisticalData.ySumAtPercentOfX.length;

			double sum = 0;
			for (int i = 0; i < valuesAsArray.length; i++) {
				/*
				 * xWith..PercentY
				 */
				sum += valuesAsArray[i];
				if (sum > (summaryStatisticalData.yTotalSum * 0.9)
						&& summaryStatisticalData.xWith90PercentY == 0)
					summaryStatisticalData.xWith90PercentY = i;
				else if (sum > (summaryStatisticalData.yTotalSum * 0.8)
						&& summaryStatisticalData.xWith80PercentY == 0)
					summaryStatisticalData.xWith80PercentY = i;
				else if (sum > (summaryStatisticalData.yTotalSum * 0.7)
						&& summaryStatisticalData.xWith70PercentY == 0)
					summaryStatisticalData.xWith70PercentY = i;
				else if (sum > (summaryStatisticalData.yTotalSum * 0.6)
						&& summaryStatisticalData.xWith60PercentY == 0)
					summaryStatisticalData.xWith60PercentY = i;
				else if (sum > (summaryStatisticalData.yTotalSum * 0.5)
						&& summaryStatisticalData.xWith50PercentY == 0)
					summaryStatisticalData.xWith50PercentY = i;
				else if (sum > (summaryStatisticalData.yTotalSum * 0.4)
						&& summaryStatisticalData.xWith40PercentY == 0)
					summaryStatisticalData.xWith40PercentY = i;
				else if (sum > (summaryStatisticalData.yTotalSum * 0.3)
						&& summaryStatisticalData.xWith30PercentY == 0)
					summaryStatisticalData.xWith30PercentY = i;
				else if (sum > (summaryStatisticalData.yTotalSum * 0.2)
						&& summaryStatisticalData.xWith20PercentY == 0)
					summaryStatisticalData.xWith20PercentY = i;
				else if (sum > (summaryStatisticalData.yTotalSum * 0.1)
						&& summaryStatisticalData.xWith10PercentY == 0)
					summaryStatisticalData.xWith10PercentY = i;

				/*
				 * ySumAtPercentOfX
				 */
				if (i % bucketWidth == 0) {
					int position = i / bucketWidth;
					if (position < summaryStatisticalData.ySumAtPercentOfX.length) {
						summaryStatisticalData.ySumAtPercentOfX[position] = sum;
					}
				}
			}

			/*
			 * create dataset for histogram
			 */
			HistogramDataset datasetHistogram = new HistogramDataset();
			datasetHistogram.addSeries(type.toString(), valuesAsArray,
					NUMBER_OF_BINS, 0, summaryStatisticalData.quantil99Percent);
			this.datasetsHistogram.put(type, datasetHistogram);

			/*
			 * create dataset for lorenz curve
			 */
			final XYSeries seriesLorenzCurve = new XYSeries("Lorenz curve "
					+ type.toString());
			for (int i = 0; i < summaryStatisticalData.ySumAtPercentOfX.length; i++) {
				double x = i
						/ (double) summaryStatisticalData.ySumAtPercentOfX.length;
				double y = summaryStatisticalData.ySumAtPercentOfX[i]
						/ (double) summaryStatisticalData.yTotalSum;
				seriesLorenzCurve.add(x, y);
			}
			seriesLorenzCurve.add(1, 1);

			final XYSeries seriesLine = new XYSeries("line of equality "
					+ type.toString());
			seriesLine.add(0, 0);
			seriesLine.add(1, 1);

			final XYSeriesCollection datasetLorenzCurve = this.datasetsLorenzCurve
					.get(type);
			datasetLorenzCurve.removeAllSeries();
			datasetLorenzCurve.addSeries(seriesLorenzCurve);
			datasetLorenzCurve.addSeries(seriesLine);
		}
		this.notifyListeners();
	}
}
