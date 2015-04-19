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

package compecon.engine.dao.hibernate.impl;

import java.util.Iterator;
import java.util.List;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;

import compecon.economy.markets.MarketOrder;
import compecon.economy.markets.MarketParticipant;
import compecon.economy.materia.GoodType;
import compecon.economy.property.Property;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.dao.MarketOrderDAO;

public class MarketOrderDAOImpl extends HibernateDAOImpl<MarketOrder> implements
		MarketOrderDAO {

	@SuppressWarnings("unchecked")
	@Override
	public void deleteAllSellingOrders(final MarketParticipant offeror) {
		final String hql = "FROM MarketOrderImpl m WHERE m.offeror = :offeror";
		final List<MarketOrder> marketOrders = getSession().createQuery(hql)
				.setEntity("offeror", offeror).list();

		for (final MarketOrder marketOrder : marketOrders) {
			delete(marketOrder);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deleteAllSellingOrders(final MarketParticipant offeror,
			final Currency currency,
			final Class<? extends Property> propertyClass) {
		final String hql = "FROM MarketOrderImpl m WHERE m.offeror = :offeror AND m.currency = :currency AND m.property.class = :propertyClass";
		final List<MarketOrder> marketOrders = getSession().createQuery(hql)
				.setEntity("offeror", offeror)
				.setParameter("currency", currency)
				.setParameter("propertyClass", propertyClass.getSimpleName())
				.list();

		for (final MarketOrder marketOrder : marketOrders) {
			delete(marketOrder);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deleteAllSellingOrders(final MarketParticipant offeror,
			final Currency currency, final Currency commodityCurrency) {
		final String hql = "FROM MarketOrderImpl m WHERE m.offeror = :offeror AND m.currency = :currency AND m.commodityCurrency = :commodityCurrency";
		final List<MarketOrder> marketOrders = getSession().createQuery(hql)
				.setEntity("offeror", offeror)
				.setParameter("currency", currency)
				.setParameter("commodityCurrency", commodityCurrency).list();

		for (final MarketOrder marketOrder : marketOrders) {
			delete(marketOrder);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deleteAllSellingOrders(final MarketParticipant offeror,
			final Currency currency, final GoodType goodType) {
		final String hql = "FROM MarketOrderImpl m WHERE m.offeror = :offeror AND m.currency = :currency AND m.goodType = :goodType";
		final List<MarketOrder> marketOrders = getSession().createQuery(hql)
				.setEntity("offeror", offeror)
				.setParameter("currency", currency)
				.setParameter("goodType", goodType).list();

		for (final MarketOrder marketOrder : marketOrders) {
			delete(marketOrder);
		}
	}

	@Override
	public double findMarginalPrice(final Currency currency,
			final Class<? extends Property> propertyClass) {
		final String hql = "SELECT m.pricePerUnit FROM MarketOrderImpl m "
				+ " WHERE m.currency = :currency AND m.property.class = :propertyClass ORDER BY pricePerUnit ASC";
		final Object marginalPrice = getSession().createQuery(hql)
				.setMaxResults(1).setParameter("currency", currency)
				.setParameter("propertyClass", propertyClass.getSimpleName())
				.uniqueResult();

		if (marginalPrice == null) {
			return Double.NaN;
		}

		return (double) marginalPrice;
	}

	@Override
	public double findMarginalPrice(final Currency currency,
			final Currency commodityCurrency) {
		final String hql = "SELECT m.pricePerUnit FROM MarketOrderImpl m "
				+ "WHERE m.currency = :currency AND m.commodityCurrency = :commodityCurrency ORDER BY pricePerUnit ASC";
		final Object marginalPrice = getSession().createQuery(hql)
				.setMaxResults(1).setParameter("currency", currency)
				.setParameter("commodityCurrency", commodityCurrency)
				.uniqueResult();

		if (marginalPrice == null) {
			return Double.NaN;
		}

		return (double) marginalPrice;
	}

	@Override
	public double findMarginalPrice(final Currency currency,
			final GoodType goodType) {
		final String hql = "SELECT m.pricePerUnit FROM MarketOrderImpl m "
				+ "WHERE m.currency = :currency AND m.goodType = :goodType ORDER BY pricePerUnit ASC";
		final Object marginalPrice = getSession().createQuery(hql)
				.setMaxResults(1).setParameter("currency", currency)
				.setParameter("goodType", goodType).uniqueResult();

		if (marginalPrice == null) {
			return Double.NaN;
		}

		return (double) marginalPrice;
	}

	@Override
	public double getAmountSum(final Currency currency,
			final Currency commodityCurrency) {
		final String queryString = "SUM(m.pricePerUnit * m.amount) FROM MarketOrderImpl m "
				+ "WHERE m.currency = :currency AND m.commodityCurrency = :commodityCurrency";
		final Object amountSum = getSession().createQuery(queryString)
				.setMaxResults(1).setParameter("currency", currency)
				.setParameter("commodityCurrency", commodityCurrency)
				.uniqueResult();

		if (amountSum == null) {
			return Double.NaN;
		}

		return (double) amountSum;
	}

	@Override
	public double getAmountSum(final Currency currency, final GoodType goodType) {
		final String queryString = "SUM(m.pricePerUnit * m.amount) FROM MarketOrderImpl m "
				+ "WHERE m.currency = :currency AND m.goodType = :goodType";
		final Object amountSum = getSession().createQuery(queryString)
				.setMaxResults(1).setParameter("currency", currency)
				.setParameter("goodType", goodType).uniqueResult();

		if (amountSum == null) {
			return Double.NaN;
		}

		return (double) amountSum;
	}

	@Override
	public Iterator<MarketOrder> getIterator(final Currency currency,
			final Class<? extends Property> propertyClass) {
		final String queryString = "FROM MarketOrderImpl m "
				+ "WHERE m.currency = :currency AND m.property.class = :propertyClass "
				+ "ORDER BY m.pricePerUnit ASC";
		final ScrollableResults itemCursor = getSession()
				.createQuery(queryString).setParameter("currency", currency)
				.setParameter("propertyClass", propertyClass.getSimpleName())
				.scroll(ScrollMode.FORWARD_ONLY);
		return new HibernateIteratorImpl<MarketOrder>(itemCursor);
	}

	@Override
	public Iterator<MarketOrder> getIterator(final Currency currency,
			final Currency commodityCurrency) {
		final String queryString = "FROM MarketOrderImpl m "
				+ "WHERE m.currency = :currency AND m.commodityCurrency = :commodityCurrency "
				+ "ORDER BY m.pricePerUnit ASC";
		final ScrollableResults itemCursor = getSession()
				.createQuery(queryString).setParameter("currency", currency)
				.setParameter("commodityCurrency", commodityCurrency)
				.scroll(ScrollMode.FORWARD_ONLY);
		return new HibernateIteratorImpl<MarketOrder>(itemCursor);
	}

	@Override
	public Iterator<MarketOrder> getIterator(final Currency currency,
			final GoodType goodType) {
		final String queryString = "FROM MarketOrderImpl m "
				+ "WHERE m.currency = :currency AND m.goodType = :goodType "
				+ "ORDER BY m.pricePerUnit ASC";
		final ScrollableResults itemCursor = getSession()
				.createQuery(queryString).setParameter("currency", currency)
				.setParameter("goodType", goodType)
				.scroll(ScrollMode.FORWARD_ONLY);
		return new HibernateIteratorImpl<MarketOrder>(itemCursor);
	}

	@Override
	public Iterator<MarketOrder> getIteratorThreadsafe(final Currency currency,
			final Currency commodityCurrency) {
		return this.getIterator(currency, commodityCurrency);
	}

	@Override
	public Iterator<MarketOrder> getIteratorThreadsafe(final Currency currency,
			final GoodType goodType) {
		return this.getIterator(currency, goodType);
	}
}