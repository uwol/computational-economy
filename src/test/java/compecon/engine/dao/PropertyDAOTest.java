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
	public void setUpApplicationContext() {
		super.setUpApplicationContext();
	}

	@After
	public void tearDown() {
		super.tearDown();
	}

	@Test
	public void testCreateAndDeleteStateBonds1() {
		// prepare
		State state = ApplicationContext.getInstance().getAgentService()
				.getInstanceState(Currency.EURO);
		CreditBank creditBank = ApplicationContext.getInstance()
				.getCreditBankFactory().newInstanceCreditBank(Currency.EURO);

		Bond bond = state.obtainBond(1000,
				creditBank.getBankAccountTransactionsDelegate());

		// total number
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAll().size());

		// owner
		assertEquals(0, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(state).size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(creditBank).size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(creditBank, Bond.class)
				.size());
		assertEquals(0, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(creditBank, Share.class)
				.size());

		// issuer
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesIssuedByAgent(state).size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesIssuedByAgent(state, Bond.class).size());
		assertEquals(0, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesIssuedByAgent(state, Share.class).size());

		assertEquals(bond, ApplicationContext.getInstance().getPropertyDAO()
				.findRandom());
	}

	@Test
	public void testCreateAndDeleteStateBonds2() {
		// prepare
		State state = ApplicationContext.getInstance().getAgentService()
				.getInstanceState(Currency.EURO);
		CreditBank creditBank = ApplicationContext.getInstance()
				.getCreditBankFactory().newInstanceCreditBank(Currency.EURO);

		Bond bond1 = state.obtainBond(1000,
				creditBank.getBankAccountTransactionsDelegate());
		Bond bond2 = state.obtainBond(1000,
				creditBank.getBankAccountTransactionsDelegate());
		Share share1 = ApplicationContext.getInstance().getShareFactory()
				.newInstanceShare(creditBank, creditBank);

		// total number
		assertEquals(3, ApplicationContext.getInstance().getPropertyDAO()
				.findAll().size());

		// owner
		assertEquals(0, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(state).size());

		assertEquals(3, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(creditBank).size());
		assertEquals(2, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(creditBank, Bond.class)
				.size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(creditBank, Share.class)
				.size());

		// issuer
		assertEquals(2, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesIssuedByAgent(state).size());
		assertEquals(2, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesIssuedByAgent(state, Bond.class).size());
		assertEquals(0, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesIssuedByAgent(state, Share.class).size());

		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesIssuedByAgent(creditBank).size());
		assertEquals(0, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesIssuedByAgent(creditBank, Bond.class).size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesIssuedByAgent(creditBank, Share.class).size());

		// transfer
		ApplicationContext.getInstance().getPropertyService()
				.transferProperty(bond2, creditBank, state);

		// owner
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(state).size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(state, Bond.class).size());
		assertEquals(0, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(state, Share.class).size());

		assertEquals(2, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(creditBank).size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(creditBank, Bond.class)
				.size());
		assertEquals(1, ApplicationContext.getInstance().getPropertyDAO()
				.findAllPropertiesOfPropertyOwner(creditBank, Share.class)
				.size());
	}
}
