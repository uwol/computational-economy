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

package compecon.economy.sectors.state.law.property;

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

import compecon.engine.Agent;
import compecon.materia.GoodType;

@Entity
@Table(name = "GoodTypeOwnership")
public class GoodTypeOwnership {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	protected int id;

	@OneToOne
	@JoinColumn(name = "agent_id", nullable = false)
	protected Agent agent;

	@ElementCollection
	@CollectionTable(name = "GoodTypeOwnership_OwnedGoodTypes", joinColumns = @JoinColumn(name = "goodtypeownership_id"))
	@MapKeyEnumerated(EnumType.STRING)
	private Map<GoodType, Double> ownedGoodTypes = new HashMap<GoodType, Double>();

	public GoodTypeOwnership() {
		for (GoodType goodType : GoodType.values())
			this.ownedGoodTypes.put(goodType, 0.0);
	}

	public Agent getAgent() {
		return agent;
	}

	public int getId() {
		return id;
	}

	public Map<GoodType, Double> getOwnedGoodTypes() {
		return ownedGoodTypes;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setOwnedGoodTypes(Map<GoodType, Double> ownedGoodTypes) {
		this.ownedGoodTypes = ownedGoodTypes;
	}
}
