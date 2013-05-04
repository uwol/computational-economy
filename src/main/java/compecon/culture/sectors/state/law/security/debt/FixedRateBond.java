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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import compecon.culture.sectors.state.law.property.IPropertyOwner;
import compecon.culture.sectors.state.law.property.PropertyRegister;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.TimeSystem;
import compecon.engine.time.calendar.HourType;

@Entity
@Table(name = "FixedRateBond")
public class FixedRateBond extends Bond {

	@Column(name = "coupon")
	protected double coupon; // interest rate in percent

	public void initialize() {
		super.initialize();

		// transfer coupon event; has to be HOUR_00, so that the coupon is
		// payed before possible deconstruction at HOUR_01
		ITimeSystemEvent transferCouponEvent = new TransferCouponEvent();
		this.timeSystemEvents.add(transferCouponEvent);
		TimeSystem.getInstance().addEvent(transferCouponEvent,
				TimeSystem.getInstance().getCurrentYear() + 2,
				TimeSystem.getInstance().getCurrentMonthType(),
				TimeSystem.getInstance().getCurrentDayType(), HourType.HOUR_00);
	}

	/*
	 * accessors
	 */

	public double getCoupon() {
		return coupon;
	}

	public void setCoupon(double coupon) {
		this.coupon = coupon;
	}

	/*
	 * business logic
	 */

	@Transient
	protected void transferCoupon() {
		IPropertyOwner owner = PropertyRegister.getInstance().getPropertyOwner(
				this);
		this.issuerBankAccount.getManagingBank().transferMoney(
				this.issuerBankAccount, owner.getTransactionsBankAccount(),
				this.coupon * this.faceValue, this.issuerBankAccountPassword,
				"bond coupon");
	}

	protected class TransferCouponEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			FixedRateBond.this.transferCoupon();
		}
	}
}
