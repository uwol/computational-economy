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

	@Column(name = "balance")
	protected double balance;

	@Enumerated(EnumType.STRING)
	@Index(name = "IDX_BA_CURRENCY")
	@Column(name = "currency")
	protected Currency currency;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected int id;

	@ManyToOne
	@JoinColumn(name = "managingBank_id")
	@Index(name = "managingBank_id")
	protected Bank managingBank;

	@Column(name = "overdraftPossible")
	protected boolean overdraftPossible = true;

	@ManyToOne
	@JoinColumn(name = "agent_id")
	@Index(name = "agent_id")
	protected Agent owner;

	/*
	 * Accessors
	 */

	public double getBalance() {
		return this.balance;
	}

	public Currency getCurrency() {
		return this.currency;
	}

	public Bank getManagingBank() {
		return this.managingBank;
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

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public void setManagingBank(Bank managingBank) {
		this.managingBank = managingBank;
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
}
