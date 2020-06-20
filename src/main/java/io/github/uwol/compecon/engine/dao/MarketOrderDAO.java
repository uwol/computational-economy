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

package io.github.uwol.compecon.engine.dao;

import java.util.Iterator;

import io.github.uwol.compecon.economy.markets.MarketOrder;
import io.github.uwol.compecon.economy.markets.MarketParticipant;
import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.property.Property;
import io.github.uwol.compecon.economy.sectors.financial.Currency;

public interface MarketOrderDAO extends GenericDAO<MarketOrder> {

	/**
	 * WARNING: Should only be called from the market order factory, which ensures a
	 * subsequent Hibernate flush.
	 *
	 * @see io.github.uwol.compecon.engine.factory.MarketOrderFactory
	 */
	public void deleteAllSellingOrders(final MarketParticipant offeror);

	/**
	 * WARNING: Should only be called from the market order factory, which ensures a
	 * subsequent Hibernate flush.
	 *
	 * @see io.github.uwol.compecon.engine.factory.MarketOrderFactory
	 */
	public void deleteAllSellingOrders(final MarketParticipant offeror, final Currency currency,
			final Class<? extends Property> propertyClass);

	/**
	 * WARNING: Should only be called from the market order factory, which ensures a
	 * subsequent Hibernate flush.
	 *
	 * @see io.github.uwol.compecon.engine.factory.MarketOrderFactory
	 */
	public void deleteAllSellingOrders(final MarketParticipant offeror, final Currency currency,
			final Currency commodityCurrency);

	/**
	 * WARNING: Should only be called from the market order factory, which ensures a
	 * subsequent Hibernate flush.
	 *
	 * @see io.github.uwol.compecon.engine.factory.MarketOrderFactory
	 */
	public void deleteAllSellingOrders(final MarketParticipant offeror, final Currency currency,
			final GoodType goodType);

	public double findMarginalPrice(final Currency currency, final Class<? extends Property> propertyClass);

	public double findMarginalPrice(final Currency currency, final Currency commodityCurrency);

	public double findMarginalPrice(final Currency currency, final GoodType goodType);

	public double getAmountSum(final Currency currency, final Currency commodityCurrency);

	public double getAmountSum(final Currency currency, final GoodType goodType);

	public Iterator<MarketOrder> getIterator(final Currency currency, final Class<? extends Property> propertyClass);

	public Iterator<MarketOrder> getIterator(final Currency currency, final Currency commodityCurrency);

	public Iterator<MarketOrder> getIterator(final Currency currency, final GoodType goodType);

	public Iterator<MarketOrder> getIteratorThreadsafe(final Currency currency, final Currency commodityCurrency);

	public Iterator<MarketOrder> getIteratorThreadsafe(final Currency currency, final GoodType goodType);
}