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

package compecon.economy.sectors.financial.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.economy.agent.Agent;
import compecon.economy.bookkeeping.impl.BalanceSheetDTO;
import compecon.economy.sectors.financial.Bank;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.security.equity.impl.JointStockCompanyImpl;
import compecon.engine.applicationcontext.ApplicationContext;

@Entity
public abstract class BankImpl extends JointStockCompanyImpl implements Bank {

	/**
	 * bank account for financing bonds
	 */
	@OneToOne(targetEntity = BankAccountImpl.class)
	@JoinColumn(name = "bankAccountBondLoan_id")
	@Index(name = "IDX_B_BA_BONDLOAN")
	protected BankAccount bankAccountBondLoan;

	/**
	 * bank account for receiving bond coupons
	 */
	@OneToOne(targetEntity = BankAccountImpl.class)
	@JoinColumn(name = "bankAccountBondCoupon_id")
	@Index(name = "IDX_B_BA_BONDCOUPON")
	protected BankAccount bankAccountInterestTransactions;

	@Override
	public void deconstruct() {
		this.isDeconstructed = true;

		final List<Agent> customers = new ArrayList<Agent>(this.getCustomers());
		for (Agent agent : customers) {
			// implicitly deletes the associated bank accounts
			this.closeCustomerAccount(agent);
		}

		// mandatory, so that this bank is removed from the DAOs index structure
		ApplicationContext.getInstance().getBankAccountService()
				.deleteAllBankAccounts(this);

		super.deconstruct();
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

		if (this.bankAccountBondLoan == null) {
			/*
			 * initialize the banks own bank account and open a customer account
			 * at this new bank, so that this bank can transfer money from its
			 * own bank account
			 */
			this.bankAccountBondLoan = this.getPrimaryBank().openBankAccount(
					this, this.primaryCurrency, true, "bond loans",
					TermType.LONG_TERM, MoneyType.DEPOSITS);
		}
	}

	@Transient
	public void assureBankAccountInterestTransactions() {
		if (this.isDeconstructed)
			return;

		if (this.bankAccountInterestTransactions == null) {
			/*
			 * initialize the banks own bank account and open a customer account
			 * at this new bank, so that this bank can transfer money from its
			 * own bank account
			 */
			this.bankAccountInterestTransactions = this.getPrimaryBank()
					.openBankAccount(this, this.primaryCurrency, true,
							"interest transactions", TermType.SHORT_TERM,
							MoneyType.DEPOSITS);
		}
	}

	@Transient
	protected void assertIsCustomerOfThisBank(final Agent agent) {
		assert (ApplicationContext.getInstance().getBankAccountDAO()
				.findAll(this, agent).size() > 0);
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
	public BankAccountDelegate getBankAccountBondLoanDelegate() {
		final BankAccountDelegate delegate = new BankAccountDelegate() {
			@Override
			public BankAccount getBankAccount() {
				BankImpl.this.assureBankAccountBondLoan();
				return BankImpl.this.bankAccountBondLoan;
			}

			@Override
			public void onTransfer(final double amount) {
			}
		};
		return delegate;
	}

	@Transient
	public BankAccountDelegate getBankAccountInterestTransactionsDelegate() {
		final BankAccountDelegate delegate = new BankAccountDelegate() {
			@Override
			public BankAccount getBankAccount() {
				BankImpl.this.assureBankAccountInterestTransactions();
				return BankImpl.this.bankAccountInterestTransactions;
			}

			@Override
			public void onTransfer(final double amount) {
			}
		};
		return delegate;
	}

	@Transient
	public List<BankAccount> getBankAccounts(final Agent customer) {
		return ApplicationContext.getInstance().getBankAccountDAO()
				.findAll(this, customer);
	}

	@Transient
	public List<BankAccount> getBankAccounts(final Agent customer,
			final Currency currency) {
		return ApplicationContext.getInstance().getBankAccountDAO()
				.findAll(this, customer, currency);
	}

	@Transient
	public Set<Agent> getCustomers() {
		final Set<Agent> customers = new HashSet<Agent>();
		for (BankAccount bankAccount : ApplicationContext.getInstance()
				.getBankAccountDAO().findAllBankAccountsManagedByBank(this)) {
			customers.add(bankAccount.getOwner());
		}
		return customers;
	}

	@Transient
	protected Bank getPrimaryBank() {
		return this;
	}

	@Override
	@Transient
	protected BalanceSheetDTO issueBalanceSheet() {
		this.assureBankAccountBondLoan();
		this.assureBankAccountInterestTransactions();

		final BalanceSheetDTO balanceSheet = super.issueBalanceSheet();

		// bank accounts of customers managed by this bank
		for (BankAccount bankAccount : ApplicationContext.getInstance()
				.getBankAccountDAO().findAllBankAccountsManagedByBank(this)) {
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
		assert (!this.isDeconstructed());
		this.assertCurrencyIsOffered(currency);

		final BankAccount bankAccount = ApplicationContext
				.getInstance()
				.getBankAccountService()
				.newInstanceBankAccount(customer, currency, overdraftPossible,
						this, name, termType, moneyType);
		return bankAccount;
	}
}
