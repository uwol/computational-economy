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

import java.util.ArrayList;
import java.util.List;

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
import compecon.engine.timesystem.ITimeSystemEvent;
import compecon.engine.timesystem.impl.HourType;

@Entity
public abstract class BondImpl extends PropertyIssuedImpl implements Bond {

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
	protected List<ITimeSystemEvent> timeSystemEvents = new ArrayList<ITimeSystemEvent>();

	public void initialize() {
		super.initialize();

		// repay face value event;
		// has to be at HOUR_01, so that at HOUR_00 the last coupon can be payed
		final ITimeSystemEvent transferFaceValueEvent = new TransferFaceValueEvent();
		this.timeSystemEvents.add(transferFaceValueEvent);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(
						transferFaceValueEvent,
						ApplicationContext.getInstance().getTimeSystem()
								.getCurrentYear()
								+ this.termInYears,
						ApplicationContext.getInstance().getTimeSystem()
								.getCurrentMonthType(),
						ApplicationContext.getInstance().getTimeSystem()
								.getCurrentDayType(), HourType.HOUR_01);
	}

	/*
	 * accessors
	 */

	public double getFaceValue() {
		return faceValue;
	}

	/**
	 * @see #faceValueFromBankAccountDelegate
	 */
	public BankAccountDelegate getFaceValueFromBankAccountDelegate() {
		return faceValueFromBankAccountDelegate;
	}

	/**
	 * @see #faceValueToBankAccountDelegate
	 */
	public BankAccountDelegate getFaceValueToBankAccountDelegate() {
		return faceValueToBankAccountDelegate;
	}

	public Currency getIssuedInCurrency() {
		return issuedInCurrency;
	}

	public int getTermInYears() {
		return termInYears;
	}

	public void setFaceValue(final double faceValue) {
		this.faceValue = faceValue;
	}

	public void setFaceValueFromBankAccountDelegate(
			final BankAccountDelegate faceValueFromBankAccountDelegate) {
		this.faceValueFromBankAccountDelegate = faceValueFromBankAccountDelegate;
	}

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

	/*
	 * assertions
	 */

	@Override
	protected void assertValidOwner() {
		assert (this.owner.equals(ApplicationContext.getInstance()
				.getPropertyService().getOwner(BondImpl.this)));
		assert (this.faceValueToBankAccountDelegate == null || this.owner
				.equals(this.faceValueToBankAccountDelegate.getBankAccount()
						.getOwner()));
	}

	@Override
	protected void assertValidIssuer() {
		super.assertValidIssuer();
		assert (this.getIssuer() == this.faceValueFromBankAccountDelegate
				.getBankAccount().getOwner());
	}

	/*
	 * business logic
	 */

	@Transient
	public void deconstruct() {
		super.deconstruct();

		// deregister from TimeSystem
		for (ITimeSystemEvent timeSystemEvent : this.timeSystemEvents)
			ApplicationContext.getInstance().getTimeSystem()
					.removeEvent(timeSystemEvent);
	}

	@Override
	@Transient
	public void resetOwner() {
		super.resetOwner();
		this.faceValueToBankAccountDelegate = null;
	}

	@Transient
	public String toString() {
		return super.toString() + ", issuer=[" + this.getIssuer()
				+ "], facevalue=[" + Currency.formatMoneySum(this.faceValue)
				+ "], issuedInCurrency=["
				+ this.issuedInCurrency.getIso4217Code() + "]";
	}

	public class TransferFaceValueEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			assert (BondImpl.this.faceValueFromBankAccountDelegate != null);
			assertValidOwner();
			assertValidIssuer();

			if (BondImpl.this.faceValueToBankAccountDelegate != null) {
				BondImpl.this.faceValueFromBankAccountDelegate
						.getBankAccount()
						.getManagingBank()
						.transferMoney(
								BondImpl.this.faceValueFromBankAccountDelegate
										.getBankAccount(),
								BondImpl.this.faceValueToBankAccountDelegate
										.getBankAccount(),
								BondImpl.this.faceValue, "bond face value");
			}
			BondImpl.this.deconstruct(); // delete bond from simulation
		}
	}
}
