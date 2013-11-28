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

package compecon.engine.dao;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import compecon.CompEconTestSupport;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.engine.applicationcontext.ApplicationContext;

public class HouseholdDAOTest extends CompEconTestSupport {

	@Before
	public void setUpApplicationContext() {
		super.setUpApplicationContext();
	}

	@After
	public void tearDown() {
		super.tearDown();
	}

	@Test
	public void testCreateAndDeleteHouseholds() {
		ApplicationContext.getInstance().getHouseholdFactory()
				.newInstanceHousehold(Currency.EURO, 0);
		ApplicationContext.getInstance().getHouseholdFactory()
				.newInstanceHousehold(Currency.EURO, 0);
		Household household = ApplicationContext.getInstance()
				.getHouseholdFactory().newInstanceHousehold(Currency.EURO, 0);

		ApplicationContext.getInstance().getHouseholdFactory()
				.newInstanceHousehold(Currency.USDOLLAR, 0);
		ApplicationContext.getInstance().getHouseholdFactory()
				.newInstanceHousehold(Currency.YEN, 0);

		assertEquals(5, ApplicationContext.getInstance().getHouseholdDAO()
				.findAll().size());
		assertEquals(3, ApplicationContext.getInstance().getHouseholdDAO()
				.findAllByCurrency(Currency.EURO).size());
		assertEquals(1, ApplicationContext.getInstance().getHouseholdDAO()
				.findAllByCurrency(Currency.USDOLLAR).size());
		assertEquals(1, ApplicationContext.getInstance().getHouseholdDAO()
				.findAllByCurrency(Currency.YEN).size());

		household.deconstruct();

		assertEquals(2, ApplicationContext.getInstance().getHouseholdDAO()
				.findAllByCurrency(Currency.EURO).size());
	}
}
