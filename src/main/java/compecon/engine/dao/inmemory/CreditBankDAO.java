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

package compecon.engine.dao.inmemory;

import java.util.List;

import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.engine.dao.DAOFactory.ICreditBankDAO;

public class CreditBankDAO extends CurrencyIndexedInMemoryDAO<CreditBank>
		implements ICreditBankDAO {

	@Override
	public synchronized void delete(CreditBank entity) {
		super.delete(entity.getPrimaryCurrency(), entity);
	}

	@Override
	public synchronized CreditBank findRandom(Currency currency) {
		List<CreditBank> creditBanks = this.findAllByCurrency(currency);
		if (creditBanks != null && !creditBanks.isEmpty()) {
			int id = this.randomizer.nextInt(creditBanks.size());
			return creditBanks.get(id);
		}
		return null;
	}

	@Override
	public synchronized List<CreditBank> findAllByCurrency(Currency currency) {
		return this.getInstancesForCurrency(currency);
	}

	@Override
	public synchronized void save(CreditBank entity) {
		super.save(entity.getPrimaryCurrency(), entity);
	}
}
