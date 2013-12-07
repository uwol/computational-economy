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

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import compecon.economy.agent.impl.AgentImpl;
import compecon.economy.materia.GoodType;
import compecon.economy.property.GoodTypeOwnership;
import compecon.economy.property.PropertyOwner;

@Entity
@Table(name = "GoodTypeOwnership")
public class GoodTypeOwnershipImpl implements GoodTypeOwnership {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	protected int id;

	@OneToOne(targetEntity = AgentImpl.class)
	@JoinColumn(name = "propertyOwner_id", nullable = false)
	protected PropertyOwner propertyOwner;

	@ElementCollection
	@CollectionTable(name = "GoodTypeOwnership_OwnedGoodTypes", joinColumns = @JoinColumn(name = "goodtypeownership_id"))
	@MapKeyEnumerated(EnumType.STRING)
	private Map<GoodType, Double> ownedGoodTypes = new HashMap<GoodType, Double>();

	public GoodTypeOwnershipImpl() {
		for (GoodType goodType : GoodType.values())
			this.ownedGoodTypes.put(goodType, 0.0);
	}

	public int getId() {
		return id;
	}

	public Map<GoodType, Double> getOwnedGoodTypes() {
		return ownedGoodTypes;
	}

	public PropertyOwner getPropertyOwner() {
		return propertyOwner;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setOwnedGoodTypes(Map<GoodType, Double> ownedGoodTypes) {
		this.ownedGoodTypes = ownedGoodTypes;
	}

	public void setPropertyOwner(PropertyOwner propertyOwner) {
		this.propertyOwner = propertyOwner;
	}
}
