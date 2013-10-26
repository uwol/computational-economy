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
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
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
	protected void assertBankAccountIsManagedByThisBank(
			final BankAccount bankAccount) {
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
	protected void assertIsCustomerOfThisBank(final Agent agent) {
		assert (DAOFactory.getBankAccountDAO().findAll(this, agent).size() > 0);
	}

	@Transient
	protected void assertIdenticalMoneyType(final BankAccount from,
			final BankAccount to) {
		assert (from.getMoneyType() != null);
		assert (from.getMoneyType().equals(to.getMoneyType()));
	}

	@Transient
	protected abstract void assertCurrencyIsOffered(final Currency currency);

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
	public List<BankAccount> getBankAccounts(final Agent customer) {
		return DAOFactory.getBankAccountDAO().findAll(this, customer);
	}

	@Transient
	public List<BankAccount> getBankAccounts(final Agent customer,
			final Currency currency) {
		return DAOFactory.getBankAccountDAO().findAll(this, customer, currency);
	}

	@Transient
	public abstract void closeCustomerAccount(final Agent customer);

	@Transient
	public BankAccount openBankAccount(final Agent customer,
			final Currency currency, final boolean overdraftPossible,
			final String name, final TermType termType,
			final MoneyType moneyType) {
		this.assertCurrencyIsOffered(currency);

		BankAccount bankAccount = BankAccountFactory.newInstanceBankAccount(
				customer, currency, overdraftPossible, this, name, termType,
				moneyType);
		return bankAccount;
	}

	@Transient
	public abstract void transferMoney(final BankAccount from,
			final BankAccount to, final double amount, final String subject);

	@Transient
	protected abstract void transferMoney(final BankAccount from,
			final BankAccount to, final double amount, final String subject,
			final boolean negativeAmountOK);

	@Transient
	public double calculateMonthlyNominalInterestRate(
			final double effectiveInterestRate) {
		// http://en.wikipedia.org/wiki/Effective_interest_rate
		return effectiveInterestRate / (1 + 11 / 24 * effectiveInterestRate)
				/ 12;
	}
}
