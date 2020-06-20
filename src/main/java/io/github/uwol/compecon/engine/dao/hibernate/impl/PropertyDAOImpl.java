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

package io.github.uwol.compecon.engine.dao.hibernate.impl;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import io.github.uwol.compecon.economy.agent.Agent;
import io.github.uwol.compecon.economy.property.Property;
import io.github.uwol.compecon.economy.property.PropertyIssued;
import io.github.uwol.compecon.economy.property.PropertyOwner;
import io.github.uwol.compecon.economy.property.impl.PropertyImpl;
import io.github.uwol.compecon.economy.property.impl.PropertyIssuedImpl;
import io.github.uwol.compecon.engine.dao.PropertyDAO;

public class PropertyDAOImpl extends HibernateDAOImpl<Property> implements PropertyDAO {

	@SuppressWarnings("unchecked")
	@Override
	public List<Property> findAllPropertiesIssuedByAgent(final Agent issuer) {
		return getSession().createCriteria(PropertyIssuedImpl.class).add(Restrictions.eq("issuer", issuer)).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Property> findAllPropertiesIssuedByAgent(final Agent issuer,
			final Class<? extends PropertyIssued> propertyClass) {
		return getSession().createCriteria(propertyClass).add(Restrictions.eq("issuer", issuer)).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Property> findAllPropertiesOfPropertyOwner(final PropertyOwner propertyOwner) {
		return getSession().createCriteria(PropertyImpl.class).add(Restrictions.eq("owner", propertyOwner)).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Property> findAllPropertiesOfPropertyOwner(final PropertyOwner propertyOwner,
			final Class<? extends Property> propertyClass) {
		return getSession().createCriteria(propertyClass).add(Restrictions.eq("owner", propertyOwner)).list();
	}

	@Override
	public void transferProperty(final PropertyOwner oldOwner, final PropertyOwner newOwner, final Property property) {
		property.setOwner(newOwner);
	}
}
