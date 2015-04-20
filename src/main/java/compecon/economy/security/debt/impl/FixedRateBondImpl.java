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

package compecon.economy.security.debt.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.security.debt.FixedRateBond;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.timesystem.TimeSystemEvent;
import compecon.engine.timesystem.impl.DayType;
import compecon.engine.timesystem.impl.HourType;
import compecon.engine.timesystem.impl.MonthType;
import compecon.math.util.MathUtil;

@Entity
public class FixedRateBondImpl extends BondImpl implements FixedRateBond,
		Comparable<FixedRateBond> {

	public class TransferCouponEvent implements TimeSystemEvent {
		@Override
		public boolean isDeconstructed() {
			return FixedRateBondImpl.this.isDeconstructed;
		}

		@Override
		public void onEvent() {
			assert (couponFromBankAccountDelegate != null);
			assertValidOwner();
			assertValidIssuer();

			if (couponToBankAccountDelegate != null) {
				final double dailyCouponValue = MathUtil
						.calculateMonthlyNominalInterestRate(coupon)
						/ 30.0
						* FixedRateBondImpl.this.faceValue;
				if (dailyCouponValue > 0) {
					couponFromBankAccountDelegate
							.getBankAccount()
							.getManagingBank()
							.transferMoney(
									couponFromBankAccountDelegate
											.getBankAccount(),
									couponToBankAccountDelegate
											.getBankAccount(),
									dailyCouponValue, "bond coupon");
				}
			}
		}
	}

	@Column(name = "coupon")
	protected double coupon; // interest rate in percent

	/**
	 * sender bank account (of the bond issuer and seller) for the periodical
	 * coupon
	 */
	@Transient
	protected BankAccountDelegate couponFromBankAccountDelegate;

	/**
	 * receiver bank account (of the bond buyer) for the periodical coupon;
	 * null, if the bond has not been transfered to a owner different from the
	 * issuer.
	 */
	@Transient
	protected BankAccountDelegate couponToBankAccountDelegate;

	@Override
	protected void assertValidIssuer() {
		super.assertValidIssuer();

		assert (getIssuer() == couponFromBankAccountDelegate.getBankAccount()
				.getOwner());
	}

	@Override
	protected void assertValidOwner() {
		super.assertValidOwner();

		assert (couponToBankAccountDelegate == null || owner
				.equals(couponToBankAccountDelegate.getBankAccount().getOwner()));
	}

	@Override
	@Transient
	public int compareTo(final FixedRateBond fixedRateBond) {
		if (this == fixedRateBond) {
			return 0;
		}
		if (coupon > fixedRateBond.getCoupon()) {
			return 1;
		}
		if (coupon < fixedRateBond.getCoupon()) {
			return -1;
		}

		assert id != fixedRateBond.getId();

		// important, so that two bonds with same price can exists
		return id - fixedRateBond.getId();
	}

	@Override
	public double getCoupon() {
		return coupon;
	}

	@Override
	public BankAccountDelegate getCouponFromBankAccountDelegate() {
		return couponFromBankAccountDelegate;
	}

	@Override
	public BankAccountDelegate getCouponToBankAccountDelegate() {
		return couponToBankAccountDelegate;
	}

	/*
	 * assertions
	 */

	@Override
	public void initialize() {
		super.initialize();

		// transfer coupon event; has to be HOUR_00, so that the coupon is
		// payed before possible deconstruction at HOUR_01
		final TimeSystemEvent transferCouponEvent = new TransferCouponEvent();
		timeSystemEvents.add(transferCouponEvent);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(transferCouponEvent, -1, MonthType.EVERY,
						DayType.EVERY, HourType.HOUR_00);
	}

	@Override
	@Transient
	public void resetOwner() {
		super.resetOwner();
		couponToBankAccountDelegate = null;
	}

	public void setCoupon(final double coupon) {
		this.coupon = coupon;
	}

	public void setCouponFromBankAccountDelegate(
			final BankAccountDelegate couponFromBankAccountDelegate) {
		this.couponFromBankAccountDelegate = couponFromBankAccountDelegate;
	}

	@Override
	public void setCouponToBankAccountDelegate(
			final BankAccountDelegate couponToBankAccountDelegate) {
		this.couponToBankAccountDelegate = couponToBankAccountDelegate;
	}

	@Override
	@Transient
	public String toString() {
		return super.toString() + ", coupon=[" + coupon + "]";
	}
}
