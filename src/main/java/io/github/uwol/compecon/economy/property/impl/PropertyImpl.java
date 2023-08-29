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

package io.github.uwol.compecon.economy.property.impl;

import io.github.uwol.compecon.economy.agent.impl.AgentImpl;
import io.github.uwol.compecon.economy.property.Property;
import io.github.uwol.compecon.economy.property.PropertyOwner;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.dao.PropertyDAO;

/**
 * property life cycle is managed by the initial property creator, i. e. when
 * the property creator is deconstructed, the property should be deconstructed,
 * too. Thus, a property is not deconstructed, when its property owner or its
 * {@link PropertyOwnership} is deconstructed.
 */
public abstract class PropertyImpl implements Property {

	protected int id;

	protected boolean isDeconstructed = false;

	protected PropertyOwner owner;

	protected void assertValidOwner() {
	}

	@Override
	public void deconstruct() {
		isDeconstructed = true;
		ApplicationContext.getInstance().getPropertyService().deleteProperty(this);
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public PropertyOwner getOwner() {
		return owner;
	}

	@Override
	public void initialize() {
	}

	@Override
	public boolean isDeconstructed() {
		return isDeconstructed;
	}

	@Override
	public void resetOwner() {
		owner = null;
	}

	public void setDeconstructed(final boolean isDeconstructed) {
		this.isDeconstructed = isDeconstructed;
	}

	public void setId(final int id) {
		this.id = id;
	}

	/**
	 * only to be called via
	 * {@link PropertyDAO#transferProperty(AgentImpl, AgentImpl, Property)}
	 */
	@Override
	public void setOwner(final PropertyOwner owner) {
		this.owner = owner;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": owner=[" + owner + "]";
	}
}
