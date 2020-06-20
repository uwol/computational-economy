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
import io.github.uwol.compecon.math.production.impl.CESProductionFunctionImpl;
import io.github.uwol.compecon.math.production.impl.RootProductionFunctionImpl;
import io.github.uwol.compecon.math.utility.UtilityFunction;
import io.github.uwol.compecon.math.utility.impl.CESUtilityFunctionImpl;

public class InputOutputModelInterdependenciesImpl implements InputOutputModel {

	@Override
	public ProductionFunction getProductionFunction(final GoodType outputGoodType) {
		switch (outputGoodType) {
		case MACHINE:
			return new RootProductionFunctionImpl(GoodType.LABOURHOUR, 5.0);

		case IRON:
			final Map<GoodType, Double> parametersIron = new LinkedHashMap<GoodType, Double>();
			parametersIron.put(GoodType.LABOURHOUR, 0.6);
			parametersIron.put(GoodType.MACHINE, 0.2);
			return new CESProductionFunctionImpl(50.0, parametersIron, -0.7, 0.65);
		case COAL:
			final Map<GoodType, Double> parametersCoal = new LinkedHashMap<GoodType, Double>();
			parametersCoal.put(GoodType.LABOURHOUR, 0.6);
			parametersCoal.put(GoodType.MACHINE, 0.2);
			return new CESProductionFunctionImpl(50.0, parametersCoal, -0.7, 0.65);
		case COTTON:
			final Map<GoodType, Double> parametersCotton = new LinkedHashMap<GoodType, Double>();
			parametersCotton.put(GoodType.LABOURHOUR, 0.6);
			parametersCotton.put(GoodType.MACHINE, 0.2);
			return new CESProductionFunctionImpl(50.0, parametersCotton, -0.7, 0.65);
		case WHEAT:
			final Map<GoodType, Double> parametersWheat = new LinkedHashMap<GoodType, Double>();
			parametersWheat.put(GoodType.LABOURHOUR, 0.6);
			parametersWheat.put(GoodType.MACHINE, 0.2);
			return new CESProductionFunctionImpl(50.0, parametersWheat, -0.7, 0.65);

		case FOOD:
			final Map<GoodType, Double> parametersFood = new LinkedHashMap<GoodType, Double>();
			parametersFood.put(GoodType.WHEAT, 0.6);
			parametersFood.put(GoodType.LABOURHOUR, 0.1);
			return new CESProductionFunctionImpl(1.0, parametersFood, -0.8, 0.7);
		case KILOWATT:
			final Map<GoodType, Double> parametersKiloWatt = new LinkedHashMap<GoodType, Double>();
			parametersKiloWatt.put(GoodType.LABOURHOUR, 0.2);
			parametersKiloWatt.put(GoodType.COAL, 0.4);
			return new CESProductionFunctionImpl(1.0, parametersKiloWatt, -0.8, 0.7);
		case CLOTHING:
			final Map<GoodType, Double> parametersClothing = new LinkedHashMap<GoodType, Double>();
			parametersClothing.put(GoodType.COTTON, 0.5);
			parametersClothing.put(GoodType.COAL, 0.2);
			return new CESProductionFunctionImpl(1.0, parametersClothing, -0.8, 0.7);
		case REALESTATE:
			final Map<GoodType, Double> parametersRealEstate = new LinkedHashMap<GoodType, Double>();
			parametersRealEstate.put(GoodType.IRON, 0.3);
			parametersRealEstate.put(GoodType.LABOURHOUR, 0.3);
			return new CESProductionFunctionImpl(1.0, parametersRealEstate, -0.8, 0.7);

		default:
			return null;
		}
	}

	@Override
	public UtilityFunction getUtilityFunctionOfHousehold() {
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
		return new CESUtilityFunctionImpl(1.0, preferences, -0.7, 0.5);
	}
}