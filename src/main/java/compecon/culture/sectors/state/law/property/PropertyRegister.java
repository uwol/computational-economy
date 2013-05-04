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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import compecon.culture.sectors.household.Household;
import compecon.engine.util.MathUtil;
import compecon.nature.materia.GoodType;

/**
 * The property register manages property rights. Each
 * {@link compecon.culture.sectors.state.law.property.IProperty} can be assigned
 * to an owning agent and can be transfered between agents.
 */
public class PropertyRegister {

	private static PropertyRegister instance;

	private Map<IPropertyOwner, Set<Property>> iPropertiesOfIPropertyOwners = new HashMap<IPropertyOwner, Set<Property>>();

	private Map<IPropertyOwner, HashMap<GoodType, Double>> goodTypesOfIPropertyOwners = new HashMap<IPropertyOwner, HashMap<GoodType, Double>>();

	private PropertyRegister() {
		super();
	}

	public static PropertyRegister getInstance() {
		if (instance == null)
			instance = new PropertyRegister();
		return instance;
	}

	private void assertInitializedDataStructure(IPropertyOwner propertyOwner) {
		if (propertyOwner != null) {
			if (!this.goodTypesOfIPropertyOwners.containsKey(propertyOwner))
				this.goodTypesOfIPropertyOwners.put(propertyOwner,
						new HashMap<GoodType, Double>());
			if (!this.iPropertiesOfIPropertyOwners.containsKey(propertyOwner))
				this.iPropertiesOfIPropertyOwners.put(propertyOwner,
						new HashSet<Property>());
		}
	}

	/*
	 * get owners
	 */
	public double getBalance(IPropertyOwner propertyOwner, GoodType goodType) {
		this.assertInitializedDataStructure(propertyOwner);

		HashMap<GoodType, Double> balancesForPropertyOwner = this.goodTypesOfIPropertyOwners
				.get(propertyOwner);
		if (balancesForPropertyOwner.containsKey(goodType))
			return balancesForPropertyOwner.get(goodType);
		return 0;
	}

	public Set<Property> getIProperties(IPropertyOwner propertyOwner) {
		this.assertInitializedDataStructure(propertyOwner);

		return this.iPropertiesOfIPropertyOwners.get(propertyOwner);
	}

	public Map<IPropertyOwner, Double> getPropertyOwners(GoodType goodType) {
		Map<IPropertyOwner, Double> propertyOwners = new HashMap<IPropertyOwner, Double>();
		for (Entry<IPropertyOwner, HashMap<GoodType, Double>> entry : this.goodTypesOfIPropertyOwners
				.entrySet()) {
			for (Entry<GoodType, Double> entry2 : entry.getValue().entrySet()) {
				if (entry2.getKey() == goodType)
					propertyOwners.put(entry.getKey(), entry2.getValue());
			}
		}
		return propertyOwners;
	}

	public IPropertyOwner getPropertyOwner(Property property) {
		for (Entry<IPropertyOwner, Set<Property>> entry : this.iPropertiesOfIPropertyOwners
				.entrySet()) {
			if (entry.getValue().contains(property))
				return entry.getKey();
		}
		return null;
	}

	/*
	 * register and deregister IProperties
	 */

	public void register(IPropertyOwner newOwner, Property property) {
		this.transfer(null, newOwner, property);
	}

	public void deregister(Property property) {
		IPropertyOwner oldOwner = getPropertyOwner(property);
		if (this.iPropertiesOfIPropertyOwners.containsKey(oldOwner))
			this.iPropertiesOfIPropertyOwners.get(oldOwner).remove(property);
	}

	public void deregister(IPropertyOwner owner) {
		if (owner == null)
			return;

		// fetch all owners registered in this register
		ArrayList<IPropertyOwner> owners = new ArrayList<IPropertyOwner>(
				this.goodTypesOfIPropertyOwners.keySet());

		// fetch a random new owner
		IPropertyOwner newOwner;
		do {
			int position = new Random().nextInt(owners.size());
			newOwner = owners.get(position);
		} while (owners.size() > 1 && newOwner == owner
				&& !(newOwner instanceof Household));

		if (newOwner != null && newOwner != owner) {

			// does the owner own a good?
			if (this.goodTypesOfIPropertyOwners.containsKey(owner)) {
				// transfer all goods
				for (Entry<GoodType, Double> entry : this.goodTypesOfIPropertyOwners
						.get(owner).entrySet()) {
					if (!entry.getKey().equals(GoodType.LABOURHOUR))
						this.transfer(owner, newOwner, entry.getKey(),
								entry.getValue());
				}
			}
			// does the owner own at least one IProperty?
			if (this.iPropertiesOfIPropertyOwners.containsKey(owner)) {
				// transfer all IProperties, via a new HashSet to avoid
				// ConcurrentModificationException
				for (Property property : new HashSet<Property>(
						this.iPropertiesOfIPropertyOwners.get(owner))) {
					this.transfer(owner, newOwner, property);
				}
			}

		}

		this.goodTypesOfIPropertyOwners.remove(owner);
		this.iPropertiesOfIPropertyOwners.remove(owner);
	}

	/*
	 * modify amount of GoodType
	 */
	public double increment(IPropertyOwner propertyOwner, GoodType goodType,
			double amount) {
		this.assertInitializedDataStructure(propertyOwner);

		if (amount < 0)
			throw new RuntimeException("amount is too small");
		double oldBalance = this.getBalance(propertyOwner, goodType);
		double newBalance = oldBalance + amount;
		this.goodTypesOfIPropertyOwners.get(propertyOwner).put(goodType,
				newBalance);
		return newBalance;
	}

	public double decrement(IPropertyOwner propertyOwner, GoodType goodType,
			double amount) {
		this.assertInitializedDataStructure(propertyOwner);

		if (amount < 0)
			throw new RuntimeException("amount is negative");

		double oldBalance = this.getBalance(propertyOwner, goodType);

		if (oldBalance < amount && !MathUtil.equal(oldBalance, amount))
			throw new RuntimeException("not enough ressources of " + goodType
					+ " to remove " + amount + " units, amount in balance is "
					+ oldBalance);

		double newBalance = Math.max(oldBalance - amount, 0);
		this.goodTypesOfIPropertyOwners.get(propertyOwner).put(goodType,
				newBalance);
		return newBalance;
	}

	public void reset(IPropertyOwner propertyOwner, GoodType goodType) {
		this.assertInitializedDataStructure(propertyOwner);

		this.goodTypesOfIPropertyOwners.get(propertyOwner).put(goodType,
				new Double(0));
	}

	/*
	 * transfer
	 */
	public void transfer(IPropertyOwner oldOwner, IPropertyOwner newOwner,
			GoodType goodType, double amount) {
		this.assertInitializedDataStructure(oldOwner);
		this.assertInitializedDataStructure(newOwner);

		this.decrement(oldOwner, goodType, amount);
		this.increment(newOwner, goodType, amount);
	}

	public void transfer(IPropertyOwner oldOwner, IPropertyOwner newOwner,
			Property property, double amount) {
		this.transfer(oldOwner, newOwner, property);
	}

	public void transfer(IPropertyOwner oldOwner, IPropertyOwner newOwner,
			Property property) {
		this.assertInitializedDataStructure(oldOwner);
		this.assertInitializedDataStructure(newOwner);

		if (this.getPropertyOwner(property) == null) {
			this.iPropertiesOfIPropertyOwners.get(newOwner).add(property);
		} else if (this.getPropertyOwner(property) == oldOwner) {
			this.iPropertiesOfIPropertyOwners.get(oldOwner).remove(property);
			this.iPropertiesOfIPropertyOwners.get(newOwner).add(property);
		} else
			throw new RuntimeException("oldOwner " + oldOwner
					+ " does not own IProperty " + property);
	}
}
