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

import org.hibernate.criterion.Restrictions;

import compecon.economy.agent.Agent;
import compecon.economy.property.Property;
import compecon.economy.property.PropertyIssued;
import compecon.economy.property.PropertyOwner;
import compecon.economy.property.impl.PropertyImpl;
import compecon.economy.property.impl.PropertyIssuedImpl;
import compecon.engine.dao.PropertyDAO;

public class PropertyDAOImpl extends HibernateDAOImpl<Property> implements
		PropertyDAO {

	@SuppressWarnings("unchecked")
	@Override
	public List<Property> findAllPropertiesOfPropertyOwner(PropertyOwner propertyOwner) {
		return (List<Property>) getSession().createCriteria(PropertyImpl.class)
				.add(Restrictions.eq("owner", propertyOwner)).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Property> findAllPropertiesOfPropertyOwner(PropertyOwner propertyOwner,
			Class<? extends Property> propertyClass) {
		return (List<Property>) getSession().createCriteria(propertyClass)
				.add(Restrictions.eq("owner", propertyOwner)).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PropertyIssued> findAllPropertiesIssuedByAgent(Agent issuer) {
		return (List<PropertyIssued>) getSession()
				.createCriteria(PropertyIssuedImpl.class)
				.add(Restrictions.eq("issuer", issuer)).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PropertyIssued> findAllPropertiesIssuedByAgent(Agent issuer,
			Class<? extends PropertyIssued> propertyClass) {
		return (List<PropertyIssued>) getSession()
				.createCriteria(propertyClass)
				.add(Restrictions.eq("issuer", issuer)).list();
	}

	@Override
	public void transferProperty(PropertyOwner oldOwner,
			PropertyOwner newOwner, Property property) {
		property.setOwner(newOwner);
	}
}
