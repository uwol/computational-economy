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

package compecon.engine.factory.impl;

import compecon.economy.sectors.financial.Bank;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
import compecon.economy.sectors.financial.BankCustomer;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.financial.impl.BankAccountImpl;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.factory.BankAccountFactory;
import compecon.engine.util.HibernateUtil;

public class BankAccountImplFactoryImpl implements BankAccountFactory {

	public void deleteBankAccount(final BankAccount bankAccount) {
		ApplicationContext.getInstance().getBankAccountDAO()
				.delete(bankAccount);
		HibernateUtil.flushSession();
	}

	@Override
	public void deleteAllBankAccounts(final Bank managingBank) {
		ApplicationContext.getInstance().getBankAccountDAO()
				.deleteAllBankAccounts(managingBank);
		HibernateUtil.flushSession();
	}

	@Override
	public void deleteAllBankAccounts(final Bank managingBank,
			final BankCustomer owner) {
		ApplicationContext.getInstance().getBankAccountDAO()
				.deleteAllBankAccounts(managingBank, owner);
		HibernateUtil.flushSession();
	}

	public BankAccount newInstanceBankAccount(final BankCustomer owner,
			final Currency currency, final boolean overdraftPossible,
			final Bank managingBank, final String name,
			final TermType termType, final MoneyType moneyType) {
		BankAccountImpl bankAccount = new BankAccountImpl();
		if (!HibernateUtil.isActive())
			bankAccount.setId(ApplicationContext.getInstance()
					.getSequenceNumberGenerator().getNextId());

		bankAccount.setOwner(owner);
		bankAccount.setOverdraftPossible(overdraftPossible);
		bankAccount.setCurrency(currency);
		bankAccount.setManagingBank(managingBank);
		bankAccount.setName(name);
		bankAccount.setTermType(termType);
		bankAccount.setMoneyType(moneyType);

		ApplicationContext.getInstance().getBankAccountDAO().save(bankAccount);
		HibernateUtil.flushSession();
		return bankAccount;
	}
}
