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

package compecon.economy.sectors.state.law.security.debt;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.Simulation;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.calendar.HourType;

@Entity
public class FixedRateBond extends Bond implements Comparable<FixedRateBond> {

	@Column(name = "coupon")
	protected double coupon; // interest rate in percent

	/**
	 * receiver bank account for the periodical coupon
	 */
	@ManyToOne
	@JoinColumn(name = "couponToBankAccount_id")
	@Index(name = "couponToBankAccount")
	protected BankAccount couponToBankAccount;

	public void initialize() {
		super.initialize();

		// transfer coupon event; has to be HOUR_00, so that the coupon is
		// payed before possible deconstruction at HOUR_01
		ITimeSystemEvent transferCouponEvent = new TransferCouponEvent();
		this.timeSystemEvents.add(transferCouponEvent);
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(
						transferCouponEvent,
						-1,
						Simulation.getInstance().getTimeSystem()
								.getCurrentMonthType(),
						Simulation.getInstance().getTimeSystem()
								.getCurrentDayType(), HourType.HOUR_00);
	}

	/*
	 * accessors
	 */

	public double getCoupon() {
		return this.coupon;
	}

	public BankAccount getCouponToBankAccount() {
		return this.couponToBankAccount;
	}

	public void setCoupon(final double coupon) {
		assert (coupon >= 0.0);
		this.coupon = coupon;
	}

	public void setCouponToBankAccount(final BankAccount couponToBankAccount) {
		assert (couponToBankAccount != null);
		this.couponToBankAccount = couponToBankAccount;
	}

	/*
	 * business logic
	 */

	@Override
	@Transient
	public int compareTo(FixedRateBond fixedRateBond) {
		if (this == fixedRateBond)
			return 0;
		if (this.coupon > fixedRateBond.getCoupon())
			return 1;
		if (this.coupon < fixedRateBond.getCoupon())
			return -1;
		// important, so that two bonds with same price can exists
		return this.hashCode() - fixedRateBond.hashCode();
	}

	public class TransferCouponEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			assert (FixedRateBond.this.couponToBankAccount != null);
			assert (FixedRateBond.this.issuerBankAccount != null);

			final double couponValue = FixedRateBond.this.coupon
					* FixedRateBond.this.faceValue;
			if (couponValue > 0) {
				assertValidOwner();

				FixedRateBond.this.issuerBankAccount.getManagingBank()
						.transferMoney(FixedRateBond.this.issuerBankAccount,
								FixedRateBond.this.couponToBankAccount,
								couponValue, "bond coupon");
			}
		}
	}

	@Transient
	public String toString() {
		return this.getClass().getSimpleName() + " [Issuer: "
				+ this.issuerBankAccount.getOwner() + ", Facevalue: "
				+ Currency.formatMoneySum(this.faceValue) + " "
				+ this.issuedInCurrency.getIso4217Code() + ", Coupon: "
				+ Currency.formatMoneySum(this.coupon) + " "
				+ this.issuedInCurrency.getIso4217Code() + "]";
	}
}
