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

package compecon.engine.service;

import java.util.List;
import java.util.Map;

import compecon.economy.agent.Agent;
import compecon.economy.materia.GoodType;
import compecon.economy.property.Property;

public interface PropertyService {

	/**
	 * @see #incrementGoodTypeAmount(Agent, GoodType, double)
	 */
	public double decrementGoodTypeAmount(Agent propertyOwner,
			GoodType goodType, double amount);

	public void deleteProperty(final Property property);

	public double getBalance(Agent agent, GoodType goodType);

	public Map<GoodType, Double> getBalance(Agent agent);

	public Agent getOwner(Property property);

	public List<Property> getProperties(Agent agent);

	public List<Property> getProperties(Agent agent,
			Class<? extends Property> propertyClass);

	/**
	 * @see #decrementGoodTypeAmount(Agent, GoodType, double)
	 */
	public double incrementGoodTypeAmount(Agent propertyOwner,
			GoodType goodType, double amount);

	public void resetGoodTypeAmount(Agent propertyOwner, GoodType goodType);

	public void transferGoodTypeAmount(Agent oldOwner, Agent newOwner,
			GoodType goodType, double amount);

	public void transferProperty(Agent oldOwner, Agent newOwner,
			Property property);

	public void transferEverythingToRandomAgent(Agent oldOwner);
}
