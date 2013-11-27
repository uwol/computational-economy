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

import compecon.economy.agent.Agent;
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
import compecon.economy.security.equity.JointStockCompany;

public interface Bank extends Agent, JointStockCompany {

	public abstract void closeCustomerAccount(final Agent customer);

	public BankAccountDelegate getBankAccountBondLoanDelegate();

	public BankAccountDelegate getBankAccountInterestTransactionsDelegate();

	public BankAccount openBankAccount(final Agent customer,
			final Currency currency, final boolean overdraftPossible,
			final String name, final TermType termType,
			final MoneyType moneyType);

	public void transferMoney(final BankAccount from, final BankAccount to,
			final double amount, final String subject);

}
