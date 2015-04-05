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

public enum DayType {
	DAY_01(1), DAY_02(2), DAY_03(3), DAY_04(4), DAY_05(5), DAY_06(6), DAY_07(7), DAY_08(
			8), DAY_09(9), DAY_10(10), DAY_11(11), DAY_12(12), DAY_13(13), DAY_14(
			14), DAY_15(15), DAY_16(16), DAY_17(17), DAY_18(18), DAY_19(19), DAY_20(
			20), DAY_21(21), DAY_22(22), DAY_23(23), DAY_24(24), DAY_25(25), DAY_26(
			26), DAY_27(27), DAY_28(28), DAY_29(29), DAY_30(30), DAY_31(31), EVERY(
			-1);

	public static DayType getDayType(final int dayNumber) {
		for (final DayType dayType : DayType.values()) {
			if (dayType.getDayNumber() == dayNumber) {
				return dayType;
			}
		}

		return null;
	}

	private int dayNumber;

	private DayType(final int dayNumber) {
		this.dayNumber = dayNumber;
	}

	public int getDayNumber() {
		return dayNumber;
	}
}
