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

package compecon.engine.dao.inmemory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import compecon.economy.markets.ordertypes.MarketOrder;
import compecon.economy.sectors.Agent;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.law.property.Property;
import compecon.engine.dao.DAOFactory;
import compecon.engine.dao.DAOFactory.IMarketOrderDAO;
import compecon.materia.GoodType;

public class MarketOrderDAO extends AgentIndexedInMemoryDAO<MarketOrder>
		implements IMarketOrderDAO {

	protected Map<Currency, Map<GoodType, SortedSet<MarketOrder>>> marketOrdersForGoodTypes = new HashMap<Currency, Map<GoodType, SortedSet<MarketOrder>>>();

	protected Map<Currency, Map<Currency, SortedSet<MarketOrder>>> marketOrdersForCurrencies = new HashMap<Currency, Map<Currency, SortedSet<MarketOrder>>>();

	protected Map<Currency, Map<Class<? extends Property>, SortedSet<MarketOrder>>> marketOrdersForPropertyClasses = new HashMap<Currency, Map<Class<? extends Property>, SortedSet<MarketOrder>>>();

	/*
	 * helpers
	 */

	private void assureInitializedDataStructure(Currency currency) {
		if (!this.marketOrdersForGoodTypes.containsKey(currency))
			this.marketOrdersForGoodTypes.put(currency,
					new HashMap<GoodType, SortedSet<MarketOrder>>());

		if (!this.marketOrdersForCurrencies.containsKey(currency))
			this.marketOrdersForCurrencies.put(currency,
					new HashMap<Currency, SortedSet<MarketOrder>>());

		if (!this.marketOrdersForPropertyClasses.containsKey(currency))
			this.marketOrdersForPropertyClasses
					.put(currency,
							new HashMap<Class<? extends Property>, SortedSet<MarketOrder>>());

	}

	private void assureInitializedDataStructure(Currency currency,
			GoodType goodType) {
		assureInitializedDataStructure(currency);

		if (!this.marketOrdersForGoodTypes.get(currency).containsKey(goodType))
			this.marketOrdersForGoodTypes.get(currency).put(goodType,
					new TreeSet<MarketOrder>());
	}

	private void assureInitializedDataStructure(Currency currency,
			Currency commodityCurrency) {
		assureInitializedDataStructure(currency);

		if (!this.marketOrdersForCurrencies.get(currency).containsKey(
				commodityCurrency))
			this.marketOrdersForCurrencies.get(currency).put(commodityCurrency,
					new TreeSet<MarketOrder>());
	}

	private void assureInitializedDataStructure(Currency currency,
			Class<? extends Property> propertyClass) {
		assureInitializedDataStructure(currency);

		if (!this.marketOrdersForPropertyClasses.get(currency).containsKey(
				propertyClass))
			this.marketOrdersForPropertyClasses.get(currency).put(
					propertyClass, new TreeSet<MarketOrder>());
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
			Class<? extends Property> propertyClass) {
		this.assureInitializedDataStructure(currency, propertyClass);

		return this.marketOrdersForPropertyClasses.get(currency).get(
				propertyClass);
	}

	private SortedSet<MarketOrder> findMarketOrders(Agent agent,
			Currency currency, GoodType goodType) {
		SortedSet<MarketOrder> marketOrders = new TreeSet<MarketOrder>();
		if (this.getInstancesForAgent(agent) != null) {
			for (MarketOrder marketOrder : this.getInstancesForAgent(agent))
				if (currency.equals(marketOrder.getOfferorsBankAcount()
						.getCurrency())
						&& goodType.equals(marketOrder.getGoodType()))
					marketOrders.add(marketOrder);
		}
		return marketOrders;
	}

	private SortedSet<MarketOrder> findMarketOrders(Agent agent,
			Currency currency, Currency commodityCurrency) {
		SortedSet<MarketOrder> marketOrders = new TreeSet<MarketOrder>();
		if (this.getInstancesForAgent(agent) != null) {
			for (MarketOrder marketOrder : this.getInstancesForAgent(agent))
				if (currency.equals(marketOrder.getOfferorsBankAcount()
						.getCurrency())
						&& commodityCurrency.equals(marketOrder
								.getCommodityCurrency()))
					marketOrders.add(marketOrder);
		}
		return marketOrders;
	}

	private SortedSet<MarketOrder> findMarketOrders(Agent agent,
			Currency currency, Class<? extends Property> propertyClass) {
		SortedSet<MarketOrder> marketOrders = new TreeSet<MarketOrder>();
		if (this.getInstancesForAgent(agent) != null) {
			for (MarketOrder marketOrder : this.getInstancesForAgent(agent))
				if (currency.equals(marketOrder.getOfferorsBankAcount()
						.getCurrency())
						&& marketOrder.getProperty() != null
						&& propertyClass.equals(marketOrder.getProperty()
								.getClass()))
					marketOrders.add(marketOrder);
		}
		return marketOrders;
	}

	/*
	 * actions
	 */

	@Override
	public synchronized void save(MarketOrder marketOrder) {
		if (marketOrder.getGoodType() != null) {
			this.getMarketOrders(
					marketOrder.getOfferorsBankAcount().getCurrency(),
					marketOrder.getGoodType()).add(marketOrder);
		}

		if (marketOrder.getCommodityCurrency() != null) {
			this.getMarketOrders(
					marketOrder.getOfferorsBankAcount().getCurrency(),
					marketOrder.getCommodityCurrency()).add(marketOrder);
		}

		if (marketOrder.getProperty() != null) {
			this.getMarketOrders(
					marketOrder.getOfferorsBankAcount().getCurrency(),
					marketOrder.getProperty().getClass()).add(marketOrder);
		}

		super.save(marketOrder.getOfferor(), marketOrder);
	}

	@Override
	public synchronized void delete(MarketOrder marketOrder) {
		if (marketOrder.getGoodType() != null) {
			this.getMarketOrders(
					marketOrder.getOfferorsBankAcount().getCurrency(),
					marketOrder.getGoodType()).remove(marketOrder);
		}

		if (marketOrder.getCommodityCurrency() != null) {
			this.getMarketOrders(
					marketOrder.getOfferorsBankAcount().getCurrency(),
					marketOrder.getCommodityCurrency()).remove(marketOrder);
		}

		if (marketOrder.getProperty() != null) {
			this.getMarketOrders(
					marketOrder.getOfferorsBankAcount().getCurrency(),
					marketOrder.getProperty().getClass()).remove(marketOrder);
		}

		super.delete(marketOrder);
	}

	@Override
	public synchronized void deleteAllSellingOrders(Agent offeror) {
		if (this.getInstancesForAgent(offeror) != null)
			for (MarketOrder marketOrder : new HashSet<MarketOrder>(
					this.getInstancesForAgent(offeror)))
				this.delete(marketOrder);
	}

	@Override
	public synchronized void deleteAllSellingOrders(Agent offeror,
			Currency currency, GoodType goodType) {
		for (MarketOrder marketOrder : this.findMarketOrders(offeror, currency,
				goodType))
			this.delete(marketOrder);
	}

	@Override
	public synchronized void deleteAllSellingOrders(Agent offeror,
			Currency currency, Currency commodityCurrency) {
		for (MarketOrder marketOrder : this.findMarketOrders(offeror, currency,
				commodityCurrency))
			this.delete(marketOrder);
	}

	@Override
	public synchronized void deleteAllSellingOrders(Agent offeror,
			Currency currency, Class<? extends Property> propertyClass) {
		for (MarketOrder marketOrder : this.findMarketOrders(offeror, currency,
				propertyClass))
			this.delete(marketOrder);
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
		for (MarketOrder marketOrder : getMarketOrders(currency, propertyClass))
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
		return this.getMarketOrders(currency, propertyClass).iterator();
	}

	@Override
	public synchronized double getAmountSum(Currency currency, GoodType goodType) {
		Iterator<MarketOrder> iterator = DAOFactory.getMarketOrderDAO()
				.getIterator(currency, goodType);
		double totalAmountSum = 0.0;
		while (iterator.hasNext()) {
			totalAmountSum += iterator.next().getAmount();
		}
		return totalAmountSum;
	}

	@Override
	public synchronized double getAmountSum(Currency currency,
			Currency commodityCurrency) {
		Iterator<MarketOrder> iterator = DAOFactory.getMarketOrderDAO()
				.getIterator(currency, commodityCurrency);
		double totalAmountSum = 0.0;
		while (iterator.hasNext()) {
			totalAmountSum += iterator.next().getAmount();
		}
		return totalAmountSum;
	}
}
