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

import compecon.economy.sectors.financial.CentralBank;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.dao.CentralBankDAO;

public class CentralBankDAOImpl extends
		CurrencyIndexedInMemoryDAOImpl<CentralBank> implements CentralBankDAO {

	@Override
	public synchronized void delete(CentralBank entity) {
		super.delete(entity.getPrimaryCurrency(), entity);
	}

	@Override
	public synchronized CentralBank findByCurrency(Currency currency) {
		// should contain only one element
		List<CentralBank> centralBanksForCurrency = this
				.getInstancesForCurrency(currency);
		if (centralBanksForCurrency == null)
			return null;

		assert (centralBanksForCurrency.size() <= 1);

		return centralBanksForCurrency.get(0);
	}

	@Override
	public synchronized void save(CentralBank entity) {
		super.save(entity.getPrimaryCurrency(), entity);
	}
}
