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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import io.github.uwol.compecon.economy.markets.MarketOrder;
import io.github.uwol.compecon.economy.markets.MarketParticipant;
import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.property.Property;
import io.github.uwol.compecon.economy.sectors.financial.BankAccountDelegate;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.log.Log;
import io.github.uwol.compecon.engine.service.MarketPriceFunction;
import io.github.uwol.compecon.engine.service.MarketService;
import io.github.uwol.compecon.math.price.PriceFunction;
import io.github.uwol.compecon.math.util.MathUtil;

public abstract class MarketServiceImpl implements MarketService {

	/*
	 * fulfillment
	 */

	/**
	 * @return A map of {@link MarketOrder}s conjoint with the amount to take from
	 *         these orders.
	 */
	protected SortedMap<MarketOrder, Double> findBestFulfillmentSet(final Currency denominatedInCurrency,
			final double maxAmount, final double maxTotalPrice, final double maxPricePerUnit, final boolean wholeNumber,
			final GoodType goodType, final Currency commodityCurrency, final Class<? extends Property> propertyClass) {

		assert (MathUtil.greaterEqual(maxAmount, 0.0) || Double.isNaN(maxAmount));
		assert (MathUtil.greaterEqual(maxTotalPrice, 0.0) || Double.isNaN(maxTotalPrice));
		assert (MathUtil.greaterEqual(maxPricePerUnit, 0.0) || Double.isNaN(maxPricePerUnit));

		// MarketOrder, Amount
		final SortedMap<MarketOrder, Double> selectedOffers = new TreeMap<MarketOrder, Double>();

		boolean restrictMaxAmount = true;
		if (Double.isInfinite(maxAmount) || Double.isNaN(maxAmount)) {
			restrictMaxAmount = false;
		}

		boolean restrictTotalPrice = true;
		if (Double.isInfinite(maxTotalPrice) || Double.isNaN(maxTotalPrice)) {
			restrictTotalPrice = false;
		}

		boolean restrictMaxPricePerUnit = true;
		if (Double.isInfinite(maxPricePerUnit) || Double.isNaN(maxPricePerUnit)) {
			restrictMaxPricePerUnit = false;
		}

		double selectedAmount = 0;
		double spentMoney = 0;

		/*
		 * identify correct iterator
		 */
		Iterator<MarketOrder> iterator;
		if (commodityCurrency != null) {
			iterator = ApplicationContext.getInstance().getMarketOrderDAO().getIterator(denominatedInCurrency,
					commodityCurrency);
		} else if (propertyClass != null) {
			iterator = ApplicationContext.getInstance().getMarketOrderDAO().getIterator(denominatedInCurrency,
					propertyClass);
		} else {
			iterator = ApplicationContext.getInstance().getMarketOrderDAO().getIterator(denominatedInCurrency,
					goodType);
		}

		/*
		 * search for orders starting with the lowest price/unit
		 */
		while (iterator.hasNext()) {
			final MarketOrder marketOrder = iterator.next();

			// is maxPricePerUnit exceeded?
			if (restrictMaxPricePerUnit && MathUtil.greater(marketOrder.getPricePerUnit(), maxPricePerUnit)) {
				break;
			}

			// is the amount correct?
			assert (marketOrder.getAmount() > 0);

			// is the currency correct?
			assert (marketOrder.getOfferorsBankAcountDelegate().getBankAccount().getCurrency()
					.equals(denominatedInCurrency));

			double amountToTakeByMaxAmountRestriction;
			double amountToTakeByTotalPriceRestriction;
			double amountToTakeByMaxPricePerUnitRestriction;

			// amountToTakeByMaxAmountRestriction
			if (restrictMaxAmount) {
				amountToTakeByMaxAmountRestriction = Math.min(maxAmount - selectedAmount, marketOrder.getAmount());
			} else {
				amountToTakeByMaxAmountRestriction = marketOrder.getAmount();
			}

			// amountToTakeByTotalPriceRestriction
			// division by 0 not allowed !
			if (restrictTotalPrice && marketOrder.getPricePerUnit() != 0) {
				amountToTakeByTotalPriceRestriction = Math
						.min((maxTotalPrice - spentMoney) / marketOrder.getPricePerUnit(), marketOrder.getAmount());
			} else {
				amountToTakeByTotalPriceRestriction = marketOrder.getAmount();
			}

			// amountToTakeByMaxPricePerUnitRestriction
			if (restrictMaxPricePerUnit && marketOrder.getPricePerUnit() > maxPricePerUnit) {
				amountToTakeByMaxPricePerUnitRestriction = 0;
			} else {
				amountToTakeByMaxPricePerUnitRestriction = marketOrder.getAmount();
			}

			// final amount decision
			double amountToTake = Math.max(0, Math.min(amountToTakeByMaxAmountRestriction,
					Math.min(amountToTakeByTotalPriceRestriction, amountToTakeByMaxPricePerUnitRestriction)));

			// wholeNumberRestriction
			if (wholeNumber) {
				amountToTake = (long) amountToTake;
			}

			final double totalPrice = amountToTake * marketOrder.getPricePerUnit();

			assert (!Double.isNaN(amountToTake) && !Double.isInfinite(amountToTake));

			if (amountToTake == 0) {
				break;
			} else {
				selectedOffers.put(marketOrder, amountToTake);
				selectedAmount += amountToTake;
				spentMoney += totalPrice;

				assert (!(spentMoney != 0 && restrictTotalPrice && (MathUtil.greater(spentMoney, maxTotalPrice))));
				assert (!(restrictMaxAmount && !MathUtil.equal(selectedAmount, maxAmount)
						&& (selectedAmount > maxAmount)));
			}
		}
		return selectedOffers;
	}

	@Override
	public SortedMap<MarketOrder, Double> findBestFulfillmentSet(final Currency denominatedInCurrency,
			final double maxAmount, final double maxTotalPrice, final double maxPricePerUnit,
			final Class<? extends Property> propertyClass) {
		return this.findBestFulfillmentSet(denominatedInCurrency, maxAmount, maxTotalPrice, maxPricePerUnit, true, null,
				null, propertyClass);
	}

	@Override
	public SortedMap<MarketOrder, Double> findBestFulfillmentSet(final Currency denominatedInCurrency,
			final double maxAmount, final double maxTotalPrice, final double maxPricePerUnit,
			final Currency commodityCurrency) {
		return this.findBestFulfillmentSet(denominatedInCurrency, maxAmount, maxTotalPrice, maxPricePerUnit, false,
				null, commodityCurrency, null);
	}

	@Override
	public SortedMap<MarketOrder, Double> findBestFulfillmentSet(final Currency denominatedInCurrency,
			final double maxAmount, final double maxTotalPrice, final double maxPricePerUnit, final GoodType goodType) {
		return this.findBestFulfillmentSet(denominatedInCurrency, maxAmount, maxTotalPrice, maxPricePerUnit,
				goodType.isWholeNumber(), goodType, null, null);
	}

	/*
	 * fixed price functions
	 */

	@Override
	public PriceFunction getFixedPriceFunction(final Currency denominatedInCurrency,
			final Class<? extends Property> propertyClass) {
		return new FixedPriceFunctionImpl(this.getMarginalMarketPrice(denominatedInCurrency, propertyClass));
	}

	@Override
	public PriceFunction getFixedPriceFunction(final Currency denominatedInCurrency, final Currency commodityCurrency) {
		return new FixedPriceFunctionImpl(this.getMarginalMarketPrice(denominatedInCurrency, commodityCurrency));
	}

	@Override
	public PriceFunction getFixedPriceFunction(final Currency denominatedInCurrency, final GoodType goodType) {
		return new FixedPriceFunctionImpl(this.getMarginalMarketPrice(denominatedInCurrency, goodType));
	}

	@Override
	public Map<GoodType, PriceFunction> getFixedPriceFunctions(final Currency denominatedInCurrency,
			final Set<GoodType> goodTypes) {
		final Map<GoodType, PriceFunction> priceFunctions = new HashMap<GoodType, PriceFunction>();
		for (final GoodType goodType : goodTypes) {
			priceFunctions.put(goodType, getFixedPriceFunction(denominatedInCurrency, goodType));
		}
		return priceFunctions;
	}

	/*
	 * marginal market price
	 */

	protected Log getLog() {
		return ApplicationContext.getInstance().getLog();
	}

	@Override
	public double getMarginalMarketPrice(final Currency denominatedInCurrency,
			final Class<? extends Property> propertyClass) {
		return ApplicationContext.getInstance().getMarketOrderDAO().findMarginalPrice(denominatedInCurrency,
				propertyClass);
	}

	@Override
	public double getMarginalMarketPrice(final Currency denominatedInCurrency, final Currency commodityCurrency) {
		return this.getMarginalMarketPrice(denominatedInCurrency, commodityCurrency, 0.0);
	}

	@Override
	public double getMarginalMarketPrice(final Currency denominatedInCurrency, final Currency commodityCurrency,
			final double atAmount) {
		return this.getMarketPriceFunction(denominatedInCurrency, commodityCurrency).getMarginalPrice(atAmount);
	}

	@Override
	public double getMarginalMarketPrice(final Currency denominatedInCurrency, final GoodType goodType) {
		return this.getMarginalMarketPrice(denominatedInCurrency, goodType, 0.0);
	}

	@Override
	public double getMarginalMarketPrice(final Currency denominatedInCurrency, final GoodType goodType,
			final double atAmount) {
		return this.getMarketPriceFunction(denominatedInCurrency, goodType).getMarginalPrice(atAmount);
	}

	@Override
	public Map<GoodType, Double> getMarginalMarketPrices(final Currency denominatedInCurrency) {
		return getMarginalMarketPrices(denominatedInCurrency, GoodType.values());
	}

	@Override
	public Map<GoodType, Double> getMarginalMarketPrices(final Currency denominatedInCurrency,
			final GoodType[] goodTypes) {
		final Map<GoodType, Double> prices = new HashMap<GoodType, Double>();
		for (final GoodType goodType : goodTypes) {
			prices.put(goodType, getMarginalMarketPrice(denominatedInCurrency, goodType));
		}
		return prices;
	}

	/*
	 * market depth
	 */

	@Override
	public Map<GoodType, Double> getMarginalMarketPrices(final Currency denominatedInCurrency,
			final Set<GoodType> goodTypes) {
		final Map<GoodType, Double> prices = new HashMap<GoodType, Double>();
		for (final GoodType goodType : goodTypes) {
			prices.put(goodType, getMarginalMarketPrice(denominatedInCurrency, goodType));
		}
		return prices;
	}

	@Override
	public double getMarketDepth(final Currency denominatedInCurrency, final Currency commodityCurrency) {
		return ApplicationContext.getInstance().getMarketOrderDAO().getAmountSum(denominatedInCurrency,
				commodityCurrency);
	}

	/*
	 * market price function
	 */

	@Override
	public double getMarketDepth(final Currency denominatedInCurrency, final GoodType goodType) {
		return ApplicationContext.getInstance().getMarketOrderDAO().getAmountSum(denominatedInCurrency, goodType);
	}

	protected Iterator<MarketOrder> getMarketOrderIterator(final Currency denominatedInCurrency,
			final Class<? extends Property> propertyClass) {
		return ApplicationContext.getInstance().getMarketOrderDAO().getIterator(denominatedInCurrency, propertyClass);
	}

	protected Iterator<MarketOrder> getMarketOrderIterator(final Currency denominatedInCurrency,
			final Currency commodityCurrency) {
		return ApplicationContext.getInstance().getMarketOrderDAO().getIterator(denominatedInCurrency,
				commodityCurrency);
	}

	protected Iterator<MarketOrder> getMarketOrderIterator(final Currency denominatedInCurrency,
			final GoodType goodType) {
		return ApplicationContext.getInstance().getMarketOrderDAO().getIterator(denominatedInCurrency, goodType);
	}

	/*
	 * iterators
	 */

	@Override
	public MarketPriceFunction getMarketPriceFunction(final Currency denominatedInCurrency,
			final Currency commodityCurrency) {
		return new MarketPriceFunctionImpl(this, denominatedInCurrency, commodityCurrency);
	}

	@Override
	public MarketPriceFunction getMarketPriceFunction(final Currency denominatedInCurrency, final GoodType goodType) {
		return new MarketPriceFunctionImpl(this, denominatedInCurrency, goodType);
	}

	@Override
	public Map<GoodType, PriceFunction> getMarketPriceFunctions(final Currency denominatedInCurrency,
			final GoodType[] goodTypes) {
		final Map<GoodType, PriceFunction> priceFunctions = new HashMap<GoodType, PriceFunction>();
		for (final GoodType goodType : goodTypes) {
			priceFunctions.put(goodType, getMarketPriceFunction(denominatedInCurrency, goodType));
		}
		return priceFunctions;
	}

	/*
	 * place selling orders
	 */

	@Override
	public Map<GoodType, PriceFunction> getMarketPriceFunctions(final Currency denominatedInCurrency,
			final Set<GoodType> goodTypes) {
		final Map<GoodType, PriceFunction> priceFunctions = new HashMap<GoodType, PriceFunction>();
		for (final GoodType goodType : goodTypes) {
			priceFunctions.put(goodType, getMarketPriceFunction(denominatedInCurrency, goodType));
		}
		return priceFunctions;
	}

	@Override
	public void placeSellingOffer(final Currency commodityCurrency, final MarketParticipant offeror,
			final BankAccountDelegate offerorsBankAcountDelegate, final double amount, final double pricePerUnit,
			final BankAccountDelegate commodityCurrencyOfferorsBankAcountDelegate) {
		if (amount > 0) {
			assert (commodityCurrency != null);
			assert (!Double.isNaN(amount));
			assert (!Double.isNaN(pricePerUnit));
			assert (amount > 0);
			assert (offeror == offerorsBankAcountDelegate.getBankAccount().getOwner());

			ApplicationContext.getInstance().getMarketOrderFactory().newInstanceCurrencyMarketOrder(commodityCurrency,
					offeror, offerorsBankAcountDelegate, amount, pricePerUnit,
					commodityCurrencyOfferorsBankAcountDelegate);
			if (getLog().isAgentSelectedByClient(offeror)) {
				getLog().log(offeror, "offering %s units of %s for %s %s per unit", MathUtil.round(amount),
						commodityCurrency, Currency.formatMoneySum(pricePerUnit),
						offerorsBankAcountDelegate.getBankAccount().getCurrency());
			}
		}
	}

	@Override
	public void placeSellingOffer(final GoodType goodType, final MarketParticipant offeror,
			final BankAccountDelegate offerorsBankAcountDelegate, final double amount, final double pricePerUnit) {
		if (amount > 0) {
			assert (goodType != null);
			assert (!Double.isNaN(amount));
			assert (!Double.isNaN(pricePerUnit));
			assert (amount > 0);
			assert (offeror == offerorsBankAcountDelegate.getBankAccount().getOwner());

			ApplicationContext.getInstance().getMarketOrderFactory().newInstanceGoodTypeMarketOrder(goodType, offeror,
					offerorsBankAcountDelegate, amount, pricePerUnit);
			if (getLog().isAgentSelectedByClient(offeror)) {
				getLog().log(offeror, "offering %s units of %s for %s %s per unit", MathUtil.round(amount), goodType,
						Currency.formatMoneySum(pricePerUnit),
						offerorsBankAcountDelegate.getBankAccount().getCurrency());
			}
		}
	}

	/*
	 * remove selling orders
	 */

	@Override
	public void placeSellingOffer(final Property property, final MarketParticipant offeror,
			final BankAccountDelegate offerorsBankAcountDelegate, final double pricePerUnit) {
		assert (property != null);
		assert (!Double.isNaN(pricePerUnit));
		assert (offeror == property.getOwner());
		assert (offeror == offerorsBankAcountDelegate.getBankAccount().getOwner());

		ApplicationContext.getInstance().getMarketOrderFactory().newInstancePropertyMarketOrder(property, offeror,
				offerorsBankAcountDelegate, pricePerUnit);
		if (getLog().isAgentSelectedByClient(offeror)) {
			getLog().log(offeror, "offering 1 unit of %s for %s %s per unit", property.getClass().getSimpleName(),
					Currency.formatMoneySum(pricePerUnit), offerorsBankAcountDelegate.getBankAccount().getCurrency());
		}
	}

	@Override
	public void removeAllSellingOffers(final MarketParticipant offeror) {
		ApplicationContext.getInstance().getMarketOrderFactory().deleteAllSellingOrders(offeror);
	}

	@Override
	public void removeAllSellingOffers(final MarketParticipant offeror, final Currency denominatedInCurrency,
			final Class<? extends Property> propertyClass) {
		ApplicationContext.getInstance().getMarketOrderFactory().deleteAllSellingOrders(offeror, denominatedInCurrency,
				propertyClass);
	}

	@Override
	public void removeAllSellingOffers(final MarketParticipant offeror, final Currency denominatedInCurrency,
			final Currency commodityCurrency) {
		ApplicationContext.getInstance().getMarketOrderFactory().deleteAllSellingOrders(offeror, denominatedInCurrency,
				commodityCurrency);
	}

	@Override
	public void removeAllSellingOffers(final MarketParticipant offeror, final Currency denominatedInCurrency,
			final GoodType goodType) {
		ApplicationContext.getInstance().getMarketOrderFactory().deleteAllSellingOrders(offeror, denominatedInCurrency,
				goodType);
	}

	protected void removeSellingOffer(final MarketOrder marketOrder) {
		ApplicationContext.getInstance().getMarketOrderFactory().deleteSellingOrder(marketOrder);
	}
}
