package compecon.engine.jmx.model.generic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.data.statistics.HistogramDataset;

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

		public int position10Percent;

		public int position20Percent;

		public int position30Percent;

		public int position40Percent;

		public int position50Percent;

		public int position60Percent;

		public int position70Percent;

		public int position80Percent;

		public int position90Percent;

		// F()
		public double totalSum;

	}

	protected T[] types;

	protected Map<T, List<Double>> values = new HashMap<T, List<Double>>();

	protected Map<T, SummaryStatisticalData> summaryStatisticalData = new HashMap<T, SummaryStatisticalData>();

	protected final int NUMBER_OF_BINS = 30;

	protected Map<T, HistogramDataset> datasets = new HashMap<T, HistogramDataset>();

	public DistributionModel(T[] initialTypes) {
		this.types = initialTypes;

		for (T type : initialTypes) {
			this.values.put(type, new ArrayList<Double>());
			HistogramDataset dataset = new HistogramDataset();
			this.datasets.put(type, dataset);
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

	public HistogramDataset getHistogramDataset(T type) {
		return this.datasets.get(type);
	}

	public void nextPeriod() {
		for (Entry<T, List<Double>> entry : this.values.entrySet()) {
			T type = entry.getKey();
			double[] values = Doubles.toArray(entry.getValue());
			Arrays.sort(values);

			SummaryStatisticalData summaryStatisticalData = new SummaryStatisticalData();
			this.summaryStatisticalData.put(entry.getKey(),
					summaryStatisticalData);

			summaryStatisticalData.originalValues = values;
			summaryStatisticalData.quantil5Percent = values[(int) (values.length * 0.05)];
			summaryStatisticalData.quantil50Percent = values[(int) (values.length * 0.5)];
			summaryStatisticalData.quantil95Percent = values[(int) (values.length * 0.95)];
			summaryStatisticalData.quantil99Percent = values[(int) (values.length * 0.99)];
			for (double value : values)
				summaryStatisticalData.totalSum += value;

			double sum = 0;
			for (int i = 0; i < values.length; i++) {
				sum += values[i];
				if (sum > (summaryStatisticalData.totalSum * 0.9)
						&& summaryStatisticalData.position90Percent == 0)
					summaryStatisticalData.position90Percent = i;
				else if (sum > (summaryStatisticalData.totalSum * 0.8)
						&& summaryStatisticalData.position80Percent == 0)
					summaryStatisticalData.position80Percent = i;
				else if (sum > (summaryStatisticalData.totalSum * 0.7)
						&& summaryStatisticalData.position70Percent == 0)
					summaryStatisticalData.position70Percent = i;
				else if (sum > (summaryStatisticalData.totalSum * 0.6)
						&& summaryStatisticalData.position60Percent == 0)
					summaryStatisticalData.position60Percent = i;
				else if (sum > (summaryStatisticalData.totalSum * 0.5)
						&& summaryStatisticalData.position50Percent == 0)
					summaryStatisticalData.position50Percent = i;
				else if (sum > (summaryStatisticalData.totalSum * 0.4)
						&& summaryStatisticalData.position40Percent == 0)
					summaryStatisticalData.position40Percent = i;
				else if (sum > (summaryStatisticalData.totalSum * 0.3)
						&& summaryStatisticalData.position30Percent == 0)
					summaryStatisticalData.position30Percent = i;
				else if (sum > (summaryStatisticalData.totalSum * 0.2)
						&& summaryStatisticalData.position20Percent == 0)
					summaryStatisticalData.position20Percent = i;
				else if (sum > (summaryStatisticalData.totalSum * 0.1)
						&& summaryStatisticalData.position10Percent == 0)
					summaryStatisticalData.position10Percent = i;
			}

			HistogramDataset dataset = new HistogramDataset();
			dataset.addSeries(type.toString(), values, NUMBER_OF_BINS, 0,
					summaryStatisticalData.quantil99Percent);
			this.datasets.put(type, dataset);
		}
		this.notifyListeners();
	}
}
