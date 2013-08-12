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

import org.hibernate.criterion.Restrictions;

import compecon.culture.sectors.state.law.property.Property;
import compecon.engine.Agent;
import compecon.engine.dao.DAOFactory.IPropertyDAO;

public class PropertyDAO extends HibernateDAO<Property> implements IPropertyDAO {

	@SuppressWarnings("unchecked")
	@Override
	public List<Property> findAllByAgent(Agent agent) {
		return (List<Property>) getSession().createCriteria(Property.class)
				.add(Restrictions.eq("owner", agent)).list();
	}

	@Override
	public void transferProperty(Agent oldOwner, Agent newOwner,
			Property property) {
		property.setOwner(newOwner);
	}

}
