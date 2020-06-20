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

package io.github.uwol.compecon.economy.sectors.financial;

public interface CreditBank extends Bank {

	public void deposit(final BankAccount bankAccount, final double amount);

	public void depositCash(final BankCustomer customer, final BankAccount to, final double amount,
			final Currency currency);

	public BankAccountDelegate getBankAccountCentralBankMoneyReservesDelegate();

	public BankAccountDelegate getBankAccountCentralBankTransactionsDelegate();

	public BankAccountDelegate getBankAccountCurrencyTradeDelegate(final Currency currency);

	public void withdraw(final BankAccount bankAccount, final double amount);

	public double withdrawCash(final BankCustomer customer, final BankAccount from, final double amount,
			final Currency currency);
}
