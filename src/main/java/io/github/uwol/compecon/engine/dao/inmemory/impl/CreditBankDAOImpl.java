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

import java.util.List;

import io.github.uwol.compecon.economy.sectors.financial.CreditBank;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.dao.CreditBankDAO;

public class CreditBankDAOImpl extends AbstractIndexedInMemoryDAOImpl<Currency, CreditBank> implements CreditBankDAO {

	@Override
	public synchronized List<CreditBank> findAllByCurrency(final Currency currency) {
		return getInstancesForKey(currency);
	}

	@Override
	public synchronized CreditBank findRandom(final Currency currency) {
		final List<CreditBank> creditBanks = findAllByCurrency(currency);

		if (creditBanks != null && !creditBanks.isEmpty()) {
			final int id = ApplicationContext.getInstance().getRandomNumberGenerator().nextInt(creditBanks.size());
			return creditBanks.get(id);
		}

		return null;
	}

	@Override
	public synchronized void save(final CreditBank entity) {
		super.save(entity.getPrimaryCurrency(), entity);
	}
}
