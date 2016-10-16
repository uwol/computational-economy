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

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

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
@Entity
@Table(name = "Property")
@org.hibernate.annotations.Table(appliesTo = "Property", indexes = { @Index(name = "IDX_P_DTYPE", columnNames = { "DTYPE" }) })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE")
public abstract class PropertyImpl implements Property {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	protected int id;

	@Column(name = "isDeconstructed")
	protected boolean isDeconstructed = false;

	@ManyToOne(targetEntity = AgentImpl.class)
	@Index(name = "IDX_P_OWNER")
	@JoinColumn(name = "owner_id")
	protected PropertyOwner owner;

	@Transient
	protected void assertValidOwner() {
	}

	@Override
	@Transient
	public void deconstruct() {
		isDeconstructed = true;
		ApplicationContext.getInstance().getPropertyService()
				.deleteProperty(this);
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
	@Transient
	public void initialize() {
	}

	@Override
	public boolean isDeconstructed() {
		return isDeconstructed;
	}

	@Override
	@Transient
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
