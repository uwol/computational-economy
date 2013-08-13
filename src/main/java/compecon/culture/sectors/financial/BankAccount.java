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

package compecon.culture.sectors.financial;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.engine.Agent;

@Entity
@Table(name = "BankAccount")
public class BankAccount {

	public enum BankAccountType {
		GIRO, SAVINGS;
	}

	@Column(name = "balance")
	protected double balance;

	@Enumerated(EnumType.STRING)
	protected BankAccountType bankAccountType;

	@Enumerated(EnumType.STRING)
	@Column(name = "currency")
	@Index(name = "IDX_BA_CURRENCY")
	protected Currency currency;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected int id;

	@ManyToOne
	@JoinColumn(name = "managingBank_id")
	@Index(name = "IDX_BA_MANAGINGBANK")
	protected Bank managingBank;

	@Column(name = "name")
	protected String name;

	@Column(name = "overdraftPossible")
	protected boolean overdraftPossible = true;

	@ManyToOne
	@JoinColumn(name = "agent_id")
	@Index(name = "IDX_BA_AGENT")
	protected Agent owner;

	/*
	 * Accessors
	 */

	public double getBalance() {
		return this.balance;
	}

	public BankAccountType getBankAccountType() {
		return bankAccountType;
	}

	public Currency getCurrency() {
		return this.currency;
	}

	public int getId() {
		return id;
	}

	public Bank getManagingBank() {
		return this.managingBank;
	}

	public String getName() {
		return name;
	}

	public boolean getOverdraftPossible() {
		return this.overdraftPossible;
	}

	public Agent getOwner() {
		return this.owner;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public void setBankAccountType(BankAccountType bankAccountType) {
		this.bankAccountType = bankAccountType;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setManagingBank(Bank managingBank) {
		this.managingBank = managingBank;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOverdraftPossible(boolean overdraftPossible) {
		this.overdraftPossible = overdraftPossible;
	}

	public void setOwner(Agent owner) {
		this.owner = owner;
	}

	/*
	 * Business logic
	 */

	@Transient
	protected void deposit(double amount) {
		if (Double.isNaN(amount) || Double.isInfinite(amount))
			throw new RuntimeException("amount is " + amount);
		this.balance = this.balance + amount;
	}

	@Transient
	protected void withdraw(double amount) {
		if (Double.isNaN(amount) || Double.isInfinite(amount))
			throw new RuntimeException("amount is " + amount);
		if (!this.overdraftPossible && amount > this.balance)
			throw new RuntimeException(
					"overdraft not allowed and not enough money to withdraw");
		this.balance = this.balance - amount;
	}

	@Override
	public String toString() {
		return "BankAccount [ID: " + this.id + ", Balance: "
				+ Currency.formatMoneySum(this.balance) + " "
				+ this.currency.getIso4217Code() + ", Owner: " + this.owner
				+ "]";
	}

}
