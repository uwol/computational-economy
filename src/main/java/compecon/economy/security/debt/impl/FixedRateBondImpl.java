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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.economy.agent.Agent;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.financial.impl.BankAccountImpl;
import compecon.economy.security.debt.FixedRateBond;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.timesystem.ITimeSystemEvent;
import compecon.engine.timesystem.impl.DayType;
import compecon.engine.timesystem.impl.HourType;
import compecon.engine.timesystem.impl.MonthType;
import compecon.engine.util.MathUtil;

@Entity
public class FixedRateBondImpl extends BondImpl implements FixedRateBond,
		Comparable<FixedRateBond> {

	@Column(name = "coupon")
	protected double coupon; // interest rate in percent

	/**
	 * sender bank account (of the bond issuer and seller) for the periodical
	 * coupon
	 */
	@ManyToOne(targetEntity = BankAccountImpl.class)
	@JoinColumn(name = "couponFromBankAccount_id")
	@Index(name = "couponFromBankAccount")
	protected BankAccount couponFromBankAccount;

	/**
	 * receiver bank account (of the bond buyer) for the periodical coupon;
	 * null, if the bond has not been transfered to a owner different from the
	 * issuer.
	 */
	@ManyToOne(targetEntity = BankAccountImpl.class)
	@JoinColumn(name = "couponToBankAccount_id")
	@Index(name = "couponToBankAccount")
	protected BankAccount couponToBankAccount;

	public void initialize() {
		super.initialize();

		// transfer coupon event; has to be HOUR_00, so that the coupon is
		// payed before possible deconstruction at HOUR_01
		final ITimeSystemEvent transferCouponEvent = new TransferCouponEvent();
		this.timeSystemEvents.add(transferCouponEvent);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(transferCouponEvent, -1, MonthType.EVERY,
						DayType.EVERY, HourType.HOUR_00);
	}

	/*
	 * accessors
	 */

	public double getCoupon() {
		return this.coupon;
	}

	public BankAccount getCouponFromBankAccount() {
		return this.couponFromBankAccount;
	}

	public BankAccount getCouponToBankAccount() {
		return this.couponToBankAccount;
	}

	public void setCoupon(final double coupon) {
		this.coupon = coupon;
	}

	public void setCouponFromBankAccount(final BankAccount couponFromBankAccount) {
		this.couponFromBankAccount = couponFromBankAccount;
	}

	public void setCouponToBankAccount(final BankAccount couponToBankAccount) {
		this.couponToBankAccount = couponToBankAccount;
	}

	/*
	 * assertions
	 */

	@Override
	protected void assertValidOwner() {
		super.assertValidOwner();
		assert (this.couponToBankAccount == null || this.owner
				.equals(this.couponToBankAccount.getOwner()));
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

	@Override
	public Agent getIssuer() {
		final Agent issuer = super.getIssuer();
		assert (issuer == this.couponFromBankAccount.getOwner());
		return issuer;
	}

	@Transient
	public String toString() {
		return this.getClass().getSimpleName() + " [Issuer: "
				+ this.faceValueFromBankAccount.getOwner() + ", Facevalue: "
				+ Currency.formatMoneySum(this.faceValue) + " "
				+ this.issuedInCurrency.getIso4217Code() + ", Coupon: "
				+ Currency.formatMoneySum(this.coupon) + " "
				+ this.issuedInCurrency.getIso4217Code() + "]";
	}

	public class TransferCouponEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			assert (FixedRateBondImpl.this.couponFromBankAccount != null);
			assertValidOwner();

			if (FixedRateBondImpl.this.couponToBankAccount != null) {
				final double dailyCouponValue = MathUtil
						.calculateMonthlyNominalInterestRate(FixedRateBondImpl.this.coupon)
						/ 30.0 * FixedRateBondImpl.this.faceValue;
				if (dailyCouponValue > 0) {
					FixedRateBondImpl.this.couponFromBankAccount
							.getManagingBank()
							.transferMoney(
									FixedRateBondImpl.this.couponFromBankAccount,
									FixedRateBondImpl.this.couponToBankAccount,
									dailyCouponValue, "bond coupon");
				}
			}
		}
	}
}
