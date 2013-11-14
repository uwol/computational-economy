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
import compecon.economy.property.GoodTypeOwnership;
import compecon.engine.dao.GoodTypeOwnershipDAO;

public class GoodTypeOwnershipDAOImpl extends
		AgentIndexedInMemoryDAOImpl<GoodTypeOwnership> implements
		GoodTypeOwnershipDAO {

	@Override
	public synchronized List<GoodTypeOwnership> findAllByAgent(Agent agent) {
		if (this.getInstancesForAgent(agent) != null)
			return new ArrayList<GoodTypeOwnership>(
					this.getInstancesForAgent(agent));
		return new ArrayList<GoodTypeOwnership>();
	}

	@Override
	public synchronized GoodTypeOwnership findFirstByAgent(Agent agent) {
		List<GoodTypeOwnership> goodTypeOwnerships = this
				.getInstancesForAgent(agent);
		if (goodTypeOwnerships != null && !goodTypeOwnerships.isEmpty())
			return goodTypeOwnerships.get(0);
		return null;
	}

	@Override
	public synchronized void save(GoodTypeOwnership goodTypeOwnership) {
		super.save(goodTypeOwnership.getAgent(), goodTypeOwnership);
	}
}