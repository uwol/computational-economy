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

package compecon.economy.materia;

import compecon.math.production.ProductionFunction;
import compecon.math.utility.UtilityFunction;

/**
 * Factory class for production and household functions, which relates inputs /
 * production factors to outputs.
 * 
 * http://en.wikipedia.org/wiki/Input-output_model
 */
public interface InputOutputModel {

	/**
	 * utility function for households modeling consumption preferences; each
	 * GoodType has to be contained here (at least transitively via the
	 * input-output-model), so that the corresponding price on the market can
	 * come to an equilibrium; preference for labour hour has to be high enough,
	 * so that labour hour prices do not fall endlessly
	 */
	public UtilityFunction getUtilityFunctionOfHousehold();

	/**
	 * @see #getUtilityFunctionOfHousehold()
	 */
	public UtilityFunction getUtilityFunctionOfState();

	public ProductionFunction getProductionFunction(GoodType outputGoodType);
}
