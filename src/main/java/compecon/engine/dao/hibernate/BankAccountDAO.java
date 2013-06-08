/*
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

package compecon.engine.dao.hibernate;

import java.util.List;

import compecon.culture.sectors.financial.Bank;
import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.Currency;
import compecon.engine.Agent;
import compecon.engine.dao.DAOFactory.IBankAccountDAO;

public class BankAccountDAO extends HibernateDAO<BankAccount> implements
		IBankAccountDAO {

	@SuppressWarnings("unchecked")
	@Override
	public void deleteAllBankAccounts(Bank managingBank) {
		String hql = "FROM BankAccount ba WHERE ba.managingBank = :managingBank";
		List<BankAccount> bankAccounts = getSession().createQuery(hql)
				.setEntity("managingBank", managingBank).list();
		for (BankAccount bankAccount : bankAccounts)
			this.delete(bankAccount);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deleteAllBankAccounts(Bank managingBank, Agent owner) {
		String hql = "FROM BankAccount ba WHERE ba.managingBank = :managingBank AND ba.owner = :owner";
		List<BankAccount> bankAccounts = getSession().createQuery(hql)
				.setEntity("managingBank", managingBank)
				.setEntity("owner", owner).list();
		for (BankAccount bankAccount : bankAccounts)
			this.delete(bankAccount);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BankAccount> findAllBankAccountsManagedByBank(Bank managingBank) {
		String hql = "FROM BankAccount ba WHERE ba.managingBank = :managingBank";
		return getSession().createQuery(hql)
				.setEntity("managingBank", managingBank).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BankAccount> findAllBankAccountsOfAgent(Agent owner) {
		String hql = "FROM BankAccount ba WHERE ba.owner = :owner";
		return getSession().createQuery(hql).setEntity("owner", owner).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BankAccount> findAll(Bank managingBank, Agent owner) {
		String hql = "FROM BankAccount ba WHERE ba.managingBank = :managingBank AND ba.owner = :owner";
		return getSession().createQuery(hql)
				.setEntity("managingBank", managingBank)
				.setEntity("owner", owner).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BankAccount> findAll(Bank managingBank, Agent owner,
			Currency currency) {
		String hql = "FROM BankAccount ba WHERE ba.managingBank = :managingBank AND ba.owner = :owner AND ba.currency = :currency";
		return getSession().createQuery(hql)
				.setEntity("managingBank", managingBank)
				.setEntity("owner", owner).setParameter("currency", currency)
				.list();
	}
}
