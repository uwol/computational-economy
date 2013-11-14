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

package compecon.materia.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import compecon.materia.GoodType;
import compecon.materia.InputOutputModel;
import compecon.math.production.ProductionFunction;
import compecon.math.production.impl.RootProductionFunctionImpl;
import compecon.math.utility.UtilityFunction;
import compecon.math.utility.impl.CESUtilityFunctionImpl;

public class InputOutputModelMinimalImpl implements InputOutputModel {

	@Override
	public UtilityFunction getUtilityFunctionOfHousehold() {
		final Map<GoodType, Double> preferences = new LinkedHashMap<GoodType, Double>();
		preferences.put(GoodType.IRON, 0.3);
		preferences.put(GoodType.COAL, 0.3);
		preferences.put(GoodType.LABOURHOUR, 0.3);
		return new CESUtilityFunctionImpl(1.0, preferences, -0.7, 0.5);
	}

	@Override
	public UtilityFunction getUtilityFunctionOfState() {
		final Map<GoodType, Double> preferences = new LinkedHashMap<GoodType, Double>();
		preferences.put(GoodType.IRON, 0.3);
		preferences.put(GoodType.COAL, 0.3);
		preferences.put(GoodType.LABOURHOUR, 0.3);
		return new CESUtilityFunctionImpl(1.0, preferences, -0.7, 0.5);
	}

	@Override
	public ProductionFunction getProductionFunction(
			final GoodType outputGoodType) {
		switch (outputGoodType) {
		case IRON:
			return new RootProductionFunctionImpl(GoodType.LABOURHOUR, 100.0);
		case COAL:
			return new RootProductionFunctionImpl(GoodType.LABOURHOUR, 100.0);
		default:
			return null;
		}
	}

}