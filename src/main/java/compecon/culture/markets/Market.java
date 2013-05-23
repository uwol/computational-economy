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
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import compecon.culture.markets.ordertypes.MarketOrder;
import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.property.Property;
import compecon.engine.Agent;
import compecon.engine.MarketOrderFactory;
import compecon.engine.dao.DAOFactory;
import compecon.engine.util.HibernateUtil;
import compecon.engine.util.MathUtil;
import compecon.nature.materia.GoodType;

public abstract class Market {

	/*
	 * place selling orders
	 */
	public void placeSellingOffer(GoodType goodType, Agent offeror,
			BankAccount offerorsBankAcount, double amount, double pricePerUnit,
			Currency currency) {
		if (!Double.isNaN(amount) && !Double.isInfinite(amount)
				&& !Double.isNaN(pricePerUnit)
				&& !Double.isInfinite(pricePerUnit) && amount > 0) {
			MarketOrderFactory
					.newInstanceGoodTypeMarketOrder(goodType, offeror,
							offerorsBankAcount, amount, pricePerUnit, currency);
		}
	}

	public void placeSellingOffer(Property property, Agent offeror,
			BankAccount offerorsBankAcount, double pricePerUnit,
			Currency currency) {
		if (!Double.isNaN(pricePerUnit) && !Double.isInfinite(pricePerUnit)) {
			MarketOrderFactory.newInstancePropertyMarketOrder(property,
					offeror, offerorsBankAcount, pricePerUnit, currency);
		}
	}

	/*
	 * remove selling orders
	 */
	public void removeAllSellingOffers(Agent offeror) {
		DAOFactory.getMarketOrderDAO().deleteAllSellingOrders(offeror);
		HibernateUtil.flushSession();
	}

	public void removeAllSellingOffers(Agent offeror, Currency currency,
			GoodType goodType) {
		DAOFactory.getMarketOrderDAO().deleteAllSellingOrders(offeror,
				currency, goodType);
		HibernateUtil.flushSession();
	}

	public void removeAllSellingOffers(Agent offeror, Currency currency,
			Class<? extends Property> propertyClass) {
		DAOFactory.getMarketOrderDAO().deleteAllSellingOrders(offeror,
				currency, propertyClass);
		HibernateUtil.flushSession();
	}

	protected void removeSellingOffer(MarketOrder marketOrder) {
		DAOFactory.getMarketOrderDAO().delete(marketOrder);
		HibernateUtil.flushSession();
	}

	/*
	 * get price
	 */
	public double getMarginalPrice(Currency currency, GoodType goodType) {
		return DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
				goodType);
	}

	public double getMarginalPrice(Currency currency,
			Class<? extends Property> propertyClass) {
		return DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
				propertyClass);
	}

	public Map<GoodType, Double> getMarginalPrices(Currency currency) {
		Map<GoodType, Double> prices = new HashMap<GoodType, Double>();
		for (GoodType goodType : GoodType.values())
			prices.put(goodType, getMarginalPrice(currency, goodType));
		return prices;
	}

	/*
	 * fulfillment
	 */

	public SortedMap<MarketOrder, Double> findBestFulfillmentSet(
			GoodType goodType, Currency currency, double maxAmount,
			double maxTotalPrice, double maxPricePerUnit) {
		if (Double.isInfinite(maxAmount))
			maxAmount = -1;

		// MarketOrder, Amount
		SortedMap<MarketOrder, Double> selectedOffers = new TreeMap<MarketOrder, Double>();

		boolean restrictMaxAmount = true;
		if (maxAmount < 0)
			restrictMaxAmount = false;

		boolean restrictTotalPrice = true;
		if (maxTotalPrice < 0)
			restrictTotalPrice = false;

		boolean restrictMaxPricePerUnit = true;
		if (maxPricePerUnit < 0)
			restrictMaxPricePerUnit = false;

		double selectedAmount = 0;
		double spentMoney = 0;

		// search for orders for the right property starting
		// with the lowest price/unit
		Iterator<MarketOrder> iterator = DAOFactory.getMarketOrderDAO()
				.getIterator(goodType, currency);
		while (iterator.hasNext()) {
			MarketOrder offer = iterator.next();

			if (restrictMaxPricePerUnit
					&& MathUtil.greater(offer.getPricePerUnit(),
							maxPricePerUnit))
				break;

			if (offer.getAmount() <= 0)
				throw new RuntimeException("amount of offer is "
						+ offer.getAmount());

			// is the currency correct?
			if (offer.getCurrency() != currency) {
				throw new RuntimeException("wrong currency " + currency);
			}
			double amountToTakeByMaxAmountRestriction;
			double amountToTakeByTotalPriceRestriction;
			double amountToTakeByMaxPricePerUnitRestriction;

			// amountToTakeByMaxAmountRestriction
			if (restrictMaxAmount)
				amountToTakeByMaxAmountRestriction = Math.min(maxAmount
						- selectedAmount, offer.getAmount());
			else
				amountToTakeByMaxAmountRestriction = offer.getAmount();

			// amountToTakeByTotalPriceRestriction
			// division by 0 not allowed !
			if (restrictTotalPrice && offer.getPricePerUnit() != 0) {
				amountToTakeByTotalPriceRestriction = Math.min(
						(maxTotalPrice - spentMoney) / offer.getPricePerUnit(),
						offer.getAmount());
			} else
				amountToTakeByTotalPriceRestriction = offer.getAmount();

			// amountToTakeByMaxPricePerUnitRestriction
			if (restrictMaxPricePerUnit
					&& offer.getPricePerUnit() > maxPricePerUnit) {
				amountToTakeByMaxPricePerUnitRestriction = 0;
			} else
				amountToTakeByMaxPricePerUnitRestriction = offer.getAmount();

			// final amount decision
			double amountToTake = Math.max(0, Math.min(
					amountToTakeByMaxAmountRestriction, Math.min(
							amountToTakeByTotalPriceRestriction,
							amountToTakeByMaxPricePerUnitRestriction)));

			double totalPrice = amountToTake * offer.getPricePerUnit();

			if (amountToTake == 0) {
				break;
			} else if (Double.isNaN(amountToTake)
					|| Double.isInfinite(amountToTake)) {
				throw new RuntimeException("amount to take is " + amountToTake);
			} else {
				selectedOffers.put(offer, amountToTake);
				selectedAmount += amountToTake;
				spentMoney += totalPrice;

				if (spentMoney != 0 && restrictTotalPrice
						&& (MathUtil.greater(spentMoney, maxTotalPrice)))
					throw new RuntimeException(
							"Market calculated incorrect amount: spent too much money");
				if (restrictMaxAmount
						&& !MathUtil.equal(selectedAmount, maxAmount)
						&& (selectedAmount > maxAmount))
					throw new RuntimeException(
							"Market calculated incorrect amount: selected too much units");
			}
		}
		return selectedOffers;
	}

	public SortedSet<MarketOrder> findBestFulfillmentSet(
			Class<? extends Property> propertyClass, Currency currency,
			double maxAmount, final double maxTotalPrice,
			final double maxPricePerUnit) {
		if (Double.isInfinite(maxAmount))
			maxAmount = -1;

		// MarketOrder, Amount
		SortedSet<MarketOrder> selectedOffers = new TreeSet<MarketOrder>();

		boolean restrictMaxAmount = true;
		if (maxAmount < 0)
			restrictMaxAmount = false;

		boolean restrictTotalPrice = true;
		if (maxTotalPrice < 0)
			restrictTotalPrice = false;

		boolean restrictMaxPricePerUnit = true;
		if (maxPricePerUnit < 0)
			restrictMaxPricePerUnit = false;

		double selectedAmount = 0;
		double spentMoney = 0;

		// search for offers for the right property starting
		// with the lowest price/unit
		Iterator<MarketOrder> iterator = DAOFactory.getMarketOrderDAO()
				.getIterator(propertyClass, currency);
		while (iterator.hasNext()) {
			MarketOrder offer = iterator.next();

			if (restrictMaxPricePerUnit
					&& MathUtil.greater(offer.getPricePerUnit(),
							maxPricePerUnit))
				break;

			// is the currency correct?
			if (offer.getCurrency() != currency) {
				throw new RuntimeException("wrong currency " + currency);
			}
			double amountToTakeByMaxAmountRestriction;
			double amountToTakeByTotalPriceRestriction;
			double amountToTakeByMaxPricePerUnitRestriction;

			// amountToTakeByMaxAmountRestriction
			if (restrictMaxAmount)
				amountToTakeByMaxAmountRestriction = Math.min(maxAmount
						- selectedAmount, 1);
			else
				amountToTakeByMaxAmountRestriction = 1;

			// amountToTakeByTotalPriceRestriction
			// division by 0 not allowed !
			if (restrictTotalPrice && offer.getPricePerUnit() != 0) {
				amountToTakeByTotalPriceRestriction = Math.min(
						(maxTotalPrice - spentMoney) / offer.getPricePerUnit(),
						1);
			} else
				amountToTakeByTotalPriceRestriction = 1;

			// amountToTakeByMaxPricePerUnitRestriction
			if (restrictMaxPricePerUnit
					&& offer.getPricePerUnit() > maxPricePerUnit) {
				amountToTakeByMaxPricePerUnitRestriction = 0;
			} else
				amountToTakeByMaxPricePerUnitRestriction = 1;

			// final amount decision
			double amountToTake = Math.max(0, Math.min(
					amountToTakeByMaxAmountRestriction, Math.min(
							amountToTakeByTotalPriceRestriction,
							amountToTakeByMaxPricePerUnitRestriction)));

			double totalPrice = amountToTake * offer.getPricePerUnit();

			if (amountToTake == 0) {
				break;
			} else if (Double.isNaN(amountToTake)
					|| Double.isInfinite(amountToTake)) {
				throw new RuntimeException("amount to take is " + amountToTake);
			} else {
				selectedOffers.add(offer);
				selectedAmount += amountToTake;
				spentMoney += totalPrice;

				if (spentMoney != 0 && restrictTotalPrice
						&& (MathUtil.greater(spentMoney, maxTotalPrice)))
					throw new RuntimeException(
							"Market calculated incorrect amount: spent too much money");
				if (restrictMaxAmount
						&& !MathUtil.equal(selectedAmount, maxAmount)
						&& (selectedAmount > maxAmount))
					throw new RuntimeException(
							"Market calculated incorrect amount: selected too much units");
			}

		}
		return selectedOffers;
	}
}
