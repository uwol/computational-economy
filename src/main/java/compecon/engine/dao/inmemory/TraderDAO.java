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

import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.trading.Trader;
import compecon.engine.dao.DAOFactory.ITraderDAO;

public class TraderDAO extends CurrencyIndexedInMemoryDAO<Trader> implements
		ITraderDAO {

	@Override
	public synchronized void delete(Trader entity) {
		super.delete(entity.getPrimaryCurrency(), entity);
	}

	@Override
	public synchronized List<Trader> findAllByCurrency(Currency currency) {
		return this.getInstancesForCurrency(currency);
	}

	@Override
	public synchronized void save(Trader entity) {
		super.save(entity.getPrimaryCurrency(), entity);
	}
}
