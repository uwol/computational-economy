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

	public static IUtilityFunction getUtilityFunctionForHousehold() {
		// consumption preferences; each GoodType has to be contained here (at
		// least transitively via the input-output-model), so that the
		// corresponding price on the market
		// can come to an equilibrium; preference for labour hour has to be high
		// enough, so that labour hour prices do not fall endlessly
		Map<GoodType, Double> preferences = new LinkedHashMap<GoodType, Double>();
		preferences.put(GoodType.IRON, 0.2);
		preferences.put(GoodType.COAL, 0.2);
		preferences.put(GoodType.COTTON, 0.2);
		preferences.put(GoodType.WHEAT, 0.2);
		preferences.put(GoodType.FOOD, 0.2);
		preferences.put(GoodType.CLOTHING, 0.2);
		preferences.put(GoodType.REALESTATE, 0.2);
		preferences.put(GoodType.KILOWATT, 0.2);
		preferences.put(GoodType.LABOURHOUR, 0.2);
		return new CESUtilityFunction(1.0, preferences, -0.9, 0.89);
	}

	public static IUtilityFunction getUtilityFunctionForState() {
		Map<GoodType, Double> preferences = new LinkedHashMap<GoodType, Double>();
		preferences.put(GoodType.IRON, 0.2);
		preferences.put(GoodType.COAL, 0.2);
		preferences.put(GoodType.COTTON, 0.2);
		preferences.put(GoodType.WHEAT, 0.2);
		preferences.put(GoodType.FOOD, 0.2);
		preferences.put(GoodType.CLOTHING, 0.2);
		preferences.put(GoodType.REALESTATE, 0.2);
		preferences.put(GoodType.KILOWATT, 0.2);
		preferences.put(GoodType.LABOURHOUR, 0.2);
		return new CESUtilityFunction(1.0, preferences, -0.9, 0.89);
	}

	public static IProductionFunction getProductionFunction(
			GoodType outputGoodType) {
		return getProductionFunctionCobbDouglasBased(outputGoodType);
	}

	public static IProductionFunction getProductionFunctionCobbDouglasBased(
			GoodType outputGoodType) {
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

		case CRAFT:
			return new RootProductionFunction(GoodType.LABOURHOUR, 100.0);
		case EDUCATION:
			return new RootProductionFunction(GoodType.LABOURHOUR, 100.0);
		case ADMINISTRATION:
			return new RootProductionFunction(GoodType.LABOURHOUR, 100.0);
		case CONSULTING:
			return new RootProductionFunction(GoodType.LABOURHOUR, 100.0);
		default:
			return null;
		}
	}

	public static IProductionFunction getProductionFunctionCESBased(
			GoodType outputGoodType) {
		switch (outputGoodType) {
		case IRON:
			Map<GoodType, Double> parametersIron = new LinkedHashMap<GoodType, Double>();
			parametersIron.put(GoodType.LABOURHOUR, 1.0);
			return new CESProductionFunction(50.0, parametersIron, -0.7, 0.65);
		case COAL:
			Map<GoodType, Double> parametersCoal = new LinkedHashMap<GoodType, Double>();
			parametersCoal.put(GoodType.LABOURHOUR, 1.0);
			return new CESProductionFunction(50.0, parametersCoal, -0.7, 0.65);
		case COTTON:
			Map<GoodType, Double> parametersSteel = new LinkedHashMap<GoodType, Double>();
			parametersSteel.put(GoodType.COAL, 0.25);
			parametersSteel.put(GoodType.LABOURHOUR, 0.25);
			parametersSteel.put(GoodType.KILOWATT, 0.25);
			return new CESProductionFunction(1.0, parametersSteel, -0.8, 0.7);
		case WHEAT:
			Map<GoodType, Double> parametersWheat = new LinkedHashMap<GoodType, Double>();
			parametersWheat.put(GoodType.LABOURHOUR, 1.0);
			return new CESProductionFunction(50.0, parametersWheat, -0.7, 0.65);

		case FOOD:
			Map<GoodType, Double> parametersFood = new LinkedHashMap<GoodType, Double>();
			parametersFood.put(GoodType.WHEAT, 0.9);
			parametersFood.put(GoodType.LABOURHOUR, 0.1);
			return new CESProductionFunction(1.0, parametersFood, -0.8, 0.7);
		case KILOWATT:
			Map<GoodType, Double> parametersKiloWatt = new LinkedHashMap<GoodType, Double>();
			parametersKiloWatt.put(GoodType.LABOURHOUR, 0.1);
			parametersKiloWatt.put(GoodType.COAL, 0.45);
			return new CESProductionFunction(1.0, parametersKiloWatt, -0.8, 0.7);
		case CLOTHING:
			Map<GoodType, Double> parametersClothing = new LinkedHashMap<GoodType, Double>();
			parametersClothing.put(GoodType.COTTON, 0.1);
			parametersClothing.put(GoodType.COAL, 0.45);
			return new CESProductionFunction(1.0, parametersClothing, -0.8, 0.7);
		case REALESTATE:
			Map<GoodType, Double> parametersRealEstate = new LinkedHashMap<GoodType, Double>();
			parametersRealEstate.put(GoodType.IRON, 0.3);
			parametersRealEstate.put(GoodType.LABOURHOUR, 0.5);
			return new CESProductionFunction(1.0, parametersRealEstate, -0.8,
					0.7);

		case CRAFT:
			Map<GoodType, Double> parametersCraft = new LinkedHashMap<GoodType, Double>();
			parametersCraft.put(GoodType.LABOURHOUR, 0.9);
			parametersCraft.put(GoodType.KILOWATT, 0.1);
			return new CESProductionFunction(1.0, parametersCraft, -0.9, 0.8);
		case EDUCATION:
			Map<GoodType, Double> parametersEducation = new LinkedHashMap<GoodType, Double>();
			parametersEducation.put(GoodType.LABOURHOUR, 0.9);
			parametersEducation.put(GoodType.KILOWATT, 0.1);
			return new CESProductionFunction(1.0, parametersEducation, -0.9,
					0.8);
		case ADMINISTRATION:
			Map<GoodType, Double> parametersAdministration = new LinkedHashMap<GoodType, Double>();
			parametersAdministration.put(GoodType.LABOURHOUR, 0.9);
			parametersAdministration.put(GoodType.KILOWATT, 0.1);
			return new CESProductionFunction(1.0, parametersAdministration,
					-0.9, 0.8);
		case CONSULTING:
			Map<GoodType, Double> parametersConsulting = new LinkedHashMap<GoodType, Double>();
			parametersConsulting.put(GoodType.LABOURHOUR, 0.9);
			parametersConsulting.put(GoodType.KILOWATT, 0.1);
			return new CESProductionFunction(1.0, parametersConsulting, -0.9,
					0.8);
		default:
			return null;
		}
	}
}