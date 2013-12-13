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

import compecon.economy.materia.GoodType;
import compecon.economy.property.Property;
import compecon.economy.property.PropertyOwner;

public interface PropertyService {

	/**
	 * @see #incrementGoodTypeAmount(PropertyOwner, GoodType, double)
	 */
	public double decrementGoodTypeAmount(final PropertyOwner propertyOwner,
			final GoodType goodType, double amount);

	public void deleteProperty(final Property property);

	public double getBalance(final PropertyOwner propertyOwner,
			final GoodType goodType);

	public Map<GoodType, Double> getBalances(final PropertyOwner propertyOwner);

	public List<Property> getProperties(final PropertyOwner propertyOwner);

	public List<Property> getProperties(final PropertyOwner propertyOwner,
			final Class<? extends Property> propertyClass);

	/**
	 * @see #decrementGoodTypeAmount(PropertyOwner, GoodType, double)
	 */
	public double incrementGoodTypeAmount(final PropertyOwner propertyOwner,
			final GoodType goodType, final double amount);

	public void resetGoodTypeAmount(final PropertyOwner propertyOwner,
			final GoodType goodType);

	public void transferGoodTypeAmount(final GoodType goodType,
			final PropertyOwner oldOwner, final PropertyOwner newOwner,
			final double amount);

	public void transferProperty(final Property property,
			final PropertyOwner oldOwner, final PropertyOwner newOwner);

	public void transferEverythingToRandomAgent(final PropertyOwner oldOwner);
}
