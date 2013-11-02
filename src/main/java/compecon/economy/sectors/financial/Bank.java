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
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.economy.sectors.Agent;
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
import compecon.economy.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.economy.sectors.state.law.security.equity.JointStockCompany;
import compecon.engine.BankAccountFactory;
import compecon.engine.dao.DAOFactory;

@Entity
public abstract class Bank extends JointStockCompany {

	/**
	 * bank account for financing bonds
	 */
	@OneToOne
	@JoinColumn(name = "bankAccountBondLoan_id")
	@Index(name = "IDX_B_BA_BONDLOAN")
	protected BankAccount bankAccountBondLoan;

	/**
	 * bank account for receiving bond coupons
	 */
	@OneToOne
	@JoinColumn(name = "bankAccountBondCoupon_id")
	@Index(name = "IDX_B_BA_BONDCOUPON")
	protected BankAccount bankAccountInterestTransactions;

	@Override
	public void deconstruct() {
		super.deconstruct();

		for (Agent agent : new ArrayList<Agent>(this.getCustomers())) {
			// implicitly deletes the associated bank accounts
			this.closeCustomerAccount(agent);
		}

		this.bankAccountBondLoan = null;
		this.bankAccountInterestTransactions = null;

		// important so that this bank is removed from the DAOs index
		DAOFactory.getBankAccountDAO().deleteAllBankAccounts(this);
	}

	/*
	 * accessors
	 */

	public BankAccount getBankAccountBondLoan() {
		return bankAccountBondLoan;
	}

	public BankAccount getBankAccountInterestTransactions() {
		return bankAccountInterestTransactions;
	}

	public void setBankAccountBondLoan(BankAccount bankAccountBondLoan) {
		this.bankAccountBondLoan = bankAccountBondLoan;
	}

	public void setBankAccountInterestTransactions(
			BankAccount bankAccountBondCoupon) {
		this.bankAccountInterestTransactions = bankAccountBondCoupon;
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
	public void assureBankAccountBondLoan() {
		if (this.isDeconstructed)
			return;

		this.assureSelfCustomerAccount();

		if (this.bankAccountBondLoan == null) {
			/*
			 * initialize the banks own bank account and open a customer account
			 * at this new bank, so that this bank can transfer money from its
			 * own bank account
			 */
			this.bankAccountBondLoan = this.primaryBank.openBankAccount(this,
					this.primaryCurrency, true, "bond loans",
					TermType.LONG_TERM, MoneyType.DEPOSITS);
		}
	}

	@Transient
	public void assureBankAccountInterestTransactions() {
		if (this.isDeconstructed)
			return;

		this.assureSelfCustomerAccount();

		if (this.bankAccountInterestTransactions == null) {
			/*
			 * initialize the banks own bank account and open a customer account
			 * at this new bank, so that this bank can transfer money from its
			 * own bank account
			 */
			this.bankAccountInterestTransactions = this.primaryBank
					.openBankAccount(this, this.primaryCurrency, true,
							"interest transactions", TermType.SHORT_TERM,
							MoneyType.DEPOSITS);
		}
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
		final Set<Agent> customers = new HashSet<Agent>();
		for (BankAccount bankAccount : DAOFactory.getBankAccountDAO()
				.findAllBankAccountsManagedByBank(this)) {
			customers.add(bankAccount.getOwner());
		}
		return customers;
	}

	@Transient
	public abstract void closeCustomerAccount(final Agent customer);

	@Transient
	public List<BankAccount> getBankAccounts(final Agent customer) {
		return DAOFactory.getBankAccountDAO().findAll(this, customer);
	}

	@Transient
	public List<BankAccount> getBankAccounts(final Agent customer,
			final Currency currency) {
		return DAOFactory.getBankAccountDAO().findAll(this, customer, currency);
	}

	@Override
	@Transient
	protected BalanceSheet issueBalanceSheet() {
		this.assureBankAccountBondLoan();
		this.assureBankAccountInterestTransactions();

		final BalanceSheet balanceSheet = super.issueBalanceSheet();

		// bank accounts of customers managed by this bank
		for (BankAccount bankAccount : DAOFactory.getBankAccountDAO()
				.findAllBankAccountsManagedByBank(this)) {
			assert (bankAccount.getCurrency().equals(this.primaryCurrency));

			if (bankAccount.getBalance() > 0.0) { // passive account
				balanceSheet.bankBorrowings += bankAccount.getBalance();
			} else {
				// active account
				balanceSheet.bankLoans += bankAccount.getBalance() * -1.0;
			}
		}

		// bank account for financing bonds
		balanceSheet.addBankAccountBalance(this.bankAccountBondLoan);

		// bank account for receiving coupons
		balanceSheet
				.addBankAccountBalance(this.bankAccountInterestTransactions);

		return balanceSheet;
	}

	@Override
	@Transient
	public void onBankCloseBankAccount(BankAccount bankAccount) {
		if (this.bankAccountInterestTransactions != null
				&& this.bankAccountInterestTransactions == bankAccount) {
			this.bankAccountInterestTransactions = null;
		}

		if (this.bankAccountBondLoan != null
				&& this.bankAccountBondLoan == bankAccount) {
			this.bankAccountBondLoan = null;
		}

		super.onBankCloseBankAccount(bankAccount);
	}

	@Transient
	public BankAccount openBankAccount(final Agent customer,
			final Currency currency, final boolean overdraftPossible,
			final String name, final TermType termType,
			final MoneyType moneyType) {
		this.assertCurrencyIsOffered(currency);

		final BankAccount bankAccount = BankAccountFactory
				.newInstanceBankAccount(customer, currency, overdraftPossible,
						this, name, termType, moneyType);
		return bankAccount;
	}

	@Transient
	public abstract void transferMoney(final BankAccount from,
			final BankAccount to, final double amount, final String subject);
}
