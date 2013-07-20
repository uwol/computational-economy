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

import java.util.List;

import compecon.culture.sectors.financial.CentralBank;
import compecon.culture.sectors.financial.Currency;
import compecon.engine.dao.DAOFactory.ICentralBankDAO;

public class CentralBankDAO extends CurrencyIndexedInMemoryDAO<CentralBank>
		implements ICentralBankDAO {

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
		if (centralBanksForCurrency.size() > 1)
			throw new RuntimeException(
					"more than one central bank per currency");
		return centralBanksForCurrency.get(0);
	}

	@Override
	public synchronized void save(CentralBank entity) {
		super.save(entity.getPrimaryCurrency(), entity);
	}
}
