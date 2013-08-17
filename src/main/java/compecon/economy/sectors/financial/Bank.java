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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.Transient;

import compecon.economy.sectors.financial.BankAccount.BankAccountType;
import compecon.economy.sectors.state.law.security.equity.JointStockCompany;
import compecon.engine.Agent;
import compecon.engine.BankAccountFactory;
import compecon.engine.dao.DAOFactory;

@Entity
public abstract class Bank extends JointStockCompany {

	@ElementCollection(targetClass = String.class)
	@CollectionTable(name = "Bank_CustomerPassword", joinColumns = @JoinColumn(name = "bank_id"))
	@MapKeyJoinColumn(name = "agent_id")
	@Column(name = "customerPassword", nullable = false)
	protected Map<Agent, String> customerPasswords = new HashMap<Agent, String>();

	@Override
	public void deconstruct() {
		super.deconstruct();

		for (Agent agent : new ArrayList<Agent>(this.customerPasswords.keySet())) {
			// implicitly deletes the associated bank accounts
			this.closeCustomerAccount(agent, this.customerPasswords.get(agent));
		}
	}

	/*
	 * accessors
	 */

	public Map<Agent, String> getCustomerPasswords() {
		return customerPasswords;
	}

	public void setCustomerPasswords(Map<Agent, String> customerPasswords) {
		this.customerPasswords = customerPasswords;
	}

	/*
	 * assertions
	 */

	@Transient
	protected void assertIsCustomerOfThisBank(Agent agent) {
		if (this.customerPasswords.get(agent) == null)
			throw new RuntimeException(agent + " is not client at " + this);
	}

	@Transient
	public void assertPasswordOk(Agent agent, String password) {
		if (!this.customerPasswords.containsKey(agent)
				|| this.customerPasswords.get(agent) != password)
			throw new RuntimeException("password not ok");
	}

	@Transient
	protected void assertBankAccountIsManagedByThisBank(BankAccount bankAccount) {
		if (bankAccount.getManagingBank() != this)
			throw new RuntimeException(bankAccount + " is managed by "
					+ bankAccount.getManagingBank() + " and not by " + this);
	}

	@Transient
	public void assureSelfCustomerAccount() {
		if (this.isDeconstructed)
			return;

		if (this.primaryBank == null) {
			this.primaryBank = this;
			String bankPassword = this.openCustomerAccount(this);
			this.bankPasswords.put(this, bankPassword);
		}
	}

	@Transient
	protected abstract void assertCurrencyIsOffered(Currency currency);

	/*
	 * business logic
	 */

	@Transient
	public Set<Agent> getCustomers() {
		return this.customerPasswords.keySet();
	}

	@Transient
	public List<BankAccount> getBankAccounts(Agent customer, String password) {
		this.assertIsCustomerOfThisBank(customer);
		this.assertPasswordOk(customer, password);

		return DAOFactory.getBankAccountDAO().findAll(this, customer);
	}

	@Transient
	public List<BankAccount> getBankAccounts(Agent customer, Currency currency,
			String password) {
		this.assertIsCustomerOfThisBank(customer);
		this.assertPasswordOk(customer, password);

		return DAOFactory.getBankAccountDAO().findAll(this, customer, currency);
	}

	@Transient
	public String openCustomerAccount(Agent customer) {
		if (this.customerPasswords.containsKey(customer))
			throw new RuntimeException(customer
					+ " has already a customer account at this bank");

		String password = this.generatePassword(8);
		this.customerPasswords.put(customer, password);
		return password;
	}

	@Transient
	public void closeCustomerAccount(Agent customer, String password) {
		this.assertIsCustomerOfThisBank(customer);
		this.assertPasswordOk(customer, password);
		this.assureTransactionsBankAccount();

		for (BankAccount bankAccount : DAOFactory.getBankAccountDAO().findAll(
				this, customer)) {
			if (this.transactionsBankAccount != null
					&& bankAccount != this.transactionsBankAccount) {
				this.transferMoney(bankAccount, this.transactionsBankAccount,
						bankAccount.getBalance(), password,
						"evening-up of closed bank account", true);
			}
			customer.onBankCloseBankAccount(bankAccount);
		}
		DAOFactory.getBankAccountDAO().deleteAllBankAccounts(this, customer);
		this.customerPasswords.remove(customer);
	}

	@Transient
	public BankAccount openBankAccount(Agent customer, Currency currency,
			String password, String name, BankAccountType bankAccountType) {
		this.assertIsCustomerOfThisBank(customer);
		this.assertPasswordOk(customer, password);
		this.assertCurrencyIsOffered(currency);

		BankAccount bankAccount = BankAccountFactory.newInstanceBankAccount(
				customer, true, currency, this, name, bankAccountType);
		return bankAccount;
	}

	@Transient
	public abstract void transferMoney(BankAccount from, BankAccount to,
			double amount, String password, String subject);

	@Transient
	protected abstract void transferMoney(BankAccount from, BankAccount to,
			double amount, String password, String subject,
			boolean negativeAmountOK);

	@Transient
	public double calculateMonthlyNominalInterestRate(
			double effectiveInterestRate) {
		// http://en.wikipedia.org/wiki/Effective_interest_rate
		return effectiveInterestRate / (1 + 11 / 24 * effectiveInterestRate)
				/ 12;
	}

	@Transient
	protected String generatePassword(int length) {
		char[] passwordArray = new char[length];

		Random randomizer = new Random();
		for (int i = 0; i < length; i++) {
			passwordArray[i] = (char) (randomizer.nextInt(26) + 97);
		}
		String password = new String(passwordArray);
		return password;
	}
}
