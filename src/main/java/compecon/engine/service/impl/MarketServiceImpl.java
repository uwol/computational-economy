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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import compecon.economy.markets.MarketOrder;
import compecon.economy.markets.MarketParticipant;
import compecon.economy.materia.GoodType;
import compecon.economy.property.Property;
import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.log.Log;
import compecon.engine.service.MarketPriceFunction;
import compecon.engine.service.MarketService;
import compecon.engine.util.HibernateUtil;
import compecon.math.price.PriceFunction;
import compecon.math.util.MathUtil;

public abstract class MarketServiceImpl implements MarketService {

	/*
	 * fulfillment
	 */

	public SortedMap<MarketOrder, Double> findBestFulfillmentSet(
			final Currency denominatedInCurrency, final double maxAmount,
			final double maxTotalPrice, final double maxPricePerUnit,
			final GoodType goodType) {
		return this.findBestFulfillmentSet(denominatedInCurrency, maxAmount,
				maxTotalPrice, maxPricePerUnit, goodType.getWholeNumber(),
				goodType, null, null);
	}

	public SortedMap<MarketOrder, Double> findBestFulfillmentSet(
			final Currency denominatedInCurrency, final double maxAmount,
			final double maxTotalPrice, final double maxPricePerUnit,
			final Currency commodityCurrency) {
		return this.findBestFulfillmentSet(denominatedInCurrency, maxAmount,
				maxTotalPrice, maxPricePerUnit, false, null, commodityCurrency,
				null);
	}

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
		final SortedMap<MarketOrder, Double> selectedOffers = new TreeMap<MarketOrder, Double>();

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
			iterator = ApplicationContext.getInstance().getMarketOrderDAO()
					.getIterator(denominatedInCurrency, commodityCurrency);
		} else if (propertyClass != null) {
			iterator = ApplicationContext.getInstance().getMarketOrderDAO()
					.getIterator(denominatedInCurrency, propertyClass);
		} else {
			iterator = ApplicationContext.getInstance().getMarketOrderDAO()
					.getIterator(denominatedInCurrency, goodType);
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
			assert (marketOrder.getOfferorsBankAcountDelegate()
					.getBankAccount().getCurrency()
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
	 * fixed price functions
	 */

	public PriceFunction getFixedPriceFunction(
			final Currency denominatedInCurrency, final GoodType goodType) {
		return new FixedPriceFunctionImpl(this.getMarginalMarketPrice(
				denominatedInCurrency, goodType));
	}

	public PriceFunction getFixedPriceFunction(
			final Currency denominatedInCurrency,
			final Currency commodityCurrency) {
		return new FixedPriceFunctionImpl(this.getMarginalMarketPrice(
				denominatedInCurrency, commodityCurrency));
	}

	public PriceFunction getFixedPriceFunction(
			final Currency denominatedInCurrency,
			final Class<? extends Property> propertyClass) {
		return new FixedPriceFunctionImpl(this.getMarginalMarketPrice(
				denominatedInCurrency, propertyClass));
	}

	public Map<GoodType, PriceFunction> getFixedPriceFunctions(
			final Currency denominatedInCurrency, final Set<GoodType> goodTypes) {
		final Map<GoodType, PriceFunction> priceFunctions = new HashMap<GoodType, PriceFunction>();
		for (GoodType goodType : goodTypes)
			priceFunctions.put(goodType,
					getFixedPriceFunction(denominatedInCurrency, goodType));
		return priceFunctions;
	}

	/*
	 * marginal market price
	 */

	public double getMarginalMarketPrice(final Currency denominatedInCurrency,
			final GoodType goodType) {
		return this
				.getMarginalMarketPrice(denominatedInCurrency, goodType, 0.0);
	}

	public double getMarginalMarketPrice(final Currency denominatedInCurrency,
			final GoodType goodType, final double atAmount) {
		return this.getMarketPriceFunction(denominatedInCurrency, goodType)
				.getMarginalPrice(atAmount);
	}

	public double getMarginalMarketPrice(final Currency denominatedInCurrency,
			final Currency commodityCurrency) {
		return this.getMarginalMarketPrice(denominatedInCurrency,
				commodityCurrency, 0.0);
	}

	public double getMarginalMarketPrice(final Currency denominatedInCurrency,
			final Currency commodityCurrency, final double atAmount) {
		return this.getMarketPriceFunction(denominatedInCurrency,
				commodityCurrency).getMarginalPrice(atAmount);
	}

	public double getMarginalMarketPrice(final Currency denominatedInCurrency,
			final Class<? extends Property> propertyClass) {
		return ApplicationContext.getInstance().getMarketOrderDAO()
				.findMarginalPrice(denominatedInCurrency, propertyClass);
	}

	public Map<GoodType, Double> getMarginalMarketPrices(
			final Currency denominatedInCurrency, final GoodType[] goodTypes) {
		final Map<GoodType, Double> prices = new HashMap<GoodType, Double>();
		for (GoodType goodType : goodTypes)
			prices.put(goodType,
					getMarginalMarketPrice(denominatedInCurrency, goodType));
		return prices;
	}

	public Map<GoodType, Double> getMarginalMarketPrices(
			final Currency denominatedInCurrency, final Set<GoodType> goodTypes) {
		final Map<GoodType, Double> prices = new HashMap<GoodType, Double>();
		for (GoodType goodType : goodTypes)
			prices.put(goodType,
					getMarginalMarketPrice(denominatedInCurrency, goodType));
		return prices;
	}

	public Map<GoodType, Double> getMarginalMarketPrices(
			final Currency denominatedInCurrency) {
		return getMarginalMarketPrices(denominatedInCurrency, GoodType.values());
	}

	/*
	 * market depth
	 */

	public double getMarketDepth(final Currency denominatedInCurrency,
			final GoodType goodType) {
		return ApplicationContext.getInstance().getMarketOrderDAO()
				.getAmountSum(denominatedInCurrency, goodType);
	}

	public double getMarketDepth(final Currency denominatedInCurrency,
			final Currency commodityCurrency) {
		return ApplicationContext.getInstance().getMarketOrderDAO()
				.getAmountSum(denominatedInCurrency, commodityCurrency);
	}

	/*
	 * market price function
	 */

	public MarketPriceFunction getMarketPriceFunction(
			final Currency denominatedInCurrency, final GoodType goodType) {
		return new MarketPriceFunctionImpl(this, denominatedInCurrency,
				goodType);
	}

	public MarketPriceFunction getMarketPriceFunction(
			final Currency denominatedInCurrency,
			final Currency commodityCurrency) {
		return new MarketPriceFunctionImpl(this, denominatedInCurrency,
				commodityCurrency);
	}

	public Map<GoodType, PriceFunction> getMarketPriceFunctions(
			final Currency denominatedInCurrency, final GoodType[] goodTypes) {
		final Map<GoodType, PriceFunction> priceFunctions = new HashMap<GoodType, PriceFunction>();
		for (GoodType goodType : goodTypes) {
			priceFunctions.put(goodType,
					getMarketPriceFunction(denominatedInCurrency, goodType));
		}
		return priceFunctions;
	}

	public Map<GoodType, PriceFunction> getMarketPriceFunctions(
			final Currency denominatedInCurrency, final Set<GoodType> goodTypes) {
		final Map<GoodType, PriceFunction> priceFunctions = new HashMap<GoodType, PriceFunction>();
		for (GoodType goodType : goodTypes) {
			priceFunctions.put(goodType,
					getMarketPriceFunction(denominatedInCurrency, goodType));
		}
		return priceFunctions;
	}

	/*
	 * iterators
	 */

	protected Iterator<MarketOrder> getMarketOrderIterator(
			final Currency denominatedInCurrency, final GoodType goodType) {
		return ApplicationContext.getInstance().getMarketOrderDAO()
				.getIterator(denominatedInCurrency, goodType);
	}

	protected Iterator<MarketOrder> getMarketOrderIterator(
			final Currency denominatedInCurrency,
			final Currency commodityCurrency) {
		return ApplicationContext.getInstance().getMarketOrderDAO()
				.getIterator(denominatedInCurrency, commodityCurrency);
	}

	protected Iterator<MarketOrder> getMarketOrderIterator(
			final Currency denominatedInCurrency,
			final Class<? extends Property> propertyClass) {
		return ApplicationContext.getInstance().getMarketOrderDAO()
				.getIterator(denominatedInCurrency, propertyClass);
	}

	/*
	 * place selling orders
	 */

	public void placeSellingOffer(final GoodType goodType,
			final MarketParticipant offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double amount, final double pricePerUnit) {
		if (amount > 0) {
			assert (goodType != null);
			assert (!Double.isNaN(amount));
			assert (!Double.isNaN(pricePerUnit));
			assert (amount > 0);
			assert (offeror == offerorsBankAcountDelegate.getBankAccount()
					.getOwner());

			ApplicationContext
					.getInstance()
					.getMarketOrderFactory()
					.newInstanceGoodTypeMarketOrder(goodType, offeror,
							offerorsBankAcountDelegate, amount, pricePerUnit);
			if (getLog().isAgentSelectedByClient(offeror))
				getLog().log(
						offeror,
						"offering "
								+ MathUtil.round(amount)
								+ " units of "
								+ goodType
								+ " for "
								+ Currency.formatMoneySum(pricePerUnit)
								+ " "
								+ offerorsBankAcountDelegate.getBankAccount()
										.getCurrency().getIso4217Code()
								+ " per unit");
		}
	}

	public void placeSellingOffer(
			final Currency commodityCurrency,
			final MarketParticipant offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double amount,
			final double pricePerUnit,
			final BankAccountDelegate commodityCurrencyOfferorsBankAcountDelegate) {
		if (amount > 0) {
			assert (commodityCurrency != null);
			assert (!Double.isNaN(amount));
			assert (!Double.isNaN(pricePerUnit));
			assert (amount > 0);
			assert (offeror == offerorsBankAcountDelegate.getBankAccount()
					.getOwner());

			ApplicationContext
					.getInstance()
					.getMarketOrderFactory()
					.newInstanceCurrencyMarketOrder(commodityCurrency, offeror,
							offerorsBankAcountDelegate, amount, pricePerUnit,
							commodityCurrencyOfferorsBankAcountDelegate);
			if (getLog().isAgentSelectedByClient(offeror))
				getLog().log(
						offeror,
						"offering "
								+ MathUtil.round(amount)
								+ " units of "
								+ commodityCurrency
								+ " for "
								+ Currency.formatMoneySum(pricePerUnit)
								+ " "
								+ offerorsBankAcountDelegate.getBankAccount()
										.getCurrency().getIso4217Code()
								+ " per unit");
		}
	}

	public void placeSellingOffer(final Property property,
			final MarketParticipant offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double pricePerUnit) {
		assert (property != null);
		assert (!Double.isNaN(pricePerUnit));
		assert (offeror == property.getOwner());
		assert (offeror == offerorsBankAcountDelegate.getBankAccount()
				.getOwner());

		ApplicationContext
				.getInstance()
				.getMarketOrderFactory()
				.newInstancePropertyMarketOrder(property, offeror,
						offerorsBankAcountDelegate, pricePerUnit);
		if (getLog().isAgentSelectedByClient(offeror))
			getLog().log(
					offeror,
					"offering 1 unit of "
							+ property.getClass().getSimpleName()
							+ " for "
							+ Currency.formatMoneySum(pricePerUnit)
							+ " "
							+ offerorsBankAcountDelegate.getBankAccount()
									.getCurrency().getIso4217Code()
							+ " per unit");
	}

	/*
	 * remove selling orders
	 */

	public void removeAllSellingOffers(final MarketParticipant offeror) {
		ApplicationContext.getInstance().getMarketOrderDAO()
				.deleteAllSellingOrders(offeror);
		HibernateUtil.flushSession();
	}

	public void removeAllSellingOffers(final MarketParticipant offeror,
			final Currency denominatedInCurrency, final GoodType goodType) {
		ApplicationContext
				.getInstance()
				.getMarketOrderDAO()
				.deleteAllSellingOrders(offeror, denominatedInCurrency,
						goodType);
		HibernateUtil.flushSession();
	}

	public void removeAllSellingOffers(final MarketParticipant offeror,
			final Currency denominatedInCurrency,
			final Currency commodityCurrency) {
		ApplicationContext
				.getInstance()
				.getMarketOrderDAO()
				.deleteAllSellingOrders(offeror, denominatedInCurrency,
						commodityCurrency);
		HibernateUtil.flushSession();
	}

	public void removeAllSellingOffers(final MarketParticipant offeror,
			final Currency denominatedInCurrency,
			final Class<? extends Property> propertyClass) {
		ApplicationContext
				.getInstance()
				.getMarketOrderDAO()
				.deleteAllSellingOrders(offeror, denominatedInCurrency,
						propertyClass);
		HibernateUtil.flushSession();
	}

	protected void removeSellingOffer(final MarketOrder marketOrder) {
		ApplicationContext.getInstance().getMarketOrderDAO()
				.delete(marketOrder);
		HibernateUtil.flushSession();
	}

	protected Log getLog() {
		return ApplicationContext.getInstance().getLog();
	}
}
