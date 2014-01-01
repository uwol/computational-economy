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

package compecon.economy.sectors.financial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import compecon.CompEconTestSupport;
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
import compecon.economy.sectors.household.Household;
import compecon.economy.sectors.household.impl.HouseholdImpl;
import compecon.engine.applicationcontext.ApplicationContext;

public class CreditBankTest extends CompEconTestSupport {

	@Before
	public void setup() throws IOException {
		super.setUpApplicationContext(configurationPropertiesFilename);
		super.setUpAgents();
	}

	@After
	public void tearDown() {
		super.tearDown();
	}

	@Test
	public void testTransferMoney() {
		Currency currency = Currency.EURO;

		Household household1_EUR = ApplicationContext.getInstance()
				.getAgentService().findHouseholds(currency).get(0);
		Household household2_EUR = ApplicationContext.getInstance()
				.getAgentService().findHouseholds(currency).get(1);

		assertEquals(0.0, household1_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getBalance(), epsilon);
		assertEquals(0.0, household2_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getBalance(), epsilon);

		Bank source = household1_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getManagingBank();
		Bank target = household2_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getManagingBank();

		// transfer money
		for (int i = 1; i < 1000; i++) {
			source.transferMoney(household1_EUR
					.getBankAccountTransactionsDelegate().getBankAccount(),
					household2_EUR.getBankAccountTransactionsDelegate()
							.getBankAccount(), 10, "Transaction" + i);
			assertEquals(-10.0 * i, household1_EUR
					.getBankAccountTransactionsDelegate().getBankAccount()
					.getBalance(), epsilon);
			assertEquals(10.0 * i, household2_EUR
					.getBankAccountTransactionsDelegate().getBankAccount()
					.getBalance(), epsilon);
		}
	}

	@Test
	public void testCreditBankDeconstruction() {
		Currency currency = Currency.EURO;

		// init household 1
		Household household1_EUR = ApplicationContext.getInstance()
				.getAgentService().findHouseholds(currency).get(0);
		CreditBank creditBank1_EUR = ApplicationContext.getInstance()
				.getAgentService().findCreditBanks(currency).get(0);
		BankAccount bankAccount1_EUR = creditBank1_EUR.openBankAccount(
				household1_EUR, currency, true, "transactions",
				TermType.SHORT_TERM, MoneyType.DEPOSITS);
		((HouseholdImpl) household1_EUR)
				.setBankAccountTransactions(bankAccount1_EUR);

		assertEquals(0.0, household1_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getBalance(), epsilon);

		// init household 2
		Household household2_EUR = ApplicationContext.getInstance()
				.getAgentService().findHouseholds(currency).get(1);
		CreditBank creditBank2_EUR = ApplicationContext.getInstance()
				.getAgentService().findCreditBanks(currency).get(1);
		BankAccount bankAccount2_EUR = creditBank2_EUR.openBankAccount(
				household2_EUR, currency, true, "transactions",
				TermType.SHORT_TERM, MoneyType.DEPOSITS);
		((HouseholdImpl) household2_EUR)
				.setBankAccountTransactions(bankAccount2_EUR);

		assertEquals(0.0, household2_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getBalance(), epsilon);

		// transfer money
		creditBank1_EUR.transferMoney(household1_EUR
				.getBankAccountTransactionsDelegate().getBankAccount(),
				household2_EUR.getBankAccountTransactionsDelegate()
						.getBankAccount(), 10, "Transaction");

		assertEquals(-10.0, household1_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getBalance(), epsilon);
		assertEquals(10.0, household2_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getBalance(), epsilon);
		assertNotNull(((HouseholdImpl) household2_EUR)
				.getBankAccountTransactions());

		// deconstruct credit bank 2
		creditBank2_EUR.deconstruct();

		/*
		 * credit bank 2 informs every customer of its deconstruction ->
		 * household2_EUR should have reset its transaction bank account
		 */
		assertNull(((HouseholdImpl) household2_EUR)
				.getBankAccountTransactions());

		// the bank account delegate is called again -> a new bank account
		// should be created
		household2_EUR.getBankAccountTransactionsDelegate().getBankAccount();
		assertNotNull(((HouseholdImpl) household2_EUR)
				.getBankAccountTransactions());

		// but at a different credit bank
		assertNotSame(household2_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getManagingBank(), creditBank2_EUR);

		// the new bank account has a balance of 0.0 EUR, instead of 10.0 EUR
		assertEquals(0.0, household2_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getBalance(), epsilon);
	}
}
