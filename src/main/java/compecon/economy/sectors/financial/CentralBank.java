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

import java.util.List;

import compecon.economy.materia.GoodType;
import compecon.economy.security.debt.FixedRateBond;

public interface CentralBank extends Bank {

	public void closeCustomerAccount(final BankCustomer customer);

	public double getAverageMarginalPriceForGoodType(final GoodType goodType);

	public BankAccountDelegate getBankAccountCentralBankMoneyDelegate();

	public double getEffectiveKeyInterestRate();

	public double getReserveRatio();

	public void obtainTender(final BankAccount moneyReservesBankAccount,
			final List<FixedRateBond> bonds);
}