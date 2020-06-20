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

package io.github.uwol.compecon.economy.materia.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.materia.InputOutputModel;
import io.github.uwol.compecon.math.production.ProductionFunction;
import io.github.uwol.compecon.math.production.impl.RootProductionFunctionImpl;
import io.github.uwol.compecon.math.utility.UtilityFunction;
import io.github.uwol.compecon.math.utility.impl.CobbDouglasUtilityFunctionImpl;

public class InputOutputModelMinimalImpl implements InputOutputModel {

	@Override
	public ProductionFunction getProductionFunction(final GoodType outputGoodType) {
		switch (outputGoodType) {
		case COAL:
			return new RootProductionFunctionImpl(GoodType.LABOURHOUR, 5.0);
		case WHEAT:
			return new RootProductionFunctionImpl(GoodType.LABOURHOUR, 5.0);
		default:
			return null;
		}
	}

	@Override
	public UtilityFunction getUtilityFunctionOfHousehold() {
		final Map<GoodType, Double> preferences = new LinkedHashMap<GoodType, Double>();
		preferences.put(GoodType.WHEAT, 0.4);
		preferences.put(GoodType.COAL, 0.4);
		preferences.put(GoodType.LABOURHOUR, 0.2);
		return new CobbDouglasUtilityFunctionImpl(1.0, preferences);
	}
}
