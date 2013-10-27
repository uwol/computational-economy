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

package compecon.materia;

import java.util.LinkedHashMap;
import java.util.Map;

import compecon.math.production.IProductionFunction;
import compecon.math.production.RootProductionFunction;
import compecon.math.utility.CESUtilityFunction;
import compecon.math.utility.IUtilityFunction;

public class InputOutputModelSegemented implements IInputOutputModel {

	public IUtilityFunction getUtilityFunctionOfHousehold() {
		final Map<GoodType, Double> preferences = new LinkedHashMap<GoodType, Double>();
		preferences.put(GoodType.IRON, 0.2);
		preferences.put(GoodType.COAL, 0.2);
		preferences.put(GoodType.COTTON, 0.2);
		preferences.put(GoodType.WHEAT, 0.2);
		preferences.put(GoodType.FOOD, 0.2);
		preferences.put(GoodType.CLOTHING, 0.2);
		preferences.put(GoodType.REALESTATE, 0.2);
		preferences.put(GoodType.KILOWATT, 0.2);
		preferences.put(GoodType.LABOURHOUR, 0.2);
		return new CESUtilityFunction(1.0, preferences, -0.7, 0.5);
	}

	public IUtilityFunction getUtilityFunctionOfState() {
		final Map<GoodType, Double> preferences = new LinkedHashMap<GoodType, Double>();
		preferences.put(GoodType.IRON, 0.2);
		preferences.put(GoodType.COAL, 0.2);
		preferences.put(GoodType.COTTON, 0.2);
		preferences.put(GoodType.WHEAT, 0.2);
		preferences.put(GoodType.FOOD, 0.2);
		preferences.put(GoodType.CLOTHING, 0.2);
		preferences.put(GoodType.REALESTATE, 0.2);
		preferences.put(GoodType.KILOWATT, 0.2);
		preferences.put(GoodType.LABOURHOUR, 0.2);
		return new CESUtilityFunction(1.0, preferences, -0.7, 0.5);
	}

	public IProductionFunction getProductionFunction(
			final GoodType outputGoodType) {
		switch (outputGoodType) {
		case IRON:
			return new RootProductionFunction(GoodType.LABOURHOUR, 100.0);
		case COAL:
			return new RootProductionFunction(GoodType.LABOURHOUR, 100.0);
		case COTTON:
			return new RootProductionFunction(GoodType.LABOURHOUR, 100.0);
		case WHEAT:
			return new RootProductionFunction(GoodType.LABOURHOUR, 100.0);

		case KILOWATT:
			return new RootProductionFunction(GoodType.LABOURHOUR, 100.0);
		case CLOTHING:
			return new RootProductionFunction(GoodType.LABOURHOUR, 100.0);
		case FOOD:
			return new RootProductionFunction(GoodType.LABOURHOUR, 100.0);
		case REALESTATE:
			return new RootProductionFunction(GoodType.LABOURHOUR, 100.0);

		default:
			return null;
		}
	}
}