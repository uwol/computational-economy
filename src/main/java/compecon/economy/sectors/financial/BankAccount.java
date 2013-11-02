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

package compecon.economy.sectors.financial;

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

import compecon.economy.sectors.Agent;

@Entity
@Table(name = "BankAccount")
public class BankAccount {

	public enum TermType {
		SHORT_TERM, LONG_TERM;
	}

	public enum MoneyType {
		DEPOSITS, CENTRALBANK_MONEY
	}

	@Column(name = "balance")
	protected double balance;

	@Enumerated(EnumType.STRING)
	protected TermType termType;

	@Enumerated(EnumType.STRING)
	@Column(name = "currency")
	@Index(name = "IDX_BA_CURRENCY")
	protected Currency currency;

	@Enumerated(EnumType.STRING)
	protected MoneyType moneyType;

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

	public TermType getTermType() {
		return termType;
	}

	public Currency getCurrency() {
		return this.currency;
	}

	public int getId() {
		return id;
	}

	public MoneyType getMoneyType() {
		return this.moneyType;
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

	public void setTermType(TermType termType) {
		this.termType = termType;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public void setMoneyType(MoneyType moneyType) {
		this.moneyType = moneyType;
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
		assert (!Double.isNaN(amount) && !Double.isInfinite(amount) && amount >= 0.0);

		this.balance = this.balance + amount;
	}

	@Transient
	protected void withdraw(double amount) {
		assert (!Double.isNaN(amount) && !Double.isInfinite(amount) && amount >= 0.0);
		assert (amount <= this.balance || this.overdraftPossible);

		this.balance = this.balance - amount;
	}

	@Override
	public String toString() {
		return "BankAccount [ID: " + this.id + ", Balance: "
				+ Currency.formatMoneySum(this.balance) + " "
				+ this.currency.getIso4217Code() + ", Name: " + this.name
				+ ", Owner: " + this.owner + "]";
	}

}
