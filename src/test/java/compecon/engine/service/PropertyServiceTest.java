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

package compecon.engine.service;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import compecon.CompEconTestSupport;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.economy.sectors.industry.Factory;
import compecon.economy.security.equity.Share;
import compecon.engine.applicationcontext.ApplicationContext;

public class PropertyServiceTest extends CompEconTestSupport {

	@Before
	public void setup() {
		super.setUpApplicationContextWithAgents();
	}

	@After
	public void tearDown() {
		super.tearDown();
	}

	@Test
	public void transferProperty() {
		Currency currency = Currency.EURO;

		Household household1_EUR = ApplicationContext.getInstance()
				.getHouseholdDAO().findAllByCurrency(currency).get(0);
		Household household2_EUR = ApplicationContext.getInstance()
				.getHouseholdDAO().findAllByCurrency(currency).get(1);
		Factory factory1_EUR = ApplicationContext.getInstance().getFactoryDAO()
				.findRandom();

		Share share = ApplicationContext.getInstance().getShareFactory()
				.newInstanceShare(factory1_EUR, factory1_EUR);

		// transfer share
		ApplicationContext.getInstance().getPropertyService()
				.transferProperty(share, factory1_EUR, household1_EUR);

		// owner
		assertEquals(0, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(factory1_EUR, Share.class)
				.size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(household1_EUR, Share.class)
				.size());

		// transfer share
		ApplicationContext.getInstance().getPropertyService()
				.transferProperty(share, household1_EUR, household2_EUR);

		// owner
		assertEquals(0, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(household1_EUR, Share.class)
				.size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(household2_EUR, Share.class)
				.size());

		// issuer
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesIssuedByAgent(factory1_EUR, Share.class)
				.size());
		assertEquals(0, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesIssuedByAgent(household2_EUR, Share.class)
				.size());
	}

	@Test
	public void transferEverything() {
		Currency currency = Currency.EURO;

		Household household1_EUR = ApplicationContext.getInstance()
				.getHouseholdDAO().findAllByCurrency(currency).get(0);
		Factory factory1_EUR = ApplicationContext.getInstance().getFactoryDAO()
				.findRandom();

		Share share = ApplicationContext.getInstance().getShareFactory()
				.newInstanceShare(factory1_EUR, factory1_EUR);

		// transfer share
		ApplicationContext.getInstance().getPropertyService()
				.transferProperty(share, factory1_EUR, household1_EUR);

		// owner
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(household1_EUR, Share.class)
				.size());

		// transfer everything
		ApplicationContext.getInstance().getPropertyService()
				.transferEverythingToRandomAgent(household1_EUR);

		// owner
		assertEquals(0, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(household1_EUR).size());
	}
}
