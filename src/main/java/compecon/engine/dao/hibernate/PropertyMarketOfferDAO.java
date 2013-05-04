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

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;

import compecon.culture.markets.PropertyMarketOffer;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.property.Property;
import compecon.engine.Agent;
import compecon.engine.dao.DAOFactory.IPropertyMarketOfferDAO;

public class PropertyMarketOfferDAO extends HibernateDAO<PropertyMarketOffer>
		implements IPropertyMarketOfferDAO {

	@Override
	public void deleteAllSellingOffers(Agent offeror) {
		String hql = "DELETE FROM PropertyMarketOffer m WHERE m.offeror = :offeror";
		getSession().createQuery(hql).setEntity("offeror", offeror)
				.executeUpdate();
	}

	@Override
	public void deleteAllSellingOffers(Agent offeror, Currency currency,
			Class<? extends Property> propertyClass) {
		String hql = "DELETE FROM PropertyMarketOffer m WHERE m.offeror = :offeror AND m.currency = :currency AND m.property.class = :propertyClass";
		getSession().createQuery(hql).setEntity("offeror", offeror)
				.setParameter("currency", currency)
				.setParameter("propertyClass", propertyClass.getName())
				.executeUpdate();
	}

	@Override
	public double findMarginalPrice(Currency currency,
			Class<? extends Property> propertyClass) {
		String hql = "SELECT m.pricePerUnit FROM PropertyMarketOffer m "
				+ " WHERE m.currency = :currency AND m.property.class = :propertyClass ORDER BY pricePerUnit ASC";
		Object marginalPrice = getSession().createQuery(hql).setMaxResults(1)
				.setParameter("currency", currency)
				.setParameter("propertyClass", propertyClass.getName())
				.uniqueResult();
		if (marginalPrice == null)
			return Double.NaN;
		return (double) marginalPrice;
	}

	@Override
	public Iterator<PropertyMarketOffer> getIterator(
			Class<? extends Property> propertyClass, Currency currency) {
		String queryString = "FROM PropertyMarketOffer m "
				+ "WHERE m.currency = :currency AND m.property.class = :propertyClass "
				+ "ORDER BY m.pricePerUnit ASC";
		ScrollableResults itemCursor = getSession().createQuery(queryString)
				.setParameter("currency", currency)
				.setParameter("propertyClass", propertyClass.getName())
				.scroll(ScrollMode.FORWARD_ONLY);
		return new HibernateIterator<PropertyMarketOffer>(itemCursor);
	}
}