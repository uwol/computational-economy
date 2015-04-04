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
import java.util.List;

import compecon.economy.agent.Agent;
import compecon.economy.property.Property;
import compecon.economy.property.PropertyIssued;
import compecon.economy.property.PropertyOwner;
import compecon.engine.dao.PropertyDAO;

public class PropertyDAOImpl extends
		AbstractDoubleIndexedInMemoryDAOImpl<PropertyOwner, Property> implements
		PropertyDAO {

	@Override
	public List<Property> findAllPropertiesIssuedByAgent(final Agent issuer) {
		final List<Property> propertiesIssuedByAgent = getInstancesForSecondKey(issuer);
		if (propertiesIssuedByAgent != null) {
			return new ArrayList<Property>(propertiesIssuedByAgent);
		}
		return new ArrayList<Property>();
	}

	@Override
	public List<Property> findAllPropertiesIssuedByAgent(final Agent issuer,
			final Class<? extends PropertyIssued> propertyClass) {
		final List<Property> propertiesIssuedByAgent = new ArrayList<Property>();
		for (final Property propertyIssued : this
				.findAllPropertiesIssuedByAgent(issuer)) {
			if (propertyClass.isAssignableFrom(propertyIssued.getClass())) {
				propertiesIssuedByAgent.add(propertyIssued);
			}
		}
		return propertiesIssuedByAgent;
	}

	@Override
	public synchronized List<Property> findAllPropertiesOfPropertyOwner(
			final PropertyOwner propertyOwner) {
		final List<Property> propertiesOfPropertyOwner = getInstancesForFirstKey(propertyOwner);
		if (propertiesOfPropertyOwner != null) {
			return new ArrayList<Property>(propertiesOfPropertyOwner);
		}
		return new ArrayList<Property>();
	}

	@Override
	public List<Property> findAllPropertiesOfPropertyOwner(
			final PropertyOwner propertyOwner,
			final Class<? extends Property> propertyClass) {
		final List<Property> propertiesOfClass = new ArrayList<Property>();
		for (final Property property : this
				.findAllPropertiesOfPropertyOwner(propertyOwner)) {
			if (propertyClass.isAssignableFrom(property.getClass())) {
				propertiesOfClass.add(property);
			}
		}
		return propertiesOfClass;
	}

	@Override
	public synchronized void save(final Property property) {
		if (property instanceof PropertyIssued) {
			super.save(property.getOwner(),
					((PropertyIssued) property).getIssuer(), property);
		} else {
			super.save(property.getOwner(), property);
		}
	}

	@Override
	public synchronized void transferProperty(final PropertyOwner oldOwner,
			final PropertyOwner newOwner, final Property property) {
		// the property is deleted and re-saved, so that the
		// agent-property-index is updated
		delete(property);
		property.setOwner(newOwner);
		this.save(property);
	}
}
