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

import compecon.math.production.CESProductionFunction;
import compecon.math.production.IProductionFunction;
import compecon.math.production.RootProductionFunction;
import compecon.math.utility.CESUtilityFunction;
import compecon.math.utility.IUtilityFunction;

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
			return new RootProductionFunction(GoodType.LABOURHOUR, 250.0);
		case COAL:
			new RootProductionFunction(GoodType.LABOURHOUR, 250.0);
		case GOLD:
			return new RootProductionFunction(GoodType.LABOURHOUR, 25.0);
		case URANIUM:
			return new RootProductionFunction(GoodType.LABOURHOUR, 25.0);
		case WHEAT:
			return new RootProductionFunction(GoodType.LABOURHOUR, 50.0);
		case STEEL:
			Map<GoodType, Double> parametersSteel = new LinkedHashMap<GoodType, Double>();
			parametersSteel.put(GoodType.KILOWATT, 0.7);
			parametersSteel.put(GoodType.LABOURHOUR, 0.1);
			parametersSteel.put(GoodType.IRON, 0.1);
			parametersSteel.put(GoodType.COAL, 0.1);
			return new CESProductionFunction(5.0, parametersSteel, -0.5, 0.4);
		case KILOWATT:
			Map<GoodType, Double> parametersKiloWatt = new LinkedHashMap<GoodType, Double>();
			parametersKiloWatt.put(GoodType.LABOURHOUR, 0.2);
			parametersKiloWatt.put(GoodType.URANIUM, 0.4);
			parametersKiloWatt.put(GoodType.COAL, 0.4);
			return new CESProductionFunction(5.0, parametersKiloWatt, -0.5, 0.4);
		case REALESTATE:
			Map<GoodType, Double> parametersRealEstate = new LinkedHashMap<GoodType, Double>();
			parametersRealEstate.put(GoodType.STEEL, 0.2);
			parametersRealEstate.put(GoodType.LABOURHOUR, 0.6);
			parametersRealEstate.put(GoodType.KILOWATT, 0.2);
			return new CESProductionFunction(5.0, parametersRealEstate, -0.5,
					0.4);
		case CAR:
			Map<GoodType, Double> parametersCar = new LinkedHashMap<GoodType, Double>();
			parametersCar.put(GoodType.STEEL, 0.2);
			parametersCar.put(GoodType.LABOURHOUR, 0.6);
			parametersCar.put(GoodType.KILOWATT, 0.2);
			return new CESProductionFunction(5.0, parametersCar, -0.5, 0.4);
		case LABOURHOUR:
			return null;
		default:
			return new RootProductionFunction(GoodType.LABOURHOUR, 100.0);
		}
	}

	public static IUtilityFunction getUtilityFunctionForHousehold() {
		// consumption preferences; each GoodType has to be contained here (at
		// least transitively via the input-output-model), so that the
		// corresponding price on the market
		// can come to an equilibrium; preference for labour hour has to be high
		// enough, so that labour hour prices do not fall endlessly
		Map<GoodType, Double> preferences = new LinkedHashMap<GoodType, Double>();
		preferences.put(GoodType.LABOURHOUR, 0.2);
		preferences.put(GoodType.WHEAT, 0.2);
		preferences.put(GoodType.KILOWATT, 0.1);
		preferences.put(GoodType.CAR, 0.2);
		preferences.put(GoodType.REALESTATE, 0.2);
		preferences.put(GoodType.GOLD, 0.1);
		return new CESUtilityFunction(1.0, preferences, -0.5, 0.4);
	}

	public static IUtilityFunction getUtilityFunctionForState() {
		Map<GoodType, Double> preferences = new LinkedHashMap<GoodType, Double>();
		preferences.put(GoodType.LABOURHOUR, 0.3);
		preferences.put(GoodType.KILOWATT, 0.2);
		preferences.put(GoodType.REALESTATE, 0.2);
		preferences.put(GoodType.GOLD, 0.3);
		return new CESUtilityFunction(1.0, preferences, -0.5, 0.4);
	}
}