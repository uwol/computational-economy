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

package compecon.engine.dao.hibernate;

import java.util.List;
import java.util.Random;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.engine.dao.DAOFactory.ICreditBankDAO;

public class CreditBankDAO extends HibernateDAO<CreditBank, Long> implements
		ICreditBankDAO {

	@Override
	public CreditBank findRandom(Currency currency) {
		Criteria crit = getSession().createCriteria(CreditBank.class);
		crit.setProjection(Projections.rowCount());
		int count = ((Number) crit.uniqueResult()).intValue();
		if (0 != count) {
			int index = new Random().nextInt(count);
			crit = getSession().createCriteria(CreditBank.class);
			crit.add(Restrictions.eq("primaryCurrency", currency));
			return (CreditBank) crit.setFirstResult(index).setMaxResults(1)
					.uniqueResult();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CreditBank> findAll(Currency currency) {
		return (List<CreditBank>) getSession().createCriteria(CreditBank.class)
				.add(Restrictions.eq("primaryCurrency", currency)).list();
	}
}
