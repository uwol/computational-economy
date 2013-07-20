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

package compecon.culture.sectors.state.law.property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import compecon.engine.Agent;
import compecon.engine.dao.DAOFactory;
import compecon.engine.util.HibernateUtil;
import compecon.engine.util.MathUtil;
import compecon.nature.materia.GoodType;

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

	protected PropertyOwnership assurePropertyOwnership(Agent agent) {
		PropertyOwnership propertyOwnership = DAOFactory
				.getPropertyOwnershipDAO().findFirstByAgent(agent);
		if (propertyOwnership == null) {
			propertyOwnership = new PropertyOwnership();
			propertyOwnership.setAgent(agent);
			DAOFactory.getPropertyOwnershipDAO().save(propertyOwnership);
			HibernateUtil.flushSession();
		}
		return propertyOwnership;
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
		return DAOFactory.getPropertyOwnershipDAO().findOwners(property).get(0);
	}

	public List<Agent> getOwners(Property property) {
		return DAOFactory.getPropertyOwnershipDAO().findOwners(property);
	}

	public List<Property> getProperties(Agent agent) {
		assurePropertyOwnership(agent);

		return DAOFactory.getPropertyOwnershipDAO().findFirstByAgent(agent)
				.getOwnedProperties();
	}

	public List<Property> getProperties(Agent agent,
			Class<? extends Property> propertyClass) {
		assurePropertyOwnership(agent);

		List<Property> propertiesOfClass = new ArrayList<Property>();
		for (Property property : DAOFactory.getPropertyOwnershipDAO()
				.findFirstByAgent(agent).getOwnedProperties()) {
			if (property.getClass() == propertyClass)
				propertiesOfClass.add(property);
		}
		return propertiesOfClass;
	}

	/*
	 * register and deregister IProperties
	 */

	public void registerProperty(Agent owner, Property property) {
		PropertyOwnership propertyOwnership = assurePropertyOwnership(owner);

		propertyOwnership.getOwnedProperties().add(property);
	}

	public void deregisterProperty(Agent owner, Property property) {
		PropertyOwnership propertyOwnership = assurePropertyOwnership(owner);
		propertyOwnership.getOwnedProperties().remove(property);
	}

	public void deregisterAllProperties(Agent oldOwner) {
		if (oldOwner == null)
			return;

		// fetch a random new owner
		Agent newOwner = oldOwner;
		while (DAOFactory.getHouseholdDAO().findAll().size() > 1
				&& newOwner == oldOwner) {
			newOwner = DAOFactory.getHouseholdDAO().findRandom();
		}

		assureGoodTypeOwnership(newOwner);
		assurePropertyOwnership(newOwner);

		// transfer all goods
		for (GoodTypeOwnership goodTypeOwnership : DAOFactory
				.getGoodTypeOwnershipDAO().findAllByAgent(oldOwner)) {
			if (newOwner != null && newOwner != oldOwner) {
				for (Entry<GoodType, Double> entry : goodTypeOwnership
						.getOwnedGoodTypes().entrySet()) {
					if (!entry.getKey().equals(GoodType.LABOURHOUR))
						this.transferGoodTypeAmount(oldOwner, newOwner,
								entry.getKey(), entry.getValue());
				}
			}

			DAOFactory.getGoodTypeOwnershipDAO().delete(goodTypeOwnership);
		}

		// transfer all Properties, via a new HashSet to avoid
		// ConcurrentModificationException
		for (PropertyOwnership propertyOwnership : DAOFactory
				.getPropertyOwnershipDAO().findAllByAgent(oldOwner)) {
			if (newOwner != null && newOwner != oldOwner) {
				for (Property property : new ArrayList<Property>(
						propertyOwnership.getOwnedProperties())) {
					this.transferProperty(oldOwner, newOwner, property);
				}
			}

			DAOFactory.getPropertyOwnershipDAO().delete(propertyOwnership);
		}
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

	public void transferProperty(Agent oldOwner, Agent newOwner,
			Property property) {
		if (newOwner == null)
			throw new RuntimeException("newOwner is " + newOwner);

		// the oldOwner is not derived from the property, as this is a expensive
		// operation

		if (oldOwner == null) {
			PropertyOwnership propertyOwnership = assurePropertyOwnership(newOwner);
			propertyOwnership.getOwnedProperties().add(property);
		} else {
			// deregister (first deregister, then register -> db unique
			// constraint of property ownership not violated)
			this.deregisterProperty(oldOwner, property);
			HibernateUtil.flushSession();

			// register
			PropertyOwnership propertyOwnership = assurePropertyOwnership(newOwner);
			propertyOwnership.getOwnedProperties().add(property);
		}

		HibernateUtil.flushSession();
	}
}
