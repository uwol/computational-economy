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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import compecon.economy.sectors.financial.Currency;

public class CurrencyIndexedInMemoryDAOImpl<T> extends InMemoryDAOImpl<T> {

	private Map<Currency, List<T>> currencyIndexedInstances = new HashMap<Currency, List<T>>();

	private synchronized void assureInitializedDataStructure(Currency currency) {
		if (currency != null) {
			if (!this.currencyIndexedInstances.containsKey(currency))
				this.currencyIndexedInstances.put(currency, new ArrayList<T>());
		}
	}

	/*
	 * get instances for currency
	 */

	protected synchronized List<T> getInstancesForCurrency(Currency currency) {
		if (this.currencyIndexedInstances.containsKey(currency))
			return this.currencyIndexedInstances.get(currency);
		return null;
	}

	/*
	 * actions
	 */

	protected synchronized void save(Currency currency, T instance) {
		this.assureInitializedDataStructure(currency);

		this.currencyIndexedInstances.get(currency).add(instance);
		super.save(instance);
	}

	protected synchronized void delete(Currency currency, T instance) {
		if (this.currencyIndexedInstances.containsKey(currency)) {
			this.currencyIndexedInstances.get(currency).remove(instance);
			if (this.currencyIndexedInstances.get(currency).isEmpty())
				this.currencyIndexedInstances.remove(currency);
		}

		super.delete(instance);
	}
}
