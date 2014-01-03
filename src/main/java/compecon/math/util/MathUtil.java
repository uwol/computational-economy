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

package compecon.math.util;

public class MathUtil {

	// epsilon for absolute values, not deviation from quotient etc.
	private static final double epsilon = 0.00001;

	private static final double defaultRoundPrecision = Math
			.round(1.0 / epsilon);

	/**
	 * Calculates the monthly nominal interest rate for a given effective yearly
	 * interest rate.<br />
	 * <br />
	 * http://en.wikipedia.org/wiki/Effective_interest_rate
	 */
	public static double calculateMonthlyNominalInterestRate(
			final double effectiveInterestRate) {
		return effectiveInterestRate / (1 + 11 / 24 * effectiveInterestRate)
				/ 12;
	}

	public static boolean equal(final double value1, final double value2) {
		if (Double.isNaN(value1) && Double.isNaN(value2))
			return true;
		if (Double.isInfinite(value1) && Double.isInfinite(value2))
			return true;
		// value1 - value2 are equal, if they differ minimally; has to hold
		// under value1 = 0
		return Math.abs(value1 - value2) <= epsilon;
	}

	public static boolean greater(final double value1, final double value2) {
		if (Double.isNaN(value1) || Double.isNaN(value2))
			return false;
		if (!Double.isInfinite(value1) && Double.isInfinite(value2))
			return false;
		if (Double.isInfinite(value1) && !Double.isInfinite(value2))
			return true;
		if (Double.isInfinite(value1) && Double.isInfinite(value2))
			return false;
		// value1 has to be significantly greater than value2
		return value1 - epsilon > value2;
	}

	public static boolean greaterEqual(final double value1, final double value2) {
		return equal(value1, value2) || greater(value1, value2);
	}

	public static boolean lesser(final double value1, final double value2) {
		if (Double.isNaN(value1) || Double.isNaN(value2))
			return false;
		if (Double.isInfinite(value1) && !Double.isInfinite(value2))
			return false;
		if (!Double.isInfinite(value1) && Double.isInfinite(value2))
			return true;
		if (Double.isInfinite(value1) && Double.isInfinite(value2))
			return false;
		// value1 has to be significantly lesser than value2
		return value1 + epsilon < value2;
	}

	public static boolean lesserEqual(final double value1, final double value2) {
		return equal(value1, value2) || lesser(value1, value2);
	}

	public static double round(final double value) {
		if (Double.isNaN(value) || Double.isInfinite(value))
			return value;
		return Math.round(value * defaultRoundPrecision)
				/ defaultRoundPrecision;
	}

	public static double log(final double num, final double base) {
		return Math.log(num) / Math.log(base);
	}

	public static double nullSafeValue(final Double number) {
		return number == null ? 0.0 : number;
	}
}
