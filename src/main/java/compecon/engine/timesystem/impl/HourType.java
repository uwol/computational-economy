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

public enum HourType {
	EVERY(-1), HOUR_00(0), HOUR_01(1), HOUR_02(2), HOUR_03(3), HOUR_04(4), HOUR_05(
			5), HOUR_06(6), HOUR_07(7), HOUR_08(8), HOUR_09(9), HOUR_10(10), HOUR_11(
			11), HOUR_12(12), HOUR_13(13), HOUR_14(14), HOUR_15(15), HOUR_16(16), HOUR_17(
			17), HOUR_18(18), HOUR_19(19), HOUR_20(20), HOUR_21(21), HOUR_22(22), HOUR_23(
			23);

	private int hourNumber;

	private HourType(final int hourNumber) {
		this.hourNumber = hourNumber;
	}

	public int getHourNumber() {
		return this.hourNumber;
	}

	public static HourType getHourType(int hourNumber) {
		for (HourType hourType : HourType.values()) {
			if (hourType.getHourNumber() == hourNumber) {
				return hourType;
			}
		}
		return null;
	}
}