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

	protected Map<Currency, Map<GoodType, SortedSet<MarketOrder>>> marketOrdersForGoodTypes = new HashMap<Currency, Map<GoodType, SortedSet<MarketOrder>>>();

	protected Map<Currency, Map<Currency, SortedSet<MarketOrder>>> marketOrdersForCurrencies = new HashMap<Currency, Map<Currency, SortedSet<MarketOrder>>>();

	protected Map<Currency, Map<Class<? extends Property>, SortedSet<MarketOrder>>> marketOrdersForPropertyClasses = new HashMap<Currency, Map<Class<? extends Property>, SortedSet<MarketOrder>>>();

	/*
	 * helpers
	 */

	private void assureInitializedDataStructure(Currency currency) {
		if (!this.marketOrdersForGoodTypes.containsKey(currency)) {
			this.marketOrdersForGoodTypes.put(currency,
					new HashMap<GoodType, SortedSet<MarketOrder>>());
		}

		if (!this.marketOrdersForCurrencies.containsKey(currency)) {
			this.marketOrdersForCurrencies.put(currency,
					new HashMap<Currency, SortedSet<MarketOrder>>());
		}

		if (!this.marketOrdersForPropertyClasses.containsKey(currency)) {
			this.marketOrdersForPropertyClasses
					.put(currency,
							new HashMap<Class<? extends Property>, SortedSet<MarketOrder>>());
		}
	}

	private void assureInitializedDataStructure(Currency currency,
			GoodType goodType) {
		assureInitializedDataStructure(currency);

		if (!this.marketOrdersForGoodTypes.get(currency).containsKey(goodType)) {
			this.marketOrdersForGoodTypes.get(currency).put(goodType,
					new TreeSet<MarketOrder>());
		}
	}

	private void assureInitializedDataStructure(Currency currency,
			Currency commodityCurrency) {
		assureInitializedDataStructure(currency);

		if (!this.marketOrdersForCurrencies.get(currency).containsKey(
				commodityCurrency)) {
			this.marketOrdersForCurrencies.get(currency).put(commodityCurrency,
					new TreeSet<MarketOrder>());
		}
	}

	private void assureInitializedDataStructure(Currency currency,
			Class<? extends Property> propertyClass) {
		assureInitializedDataStructure(currency);

		if (!this.marketOrdersForPropertyClasses.get(currency).containsKey(
				propertyClass)) {
			this.marketOrdersForPropertyClasses.get(currency).put(
					propertyClass, new TreeSet<MarketOrder>());
		}
	}

	/*
	 * get market offers for type
	 */

	private SortedSet<MarketOrder> getMarketOrders(Currency currency,
			GoodType goodType) {
		this.assureInitializedDataStructure(currency, goodType);

		return this.marketOrdersForGoodTypes.get(currency).get(goodType);
	}

	private SortedSet<MarketOrder> getMarketOrders(Currency currency,
			Currency commodityCurrency) {
		this.assureInitializedDataStructure(currency, commodityCurrency);

		return this.marketOrdersForCurrencies.get(currency).get(
				commodityCurrency);
	}

	private SortedSet<MarketOrder> getMarketOrders(Currency currency,
			Class<? extends Property> propertyIndexInterface) {
		this.assureInitializedDataStructure(currency, propertyIndexInterface);

		return this.marketOrdersForPropertyClasses.get(currency).get(
				propertyIndexInterface);
	}

	private SortedSet<MarketOrder> findMarketOrders(MarketParticipant offeror,
			Currency currency, GoodType goodType) {
		final SortedSet<MarketOrder> marketOrders = new TreeSet<MarketOrder>();
		final List<MarketOrder> marketOrdersForOfferor = this
				.getInstancesForKey(offeror);
		if (marketOrdersForOfferor != null) {
			for (MarketOrder marketOrder : marketOrdersForOfferor) {
				if (currency.equals(marketOrder.getCurrency())
						&& goodType.equals(marketOrder.getGoodType())) {
					marketOrders.add(marketOrder);
				}
			}
		}
		return marketOrders;
	}

	private SortedSet<MarketOrder> findMarketOrders(MarketParticipant offeror,
			Currency currency, Currency commodityCurrency) {
		final SortedSet<MarketOrder> marketOrders = new TreeSet<MarketOrder>();
		final List<MarketOrder> marketOrdersForOfferor = this
				.getInstancesForKey(offeror);
		if (marketOrdersForOfferor != null) {
			for (MarketOrder marketOrder : marketOrdersForOfferor) {
				if (currency.equals(marketOrder.getCurrency())
						&& commodityCurrency.equals(marketOrder
								.getCommodityCurrency())) {
					marketOrders.add(marketOrder);
				}
			}
		}
		return marketOrders;
	}

	private SortedSet<MarketOrder> findMarketOrders(MarketParticipant offeror,
			Currency currency, Class<? extends Property> propertyClass) {
		final SortedSet<MarketOrder> marketOrders = new TreeSet<MarketOrder>();
		final List<MarketOrder> marketOrdersForOfferor = this
				.getInstancesForKey(offeror);
		if (marketOrdersForOfferor != null) {
			for (MarketOrder marketOrder : marketOrdersForOfferor) {
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

	/*
	 * actions
	 */

	@Override
	public synchronized void save(MarketOrder marketOrder) {
		if (marketOrder.getGoodType() != null) {
			this.getMarketOrders(marketOrder.getCurrency(),
					marketOrder.getGoodType()).add(marketOrder);
		}

		if (marketOrder.getCommodityCurrency() != null) {
			this.getMarketOrders(marketOrder.getCurrency(),
					marketOrder.getCommodityCurrency()).add(marketOrder);
		}

		if (marketOrder.getProperty() != null) {
			final Class<? extends Property> propertyIndexInterface = this
					.getIndexInterface(marketOrder.getProperty().getClass());
			this.getMarketOrders(marketOrder.getCurrency(),
					propertyIndexInterface).add(marketOrder);
		}

		super.save(marketOrder.getOfferor(), marketOrder);
	}

	@Override
	public synchronized void delete(MarketOrder marketOrder) {
		if (marketOrder.getGoodType() != null) {
			this.getMarketOrders(marketOrder.getCurrency(),
					marketOrder.getGoodType()).remove(marketOrder);
		}

		if (marketOrder.getCommodityCurrency() != null) {
			this.getMarketOrders(marketOrder.getCurrency(),
					marketOrder.getCommodityCurrency()).remove(marketOrder);
		}

		if (marketOrder.getProperty() != null) {
			final Class<? extends Property> propertyIndexInterface = this
					.getIndexInterface(marketOrder.getProperty().getClass());
			this.getMarketOrders(marketOrder.getCurrency(),
					propertyIndexInterface).remove(marketOrder);
		}

		super.delete(marketOrder);
	}

	@Override
	public synchronized void deleteAllSellingOrders(MarketParticipant offeror) {
		if (this.getInstancesForKey(offeror) != null) {
			for (MarketOrder marketOrder : new HashSet<MarketOrder>(
					this.getInstancesForKey(offeror))) {
				this.delete(marketOrder);
			}
		}
	}

	@Override
	public synchronized void deleteAllSellingOrders(MarketParticipant offeror,
			Currency currency, GoodType goodType) {
		for (MarketOrder marketOrder : this.findMarketOrders(offeror, currency,
				goodType)) {
			this.delete(marketOrder);
		}
	}

	@Override
	public synchronized void deleteAllSellingOrders(MarketParticipant offeror,
			Currency currency, Currency commodityCurrency) {
		for (MarketOrder marketOrder : this.findMarketOrders(offeror, currency,
				commodityCurrency)) {
			this.delete(marketOrder);
		}
	}

	@Override
	public synchronized void deleteAllSellingOrders(MarketParticipant offeror,
			Currency currency, Class<? extends Property> propertyClass) {
		for (MarketOrder marketOrder : this.findMarketOrders(offeror, currency,
				propertyClass)) {
			this.delete(marketOrder);
		}
	}

	@Override
	public synchronized double findMarginalPrice(Currency currency,
			GoodType goodType) {
		for (MarketOrder marketOrder : this.getMarketOrders(currency, goodType)) {
			return marketOrder.getPricePerUnit();
		}
		return Double.NaN;
	}

	@Override
	public synchronized double findMarginalPrice(Currency currency,
			Currency commodityCurrency) {
		for (MarketOrder marketOrder : this.getMarketOrders(currency,
				commodityCurrency)) {
			return marketOrder.getPricePerUnit();
		}
		return Double.NaN;
	}

	@Override
	public synchronized double findMarginalPrice(Currency currency,
			Class<? extends Property> propertyClass) {
		final Class<? extends Property> propertyIndexInterface = this
				.getIndexInterface(propertyClass);
		for (MarketOrder marketOrder : getMarketOrders(currency,
				propertyIndexInterface))
			return marketOrder.getPricePerUnit();
		return Double.NaN;
	}

	@Override
	public synchronized Iterator<MarketOrder> getIterator(Currency currency,
			GoodType goodType) {
		return this.getMarketOrders(currency, goodType).iterator();
	}

	@Override
	public synchronized Iterator<MarketOrder> getIterator(Currency currency,
			Currency commodityCurrency) {
		return this.getMarketOrders(currency, commodityCurrency).iterator();
	}

	@Override
	public synchronized Iterator<MarketOrder> getIterator(Currency currency,
			Class<? extends Property> propertyClass) {
		final Class<? extends Property> propertyIndexInterface = this
				.getIndexInterface(propertyClass);
		return this.getMarketOrders(currency, propertyIndexInterface)
				.iterator();
	}

	@Override
	public synchronized Iterator<MarketOrder> getIteratorThreadsafe(
			Currency currency, GoodType goodType) {
		return new TreeSet<MarketOrder>(
				this.getMarketOrders(currency, goodType)).iterator();
	}

	@Override
	public synchronized Iterator<MarketOrder> getIteratorThreadsafe(
			Currency currency, Currency commodityCurrency) {
		return new TreeSet<MarketOrder>(this.getMarketOrders(currency,
				commodityCurrency)).iterator();
	}

	@Override
	public synchronized double getAmountSum(Currency currency, GoodType goodType) {
		final Iterator<MarketOrder> iterator = this.getIterator(currency,
				goodType);
		double totalAmountSum = 0.0;
		while (iterator.hasNext()) {
			totalAmountSum += iterator.next().getAmount();
		}
		return totalAmountSum;
	}

	@Override
	public synchronized double getAmountSum(Currency currency,
			Currency commodityCurrency) {
		final Iterator<MarketOrder> iterator = this.getIterator(currency,
				commodityCurrency);
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
}
