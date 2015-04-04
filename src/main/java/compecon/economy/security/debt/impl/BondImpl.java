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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import compecon.economy.property.impl.PropertyIssuedImpl;
import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.security.debt.Bond;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.timesystem.TimeSystemEvent;
import compecon.engine.timesystem.impl.HourType;

@Entity
public abstract class BondImpl extends PropertyIssuedImpl implements Bond {

	public class TransferFaceValueEvent implements TimeSystemEvent {
		@Override
		public boolean isDeconstructed() {
			return BondImpl.this.isDeconstructed;
		}

		@Override
		public void onEvent() {
			assert (faceValueFromBankAccountDelegate != null);
			assertValidOwner();
			assertValidIssuer();

			if (faceValueToBankAccountDelegate != null) {
				faceValueFromBankAccountDelegate
						.getBankAccount()
						.getManagingBank()
						.transferMoney(
								faceValueFromBankAccountDelegate
										.getBankAccount(),
								faceValueToBankAccountDelegate.getBankAccount(),
								faceValue, "bond face value");
			}
			deconstruct(); // delete bond from simulation
		}
	}

	@Column(name = "faceValue")
	protected double faceValue; // par value or principal

	/**
	 * sender bank account (of the bond issuer and seller) for the final face
	 * value re-transfer at the end of the bond life cycle
	 */
	@Transient
	protected BankAccountDelegate faceValueFromBankAccountDelegate;

	/**
	 * receiver bank account (of the bond buyer) for the final face value
	 * re-transfer at the end of the bond life cycle. null, if the bond has not
	 * been transfered to a owner different from the issuer.
	 */
	@Transient
	protected BankAccountDelegate faceValueToBankAccountDelegate;

	@Enumerated(value = EnumType.STRING)
	protected Currency issuedInCurrency;

	protected int termInYears = 1;

	@Transient
	protected Set<TimeSystemEvent> timeSystemEvents = new HashSet<TimeSystemEvent>();

	/*
	 * accessors
	 */

	@Override
	protected void assertValidIssuer() {
		super.assertValidIssuer();

		assert (getIssuer() == faceValueFromBankAccountDelegate
				.getBankAccount().getOwner());
	}

	@Override
	protected void assertValidOwner() {
		assert (faceValueToBankAccountDelegate == null || owner
				.equals(faceValueToBankAccountDelegate.getBankAccount()
						.getOwner()));
	}

	@Override
	@Transient
	public void deconstruct() {
		super.deconstruct();

		// deregister from TimeSystem
		ApplicationContext.getInstance().getTimeSystem()
				.removeEvents(timeSystemEvents);
	}

	@Override
	public double getFaceValue() {
		return faceValue;
	}

	/**
	 * @see #faceValueFromBankAccountDelegate
	 */
	@Override
	public BankAccountDelegate getFaceValueFromBankAccountDelegate() {
		return faceValueFromBankAccountDelegate;
	}

	/**
	 * @see #faceValueToBankAccountDelegate
	 */
	@Override
	public BankAccountDelegate getFaceValueToBankAccountDelegate() {
		return faceValueToBankAccountDelegate;
	}

	@Override
	public Currency getIssuedInCurrency() {
		return issuedInCurrency;
	}

	@Override
	public int getTermInYears() {
		return termInYears;
	}

	@Override
	public void initialize() {
		super.initialize();

		// repay face value event;
		// has to be at HOUR_01, so that at HOUR_00 the last coupon can be payed
		final TimeSystemEvent transferFaceValueEvent = new TransferFaceValueEvent();
		timeSystemEvents.add(transferFaceValueEvent);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(
						transferFaceValueEvent,
						ApplicationContext.getInstance().getTimeSystem()
								.getCurrentYear()
								+ termInYears,
						ApplicationContext.getInstance().getTimeSystem()
								.getCurrentMonthType(),
						ApplicationContext.getInstance().getTimeSystem()
								.getCurrentDayType(), HourType.HOUR_01);
	}

	@Override
	@Transient
	public void resetOwner() {
		super.resetOwner();
		faceValueToBankAccountDelegate = null;
	}

	/*
	 * assertions
	 */

	public void setFaceValue(final double faceValue) {
		this.faceValue = faceValue;
	}

	public void setFaceValueFromBankAccountDelegate(
			final BankAccountDelegate faceValueFromBankAccountDelegate) {
		this.faceValueFromBankAccountDelegate = faceValueFromBankAccountDelegate;
	}

	/*
	 * business logic
	 */

	@Override
	public void setFaceValueToBankAccountDelegate(
			final BankAccountDelegate faceValueToBankAccountDelegate) {
		this.faceValueToBankAccountDelegate = faceValueToBankAccountDelegate;
	}

	public void setIssuedInCurrency(final Currency issuedInCurrency) {
		this.issuedInCurrency = issuedInCurrency;
	}

	public void setTermInYears(final int termInYears) {
		this.termInYears = termInYears;
	}

	@Override
	@Transient
	public String toString() {
		return super.toString() + ", issuer=[" + getIssuer() + "], facevalue=["
				+ Currency.formatMoneySum(faceValue) + "], issuedInCurrency=["
				+ issuedInCurrency.getIso4217Code() + "]";
	}
}
