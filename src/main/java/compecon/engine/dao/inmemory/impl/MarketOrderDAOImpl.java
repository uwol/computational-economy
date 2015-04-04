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

package compecon.engine.dao.inmemory.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import compecon.economy.markets.MarketOrder;
import compecon.economy.markets.MarketParticipant;
import compecon.economy.materia.GoodType;
import compecon.economy.property.Property;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.dao.MarketOrderDAO;

public class MarketOrderDAOImpl extends
		AbstractIndexedInMemoryDAOImpl<MarketParticipant, MarketOrder>
		implements MarketOrderDAO {

	protected Map<Currency, Map<Currency, SortedSet<MarketOrder>>> marketOrdersForCurrencies = new HashMap<Currency, Map<Currency, SortedSet<MarketOrder>>>();

	protected Map<Currency, Map<GoodType, SortedSet<MarketOrder>>> marketOrdersForGoodTypes = new HashMap<Currency, Map<GoodType, SortedSet<MarketOrder>>>();

	protected Map<Currency, Map<Class<? extends Property>, SortedSet<MarketOrder>>> marketOrdersForPropertyClasses = new HashMap<Currency, Map<Class<? extends Property>, SortedSet<MarketOrder>>>();

	/*
	 * helpers
	 */

	private void assureInitializedDataStructure(final Currency currency) {
		if (!marketOrdersForGoodTypes.containsKey(currency)) {
			marketOrdersForGoodTypes.put(currency,
					new HashMap<GoodType, SortedSet<MarketOrder>>());
		}

		if (!marketOrdersForCurrencies.containsKey(currency)) {
			marketOrdersForCurrencies.put(currency,
					new HashMap<Currency, SortedSet<MarketOrder>>());
		}

		if (!marketOrdersForPropertyClasses.containsKey(currency)) {
			marketOrdersForPropertyClasses
					.put(currency,
							new HashMap<Class<? extends Property>, SortedSet<MarketOrder>>());
		}
	}

	private void assureInitializedDataStructure(final Currency currency,
			final Class<? extends Property> propertyClass) {
		assureInitializedDataStructure(currency);

		final Map<Class<? extends Property>, SortedSet<MarketOrder>> marketOrdersForPropertyClass = marketOrdersForPropertyClasses
				.get(currency);
		if (!marketOrdersForPropertyClass.containsKey(propertyClass)) {
			marketOrdersForPropertyClass.put(propertyClass,
					new TreeSet<MarketOrder>());
		}
	}

	private void assureInitializedDataStructure(final Currency currency,
			final Currency commodityCurrency) {
		assureInitializedDataStructure(currency);

		final Map<Currency, SortedSet<MarketOrder>> marketOrdersForCurrency = marketOrdersForCurrencies
				.get(currency);
		if (!marketOrdersForCurrency.containsKey(commodityCurrency)) {
			marketOrdersForCurrency.put(commodityCurrency,
					new TreeSet<MarketOrder>());
		}
	}

	private void assureInitializedDataStructure(final Currency currency,
			final GoodType goodType) {
		assureInitializedDataStructure(currency);

		final Map<GoodType, SortedSet<MarketOrder>> marketOrdersForGoodTypesAndCurrency = marketOrdersForGoodTypes
				.get(currency);
		if (!marketOrdersForGoodTypesAndCurrency.containsKey(goodType)) {
			marketOrdersForGoodTypesAndCurrency.put(goodType,
					new TreeSet<MarketOrder>());
		}
	}

	/*
	 * get market offers for type
	 */

	@Override
	public synchronized void delete(final MarketOrder marketOrder) {
		if (marketOrder.getGoodType() != null) {
			final SortedSet<MarketOrder> marketOrders = this.getMarketOrders(
					marketOrder.getCurrency(), marketOrder.getGoodType());
			marketOrders.remove(marketOrder);
		}

		if (marketOrder.getCommodityCurrency() != null) {
			final SortedSet<MarketOrder> marketOrders = this.getMarketOrders(
					marketOrder.getCurrency(),
					marketOrder.getCommodityCurrency());
			marketOrders.remove(marketOrder);
		}

		if (marketOrder.getProperty() != null) {
			final Class<? extends Property> propertyIndexInterface = getIndexInterface(marketOrder
					.getProperty().getClass());
			final SortedSet<MarketOrder> marketOrders = this.getMarketOrders(
					marketOrder.getCurrency(), propertyIndexInterface);
			marketOrders.remove(marketOrder);
		}

		super.delete(marketOrder);
	}

	@Override
	public synchronized void deleteAllSellingOrders(
			final MarketParticipant offeror) {
		if (getInstancesForKey(offeror) != null) {
			for (final MarketOrder marketOrder : new HashSet<MarketOrder>(
					getInstancesForKey(offeror))) {
				delete(marketOrder);
			}
		}
	}

	@Override
	public synchronized void deleteAllSellingOrders(
			final MarketParticipant offeror, final Currency currency,
			final Class<? extends Property> propertyClass) {
		for (final MarketOrder marketOrder : this.findMarketOrders(offeror,
				currency, propertyClass)) {
			delete(marketOrder);
		}
	}

	@Override
	public synchronized void deleteAllSellingOrders(
			final MarketParticipant offeror, final Currency currency,
			final Currency commodityCurrency) {
		for (final MarketOrder marketOrder : this.findMarketOrders(offeror,
				currency, commodityCurrency)) {
			delete(marketOrder);
		}
	}

	@Override
	public synchronized void deleteAllSellingOrders(
			final MarketParticipant offeror, final Currency currency,
			final GoodType goodType) {
		for (final MarketOrder marketOrder : this.findMarketOrders(offeror,
				currency, goodType)) {
			delete(marketOrder);
		}
	}

	@Override
	public synchronized double findMarginalPrice(final Currency currency,
			final Class<? extends Property> propertyClass) {
		final Class<? extends Property> propertyIndexInterface = getIndexInterface(propertyClass);
		for (final MarketOrder marketOrder : getMarketOrders(currency,
				propertyIndexInterface)) {
			return marketOrder.getPricePerUnit();
		}
		return Double.NaN;
	}

	/*
	 * actions
	 */

	@Override
	public synchronized double findMarginalPrice(final Currency currency,
			final Currency commodityCurrency) {
		for (final MarketOrder marketOrder : this.getMarketOrders(currency,
				commodityCurrency)) {
			return marketOrder.getPricePerUnit();
		}
		return Double.NaN;
	}

	@Override
	public synchronized double findMarginalPrice(final Currency currency,
			final GoodType goodType) {
		for (final MarketOrder marketOrder : this.getMarketOrders(currency,
				goodType)) {
			return marketOrder.getPricePerUnit();
		}
		return Double.NaN;
	}

	private SortedSet<MarketOrder> findMarketOrders(
			final MarketParticipant offeror, final Currency currency,
			final Class<? extends Property> propertyClass) {
		final SortedSet<MarketOrder> marketOrders = new TreeSet<MarketOrder>();
		final List<MarketOrder> marketOrdersForOfferor = getInstancesForKey(offeror);
		if (marketOrdersForOfferor != null) {
			for (final MarketOrder marketOrder : marketOrdersForOfferor) {
				if (currency.equals(marketOrder.getCurrency())
						&& marketOrder.getProperty() != null
						&& propertyClass.equals(marketOrder.getProperty()
								.getClass())) {
					marketOrders.add(marketOrder);
				}
			}
		}
		return marketOrders;
	}

	private SortedSet<MarketOrder> findMarketOrders(
			final MarketParticipant offeror, final Currency currency,
			final Currency commodityCurrency) {
		final SortedSet<MarketOrder> marketOrders = new TreeSet<MarketOrder>();
		final List<MarketOrder> marketOrdersForOfferor = getInstancesForKey(offeror);
		if (marketOrdersForOfferor != null) {
			for (final MarketOrder marketOrder : marketOrdersForOfferor) {
				if (currency.equals(marketOrder.getCurrency())
						&& commodityCurrency.equals(marketOrder
								.getCommodityCurrency())) {
					marketOrders.add(marketOrder);
				}
			}
		}
		return marketOrders;
	}

	private SortedSet<MarketOrder> findMarketOrders(
			final MarketParticipant offeror, final Currency currency,
			final GoodType goodType) {
		final SortedSet<MarketOrder> marketOrders = new TreeSet<MarketOrder>();
		final List<MarketOrder> marketOrdersForOfferor = getInstancesForKey(offeror);
		if (marketOrdersForOfferor != null) {
			for (final MarketOrder marketOrder : marketOrdersForOfferor) {
				if (currency.equals(marketOrder.getCurrency())
						&& goodType.equals(marketOrder.getGoodType())) {
					marketOrders.add(marketOrder);
				}
			}
		}
		return marketOrders;
	}

	@Override
	public synchronized double getAmountSum(final Currency currency,
			final Currency commodityCurrency) {
		final Iterator<MarketOrder> iterator = this.getIterator(currency,
				commodityCurrency);
		double totalAmountSum = 0.0;
		while (iterator.hasNext()) {
			totalAmountSum += iterator.next().getAmount();
		}
		return totalAmountSum;
	}

	@Override
	public synchronized double getAmountSum(final Currency currency,
			final GoodType goodType) {
		final Iterator<MarketOrder> iterator = this.getIterator(currency,
				goodType);
		double totalAmountSum = 0.0;
		while (iterator.hasNext()) {
			totalAmountSum += iterator.next().getAmount();
		}
		return totalAmountSum;
	}

	@SuppressWarnings("unchecked")
	protected Class<? extends Property> getIndexInterface(
			final Class<? extends Property> propertyClass) {
		/*
		 * the property object should be stored in the DAO with the first
		 * interface as the key; e. g. a property object of class ShareImpl
		 * should be stored in the list indexed by interface Share
		 */

		// if the propertyClass is already an interface
		if (propertyClass.isInterface()) {
			return propertyClass;
		} else {
			// determine primary interface of class
			final Class<?>[] interfacesOfPropertyClass = propertyClass
					.getInterfaces();

			// as the property implements at least interface Property,
			// interfacesOfPropertyClass.length > 0
			assert (interfacesOfPropertyClass.length > 0);

			return (Class<? extends Property>) interfacesOfPropertyClass[0];
		}
	}

	@Override
	public synchronized Iterator<MarketOrder> getIterator(
			final Currency currency,
			final Class<? extends Property> propertyClass) {
		final Class<? extends Property> propertyIndexInterface = getIndexInterface(propertyClass);
		return this.getMarketOrders(currency, propertyIndexInterface)
				.iterator();
	}

	@Override
	public synchronized Iterator<MarketOrder> getIterator(
			final Currency currency, final Currency commodityCurrency) {
		return this.getMarketOrders(currency, commodityCurrency).iterator();
	}

	@Override
	public synchronized Iterator<MarketOrder> getIterator(
			final Currency currency, final GoodType goodType) {
		return this.getMarketOrders(currency, goodType).iterator();
	}

	@Override
	public synchronized Iterator<MarketOrder> getIteratorThreadsafe(
			final Currency currency, final Currency commodityCurrency) {
		return new TreeSet<MarketOrder>(this.getMarketOrders(currency,
				commodityCurrency)).iterator();
	}

	@Override
	public synchronized Iterator<MarketOrder> getIteratorThreadsafe(
			final Currency currency, final GoodType goodType) {
		return new TreeSet<MarketOrder>(
				this.getMarketOrders(currency, goodType)).iterator();
	}

	private SortedSet<MarketOrder> getMarketOrders(final Currency currency,
			final Class<? extends Property> propertyIndexInterface) {
		this.assureInitializedDataStructure(currency, propertyIndexInterface);

		return marketOrdersForPropertyClasses.get(currency).get(
				propertyIndexInterface);
	}

	private SortedSet<MarketOrder> getMarketOrders(final Currency currency,
			final Currency commodityCurrency) {
		this.assureInitializedDataStructure(currency, commodityCurrency);

		return marketOrdersForCurrencies.get(currency).get(commodityCurrency);
	}

	private SortedSet<MarketOrder> getMarketOrders(final Currency currency,
			final GoodType goodType) {
		this.assureInitializedDataStructure(currency, goodType);

		return marketOrdersForGoodTypes.get(currency).get(goodType);
	}

	@Override
	public synchronized void save(final MarketOrder marketOrder) {
		if (marketOrder.getGoodType() != null) {
			this.getMarketOrders(marketOrder.getCurrency(),
					marketOrder.getGoodType()).add(marketOrder);
		}

		if (marketOrder.getCommodityCurrency() != null) {
			this.getMarketOrders(marketOrder.getCurrency(),
					marketOrder.getCommodityCurrency()).add(marketOrder);
		}

		if (marketOrder.getProperty() != null) {
			final Class<? extends Property> propertyIndexInterface = getIndexInterface(marketOrder
					.getProperty().getClass());
			this.getMarketOrders(marketOrder.getCurrency(),
					propertyIndexInterface).add(marketOrder);
		}

		super.save(marketOrder.getOfferor(), marketOrder);
	}
}
