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

package io.github.uwol.compecon.engine.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.github.uwol.compecon.CompEconTestSupport;
import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.economy.sectors.household.Household;
import io.github.uwol.compecon.economy.sectors.industry.Factory;
import io.github.uwol.compecon.economy.security.debt.Bond;
import io.github.uwol.compecon.economy.security.equity.Share;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;

public class PropertyServiceTest extends CompEconTestSupport {

	@Before
	public void setup() throws IOException {
		super.setUpApplicationContext(testConfigurationPropertiesFilename);
		super.setUpTestAgents();
	}

	@Override
	@After
	public void tearDown() {
		super.tearDown();
	}

	@Test
	public void testDeleteProperty() {
		final Factory factory1_EUR = ApplicationContext.getInstance().getAgentService().findRandomFactory();

		// create share
		final Share share = ApplicationContext.getInstance().getShareFactory().newInstanceShare(factory1_EUR,
				factory1_EUR);

		assertEquals(1, ApplicationContext.getInstance().getPropertyService()
				.findAllPropertiesOfPropertyOwner(factory1_EUR, Share.class).size());

		// delete share
		ApplicationContext.getInstance().getPropertyService().deleteProperty(share);

		assertEquals(0, ApplicationContext.getInstance().getPropertyService()
				.findAllPropertiesOfPropertyOwner(factory1_EUR, Share.class).size());
	}

	@Test
	public void testFindAllPropertiesIssuedByAgent() {
		final Factory factory1_EUR = ApplicationContext.getInstance().getAgentService().findRandomFactory();

		// create share
		ApplicationContext.getInstance().getShareFactory().newInstanceShare(factory1_EUR, factory1_EUR);

		// create bond
		ApplicationContext.getInstance().getFixedRateBondFactory().newInstanceFixedRateBond(factory1_EUR, factory1_EUR,
				Currency.EURO, factory1_EUR.getBankAccountTransactionsDelegate(),
				factory1_EUR.getBankAccountTransactionsDelegate(), 100, 1);

		assertEquals(2, ApplicationContext.getInstance().getPropertyService()
				.findAllPropertiesIssuedByAgent(factory1_EUR).size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyService()
				.findAllPropertiesIssuedByAgent(factory1_EUR, Share.class).size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyService()
				.findAllPropertiesIssuedByAgent(factory1_EUR, Bond.class).size());
	}

	@Test
	public void testFindAllPropertiesOfPropertyOwner() {
		final Factory factory1_EUR = ApplicationContext.getInstance().getAgentService().findRandomFactory();

		// create share
		ApplicationContext.getInstance().getShareFactory().newInstanceShare(factory1_EUR, factory1_EUR);

		// create bond
		ApplicationContext.getInstance().getFixedRateBondFactory().newInstanceFixedRateBond(factory1_EUR, factory1_EUR,
				Currency.EURO, factory1_EUR.getBankAccountTransactionsDelegate(),
				factory1_EUR.getBankAccountTransactionsDelegate(), 100, 1);

		assertEquals(2, ApplicationContext.getInstance().getPropertyService()
				.findAllPropertiesOfPropertyOwner(factory1_EUR).size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyService()
				.findAllPropertiesOfPropertyOwner(factory1_EUR, Share.class).size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyService()
				.findAllPropertiesOfPropertyOwner(factory1_EUR, Bond.class).size());
	}

	@Test
	public void testFindCapital() {
		final Currency currency = Currency.EURO;

		final Factory factory1_EUR = ApplicationContext.getInstance().getAgentService().findFactories(currency).get(0);

		// increment
		ApplicationContext.getInstance().getPropertyService().incrementGoodTypeAmount(factory1_EUR, GoodType.IRON, 1.0);
		ApplicationContext.getInstance().getPropertyService().incrementGoodTypeAmount(factory1_EUR, GoodType.MACHINE,
				2.0);

		assertFalse(ApplicationContext.getInstance().getPropertyService().getCapitalBalances(factory1_EUR)
				.containsKey(GoodType.IRON));
		assertTrue(ApplicationContext.getInstance().getPropertyService().getCapitalBalances(factory1_EUR)
				.containsKey(GoodType.MACHINE));
		assertEquals(2.0, ApplicationContext.getInstance().getPropertyService().getCapitalBalances(factory1_EUR)
				.get(GoodType.MACHINE), epsilon);
	}

	@Test
	public void testIncrementAndDecrementGoodType() {
		final Currency currency = Currency.EURO;

		final Household household1_EUR = ApplicationContext.getInstance().getAgentService().findHouseholds(currency)
				.get(0);

		assertEquals(0.0,
				ApplicationContext.getInstance().getPropertyService().getGoodTypeBalance(household1_EUR, GoodType.IRON),
				epsilon);

		// increment
		ApplicationContext.getInstance().getPropertyService().incrementGoodTypeAmount(household1_EUR, GoodType.IRON,
				1.1);

		assertEquals(1.1,
				ApplicationContext.getInstance().getPropertyService().getGoodTypeBalance(household1_EUR, GoodType.IRON),
				epsilon);
		assertEquals(1.1, ApplicationContext.getInstance().getPropertyService().getGoodTypeBalances(household1_EUR)
				.get(GoodType.IRON), epsilon);

		assertEquals(0.0, ApplicationContext.getInstance().getPropertyService().getGoodTypeBalance(household1_EUR,
				GoodType.WHEAT), epsilon);
		assertEquals(0.0, ApplicationContext.getInstance().getPropertyService().getGoodTypeBalances(household1_EUR)
				.get(GoodType.WHEAT), epsilon);

		// decrement
		ApplicationContext.getInstance().getPropertyService().decrementGoodTypeAmount(household1_EUR, GoodType.IRON,
				1.0);

		assertEquals(0.1,
				ApplicationContext.getInstance().getPropertyService().getGoodTypeBalance(household1_EUR, GoodType.IRON),
				epsilon);

		// reset
		ApplicationContext.getInstance().getPropertyService().resetGoodTypeAmount(household1_EUR, GoodType.IRON);

		assertEquals(0.0,
				ApplicationContext.getInstance().getPropertyService().getGoodTypeBalance(household1_EUR, GoodType.IRON),
				epsilon);
	}

	@Test
	public void testTransferEverythingToRandomAgent() {
		final Currency currency = Currency.EURO;

		final Household household1_EUR = ApplicationContext.getInstance().getAgentService().findHouseholds(currency)
				.get(0);
		final Factory factory1_EUR = ApplicationContext.getInstance().getAgentService().findRandomFactory();

		final Share share = ApplicationContext.getInstance().getShareFactory().newInstanceShare(factory1_EUR,
				factory1_EUR);

		// transfer share
		ApplicationContext.getInstance().getPropertyService().transferProperty(share, factory1_EUR, household1_EUR);

		// owner
		assertEquals(1, ApplicationContext.getInstance().getPropertyService()
				.findAllPropertiesOfPropertyOwner(household1_EUR, Share.class).size());

		// transfer everything
		ApplicationContext.getInstance().getPropertyService().transferEverythingToRandomAgent(household1_EUR);

		// owner
		assertEquals(0, ApplicationContext.getInstance().getPropertyService()
				.findAllPropertiesOfPropertyOwner(household1_EUR).size());
	}

	@Test
	public void testTransferGoodTypeAmount() {
		final Currency currency = Currency.EURO;

		final Household household1_EUR = ApplicationContext.getInstance().getAgentService().findHouseholds(currency)
				.get(0);
		final Household household2_EUR = ApplicationContext.getInstance().getAgentService().findHouseholds(currency)
				.get(1);

		// increment
		ApplicationContext.getInstance().getPropertyService().incrementGoodTypeAmount(household1_EUR, GoodType.IRON,
				1.1);

		assertEquals(1.1,
				ApplicationContext.getInstance().getPropertyService().getGoodTypeBalance(household1_EUR, GoodType.IRON),
				epsilon);

		// transfer
		ApplicationContext.getInstance().getPropertyService().transferGoodTypeAmount(GoodType.IRON, household1_EUR,
				household2_EUR, 1.0);

		assertEquals(0.1,
				ApplicationContext.getInstance().getPropertyService().getGoodTypeBalance(household1_EUR, GoodType.IRON),
				epsilon);
		assertEquals(1.0,
				ApplicationContext.getInstance().getPropertyService().getGoodTypeBalance(household2_EUR, GoodType.IRON),
				epsilon);
	}

	@Test
	public void testTransferProperty() {
		final Currency currency = Currency.EURO;

		final Household household1_EUR = ApplicationContext.getInstance().getAgentService().findHouseholds(currency)
				.get(0);
		final Household household2_EUR = ApplicationContext.getInstance().getAgentService().findHouseholds(currency)
				.get(1);
		final Factory factory1_EUR = ApplicationContext.getInstance().getAgentService().findRandomFactory();

		final Share share = ApplicationContext.getInstance().getShareFactory().newInstanceShare(factory1_EUR,
				factory1_EUR);

		// transfer share
		ApplicationContext.getInstance().getPropertyService().transferProperty(share, factory1_EUR, household1_EUR);

		// owner
		assertEquals(0, ApplicationContext.getInstance().getPropertyService()
				.findAllPropertiesOfPropertyOwner(factory1_EUR, Share.class).size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyService()
				.findAllPropertiesOfPropertyOwner(household1_EUR, Share.class).size());

		// transfer share
		ApplicationContext.getInstance().getPropertyService().transferProperty(share, household1_EUR, household2_EUR);

		// owner
		assertEquals(0, ApplicationContext.getInstance().getPropertyService()
				.findAllPropertiesOfPropertyOwner(household1_EUR, Share.class).size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyService()
				.findAllPropertiesOfPropertyOwner(household2_EUR, Share.class).size());

		// issuer
		assertEquals(1, ApplicationContext.getInstance().getPropertyService()
				.findAllPropertiesIssuedByAgent(factory1_EUR, Share.class).size());
		assertEquals(0, ApplicationContext.getInstance().getPropertyService()
				.findAllPropertiesIssuedByAgent(household2_EUR, Share.class).size());
	}
}
