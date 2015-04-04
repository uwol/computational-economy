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

import compecon.economy.property.GoodTypeOwnership;
import compecon.economy.property.PropertyOwner;
import compecon.engine.dao.GoodTypeOwnershipDAO;

public class GoodTypeOwnershipDAOImpl extends
		AbstractIndexedInMemoryDAOImpl<PropertyOwner, GoodTypeOwnership>
		implements GoodTypeOwnershipDAO {

	@Override
	public synchronized List<GoodTypeOwnership> findAllByPropertyOwner(
			final PropertyOwner propertyOwner) {
		final List<GoodTypeOwnership> goodTypeOwnerships = getInstancesForKey(propertyOwner);
		if (goodTypeOwnerships != null) {
			return new ArrayList<GoodTypeOwnership>(goodTypeOwnerships);
		}
		return new ArrayList<GoodTypeOwnership>();
	}

	@Override
	public synchronized GoodTypeOwnership findFirstByPropertyOwner(
			final PropertyOwner propertyOwner) {
		final List<GoodTypeOwnership> goodTypeOwnerships = getInstancesForKey(propertyOwner);
		if (goodTypeOwnerships != null && !goodTypeOwnerships.isEmpty()) {
			return goodTypeOwnerships.get(0);
		}
		return null;
	}

	@Override
	public synchronized void save(final GoodTypeOwnership goodTypeOwnership) {
		super.save(goodTypeOwnership.getPropertyOwner(), goodTypeOwnership);
	}
}