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

package compecon.economy.agent.impl;

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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.economy.agent.Agent;
import compecon.economy.bookkeeping.impl.BalanceSheetDTO;
import compecon.economy.materia.GoodType;
import compecon.economy.property.Property;
import compecon.economy.property.PropertyIssued;
import compecon.economy.property.PropertyOwner;
import compecon.economy.sectors.financial.Bank;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.financial.impl.BankAccountImpl;
import compecon.economy.security.debt.Bond;
import compecon.economy.security.equity.Share;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.log.Log;
import compecon.engine.timesystem.TimeSystemEvent;
import compecon.engine.timesystem.impl.DayType;
import compecon.engine.timesystem.impl.MonthType;

@Entity
@Table(name = "Agent")
@org.hibernate.annotations.Table(appliesTo = "Agent", indexes = { @Index(name = "IDX_A_DTYPE", columnNames = { "DTYPE" }) })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE")
public abstract class AgentImpl implements Agent {

	public class BalanceSheetPublicationEvent implements TimeSystemEvent {
		@Override
		public boolean isDeconstructed() {
			return isDeconstructed;
		}

		@Override
		public void onEvent() {
			final BalanceSheetDTO balanceSheet = issueBalanceSheet();

			// TODO: could be placed in its own life sign event
			getLog().agent_onLifesign(AgentImpl.this);

			getLog().agent_onPublishBalanceSheet(AgentImpl.this, balanceSheet);
		}
	}

	/**
	 * bank account for basic daily transactions
	 */
	@OneToOne(targetEntity = BankAccountImpl.class)
	@JoinColumn(name = "bankAccountTransactions_id")
	@Index(name = "IDX_A_BA_TRANSACTIONS")
	protected BankAccount bankAccountTransactions;

	@Transient
	protected final BankAccountDelegate bankAccountTransactionsDelegate = new BankAccountDelegate() {
		@Override
		public BankAccount getBankAccount() {
			AgentImpl.this.assureBankAccountTransactions();
			return bankAccountTransactions;
		}

		@Override
		public void onTransfer(final double amount) {
		}
	};

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	protected int id;

	@Column(name = "isDeconstructed")
	protected boolean isDeconstructed = false;

	@Transient
	private boolean isInitialized = false;

	@Column(name = "primaryCurrency")
	@Enumerated(EnumType.STRING)
	@Index(name = "IDX_A_PRIMARYCURRENCY")
	protected Currency primaryCurrency;

	/**
	 * maxCredit limits the demand for money when buying production input
	 * factors, thus limiting M1 in the monetary system
	 */
	@Column(name = "referenceCredit")
	protected double referenceCredit;

	@Transient
	protected Set<TimeSystemEvent> timeSystemEvents = new HashSet<TimeSystemEvent>();

	@Transient
	protected void assureBankAccountTransactions() {
		if (isDeconstructed) {
			return;
		}

		// initialize bank account
		if (bankAccountTransactions == null) {
			final Bank randomBank = ApplicationContext.getInstance()
					.getAgentService().findRandomCreditBank(primaryCurrency);
			bankAccountTransactions = randomBank.openBankAccount(this,
					primaryCurrency, true, "transactions", TermType.SHORT_TERM,
					MoneyType.DEPOSITS);
		}
	}

	/*
	 * accessors
	 */

	/**
	 * deregisters the agent from all referencing objects
	 */
	@Override
	@Transient
	public void deconstruct() {
		isDeconstructed = true;

		getLog().agent_onDeconstruct(this);

		// deregister from time system
		ApplicationContext.getInstance().getTimeSystem()
				.removeEvents(timeSystemEvents);
		timeSystemEvents = null;

		// remove selling offers from market
		ApplicationContext.getInstance().getMarketService()
				.removeAllSellingOffers(this);

		// delete properties issued by this agent
		for (final Property propertyIssued : ApplicationContext.getInstance()
				.getPropertyDAO().findAllPropertiesIssuedByAgent(this)) {
			ApplicationContext.getInstance().getPropertyService()
					.deleteProperty(propertyIssued);
		}

		// deregister from property register
		ApplicationContext.getInstance().getPropertyService()
				.transferEverythingToRandomAgent(this);

		// close bank accounts
		for (final BankAccount bankAccount : ApplicationContext.getInstance()
				.getBankAccountDAO().findAllBankAccountsOfAgent(this)) {
			if (bankAccount.getOwner() == this) {
				bankAccount.getManagingBank().closeCustomerAccount(this);
			}
		}

		assert (ApplicationContext.getInstance().getBankAccountDAO()
				.findAllBankAccountsOfAgent(this).size() == 0);

		// deregister from cash register
		ApplicationContext.getInstance().getHardCashService().deregister(this);
	}

	public BankAccount getBankAccountTransactions() {
		return bankAccountTransactions;
	}

	@Override
	@Transient
	public BankAccountDelegate getBankAccountTransactionsDelegate() {
		return bankAccountTransactionsDelegate;
	}

	@Override
	public int getId() {
		return id;
	}

	@Transient
	protected Log getLog() {
		return ApplicationContext.getInstance().getLog();
	}

	@Transient
	protected Bank getPrimaryBank() {
		assureBankAccountTransactions();
		return bankAccountTransactions.getManagingBank();
	}

	@Override
	public Currency getPrimaryCurrency() {
		return primaryCurrency;
	}

	public double getReferenceCredit() {
		return referenceCredit;
	}

	@Override
	public Set<TimeSystemEvent> getTimeSystemEvents() {
		return timeSystemEvents;
	}

	@Override
	public void initialize() {
		assert (!isInitialized);

		// balance sheet publication
		final TimeSystemEvent balanceSheetPublicationEvent = new BalanceSheetPublicationEvent();
		timeSystemEvents.add(balanceSheetPublicationEvent);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(
						balanceSheetPublicationEvent,
						-1,
						MonthType.EVERY,
						DayType.EVERY,
						ApplicationContext.getInstance().getConfiguration().agentConfig
								.getBalanceSheetPublicationHourType());

		getLog().agent_onConstruct(this);

		isInitialized = true;
	}

	@Override
	public boolean isDeconstructed() {
		return isDeconstructed;
	}

	@Transient
	protected BalanceSheetDTO issueBalanceSheet() {
		assureBankAccountTransactions();

		final Currency referenceCurrency = bankAccountTransactions
				.getCurrency();

		assert (referenceCurrency != null);

		final BalanceSheetDTO balanceSheet = new BalanceSheetDTO(
				referenceCurrency);

		// hard cash
		balanceSheet.hardCash = ApplicationContext.getInstance()
				.getHardCashService().getBalance(this, referenceCurrency);

		// bank deposits
		balanceSheet.addBankAccountBalance(bankAccountTransactions);

		// owned properties
		for (final Property property : ApplicationContext.getInstance()
				.getPropertyService().findAllPropertiesOfPropertyOwner(this)) {
			assert (property.getOwner() == AgentImpl.this);

			// owned bonds
			if (property instanceof Bond) {
				final Bond bond = ((Bond) property);

				if (bond.isDeconstructed()) {
					ApplicationContext.getInstance().getPropertyService()
							.deleteProperty(bond);
				} else {
					/*
					 * check, that the agent does not take into account bonds,
					 * which are issued by the agent (and have not been sold,
					 * yet -> are owned by the agent)
					 */
					if (bond.getIssuer() != this) {
						balanceSheet.bonds += ((Bond) property).getFaceValue();
					}
				}
			}
		}

		// inventory by value
		final Map<GoodType, Double> prices = ApplicationContext.getInstance()
				.getMarketService().getMarginalMarketPrices(primaryCurrency);
		for (final Entry<GoodType, Double> balanceEntry : ApplicationContext
				.getInstance().getPropertyService().getGoodTypeBalances(this)
				.entrySet()) {
			final GoodType goodType = balanceEntry.getKey();
			final double amount = balanceEntry.getValue();
			final double price = prices.get(goodType);
			if (!Double.isNaN(price)) {
				balanceSheet.inventoryValue += amount * price;
			}
		}

		// inventory by amount
		balanceSheet.inventoryQuantitative.putAll(ApplicationContext
				.getInstance().getPropertyService().getGoodTypeBalances(this));

		// --------------

		// issued properties
		for (final Property property : ApplicationContext.getInstance()
				.getPropertyService().findAllPropertiesIssuedByAgent(this)) {
			final PropertyIssued propertyIssued = (PropertyIssued) property;

			assert (propertyIssued.getIssuer() == AgentImpl.this);

			// issued bonds
			if (propertyIssued instanceof Bond) {
				final Bond bond = (Bond) property;
				if (!bond.isDeconstructed() && !bond.getOwner().equals(this)) {
					assert (bond.getOwner() != this);

					balanceSheet.financialLiabilities += bond.getFaceValue();
				}
			}

			// issued capital / shares
			if (propertyIssued instanceof Share) {
				final Share share = (Share) propertyIssued;
				balanceSheet.issuedCapital.add(share);
			}
		}

		return balanceSheet;
	}

	/*
	 * assertions
	 */

	@Override
	@Transient
	public void onBankCloseBankAccount(final BankAccount bankAccount) {
		if (bankAccountTransactions == bankAccount) {
			bankAccountTransactions = null;
		}
	}

	/*
	 * business logic
	 */

	@Override
	@Transient
	public void onPropertyTransferred(final Property property,
			final PropertyOwner oldOwner, final PropertyOwner newOwner) {
	}

	public void setBankAccountTransactions(
			final BankAccount bankAccountTransactions) {
		this.bankAccountTransactions = bankAccountTransactions;
	}

	public void setDeconstructed(final boolean isDeconstructed) {
		this.isDeconstructed = isDeconstructed;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public void setPrimaryCurrency(final Currency primaryCurrency) {
		this.primaryCurrency = primaryCurrency;
	}

	public void setReferenceCredit(final double referenceCredit) {
		this.referenceCredit = referenceCredit;
	}

	public void setTimeSystemEvents(final Set<TimeSystemEvent> timeSystemEvents) {
		this.timeSystemEvents = timeSystemEvents;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": id=[" + id
				+ "], primaryCurrency=[" + primaryCurrency.getIso4217Code()
				+ "]";
	}
}
