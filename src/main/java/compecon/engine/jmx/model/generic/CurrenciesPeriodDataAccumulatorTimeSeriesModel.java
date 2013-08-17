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

import java.util.HashMap;
import java.util.Map;

import compecon.economy.sectors.financial.Currency;

public class CurrenciesPeriodDataAccumulatorTimeSeriesModel<T> {

	protected Map<Currency, PeriodDataAccumulatorTimeSeriesModel<T>> periodDataAccumulatorTimeSeriesModels = new HashMap<Currency, PeriodDataAccumulatorTimeSeriesModel<T>>();

	public CurrenciesPeriodDataAccumulatorTimeSeriesModel(T[] initialTypes) {
		for (Currency currency : Currency.values())
			periodDataAccumulatorTimeSeriesModels.put(currency,
					new PeriodDataAccumulatorTimeSeriesModel<T>(initialTypes));
	}

	public CurrenciesPeriodDataAccumulatorTimeSeriesModel(T[] initialTypes,
			String titleSuffix) {
		for (Currency currency : Currency.values())
			periodDataAccumulatorTimeSeriesModels.put(currency,
					new PeriodDataAccumulatorTimeSeriesModel<T>(initialTypes,
							titleSuffix));
	}

	public PeriodDataAccumulatorTimeSeriesModel<T> getPeriodDataAccumulatorTimeSeriesModelForCurrency(
			Currency currency) {
		return this.periodDataAccumulatorTimeSeriesModels.get(currency);
	}

	public void nextPeriod() {
		for (PeriodDataAccumulatorTimeSeriesModel<T> model : this.periodDataAccumulatorTimeSeriesModels
				.values()) {
			model.nextPeriod();
		}
	}
}
