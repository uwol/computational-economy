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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.economy.sectors.Agent;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.law.property.Property;
import compecon.economy.sectors.state.law.property.PropertyRegister;
import compecon.engine.Simulation;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.calendar.HourType;

@Entity
public abstract class Bond extends Property {

	@Column(name = "faceValue")
	protected double faceValue; // par value or principal

	/**
	 * sender bank account (of the bond issuer and seller) for the final face
	 * value re-transfer at the end of the bond life cycle
	 */
	@ManyToOne
	@JoinColumn(name = "faceValueFromBankAccount_id")
	@Index(name = "faceValueFromBankAccount")
	protected BankAccount faceValueFromBankAccount;

	/**
	 * receiver bank account (of the bond buyer) for the final face value
	 * re-transfer at the end of the bond life cycle. null, if the bond has not
	 * been transfered to a owner different from the issuer.
	 */
	@ManyToOne
	@JoinColumn(name = "faceValueToBankAccount_id")
	@Index(name = "faceValueToBankAccount")
	protected BankAccount faceValueToBankAccount;

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
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(
						transferFaceValueEvent,
						Simulation.getInstance().getTimeSystem()
								.getCurrentYear()
								+ this.termInYears,
						Simulation.getInstance().getTimeSystem()
								.getCurrentMonthType(),
						Simulation.getInstance().getTimeSystem()
								.getCurrentDayType(), HourType.HOUR_01);
	}

	/*
	 * accessors
	 */

	public double getFaceValue() {
		return faceValue;
	}

	/**
	 * @see #faceValueFromBankAccount
	 */
	public BankAccount getFaceValueFromBankAccount() {
		return faceValueFromBankAccount;
	}

	/**
	 * @see #faceValueToBankAccount
	 */
	public BankAccount getFaceValueToBankAccount() {
		return faceValueToBankAccount;
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

	public void setFaceValueFromBankAccount(
			final BankAccount faceValueFromBankAccount) {
		this.faceValueFromBankAccount = faceValueFromBankAccount;
	}

	public void setFaceValueToBankAccount(
			final BankAccount faceValueToBankAccount) {
		this.faceValueToBankAccount = faceValueToBankAccount;
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

	protected void assertValidOwner() {
		assert (this.owner.equals(PropertyRegister.getInstance().getOwner(
				Bond.this)));
		assert (this.faceValueToBankAccount == null || this.owner
				.equals(this.faceValueToBankAccount.getOwner()));
	}

	/*
	 * business logic
	 */

	@Transient
	public void deconstruct() {
		super.deconstruct();

		// deregister from TimeSystem
		for (ITimeSystemEvent timeSystemEvent : this.timeSystemEvents)
			Simulation.getInstance().getTimeSystem()
					.removeEvent(timeSystemEvent);
	}

	public Agent getIssuer() {
		return this.faceValueFromBankAccount.getOwner();
	}

	@Transient
	public String toString() {
		return this.getClass().getSimpleName() + " [Issuer: "
				+ this.faceValueFromBankAccount.getOwner() + ", Facevalue: "
				+ Currency.formatMoneySum(this.faceValue) + " "
				+ this.issuedInCurrency.getIso4217Code() + "]";
	}

	public class TransferFaceValueEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			assert (Bond.this.faceValueFromBankAccount != null);
			assertValidOwner();

			if (Bond.this.faceValueToBankAccount != null) {
				Bond.this.faceValueFromBankAccount.getManagingBank()
						.transferMoney(Bond.this.faceValueFromBankAccount,
								Bond.this.faceValueToBankAccount,
								Bond.this.faceValue, "bond face value");
			}
			Bond.this.deconstruct(); // delete bond from simulation
		}
	}
}
