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

public interface BankAccount {

	public enum MoneyType {
		CENTRALBANK_MONEY, DEPOSITS
	}

	public enum TermType {
		LONG_TERM, SHORT_TERM;
	}

	public void deposit(final double amount);

	public double getBalance();

	public Currency getCurrency();

	public int getId();

	public Bank getManagingBank();

	public MoneyType getMoneyType();

	public String getName();

	public boolean getOverdraftPossible();

	public BankCustomer getOwner();

	public TermType getTermType();

	public void withdraw(final double amount);

}
