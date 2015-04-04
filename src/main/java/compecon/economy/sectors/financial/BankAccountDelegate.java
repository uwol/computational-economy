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

/**
 * interface for delegating to a bank account. Is applied in the context of
 * bonds, shares etc. where the delegate has to identify bank accounts involved
 * in dividenc transactions etc.<br />
 * <br />
 * This pattern allows lazy evaluation and loose coupling of bank accounts to
 * bonds, shares etc.
 */
public interface BankAccountDelegate {

	/**
	 * the delegated bank account
	 *
	 * @return must not be null
	 */
	public BankAccount getBankAccount();

	public void onTransfer(final double amount);
}
