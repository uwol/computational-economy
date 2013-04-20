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

	/*
	 * Accessors
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
	 * Assertions
	 */

	@Transient
	protected void assertIsClientAtThisBank(Agent agent) {
		if (this.customerPasswords.get(agent) == null)
			throw new RuntimeException(agent + " is not client at " + this);
	}

	@Transient
	protected void assertPasswordOk(Agent agent, String password) {
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
	protected void assertClientHasNoBankAccountAtThisBank(Agent client) {
		if (this.customerBankAccounts.containsKey(client))
			throw new RuntimeException("client " + client
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
	 * Business logic
	 */

	@Transient
	public Set<Agent> getCustomers() {
		return this.customerBankAccounts.keySet();
	}

	@Transient
	public BankAccount getBankAccount(Agent client, String password) {
		this.assertIsClientAtThisBank(client);
		this.assertPasswordOk(client, password);

		return this.customerBankAccounts.get(client);
	}

	@Transient
	public ArrayList<BankAccount> getBankAccounts(Agent client,
			Currency currency, String password) {
		this.assertIsClientAtThisBank(client);
		this.assertPasswordOk(client, password);

		ArrayList<BankAccount> bankAccounts = new ArrayList<BankAccount>();
		if (this.customerBankAccounts.get(client).getCurrency() == currency)
			bankAccounts.add(this.customerBankAccounts.get(client));
		return bankAccounts;
	}

	@Transient
	public String openCustomerAccount(Agent client) {
		if (this.hasBankAccount(client))
			throw new RuntimeException(client
					+ " has already a client account at this central bank");

		String password = this.generatePassword(8);
		this.customerPasswords.put(client, password);
		return password;
	}

	@Transient
	public void closeCustomerAccount(Agent client, String password) {
		this.assertTransactionsBankAccount();

		if (!this.hasBankAccount(client))
			throw new RuntimeException(client + " is not client at " + this);

		this.assertIsClientAtThisBank(client);
		this.assertPasswordOk(client, password);

		this.transferMoney(this.customerBankAccounts.get(client),
				this.transactionsBankAccount,
				this.customerBankAccounts.get(client).getBalance(), password,
				"evening-up of closed bank account", true);
		this.customerBankAccounts.remove(client);
		this.customerPasswords.remove(client);
	}

	@Transient
	public BankAccount openBankAccount(Agent client, Currency currency,
			String password) {
		this.assertIsClientAtThisBank(client);
		this.assertPasswordOk(client, password);
		this.assertCurrencyIsOffered(currency);
		this.assertClientHasNoBankAccountAtThisBank(client);

		BankAccount bankAccount = new BankAccount(client, true, currency, this);
		this.customerBankAccounts.put(client, bankAccount);
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
