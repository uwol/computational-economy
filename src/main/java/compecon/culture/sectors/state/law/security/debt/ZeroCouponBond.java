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

package compecon.culture.sectors.state.law.security.debt;

import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.Currency;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.MonthType;

public class ZeroCouponBond extends Bond {
	public ZeroCouponBond(int maturityYear, MonthType maturityMonth,
			DayType maturityDay, BankAccount issuerBankAccount,
			String issuerBankAccountPassword, Currency issuedInCurrency) {
		super(maturityYear, maturityMonth, maturityDay, issuerBankAccount,
				issuerBankAccountPassword, issuedInCurrency);
		this.faceValue = 100;
	}
}
