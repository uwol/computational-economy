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

package compecon.economy.materia.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import compecon.economy.materia.GoodType;
import compecon.economy.materia.InputOutputModel;
import compecon.math.production.ProductionFunction;
import compecon.math.production.impl.CESProductionFunctionImpl;
import compecon.math.production.impl.RootProductionFunctionImpl;
import compecon.math.utility.UtilityFunction;
import compecon.math.utility.impl.CESUtilityFunctionImpl;

public class InputOutputModelTestingImpl implements InputOutputModel {

	@Override
	public UtilityFunction getUtilityFunctionOfHousehold() {
		final Map<GoodType, Double> preferences = new LinkedHashMap<GoodType, Double>();
		preferences.put(GoodType.WHEAT, 0.3);
		preferences.put(GoodType.COAL, 0.3);
		preferences.put(GoodType.LABOURHOUR, 0.3);
		return new CESUtilityFunctionImpl(1.0, preferences, -0.7, 0.5);
	}

	@Override
	public ProductionFunction getProductionFunction(
			final GoodType outputGoodType) {
		switch (outputGoodType) {
		case MACHINE:
			return new RootProductionFunctionImpl(GoodType.LABOURHOUR, 5.0);

		case WHEAT:
			Map<GoodType, Double> parametersWheat = new LinkedHashMap<GoodType, Double>();
			parametersWheat.put(GoodType.LABOURHOUR, 1.0);
			parametersWheat.put(GoodType.MACHINE, 0.2);
			return new CESProductionFunctionImpl(50.0, parametersWheat, -0.7,
					0.65);
		case COAL:
			Map<GoodType, Double> parametersCoal = new LinkedHashMap<GoodType, Double>();
			parametersCoal.put(GoodType.LABOURHOUR, 1.0);
			parametersCoal.put(GoodType.MACHINE, 0.2);
			return new CESProductionFunctionImpl(50.0, parametersCoal, -0.7,
					0.65);

		default:
			return null;
		}
	}
}