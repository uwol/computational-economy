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

package compecon.engine.dao.inmemory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import compecon.culture.markets.ordertypes.MarketOrder;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.property.Property;
import compecon.engine.Agent;
import compecon.engine.dao.DAOFactory.IMarketOrderDAO;
import compecon.nature.materia.GoodType;

public class MarketOrderDAO extends AgentIndexedInMemoryDAO<MarketOrder>
		implements IMarketOrderDAO {

	protected Map<Currency, Map<GoodType, SortedSet<MarketOrder>>> marketOrdersForGoodTypes = new HashMap<Currency, Map<GoodType, SortedSet<MarketOrder>>>();

	protected Map<Currency, Map<Class<? extends Property>, SortedSet<MarketOrder>>> marketOrdersForPropertyClasses = new HashMap<Currency, Map<Class<? extends Property>, SortedSet<MarketOrder>>>();

	/*
	 * helpers
	 */

	private void assertInitializedDataStructure(Currency currency) {
		if (!this.marketOrdersForGoodTypes.containsKey(currency))
			this.marketOrdersForGoodTypes.put(currency,
					new HashMap<GoodType, SortedSet<MarketOrder>>());

		if (!this.marketOrdersForPropertyClasses.containsKey(currency))
			this.marketOrdersForPropertyClasses
					.put(currency,
							new HashMap<Class<? extends Property>, SortedSet<MarketOrder>>());

	}

	private void assertInitializedDataStructure(Currency currency,
			GoodType goodType) {
		assertInitializedDataStructure(currency);

		if (!this.marketOrdersForGoodTypes.get(currency).containsKey(goodType))
			this.marketOrdersForGoodTypes.get(currency).put(goodType,
					new TreeSet<MarketOrder>());
	}

	private void assertInitializedDataStructure(Currency currency,
			Class<? extends Property> propertyClass) {
		assertInitializedDataStructure(currency);

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
		this.assertInitializedDataStructure(currency, goodType);

		return this.marketOrdersForGoodTypes.get(currency).get(goodType);
	}

	private SortedSet<MarketOrder> getMarketOrders(Currency currency,
			Class<? extends Property> propertyClass) {
		this.assertInitializedDataStructure(currency, propertyClass);

		return this.marketOrdersForPropertyClasses.get(currency).get(
				propertyClass);
	}

	private SortedSet<MarketOrder> findMarketOrders(Agent agent,
			Currency currency, GoodType goodType) {
		SortedSet<MarketOrder> marketOrders = new TreeSet<MarketOrder>();
		if (this.getInstancesForAgent(agent) != null) {
			for (MarketOrder marketOrder : this.getInstancesForAgent(agent))
				if (currency.equals(marketOrder.getCurrency())
						&& goodType.equals(marketOrder.getGoodType()))
					marketOrders.add(marketOrder);
		}
		return marketOrders;
	}

	private SortedSet<MarketOrder> findMarketOrders(Agent agent,
			Currency currency, Class<? extends Property> propertyClass) {
		SortedSet<MarketOrder> marketOrders = new TreeSet<MarketOrder>();
		if (this.getInstancesForAgent(agent) != null) {
			for (MarketOrder marketOrder : this.getInstancesForAgent(agent))
				if (currency.equals(marketOrder.getCurrency())
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
			this.getMarketOrders(marketOrder.getCurrency(),
					marketOrder.getGoodType()).add(marketOrder);
		}

		if (marketOrder.getProperty() != null) {
			this.getMarketOrders(marketOrder.getCurrency(),
					marketOrder.getProperty().getClass()).add(marketOrder);
		}

		super.save(marketOrder.getOfferor(), marketOrder);
	}

	@Override
	public synchronized void delete(MarketOrder marketOrder) {
		if (marketOrder.getGoodType() != null) {
			this.getMarketOrders(marketOrder.getCurrency(),
					marketOrder.getGoodType()).remove(marketOrder);
		}

		if (marketOrder.getProperty() != null) {
			this.getMarketOrders(marketOrder.getCurrency(),
					marketOrder.getProperty().getClass()).remove(marketOrder);
		}

		super.delete(marketOrder.getOfferor(), marketOrder);
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
			Currency currency, Class<? extends Property> propertyClass) {
		for (MarketOrder marketOrder : this.findMarketOrders(offeror, currency,
				propertyClass))
			this.delete(marketOrder);
	}

	@Override
	public synchronized double findMarginalPrice(Currency currency,
			GoodType goodType) {
		for (MarketOrder marketOrder : this.getMarketOrders(currency, goodType))
			return marketOrder.getPricePerUnit();
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
	public synchronized Iterator<MarketOrder> getIterator(GoodType goodType,
			Currency currency) {
		return this.getMarketOrders(currency, goodType).iterator();
	}

	@Override
	public synchronized Iterator<MarketOrder> getIterator(
			Class<? extends Property> propertyClass, Currency currency) {
		return this.getMarketOrders(currency, propertyClass).iterator();
	}
}
