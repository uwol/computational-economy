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
import compecon.culture.sectors.state.law.property.IPropertyOwner;
import compecon.culture.sectors.state.law.property.PropertyRegister;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.HourType;
import compecon.engine.time.calendar.MonthType;

public class FixedRateBond extends Bond {
	protected class TransferCouponEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			FixedRateBond.this.transferCoupon();
		}
	}

	protected final double coupon; // interest rate

	public FixedRateBond(int maturityYear, MonthType maturityMonth,
			DayType maturityDay, BankAccount issuerBankAccount,
			String issuerBankAccountPassword, Currency issuedInCurrency,
			double coupon) {
		this(maturityYear, maturityMonth, maturityDay, issuerBankAccount,
				issuerBankAccountPassword, issuedInCurrency, coupon, 100);
	}

	public FixedRateBond(int maturityYear, MonthType maturityMonth,
			DayType maturityDay, BankAccount issuerBankAccount,
			String issuerBankAccountPassword, Currency issuedInCurrency,
			double coupon, double faceValue) {
		super(maturityYear, maturityMonth, maturityDay, issuerBankAccount,
				issuerBankAccountPassword, issuedInCurrency);

		this.faceValue = faceValue;
		this.coupon = coupon; // in percent

		// transfer coupon event; has to be HOUR_00, so that the coupon is
		// payed before possible deconstruction at HOUR_01
		ITimeSystemEvent transferCouponEvent = new TransferCouponEvent();
		this.timeSystemEvents.add(transferCouponEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				transferCouponEvent, maturityYear, maturityMonth, maturityDay,
				HourType.HOUR_00);
	}

	protected void transferCoupon() {
		IPropertyOwner owner = PropertyRegister.getInstance().getPropertyOwner(
				this);
		this.issuerBankAccount.getManagingBank().transferMoney(
				this.issuerBankAccount, owner.getTransactionsBankAccount(),
				this.coupon * this.faceValue, this.issuerBankAccountPassword,
				"bond coupon");
	}
}
