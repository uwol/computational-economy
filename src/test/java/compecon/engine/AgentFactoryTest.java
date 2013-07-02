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

package compecon.engine;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import compecon.CompEconTestSupport;
import compecon.culture.sectors.financial.Currency;
import compecon.engine.dao.DAOFactory;

public class AgentFactoryTest extends CompEconTestSupport {

	@Before
	public void setUp() {
		super.setUp();
	}

	@After
	public void tearDown() {
		super.tearDown();
	}

	@Test
	public void createAndDeleteAgents1() {
		assertEquals(Currency.values().length, DAOFactory.getCentralBankDAO()
				.findAll().size());
		assertEquals(Currency.values().length * 2, DAOFactory
				.getCreditBankDAO().findAll().size());
		assertEquals(Currency.values().length * 2, DAOFactory.getHouseholdDAO()
				.findAll().size());
	}

	@Test
	public void createAndDeleteAgents2() {
		assertEquals(Currency.values().length, DAOFactory.getCentralBankDAO()
				.findAll().size());
		assertEquals(Currency.values().length * 2, DAOFactory
				.getCreditBankDAO().findAll().size());
		assertEquals(Currency.values().length * 2, DAOFactory.getHouseholdDAO()
				.findAll().size());
	}
}
