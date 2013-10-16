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

package compecon.economy.markets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import compecon.economy.markets.ordertypes.MarketOrder;
import compecon.economy.sectors.Agent;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.law.property.Property;
import compecon.engine.MarketOrderFactory;
import compecon.engine.Simulation;
import compecon.engine.dao.DAOFactory;
import compecon.engine.statistics.Log;
import compecon.engine.util.HibernateUtil;
import compecon.engine.util.MathUtil;
import compecon.materia.GoodType;
import compecon.math.price.FixedPriceFunction;
import compecon.math.price.IPriceFunction;
import compecon.math.price.IPriceFunction.PriceFunctionConfig;

public abstract class Market {

	/**
	 * market offers define a rising step function
	 */
	public class MarketPriceFunction implements IPriceFunction {

		protected final Market market;

		protected final Currency denominatedInCurrency;

		protected final GoodType goodType;

		public MarketPriceFunction(Market market,
				Currency denominatedInCurrency, GoodType goodType) {
			this.market = market;
			this.denominatedInCurrency = denominatedInCurrency;
			this.goodType = goodType;
		}

		/**
		 * calculates the average price for a given amount to buy; average, as a
		 * rising amounts induces a rising marginal price depending on market
		 * depth.
		 */
		@Override
		public double getPrice(double numberOfGoods) {
			return this.market.getAveragePrice(denominatedInCurrency, goodType,
					numberOfGoods);
		}

		/**
		 * the marginal price for an additional unit
		 */
		@Override
		public double getMarginalPrice(double numberOfGoods) {
			return this.market.getPrice(denominatedInCurrency, goodType,
					numberOfGoods);
		}

		@Override
		public PriceFunctionConfig[] getAnalyticalPriceFunctionParameters(
				double maxBudget) {
			return this.market.getAnalyticalPriceFunctionConfigs(
					denominatedInCurrency, goodType, maxBudget);
		}
	}

	/*
	 * fulfillment
	 */

	/**
	 * @return A map of {@link MarketOrder}s conjoint with the amount to take
	 *         from these orders.
	 */
	public SortedMap<MarketOrder, Double> findBestFulfillmentSet(
			final Currency denominatedInCurrency, final double maxAmount,
			final double maxTotalPrice, final double maxPricePerUnit,
			final GoodType goodType) {
		return this.findBestFulfillmentSet(denominatedInCurrency, maxAmount,
				maxTotalPrice, maxPricePerUnit, goodType.getWholeNumber(),
				goodType, null, null);
	}

	/**
	 * @return A map of {@link MarketOrder}s conjoint with the amount to take
	 *         from these orders.
	 */
	public SortedMap<MarketOrder, Double> findBestFulfillmentSet(
			final Currency denominatedInCurrency, final double maxAmount,
			final double maxTotalPrice, final double maxPricePerUnit,
			final Currency commodityCurrency) {
		return this.findBestFulfillmentSet(denominatedInCurrency, maxAmount,
				maxTotalPrice, maxPricePerUnit, false, null, commodityCurrency,
				null);
	}

	/**
	 * @return A map of {@link MarketOrder}s conjoint with the amount to take
	 *         from these orders.
	 */
	public SortedMap<MarketOrder, Double> findBestFulfillmentSet(
			final Currency denominatedInCurrency, final double maxAmount,
			final double maxTotalPrice, final double maxPricePerUnit,
			final Class<? extends Property> propertyClass) {
		return this
				.findBestFulfillmentSet(denominatedInCurrency, maxAmount,
						maxTotalPrice, maxPricePerUnit, true, null, null,
						propertyClass);
	}

	/**
	 * @return A map of {@link MarketOrder}s conjoint with the amount to take
	 *         from these orders.
	 */
	protected SortedMap<MarketOrder, Double> findBestFulfillmentSet(
			final Currency denominatedInCurrency, double maxAmount,
			double maxTotalPrice, double maxPricePerUnit, boolean wholeNumber,
			final GoodType goodType, Currency commodityCurrency,
			final Class<? extends Property> propertyClass) {

		assert (MathUtil.greaterEqual(maxAmount, 0.0) || Double
				.isNaN(maxAmount));
		assert (MathUtil.greaterEqual(maxTotalPrice, 0.0) || Double
				.isNaN(maxTotalPrice));
		assert (MathUtil.greaterEqual(maxPricePerUnit, 0.0) || Double
				.isNaN(maxPricePerUnit));

		// MarketOrder, Amount
		SortedMap<MarketOrder, Double> selectedOffers = new TreeMap<MarketOrder, Double>();

		boolean restrictMaxAmount = true;
		if (Double.isInfinite(maxAmount) || Double.isNaN(maxAmount))
			restrictMaxAmount = false;

		boolean restrictTotalPrice = true;
		if (Double.isInfinite(maxTotalPrice) || Double.isNaN(maxTotalPrice))
			restrictTotalPrice = false;

		boolean restrictMaxPricePerUnit = true;
		if (Double.isInfinite(maxPricePerUnit) || Double.isNaN(maxPricePerUnit))
			restrictMaxPricePerUnit = false;

		double selectedAmount = 0;
		double spentMoney = 0;

		/*
		 * identify correct iterator
		 */
		Iterator<MarketOrder> iterator;
		if (commodityCurrency != null) {
			iterator = DAOFactory.getMarketOrderDAO().getIterator(
					denominatedInCurrency, commodityCurrency);
		} else if (propertyClass != null) {
			iterator = DAOFactory.getMarketOrderDAO().getIterator(
					denominatedInCurrency, propertyClass);
		} else {
			iterator = DAOFactory.getMarketOrderDAO().getIterator(
					denominatedInCurrency, goodType);
		}

		/*
		 * search for orders starting with the lowest price/unit
		 */
		while (iterator.hasNext()) {
			MarketOrder marketOrder = iterator.next();

			// is maxPricePerUnit exceeded?
			if (restrictMaxPricePerUnit
					&& MathUtil.greater(marketOrder.getPricePerUnit(),
							maxPricePerUnit))
				break;

			// is the amount correct?
			assert (marketOrder.getAmount() > 0);

			// is the currency correct?
			assert (marketOrder.getOfferorsBankAcount().getCurrency()
					.equals(denominatedInCurrency));

			double amountToTakeByMaxAmountRestriction;
			double amountToTakeByTotalPriceRestriction;
			double amountToTakeByMaxPricePerUnitRestriction;

			// amountToTakeByMaxAmountRestriction
			if (restrictMaxAmount)
				amountToTakeByMaxAmountRestriction = Math.min(maxAmount
						- selectedAmount, marketOrder.getAmount());
			else
				amountToTakeByMaxAmountRestriction = marketOrder.getAmount();

			// amountToTakeByTotalPriceRestriction
			// division by 0 not allowed !
			if (restrictTotalPrice && marketOrder.getPricePerUnit() != 0) {
				amountToTakeByTotalPriceRestriction = Math.min(
						(maxTotalPrice - spentMoney)
								/ marketOrder.getPricePerUnit(),
						marketOrder.getAmount());
			} else
				amountToTakeByTotalPriceRestriction = marketOrder.getAmount();

			// amountToTakeByMaxPricePerUnitRestriction
			if (restrictMaxPricePerUnit
					&& marketOrder.getPricePerUnit() > maxPricePerUnit) {
				amountToTakeByMaxPricePerUnitRestriction = 0;
			} else
				amountToTakeByMaxPricePerUnitRestriction = marketOrder
						.getAmount();

			// final amount decision
			double amountToTake = Math.max(0, Math.min(
					amountToTakeByMaxAmountRestriction, Math.min(
							amountToTakeByTotalPriceRestriction,
							amountToTakeByMaxPricePerUnitRestriction)));

			// wholeNumberRestriction
			if (wholeNumber) {
				amountToTake = (long) amountToTake;
			}

			double totalPrice = amountToTake * marketOrder.getPricePerUnit();

			assert (!Double.isNaN(amountToTake) && !Double
					.isInfinite(amountToTake));

			if (amountToTake == 0) {
				break;
			} else {
				selectedOffers.put(marketOrder, amountToTake);
				selectedAmount += amountToTake;
				spentMoney += totalPrice;

				assert (!(spentMoney != 0 && restrictTotalPrice && (MathUtil
						.greater(spentMoney, maxTotalPrice))));
				assert (!(restrictMaxAmount
						&& !MathUtil.equal(selectedAmount, maxAmount) && (selectedAmount > maxAmount)));
			}
		}
		return selectedOffers;
	}

	/*
	 * getters
	 */

	public double getMarketDepth(Currency denominatedInCurrency,
			GoodType goodType) {
		return DAOFactory.getMarketOrderDAO().getAmountSum(
				denominatedInCurrency, goodType);
	}

	public double getMarketDepth(Currency denominatedInCurrency,
			Currency commodityCurrency) {
		return DAOFactory.getMarketOrderDAO().getAmountSum(
				denominatedInCurrency, commodityCurrency);
	}

	public double getAveragePrice(final Currency denominatedInCurrency,
			final GoodType goodType, final double atAmount) {
		// case 1: no market depth -> marginal price is searched
		if (MathUtil.equal(atAmount, 0.0))
			return this.getPrice(denominatedInCurrency, goodType);

		// a market price in the depth of the market is searched
		final Map<MarketOrder, Double> marketOrders = this
				.findBestFulfillmentSet(denominatedInCurrency, atAmount,
						Double.NaN, Double.NaN, goodType);
		double totalPriceSum = 0.0;
		double totalAmountSum = 0.0;
		for (Entry<MarketOrder, Double> relevantMarketOrderEntry : marketOrders
				.entrySet()) {
			totalAmountSum += relevantMarketOrderEntry.getValue();
			totalPriceSum += relevantMarketOrderEntry.getValue()
					* relevantMarketOrderEntry.getKey().getPricePerUnit();
		}
		// case 2: numberOfGoods is not offered on market, completely
		if (MathUtil.lesser(totalAmountSum, atAmount))
			return Double.NaN;

		// case 3: regular case
		assert (MathUtil.equal(atAmount, totalAmountSum));

		return totalPriceSum / atAmount;
	}

	public double getPrice(Currency denominatedInCurrency, GoodType goodType) {
		return DAOFactory.getMarketOrderDAO().findMarginalPrice(
				denominatedInCurrency, goodType);
	}

	public double getPrice(Currency denominatedInCurrency, GoodType goodType,
			double atAmount) {
		Iterator<MarketOrder> iterator = DAOFactory.getMarketOrderDAO()
				.getIterator(denominatedInCurrency, goodType);
		double totalAmount = 0.0;
		while (iterator.hasNext()) {
			MarketOrder marketOrder = iterator.next();
			totalAmount += marketOrder.getAmount();
			if (totalAmount >= atAmount)
				return marketOrder.getPricePerUnit();
		}
		return Double.NaN;
	}

	public double getPrice(Currency denominatedInCurrency,
			Currency commodityCurrency) {
		return DAOFactory.getMarketOrderDAO().findMarginalPrice(
				denominatedInCurrency, commodityCurrency);
	}

	public double getPrice(Currency denominatedInCurrency,
			Currency commodityCurrency, double atAmount) {
		Iterator<MarketOrder> iterator = DAOFactory.getMarketOrderDAO()
				.getIterator(denominatedInCurrency, commodityCurrency);
		double totalAmount = 0.0;
		while (iterator.hasNext()) {
			MarketOrder marketOrder = iterator.next();
			totalAmount += marketOrder.getAmount();
			if (totalAmount >= atAmount)
				return marketOrder.getPricePerUnit();
		}
		return Double.NaN;
	}

	public double getPrice(Currency denominatedInCurrency,
			Class<? extends Property> propertyClass) {
		return DAOFactory.getMarketOrderDAO().findMarginalPrice(
				denominatedInCurrency, propertyClass);
	}

	public Map<GoodType, Double> getPrices(Currency denominatedInCurrency,
			GoodType[] goodTypes) {
		Map<GoodType, Double> prices = new HashMap<GoodType, Double>();
		for (GoodType goodType : goodTypes)
			prices.put(goodType, getPrice(denominatedInCurrency, goodType));
		return prices;
	}

	public Map<GoodType, Double> getPrices(Currency denominatedInCurrency,
			Set<GoodType> goodTypes) {
		Map<GoodType, Double> prices = new HashMap<GoodType, Double>();
		for (GoodType goodType : goodTypes)
			prices.put(goodType, getPrice(denominatedInCurrency, goodType));
		return prices;
	}

	public Map<GoodType, Double> getPrices(Currency denominatedInCurrency) {
		return getPrices(denominatedInCurrency, GoodType.values());
	}

	public FixedPriceFunction getFixedPriceFunction(
			Currency denominatedInCurrency, GoodType goodType) {
		return new FixedPriceFunction(this.getPrice(denominatedInCurrency,
				goodType));
	}

	public Map<GoodType, IPriceFunction> getFixedPriceFunctions(
			Currency denominatedInCurrency, GoodType[] goodTypes) {
		Map<GoodType, IPriceFunction> priceFunctions = new HashMap<GoodType, IPriceFunction>();
		for (GoodType goodType : goodTypes)
			priceFunctions.put(goodType,
					getFixedPriceFunction(denominatedInCurrency, goodType));
		return priceFunctions;
	}

	public Map<GoodType, IPriceFunction> getFixedPriceFunctions(
			Currency denominatedInCurrency, Set<GoodType> goodTypes) {
		Map<GoodType, IPriceFunction> priceFunctions = new HashMap<GoodType, IPriceFunction>();
		for (GoodType goodType : goodTypes)
			priceFunctions.put(goodType,
					getFixedPriceFunction(denominatedInCurrency, goodType));
		return priceFunctions;
	}

	public MarketPriceFunction getMarketPriceFunction(
			Currency denominatedInCurrency, GoodType goodType) {
		return new MarketPriceFunction(this, denominatedInCurrency, goodType);
	}

	public Map<GoodType, IPriceFunction> getMarketPriceFunctions(
			Currency denominatedInCurrency, GoodType[] goodTypes) {
		Map<GoodType, IPriceFunction> priceFunctions = new HashMap<GoodType, IPriceFunction>();
		for (GoodType goodType : goodTypes)
			priceFunctions.put(goodType,
					getMarketPriceFunction(denominatedInCurrency, goodType));
		return priceFunctions;
	}

	public Map<GoodType, IPriceFunction> getMarketPriceFunctions(
			Currency denominatedInCurrency, Set<GoodType> goodTypes) {
		Map<GoodType, IPriceFunction> priceFunctions = new HashMap<GoodType, IPriceFunction>();
		for (GoodType goodType : goodTypes)
			priceFunctions.put(goodType,
					getMarketPriceFunction(denominatedInCurrency, goodType));
		return priceFunctions;
	}

	public Iterator<MarketOrder> getMarketOrderIterator(
			Currency denominatedInCurrency, GoodType goodType) {
		return DAOFactory.getMarketOrderDAO().getIterator(
				denominatedInCurrency, goodType);
	}

	public Iterator<MarketOrder> getMarketOrderIterator(
			Currency denominatedInCurrency, Currency commodityCurrency) {
		return DAOFactory.getMarketOrderDAO().getIterator(
				denominatedInCurrency, commodityCurrency);
	}

	public Iterator<MarketOrder> getMarketOrderIterator(
			Currency denominatedInCurrency,
			Class<? extends Property> propertyClass) {
		return DAOFactory.getMarketOrderDAO().getIterator(
				denominatedInCurrency, propertyClass);
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
	protected PriceFunctionConfig[] getAnalyticalPriceFunctionConfigs(
			Currency denominatedInCurrency, GoodType goodType, double maxBudget) {
		List<PriceFunctionConfig> parameterSets = new ArrayList<PriceFunctionConfig>();
		Iterator<MarketOrder> iterator = DAOFactory.getMarketOrderDAO()
				.getIterator(denominatedInCurrency, goodType);

		// a_(n-1)
		double intervalLeftBoundary = 0.0;
		double sumOfPriceTimesAmount = 0.0;

		/*
		 * calculate step-wise price function parameters, starting with the
		 * lowest price
		 */
		while (iterator.hasNext()) {
			MarketOrder marketOrder = iterator.next();

			// a_n
			double intervalRightBoundary = intervalLeftBoundary
					+ marketOrder.getAmount();

			double coefficientXPower0 = marketOrder.getPricePerUnit();
			double coefficientXPowerMinus1 = sumOfPriceTimesAmount
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

	/*
	 * place selling orders
	 */
	public void placeSellingOffer(GoodType goodType, Agent offeror,
			BankAccount offerorsBankAcount, double amount, double pricePerUnit) {

		assert (goodType != null && !Double.isNaN(amount)
				&& !Double.isNaN(pricePerUnit) && amount > 0);

		MarketOrderFactory.newInstanceGoodTypeMarketOrder(goodType, offeror,
				offerorsBankAcount, amount, pricePerUnit);
		if (getLog().isAgentSelectedByClient(offeror))
			getLog().log(
					offeror,
					"offering " + MathUtil.round(amount) + " units of "
							+ goodType + " for "
							+ Currency.formatMoneySum(pricePerUnit) + " "
							+ offerorsBankAcount.getCurrency().getIso4217Code()
							+ " per unit");
	}

	public void placeSellingOffer(Currency commodityCurrency, Agent offeror,
			BankAccount offerorsBankAcount, double amount, double pricePerUnit,
			BankAccount commodityCurrencyOfferorsBankAcount) {

		assert (commodityCurrency != null && !Double.isNaN(amount)
				&& !Double.isNaN(pricePerUnit) && amount > 0);

		MarketOrderFactory.newInstanceCurrencyMarketOrder(commodityCurrency,
				offeror, offerorsBankAcount, amount, pricePerUnit,
				commodityCurrencyOfferorsBankAcount);
		if (getLog().isAgentSelectedByClient(offeror))
			getLog().log(
					offeror,
					"offering " + MathUtil.round(amount) + " units of "
							+ commodityCurrency + " for "
							+ Currency.formatMoneySum(pricePerUnit) + " "
							+ offerorsBankAcount.getCurrency().getIso4217Code()
							+ " per unit");
	}

	public void placeSellingOffer(Property property, Agent offeror,
			BankAccount offerorsBankAcount, double pricePerUnit) {

		assert (property != null && !Double.isNaN(pricePerUnit));

		MarketOrderFactory.newInstancePropertyMarketOrder(property, offeror,
				offerorsBankAcount, pricePerUnit);
		if (getLog().isAgentSelectedByClient(offeror))
			getLog().log(
					offeror,
					"offering 1 unit of " + property.getClass().getSimpleName()
							+ " for " + Currency.formatMoneySum(pricePerUnit)
							+ " "
							+ offerorsBankAcount.getCurrency().getIso4217Code()
							+ " per unit");
	}

	/*
	 * remove selling orders
	 */
	public void removeAllSellingOffers(Agent offeror) {
		DAOFactory.getMarketOrderDAO().deleteAllSellingOrders(offeror);
		HibernateUtil.flushSession();
	}

	public void removeAllSellingOffers(Agent offeror,
			Currency denominatedInCurrency, GoodType goodType) {
		DAOFactory.getMarketOrderDAO().deleteAllSellingOrders(offeror,
				denominatedInCurrency, goodType);
		HibernateUtil.flushSession();
	}

	public void removeAllSellingOffers(Agent offeror,
			Currency denominatedInCurrency, Currency commodityCurrency) {
		DAOFactory.getMarketOrderDAO().deleteAllSellingOrders(offeror,
				denominatedInCurrency, commodityCurrency);
		HibernateUtil.flushSession();
	}

	public void removeAllSellingOffers(Agent offeror,
			Currency denominatedInCurrency,
			Class<? extends Property> propertyClass) {
		DAOFactory.getMarketOrderDAO().deleteAllSellingOrders(offeror,
				denominatedInCurrency, propertyClass);
		HibernateUtil.flushSession();
	}

	protected void removeSellingOffer(MarketOrder marketOrder) {
		DAOFactory.getMarketOrderDAO().delete(marketOrder);
		HibernateUtil.flushSession();
	}

	protected Log getLog() {
		return Simulation.getInstance().getLog();
	}
}
