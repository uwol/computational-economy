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

package io.github.uwol.compecon.economy.materia;

public enum GoodType {

	CLOTHING(false, false, Sector.SECONDARY), COAL(false, false, Sector.PRIMARY), COTTON(
			false, false, Sector.PRIMARY), FOOD(false, false, Sector.SECONDARY), IRON(
			false, false, Sector.PRIMARY), KILOWATT(false, false,
			Sector.SECONDARY), LABOURHOUR(false, false, Sector.TERTIARY), MACHINE(
			false, true, Sector.SECONDARY), REALESTATE(false, false,
			Sector.SECONDARY), WHEAT(false, false, Sector.PRIMARY);

	// CRAFT(false, false, Sector.TERTIARY), EDUCATION(false, false,
	// Sector.TERTIARY),
	// ADMINISTRATION(false, false, Sector.TERTIARY), CONSULTING(false,
	// false, Sector.TERTIARY)

	public enum Sector {
		PRIMARY, SECONDARY, TERTIARY;
	}

	/**
	 * capital goods are durable goods <br />
	 * consumption goods mostly are not durable goods, but can be (e. g. cars) <br />
	 * <br />
	 * http://en.wikipedia.org/wiki/Capital_good
	 */
	protected final boolean durable;

	protected final Sector sector;

	protected final boolean wholeNumber;

	private GoodType(final boolean wholeNumber, final boolean durable,
			final Sector sector) {
		this.durable = durable;
		this.wholeNumber = wholeNumber;
		this.sector = sector;
	}

	public Sector getSector() {
		return sector;
	}

	/**
	 * @see #durable
	 */
	public boolean isDurable() {
		return durable;
	}

	public boolean isWholeNumber() {
		return wholeNumber;
	}
}