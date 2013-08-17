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

package compecon.economy.sectors.state.law.bookkeeping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.law.security.equity.Share;
import compecon.materia.GoodType;

public class BalanceSheet {

	public final Currency referenceCurrency;

	/*
	 * Assets ----------------------------------------
	 */

	// cash in notes and coins
	public double hardCash;

	// cash in bank deposits, demand deposits
	public double cashShortTerm;

	// cash in bank deposits, savings deposits
	public double cashLongTerm;

	// Accounts receivable also known as Debtors, is money owed to a business by
	// its clients (customers) and shown on its balance sheet as an asset. ->
	// http://en.wikipedia.org/wiki/Accounts_receivable
	// public double accountsReceivable;

	// money borrowed to other agents
	// public double borrowings;

	// money given as loans to other agents in banking context -> active
	// accounts
	public double bankLoans;

	// owned securities / bonds, shares or other properties
	public double bonds;

	// owned good types
	public Map<GoodType, Double> inventory = new HashMap<GoodType, Double>();

	/*
	 * Liabilities --------------------------------------
	 */

	// Accounts payable is money owed by a business to its suppliers and shown
	// on its Balance Sheet as a liability. ->
	// http://en.wikipedia.org/wiki/Accounts_payable
	// public double accountsPayable;

	// money borrowed from other agents as loans from them
	public double loans;

	// issued bonds
	public double financialLiabilities;

	// borrowings from customers in banking context -> passive accounts
	public double bankBorrowings;

	/*
	 * Equity ----------------------------------------------
	 */

	public Set<Share> issuedCapital = new HashSet<Share>();

	public BalanceSheet(Currency referenceCurrency) {
		this.referenceCurrency = referenceCurrency;
	}
}
