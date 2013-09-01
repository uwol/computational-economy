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

package compecon.engine.dao.inmemory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import compecon.economy.sectors.Agent;
import compecon.economy.sectors.financial.Bank;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.dao.DAOFactory.IBankAccountDAO;

public class BankAccountDAO extends AgentIndexedInMemoryDAO<BankAccount>
		implements IBankAccountDAO {

	protected Map<Bank, List<BankAccount>> bankAccounts = new HashMap<Bank, List<BankAccount>>();

	/*
	 * helpers
	 */

	private void assureInitializedDataStructure(Bank bank) {
		if (!this.bankAccounts.containsKey(bank))
			this.bankAccounts.put(bank, new ArrayList<BankAccount>());
	}

	/*
	 * actions
	 */

	@Override
	public synchronized void delete(BankAccount bankAccount) {
		assureInitializedDataStructure(bankAccount.getManagingBank());

		this.bankAccounts.get(bankAccount.getManagingBank())
				.remove(bankAccount);
		super.delete(bankAccount);
	}

	@Override
	public void deleteAllBankAccounts(Bank managingBank) {
		assureInitializedDataStructure(managingBank);

		for (BankAccount bankAccount : new HashSet<BankAccount>(
				this.bankAccounts.get(managingBank))) {
			this.delete(bankAccount);
		}
		this.bankAccounts.remove(managingBank);
	}

	@Override
	public void deleteAllBankAccounts(Bank managingBank, Agent owner) {
		assureInitializedDataStructure(managingBank);

		for (BankAccount bankAccount : new HashSet<BankAccount>(
				this.getInstancesForAgent(owner))) {
			if (bankAccount.getManagingBank() == managingBank)
				this.delete(bankAccount);
		}
	}

	@Override
	public List<BankAccount> findAllBankAccountsManagedByBank(Bank managingBank) {
		assureInitializedDataStructure(managingBank);

		List<BankAccount> bankAccountManagedByBank = this.bankAccounts
				.get(managingBank);
		return new ArrayList<BankAccount>(bankAccountManagedByBank);
	}

	@Override
	public List<BankAccount> findAllBankAccountsOfAgent(Agent owner) {
		List<BankAccount> bankAccounts = this.getInstancesForAgent(owner);
		if (bankAccounts != null)
			return new ArrayList<BankAccount>(bankAccounts);
		return new ArrayList<BankAccount>();
	}

	@Override
	public List<BankAccount> findAll(Bank managingBank, Agent owner) {
		List<BankAccount> bankAccounts = new ArrayList<BankAccount>();
		for (BankAccount bankAccount : this.findAllBankAccountsOfAgent(owner)) {
			if (bankAccount.getManagingBank() == managingBank)
				bankAccounts.add(bankAccount);
		}
		return bankAccounts;
	}

	@Override
	public List<BankAccount> findAll(Bank managingBank, Agent owner,
			Currency currency) {
		List<BankAccount> bankAccounts = new ArrayList<BankAccount>();
		for (BankAccount bankAccount : this.findAllBankAccountsOfAgent(owner)) {
			if (bankAccount.getManagingBank() == managingBank
					&& currency.equals(bankAccount.getCurrency()))
				bankAccounts.add(bankAccount);
		}
		return bankAccounts;
	}

	@Override
	public synchronized void save(BankAccount bankAccount) {
		assureInitializedDataStructure(bankAccount.getManagingBank());

		this.bankAccounts.get(bankAccount.getManagingBank()).add(bankAccount);
		super.save(bankAccount.getOwner(), bankAccount);
	}
}
