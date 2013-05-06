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

import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.property.IPropertyOwner;
import compecon.culture.sectors.state.law.property.Property;
import compecon.culture.sectors.state.law.property.PropertyRegister;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.TimeSystem;
import compecon.engine.time.calendar.HourType;

@Entity
public abstract class Bond extends Property {

	@Column(name = "faceValue")
	protected double faceValue; // par value or principal

	@ManyToOne
	@JoinColumn(name = "issuerBankAccount_id")
	@Index(name = "issuerBankAccount")
	protected BankAccount issuerBankAccount;

	@Column(name = "issuerBankAccountPassword")
	protected String issuerBankAccountPassword;

	@Enumerated(value = EnumType.STRING)
	protected Currency issuedInCurrency;

	@Transient
	protected List<ITimeSystemEvent> timeSystemEvents = new ArrayList<ITimeSystemEvent>();

	public void initialize() {
		super.initialize();

		// repay face value event;
		// has to be at HOUR_01, so that at HOUR_00 the last coupon can be payed
		ITimeSystemEvent transferFaceValueEvent = new TransferFaceValueEvent();
		this.timeSystemEvents.add(transferFaceValueEvent);
		TimeSystem.getInstance().addEvent(transferFaceValueEvent,
				TimeSystem.getInstance().getCurrentYear() + 2,
				TimeSystem.getInstance().getCurrentMonthType(),
				TimeSystem.getInstance().getCurrentDayType(), HourType.HOUR_01);
	}

	/*
	 * accessors
	 */

	public double getFaceValue() {
		return faceValue;
	}

	public BankAccount getIssuerBankAccount() {
		return issuerBankAccount;
	}

	public String getIssuerBankAccountPassword() {
		return issuerBankAccountPassword;
	}

	public Currency getIssuedInCurrency() {
		return issuedInCurrency;
	}

	public void setFaceValue(double faceValue) {
		this.faceValue = faceValue;
	}

	public void setIssuerBankAccount(BankAccount issuerBankAccount) {
		this.issuerBankAccount = issuerBankAccount;
	}

	public void setIssuerBankAccountPassword(String issuerBankAccountPassword) {
		this.issuerBankAccountPassword = issuerBankAccountPassword;
	}

	public void setIssuedInCurrency(Currency issuedInCurrency) {
		this.issuedInCurrency = issuedInCurrency;
	}

	/*
	 * business logic
	 */

	@Transient
	protected void transferFaceValue() {
		IPropertyOwner owner = PropertyRegister.getInstance().getPropertyOwner(
				this);
		this.issuerBankAccount.getManagingBank().transferMoney(
				this.issuerBankAccount, owner.getTransactionsBankAccount(),
				this.faceValue, this.issuerBankAccountPassword,
				"bond face value");
	}

	@Transient
	protected void deconstruct() {

		// deregister from TimeSystem
		for (ITimeSystemEvent timeSystemEvent : this.timeSystemEvents)
			TimeSystem.getInstance().removeEvent(timeSystemEvent);

	}

	protected class TransferFaceValueEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			Bond.this.transferFaceValue();
			Bond.this.deconstruct(); // delete bond from simulation
		}
	}
}
