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

import compecon.culture.markets.GoodTypeMarketOffer;
import compecon.culture.markets.MarketOffer;
import compecon.culture.sectors.financial.Currency;
import compecon.engine.Agent;
import compecon.engine.dao.DAOFactory.IGoodTypeMarketOfferDAO;
import compecon.nature.materia.GoodType;

public class GoodTypeMarketOfferDAO extends InMemoryDAO<GoodTypeMarketOffer>
		implements IGoodTypeMarketOfferDAO {

	protected Map<Currency, Map<GoodType, SortedSet<GoodTypeMarketOffer>>> sellingOffersForGoodTypes = new HashMap<Currency, Map<GoodType, SortedSet<GoodTypeMarketOffer>>>();

	protected Map<Agent, Set<GoodTypeMarketOffer>> indexAgentGoodTypeMarketOffers = new HashMap<Agent, Set<GoodTypeMarketOffer>>();

	/*
	 * helpers
	 */

	private void assertInitializedDataStructure(Currency currency) {
		if (!this.sellingOffersForGoodTypes.containsKey(currency))
			this.sellingOffersForGoodTypes.put(currency,
					new HashMap<GoodType, SortedSet<GoodTypeMarketOffer>>());
	}

	private void assertInitializedDataStructure(Currency currency,
			GoodType goodType) {
		assertInitializedDataStructure(currency);

		if (!this.sellingOffersForGoodTypes.get(currency).containsKey(goodType))
			this.sellingOffersForGoodTypes.get(currency).put(goodType,
					new TreeSet<GoodTypeMarketOffer>());
	}

	private void assertInitializedDataStructure(Agent agent) {
		if (!this.indexAgentGoodTypeMarketOffers.containsKey(agent))
			this.indexAgentGoodTypeMarketOffers.put(agent,
					new HashSet<GoodTypeMarketOffer>());
	}

	/*
	 * get market offers for type
	 */

	private SortedSet<GoodTypeMarketOffer> getMarketOffers(Currency currency,
			GoodType goodType) {
		this.assertInitializedDataStructure(currency, goodType);

		return this.sellingOffersForGoodTypes.get(currency).get(goodType);
	}

	private Set<GoodTypeMarketOffer> getMarketOffers(Agent agent) {
		this.assertInitializedDataStructure(agent);

		return this.indexAgentGoodTypeMarketOffers.get(agent);
	}

	private SortedSet<GoodTypeMarketOffer> findMarketOffers(Agent agent,
			Currency currency, GoodType goodType) {
		this.assertInitializedDataStructure(agent);

		SortedSet<GoodTypeMarketOffer> marketOffers = new TreeSet<GoodTypeMarketOffer>();
		if (this.indexAgentGoodTypeMarketOffers.containsKey(agent)) {
			for (GoodTypeMarketOffer marketOffer : this.indexAgentGoodTypeMarketOffers
					.get(agent))
				if (currency.equals(marketOffer.getCurrency())
						&& goodType.equals(marketOffer.getGoodType()))
					marketOffers.add(marketOffer);
		}
		return marketOffers;
	}

	/*
	 * actions
	 */

	@Override
	public synchronized void save(GoodTypeMarketOffer goodTypeMarketOffer) {
		this.getMarketOffers(goodTypeMarketOffer.getCurrency(),
				goodTypeMarketOffer.getGoodType()).add(goodTypeMarketOffer);
		this.getMarketOffers(goodTypeMarketOffer.getOfferor()).add(
				goodTypeMarketOffer);

		super.save(goodTypeMarketOffer);
	}

	@Override
	public synchronized void delete(GoodTypeMarketOffer goodTypeMarketOffer) {
		this.getMarketOffers(goodTypeMarketOffer.getCurrency(),
				goodTypeMarketOffer.getGoodType()).remove(goodTypeMarketOffer);
		this.getMarketOffers(goodTypeMarketOffer.getOfferor()).remove(
				goodTypeMarketOffer);

		super.delete(goodTypeMarketOffer);
	}

	@Override
	public synchronized void deleteAllSellingOffers(Agent offeror) {
		this.assertInitializedDataStructure(offeror);

		Set<GoodTypeMarketOffer> sellingOffersToRemove = this
				.getMarketOffers(offeror);
		for (GoodTypeMarketOffer goodTypeMarketOffer : sellingOffersToRemove)
			this.delete(goodTypeMarketOffer);
	}

	@Override
	public synchronized void deleteAllSellingOffers(Agent offeror,
			Currency currency, GoodType goodType) {
		Set<GoodTypeMarketOffer> sellingOffersToRemove = this.findMarketOffers(
				offeror, currency, goodType);
		for (GoodTypeMarketOffer goodTypeMarketOffer : sellingOffersToRemove)
			this.delete(goodTypeMarketOffer);
	}

	@Override
	public synchronized double findMarginalPrice(Currency currency,
			GoodType goodType) {
		for (MarketOffer marketOffer : this.getMarketOffers(currency, goodType))
			return marketOffer.getPricePerUnit();
		return Double.NaN;
	}

	@Override
	public synchronized Iterator<GoodTypeMarketOffer> getIterator(
			GoodType goodType, Currency currency) {
		return this.getMarketOffers(currency, goodType).iterator();
	}
}
