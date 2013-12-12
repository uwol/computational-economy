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

package compecon.engine.service;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import compecon.economy.markets.MarketOrder;
import compecon.economy.markets.MarketParticipant;
import compecon.economy.materia.GoodType;
import compecon.economy.property.Property;
import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.service.impl.MarketServiceImpl.MarketPriceFunction;
import compecon.math.price.PriceFunction;
import compecon.math.price.impl.FixedPriceFunction;

public interface MarketService {

	/**
	 * @return A map of {@link MarketOrder}s conjoint with the amount to take
	 *         from these orders.
	 */
	public SortedMap<MarketOrder, Double> findBestFulfillmentSet(
			final Currency denominatedInCurrency, final double maxAmount,
			final double maxTotalPrice, final double maxPricePerUnit,
			final GoodType goodType);

	/**
	 * @return A map of {@link MarketOrder}s conjoint with the amount to take
	 *         from these orders.
	 */
	public SortedMap<MarketOrder, Double> findBestFulfillmentSet(
			final Currency denominatedInCurrency, final double maxAmount,
			final double maxTotalPrice, final double maxPricePerUnit,
			final Currency commodityCurrency);

	/**
	 * @return A map of {@link MarketOrder}s conjoint with the amount to take
	 *         from these orders.
	 */
	public SortedMap<MarketOrder, Double> findBestFulfillmentSet(
			final Currency denominatedInCurrency, final double maxAmount,
			final double maxTotalPrice, final double maxPricePerUnit,
			final Class<? extends Property> propertyClass);

	public double getMarketDepth(final Currency denominatedInCurrency,
			final GoodType goodType);

	public double getMarketDepth(final Currency denominatedInCurrency,
			final Currency commodityCurrency);

	public double getAveragePrice(final Currency denominatedInCurrency,
			final GoodType goodType, final double atAmount);

	public double getPrice(final Currency denominatedInCurrency,
			final GoodType goodType);

	public double getPrice(final Currency denominatedInCurrency,
			final GoodType goodType, final double atAmount);

	public double getPrice(final Currency denominatedInCurrency,
			final Currency commodityCurrency);

	public double getPrice(final Currency denominatedInCurrency,
			final Currency commodityCurrency, final double atAmount);

	public double getPrice(final Currency denominatedInCurrency,
			final Class<? extends Property> propertyClass);

	public Map<GoodType, Double> getPrices(
			final Currency denominatedInCurrency, final GoodType[] goodTypes);

	public Map<GoodType, Double> getPrices(
			final Currency denominatedInCurrency, final Set<GoodType> goodTypes);

	public Map<GoodType, Double> getPrices(final Currency denominatedInCurrency);

	public FixedPriceFunction getFixedPriceFunction(
			final Currency denominatedInCurrency, final GoodType goodType);

	public Map<GoodType, PriceFunction> getFixedPriceFunctions(
			final Currency denominatedInCurrency, final GoodType[] goodTypes);

	public Map<GoodType, PriceFunction> getFixedPriceFunctions(
			final Currency denominatedInCurrency, final Set<GoodType> goodTypes);

	public MarketPriceFunction getMarketPriceFunction(
			final Currency denominatedInCurrency, final GoodType goodType);

	public Map<GoodType, PriceFunction> getMarketPriceFunctions(
			final Currency denominatedInCurrency, final GoodType[] goodTypes);

	public Map<GoodType, PriceFunction> getMarketPriceFunctions(
			final Currency denominatedInCurrency, final Set<GoodType> goodTypes);

	public Iterator<MarketOrder> getMarketOrderIterator(
			final Currency denominatedInCurrency, final GoodType goodType);

	public Iterator<MarketOrder> getMarketOrderIterator(
			final Currency denominatedInCurrency,
			final Currency commodityCurrency);

	public Iterator<MarketOrder> getMarketOrderIterator(
			final Currency denominatedInCurrency,
			final Class<? extends Property> propertyClass);

	public void placeSellingOffer(final GoodType goodType,
			final MarketParticipant offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double amount, final double pricePerUnit);

	/**
	 * Place offer for a certain amount of money.
	 * 
	 * @param commodityCurrency
	 *            Currency of money to be offered, e.g. EURO.
	 * @param offeror
	 * @param offerorsBankAcountDelegate
	 *            Delegate to the bank account of the offeror;
	 *            offerorsBankAcount.currency (e.g. USD) != commodityCurrency
	 *            (e.g. EURO)
	 * @param amount
	 *            Money amount
	 * @param pricePerUnit
	 * @param commodityCurrencyOfferorsBankAcountDelegate
	 *            Delegate to the bank account containing the commodity
	 *            currency, i. e. the bank account the commodity currency should
	 *            be transferred from.
	 */
	public void placeSellingOffer(
			final Currency commodityCurrency,
			final MarketParticipant offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double amount,
			final double pricePerUnit,
			final BankAccountDelegate commodityCurrencyOfferorsBankAcountDelegate);

	public void placeSellingOffer(final Property property,
			final MarketParticipant offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double pricePerUnit);

	public void removeAllSellingOffers(final MarketParticipant offeror);

	public void removeAllSellingOffers(final MarketParticipant offeror,
			final Currency denominatedInCurrency, final GoodType goodType);

	public void removeAllSellingOffers(final MarketParticipant offeror,
			final Currency denominatedInCurrency,
			final Currency commodityCurrency);

	public void removeAllSellingOffers(final MarketParticipant offeror,
			final Currency denominatedInCurrency,
			final Class<? extends Property> propertyClass);
}
