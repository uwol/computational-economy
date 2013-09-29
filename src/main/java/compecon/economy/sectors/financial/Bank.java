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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Transient;

import compecon.economy.sectors.Agent;
import compecon.economy.sectors.financial.BankAccount.BankAccountType;
import compecon.economy.sectors.state.law.security.equity.JointStockCompany;
import compecon.engine.BankAccountFactory;
import compecon.engine.dao.DAOFactory;

@Entity
public abstract class Bank extends JointStockCompany {

	@Override
	public void deconstruct() {
		super.deconstruct();

		for (Agent agent : new ArrayList<Agent>(this.getCustomers())) {
			// implicitly deletes the associated bank accounts
			this.closeCustomerAccount(agent);
		}

		// important so that this bank is removed from the DAOs index
		DAOFactory.getBankAccountDAO().deleteAllBankAccounts(this);
	}

	/*
	 * assertions
	 */

	@Transient
	protected void assertBankAccountIsManagedByThisBank(BankAccount bankAccount) {
		assert (bankAccount.getManagingBank() == this);
	}

	@Transient
	public void assureSelfCustomerAccount() {
		if (this.isDeconstructed)
			return;

		if (this.primaryBank == null) {
			this.primaryBank = this;
		}
	}

	@Transient
	protected void assertIsCustomerOfThisBank(Agent agent) {
		assert (DAOFactory.getBankAccountDAO().findAll(this, agent).size() > 0);
	}

	@Transient
	protected abstract void assertCurrencyIsOffered(Currency currency);

	/*
	 * business logic
	 */

	@Transient
	public Set<Agent> getCustomers() {
		Set<Agent> customers = new HashSet<Agent>();
		for (BankAccount bankAccount : DAOFactory.getBankAccountDAO()
				.findAllBankAccountsManagedByBank(this)) {
			customers.add(bankAccount.getOwner());
		}
		return customers;
	}

	@Transient
	public List<BankAccount> getBankAccounts(Agent customer) {
		return DAOFactory.getBankAccountDAO().findAll(this, customer);
	}

	@Transient
	public List<BankAccount> getBankAccounts(Agent customer, Currency currency) {
		return DAOFactory.getBankAccountDAO().findAll(this, customer, currency);
	}

	@Transient
	public void closeCustomerAccount(Agent customer) {
		this.assureTransactionsBankAccount();

		for (BankAccount bankAccount : DAOFactory.getBankAccountDAO().findAll(
				this, customer)) {
			if (this.transactionsBankAccount != null
					&& bankAccount != this.transactionsBankAccount) {
				this.transferMoney(bankAccount, this.transactionsBankAccount,
						bankAccount.getBalance(),
						"evening-up of closed bank account", true);
			}
			customer.onBankCloseBankAccount(bankAccount);
		}
		DAOFactory.getBankAccountDAO().deleteAllBankAccounts(this, customer);
	}

	@Transient
	public BankAccount openBankAccount(Agent customer, Currency currency,
			String name, BankAccountType bankAccountType) {
		this.assertCurrencyIsOffered(currency);

		BankAccount bankAccount = BankAccountFactory.newInstanceBankAccount(
				customer, true, currency, this, name, bankAccountType);
		return bankAccount;
	}

	@Transient
	public abstract void transferMoney(BankAccount from, BankAccount to,
			double amount, String subject);

	@Transient
	protected abstract void transferMoney(BankAccount from, BankAccount to,
			double amount, String subject, boolean negativeAmountOK);

	@Transient
	public double calculateMonthlyNominalInterestRate(
			double effectiveInterestRate) {
		// http://en.wikipedia.org/wiki/Effective_interest_rate
		return effectiveInterestRate / (1 + 11 / 24 * effectiveInterestRate)
				/ 12;
	}
}
