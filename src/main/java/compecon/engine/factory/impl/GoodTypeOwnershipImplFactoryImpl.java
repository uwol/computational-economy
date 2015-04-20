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

package compecon.engine.factory.impl;

import compecon.economy.property.GoodTypeOwnership;
import compecon.economy.property.PropertyOwner;
import compecon.economy.property.impl.GoodTypeOwnershipImpl;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.factory.GoodTypeOwnershipFactory;
import compecon.engine.util.HibernateUtil;

public class GoodTypeOwnershipImplFactoryImpl implements
		GoodTypeOwnershipFactory {

	@Override
	public void deleteGoodTypeOwnership(
			final GoodTypeOwnership goodTypeOwnership) {
		ApplicationContext.getInstance().getGoodTypeOwnershipDAO()
				.delete(goodTypeOwnership);
		HibernateUtil.flushSession();
	}

	@Override
	public GoodTypeOwnership newInstanceGoodTypeOwnership(
			final PropertyOwner propertyOwner) {
		assert (propertyOwner != null);

		final GoodTypeOwnershipImpl goodTypeOwnership = new GoodTypeOwnershipImpl();

		if (!HibernateUtil.isActive()) {
			goodTypeOwnership.setId(ApplicationContext.getInstance()
					.getSequenceNumberGenerator().getNextId());
		}

		goodTypeOwnership.setPropertyOwner(propertyOwner);
		ApplicationContext.getInstance().getGoodTypeOwnershipDAO()
				.save(goodTypeOwnership);
		HibernateUtil.flushSession();
		return goodTypeOwnership;
	}
}
