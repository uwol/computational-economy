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

package compecon.engine.statistics;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import compecon.economy.materia.GoodType;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.timesystem.impl.DayType;
import compecon.engine.timesystem.impl.MonthType;

public class PricesModel extends NotificationListenerModel {

	public class PriceModel {

		double[] close = new double[NUMBER_OF_DAYS];
		Date[] date = new Date[NUMBER_OF_DAYS];
		double[] high = new double[NUMBER_OF_DAYS];

		int i = -1;
		private DayType lastDate_dayType;
		private MonthType lastDate_monthType;
		private int lastDate_year;
		double[] low = new double[NUMBER_OF_DAYS];
		double[] open = new double[NUMBER_OF_DAYS];

		double[] volume = new double[NUMBER_OF_DAYS];

		public double[] getClose() {
			return Arrays.copyOf(close, i);
		}

		public Date[] getDate() {
			return Arrays.copyOf(date, i);
		}

		public double[] getHigh() {
			return Arrays.copyOf(high, i);
		}

		public double[] getLow() {
			return Arrays.copyOf(low, i);
		}

		public double[] getOpen() {
			return Arrays.copyOf(open, i);
		}

		public double[] getVolume() {
			return Arrays.copyOf(volume, i);
		}

		public boolean hasData() {
			return i > -1;
		}

		public void tick(final double price, final double volume) {
			// current day?
			if (lastDate_year == ApplicationContext.getInstance()
					.getTimeSystem().getCurrentYear()
					&& lastDate_monthType == ApplicationContext.getInstance()
							.getTimeSystem().getCurrentMonthType()
					&& lastDate_dayType == ApplicationContext.getInstance()
							.getTimeSystem().getCurrentDayType()) {
				this.volume[i] += volume;

				if (price > high[i]) {
					high[i] = price;
				}
				if (price < low[i]) {
					low[i] = price;
				}
				close[i] = price;
			} else { // new day
				if (i < NUMBER_OF_DAYS - 1) {
					i++;
				} else {
					System.arraycopy(date, 1, date, 0, NUMBER_OF_DAYS - 1);
					System.arraycopy(high, 1, high, 0, NUMBER_OF_DAYS - 1);
					System.arraycopy(low, 1, low, 0, NUMBER_OF_DAYS - 1);
					System.arraycopy(open, 1, open, 0, NUMBER_OF_DAYS - 1);
					System.arraycopy(close, 1, close, 0, NUMBER_OF_DAYS - 1);
					System.arraycopy(this.volume, 1, this.volume, 0,
							NUMBER_OF_DAYS - 1);
				}

				date[i] = ApplicationContext.getInstance().getTimeSystem()
						.getCurrentDate();
				high[i] = price;
				low[i] = price;
				open[i] = price;
				close[i] = price;
				this.volume[i] = volume;

				lastDate_year = ApplicationContext.getInstance()
						.getTimeSystem().getCurrentYear();
				lastDate_monthType = ApplicationContext.getInstance()
						.getTimeSystem().getCurrentMonthType();
				lastDate_dayType = ApplicationContext.getInstance()
						.getTimeSystem().getCurrentDayType();
			}
		}
	}

	private final int NUMBER_OF_DAYS = 180;

	protected final Map<Currency, Map<Currency, PriceModel>> priceModelsForCurrencies = new HashMap<Currency, Map<Currency, PriceModel>>();

	protected final Map<Currency, Map<GoodType, PriceModel>> priceModelsForGoodTypes = new HashMap<Currency, Map<GoodType, PriceModel>>();

	public Map<Currency, Map<Currency, PriceModel>> getPriceModelsForCurrencies() {
		return priceModelsForCurrencies;
	}

	public Map<Currency, Map<GoodType, PriceModel>> getPriceModelsForGoodTypes() {
		return priceModelsForGoodTypes;
	}

	public void market_onTick(final double pricePerUnit,
			final Currency commodityCurrency, final Currency currency,
			final double amount) {
		if (!priceModelsForCurrencies.containsKey(currency)) {
			priceModelsForCurrencies.put(currency,
					new HashMap<Currency, PriceModel>());
		}

		final Map<Currency, PriceModel> priceModelsForCurrency = priceModelsForCurrencies
				.get(currency);
		if (!priceModelsForCurrency.containsKey(commodityCurrency)) {
			priceModelsForCurrency.put(commodityCurrency, new PriceModel());
		}

		priceModelsForCurrency.get(commodityCurrency)
				.tick(pricePerUnit, amount);
	}

	public void market_onTick(final double pricePerUnit,
			final GoodType goodType, final Currency currency,
			final double amount) {
		if (!priceModelsForGoodTypes.containsKey(currency)) {
			priceModelsForGoodTypes.put(currency,
					new HashMap<GoodType, PriceModel>());
		}

		final Map<GoodType, PriceModel> priceModelsForCurrency = priceModelsForGoodTypes
				.get(currency);
		if (!priceModelsForCurrency.containsKey(goodType)) {
			priceModelsForCurrency.put(goodType, new PriceModel());
		}

		priceModelsForCurrency.get(goodType).tick(pricePerUnit, amount);
	}

	public void nextPeriod() {
		notifyListeners();
	}
}
