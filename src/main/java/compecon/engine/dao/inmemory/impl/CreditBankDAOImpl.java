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

package compecon.engine.dao.inmemory.impl;

import java.util.List;

import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.dao.CreditBankDAO;

public class CreditBankDAOImpl extends
		AbstractIndexedInMemoryDAOImpl<Currency, CreditBank> implements
		CreditBankDAO {

	@Override
	public synchronized CreditBank findRandom(Currency currency) {
		final List<CreditBank> creditBanks = this.findAllByCurrency(currency);
		if (creditBanks != null && !creditBanks.isEmpty()) {
			int id = this.randomizer.nextInt(creditBanks.size());
			return creditBanks.get(id);
		}
		return null;
	}

	@Override
	public synchronized List<CreditBank> findAllByCurrency(Currency currency) {
		return this.getInstancesForKey(currency);
	}

	@Override
	public synchronized void save(CreditBank entity) {
		super.save(entity.getPrimaryCurrency(), entity);
	}
}
