/*
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

package compecon.culture.markets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.property.IProperty;
import compecon.engine.Agent;
import compecon.engine.util.MathUtil;
import compecon.nature.materia.GoodType;

public abstract class Market {

	protected String name;

	// the order book

	protected Map<GoodType, SortedSet<MarketOffer>> sellingOffersForGoodType = new HashMap<GoodType, SortedSet<MarketOffer>>();

	protected Map<Class<? extends IProperty>, SortedSet<MarketOffer>> sellingOffersForIPropertyClass = new HashMap<Class<? extends IProperty>, SortedSet<MarketOffer>>();

	private void assertInitializedDataStructure(GoodType goodType) {
		if (!this.sellingOffersForGoodType.containsKey(goodType))
			sellingOffersForGoodType.put(goodType, new TreeSet<MarketOffer>());
	}

	private void assertInitializedDataStructure(
			Class<? extends IProperty> propertyClass) {
		Class<? extends IProperty> clazz = propertyClass;
		if (!this.sellingOffersForIPropertyClass.containsKey(clazz))
			sellingOffersForIPropertyClass.put(clazz,
					new TreeSet<MarketOffer>());
	}

	private void assertInitializedDataStructure(IProperty property) {
		assertInitializedDataStructure(property.getClass());
	}

	/*
	 * get market offers for type
	 */
	private SortedSet<MarketOffer> getMarketOffers(GoodType goodType) {
		return this.sellingOffersForGoodType.get(goodType);
	}

	private SortedSet<MarketOffer> getMarketOffers(
			Class<? extends IProperty> propertyClass) {
		return this.sellingOffersForIPropertyClass.get(propertyClass);
	}

	private SortedSet<MarketOffer> getMarketOffers(IProperty property) {
		return this.getMarketOffers(property.getClass());
	}

	/*
	 * place selling offers
	 */
	public void placeSellingOffer(GoodType goodType, Agent offeror,
			Currency currency, BankAccount offerorsBankAcount, double amount,
			double pricePerUnit) {
		if (!Double.isNaN(amount) && !Double.isInfinite(amount)
				&& !Double.isNaN(pricePerUnit)
				&& !Double.isInfinite(pricePerUnit) && amount > 0) {
			assertInitializedDataStructure(goodType);
			MarketOffer marketOffer = new MarketOffer(offeror, currency,
					offerorsBankAcount, goodType, pricePerUnit, amount);
			SortedSet<MarketOffer> marketOffers = getMarketOffers(goodType);
			int sizeBefore = marketOffers.size();
			marketOffers.add(marketOffer);
			if (marketOffers.size() != sizeBefore + 1)
				throw new RuntimeException("marketOffer not added");
		}
	}

	public void placeSellingOffer(IProperty property, Agent offeror,
			Currency currency, BankAccount offerorsBankAcount,
			double pricePerUnit) {
		if (!Double.isNaN(pricePerUnit) && !Double.isInfinite(pricePerUnit)) {
			assertInitializedDataStructure(property);
			MarketOffer marketOffer = new MarketOffer(offeror, currency,
					offerorsBankAcount, property, pricePerUnit, 1);
			SortedSet<MarketOffer> marketOffers = getMarketOffers(property);
			int sizeBefore = marketOffers.size();
			marketOffers.add(marketOffer);
			if (marketOffers.size() != sizeBefore + 1)
				throw new RuntimeException("marketOffer not added");
		}
	}

	/*
	 * remove selling offers
	 */
	public void removeSellingOffer(MarketOffer offer) {
		SortedSet<MarketOffer> marketOffers;

		if (offer.getProperty() instanceof GoodType)
			marketOffers = this.getMarketOffers((GoodType) offer.getProperty());
		else
			marketOffers = this.getMarketOffers(offer.getProperty());

		if (!marketOffers.contains(offer))
			throw new RuntimeException(
					"offer to remove not maintained by this market");
		marketOffers.remove(offer);

		if (marketOffers.contains(offer))
			throw new RuntimeException("offer could not be removed");
	}

	public void removeAllSellingOffers(Agent offeror) {
		for (Entry<GoodType, SortedSet<MarketOffer>> entry : this.sellingOffersForGoodType
				.entrySet())
			removeAllSellingOffers(offeror, entry.getKey());
		for (Entry<Class<? extends IProperty>, SortedSet<MarketOffer>> entry : this.sellingOffersForIPropertyClass
				.entrySet())
			removeAllSellingOffers(offeror, entry.getKey());
	}

	public void removeAllSellingOffers(Agent offeror, GoodType goodType) {
		HashSet<MarketOffer> sellingOffersToRemove = new HashSet<MarketOffer>();

		assertInitializedDataStructure(goodType);
		for (MarketOffer marketOffer : getMarketOffers((GoodType) goodType))
			if (marketOffer.getOfferor() == offeror)
				sellingOffersToRemove.add(marketOffer);

		getMarketOffers(goodType).removeAll(sellingOffersToRemove);
	}

	public void removeAllSellingOffers(Agent offeror,
			Class<? extends IProperty> propertyClass) {
		HashSet<MarketOffer> sellingOffersToRemove = new HashSet<MarketOffer>();

		assertInitializedDataStructure(propertyClass);
		for (MarketOffer marketOffer : getMarketOffers(propertyClass))
			if (marketOffer.getOfferor() == offeror)
				sellingOffersToRemove.add(marketOffer);

		getMarketOffers(propertyClass).removeAll(sellingOffersToRemove);
	}

	protected void removeEmptySellingOffers() {
		for (Entry<GoodType, SortedSet<MarketOffer>> entry : this.sellingOffersForGoodType
				.entrySet()) {
			HashSet<MarketOffer> sellingOffersToRemove = new HashSet<MarketOffer>();

			for (MarketOffer marketOffer : entry.getValue()) {
				if (marketOffer.getAmount() <= 0
						|| Double.isNaN(marketOffer.getAmount())
						|| Double.isInfinite(marketOffer.getAmount())
						|| Double.isNaN(marketOffer.getPricePerUnit())
						|| Double.isInfinite(marketOffer.getPricePerUnit()))
					sellingOffersToRemove.add(marketOffer);
			}

			entry.getValue().removeAll(sellingOffersToRemove);
		}
	}

	/*
	 * get price
	 */
	public double getMarginalPrice(GoodType goodType, Currency currency) {
		assertInitializedDataStructure(goodType);
		for (MarketOffer marketOffer : getMarketOffers(goodType)) {
			// search for an offer for the right property and currency
			if (marketOffer.getCurrency() == currency)
				return marketOffer.getPricePerUnit();
		}
		return Double.NaN;
	}

	public double getMarginalPrice(Class<? extends IProperty> propertyClass,
			Currency currency) {
		assertInitializedDataStructure(propertyClass);
		for (MarketOffer marketOffer : getMarketOffers(propertyClass)) {
			// search for an offer for the right property and currency
			if (marketOffer.getCurrency() == currency)
				return marketOffer.getPricePerUnit();
		}
		return Double.NaN;
	}

	public Map<GoodType, Double> getMarginalPrices(Currency currency) {
		Map<GoodType, Double> prices = new HashMap<GoodType, Double>();
		for (GoodType goodType : GoodType.values())
			prices.put(goodType, getMarginalPrice(goodType, currency));
		return prices;
	}

	public double getPriceForAmount(GoodType goodType, Currency currency,
			double demandedAmount) {
		assertInitializedDataStructure(goodType);
		double totalPrice = 0;

		// MarketOffer, amount to take
		Map<MarketOffer, Double> fulfillmentSet = this.findBestFulfillmentSet(
				goodType, currency, -1, demandedAmount, -1, -1, -1);

		Iterator<Entry<MarketOffer, Double>> iterator = fulfillmentSet
				.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<MarketOffer, Double> entry = iterator.next();
			totalPrice = totalPrice + entry.getKey().getPricePerUnit()
					* entry.getValue();
		}
		return totalPrice;
	}

	public double getPriceForAmount(Class<? extends IProperty> propertyClass,
			Currency currency, double demandedAmount) {
		assertInitializedDataStructure(propertyClass);
		double totalPrice = 0;

		// MarketOffer, amount to take
		Map<MarketOffer, Double> fulfillmentSet = this.findBestFulfillmentSet(
				propertyClass, currency, -1, demandedAmount, -1, -1, -1);

		Iterator<Entry<MarketOffer, Double>> iterator = fulfillmentSet
				.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<MarketOffer, Double> entry = iterator.next();
			totalPrice = totalPrice + entry.getKey().getPricePerUnit()
					* entry.getValue();
		}
		return totalPrice;
	}

	/*
	 * get amount
	 */
	public double getTotalAvailableAmount(GoodType goodType, Currency currency) {
		assertInitializedDataStructure(goodType);
		double totalAmount = 0;
		for (MarketOffer marketOffer : getMarketOffers(goodType)) {
			if (marketOffer.getCurrency() == currency)
				totalAmount = totalAmount + marketOffer.getAmount();
		}
		return totalAmount;
	}

	public double getTotalAvailableAmount(Class<IProperty> propertyClass,
			Currency currency) {
		assertInitializedDataStructure(propertyClass);
		double totalAmount = 0;
		for (MarketOffer marketOffer : getMarketOffers(propertyClass)) {
			if (marketOffer.getCurrency() == currency)
				totalAmount = totalAmount + marketOffer.getAmount();
		}
		return totalAmount;
	}

	public double getAvailableAmountForMoneySum(GoodType goodType,
			Currency currency, double maxMoneySum) {
		assertInitializedDataStructure(goodType);
		double amountAvailableForMoneySum = 0;
		Map<MarketOffer, Double> fulfillmentSet = this.findBestFulfillmentSet(
				goodType, currency, -1, -1, maxMoneySum, -1, -1);

		for (Entry<MarketOffer, Double> entry : fulfillmentSet.entrySet()) {
			amountAvailableForMoneySum = amountAvailableForMoneySum
					+ entry.getValue();
		}
		return amountAvailableForMoneySum;
	}

	public double getAvailableAmountForMoneySum(
			Class<? extends IProperty> propertyClass, Currency currency,
			double maxMoneySum) {
		assertInitializedDataStructure(propertyClass);
		double amountAvailableForMoneySum = 0;
		Map<MarketOffer, Double> fulfillmentSet = this.findBestFulfillmentSet(
				propertyClass, currency, -1, -1, maxMoneySum, -1, -1);

		for (Entry<MarketOffer, Double> entry : fulfillmentSet.entrySet()) {
			amountAvailableForMoneySum = amountAvailableForMoneySum
					+ entry.getValue();
		}
		return amountAvailableForMoneySum;
	}

	public SortedMap<MarketOffer, Double> findBestFulfillmentSet(
			GoodType goodType, Currency currency, final double minAmount,
			final double maxAmount, final double maxTotalPrice,
			final double maxTotalPriceForMinAmount, final double maxPricePerUnit) {
		assertInitializedDataStructure(goodType);
		return this.findBestFulfillmentSet(getMarketOffers(goodType), currency,
				minAmount, maxAmount, maxTotalPrice, maxTotalPriceForMinAmount,
				maxPricePerUnit);
	}

	public SortedMap<MarketOffer, Double> findBestFulfillmentSet(
			Class<? extends IProperty> propertyClass, Currency currency,
			final double minAmount, final double maxAmount,
			final double maxTotalPrice, final double maxTotalPriceForMinAmount,
			final double maxPricePerUnit) {
		assertInitializedDataStructure(propertyClass);
		return this.findBestFulfillmentSet(getMarketOffers(propertyClass),
				currency, minAmount, maxAmount, maxTotalPrice,
				maxTotalPriceForMinAmount, maxPricePerUnit);
	}

	/**
	 * maxTotalPrice is ignored, if amount(maxTotalPrice) < minAmount
	 */
	private SortedMap<MarketOffer, Double> findBestFulfillmentSet(
			SortedSet<MarketOffer> marketOffers, Currency currency,
			final double minAmount, double maxAmount,
			final double maxTotalPrice, final double maxTotalPriceForMinAmount,
			final double maxPricePerUnit) {

		if (Double.isInfinite(maxAmount))
			maxAmount = -1;

		// MarketOffer, Amount
		SortedMap<MarketOffer, Double> selectedOffers = new TreeMap<MarketOffer, Double>();

		boolean restrictMaxAmount = true;
		if (maxAmount < 0)
			restrictMaxAmount = false;

		boolean restrictTotalPrice = true;
		if (maxTotalPrice < 0)
			restrictTotalPrice = false;

		boolean restrictTotalPriceForMinAmount = true;
		if (minAmount < 0 || maxTotalPriceForMinAmount < 0)
			restrictTotalPriceForMinAmount = false;

		boolean restrictMaxPricePerUnit = true;
		if (maxPricePerUnit < 0)
			restrictMaxPricePerUnit = false;

		double selectedAmount = 0;
		double spentMoney = 0;

		// double amountLeft = maxAmount;
		// double moneyLeft = maxTotalPrice;

		// search for offers for the right property and currency starting
		// with the lowest price/unit
		for (MarketOffer offer : marketOffers) {
			if (offer.getAmount() <= 0)
				throw new RuntimeException("amount of offer is "
						+ offer.getAmount());

			if (offer.getCurrency() == currency) { // is the currency correct?
				double amountToTakeByMaxAmountRestriction;
				double amountToTakeByMinAmountRestriction;
				double amountToTakeByTotalPriceRestriction;
				double amountToTakeByTotalPriceForMinAmountRestriction;
				double amountToTakeByMaxPricePerUnitRestriction;

				// amountToTakeByMaxAmountRestriction
				if (restrictMaxAmount)
					amountToTakeByMaxAmountRestriction = Math.min(maxAmount
							- selectedAmount, offer.getAmount());
				else
					amountToTakeByMaxAmountRestriction = offer.getAmount();

				// amountToTakeByMinAmountRestriction
				if (minAmount > 0)
					amountToTakeByMinAmountRestriction = Math.max(
							0,
							Math.min(minAmount - selectedAmount,
									offer.getAmount()));
				else
					amountToTakeByMinAmountRestriction = 0;

				// amountToTakeByTotalPriceRestriction
				// division by 0 not allowed !
				if (restrictTotalPrice && offer.getPricePerUnit() != 0) {
					amountToTakeByTotalPriceRestriction = Math.min(
							(maxTotalPrice - spentMoney)
									/ offer.getPricePerUnit(),
							offer.getAmount());
				} else
					amountToTakeByTotalPriceRestriction = offer.getAmount();

				// amountToTakeByTotalPriceForMinAmountRestriction
				// division by 0 not allowed !
				if (restrictTotalPriceForMinAmount
						&& offer.getPricePerUnit() != 0) {
					amountToTakeByTotalPriceForMinAmountRestriction = Math.min(
							(maxTotalPriceForMinAmount - spentMoney)
									/ offer.getPricePerUnit(),
							offer.getAmount());
				} else
					amountToTakeByTotalPriceForMinAmountRestriction = offer
							.getAmount();

				// amountToTakeByMaxPricePerUnitRestriction
				if (restrictMaxPricePerUnit
						&& offer.getPricePerUnit() > maxPricePerUnit) {
					amountToTakeByMaxPricePerUnitRestriction = 0;
				} else
					amountToTakeByMaxPricePerUnitRestriction = offer
							.getAmount();

				// final amount decision
				double amountToTake = Math
						.max(0,
								Math.min(
										amountToTakeByMaxAmountRestriction,
										Math.max(
												Math.min(
														amountToTakeByTotalPriceRestriction,
														amountToTakeByMaxPricePerUnitRestriction),
												Math.min(
														amountToTakeByTotalPriceForMinAmountRestriction,
														amountToTakeByMinAmountRestriction))));

				double totalPrice = amountToTake * offer.getPricePerUnit();

				if (amountToTake == 0) {
					break;
				} else if (Double.isNaN(amountToTake)
						|| Double.isInfinite(amountToTake)) {
					throw new RuntimeException("amount to take is "
							+ amountToTake);
				} else {
					selectedOffers.put(offer, amountToTake);
					selectedAmount += amountToTake;
					spentMoney += totalPrice;

					if (restrictTotalPrice && (minAmount <= 0)
							&& (spentMoney - 0.00001 > maxTotalPrice))
						throw new RuntimeException(
								"Market calculated incorrect amount: spent too much money");
					if (restrictMaxAmount
							&& !MathUtil.equal(selectedAmount, maxAmount)
							&& (selectedAmount > maxAmount))
						throw new RuntimeException(
								"Market calculated incorrect amount: selected too much units");
				}
			}
		}
		return selectedOffers;
	}
}
