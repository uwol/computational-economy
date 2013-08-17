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

import compecon.math.production.CobbDouglasProductionFunction;
import compecon.math.production.IProductionFunction;
import compecon.math.production.RootProductionFunction;

/**
 * Factory class for production functions, which relates inputs / production
 * factors to outputs.
 * 
 * http://en.wikipedia.org/wiki/Input-output_model
 */
public class InputOutputModel {
	public static IProductionFunction getProductionFunction(
			GoodType outputGoodType) {
		switch (outputGoodType) {
		case IRON:
			return new RootProductionFunction(GoodType.LABOURHOUR, 1000);
		case COAL:
			new RootProductionFunction(GoodType.LABOURHOUR, 10000);
		case GOLD:
			return new RootProductionFunction(GoodType.LABOURHOUR, 100);
		case URANIUM:
			return new RootProductionFunction(GoodType.LABOURHOUR, 100);
		case WHEAT:
			return new RootProductionFunction(GoodType.LABOURHOUR, 1000);
		case STEEL:
			Map<GoodType, Double> parametersSteel = new LinkedHashMap<GoodType, Double>();
			parametersSteel.put(GoodType.KILOWATT, 0.7);
			parametersSteel.put(GoodType.LABOURHOUR, 0.1);
			parametersSteel.put(GoodType.IRON, 0.1);
			parametersSteel.put(GoodType.COAL, 0.1);
			return new CobbDouglasProductionFunction(parametersSteel, 5);
		case KILOWATT:
			Map<GoodType, Double> parametersKiloWatt = new LinkedHashMap<GoodType, Double>();
			parametersKiloWatt.put(GoodType.LABOURHOUR, 0.2);
			parametersKiloWatt.put(GoodType.URANIUM, 0.8);
			return new CobbDouglasProductionFunction(parametersKiloWatt, 20);
		case REALESTATE:
			Map<GoodType, Double> parametersRealEstate = new LinkedHashMap<GoodType, Double>();
			parametersRealEstate.put(GoodType.STEEL, 0.2);
			parametersRealEstate.put(GoodType.LABOURHOUR, 0.6);
			parametersRealEstate.put(GoodType.KILOWATT, 0.2);
			return new CobbDouglasProductionFunction(parametersRealEstate, 5);
		case CAR:
			Map<GoodType, Double> parametersCar = new LinkedHashMap<GoodType, Double>();
			parametersCar.put(GoodType.STEEL, 0.2);
			parametersCar.put(GoodType.LABOURHOUR, 0.6);
			parametersCar.put(GoodType.KILOWATT, 0.2);
			return new CobbDouglasProductionFunction(parametersCar, 5);
		case LABOURHOUR:
			return null;
		default:
			return new RootProductionFunction(GoodType.LABOURHOUR, 100);
		}
	}
}