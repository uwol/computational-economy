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
import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.State;
import compecon.economy.security.debt.Bond;
import compecon.economy.security.equity.Share;
import compecon.engine.applicationcontext.ApplicationContext;

public class PropertyDAOTest extends CompEconTestSupport {

	@Before
	public void setup() {
		super.setUpApplicationContextWithAgents();
	}

	@After
	public void tearDown() {
		super.tearDown();
	}

	@Test
	public void testCreateAndDeletePropertyOwnedAndIssuedBy() {
		Currency currency = Currency.EURO;

		// prepare
		State state_EUR = ApplicationContext.getInstance().getAgentService()
				.findState(currency);
		CreditBank creditBank1_EUR = ApplicationContext.getInstance()
				.getAgentService().findCreditBanks(currency).get(0);

		Bond bond = state_EUR.obtainBond(1000, creditBank1_EUR,
				creditBank1_EUR.getBankAccountTransactionsDelegate());

		// total number
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAll().size());

		// owner
		assertEquals(0, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(state_EUR).size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(creditBank1_EUR).size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(creditBank1_EUR, Bond.class)
				.size());
		assertEquals(0, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(creditBank1_EUR, Share.class)
				.size());

		// issuer
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesIssuedByAgent(state_EUR).size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesIssuedByAgent(state_EUR, Bond.class).size());
		assertEquals(0, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesIssuedByAgent(state_EUR, Share.class).size());

		// random access
		assertEquals(bond, ApplicationContext.getInstance().getPropertyDAO()
				.findRandom());

		bond.deconstruct();

		// owner
		assertEquals(0, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(state_EUR).size());
		assertEquals(0, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(creditBank1_EUR).size());

		// issuer
		assertEquals(0, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesIssuedByAgent(state_EUR).size());
		assertEquals(0, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesIssuedByAgent(state_EUR, Bond.class).size());
		assertEquals(0, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesIssuedByAgent(state_EUR, Share.class).size());
	}

	@Test
	public void testTransferProperty() {
		Currency currency = Currency.EURO;

		// prepare
		State state_EUR = ApplicationContext.getInstance().getAgentService()
				.findState(currency);
		CreditBank creditBank1_EUR = ApplicationContext.getInstance()
				.getAgentService().findCreditBanks(currency).get(0);

		Bond bond1 = state_EUR.obtainBond(1000, creditBank1_EUR,
				creditBank1_EUR.getBankAccountTransactionsDelegate());
		Bond bond2 = state_EUR.obtainBond(1000, creditBank1_EUR,
				creditBank1_EUR.getBankAccountTransactionsDelegate());
		Share share1 = ApplicationContext.getInstance().getShareFactory()
				.newInstanceShare(creditBank1_EUR, creditBank1_EUR);

		// total number
		assertEquals(3, ApplicationContext.getInstance().getPropertyDAO()
				.findAll().size());

		// transfer
		ApplicationContext.getInstance().getPropertyService()
				.transferProperty(bond2, creditBank1_EUR, state_EUR);

		// owner
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(state_EUR).size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(state_EUR, Bond.class).size());
		assertEquals(0, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(state_EUR, Share.class)
				.size());

		assertEquals(2, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(creditBank1_EUR).size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(creditBank1_EUR, Bond.class)
				.size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(creditBank1_EUR, Share.class)
				.size());
	}
}
