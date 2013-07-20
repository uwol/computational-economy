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
import java.util.TreeMap;

import compecon.culture.markets.ordertypes.MarketOrder;
import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.property.Property;
import compecon.engine.Agent;
import compecon.engine.MarketOrderFactory;
import compecon.engine.dao.DAOFactory;
import compecon.engine.jmx.Log;
import compecon.engine.util.HibernateUtil;
import compecon.engine.util.MathUtil;
import compecon.nature.materia.GoodType;

public abstract class Market {

	/*
	 * place selling orders
	 */
	public void placeSellingOffer(GoodType goodType, Agent offeror,
			BankAccount offerorsBankAcount, double amount, double pricePerUnit) {
		if (goodType != null && !Double.isNaN(amount)
				&& !Double.isNaN(pricePerUnit) && amount > 0) {
			MarketOrderFactory.newInstanceGoodTypeMarketOrder(goodType,
					offeror, offerorsBankAcount, amount, pricePerUnit);
			if (Log.isAgentSelectedByClient(offeror))
				Log.log(offeror,
						"offering "
								+ MathUtil.round(amount)
								+ " units of "
								+ goodType
								+ " for "
								+ Currency.round(pricePerUnit)
								+ " "
								+ offerorsBankAcount.getCurrency()
										.getIso4217Code() + " per unit");
		} else
			throw new RuntimeException("error placing selling offer");
	}

	public void placeSellingOffer(Currency commodityCurrency, Agent offeror,
			BankAccount offerorsBankAcount, double amount, double pricePerUnit,
			BankAccount commodityCurrencyOfferorsBankAcount,
			String commodityCurrencyOfferorsBankAcountPassword) {
		if (commodityCurrency != null && !Double.isNaN(amount)
				&& !Double.isNaN(pricePerUnit) && amount > 0
				&& commodityCurrencyOfferorsBankAcountPassword != null) {
			commodityCurrencyOfferorsBankAcount.getManagingBank()
					.assertPasswordOk(offeror,
							commodityCurrencyOfferorsBankAcountPassword);

			MarketOrderFactory.newInstanceCurrencyMarketOrder(
					commodityCurrency, offeror, offerorsBankAcount, amount,
					pricePerUnit, commodityCurrencyOfferorsBankAcount,
					commodityCurrencyOfferorsBankAcountPassword);
			if (Log.isAgentSelectedByClient(offeror))
				Log.log(offeror,
						"offering "
								+ MathUtil.round(amount)
								+ " units of "
								+ commodityCurrency
								+ " for "
								+ Currency.round(pricePerUnit)
								+ " "
								+ offerorsBankAcount.getCurrency()
										.getIso4217Code() + " per unit");
		} else
			throw new RuntimeException("error placing selling offer");
	}

	public void placeSellingOffer(Property property, Agent offeror,
			BankAccount offerorsBankAcount, double pricePerUnit) {
		if (property != null && !Double.isNaN(pricePerUnit)) {
			MarketOrderFactory.newInstancePropertyMarketOrder(property,
					offeror, offerorsBankAcount, pricePerUnit);

			if (Log.isAgentSelectedByClient(offeror))
				Log.log(offeror, "offering 1 unit of "
						+ property.getClass().getSimpleName() + " for "
						+ Currency.round(pricePerUnit) + " "
						+ offerorsBankAcount.getCurrency().getIso4217Code()
						+ " per unit");
		} else
			throw new RuntimeException("error placing selling offer");
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

	/*
	 * get price
	 */
	public double getMarginalPrice(Currency denominatedInCurrency,
			GoodType goodType) {
		return DAOFactory.getMarketOrderDAO().findMarginalPrice(
				denominatedInCurrency, goodType);
	}

	public double getMarginalPrice(Currency denominatedInCurrency,
			Currency commodityCurrency) {
		return DAOFactory.getMarketOrderDAO().findMarginalPrice(
				denominatedInCurrency, commodityCurrency);
	}

	public double getMarginalPrice(Currency denominatedInCurrency,
			Class<? extends Property> propertyClass) {
		return DAOFactory.getMarketOrderDAO().findMarginalPrice(
				denominatedInCurrency, propertyClass);
	}

	public Map<GoodType, Double> getMarginalPrices(
			Currency denominatedInCurrency) {
		Map<GoodType, Double> prices = new HashMap<GoodType, Double>();
		for (GoodType goodType : GoodType.values())
			prices.put(goodType,
					getMarginalPrice(denominatedInCurrency, goodType));
		return prices;
	}

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

	protected SortedMap<MarketOrder, Double> findBestFulfillmentSet(
			final Currency denominatedInCurrency, double maxAmount,
			double maxTotalPrice, double maxPricePerUnit, boolean wholeNumber,
			final GoodType goodType, Currency commodityCurrency,
			final Class<? extends Property> propertyClass) {

		if (MathUtil.lesser(maxAmount, 0.0))
			throw new RuntimeException("maxAmount is " + maxAmount);
		if (MathUtil.lesser(maxTotalPrice, 0.0))
			throw new RuntimeException("maxTotalPrice is " + maxTotalPrice);
		if (MathUtil.lesser(maxPricePerUnit, 0.0))
			throw new RuntimeException("maxPricePerUnit is " + maxPricePerUnit);

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
			MarketOrder offer = iterator.next();

			// is maxPricePerUnit exceeded?
			if (restrictMaxPricePerUnit
					&& MathUtil.greater(offer.getPricePerUnit(),
							maxPricePerUnit))
				break;

			// is the amount correct?
			if (offer.getAmount() <= 0)
				throw new RuntimeException("amount of offer is "
						+ offer.getAmount());

			// is the currency correct?
			if (offer.getOfferorsBankAcount().getCurrency() != denominatedInCurrency) {
				throw new RuntimeException("wrong currency "
						+ denominatedInCurrency);
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

			// wholeNumberRestriction
			if (wholeNumber) {
				amountToTake = (long) amountToTake;
			}

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
}
