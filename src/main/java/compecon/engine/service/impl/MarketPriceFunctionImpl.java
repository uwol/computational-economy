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

package compecon.engine.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import compecon.economy.markets.MarketOrder;
import compecon.economy.materia.GoodType;
import compecon.economy.property.Property;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.service.MarketPriceFunction;
import compecon.math.price.PriceFunction.PriceFunctionConfig;
import compecon.math.util.MathUtil;

/**
 * Market orders constitute a rising step function, that is represented by this
 * class. This class implements an performance-oriented approach for iterating
 * over the step function.
 */
public class MarketPriceFunctionImpl implements MarketPriceFunction {

	protected final Currency denominatedInCurrency;

	protected final GoodType goodType;

	protected final Currency commodityCurrency;

	protected final Class<? extends Property> propertyClass;

	protected final MarketServiceImpl marketService;

	protected double amountUntilCurrentMarketOrder = 0.0;

	protected double averagePricePerUnitUntilCurrentMarketOrder = Double.NaN;

	protected MarketOrder currentMarketOrder;

	protected Iterator<MarketOrder> marketOrderIterator;

	public MarketPriceFunctionImpl(final MarketServiceImpl marketService,
			final Currency denominatedInCurrency, final GoodType goodType) {
		this.denominatedInCurrency = denominatedInCurrency;
		this.marketService = marketService;
		this.commodityCurrency = null;
		this.goodType = goodType;
		this.propertyClass = null;
	}

	public MarketPriceFunctionImpl(final MarketServiceImpl marketService,
			final Currency denominatedInCurrency,
			final Currency commodityCurrency) {
		this.denominatedInCurrency = denominatedInCurrency;
		this.marketService = marketService;
		this.commodityCurrency = commodityCurrency;
		this.goodType = null;
		this.propertyClass = null;
	}

	public MarketPriceFunctionImpl(final MarketServiceImpl marketService,
			final Currency denominatedInCurrency,
			final Class<? extends Property> propertyClass) {
		this.denominatedInCurrency = denominatedInCurrency;
		this.marketService = marketService;
		this.commodityCurrency = null;
		this.goodType = null;
		this.propertyClass = propertyClass;
	}

	@Override
	public double getPrice(double atAmount) {
		checkReset(atAmount);

		// case 1: no market depth -> marginal price is searched
		if (MathUtil.equal(atAmount, 0.0)) {
			return this.getMarginalPrice(atAmount);
		}

		/*
		 * a market price in the depth of the market is searched
		 */
		do {
			if (this.currentMarketOrder != null) {
				if (this.amountUntilCurrentMarketOrder
						+ this.currentMarketOrder.getAmount() >= atAmount) {
					// case 2: regular case
					return (this.averagePricePerUnitUntilCurrentMarketOrder
							* this.amountUntilCurrentMarketOrder + (atAmount - this.amountUntilCurrentMarketOrder)
							* this.currentMarketOrder.getPricePerUnit())
							/ atAmount;
				}
			}
		} while (this.nextMarketOrder());

		// case 3: numberOfGoods is not offered on market, completely
		return Double.NaN;
	}

	@Override
	public double getMarginalPrice(double atAmount) {
		checkReset(atAmount);

		do {
			if (this.currentMarketOrder != null) {
				if (this.amountUntilCurrentMarketOrder
						+ this.currentMarketOrder.getAmount() >= atAmount) {
					return this.currentMarketOrder.getPricePerUnit();
				}
			}
		} while (nextMarketOrder());

		return Double.NaN;
	}

	/**
	 * p(x) = p_1 * x | 0 <= x < a_1 <br />
	 * p(x) = [p_1 * a_1 + p_2 * (x - a_1)] / [a_1 + (x - a_1)] | a_1 <= x < a_2 <br />
	 * p(x) = [p_1 * a_1 + p_2 * a_2 + p_3 * (x - a_2)] / [a_1 + (a_2 - a_1) +
	 * (x - a_2)] | a_2 <= x < a_3 <br />
	 * p(x) = [p_1 * a_1 + p_2 * a_2 + p_3 * a_3 + p_4 * (x - a_3)] / [a_1 +
	 * (a_2 - a_1) + (a_3 - a_2) + (x - a_3)] | a_3 <= x < a_4 <br />
	 * <br />
	 * p(x) = [p_1 * a_1 + p_2 * a_2 + p_3 * a_3 + ... + p_n * (x - a_(n-1))] /
	 * [x] | a_(n-1) <= x < a_n <br />
	 * <br />
	 * => <br />
	 * <br />
	 * p(x) = [p_1 * a_1 + p_2 * a_2 + p_3 * a_3 + ... + p_(n-1) * a_(n-1)] / x
	 * - a_(n-1) / x + p_n | a_(n-1) <= x < a_n <br />
	 * p(x) = [p_1 * a_1 + p_2 * a_2 + p_3 * a_3 + ... + p_(n-1) * a_(n-1) - p_n
	 * * a_(n-1)] / x + p_n | a_(n-1) <= x < a_n <br />
	 */
	@Override
	public PriceFunctionConfig[] getAnalyticalPriceFunctionParameters(
			final double maxBudget) {
		final List<PriceFunctionConfig> parameterSets = new ArrayList<PriceFunctionConfig>();
		final Iterator<MarketOrder> iterator = marketService
				.getMarketOrderIterator(denominatedInCurrency, goodType);

		// a_(n-1)
		double intervalLeftBoundary = 0.0;
		double sumOfPriceTimesAmount = 0.0;

		/*
		 * calculate step-wise price function parameters, starting with the
		 * lowest price
		 */
		while (iterator.hasNext()) {
			final MarketOrder marketOrder = iterator.next();

			// a_n
			final double intervalRightBoundary = intervalLeftBoundary
					+ marketOrder.getAmount();

			final double coefficientXPower0 = marketOrder.getPricePerUnit();
			final double coefficientXPowerMinus1 = sumOfPriceTimesAmount
					- marketOrder.getPricePerUnit() * intervalLeftBoundary;

			parameterSets.add(new PriceFunctionConfig(intervalLeftBoundary,
					intervalRightBoundary, coefficientXPower0,
					coefficientXPowerMinus1));

			sumOfPriceTimesAmount += marketOrder.getPricePerUnit()
					* marketOrder.getAmount();
			intervalLeftBoundary = intervalRightBoundary;

			if (sumOfPriceTimesAmount > maxBudget)
				break;
		}
		return parameterSets.toArray(new PriceFunctionConfig[0]);
	}

	protected boolean nextMarketOrder() {
		if (this.marketOrderIterator.hasNext()) {
			this.averagePricePerUnitUntilCurrentMarketOrder = (this.averagePricePerUnitUntilCurrentMarketOrder
					* this.amountUntilCurrentMarketOrder + this.currentMarketOrder
					.getPricePerUnit() * this.currentMarketOrder.getAmount())
					/ (this.amountUntilCurrentMarketOrder + this.currentMarketOrder
							.getAmount());
			this.amountUntilCurrentMarketOrder += this.currentMarketOrder
					.getAmount();

			this.currentMarketOrder = this.marketOrderIterator.next();
			return true;
		}
		return false;
	}

	protected void checkReset(final double forAmount) {
		/*
		 * if a preceding call of the market price function has iterated to far
		 * on market orders, we have to restart on the first market order
		 */
		if (this.marketOrderIterator == null
				|| amountUntilCurrentMarketOrder > forAmount) {
			reset();
		}
	}

	public void reset() {
		this.amountUntilCurrentMarketOrder = 0.0;
		this.currentMarketOrder = null;
		this.averagePricePerUnitUntilCurrentMarketOrder = Double.NaN;

		if (this.goodType != null) {
			this.marketOrderIterator = this.marketService
					.getMarketOrderIterator(this.denominatedInCurrency,
							this.goodType);
		} else if (this.commodityCurrency != null) {
			this.marketOrderIterator = this.marketService
					.getMarketOrderIterator(this.denominatedInCurrency,
							this.commodityCurrency);
		} else if (this.propertyClass != null) {
			this.marketOrderIterator = this.marketService
					.getMarketOrderIterator(this.denominatedInCurrency,
							this.propertyClass);
		} else {
			this.marketOrderIterator = null;
		}

		if (this.marketOrderIterator.hasNext()) {
			this.currentMarketOrder = this.marketOrderIterator.next();
			this.averagePricePerUnitUntilCurrentMarketOrder = this.currentMarketOrder
					.getPricePerUnit();
		}
	}
}
