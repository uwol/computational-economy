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

package compecon.economy.sectors;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.economy.sectors.financial.Bank;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.BankAccount.BankAccountType;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.economy.sectors.state.law.property.HardCashRegister;
import compecon.economy.sectors.state.law.property.Property;
import compecon.economy.sectors.state.law.property.PropertyRegister;
import compecon.economy.sectors.state.law.security.debt.Bond;
import compecon.engine.AgentFactory;
import compecon.engine.MarketFactory;
import compecon.engine.PropertyFactory;
import compecon.engine.Simulation;
import compecon.engine.statistics.Log;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.calendar.HourType;
import compecon.materia.GoodType;

@Entity
@Table(name = "Agent")
@org.hibernate.annotations.Table(appliesTo = "Agent", indexes = { @Index(name = "IDX_A_DTYPE", columnNames = { "DTYPE" }) })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE")
public abstract class Agent {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	protected int id;

	@Column(name = "isDeconstructed")
	protected boolean isDeconstructed = false;

	@Transient
	protected Set<ITimeSystemEvent> timeSystemEvents = new HashSet<ITimeSystemEvent>();

	@Column(name = "primaryCurrency")
	@Enumerated(EnumType.STRING)
	@Index(name = "IDX_A_PRIMARYCURRENCY")
	protected Currency primaryCurrency;

	@ManyToOne
	@JoinColumn(name = "primaryBank_id")
	@Index(name = "IDX_A_PRIMARYBANK")
	protected Bank primaryBank;

	// maxCredit limits the demand for money when buying production input
	// factors, thus limiting M1 in the monetary system
	@Column(name = "referenceCredit")
	protected double referenceCredit;

	@OneToOne
	@JoinColumn(name = "transactionsBankAccount_id")
	@Index(name = "IDX_A_TRANSACTIONSBANKACCOUNT")
	// bank account for basic daily transactions
	protected BankAccount transactionsBankAccount;

	@Transient
	protected final HourType BALANCE_SHEET_PUBLICATION_HOUR_TYPE = HourType.HOUR_23;

	@Transient
	private final String agentTypeName = this.getClass().getSimpleName();

	public void initialize() {
		Log.agent_onConstruct(this);
	}

	/**
	 * deregisters the agent from all referencing objects
	 */
	@Transient
	public void deconstruct() {
		this.isDeconstructed = true;
		Log.agent_onDeconstruct(this);

		// deregister from time system
		for (ITimeSystemEvent timeSystemEvent : this.timeSystemEvents)
			Simulation.getInstance().getTimeSystem()
					.removeEvent(timeSystemEvent);

		// remove selling offers from primary market
		MarketFactory.getInstance().removeAllSellingOffers(this);

		// deregister from poperty register
		PropertyRegister.getInstance().transferEverythingToRandomAgent(this);

		// deregister from cash register
		HardCashRegister.getInstance().deregister(this);

		// deregister from banks
		if (this.transactionsBankAccount != null) {
			this.transactionsBankAccount.getManagingBank()
					.closeCustomerAccount(this);
		}

		this.primaryBank = null;
		this.transactionsBankAccount = null;
		this.timeSystemEvents = null;

		AgentFactory.deleteAgent(this);
		// no flush here, as calling deconstruct methods might necessiate
		// further cleanup actions / current state might not be consistent
	}

	/*
	 * accessors
	 */

	public int getId() {
		return id;
	}

	public boolean isDeconstructed() {
		return isDeconstructed;
	}

	public Set<ITimeSystemEvent> getTimeSystemEvents() {
		return timeSystemEvents;
	}

	public Bank getPrimaryBank() {
		return primaryBank;
	}

	public Currency getPrimaryCurrency() {
		return primaryCurrency;
	}

	public double getReferenceCredit() {
		return this.referenceCredit;
	}

	public BankAccount getTransactionsBankAccount() {
		return transactionsBankAccount;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setDeconstructed(boolean isDeconstructed) {
		this.isDeconstructed = isDeconstructed;
	}

	public void setTimeSystemEvents(Set<ITimeSystemEvent> timeSystemEvents) {
		this.timeSystemEvents = timeSystemEvents;
	}

	public void setPrimaryBank(Bank primaryBank) {
		this.primaryBank = primaryBank;
	}

	public void setPrimaryCurrency(Currency primaryCurrency) {
		this.primaryCurrency = primaryCurrency;
	}

	public void setReferenceCredit(double referenceCredit) {
		this.referenceCredit = referenceCredit;
	}

	public void setTransactionsBankAccount(BankAccount transactionsBankAccount) {
		this.transactionsBankAccount = transactionsBankAccount;
	}

	/*
	 * assertions
	 */

	@Transient
	public void assureBankCustomerAccount() {
		if (this.isDeconstructed)
			return;

		if (this.primaryBank == null) {
			this.primaryBank = AgentFactory
					.getRandomInstanceCreditBank(this.primaryCurrency);
		}
	}

	@Transient
	public void assureTransactionsBankAccount() {
		if (this.isDeconstructed)
			return;

		this.assureBankCustomerAccount();

		// initialize bank account
		if (this.transactionsBankAccount == null) {
			this.transactionsBankAccount = this.primaryBank.openBankAccount(
					this, this.primaryCurrency, "transactions account",
					BankAccountType.GIRO);
		}
	}

	/**
	 * this method is triggered in the event that the bank of the bank account
	 * closes the bank account, so that the customer agent can react.
	 */
	@Transient
	public void onBankCloseBankAccount(BankAccount bankAccount) {
		if (this.transactionsBankAccount == bankAccount)
			this.transactionsBankAccount = null;
	}

	/*
	 * business logic
	 */

	@Transient
	public BalanceSheet issueBasicBalanceSheet() {
		this.assureTransactionsBankAccount();

		Currency referenceCurrency = Agent.this.transactionsBankAccount
				.getCurrency();

		assert (referenceCurrency != null);

		BalanceSheet balanceSheet = new BalanceSheet(referenceCurrency);

		// hard cash
		// TODO convert other currencies
		balanceSheet.hardCash = HardCashRegister.getInstance().getBalance(
				Agent.this, referenceCurrency);

		// bank deposits
		if (Agent.this.transactionsBankAccount.getBalance() > 0.0) {
			balanceSheet.cashShortTerm += Agent.this.transactionsBankAccount
					.getBalance();
		} else {
			balanceSheet.loans += -1.0
					* Agent.this.transactionsBankAccount.getBalance();
		}

		// bonds
		for (Property property : PropertyRegister.getInstance().getProperties(
				Agent.this)) {
			if (property instanceof Bond) {
				Bond bond = ((Bond) property);
				assert (bond.getOwner() == Agent.this);

				if (bond.isDeconstructed()) {
					PropertyFactory.deleteProperty(bond);
				} else {
					// important, so that agents do not count bonds that have
					// not been sold, yet
					if (bond.getIssuerBankAccount().getOwner() != this) {
						balanceSheet.bonds += ((Bond) property).getFaceValue();
					}
				}
			}
		}

		// inventory by value
		Map<GoodType, Double> prices = MarketFactory.getInstance().getPrices(
				this.primaryCurrency);
		for (Entry<GoodType, Double> balanceEntry : PropertyRegister
				.getInstance().getBalance(Agent.this).entrySet()) {
			GoodType goodType = balanceEntry.getKey();
			double amount = balanceEntry.getValue();
			double price = prices.get(goodType);
			if (!Double.isNaN(price)) {
				balanceSheet.inventoryValue += amount * price;
			}
		}

		// inventory by amount
		balanceSheet.inventoryQuantitative.putAll(PropertyRegister
				.getInstance().getBalance(Agent.this));

		return balanceSheet;
	}

	@Override
	public String toString() {
		return this.agentTypeName + " [ID: " + this.id + ", "
				+ this.primaryCurrency.getIso4217Code() + "]";
	}
}
