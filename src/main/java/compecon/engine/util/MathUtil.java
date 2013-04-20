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

package compecon.engine.util;

public class MathUtil {
	public static boolean equal(double value1, double value2) {
		if (Double.isNaN(value1) && Double.isNaN(value2))
			return true;
		if (Double.isInfinite(value1) && Double.isInfinite(value2))
			return true;
		return Math.abs(value1 - value2) < 0.0000001;
	}

	public static double round(double value) {
		if (Double.isNaN(value) || Double.isInfinite(value))
			return value;
		return Math.round(value * 1000.) / 1000.;
	}
}
