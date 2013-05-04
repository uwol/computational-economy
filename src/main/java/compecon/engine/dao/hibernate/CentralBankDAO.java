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

import org.hibernate.criterion.Restrictions;

import compecon.culture.sectors.financial.CentralBank;
import compecon.culture.sectors.financial.Currency;
import compecon.engine.dao.DAOFactory.ICentralBankDAO;

public class CentralBankDAO extends HibernateDAO<CentralBank> implements
		ICentralBankDAO {
	public CentralBank findByCurrency(Currency currency) {
		Object object = getSession().createCriteria(CentralBank.class)
				.add(Restrictions.eq("primaryCurrency", currency))
				.uniqueResult();
		if (object == null)
			return null;
		return (CentralBank) object;
	}
}
