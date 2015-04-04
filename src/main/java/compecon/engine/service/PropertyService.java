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

package compecon.engine.service;

import java.util.List;
import java.util.Map;

import compecon.economy.agent.Agent;
import compecon.economy.materia.GoodType;
import compecon.economy.property.Property;
import compecon.economy.property.PropertyIssued;
import compecon.economy.property.PropertyOwner;

public interface PropertyService {

	/**
	 * @see #incrementGoodTypeAmount(PropertyOwner, GoodType, double)
	 */
	public double decrementGoodTypeAmount(final PropertyOwner propertyOwner,
			final GoodType goodType, double amount);

	/**
	 * deletes the given property from the persistence backend.
	 */
	public void deleteProperty(final Property property);

	/**
	 * returns all properties issued by the given agent.
	 */
	public List<Property> findAllPropertiesIssuedByAgent(final Agent issuer);

	/**
	 * returns all properties issued by the given agent, which are assignable to
	 * the given property class. E. g. for the property class "Share" all owned
	 * objects of "ShareImpl" are returned.
	 */
	public List<Property> findAllPropertiesIssuedByAgent(final Agent issuer,
			final Class<? extends PropertyIssued> propertyClass);

	/**
	 * returns all properties owned by the given property owner.
	 */
	public List<Property> findAllPropertiesOfPropertyOwner(
			final PropertyOwner propertyOwner);

	/**
	 * returns all properties owned by the given property owner, which are
	 * assignable to the given property class. E. g. for the property class
	 * "Share" all owned objects of "ShareImpl" are returned.
	 */
	public List<Property> findAllPropertiesOfPropertyOwner(
			final PropertyOwner propertyOwner,
			final Class<? extends Property> propertyClass);

	/**
	 * returns the amounts of all capital owned by the given property owner.
	 */
	public Map<GoodType, Double> getCapitalBalances(
			final PropertyOwner propertyOwner);

	/**
	 * returns the amount of given good type owned by the given property owner.
	 */
	public double getGoodTypeBalance(final PropertyOwner propertyOwner,
			final GoodType goodType);

	/**
	 * returns the amounts of all good types owned by the given property owner.
	 */
	public Map<GoodType, Double> getGoodTypeBalances(
			final PropertyOwner propertyOwner);

	/**
	 * @see #decrementGoodTypeAmount(PropertyOwner, GoodType, double)
	 */
	public double incrementGoodTypeAmount(final PropertyOwner propertyOwner,
			final GoodType goodType, final double amount);

	/**
	 * Resets the owned amount of the given good type for the property owner to
	 * 0. Needed for cases, when goods are consumed.
	 */
	public void resetGoodTypeAmount(final PropertyOwner propertyOwner,
			final GoodType goodType);

	/**
	 * transfers all properties from the given oldOwner to a random new owner.
	 * Needed for cases, when property owners are deconstructed.
	 */
	public void transferEverythingToRandomAgent(final PropertyOwner oldOwner);

	/**
	 * Transfers the given amount of good from the old owner to the new owner.
	 */
	public void transferGoodTypeAmount(final GoodType goodType,
			final PropertyOwner oldOwner, final PropertyOwner newOwner,
			final double amount);

	/**
	 * Transfers the given property from the old owner to the new owner. Both
	 * owners are informed on the transaction via their callback methods.
	 */
	public void transferProperty(final Property property,
			final PropertyOwner oldOwner, final PropertyOwner newOwner);
}
