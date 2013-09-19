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

package compecon.engine.jmx.model.timeseries;

import org.jfree.data.time.Day;

import compecon.engine.jmx.model.accumulator.PeriodDataAccumulator;
import compecon.engine.time.TimeSystem;

public class PeriodDataQuotientTimeSeriesModel extends
		AbstractPeriodDataSingleTimeSeriesModel {

	protected final PeriodDataAccumulator periodDataDividendModel = new PeriodDataAccumulator();

	protected final PeriodDataAccumulator periodDataDivisorModel = new PeriodDataAccumulator();

	public PeriodDataQuotientTimeSeriesModel(String title) {
		super(title);
	}

	public void add(double dividendAmount, double divisorAmount) {
		this.periodDataDividendModel.add(dividendAmount);
		this.periodDataDivisorModel.add(divisorAmount);
	}

	public double getValue() {
		return this.periodDataDividendModel.getAmount()
				/ this.periodDataDivisorModel.getAmount();
	}

	public void nextPeriod() {
		double dividend = this.periodDataDividendModel.getAmount();
		double divisor = this.periodDataDivisorModel.getAmount();

		this.timeSeries.addOrUpdate(new Day(TimeSystem.getInstance()
				.getCurrentDate()), dividend / divisor);

		this.periodDataDividendModel.reset();
		this.periodDataDivisorModel.reset();
	}

}
