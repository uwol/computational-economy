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

package compecon.engine.dao.hibernate;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import compecon.economy.sectors.state.law.property.GoodTypeOwnership;
import compecon.engine.Agent;
import compecon.engine.dao.DAOFactory.IGoodTypeOwnershipDAO;

public class GoodTypeOwnershipDAO extends HibernateDAO<GoodTypeOwnership>
		implements IGoodTypeOwnershipDAO {

	@SuppressWarnings("unchecked")
	@Override
	public List<GoodTypeOwnership> findAllByAgent(Agent agent) {
		return (List<GoodTypeOwnership>) getSession()
				.createCriteria(GoodTypeOwnership.class)
				.add(Restrictions.eq("agent", agent)).list();
	}

	@Override
	public GoodTypeOwnership findFirstByAgent(Agent agent) {
		Object object = getSession().createCriteria(GoodTypeOwnership.class)
				.add(Restrictions.eq("agent", agent)).uniqueResult();
		if (object == null)
			return null;
		return (GoodTypeOwnership) object;
	}

}
