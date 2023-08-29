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

package io.github.uwol.compecon.engine.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.github.uwol.compecon.economy.agent.Agent;
import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.property.GoodTypeOwnership;
import io.github.uwol.compecon.economy.property.Property;
import io.github.uwol.compecon.economy.property.PropertyIssued;
import io.github.uwol.compecon.economy.property.PropertyOwner;
import io.github.uwol.compecon.economy.sectors.household.Household;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.service.PropertyService;
import io.github.uwol.compecon.math.util.MathUtil;

/**
 * The property service manages property rights. Each {@link Property} can be
 * assigned to an property owner and can be transfered between owners.
 */
public class PropertyServiceImpl implements PropertyService {

	/*
	 * assures
	 */

	protected GoodTypeOwnership assureGoodTypeOwnership(final PropertyOwner propertyOwner) {
		assert (propertyOwner != null);

		final GoodTypeOwnership goodTypeOwnership = ApplicationContext.getInstance().getGoodTypeOwnershipDAO()
				.findFirstByPropertyOwner(propertyOwner);
		if (goodTypeOwnership == null) {
			assert (!propertyOwner.isDeconstructed());
			return ApplicationContext.getInstance().getGoodTypeOwnershipFactory()
					.newInstanceGoodTypeOwnership(propertyOwner);
		}
		return goodTypeOwnership;
	}

	@Override
	public double decrementGoodTypeAmount(final PropertyOwner propertyOwner, final GoodType goodType,
			final double amount) {
		assert (amount >= 0.0);

		final GoodTypeOwnership goodTypeOwnership = assureGoodTypeOwnership(propertyOwner);
		final double oldBalance = goodTypeOwnership.getOwnedGoodTypes().get(goodType);

		assert (MathUtil.lesserEqual(amount, oldBalance))
				: "cannot decrement " + amount + " from " + oldBalance + " " + goodType;

		final double newBalance = Math.max(oldBalance - amount, 0);
		goodTypeOwnership.getOwnedGoodTypes().put(goodType, newBalance);

		return newBalance;
	}

	@Override
	public void deleteProperty(final Property property) {
		ApplicationContext.getInstance().getPropertyDAO().delete(property);
	}

	@Override
	public List<Property> findAllPropertiesIssuedByAgent(final Agent issuer) {
		return ApplicationContext.getInstance().getPropertyDAO().findAllPropertiesIssuedByAgent(issuer);
	}

	@Override
	public List<Property> findAllPropertiesIssuedByAgent(final Agent issuer,
			final Class<? extends PropertyIssued> propertyClass) {
		return ApplicationContext.getInstance().getPropertyDAO().findAllPropertiesIssuedByAgent(issuer, propertyClass);
	}

	@Override
	public List<Property> findAllPropertiesOfPropertyOwner(final PropertyOwner propertyOwner) {
		return ApplicationContext.getInstance().getPropertyDAO().findAllPropertiesOfPropertyOwner(propertyOwner);
	}

	@Override
	public List<Property> findAllPropertiesOfPropertyOwner(final PropertyOwner propertyOwner,
			final Class<? extends Property> propertyClass) {
		return ApplicationContext.getInstance().getPropertyDAO().findAllPropertiesOfPropertyOwner(propertyOwner,
				propertyClass);
	}

	@Override
	public Map<GoodType, Double> getCapitalBalances(final PropertyOwner propertyOwner) {
		final Map<GoodType, Double> capital = new HashMap<GoodType, Double>();
		for (final Entry<GoodType, Double> entry : getGoodTypeBalances(propertyOwner).entrySet()) {
			if (entry.getKey().isDurable()) {
				capital.put(entry.getKey(), entry.getValue());
			}
		}
		return capital;
	}

	@Override
	public double getGoodTypeBalance(final PropertyOwner propertyOwner, final GoodType goodType) {
		assureGoodTypeOwnership(propertyOwner);

		final GoodTypeOwnership goodTypeOwnership = ApplicationContext.getInstance().getGoodTypeOwnershipDAO()
				.findFirstByPropertyOwner(propertyOwner);
		return goodTypeOwnership.getOwnedGoodTypes().get(goodType);
	}

	@Override
	public Map<GoodType, Double> getGoodTypeBalances(final PropertyOwner propertyOwner) {
		assureGoodTypeOwnership(propertyOwner);

		return new HashMap<GoodType, Double>(ApplicationContext.getInstance().getGoodTypeOwnershipDAO()
				.findFirstByPropertyOwner(propertyOwner).getOwnedGoodTypes());
	}

	@Override
	public double incrementGoodTypeAmount(final PropertyOwner propertyOwner, final GoodType goodType,
			final double amount) {
		assert (amount >= 0.0);

		final GoodTypeOwnership goodTypeOwnership = assureGoodTypeOwnership(propertyOwner);
		final double newBalance = goodTypeOwnership.getOwnedGoodTypes().get(goodType) + amount;
		goodTypeOwnership.getOwnedGoodTypes().put(goodType, newBalance);

		return newBalance;
	}

	@Override
	public void resetGoodTypeAmount(final PropertyOwner propertyOwner, final GoodType goodType) {
		final GoodTypeOwnership goodTypeOwnership = assureGoodTypeOwnership(propertyOwner);
		goodTypeOwnership.getOwnedGoodTypes().put(goodType, 0.0);
	}

	@Override
	public void transferEverythingToRandomAgent(final PropertyOwner oldOwner) {
		if (oldOwner == null) {
			return;
		}

		// fetch a random new owner
		Household newOwnerHousehold = null;
		while ((newOwnerHousehold == null || oldOwner == newOwnerHousehold)
				&& ApplicationContext.getInstance().getHouseholdDAO().findAll().size() > 1) {
			newOwnerHousehold = ApplicationContext.getInstance().getHouseholdDAO().findRandom();
		}

		assert (newOwnerHousehold != oldOwner);
		assert (newOwnerHousehold == null || !newOwnerHousehold.isDeconstructed());

		// transfer all goods
		if (newOwnerHousehold != null) {
			assureGoodTypeOwnership(newOwnerHousehold);

			for (final GoodTypeOwnership goodTypeOwnership : ApplicationContext.getInstance().getGoodTypeOwnershipDAO()
					.findAllByPropertyOwner(oldOwner)) {
				for (final Entry<GoodType, Double> entry : goodTypeOwnership.getOwnedGoodTypes().entrySet()) {
					if (!entry.getKey().equals(GoodType.LABOURHOUR)) {
						transferGoodTypeAmount(entry.getKey(), oldOwner, newOwnerHousehold, entry.getValue());
					}
				}
			}
		}

		// transfer all properties, eventually to null property owner!
		for (final Property property : ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(oldOwner)) {
			transferProperty(property, oldOwner, newOwnerHousehold);
		}

		// remove good type ownerships as they should have been zeroed
		for (final GoodTypeOwnership goodTypeOwnership : ApplicationContext.getInstance().getGoodTypeOwnershipDAO()
				.findAllByPropertyOwner(oldOwner)) {
			ApplicationContext.getInstance().getGoodTypeOwnershipFactory().deleteGoodTypeOwnership(goodTypeOwnership);
		}

		assert (ApplicationContext.getInstance().getGoodTypeOwnershipDAO().findAllByPropertyOwner(oldOwner)
				.size() == 0);
	}

	@Override
	public void transferGoodTypeAmount(final GoodType goodType, final PropertyOwner oldOwner,
			final PropertyOwner newOwner, final double amount) {
		decrementGoodTypeAmount(oldOwner, goodType, amount);
		incrementGoodTypeAmount(newOwner, goodType, amount);
	}

	/**
	 * newOwner with value null is allowed, e. g. for shares
	 */
	@Override
	public void transferProperty(final Property property, final PropertyOwner oldOwner, final PropertyOwner newOwner) {

		// consistency check
		assert (oldOwner == property.getOwner());

		property.resetOwner();
		ApplicationContext.getInstance().getPropertyDAO().transferProperty(oldOwner, newOwner, property);
		oldOwner.onPropertyTransferred(property, oldOwner, newOwner);
		if (newOwner != null) {
			newOwner.onPropertyTransferred(property, oldOwner, newOwner);
		}
	}
}
