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

package io.github.uwol.compecon.engine.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.github.uwol.compecon.economy.markets.MarketOrder;
import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.property.Property;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.engine.service.MarketPriceFunction;
import io.github.uwol.compecon.math.util.MathUtil;

/**
 * Market orders constitute a rising step function, that is represented by this
 * class. This class implements an performance-oriented approach for iterating
 * over the step function.
 */
public class MarketPriceFunctionImpl implements MarketPriceFunction {

	protected double amountUntilCurrentMarketOrder = 0.0;

	protected double averagePricePerUnitUntilCurrentMarketOrder = Double.NaN;

	protected final Currency commodityCurrency;

	protected MarketOrder currentMarketOrder;

	protected final Currency denominatedInCurrency;

	protected final GoodType goodType;

	protected Iterator<MarketOrder> marketOrderIterator;

	protected final MarketServiceImpl marketService;

	protected final Class<? extends Property> propertyClass;

	public MarketPriceFunctionImpl(final MarketServiceImpl marketService, final Currency denominatedInCurrency,
			final Class<? extends Property> propertyClass) {
		this.denominatedInCurrency = denominatedInCurrency;
		this.marketService = marketService;
		commodityCurrency = null;
		goodType = null;
		this.propertyClass = propertyClass;
	}

	public MarketPriceFunctionImpl(final MarketServiceImpl marketService, final Currency denominatedInCurrency,
			final Currency commodityCurrency) {
		this.denominatedInCurrency = denominatedInCurrency;
		this.marketService = marketService;
		this.commodityCurrency = commodityCurrency;
		goodType = null;
		propertyClass = null;
	}

	public MarketPriceFunctionImpl(final MarketServiceImpl marketService, final Currency denominatedInCurrency,
			final GoodType goodType) {
		this.denominatedInCurrency = denominatedInCurrency;
		this.marketService = marketService;
		commodityCurrency = null;
		this.goodType = goodType;
		propertyClass = null;
	}

	protected void checkReset(final double forAmount) {
		/*
		 * if a preceding call of the market price function has iterated to far on
		 * market orders, we have to restart on the first market order
		 */
		if (marketOrderIterator == null || amountUntilCurrentMarketOrder > forAmount) {
			reset();
		}
	}

	/**
	 * p(x) = p_1 * x | 0 <= x < a_1 <br />
	 * p(x) = [p_1 * a_1 + p_2 * (x - a_1)] / [a_1 + (x - a_1)] | a_1 <= x < a_2
	 * <br />
	 * p(x) = [p_1 * a_1 + p_2 * a_2 + p_3 * (x - a_2)] / [a_1 + (a_2 - a_1) + (x -
	 * a_2)] | a_2 <= x < a_3 <br />
	 * p(x) = [p_1 * a_1 + p_2 * a_2 + p_3 * a_3 + p_4 * (x - a_3)] / [a_1 + (a_2 -
	 * a_1) + (a_3 - a_2) + (x - a_3)] | a_3 <= x < a_4 <br />
	 * <br />
	 * p(x) = [p_1 * a_1 + p_2 * a_2 + p_3 * a_3 + ... + p_n * (x - a_(n-1))] / [x]
	 * | a_(n-1) <= x < a_n <br />
	 * <br />
	 * => <br />
	 * <br />
	 * p(x) = [p_1 * a_1 + p_2 * a_2 + p_3 * a_3 + ... + p_(n-1) * a_(n-1)] / x -
	 * a_(n-1) / x + p_n | a_(n-1) <= x < a_n <br />
	 * p(x) = [p_1 * a_1 + p_2 * a_2 + p_3 * a_3 + ... + p_(n-1) * a_(n-1) - p_n *
	 * a_(n-1)] / x + p_n | a_(n-1) <= x < a_n <br />
	 */
	@Override
	public PriceFunctionConfig[] getAnalyticalPriceFunctionParameters(final double maxBudget) {
		final List<PriceFunctionConfig> parameterSets = new ArrayList<PriceFunctionConfig>();
		final Iterator<MarketOrder> iterator = marketService.getMarketOrderIterator(denominatedInCurrency, goodType);

		// a_(n-1)
		double intervalLeftBoundary = 0.0;
		double sumOfPriceTimesAmount = 0.0;

		/*
		 * calculate step-wise price function parameters, starting with the lowest price
		 */
		while (iterator.hasNext()) {
			final MarketOrder marketOrder = iterator.next();

			// a_n
			final double intervalRightBoundary = intervalLeftBoundary + marketOrder.getAmount();

			final double coefficientXPower0 = marketOrder.getPricePerUnit();
			final double coefficientXPowerMinus1 = sumOfPriceTimesAmount
					- marketOrder.getPricePerUnit() * intervalLeftBoundary;

			parameterSets.add(new PriceFunctionConfig(intervalLeftBoundary, intervalRightBoundary, coefficientXPower0,
					coefficientXPowerMinus1));

			sumOfPriceTimesAmount += marketOrder.getPricePerUnit() * marketOrder.getAmount();
			intervalLeftBoundary = intervalRightBoundary;

			if (sumOfPriceTimesAmount > maxBudget) {
				break;
			}
		}
		return parameterSets.toArray(new PriceFunctionConfig[0]);
	}

	@Override
	public double getMarginalPrice(final double atAmount) {
		checkReset(atAmount);

		do {
			if (currentMarketOrder != null) {
				if (amountUntilCurrentMarketOrder + currentMarketOrder.getAmount() >= atAmount) {
					return currentMarketOrder.getPricePerUnit();
				}
			}
		} while (nextMarketOrder());

		return Double.NaN;
	}

	@Override
	public double getPrice(final double atAmount) {
		checkReset(atAmount);

		// case 1: no market depth -> marginal price is searched
		if (MathUtil.equal(atAmount, 0.0)) {
			return getMarginalPrice(atAmount);
		}

		/*
		 * a market price in the depth of the market is searched
		 */
		do {
			if (currentMarketOrder != null) {
				if (amountUntilCurrentMarketOrder + currentMarketOrder.getAmount() >= atAmount) {
					// case 2: regular case
					return (averagePricePerUnitUntilCurrentMarketOrder * amountUntilCurrentMarketOrder
							+ (atAmount - amountUntilCurrentMarketOrder) * currentMarketOrder.getPricePerUnit())
							/ atAmount;
				}
			}
		} while (nextMarketOrder());

		// case 3: numberOfGoods is not offered on market, completely
		return Double.NaN;
	}

	protected boolean nextMarketOrder() {
		if (marketOrderIterator.hasNext()) {
			averagePricePerUnitUntilCurrentMarketOrder = (averagePricePerUnitUntilCurrentMarketOrder
					* amountUntilCurrentMarketOrder
					+ currentMarketOrder.getPricePerUnit() * currentMarketOrder.getAmount())
					/ (amountUntilCurrentMarketOrder + currentMarketOrder.getAmount());
			amountUntilCurrentMarketOrder += currentMarketOrder.getAmount();

			currentMarketOrder = marketOrderIterator.next();
			return true;
		}
		return false;
	}

	@Override
	public void reset() {
		amountUntilCurrentMarketOrder = 0.0;
		currentMarketOrder = null;
		averagePricePerUnitUntilCurrentMarketOrder = Double.NaN;

		if (goodType != null) {
			marketOrderIterator = marketService.getMarketOrderIterator(denominatedInCurrency, goodType);
		} else if (commodityCurrency != null) {
			marketOrderIterator = marketService.getMarketOrderIterator(denominatedInCurrency, commodityCurrency);
		} else if (propertyClass != null) {
			marketOrderIterator = marketService.getMarketOrderIterator(denominatedInCurrency, propertyClass);
		} else {
			marketOrderIterator = null;
		}

		if (marketOrderIterator.hasNext()) {
			currentMarketOrder = marketOrderIterator.next();
			averagePricePerUnitUntilCurrentMarketOrder = currentMarketOrder.getPricePerUnit();
		}
	}
}
