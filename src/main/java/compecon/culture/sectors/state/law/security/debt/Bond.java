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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.property.IProperty;
import compecon.culture.sectors.state.law.property.IPropertyOwner;
import compecon.culture.sectors.state.law.property.PropertyRegister;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.TimeSystem;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.HourType;
import compecon.engine.time.calendar.MonthType;

@Entity
@Table(name = "Bond")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Bond implements IProperty {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected int id;

	@Column(name = "isDeconstructed")
	protected boolean isDeconstructed = false;

	@Column(name = "faceValue")
	protected double faceValue; // par value or principal

	@OneToOne
	@JoinColumn(name = "issuerBankAccount")
	@Index(name = "issuerBankAccount")
	protected BankAccount issuerBankAccount;

	@Column(name = "issuerBankAccountPassword")
	protected String issuerBankAccountPassword;

	@Enumerated(value = EnumType.STRING)
	protected Currency issuedInCurrency;

	@Transient
	// TODO
	protected List<ITimeSystemEvent> timeSystemEvents = new ArrayList<ITimeSystemEvent>();

	public Bond(int maturityYear, MonthType maturityMonth, DayType maturityDay,
			BankAccount issuerBankAccount, String issuerBankAccountPassword,
			Currency issuedInCurrency) {
		this.issuerBankAccount = issuerBankAccount;
		this.issuerBankAccountPassword = issuerBankAccountPassword;
		this.issuedInCurrency = issuedInCurrency;

		// repay face value event;
		// has to be at HOUR_01, so that at HOUR_00 the last coupon can be payed
		ITimeSystemEvent transferFaceValueEvent = new TransferFaceValueEvent();
		this.timeSystemEvents.add(transferFaceValueEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				transferFaceValueEvent, maturityYear, maturityMonth,
				maturityDay, HourType.HOUR_01);
	}

	public double getFaceValue() {
		return this.faceValue;
	}

	protected void transferFaceValue() {
		IPropertyOwner owner = PropertyRegister.getInstance().getPropertyOwner(
				this);
		this.issuerBankAccount.getManagingBank().transferMoney(
				this.issuerBankAccount, owner.getTransactionsBankAccount(),
				this.faceValue, this.issuerBankAccountPassword,
				"bond face value");
	}

	protected void deconstruct() {
		this.isDeconstructed = true;

		// deregister from TimeSystem
		for (ITimeSystemEvent timeSystemEvent : this.timeSystemEvents)
			TimeSystem.getInstance().removeEvent(timeSystemEvent);

		// deregister from property rights system
		PropertyRegister.getInstance().deregister(this);
	}

	public boolean isDecostructed() {
		return this.isDeconstructed;
	}

	protected class TransferFaceValueEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			Bond.this.transferFaceValue();
			Bond.this.deconstruct(); // delete bond from simulation
		}
	}
}
