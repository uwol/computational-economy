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
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import compecon.culture.markets.MarketOffer;
import compecon.culture.markets.PropertyMarketOffer;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.property.Property;
import compecon.engine.Agent;
import compecon.engine.dao.DAOFactory.IPropertyMarketOfferDAO;

public class PropertyMarketOfferDAO extends InMemoryDAO<PropertyMarketOffer>
		implements IPropertyMarketOfferDAO {

	protected Map<Currency, Map<Class<? extends Property>, SortedSet<PropertyMarketOffer>>> sellingOffersForPropertyClasses = new HashMap<Currency, Map<Class<? extends Property>, SortedSet<PropertyMarketOffer>>>();

	protected Map<Agent, Set<PropertyMarketOffer>> indexAgentPropertyMarketOffers = new HashMap<Agent, Set<PropertyMarketOffer>>();

	/*
	 * helpers
	 */

	private void assertInitializedDataStructure(Currency currency) {
		if (!this.sellingOffersForPropertyClasses.containsKey(currency))
			this.sellingOffersForPropertyClasses
					.put(currency,
							new HashMap<Class<? extends Property>, SortedSet<PropertyMarketOffer>>());
	}

	private void assertInitializedDataStructure(Currency currency,
			Class<? extends Property> propertyClass) {
		assertInitializedDataStructure(currency);

		if (!this.sellingOffersForPropertyClasses.get(currency).containsKey(
				propertyClass))
			this.sellingOffersForPropertyClasses.get(currency).put(
					propertyClass, new TreeSet<PropertyMarketOffer>());
	}

	private void assertInitializedDataStructure(Currency currency,
			Property property) {
		assertInitializedDataStructure(currency, property.getClass());
	}

	private void assertInitializedDataStructure(Agent agent) {
		if (!this.indexAgentPropertyMarketOffers.containsKey(agent))
			this.indexAgentPropertyMarketOffers.put(agent,
					new HashSet<PropertyMarketOffer>());
	}

	/*
	 * get market offers for type
	 */

	private SortedSet<PropertyMarketOffer> getMarketOffers(Currency currency,
			Class<? extends Property> propertyClass) {
		this.assertInitializedDataStructure(currency, propertyClass);

		return this.sellingOffersForPropertyClasses.get(currency).get(
				propertyClass);
	}

	private SortedSet<PropertyMarketOffer> getMarketOffers(Currency currency,
			Property property) {
		return this.getMarketOffers(currency, property.getClass());
	}

	private Set<PropertyMarketOffer> getMarketOffers(Agent agent) {
		this.assertInitializedDataStructure(agent);

		return this.indexAgentPropertyMarketOffers.get(agent);
	}

	private SortedSet<PropertyMarketOffer> findMarketOffers(Agent agent,
			Currency currency, Class<? extends Property> propertyClass) {
		this.assertInitializedDataStructure(agent);

		SortedSet<PropertyMarketOffer> marketOffers = new TreeSet<PropertyMarketOffer>();
		if (this.indexAgentPropertyMarketOffers.containsKey(agent)) {
			for (PropertyMarketOffer marketOffer : this.indexAgentPropertyMarketOffers
					.get(agent))
				if (currency.equals(marketOffer.getCurrency())
						&& propertyClass.equals(marketOffer.getProperty()
								.getClass()))
					marketOffers.add(marketOffer);
		}
		return marketOffers;
	}

	/*
	 * actions
	 */

	@Override
	public synchronized void save(PropertyMarketOffer propertyMarketOffer) {
		this.getMarketOffers(propertyMarketOffer.getCurrency(),
				propertyMarketOffer.getProperty()).add(propertyMarketOffer);
		this.getMarketOffers(propertyMarketOffer.getOfferor()).add(
				propertyMarketOffer);

		super.save(propertyMarketOffer);
	}

	@Override
	public synchronized void delete(PropertyMarketOffer propertyMarketOffer) {
		this.getMarketOffers(propertyMarketOffer.getCurrency(),
				propertyMarketOffer.getProperty()).remove(propertyMarketOffer);
		this.getMarketOffers(propertyMarketOffer.getOfferor()).remove(
				propertyMarketOffer);

		super.delete(propertyMarketOffer);
	}

	@Override
	public void deleteAllSellingOffers(Agent offeror) {
		this.assertInitializedDataStructure(offeror);

		Set<PropertyMarketOffer> sellingOffersToRemove = this
				.getMarketOffers(offeror);
		for (PropertyMarketOffer propertyMarketOffer : sellingOffersToRemove)
			this.delete(propertyMarketOffer);
	}

	@Override
	public synchronized void deleteAllSellingOffers(Agent offeror,
			Currency currency, Class<? extends Property> propertyClass) {
		Set<PropertyMarketOffer> sellingOffersToRemove = this.findMarketOffers(
				offeror, currency, propertyClass);
		for (PropertyMarketOffer propertyMarketOffer : sellingOffersToRemove)
			this.delete(propertyMarketOffer);
	}

	@Override
	public synchronized double findMarginalPrice(Currency currency,
			Class<? extends Property> propertyClass) {
		for (MarketOffer marketOffer : getMarketOffers(currency, propertyClass))
			return marketOffer.getPricePerUnit();
		return Double.NaN;
	}

	@Override
	public synchronized Iterator<PropertyMarketOffer> getIterator(
			Class<? extends Property> propertyClass, Currency currency) {
		return this.getMarketOffers(currency, propertyClass).iterator();
	}
}
