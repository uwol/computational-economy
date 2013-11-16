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

import compecon.economy.agent.Agent;
import compecon.economy.property.GoodTypeOwnership;
import compecon.economy.property.impl.GoodTypeOwnershipImpl;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.service.GoodTypeOwnershipService;
import compecon.engine.util.HibernateUtil;

public class GoodTypeOwnershipServiceImpl implements GoodTypeOwnershipService {

	@Override
	public GoodTypeOwnership newInstanceGoodTypeOwnership(Agent owner) {
		GoodTypeOwnership goodTypeOwnership = new GoodTypeOwnershipImpl();
		goodTypeOwnership.setAgent(owner);
		ApplicationContext.getInstance().getGoodTypeOwnershipDAO()
				.save(goodTypeOwnership);
		HibernateUtil.flushSession();
		return goodTypeOwnership;
	}

	@Override
	public void deleteGoodTypeOwnership(GoodTypeOwnership goodTypeOwnership) {
		ApplicationContext.getInstance().getGoodTypeOwnershipDAO()
				.delete(goodTypeOwnership);
		HibernateUtil.flushSession();
	}
}
