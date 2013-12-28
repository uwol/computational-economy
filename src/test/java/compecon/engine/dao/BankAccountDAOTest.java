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
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.engine.applicationcontext.ApplicationContext;

public class BankAccountDAOTest extends CompEconTestSupport {

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
		CreditBank creditBank1_EUR = ApplicationContext.getInstance()
				.getAgentService().findCreditBanks(currency).get(0);
		Household household1_EUR = ApplicationContext.getInstance()
				.getAgentService().findHouseholds(currency).get(0);
		Household household2_EUR = ApplicationContext.getInstance()
				.getAgentService().findHouseholds(currency).get(1);

		assertEquals(0, ApplicationContext.getInstance().getBankAccountDAO()
				.findAllBankAccountsManagedByBank(creditBank1_EUR).size());
		assertEquals(0, ApplicationContext.getInstance().getBankAccountDAO()
				.findAll(creditBank1_EUR, household1_EUR).size());
		assertEquals(0, ApplicationContext.getInstance().getBankAccountDAO()
				.findAll(creditBank1_EUR, household1_EUR, currency).size());
		assertEquals(0, ApplicationContext.getInstance().getBankAccountDAO()
				.findAllBankAccountsOfAgent(household1_EUR).size());

		// create bank account for household 1
		creditBank1_EUR.openBankAccount(household1_EUR, currency, false,
				"test bank account", TermType.SHORT_TERM, MoneyType.DEPOSITS);

		// create bank account for household 2
		creditBank1_EUR.openBankAccount(household2_EUR, currency, false,
				"test bank account", TermType.SHORT_TERM, MoneyType.DEPOSITS);

		assertEquals(2, ApplicationContext.getInstance().getBankAccountDAO()
				.findAllBankAccountsManagedByBank(creditBank1_EUR).size());
		assertEquals(1, ApplicationContext.getInstance().getBankAccountDAO()
				.findAll(creditBank1_EUR, household1_EUR).size());
		assertEquals(1, ApplicationContext.getInstance().getBankAccountDAO()
				.findAll(creditBank1_EUR, household1_EUR, currency).size());
		assertEquals(0, ApplicationContext.getInstance().getBankAccountDAO()
				.findAll(creditBank1_EUR, household1_EUR, Currency.USDOLLAR)
				.size());
		assertEquals(1, ApplicationContext.getInstance().getBankAccountDAO()
				.findAllBankAccountsOfAgent(household1_EUR).size());

		// delete bank accounts of household 1
		ApplicationContext.getInstance().getBankAccountFactory()
				.deleteAllBankAccounts(creditBank1_EUR, household1_EUR);

		assertEquals(1, ApplicationContext.getInstance().getBankAccountDAO()
				.findAllBankAccountsManagedByBank(creditBank1_EUR).size());
		assertEquals(0, ApplicationContext.getInstance().getBankAccountDAO()
				.findAll(creditBank1_EUR, household1_EUR).size());
		assertEquals(0, ApplicationContext.getInstance().getBankAccountDAO()
				.findAll(creditBank1_EUR, household1_EUR, currency).size());
		assertEquals(0, ApplicationContext.getInstance().getBankAccountDAO()
				.findAllBankAccountsOfAgent(household1_EUR).size());

		// delete bank accounts managed by credit bank 1
		ApplicationContext.getInstance().getBankAccountFactory()
				.deleteAllBankAccounts(creditBank1_EUR);

		assertEquals(0, ApplicationContext.getInstance().getBankAccountDAO()
				.findAllBankAccountsManagedByBank(creditBank1_EUR).size());
		assertEquals(0, ApplicationContext.getInstance().getBankAccountDAO()
				.findAll(creditBank1_EUR, household2_EUR).size());
		assertEquals(0, ApplicationContext.getInstance().getBankAccountDAO()
				.findAll(creditBank1_EUR, household2_EUR, currency).size());
		assertEquals(0, ApplicationContext.getInstance().getBankAccountDAO()
				.findAllBankAccountsOfAgent(household2_EUR).size());
	}
}
