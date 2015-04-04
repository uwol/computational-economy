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

package compecon.economy.sectors.financial;

public enum Currency {
	EURO("EUR"), USDOLLAR("USD"), YEN("YEN");

	public static String formatMoneySum(final double value) {
		final double million = 1000000;
		final double billion = 1000000000;

		if (Double.isNaN(value) || Double.isInfinite(value)) {
			return "" + value;
		}
		if (Math.abs(value) < million) {
			return String.format("%.2f", value);
		} else if (Math.abs(value) < billion) {
			return String.format("%.2f M", value / million);
		} else {
			return String.format("%.2f B", value / billion);
		}
	}

	public static double round(final double value) {
		if (Double.isNaN(value) || Double.isInfinite(value)) {
			return value;
		}
		return Math.round(value * 100.) / 100.;
	}

	protected String iso4217Code;

	private Currency(final String iso4217Code) {
		this.iso4217Code = iso4217Code;
	}

	public String getIso4217Code() {
		return iso4217Code;
	}
}
