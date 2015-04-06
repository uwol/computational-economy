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

package compecon.simulation.minimal.csv.impl;

import compecon.economy.materia.GoodType;
import compecon.engine.statistics.NotificationListenerModel.ModelListener;
import compecon.engine.statistics.PricesModel;
import compecon.engine.statistics.PricesModel.PriceModel;
import compecon.engine.statistics.accumulator.PeriodDataQuotientAccumulator;

public class PriceCsvWriterImpl extends CsvPeriodWriterImpl implements
		ModelListener {

	protected final PeriodDataQuotientAccumulator accumulator = new PeriodDataQuotientAccumulator();

	protected final GoodType goodType;

	protected final PricesModel pricesModel;

	public PriceCsvWriterImpl(final String csvFileName,
			final PricesModel pricesModel, final GoodType goodType) {
		super(csvFileName);

		this.goodType = goodType;
		this.pricesModel = pricesModel;

		writeCsvLine("period", "price");
	}

	@Override
	public void notifyListener() {
		final PriceModel priceModel = pricesModel.getPriceModelsForGoodTypes()
				.get(goodType);

		if (priceModel != null) {
			final double[] close = priceModel.getClose();

			if (close.length > 0) {
				final double closingPrice = close[close.length - 1];
				accumulator.add(closingPrice, 1);
			}
		}

		if (isPeriodEnd()) {
			writeCsvLine(getPeriodLabel(),
					Double.toString(accumulator.getAmount()));
			accumulator.reset();
		}
	}
}
