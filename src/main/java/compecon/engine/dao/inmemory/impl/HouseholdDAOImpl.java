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

import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.engine.dao.HouseholdDAO;

public class HouseholdDAOImpl extends
		AbstractIndexedInMemoryDAOImpl<Currency, Household> implements
		HouseholdDAO {

	@Override
	public synchronized List<Household> findAllByCurrency(Currency currency) {
		return this.getInstancesForKey(currency);
	}

	@Override
	public synchronized void save(Household entity) {
		super.save(entity.getPrimaryCurrency(), entity);
	}
}
