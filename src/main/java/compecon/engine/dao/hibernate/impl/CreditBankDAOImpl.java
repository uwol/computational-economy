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

import java.util.List;
import java.util.Random;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.financial.impl.CreditBankImpl;
import compecon.engine.dao.CreditBankDAO;

public class CreditBankDAOImpl extends HibernateDAOImpl<CreditBank> implements
		CreditBankDAO {

	@SuppressWarnings("unchecked")
	@Override
	public List<CreditBank> findAllByCurrency(final Currency currency) {
		return getSession().createCriteria(CreditBankImpl.class)
				.add(Restrictions.eq("primaryCurrency", currency)).list();
	}

	@Override
	public CreditBank findRandom(final Currency currency) {
		Criteria crit = getSession().createCriteria(CreditBankImpl.class);
		crit.add(Restrictions.eq("primaryCurrency", currency));
		crit.setProjection(Projections.rowCount());
		final int count = ((Number) crit.uniqueResult()).intValue();
		if (0 != count) {
			final int index = new Random().nextInt(count);
			crit = getSession().createCriteria(CreditBankImpl.class);
			crit.add(Restrictions.eq("primaryCurrency", currency));
			return (CreditBankImpl) crit.setFirstResult(index).setMaxResults(1)
					.uniqueResult();
		}
		return null;
	}
}
