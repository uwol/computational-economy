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

package io.github.uwol.compecon.engine.dao.inmemory.impl;

import java.util.ArrayList;
import java.util.List;

import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.economy.sectors.industry.Factory;
import io.github.uwol.compecon.engine.dao.FactoryDAO;

public class FactoryDAOImpl extends AbstractIndexedInMemoryDAOImpl<Currency, Factory> implements FactoryDAO {

	@Override
	public synchronized List<Factory> findAllByCurrency(final Currency currency) {
		return getInstancesForKey(currency);
	}

	@Override
	public synchronized List<Factory> findAllByCurrencyAndProducedGoodType(final Currency currency,
			final GoodType producedGoodType) {
		final List<Factory> factoriesProducingGoodType = new ArrayList<Factory>();
		for (final Factory factory : getInstancesForKey(currency)) {
			if (producedGoodType.equals(factory.getProducedGoodType())) {
				factoriesProducingGoodType.add(factory);
			}
		}
		return factoriesProducingGoodType;
	}

	@Override
	public synchronized void save(final Factory entity) {
		super.save(entity.getPrimaryCurrency(), entity);
	}
}
