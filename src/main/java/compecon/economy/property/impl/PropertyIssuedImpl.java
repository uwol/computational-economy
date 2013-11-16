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

package compecon.economy.property.impl;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.economy.agent.Agent;
import compecon.economy.agent.impl.AgentImpl;
import compecon.economy.property.PropertyIssued;

@Entity
public abstract class PropertyIssuedImpl extends PropertyImpl implements
		PropertyIssued {

	@ManyToOne(targetEntity = AgentImpl.class)
	@JoinColumn(name = "issuer_id")
	@Index(name = "IDX_P_ISSUER")
	protected Agent issuer;

	public Agent getIssuer() {
		return this.issuer;
	}

	public void setIssuer(Agent issuer) {
		this.issuer = issuer;
	}

	@Transient
	protected void assertValidIssuer() {
	}
}
