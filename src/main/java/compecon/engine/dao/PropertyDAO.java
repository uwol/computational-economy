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

package compecon.engine.dao;

import java.util.List;

import compecon.economy.agent.Agent;
import compecon.economy.property.Property;
import compecon.economy.property.PropertyIssued;
import compecon.economy.property.PropertyOwner;

public interface PropertyDAO extends GenericDAO<Property> {

	public List<Property> findAllPropertiesOfPropertyOwner(
			final PropertyOwner propertyOwner);

	public List<Property> findAllPropertiesOfPropertyOwner(
			final PropertyOwner propertyOwner,
			final Class<? extends Property> propertyClass);

	public List<Property> findAllPropertiesIssuedByAgent(final Agent issuer);

	public List<Property> findAllPropertiesIssuedByAgent(final Agent issuer,
			final Class<? extends PropertyIssued> propertyClass);

	/**
	 * WARNING: Should only be called from the property service, which ensures a
	 * subsequent Hibernate flush.
	 * 
	 * @see compecon.engine.service.PropertyService
	 */
	public void transferProperty(final PropertyOwner oldOwner,
			final PropertyOwner newOwner, final Property property);
}
