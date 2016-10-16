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

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.github.uwol.compecon.CompEconTestSupport;
import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;

public class AgentServiceTest extends CompEconTestSupport {

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
	public void testGetAgentMethods() {
		for (final Currency currency : Currency.values()) {
			// state
			Assert.assertNotNull(ApplicationContext.getInstance()
					.getAgentService().findState(currency));

			// central bank
			Assert.assertNotNull(ApplicationContext.getInstance()
					.getAgentService().findCentralBank(currency));

			// credit banks
			Assert.assertEquals(2, ApplicationContext.getInstance()
					.getAgentService().findCreditBanks(currency).size());

			// central bank
			Assert.assertNotNull(ApplicationContext.getInstance()
					.getAgentService().findRandomCreditBank(currency));

			// factories
			Assert.assertEquals(2, ApplicationContext.getInstance()
					.getAgentService().findFactories(currency).size());

			Assert.assertEquals(1, ApplicationContext.getInstance()
					.getAgentService().findFactories(currency, GoodType.WHEAT)
					.size());

			Assert.assertEquals(1, ApplicationContext.getInstance()
					.getAgentService().findFactories(currency, GoodType.COAL)
					.size());

			// traders
			Assert.assertEquals(1, ApplicationContext.getInstance()
					.getAgentService().findTraders(currency).size());

			// households
			Assert.assertEquals(2, ApplicationContext.getInstance()
					.getAgentService().findHouseholds(currency).size());
		}
	}
}
