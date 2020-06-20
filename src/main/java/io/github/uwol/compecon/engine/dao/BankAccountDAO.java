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

package io.github.uwol.compecon.engine.dao;

import java.util.List;

import io.github.uwol.compecon.economy.sectors.financial.Bank;
import io.github.uwol.compecon.economy.sectors.financial.BankAccount;
import io.github.uwol.compecon.economy.sectors.financial.BankCustomer;
import io.github.uwol.compecon.economy.sectors.financial.Currency;

public interface BankAccountDAO extends GenericDAO<BankAccount> {

	/**
	 * WARNING: Should only be called from the bank account factory, which ensures a
	 * subsequent Hibernate flush.
	 *
	 * @see io.github.uwol.compecon.engine.factory.BankAccountFactory
	 */
	public void deleteAllBankAccounts(final Bank managingBank);

	/**
	 * WARNING: Should only be called from the bank account factory, which ensures a
	 * subsequent Hibernate flush.
	 *
	 * @see io.github.uwol.compecon.engine.factory.BankAccountFactory
	 */
	public void deleteAllBankAccounts(final Bank managingBank, final BankCustomer owner);

	public List<BankAccount> findAll(final Bank managingBank, final BankCustomer owner);

	public List<BankAccount> findAll(final Bank managingBank, final BankCustomer owner, final Currency currency);

	public List<BankAccount> findAllBankAccountsManagedByBank(final Bank managingBank);

	public List<BankAccount> findAllBankAccountsOfAgent(final BankCustomer owner);
}
