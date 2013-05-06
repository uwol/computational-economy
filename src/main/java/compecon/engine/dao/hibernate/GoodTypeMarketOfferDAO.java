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

package compecon.engine.dao.hibernate;

import java.util.Iterator;
import java.util.List;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;

import compecon.culture.markets.GoodTypeMarketOffer;
import compecon.culture.sectors.financial.Currency;
import compecon.engine.Agent;
import compecon.engine.dao.DAOFactory.IGoodTypeMarketOfferDAO;
import compecon.nature.materia.GoodType;

public class GoodTypeMarketOfferDAO extends HibernateDAO<GoodTypeMarketOffer>
		implements IGoodTypeMarketOfferDAO {

	@SuppressWarnings("unchecked")
	@Override
	public void deleteAllSellingOffers(Agent offeror) {
		String hql = "FROM GoodTypeMarketOffer m WHERE m.offeror = :offeror";
		List<GoodTypeMarketOffer> marketOffers = getSession().createQuery(hql)
				.setEntity("offeror", offeror).list();
		for (GoodTypeMarketOffer marketOffer : marketOffers)
			this.delete(marketOffer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deleteAllSellingOffers(Agent offeror, Currency currency,
			GoodType goodType) {
		String hql = "FROM GoodTypeMarketOffer m WHERE m.offeror = :offeror AND m.currency = :currency AND m.goodType = :goodType";
		List<GoodTypeMarketOffer> marketOffers = getSession().createQuery(hql)
				.setEntity("offeror", offeror)
				.setParameter("currency", currency)
				.setParameter("goodType", goodType).list();
		for (GoodTypeMarketOffer marketOffer : marketOffers)
			this.delete(marketOffer);
	}

	@Override
	public double findMarginalPrice(Currency currency, GoodType goodType) {
		String hql = "SELECT m.pricePerUnit FROM GoodTypeMarketOffer m "
				+ "WHERE m.currency = :currency AND m.goodType = :goodType ORDER BY pricePerUnit ASC";
		Object marginalPrice = getSession().createQuery(hql).setMaxResults(1)
				.setParameter("currency", currency)
				.setParameter("goodType", goodType).uniqueResult();
		if (marginalPrice == null)
			return Double.NaN;
		return (double) marginalPrice;
	}

	@Override
	public Iterator<GoodTypeMarketOffer> getIterator(GoodType goodType,
			Currency currency) {
		String queryString = "FROM GoodTypeMarketOffer m "
				+ "WHERE m.currency = :currency AND m.goodType = :goodType "
				+ "ORDER BY m.pricePerUnit ASC";
		ScrollableResults itemCursor = getSession().createQuery(queryString)
				.setParameter("currency", currency)
				.setParameter("goodType", goodType)
				.scroll(ScrollMode.FORWARD_ONLY);
		return new HibernateIterator<GoodTypeMarketOffer>(itemCursor);
	}
}