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

package compecon.engine.dao.hibernate.impl;

import org.hibernate.criterion.Restrictions;

import compecon.economy.sectors.financial.CentralBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.financial.impl.CentralBankImpl;
import compecon.engine.dao.CentralBankDAO;

public class CentralBankDAOImpl extends HibernateDAOImpl<CentralBank> implements
		CentralBankDAO {

	@Override
	public CentralBank findByCurrency(final Currency currency) {
		final Object object = getSession()
				.createCriteria(CentralBankImpl.class)
				.add(Restrictions.eq("primaryCurrency", currency))
				.uniqueResult();
		if (object == null) {
			return null;
		}
		return (CentralBank) object;
	}
}
