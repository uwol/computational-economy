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

package compecon.economy.sectors.state.law.property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import compecon.economy.sectors.household.Household;
import compecon.engine.Agent;
import compecon.engine.dao.DAOFactory;
import compecon.engine.util.HibernateUtil;
import compecon.engine.util.MathUtil;
import compecon.materia.GoodType;

/**
 * The property register manages property rights. Each
 * {@link compecon.culture.sectors.state.law.property.IProperty} can be assigned
 * to an owning agent and can be transfered between agents.
 */
public class PropertyRegister {

	private static PropertyRegister instance;

	private PropertyRegister() {
		super();
	}

	public static PropertyRegister getInstance() {
		if (instance == null)
			instance = new PropertyRegister();
		return instance;
	}

	/*
	 * assures
	 */

	protected GoodTypeOwnership assureGoodTypeOwnership(Agent agent) {
		if (agent == null)
			throw new RuntimeException("agent is " + agent);

		GoodTypeOwnership goodTypeOwnership = DAOFactory
				.getGoodTypeOwnershipDAO().findFirstByAgent(agent);
		if (goodTypeOwnership == null) {
			goodTypeOwnership = new GoodTypeOwnership();
			goodTypeOwnership.setAgent(agent);
			DAOFactory.getGoodTypeOwnershipDAO().save(goodTypeOwnership);
			HibernateUtil.flushSession();
		}
		return goodTypeOwnership;
	}

	/*
	 * get owners
	 */

	public double getBalance(Agent agent, GoodType goodType) {
		assureGoodTypeOwnership(agent);

		GoodTypeOwnership goodTypeOwnership = DAOFactory
				.getGoodTypeOwnershipDAO().findFirstByAgent(agent);
		return goodTypeOwnership.getOwnedGoodTypes().get(goodType);
	}

	public Map<GoodType, Double> getBalance(Agent agent) {
		assureGoodTypeOwnership(agent);

		return DAOFactory.getGoodTypeOwnershipDAO().findFirstByAgent(agent)
				.getOwnedGoodTypes();
	}

	public Agent getOwner(Property property) {
		return property.getOwner();
	}

	public List<Property> getProperties(Agent agent) {
		return DAOFactory.getPropertyDAO().findAllByAgent(agent);
	}

	public List<Property> getProperties(Agent agent,
			Class<? extends Property> propertyClass) {
		List<Property> propertiesOfClass = new ArrayList<Property>();
		for (Property property : DAOFactory.getPropertyDAO().findAllByAgent(
				agent)) {
			if (property.getClass() == propertyClass)
				propertiesOfClass.add(property);
		}
		return propertiesOfClass;
	}

	/*
	 * modify owned amount of GoodType
	 */
	public double incrementGoodTypeAmount(Agent propertyOwner,
			GoodType goodType, double amount) {
		if (amount < 0)
			throw new RuntimeException("amount is too small");

		GoodTypeOwnership goodTypeOwnership = assureGoodTypeOwnership(propertyOwner);
		double newBalance = goodTypeOwnership.getOwnedGoodTypes().get(goodType)
				+ amount;
		goodTypeOwnership.getOwnedGoodTypes().put(goodType, newBalance);

		HibernateUtil.flushSession();

		return newBalance;
	}

	public double decrementGoodTypeAmount(Agent propertyOwner,
			GoodType goodType, double amount) {
		if (amount < 0)
			throw new RuntimeException("amount is negative");

		GoodTypeOwnership goodTypeOwnership = assureGoodTypeOwnership(propertyOwner);
		double oldBalance = goodTypeOwnership.getOwnedGoodTypes().get(goodType);
		if (oldBalance < amount && !MathUtil.equal(oldBalance, amount))
			throw new RuntimeException("not enough ressources of " + goodType
					+ " to remove " + amount + " units, amount in balance is "
					+ oldBalance);

		double newBalance = Math.max(oldBalance - amount, 0);
		goodTypeOwnership.getOwnedGoodTypes().put(goodType, newBalance);

		HibernateUtil.flushSession();

		return newBalance;
	}

	public void resetGoodTypeAmount(Agent propertyOwner, GoodType goodType) {
		GoodTypeOwnership goodTypeOwnership = assureGoodTypeOwnership(propertyOwner);
		goodTypeOwnership.getOwnedGoodTypes().put(goodType, 0.0);
	}

	/*
	 * transfer
	 */
	public void transferGoodTypeAmount(Agent oldOwner, Agent newOwner,
			GoodType goodType, double amount) {
		this.decrementGoodTypeAmount(oldOwner, goodType, amount);
		this.incrementGoodTypeAmount(newOwner, goodType, amount);
	}

	/**
	 * newOwner with value null is allowed, e. g. for shares
	 */
	public void transferProperty(Agent oldOwner, Agent newOwner,
			Property property) {
		// consistency check
		if (oldOwner != property.getOwner())
			throw new RuntimeException("oldOwner is not correct");

		DAOFactory.getPropertyDAO().transferProperty(oldOwner, newOwner,
				property);
	}

	public void transferEverythingToRandomAgent(Agent oldOwner) {
		if (oldOwner == null)
			return;

		// fetch a random new owner
		Household newOwnerHousehold = null;
		while ((newOwnerHousehold == null || oldOwner == newOwnerHousehold)
				&& DAOFactory.getHouseholdDAO().findAll().size() > 1) {
			newOwnerHousehold = DAOFactory.getHouseholdDAO().findRandom();
		}

		if (newOwnerHousehold == oldOwner)
			throw new RuntimeException("invalid state");

		if (newOwnerHousehold != null && newOwnerHousehold.isDeconstructed())
			throw new RuntimeException(
					"invalid household selected as new owner");

		// transfer all goods
		if (newOwnerHousehold != null) {
			assureGoodTypeOwnership(newOwnerHousehold);

			for (GoodTypeOwnership goodTypeOwnership : DAOFactory
					.getGoodTypeOwnershipDAO().findAllByAgent(oldOwner)) {

				for (Entry<GoodType, Double> entry : goodTypeOwnership
						.getOwnedGoodTypes().entrySet()) {
					if (!entry.getKey().equals(GoodType.LABOURHOUR))
						this.transferGoodTypeAmount(oldOwner,
								newOwnerHousehold, entry.getKey(),
								entry.getValue());
				}
			}
		}

		// transfer all properties, eventually to null!
		for (Property property : DAOFactory.getPropertyDAO().findAllByAgent(
				oldOwner)) {
			this.transferProperty(oldOwner, newOwnerHousehold, property);
		}

		// remove good type ownerships as they should have been zeroed
		for (GoodTypeOwnership goodTypeOwnership : DAOFactory
				.getGoodTypeOwnershipDAO().findAllByAgent(oldOwner)) {
			DAOFactory.getGoodTypeOwnershipDAO().delete(goodTypeOwnership);
		}
	}
}
