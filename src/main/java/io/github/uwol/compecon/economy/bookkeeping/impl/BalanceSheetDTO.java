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

package io.github.uwol.compecon.economy.bookkeeping.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.sectors.financial.BankAccount;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.economy.sectors.financial.BankAccount.MoneyType;
import io.github.uwol.compecon.economy.sectors.financial.BankAccount.TermType;
import io.github.uwol.compecon.economy.security.equity.Share;

public class BalanceSheetDTO {

	// borrowings from customers in banking context -> passive accounts
	public double bankBorrowings;

	/*
	 * Assets ----------------------------------------
	 */

	// money given as loans to other agents in banking context -> active
	// accounts
	public double bankLoans;

	// owned securities / bonds, shares or other properties
	public double bonds;

	public double cashCentralBankLongTerm;

	public double cashCentralBankShortTerm;

	// cash in bank foreign currency deposits, demand deposits; denominated in
	// local currency
	public double cashForeignCurrency;

	public double cashGiroLongTerm;

	// Accounts receivable also known as Debtors, is money owed to a business by
	// its clients (customers) and shown on its balance sheet as an asset. ->
	// http://en.wikipedia.org/wiki/Accounts_receivable
	// public double accountsReceivable;

	// money borrowed to other agents
	// public double borrowings;

	// cash on bank accounts -> passive accounts
	public double cashGiroShortTerm;

	// issued bonds
	public double financialLiabilities;

	// cash in notes and coins
	public double hardCash;

	// owned good types by number of pieces
	public final Map<GoodType, Double> inventoryQuantitative = new HashMap<GoodType, Double>();

	// owned good types by value in referenceCurrency
	public double inventoryValue;

	/*
	 * Equity ----------------------------------------------
	 */

	public Set<Share> issuedCapital = new HashSet<Share>();

	public double loansCentralBankLongTerm;

	/*
	 * Liabilities --------------------------------------
	 */

	// Accounts payable is money owed by a business to its suppliers and shown
	// on its Balance Sheet as a liability. ->
	// http://en.wikipedia.org/wiki/Accounts_payable
	// public double accountsPayable;

	public double loansCentralBankShortTerm;

	public double loansGiroLongTerm;

	// money given as loans to other agents in banking context -> active
	// accounts
	public double loansGiroShortTerm;

	public final Currency referenceCurrency;

	public BalanceSheetDTO(final Currency referenceCurrency) {
		this.referenceCurrency = referenceCurrency;
	}

	public void addBankAccountBalance(final BankAccount bankAccount) {
		if (bankAccount != null) {
			if (bankAccount.getBalance() > 0.0) {
				addCash(bankAccount.getMoneyType(), bankAccount.getTermType(),
						bankAccount.getBalance());
			} else {
				addLoan(bankAccount.getMoneyType(), bankAccount.getTermType(),
						-1.0 * bankAccount.getBalance());
			}
		}
	}

	protected void addCash(final MoneyType moneyType, final TermType termType,
			final double value) {
		switch (moneyType) {
		case DEPOSITS:
			switch (termType) {
			case SHORT_TERM:
				cashGiroShortTerm += value;
				break;
			case LONG_TERM:
				cashGiroLongTerm += value;
				break;
			}
			break;
		case CENTRALBANK_MONEY:
			switch (termType) {
			case SHORT_TERM:
				cashCentralBankShortTerm += value;
				break;
			case LONG_TERM:
				cashCentralBankLongTerm += value;
				break;
			}
			break;
		}
	}

	protected void addLoan(final MoneyType moneyType, final TermType termType,
			final double value) {
		switch (moneyType) {
		case DEPOSITS:
			switch (termType) {
			case SHORT_TERM:
				loansGiroShortTerm += value;
				break;
			case LONG_TERM:
				loansGiroLongTerm += value;
				break;
			}
			break;
		case CENTRALBANK_MONEY:
			switch (termType) {
			case SHORT_TERM:
				loansCentralBankShortTerm += value;
				break;
			case LONG_TERM:
				loansCentralBankLongTerm += value;
				break;
			}
			break;
		}
	}

	public double getBalanceActive() {
		return hardCash + cashGiroShortTerm + cashGiroLongTerm
				+ cashCentralBankShortTerm + cashCentralBankLongTerm
				+ cashForeignCurrency + bankLoans + bonds + inventoryValue;
	}

	public double getBalancePassive() {
		return loansGiroShortTerm + loansGiroLongTerm
				+ loansCentralBankShortTerm + loansCentralBankLongTerm
				+ financialLiabilities + bankBorrowings + getEquity();
	}

	public double getEquity() {
		return getBalanceActive() - loansGiroShortTerm - loansGiroLongTerm
				- loansCentralBankShortTerm - loansCentralBankLongTerm
				- financialLiabilities - bankBorrowings;
	}

}
