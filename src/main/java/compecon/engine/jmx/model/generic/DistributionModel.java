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

		public double ySumUnder10PercentX;

		public double ySumUnder20PercentX;

		public double ySumUnder30PercentX;

		public double ySumUnder40PercentX;

		public double ySumUnder50PercentX;

		public double ySumUnder60PercentX;

		public double ySumUnder70PercentX;

		public double ySumUnder80PercentX;

		public double ySumUnder90PercentX;

		// F()
		public double totalSum;

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
				summaryStatisticalData.totalSum += value;

			double sum = 0;
			for (int i = 0; i < valuesAsArray.length; i++) {
				/*
				 * xWith..PercentY
				 */
				sum += valuesAsArray[i];
				if (sum > (summaryStatisticalData.totalSum * 0.9)
						&& summaryStatisticalData.xWith90PercentY == 0)
					summaryStatisticalData.xWith90PercentY = i;
				else if (sum > (summaryStatisticalData.totalSum * 0.8)
						&& summaryStatisticalData.xWith80PercentY == 0)
					summaryStatisticalData.xWith80PercentY = i;
				else if (sum > (summaryStatisticalData.totalSum * 0.7)
						&& summaryStatisticalData.xWith70PercentY == 0)
					summaryStatisticalData.xWith70PercentY = i;
				else if (sum > (summaryStatisticalData.totalSum * 0.6)
						&& summaryStatisticalData.xWith60PercentY == 0)
					summaryStatisticalData.xWith60PercentY = i;
				else if (sum > (summaryStatisticalData.totalSum * 0.5)
						&& summaryStatisticalData.xWith50PercentY == 0)
					summaryStatisticalData.xWith50PercentY = i;
				else if (sum > (summaryStatisticalData.totalSum * 0.4)
						&& summaryStatisticalData.xWith40PercentY == 0)
					summaryStatisticalData.xWith40PercentY = i;
				else if (sum > (summaryStatisticalData.totalSum * 0.3)
						&& summaryStatisticalData.xWith30PercentY == 0)
					summaryStatisticalData.xWith30PercentY = i;
				else if (sum > (summaryStatisticalData.totalSum * 0.2)
						&& summaryStatisticalData.xWith20PercentY == 0)
					summaryStatisticalData.xWith20PercentY = i;
				else if (sum > (summaryStatisticalData.totalSum * 0.1)
						&& summaryStatisticalData.xWith10PercentY == 0)
					summaryStatisticalData.xWith10PercentY = i;

				/*
				 * ySumUnder..PercentX
				 */
				int position10PercentOfX = (int) (valuesAsArray.length * 0.1);
				int position20PercentOfX = (int) (valuesAsArray.length * 0.2);
				int position30PercentOfX = (int) (valuesAsArray.length * 0.3);
				int position40PercentOfX = (int) (valuesAsArray.length * 0.4);
				int position50PercentOfX = (int) (valuesAsArray.length * 0.5);
				int position60PercentOfX = (int) (valuesAsArray.length * 0.6);
				int position70PercentOfX = (int) (valuesAsArray.length * 0.7);
				int position80PercentOfX = (int) (valuesAsArray.length * 0.8);
				int position90PercentOfX = (int) (valuesAsArray.length * 0.9);
				if (i == position10PercentOfX)
					summaryStatisticalData.ySumUnder10PercentX = sum;
				else if (i == position20PercentOfX)
					summaryStatisticalData.ySumUnder20PercentX = sum;
				else if (i == position30PercentOfX)
					summaryStatisticalData.ySumUnder30PercentX = sum;
				else if (i == position40PercentOfX)
					summaryStatisticalData.ySumUnder40PercentX = sum;
				else if (i == position50PercentOfX)
					summaryStatisticalData.ySumUnder50PercentX = sum;
				else if (i == position60PercentOfX)
					summaryStatisticalData.ySumUnder60PercentX = sum;
				else if (i == position70PercentOfX)
					summaryStatisticalData.ySumUnder70PercentX = sum;
				else if (i == position80PercentOfX)
					summaryStatisticalData.ySumUnder80PercentX = sum;
				else if (i == position90PercentOfX)
					summaryStatisticalData.ySumUnder90PercentX = sum;
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
			double totalSum = summaryStatisticalData.totalSum;

			final XYSeries seriesLorenzCurve = new XYSeries("Lorenz curve "
					+ type.toString());
			seriesLorenzCurve.add(0, 0);
			seriesLorenzCurve.add(0.1,
					summaryStatisticalData.ySumUnder10PercentX / totalSum);
			seriesLorenzCurve.add(0.2,
					summaryStatisticalData.ySumUnder20PercentX / totalSum);
			seriesLorenzCurve.add(0.3,
					summaryStatisticalData.ySumUnder30PercentX / totalSum);
			seriesLorenzCurve.add(0.4,
					summaryStatisticalData.ySumUnder40PercentX / totalSum);
			seriesLorenzCurve.add(0.5,
					summaryStatisticalData.ySumUnder50PercentX / totalSum);
			seriesLorenzCurve.add(0.6,
					summaryStatisticalData.ySumUnder60PercentX / totalSum);
			seriesLorenzCurve.add(0.7,
					summaryStatisticalData.ySumUnder70PercentX / totalSum);
			seriesLorenzCurve.add(0.8,
					summaryStatisticalData.ySumUnder80PercentX / totalSum);
			seriesLorenzCurve.add(0.9,
					summaryStatisticalData.ySumUnder90PercentX / totalSum);
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
