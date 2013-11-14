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

package compecon.math.utility.impl;

import compecon.materia.GoodType;
import compecon.math.impl.ConvexFunctionImpl;

/**
 * A convex function -> any local optimum must be a global optimum<br />
 * http://en.wikipedia.org/wiki/Convex_optimization
 */
public abstract class ConvexUtilityFunctionImpl extends UtilityFunctionImpl {

	protected ConvexUtilityFunctionImpl(ConvexFunctionImpl<GoodType> delegate) {
		super(delegate);
	}
}
