/*
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

package compecon.culture.sectors.state.law.property;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import compecon.engine.Agent;

@Entity
@Table(name = "PropertyOwnership")
public class PropertyOwnership {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	protected int id;

	@OneToOne
	@JoinColumn(name = "agent_id")
	protected Agent agent;

	@OneToMany
	@JoinTable(name = "PropertyOwnership_OwnedProperties", joinColumns = @JoinColumn(name = "propertyownership_id"), inverseJoinColumns = @JoinColumn(name = "property_id"))
	protected List<Property> ownedProperties = new ArrayList<Property>();

	public Agent getAgent() {
		return agent;
	}

	public int getId() {
		return id;
	}

	public List<Property> getOwnedProperties() {
		return ownedProperties;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setOwnedProperties(List<Property> ownedProperties) {
		this.ownedProperties = ownedProperties;
	}

}
