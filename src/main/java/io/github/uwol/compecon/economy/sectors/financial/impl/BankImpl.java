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

package io.github.uwol.compecon.economy.sectors.financial.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import io.github.uwol.compecon.economy.bookkeeping.impl.BalanceSheetDTO;
import io.github.uwol.compecon.economy.sectors.financial.Bank;
import io.github.uwol.compecon.economy.sectors.financial.BankAccount;
import io.github.uwol.compecon.economy.sectors.financial.BankAccountDelegate;
import io.github.uwol.compecon.economy.sectors.financial.BankCustomer;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.economy.sectors.financial.BankAccount.MoneyType;
import io.github.uwol.compecon.economy.sectors.financial.BankAccount.TermType;
import io.github.uwol.compecon.economy.security.equity.impl.JointStockCompanyImpl;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;

@Entity
public abstract class BankImpl extends JointStockCompanyImpl implements Bank {

	/**
	 * bank account for financing bonds
	 */
	@OneToOne(targetEntity = BankAccountImpl.class)
	@JoinColumn(name = "bankAccountBondLoan_id")
	@Index(name = "IDX_B_BA_BONDLOAN")
	protected BankAccount bankAccountBondLoan;

	@Transient
	protected final BankAccountDelegate bankAccountBondLoanDelegate = new BankAccountDelegate() {
		@Override
		public BankAccount getBankAccount() {
			BankImpl.this.assureBankAccountBondLoan();
			return bankAccountBondLoan;
		}

		@Override
		public void onTransfer(final double amount) {
		}
	};

	/**
	 * bank account for receiving bond coupons
	 */
	@OneToOne(targetEntity = BankAccountImpl.class)
	@JoinColumn(name = "bankAccountBondCoupon_id")
	@Index(name = "IDX_B_BA_BONDCOUPON")
	protected BankAccount bankAccountInterestTransactions;

	@Transient
	protected final BankAccountDelegate bankAccountInterestTransactionsDelegate = new BankAccountDelegate() {
		@Override
		public BankAccount getBankAccount() {
			BankImpl.this.assureBankAccountInterestTransactions();
			return bankAccountInterestTransactions;
		}

		@Override
		public void onTransfer(final double amount) {
		}
	};

	@Transient
	protected void assertBankAccountIsManagedByThisBank(
			final BankAccount bankAccount) {
		assert (bankAccount.getManagingBank() == this);
	}

	@Transient
	protected abstract void assertCurrencyIsOffered(final Currency currency);

	@Transient
	protected void assertIdenticalMoneyType(final BankAccount from,
			final BankAccount to) {
		assert (from.getMoneyType() != null);
		assert (from.getMoneyType().equals(to.getMoneyType()));
	}

	@Transient
	protected void assertIsCustomerOfThisBank(final BankCustomer customer) {
		assert (ApplicationContext.getInstance().getBankAccountDAO()
				.findAll(this, customer).size() > 0);
	}

	@Transient
	public void assureBankAccountBondLoan() {
		if (isDeconstructed) {
			return;
		}

		if (bankAccountBondLoan == null) {
			/*
			 * initialize the banks own bank account and open a customer account
			 * at this new bank, so that this bank can transfer money from its
			 * own bank account
			 */
			bankAccountBondLoan = getPrimaryBank().openBankAccount(this,
					primaryCurrency, true, "bond loans", TermType.LONG_TERM,
					MoneyType.DEPOSITS);
		}
	}

	@Transient
	public void assureBankAccountInterestTransactions() {
		if (isDeconstructed) {
			return;
		}

		if (bankAccountInterestTransactions == null) {
			/*
			 * initialize the banks own bank account and open a customer account
			 * at this new bank, so that this bank can transfer money from its
			 * own bank account
			 */
			bankAccountInterestTransactions = getPrimaryBank().openBankAccount(
					this, primaryCurrency, true, "interest transactions",
					TermType.SHORT_TERM, MoneyType.DEPOSITS);
		}
	}

	@Override
	public void deconstruct() {
		isDeconstructed = true;

		final List<BankCustomer> customers = new ArrayList<BankCustomer>(
				getCustomers());
		for (final BankCustomer customer : customers) {
			// implicitly deletes the associated bank accounts
			closeCustomerAccount(customer);
		}

		// mandatory, so that this bank is removed from the DAOs index structure
		ApplicationContext.getInstance().getBankAccountFactory()
				.deleteAllBankAccounts(this);

		super.deconstruct();
	}

	public BankAccount getBankAccountBondLoan() {
		return bankAccountBondLoan;
	}

	@Override
	@Transient
	public BankAccountDelegate getBankAccountBondLoanDelegate() {
		return bankAccountBondLoanDelegate;
	}

	public BankAccount getBankAccountInterestTransactions() {
		return bankAccountInterestTransactions;
	}

	@Override
	@Transient
	public BankAccountDelegate getBankAccountInterestTransactionsDelegate() {
		return bankAccountInterestTransactionsDelegate;
	}

	@Transient
	public List<BankAccount> getBankAccounts(final BankCustomer customer) {
		return ApplicationContext.getInstance().getBankAccountDAO()
				.findAll(this, customer);
	}

	@Transient
	public List<BankAccount> getBankAccounts(final BankCustomer customer,
			final Currency currency) {
		return ApplicationContext.getInstance().getBankAccountDAO()
				.findAll(this, customer, currency);
	}

	@Transient
	public Set<BankCustomer> getCustomers() {
		final Set<BankCustomer> customers = new HashSet<BankCustomer>();
		for (final BankAccount bankAccount : ApplicationContext.getInstance()
				.getBankAccountDAO().findAllBankAccountsManagedByBank(this)) {
			customers.add(bankAccount.getOwner());
		}
		return customers;
	}

	@Override
	@Transient
	protected Bank getPrimaryBank() {
		return this;
	}

	@Override
	@Transient
	protected BalanceSheetDTO issueBalanceSheet() {
		assureBankAccountBondLoan();
		assureBankAccountInterestTransactions();

		final BalanceSheetDTO balanceSheet = super.issueBalanceSheet();

		// bank accounts of customers managed by this bank
		for (final BankAccount bankAccount : ApplicationContext.getInstance()
				.getBankAccountDAO().findAllBankAccountsManagedByBank(this)) {
			assert (bankAccount.getCurrency().equals(primaryCurrency));

			if (bankAccount.getBalance() > 0.0) { // passive account
				balanceSheet.bankBorrowings += bankAccount.getBalance();
			} else {
				// active account
				balanceSheet.bankLoans += bankAccount.getBalance() * -1.0;
			}
		}

		// bank account for financing bonds
		balanceSheet.addBankAccountBalance(bankAccountBondLoan);

		// bank account for receiving coupons
		balanceSheet.addBankAccountBalance(bankAccountInterestTransactions);

		return balanceSheet;
	}

	@Override
	@Transient
	public void onBankCloseBankAccount(final BankAccount bankAccount) {
		if (bankAccountInterestTransactions != null
				&& bankAccountInterestTransactions == bankAccount) {
			bankAccountInterestTransactions = null;
		}

		if (bankAccountBondLoan != null && bankAccountBondLoan == bankAccount) {
			bankAccountBondLoan = null;
		}

		super.onBankCloseBankAccount(bankAccount);
	}

	@Override
	@Transient
	public BankAccount openBankAccount(final BankCustomer customer,
			final Currency currency, final boolean overdraftPossible,
			final String name, final TermType termType,
			final MoneyType moneyType) {
		assert (!isDeconstructed());
		assertCurrencyIsOffered(currency);

		final BankAccount bankAccount = ApplicationContext
				.getInstance()
				.getBankAccountFactory()
				.newInstanceBankAccount(customer, currency, overdraftPossible,
						this, name, termType, moneyType);
		return bankAccount;
	}

	public void setBankAccountBondLoan(final BankAccount bankAccountBondLoan) {
		this.bankAccountBondLoan = bankAccountBondLoan;
	}

	public void setBankAccountInterestTransactions(
			final BankAccount bankAccountBondCoupon) {
		bankAccountInterestTransactions = bankAccountBondCoupon;
	}
}
