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

package compecon.culture.sectors.financial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import compecon.culture.sectors.state.law.security.equity.JointStockCompany;
import compecon.engine.Agent;
import compecon.engine.BankAccountFactory;

@Entity
public abstract class Bank extends JointStockCompany {

	@ElementCollection(targetClass = String.class)
	@CollectionTable(name = "Bank_CustomerPassword", joinColumns = @JoinColumn(name = "bank_id"))
	@MapKeyJoinColumn(name = "agent_id")
	@Column(name = "customerPassword", nullable = false)
	protected Map<Agent, String> customerPasswords = new HashMap<Agent, String>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "managingBank")
	@MapKeyJoinColumn(name = "agent_id")
	protected Map<Agent, BankAccount> customerBankAccounts = new HashMap<Agent, BankAccount>();

	@Override
	public void deconstruct() {
		super.deconstruct();

		for (Agent agent : new ArrayList<Agent>(
				this.customerBankAccounts.keySet())) {
			this.closeCustomerAccount(agent, this.customerPasswords.get(agent));
		}
	}

	/*
	 * accessors
	 */

	public Map<Agent, BankAccount> getCustomerBankAccounts() {
		return customerBankAccounts;
	}

	public Map<Agent, String> getCustomerPasswords() {
		return customerPasswords;
	}

	public void setCustomerBankAccounts(
			Map<Agent, BankAccount> customerBankAccounts) {
		this.customerBankAccounts = customerBankAccounts;
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
		if (!this.checkPassword(agent, password))
			throw new RuntimeException("password not ok");
	}

	@Transient
	protected boolean checkPassword(Agent agent, String password) {
		if (this.customerPasswords.get(agent) != null
				&& this.customerPasswords.get(agent) == password)
			return true;
		return false;
	}

	@Transient
	protected void assertCustomerHasNoBankAccountAtThisBank(Agent customer) {
		if (this.customerBankAccounts.containsKey(customer))
			throw new RuntimeException("customer " + customer
					+ " has already a bank account at this bank");
	}

	@Transient
	protected void assertBankAccountIsManagedByThisBank(BankAccount bankAccount) {
		if (bankAccount.getManagingBank() != this)
			throw new RuntimeException(bankAccount + " is managed by "
					+ bankAccount.getManagingBank() + " and not by " + this);
	}

	@Transient
	protected abstract void assertCurrencyIsOffered(Currency currency);

	/*
	 * business logic
	 */

	@Transient
	public Set<Agent> getCustomers() {
		return this.customerBankAccounts.keySet();
	}

	@Transient
	public BankAccount getBankAccount(Agent customer, String password) {
		this.assertIsCustomerOfThisBank(customer);
		this.assertPasswordOk(customer, password);

		return this.customerBankAccounts.get(customer);
	}

	@Transient
	public ArrayList<BankAccount> getBankAccounts(Agent customer,
			Currency currency, String password) {
		this.assertIsCustomerOfThisBank(customer);
		this.assertPasswordOk(customer, password);

		ArrayList<BankAccount> bankAccounts = new ArrayList<BankAccount>();
		if (this.customerBankAccounts.get(customer).getCurrency() == currency)
			bankAccounts.add(this.customerBankAccounts.get(customer));
		return bankAccounts;
	}

	@Transient
	public String openCustomerAccount(Agent customer) {
		if (this.hasBankAccount(customer))
			throw new RuntimeException(customer
					+ " has already a client account at this central bank");

		String password = this.generatePassword(8);
		this.customerPasswords.put(customer, password);
		return password;
	}

	@Transient
	public void closeCustomerAccount(Agent customer, String password) {
		this.assertIsCustomerOfThisBank(customer);
		this.assertPasswordOk(customer, password);
		this.assureTransactionsBankAccount();

		if (this.hasBankAccount(customer)) {
			BankAccount bankAccount = this.customerBankAccounts.get(customer);
			if (this.transactionsBankAccount != null)
				this.transferMoney(bankAccount, this.transactionsBankAccount,
						this.customerBankAccounts.get(customer).getBalance(),
						password, "evening-up of closed bank account", true);
			this.customerBankAccounts.remove(customer);
			customer.onBankCloseCustomerAccount(bankAccount);
			BankAccountFactory.deleteBankAccount(bankAccount);
		}

		this.customerPasswords.remove(customer);
	}

	@Transient
	public BankAccount openBankAccount(Agent customer, Currency currency,
			String password) {
		this.assertIsCustomerOfThisBank(customer);
		this.assertPasswordOk(customer, password);
		this.assertCurrencyIsOffered(currency);
		this.assertCustomerHasNoBankAccountAtThisBank(customer);

		BankAccount bankAccount = BankAccountFactory.newInstanceBankAccount(
				customer, true, currency, this);
		this.customerBankAccounts.put(customer, bankAccount);
		return bankAccount;
	}

	@Transient
	protected boolean hasBankAccount(Agent agent) {
		return this.customerBankAccounts.containsKey(agent);
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
