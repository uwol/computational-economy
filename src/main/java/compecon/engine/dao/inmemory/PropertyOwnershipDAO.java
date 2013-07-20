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

package compecon.engine.dao.inmemory;

import java.util.ArrayList;
import java.util.List;

import compecon.culture.sectors.state.law.property.Property;
import compecon.culture.sectors.state.law.property.PropertyOwnership;
import compecon.engine.Agent;
import compecon.engine.dao.DAOFactory.IPropertyOwnershipDAO;

public class PropertyOwnershipDAO extends
		AgentIndexedInMemoryDAO<PropertyOwnership> implements
		IPropertyOwnershipDAO {

	@Override
	public synchronized void delete(PropertyOwnership propertyOwnership) {
		super.delete(propertyOwnership);
	}

	@Override
	public List<PropertyOwnership> findAllByAgent(Agent agent) {
		if (this.getInstancesForAgent(agent) != null)
			return new ArrayList<PropertyOwnership>(
					this.getInstancesForAgent(agent));
		return new ArrayList<PropertyOwnership>();
	}

	@Override
	public PropertyOwnership findFirstByAgent(Agent agent) {
		List<PropertyOwnership> propertyOwnerships = this
				.getInstancesForAgent(agent);
		if (propertyOwnerships != null && !propertyOwnerships.isEmpty())
			return propertyOwnerships.get(0);
		return null;
	}

	@Override
	public List<Agent> findOwners(Property property) {
		List<Agent> owners = new ArrayList<Agent>();
		for (PropertyOwnership propertyOwnership : this.findAll()) {
			if (propertyOwnership.getOwnedProperties().contains(property)) {
				owners.add(propertyOwnership.getAgent());
			}
		}
		return owners;
	}

	@Override
	public synchronized void save(PropertyOwnership propertyOwnership) {
		super.save(propertyOwnership.getAgent(), propertyOwnership);
	}

}
