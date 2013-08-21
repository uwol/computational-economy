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

package compecon.engine.jmx.model;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import compecon.economy.sectors.financial.Currency;
import compecon.engine.time.TimeSystem;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.MonthType;
import compecon.materia.GoodType;

public class PricesModel extends NotificationListenerModel {

	public class PriceModel {
		private final int NUMBER_OF_DAYS = 180;

		private int lastDate_year;
		private MonthType lastDate_monthType;
		private DayType lastDate_dayType;

		Date[] date = new Date[NUMBER_OF_DAYS];
		double[] high = new double[NUMBER_OF_DAYS];
		double[] low = new double[NUMBER_OF_DAYS];
		double[] open = new double[NUMBER_OF_DAYS];
		double[] close = new double[NUMBER_OF_DAYS];
		double[] volume = new double[NUMBER_OF_DAYS];

		int i = -1;

		public void tick(double price, double volume) {
			// current day?
			if (this.lastDate_year == TimeSystem.getInstance().getCurrentYear()
					&& this.lastDate_monthType == TimeSystem.getInstance()
							.getCurrentMonthType()
					&& this.lastDate_dayType == TimeSystem.getInstance()
							.getCurrentDayType()) {
				this.volume[i] += volume;
				if (price > this.high[i])
					this.high[i] = price;
				if (price < this.low[i])
					this.low[i] = price;
				this.close[i] = price;
			} else { // new day
				if (i < this.NUMBER_OF_DAYS - 1) {
					i++;
				} else {
					System.arraycopy(this.date, 1, this.date, 0,
							this.NUMBER_OF_DAYS - 1);
					System.arraycopy(this.high, 1, this.high, 0,
							this.NUMBER_OF_DAYS - 1);
					System.arraycopy(this.low, 1, this.low, 0,
							this.NUMBER_OF_DAYS - 1);
					System.arraycopy(this.open, 1, this.open, 0,
							this.NUMBER_OF_DAYS - 1);
					System.arraycopy(this.close, 1, this.close, 0,
							this.NUMBER_OF_DAYS - 1);
					System.arraycopy(this.volume, 1, this.volume, 0,
							this.NUMBER_OF_DAYS - 1);
				}

				this.date[i] = TimeSystem.getInstance().getCurrentDate();
				this.high[i] = price;
				this.low[i] = price;
				this.open[i] = price;
				this.close[i] = price;
				this.volume[i] = volume;

				this.lastDate_year = TimeSystem.getInstance().getCurrentYear();
				this.lastDate_monthType = TimeSystem.getInstance()
						.getCurrentMonthType();
				this.lastDate_dayType = TimeSystem.getInstance()
						.getCurrentDayType();
			}
		}

		public Date[] getDate() {
			return Arrays.copyOf(this.date, i);
		}

		public double[] getHigh() {
			return Arrays.copyOf(this.high, i);
		}

		public double[] getLow() {
			return Arrays.copyOf(this.low, i);
		}

		public double[] getOpen() {
			return Arrays.copyOf(this.open, i);
		}

		public double[] getClose() {
			return Arrays.copyOf(this.close, i);
		}

		public double[] getVolume() {
			return Arrays.copyOf(this.volume, i);
		}

		public boolean hasData() {
			return this.i > -1;
		}
	}

	protected final Map<Currency, Map<GoodType, PriceModel>> priceModelsForGoodTypes = new HashMap<Currency, Map<GoodType, PriceModel>>();

	protected final Map<Currency, Map<Currency, PriceModel>> priceModelsForCurrencies = new HashMap<Currency, Map<Currency, PriceModel>>();

	public void market_onTick(double pricePerUnit, GoodType goodType,
			Currency currency, double amount) {
		if (!this.priceModelsForGoodTypes.containsKey(currency))
			this.priceModelsForGoodTypes.put(currency,
					new HashMap<GoodType, PriceModel>());

		Map<GoodType, PriceModel> priceModelsForCurrency = this.priceModelsForGoodTypes
				.get(currency);
		if (!priceModelsForCurrency.containsKey(goodType))
			priceModelsForCurrency.put(goodType, new PriceModel());
		priceModelsForCurrency.get(goodType).tick(pricePerUnit, amount);
	}

	public void market_onTick(double pricePerUnit, Currency commodityCurrency,
			Currency currency, double amount) {
		if (!this.priceModelsForCurrencies.containsKey(currency))
			this.priceModelsForCurrencies.put(currency,
					new HashMap<Currency, PriceModel>());

		Map<Currency, PriceModel> priceModelsForCurrency = this.priceModelsForCurrencies
				.get(currency);
		if (!priceModelsForCurrency.containsKey(commodityCurrency))
			priceModelsForCurrency.put(commodityCurrency, new PriceModel());
		priceModelsForCurrency.get(commodityCurrency)
				.tick(pricePerUnit, amount);
	}

	public Map<Currency, Map<GoodType, PriceModel>> getPriceModelsForGoodTypes() {
		return this.priceModelsForGoodTypes;
	}

	public Map<Currency, Map<Currency, PriceModel>> getPriceModelsForCurrencies() {
		return this.priceModelsForCurrencies;
	}

	public void nextPeriod() {
		this.notifyListeners();
	}
}
