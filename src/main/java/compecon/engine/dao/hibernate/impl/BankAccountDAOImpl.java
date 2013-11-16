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

package compecon.engine.dao.hibernate.impl;

import java.util.List;

import compecon.economy.agent.Agent;
import compecon.economy.sectors.financial.Bank;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.dao.BankAccountDAO;

public class BankAccountDAOImpl extends HibernateDAOImpl<BankAccount> implements
		BankAccountDAO {

	@Override
	public void deleteAllBankAccounts(Bank managingBank) {
		final List<BankAccount> bankAccounts = this
				.findAllBankAccountsManagedByBank(managingBank);
		for (BankAccount bankAccount : bankAccounts) {
			this.delete(bankAccount);
		}
	}

	@Override
	public void deleteAllBankAccounts(Bank managingBank, Agent owner) {
		final List<BankAccount> bankAccounts = this
				.findAll(managingBank, owner);
		for (BankAccount bankAccount : bankAccounts) {
			this.delete(bankAccount);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BankAccount> findAllBankAccountsManagedByBank(Bank managingBank) {
		String hql = "FROM BankAccountImpl ba WHERE ba.managingBank = :managingBank";
		return getSession().createQuery(hql)
				.setEntity("managingBank", managingBank).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BankAccount> findAllBankAccountsOfAgent(Agent owner) {
		String hql = "FROM BankAccountImpl ba WHERE ba.owner = :owner";
		return getSession().createQuery(hql).setEntity("owner", owner).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BankAccount> findAll(Bank managingBank, Agent owner) {
		String hql = "FROM BankAccountImpl ba WHERE ba.managingBank = :managingBank AND ba.owner = :owner";
		return getSession().createQuery(hql)
				.setEntity("managingBank", managingBank)
				.setEntity("owner", owner).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BankAccount> findAll(Bank managingBank, Agent owner,
			Currency currency) {
		String hql = "FROM BankAccountImpl ba WHERE ba.managingBank = :managingBank AND ba.owner = :owner AND ba.currency = :currency";
		return getSession().createQuery(hql)
				.setEntity("managingBank", managingBank)
				.setEntity("owner", owner).setParameter("currency", currency)
				.list();
	}
}
