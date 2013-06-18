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

package compecon.culture.sectors.financial;

public enum Currency {
	EURO("EUR"), USDOLLAR("USD"), YEN("YEN");

	protected String iso4217Code;

	private Currency(String iso4217Code) {
		this.iso4217Code = iso4217Code;
	}

	public String getIso4217Code() {
		return this.iso4217Code;
	}

	public static double round(double value) {
		if (Double.isNaN(value) || Double.isInfinite(value))
			return value;
		return Math.round(value * 100.) / 100.;
	}
}