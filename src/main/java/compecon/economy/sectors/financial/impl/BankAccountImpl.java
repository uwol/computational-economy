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

package compecon.economy.sectors.financial.impl;

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

import compecon.economy.agent.impl.AgentImpl;
import compecon.economy.sectors.financial.Bank;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.BankCustomer;
import compecon.economy.sectors.financial.Currency;

@Entity
@Table(name = "BankAccount")
public class BankAccountImpl implements BankAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected int id;

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

	@ManyToOne(targetEntity = BankImpl.class)
	@JoinColumn(name = "managingBank_id")
	@Index(name = "IDX_BA_MANAGINGBANK")
	protected Bank managingBank;

	@Column(name = "name")
	protected String name;

	@Column(name = "overdraftPossible")
	protected boolean overdraftPossible = true;

	@ManyToOne(targetEntity = AgentImpl.class)
	@JoinColumn(name = "agent_id")
	@Index(name = "IDX_BA_OWNER")
	protected BankCustomer owner;

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

	public BankCustomer getOwner() {
		return this.owner;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public void setTermType(final TermType termType) {
		this.termType = termType;
	}

	public void setCurrency(final Currency currency) {
		this.currency = currency;
	}

	public void setMoneyType(final MoneyType moneyType) {
		this.moneyType = moneyType;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setManagingBank(final Bank managingBank) {
		this.managingBank = managingBank;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOverdraftPossible(final boolean overdraftPossible) {
		this.overdraftPossible = overdraftPossible;
	}

	public void setOwner(BankCustomer owner) {
		this.owner = owner;
	}

	/*
	 * Business logic
	 */

	@Transient
	public void deposit(final double amount) {
		assert (!Double.isNaN(amount) && !Double.isInfinite(amount) && amount >= 0.0);

		this.balance = this.balance + amount;
	}

	@Transient
	public void withdraw(final double amount) {
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
