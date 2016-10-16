/*
Copyright (C) 2015 u.wol@wwu.de

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

package io.github.uwol.compecon.engine.statistics.accumulator;

public class PeriodDataQuotientAccumulator {

	final PeriodDataAccumulator dividend = new PeriodDataAccumulator();

	final PeriodDataAccumulator divisor = new PeriodDataAccumulator();

	public void add(final double dividendAmount, final double divisorAmount) {
		dividend.add(dividendAmount);
		divisor.add(divisorAmount);
	}

	public double getAmount() {
		return dividend.getAmount() / divisor.getAmount();
	}

	/**
	 * Reset values to zero
	 */
	public void reset() {
		dividend.reset();
		divisor.reset();
	}
}
