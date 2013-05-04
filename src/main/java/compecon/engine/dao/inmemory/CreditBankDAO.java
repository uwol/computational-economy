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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.engine.dao.DAOFactory.ICreditBankDAO;

public class CreditBankDAO extends InMemoryDAO<CreditBank> implements
		ICreditBankDAO {

	Map<Currency, List<CreditBank>> creditBanksByCurrencies = new HashMap<Currency, List<CreditBank>>();

	protected void assertCurrencyDataStructure(Currency currency) {
		if (currency != null) {
			if (!this.creditBanksByCurrencies.containsKey(currency)) {
				this.creditBanksByCurrencies.put(currency,
						new ArrayList<CreditBank>());
			}
		}
	}

	@Override
	public synchronized void save(CreditBank entity) {
		assertCurrencyDataStructure(entity.getPrimaryCurrency());
		super.save(entity);
		if (entity.getPrimaryCurrency() != null)
			this.creditBanksByCurrencies.get(entity.getPrimaryCurrency()).add(
					entity);
	}

	@Override
	public synchronized void delete(CreditBank entity) {
		assertCurrencyDataStructure(entity.getPrimaryCurrency());
		super.delete(entity);
		if (entity.getPrimaryCurrency() != null)
			this.creditBanksByCurrencies.get(entity.getPrimaryCurrency())
					.remove(entity);
	}

	@Override
	public synchronized CreditBank findRandom(Currency currency) {
		if (this.creditBanksByCurrencies.containsKey(currency)) {
			List<CreditBank> creditBanks = this.creditBanksByCurrencies
					.get(currency);
			int id = this.randomizer.nextInt(creditBanks.size());
			return creditBanks.get(id);
		}
		return null;
	}

	@Override
	public synchronized List<CreditBank> findAll(Currency currency) {
		if (this.creditBanksByCurrencies.containsKey(currency)) {
			return creditBanksByCurrencies.get(currency);
		}
		return new ArrayList<CreditBank>();
	}
}
