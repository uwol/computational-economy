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

package io.github.uwol.compecon.engine.statistics.timeseries;

import org.jfree.data.time.Day;

import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.statistics.accumulator.PeriodDataAccumulator;

public class PeriodDataQuotientTimeSeriesModel extends
		AbstractPeriodDataSingleTimeSeriesModel {

	protected final PeriodDataAccumulator periodDataDividendModel = new PeriodDataAccumulator();

	protected final PeriodDataAccumulator periodDataDivisorModel = new PeriodDataAccumulator();

	public PeriodDataQuotientTimeSeriesModel(final String title) {
		super(title);
	}

	public void add(final double dividendAmount, final double divisorAmount) {
		periodDataDividendModel.add(dividendAmount);
		periodDataDivisorModel.add(divisorAmount);
	}

	public double getValue() {
		// Double.NaN or Double.Infinite leads to blank JFreeChart diagrams
		if (periodDataDivisorModel.getAmount() == 0.0) {
			return 0.0;
		}

		return periodDataDividendModel.getAmount()
				/ periodDataDivisorModel.getAmount();
	}

	@Override
	public void nextPeriod() {
		timeSeries.addOrUpdate(new Day(ApplicationContext.getInstance()
				.getTimeSystem().getCurrentDate()), getValue());

		periodDataDividendModel.reset();
		periodDataDivisorModel.reset();
	}
}
