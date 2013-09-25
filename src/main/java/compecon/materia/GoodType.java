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

package compecon.materia;

public enum GoodType {

	LABOURHOUR(false, Sector.TERTIARY), IRON(false, Sector.PRIMARY), COAL(
			false, Sector.PRIMARY), COTTON(false, Sector.PRIMARY), WHEAT(false,
			Sector.PRIMARY), FOOD(false, Sector.SECONDARY), CLOTHING(false,
			Sector.SECONDARY), KILOWATT(false, Sector.SECONDARY), REALESTATE(
			false, Sector.SECONDARY), CRAFT(false, Sector.TERTIARY), EDUCATION(
			false, Sector.TERTIARY), ADMINISTRATION(false, Sector.TERTIARY), CONSULTING(
			false, Sector.TERTIARY);

	public enum Sector {
		PRIMARY, SECONDARY, TERTIARY;
	}

	protected final boolean wholeNumber;

	protected final Sector sector;

	private GoodType(boolean wholeNumber, Sector sector) {
		this.wholeNumber = wholeNumber;
		this.sector = sector;
	}

	public boolean getWholeNumber() {
		return this.wholeNumber;
	}

	public Sector getSector() {
		return this.sector;
	}

}