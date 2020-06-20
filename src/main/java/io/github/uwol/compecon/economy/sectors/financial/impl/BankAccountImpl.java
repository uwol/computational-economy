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

package io.github.uwol.compecon.economy.sectors.financial.impl;

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

import io.github.uwol.compecon.economy.agent.impl.AgentImpl;
import io.github.uwol.compecon.economy.sectors.financial.Bank;
import io.github.uwol.compecon.economy.sectors.financial.BankAccount;
import io.github.uwol.compecon.economy.sectors.financial.BankCustomer;
import io.github.uwol.compecon.economy.sectors.financial.Currency;

@Entity
@Table(name = "BankAccount")
public class BankAccountImpl implements BankAccount {

	@Column(name = "balance")
	protected double balance;

	@Enumerated(EnumType.STRING)
	@Column(name = "currency")
	@Index(name = "IDX_BA_CURRENCY")
	protected Currency currency;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	protected int id;

	@ManyToOne(targetEntity = BankImpl.class)
	@JoinColumn(name = "managingBank_id")
	@Index(name = "IDX_BA_MANAGINGBANK")
	protected Bank managingBank;

	@Enumerated(EnumType.STRING)
	protected MoneyType moneyType;

	@Column(name = "name")
	protected String name;

	@Column(name = "overdraftPossible")
	protected boolean overdraftPossible = true;

	@ManyToOne(targetEntity = AgentImpl.class)
	@JoinColumn(name = "agent_id")
	@Index(name = "IDX_BA_OWNER")
	protected BankCustomer owner;

	@Enumerated(EnumType.STRING)
	protected TermType termType;

	@Override
	@Transient
	public void deposit(final double amount) {
		assert (!Double.isNaN(amount) && !Double.isInfinite(amount) && amount >= 0.0);

		balance = balance + amount;
	}

	@Override
	public double getBalance() {
		return balance;
	}

	@Override
	public Currency getCurrency() {
		return currency;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public Bank getManagingBank() {
		return managingBank;
	}

	@Override
	public MoneyType getMoneyType() {
		return moneyType;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean getOverdraftPossible() {
		return overdraftPossible;
	}

	@Override
	public BankCustomer getOwner() {
		return owner;
	}

	@Override
	public TermType getTermType() {
		return termType;
	}

	public void setBalance(final double balance) {
		this.balance = balance;
	}

	public void setCurrency(final Currency currency) {
		this.currency = currency;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public void setManagingBank(final Bank managingBank) {
		this.managingBank = managingBank;
	}

	public void setMoneyType(final MoneyType moneyType) {
		this.moneyType = moneyType;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setOverdraftPossible(final boolean overdraftPossible) {
		this.overdraftPossible = overdraftPossible;
	}

	public void setOwner(final BankCustomer owner) {
		this.owner = owner;
	}

	public void setTermType(final TermType termType) {
		this.termType = termType;
	}

	@Override
	public String toString() {
		return "BankAccount [ID: " + id + ", Balance: " + Currency.formatMoneySum(balance) + " " + currency + ", Name: "
				+ name + ", Owner: " + owner + "]";
	}

	@Override
	@Transient
	public void withdraw(final double amount) {
		assert (!Double.isNaN(amount) && !Double.isInfinite(amount) && amount >= 0.0);
		assert (amount <= balance || overdraftPossible);

		balance = balance - amount;
	}

}
