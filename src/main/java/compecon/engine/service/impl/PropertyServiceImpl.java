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
import compecon.economy.sectors.household.Household;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.service.PropertyService;
import compecon.engine.util.HibernateUtil;
import compecon.math.util.MathUtil;

/**
 * The property service manages property rights. Each {@link Property} can be
 * assigned to an owning agent and can be transfered between agents.
 */
public class PropertyServiceImpl implements PropertyService {

	/*
	 * assures
	 */

	protected GoodTypeOwnership assureGoodTypeOwnership(Agent agent) {
		assert (agent != null);

		final GoodTypeOwnership goodTypeOwnership = ApplicationContext
				.getInstance().getGoodTypeOwnershipDAO()
				.findFirstByAgent(agent);
		if (goodTypeOwnership == null) {
			assert (!agent.isDeconstructed());
			return ApplicationContext.getInstance()
					.getGoodTypeOwnershipFactory()
					.newInstanceGoodTypeOwnership(agent);
		}
		return goodTypeOwnership;
	}

	public double decrementGoodTypeAmount(Agent propertyOwner,
			GoodType goodType, double amount) {
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

	/*
	 * get owners
	 */

	public double getBalance(Agent agent, GoodType goodType) {
		assureGoodTypeOwnership(agent);

		GoodTypeOwnership goodTypeOwnership = ApplicationContext.getInstance()
				.getGoodTypeOwnershipDAO().findFirstByAgent(agent);
		return goodTypeOwnership.getOwnedGoodTypes().get(goodType);
	}

	public Map<GoodType, Double> getBalance(Agent agent) {
		assureGoodTypeOwnership(agent);

		return ApplicationContext.getInstance().getGoodTypeOwnershipDAO()
				.findFirstByAgent(agent).getOwnedGoodTypes();
	}

	public Agent getOwner(Property property) {
		return property.getOwner();
	}

	public List<Property> getProperties(Agent agent) {
		return ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfAgent(agent);
	}

	public List<Property> getProperties(Agent agent,
			Class<? extends Property> propertyClass) {
		return ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfAgent(agent, propertyClass);
	}

	public double incrementGoodTypeAmount(Agent propertyOwner,
			GoodType goodType, double amount) {
		assert (amount >= 0.0);

		GoodTypeOwnership goodTypeOwnership = assureGoodTypeOwnership(propertyOwner);
		double newBalance = goodTypeOwnership.getOwnedGoodTypes().get(goodType)
				+ amount;
		goodTypeOwnership.getOwnedGoodTypes().put(goodType, newBalance);

		HibernateUtil.flushSession();

		return newBalance;
	}

	public void resetGoodTypeAmount(Agent propertyOwner, GoodType goodType) {
		GoodTypeOwnership goodTypeOwnership = assureGoodTypeOwnership(propertyOwner);
		goodTypeOwnership.getOwnedGoodTypes().put(goodType, 0.0);

		HibernateUtil.flushSession();
	}

	/*
	 * transfer
	 */
	public void transferGoodTypeAmount(Agent oldOwner, Agent newOwner,
			GoodType goodType, double amount) {
		this.decrementGoodTypeAmount(oldOwner, goodType, amount);
		this.incrementGoodTypeAmount(newOwner, goodType, amount);

		HibernateUtil.flushSession();
	}

	/**
	 * newOwner with value null is allowed, e. g. for shares
	 */
	public void transferProperty(Agent oldOwner, Agent newOwner,
			Property property) {
		// consistency check
		assert (oldOwner == property.getOwner());

		property.resetOwner();
		ApplicationContext.getInstance().getPropertyDAO()
				.transferProperty(oldOwner, newOwner, property);

		HibernateUtil.flushSession();
	}

	public void transferEverythingToRandomAgent(Agent oldOwner) {
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
					.findAllByAgent(oldOwner)) {
				for (Entry<GoodType, Double> entry : goodTypeOwnership
						.getOwnedGoodTypes().entrySet()) {
					if (!entry.getKey().equals(GoodType.LABOURHOUR)) {
						this.transferGoodTypeAmount(oldOwner,
								newOwnerHousehold, entry.getKey(),
								entry.getValue());
					}
				}
			}
		}

		// transfer all properties, eventually to null!
		for (Property property : ApplicationContext.getInstance()
				.getPropertyDAO().findAllPropertiesOfAgent(oldOwner)) {
			// FIXME set bank account delegates of new owner
			this.transferProperty(oldOwner, newOwnerHousehold, property);
		}

		// remove good type ownerships as they should have been zeroed
		for (GoodTypeOwnership goodTypeOwnership : ApplicationContext
				.getInstance().getGoodTypeOwnershipDAO()
				.findAllByAgent(oldOwner)) {
			ApplicationContext.getInstance().getGoodTypeOwnershipFactory()
					.deleteGoodTypeOwnership(goodTypeOwnership);
		}

		assert (ApplicationContext.getInstance().getGoodTypeOwnershipDAO()
				.findAllByAgent(oldOwner).size() == 0);
	}
}
