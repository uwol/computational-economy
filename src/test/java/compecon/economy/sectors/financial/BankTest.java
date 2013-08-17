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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import compecon.CompEconTestSupport;
import compecon.economy.sectors.financial.Bank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.engine.dao.DAOFactory;

public class BankTest extends CompEconTestSupport {

	final double epsilon = 0.0001;

	@Before
	public void setUp() {
		super.setUp();
	}

	@After
	public void tearDown() {
		super.tearDown();
	}

	@Test
	public void transferMoney() {
		Currency currency = Currency.EURO;

		Household household1_EUR = DAOFactory.getHouseholdDAO()
				.findAllByCurrency(currency).get(0);
		Household household2_EUR = DAOFactory.getHouseholdDAO()
				.findAllByCurrency(currency).get(1);

		assertEquals(0.0, household1_EUR.getTransactionsBankAccount()
				.getBalance(), epsilon);

		Bank source = household1_EUR.getTransactionsBankAccount()
				.getManagingBank();
		Bank target = household2_EUR.getTransactionsBankAccount()
				.getManagingBank();
		for (int i = 1; i < 1000; i++) {
			source.transferMoney(household1_EUR.getTransactionsBankAccount(),
					household2_EUR.getTransactionsBankAccount(), 10,
					household1_EUR.getBankPasswords().get(source),
					"Transaction" + i);
			assertEquals(-10.0 * i, household1_EUR.getTransactionsBankAccount()
					.getBalance(), epsilon);
			assertEquals(10.0 * i, household2_EUR.getTransactionsBankAccount()
					.getBalance(), epsilon);
		}
	}
}
