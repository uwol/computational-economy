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

package compecon.nature.materia;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import compecon.nature.math.production.CobbDouglasProductionFunction;
import compecon.nature.math.production.IProductionFunction;
import compecon.nature.math.production.RootProductionFunction;

/**
 * Factory class for production functions, which relates inputs / production
 * factors to outputs.
 * 
 * http://en.wikipedia.org/wiki/Input-output_model
 */
public class InputOutputModel {
	protected static Map<GoodType, IProductionFunction> productionFunctionsForOutputGoodType = new HashMap<GoodType, IProductionFunction>();

	static {

		productionFunctionsForOutputGoodType.put(GoodType.IRON,
				new RootProductionFunction(GoodType.LABOURHOUR, 10000));

		productionFunctionsForOutputGoodType.put(GoodType.COAL,
				new RootProductionFunction(GoodType.LABOURHOUR, 10000));

		productionFunctionsForOutputGoodType.put(GoodType.GOLD,
				new RootProductionFunction(GoodType.LABOURHOUR, 100));

		productionFunctionsForOutputGoodType.put(GoodType.URANIUM,
				new RootProductionFunction(GoodType.LABOURHOUR, 100));

		productionFunctionsForOutputGoodType.put(GoodType.WHEAT,
				new RootProductionFunction(GoodType.LABOURHOUR, 1000));

		Map<GoodType, Double> parametersSteel = new LinkedHashMap<GoodType, Double>();
		parametersSteel.put(GoodType.KILOWATT, 0.7);
		parametersSteel.put(GoodType.LABOURHOUR, 0.1);
		parametersSteel.put(GoodType.IRON, 0.1);
		parametersSteel.put(GoodType.COAL, 0.1);
		productionFunctionsForOutputGoodType.put(GoodType.STEEL,
				new CobbDouglasProductionFunction(parametersSteel, 5));

		Map<GoodType, Double> parametersKiloWatt = new LinkedHashMap<GoodType, Double>();
		parametersKiloWatt.put(GoodType.LABOURHOUR, 0.2);
		parametersKiloWatt.put(GoodType.URANIUM, 0.8);
		productionFunctionsForOutputGoodType.put(GoodType.KILOWATT,
				new CobbDouglasProductionFunction(parametersKiloWatt, 20));

		Map<GoodType, Double> parametersRealEstate = new LinkedHashMap<GoodType, Double>();
		parametersRealEstate.put(GoodType.STEEL, 0.2);
		parametersRealEstate.put(GoodType.LABOURHOUR, 0.6);
		parametersRealEstate.put(GoodType.KILOWATT, 0.2);
		productionFunctionsForOutputGoodType.put(GoodType.REALESTATE,
				new CobbDouglasProductionFunction(parametersRealEstate, 5));

		Map<GoodType, Double> parametersCar = new LinkedHashMap<GoodType, Double>();
		parametersCar.put(GoodType.STEEL, 0.2);
		parametersCar.put(GoodType.LABOURHOUR, 0.6);
		parametersCar.put(GoodType.KILOWATT, 0.2);
		productionFunctionsForOutputGoodType.put(GoodType.CAR,
				new CobbDouglasProductionFunction(parametersCar, 5));

		// production functions for remaining output good types
		for (GoodType goodType : GoodType.values()) {
			if (!GoodType.LABOURHOUR.equals(goodType)) {
				if (!productionFunctionsForOutputGoodType.containsKey(goodType)) {
					productionFunctionsForOutputGoodType
							.put(goodType, new RootProductionFunction(
									GoodType.LABOURHOUR, 100));
				}
			}
		}
	}

	public static IProductionFunction getProductionFunction(
			GoodType outputGoodType) {
		return productionFunctionsForOutputGoodType.get(outputGoodType);
	}
}