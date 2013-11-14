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
import compecon.engine.dao.PropertyDAO;

public class PropertyDAOImpl extends AgentIndexedInMemoryDAOImpl<Property> implements
		PropertyDAO {

	@Override
	public synchronized List<Property> findAllByAgent(Agent agent) {
		if (this.getInstancesForAgent(agent) != null)
			return new ArrayList<Property>(this.getInstancesForAgent(agent));
		return new ArrayList<Property>();
	}

	@Override
	public synchronized void save(Property property) {
		super.save(property.getOwner(), property);
	}

	@Override
	public synchronized void transferProperty(Agent oldOwner, Agent newOwner,
			Property property) {
		// the property is deleted and re-saved, so that the
		// agent-property-index is updated
		this.delete(property);
		property.setOwner(newOwner);
		this.save(newOwner, property);
	}
}
