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
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import compecon.culture.markets.GoodTypeMarketOffer;
import compecon.culture.markets.MarketOffer;
import compecon.culture.sectors.financial.Currency;
import compecon.engine.Agent;
import compecon.engine.dao.DAOFactory.IGoodTypeMarketOfferDAO;
import compecon.nature.materia.GoodType;

public class GoodTypeMarketOfferDAO extends
		AgentIndexedInMemoryDAO<GoodTypeMarketOffer> implements
		IGoodTypeMarketOfferDAO {

	protected Map<Currency, Map<GoodType, SortedSet<GoodTypeMarketOffer>>> sellingOffersForGoodTypes = new HashMap<Currency, Map<GoodType, SortedSet<GoodTypeMarketOffer>>>();

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

	/*
	 * get market offers for type
	 */

	private SortedSet<GoodTypeMarketOffer> getMarketOffers(Currency currency,
			GoodType goodType) {
		this.assertInitializedDataStructure(currency, goodType);

		return this.sellingOffersForGoodTypes.get(currency).get(goodType);
	}

	private SortedSet<GoodTypeMarketOffer> findMarketOffers(Agent agent,
			Currency currency, GoodType goodType) {
		SortedSet<GoodTypeMarketOffer> marketOffers = new TreeSet<GoodTypeMarketOffer>();
		if (this.getFor(agent) != null) {
			for (GoodTypeMarketOffer marketOffer : this.getFor(agent))
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

		super.save(goodTypeMarketOffer.getOfferor(), goodTypeMarketOffer);
	}

	@Override
	public synchronized void delete(GoodTypeMarketOffer goodTypeMarketOffer) {
		this.getMarketOffers(goodTypeMarketOffer.getCurrency(),
				goodTypeMarketOffer.getGoodType()).remove(goodTypeMarketOffer);

		super.delete(goodTypeMarketOffer.getOfferor(), goodTypeMarketOffer);
	}

	@Override
	public synchronized void deleteAllSellingOffers(Agent offeror) {
		if (this.getFor(offeror) != null)
			for (GoodTypeMarketOffer goodTypeMarketOffer : this.getFor(offeror))
				this.delete(goodTypeMarketOffer);
	}

	@Override
	public synchronized void deleteAllSellingOffers(Agent offeror,
			Currency currency, GoodType goodType) {
		for (GoodTypeMarketOffer goodTypeMarketOffer : this.findMarketOffers(
				offeror, currency, goodType))
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
