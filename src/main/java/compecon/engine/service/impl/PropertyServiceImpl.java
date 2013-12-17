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

package compecon.engine.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import compecon.economy.agent.Agent;
import compecon.economy.materia.GoodType;
import compecon.economy.property.GoodTypeOwnership;
import compecon.economy.property.Property;
import compecon.economy.property.PropertyIssued;
import compecon.economy.property.PropertyOwner;
import compecon.economy.sectors.household.Household;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.service.PropertyService;
import compecon.engine.util.HibernateUtil;
import compecon.math.util.MathUtil;

/**
 * The property service manages property rights. Each {@link Property} can be
 * assigned to an property owner and can be transfered between owners.
 */
public class PropertyServiceImpl implements PropertyService {

	/*
	 * assures
	 */

	protected GoodTypeOwnership assureGoodTypeOwnership(
			final PropertyOwner propertyOwner) {
		assert (propertyOwner != null);

		final GoodTypeOwnership goodTypeOwnership = ApplicationContext
				.getInstance().getGoodTypeOwnershipDAO()
				.findFirstByPropertyOwner(propertyOwner);
		if (goodTypeOwnership == null) {
			assert (!propertyOwner.isDeconstructed());
			return ApplicationContext.getInstance()
					.getGoodTypeOwnershipFactory()
					.newInstanceGoodTypeOwnership(propertyOwner);
		}
		return goodTypeOwnership;
	}

	public double decrementGoodTypeAmount(final PropertyOwner propertyOwner,
			final GoodType goodType, double amount) {
		assert (amount >= 0.0);

		GoodTypeOwnership goodTypeOwnership = assureGoodTypeOwnership(propertyOwner);
		double oldBalance = goodTypeOwnership.getOwnedGoodTypes().get(goodType);

		assert (oldBalance >= amount || MathUtil.equal(oldBalance, amount));

		double newBalance = Math.max(oldBalance - amount, 0);
		goodTypeOwnership.getOwnedGoodTypes().put(goodType, newBalance);

		HibernateUtil.flushSession();

		return newBalance;
	}

	public void deleteProperty(final Property property) {
		ApplicationContext.getInstance().getPropertyDAO().delete(property);
		HibernateUtil.flushSession();
	}

	public List<Property> findAllPropertiesOfPropertyOwner(
			final PropertyOwner propertyOwner) {
		return ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(propertyOwner);
	}

	public List<Property> findAllPropertiesOfPropertyOwner(
			final PropertyOwner propertyOwner,
			final Class<? extends Property> propertyClass) {
		return ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(propertyOwner, propertyClass);
	}

	public List<Property> findAllPropertiesIssuedByAgent(final Agent issuer) {
		return ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesIssuedByAgent(issuer);
	}

	public List<Property> findAllPropertiesIssuedByAgent(final Agent issuer,
			final Class<? extends PropertyIssued> propertyClass) {
		return ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesIssuedByAgent(issuer, propertyClass);
	}

	public double getBalance(final PropertyOwner propertyOwner,
			final GoodType goodType) {
		assureGoodTypeOwnership(propertyOwner);

		GoodTypeOwnership goodTypeOwnership = ApplicationContext.getInstance()
				.getGoodTypeOwnershipDAO()
				.findFirstByPropertyOwner(propertyOwner);
		return goodTypeOwnership.getOwnedGoodTypes().get(goodType);
	}

	public Map<GoodType, Double> getBalances(final PropertyOwner propertyOwner) {
		assureGoodTypeOwnership(propertyOwner);

		return ApplicationContext.getInstance().getGoodTypeOwnershipDAO()
				.findFirstByPropertyOwner(propertyOwner).getOwnedGoodTypes();
	}

	public double incrementGoodTypeAmount(final PropertyOwner propertyOwner,
			final GoodType goodType, double amount) {
		assert (amount >= 0.0);

		GoodTypeOwnership goodTypeOwnership = assureGoodTypeOwnership(propertyOwner);
		double newBalance = goodTypeOwnership.getOwnedGoodTypes().get(goodType)
				+ amount;
		goodTypeOwnership.getOwnedGoodTypes().put(goodType, newBalance);

		HibernateUtil.flushSession();

		return newBalance;
	}

	public void resetGoodTypeAmount(final PropertyOwner propertyOwner,
			final GoodType goodType) {
		GoodTypeOwnership goodTypeOwnership = assureGoodTypeOwnership(propertyOwner);
		goodTypeOwnership.getOwnedGoodTypes().put(goodType, 0.0);

		HibernateUtil.flushSession();
	}

	public void transferGoodTypeAmount(final GoodType goodType,
			final PropertyOwner oldOwner, final PropertyOwner newOwner,
			final double amount) {
		this.decrementGoodTypeAmount(oldOwner, goodType, amount);
		this.incrementGoodTypeAmount(newOwner, goodType, amount);

		HibernateUtil.flushSession();
	}

	/**
	 * newOwner with value null is allowed, e. g. for shares
	 */
	public void transferProperty(final Property property,
			final PropertyOwner oldOwner, final PropertyOwner newOwner) {

		// consistency check
		assert (oldOwner == property.getOwner());

		property.resetOwner();
		ApplicationContext.getInstance().getPropertyDAO()
				.transferProperty(oldOwner, newOwner, property);
		oldOwner.onPropertyTransferred(property, oldOwner, newOwner);
		if (newOwner != null) {
			newOwner.onPropertyTransferred(property, oldOwner, newOwner);
		}

		HibernateUtil.flushSession();
	}

	public void transferEverythingToRandomAgent(final PropertyOwner oldOwner) {
		if (oldOwner == null)
			return;

		// fetch a random new owner
		Household newOwnerHousehold = null;
		while ((newOwnerHousehold == null || oldOwner == newOwnerHousehold)
				&& ApplicationContext.getInstance().getHouseholdDAO().findAll()
						.size() > 1) {
			newOwnerHousehold = ApplicationContext.getInstance()
					.getHouseholdDAO().findRandom();
		}

		assert (newOwnerHousehold != oldOwner);
		assert (newOwnerHousehold == null || !newOwnerHousehold
				.isDeconstructed());

		// transfer all goods
		if (newOwnerHousehold != null) {
			assureGoodTypeOwnership(newOwnerHousehold);

			for (GoodTypeOwnership goodTypeOwnership : ApplicationContext
					.getInstance().getGoodTypeOwnershipDAO()
					.findAllByPropertyOwner(oldOwner)) {
				for (Entry<GoodType, Double> entry : goodTypeOwnership
						.getOwnedGoodTypes().entrySet()) {
					if (!entry.getKey().equals(GoodType.LABOURHOUR)) {
						this.transferGoodTypeAmount(entry.getKey(), oldOwner,
								newOwnerHousehold, entry.getValue());
					}
				}
			}
		}

		// transfer all properties, eventually to null property owner!
		for (Property property : ApplicationContext.getInstance()
				.getPropertyDAO().findAllPropertiesOfPropertyOwner(oldOwner)) {
			this.transferProperty(property, oldOwner, newOwnerHousehold);
		}

		// remove good type ownerships as they should have been zeroed
		for (GoodTypeOwnership goodTypeOwnership : ApplicationContext
				.getInstance().getGoodTypeOwnershipDAO()
				.findAllByPropertyOwner(oldOwner)) {
			ApplicationContext.getInstance().getGoodTypeOwnershipFactory()
					.deleteGoodTypeOwnership(goodTypeOwnership);
		}

		assert (ApplicationContext.getInstance().getGoodTypeOwnershipDAO()
				.findAllByPropertyOwner(oldOwner).size() == 0);
	}
}
