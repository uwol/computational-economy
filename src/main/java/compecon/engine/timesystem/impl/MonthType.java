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

package compecon.engine.timesystem.impl;

public enum MonthType {
	EVERY(-1), JANUARY(0), FEBRUARY(1), MARCH(2), APRIL(3), MAY(4), JUNE(5), JULY(
			6), AUGUST(7), SEPTEBER(8), OCTOBER(9), NOVEMBER(10), DECEMBER(11);

	private int monthNumber;

	private MonthType(final int monthNumber) {
		this.monthNumber = monthNumber;
	}

	public int getMonthNumber() {
		return this.monthNumber;
	}

	public static MonthType getMonthType(final int monthNumber) {
		for (MonthType monthType : MonthType.values()) {
			if (monthType.getMonthNumber() == monthNumber) {
				return monthType;
			}
		}
		return null;
	}
}
