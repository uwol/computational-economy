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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import compecon.culture.PricingBehaviour;
import compecon.culture.markets.SettlementMarket.ISettlementEvent;
import compecon.culture.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.culture.sectors.state.law.property.HardCashRegister;
import compecon.culture.sectors.state.law.property.Property;
import compecon.culture.sectors.state.law.property.PropertyRegister;
import compecon.culture.sectors.state.law.security.debt.Bond;
import compecon.culture.sectors.state.law.security.debt.FixedRateBond;
import compecon.engine.Agent;
import compecon.engine.AgentFactory;
import compecon.engine.MarketFactory;
import compecon.engine.PropertyFactory;
import compecon.engine.jmx.Log;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.TimeSystem;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.HourType;
import compecon.engine.time.calendar.MonthType;
import compecon.engine.util.MathUtil;
import compecon.nature.materia.GoodType;

/**
 * Agent type credit bank manages bank accounts, creates money by credit and
 * follows minimum reserve requirements of central banks.
 */
@Entity
public class CreditBank extends Bank implements
		compecon.culture.sectors.financial.ICentralBankCustomer {

	@Transient
	private boolean centralBankAccountsInitialized = false;

	@Transient
	protected final int MAX_CREDIT = 100000;

	@Transient
	protected Map<Currency, PricingBehaviour> foreignCurrencyPricingBehaviours = new HashMap<Currency, PricingBehaviour>();

	@ElementCollection
	@CollectionTable(name = "CreditBank_OfferedCurrencies", joinColumns = @JoinColumn(name = "creditbank_id"))
	@Column(name = "offeredcurrency")
	protected Set<Currency> offeredCurrencies = new HashSet<Currency>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "CreditBank_IssuedBonds", joinColumns = @JoinColumn(name = "creditBank_id"), inverseJoinColumns = @JoinColumn(name = "bond_id"))
	protected Set<Bond> issuedBonds = new HashSet<Bond>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "CreditBank_ForeignCurrencyBankAccounts", joinColumns = @JoinColumn(name = "creditBank_id"), inverseJoinColumns = @JoinColumn(name = "bankAccount_id"))
	@MapKeyEnumerated
	protected Map<Currency, BankAccount> transactionForeignCurrencyAccounts = new HashMap<Currency, BankAccount>();

	@Override
	public void initialize() {
		super.initialize();

		// currency arbitrage
		ITimeSystemEvent currencyArbitrageEvent = new CurrencyArbitrageEvent();
		this.timeSystemEvents.add(currencyArbitrageEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				currencyArbitrageEvent, -1, MonthType.EVERY, DayType.EVERY,
				TimeSystem.getInstance().suggestRandomHourType());

		// offer primary currency on exchange markets
		ITimeSystemEvent primaryCurrencyOfferEvent = new PrimaryCurrencyOfferEvent();
		this.timeSystemEvents.add(primaryCurrencyOfferEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				primaryCurrencyOfferEvent, -1, MonthType.EVERY, DayType.EVERY,
				TimeSystem.getInstance().suggestRandomHourType());

		// calculate interest on customers bank accounts
		ITimeSystemEvent interestCalculationEvent = new DailyInterestCalculationEvent();
		this.timeSystemEvents.add(interestCalculationEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				interestCalculationEvent, -1, MonthType.EVERY, DayType.EVERY,
				HourType.HOUR_02);

		// check money reserves at the central bank
		ITimeSystemEvent checkMoneyReservesEvent = new CheckMoneyReservesEvent();
		this.timeSystemEvents.add(checkMoneyReservesEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				checkMoneyReservesEvent, -1, MonthType.EVERY, DayType.EVERY,
				HourType.HOUR_12);

		// balance sheet publication
		ITimeSystemEvent balanceSheetPublicationEvent = new BalanceSheetPublicationEvent();
		this.timeSystemEvents.add(balanceSheetPublicationEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				balanceSheetPublicationEvent, -1, MonthType.EVERY,
				DayType.EVERY, BALANCE_SHEET_PUBLICATION_HOUR_TYPE);

		for (Currency currency : Currency.values()) {
			if (currency != this.primaryCurrency) {
				double initialPrice = MarketFactory.getInstance()
						.getMarginalPrice(this.primaryCurrency, currency);
				if (Double.isNaN(initialPrice))
					initialPrice = 1.0;
				this.foreignCurrencyPricingBehaviours.put(currency,
						new PricingBehaviour(this, currency,
								this.primaryCurrency, initialPrice));
			}
		}
	}

	@Transient
	public void deconstruct() {
		super.deconstruct();

		this.transactionForeignCurrencyAccounts = null;
	}

	/*
	 * accessors
	 */

	public Set<Currency> getOfferedCurrencies() {
		return offeredCurrencies;
	}

	public Set<Bond> getIssuedBonds() {
		return issuedBonds;
	}

	public Map<Currency, BankAccount> getTransactionForeignCurrencyAccounts() {
		return transactionForeignCurrencyAccounts;
	}

	public void setOfferedCurrencies(Set<Currency> offeredCurrencies) {
		this.offeredCurrencies = offeredCurrencies;
	}

	public void setIssuedBonds(Set<Bond> issuedBonds) {
		this.issuedBonds = issuedBonds;
	}

	public void setTransactionForeignCurrencyAccounts(
			Map<Currency, BankAccount> transactionForeignCurrencyAccounts) {
		this.transactionForeignCurrencyAccounts = transactionForeignCurrencyAccounts;
	}

	/*
	 * assertions
	 */

	@Transient
	@Override
	public void assureTransactionsBankAccount() {
		if (this.isDeconstructed)
			return;

		if (this.primaryBank == null) {
			this.primaryBank = this;
			String bankPassword = this.openCustomerAccount(this);
			this.bankPasswords.put(this, bankPassword);
		}
		if (this.transactionsBankAccount == null) {
			/*
			 * initialize the banks own bank account and open a customer account
			 * at this new bank, so that this bank can transfer money from its
			 * own bank account
			 */
			this.transactionsBankAccount = this.primaryBank.openBankAccount(
					this, this.primaryCurrency,
					this.bankPasswords.get(this.primaryBank));
		}
	}

	@Transient
	public void assureCentralBankAccount() {
		if (this.isDeconstructed)
			return;

		if (!this.centralBankAccountsInitialized) {
			// initialize bank accounts at central banks
			for (Currency currency : offeredCurrencies) {
				String centralBankPassword = AgentFactory
						.getInstanceCentralBank(currency).openCustomerAccount(
								this);
				AgentFactory.getInstanceCentralBank(currency).openBankAccount(
						this, currency, centralBankPassword);
				this.bankPasswords.put(
						AgentFactory.getInstanceCentralBank(currency),
						centralBankPassword);
			}
			this.centralBankAccountsInitialized = true;
		}
	}

	@Transient
	public void assureTransactionsForeignCurrencyBankAccounts() {
		if (this.isDeconstructed)
			return;

		for (Currency currency : Currency.values()) {
			if (currency != this.primaryCurrency
					&& !this.transactionForeignCurrencyAccounts
							.containsKey(currency)) {
				CreditBank foreignCurrencyCreditBank = AgentFactory
						.getRandomInstanceCreditBank(currency);
				String bankPassword = foreignCurrencyCreditBank
						.openCustomerAccount(this);
				this.bankPasswords.put(foreignCurrencyCreditBank, bankPassword);
				BankAccount bankAccount = foreignCurrencyCreditBank
						.openBankAccount(this, currency, this.bankPasswords
								.get(foreignCurrencyCreditBank));
				this.transactionForeignCurrencyAccounts.put(currency,
						bankAccount);
			}
		}
	}

	@Transient
	protected void assertPasswordOk(CentralBank centralBank, String password) {
		if (this.bankPasswords.get(centralBank) == null)
			throw new RuntimeException("passwords is null");

		if (this.bankPasswords.get(centralBank) != password)
			throw new RuntimeException("passwords not equal");
	}

	@Transient
	protected void assertCurrencyIsOffered(Currency currency) {
		if (!this.getOfferedCurrencies().contains(currency))
			throw new RuntimeException(currency
					+ " are not offered at this bank");
	}

	/*
	 * business logic
	 */

	@Transient
	public void depositCash(Agent client, BankAccount to, double amount,
			Currency currency, String password) {
		this.assertIsCustomerOfThisBank(client);
		this.assertPasswordOk(client, password);
		this.assertBankAccountIsManagedByThisBank(to);
		this.assertCurrencyIsOffered(currency);

		// transfer money
		HardCashRegister.getInstance().decrement(client, currency, amount);
		to.deposit(amount);
	}

	@Transient
	public double withdrawCash(Agent client, BankAccount from, double amount,
			Currency currency, String password) {
		this.assertIsCustomerOfThisBank(client);
		this.assertPasswordOk(client, password);
		this.assertBankAccountIsManagedByThisBank(from);
		this.assertCurrencyIsOffered(currency);

		// transfer money
		from.withdraw(amount);
		return HardCashRegister.getInstance().increment(client, currency,
				amount);
	}

	@Transient
	public void transferMoney(BankAccount from, BankAccount to, double amount,
			String password, String subject) {
		this.transferMoney(from, to, amount, password, subject, false);
		Log.bank_onTransfer(from.getOwner(), to.getOwner(), from.getCurrency(),
				amount, subject);
	}

	@Transient
	protected void transferMoney(BankAccount from, BankAccount to,
			double amount, String password, String subject,
			boolean negativeAmountOK) {
		this.assureCentralBankAccount();
		this.assertIsCustomerOfThisBank(from.getOwner());
		this.assertBankAccountIsManagedByThisBank(from);

		if (!negativeAmountOK && amount < 0)
			throw new RuntimeException("amount must be >= 0");

		if (from.getCurrency() != to.getCurrency())
			throw new RuntimeException(
					"both bank accounts must have the same currency");

		this.assertPasswordOk(from.getOwner(), password);

		if (from.getBalance() - amount < 0 && !from.getOverdraftPossible())
			throw new RuntimeException(
					"amount is too high and bank account cannot be overdraft");

		// is the money flowing internally in this bank?
		if (to.getManagingBank() == this && from.getManagingBank() == this) {
			// transfer money internally
			from.withdraw(amount);
			to.deposit(amount);
		} else { // transfer to another bank
			CentralBank centralBank = AgentFactory.getInstanceCentralBank(from
					.getCurrency());

			// central bank account of this credit bank
			BankAccount toCentralBankAccountOfThisBank = centralBank
					.getBankAccount(this, this.bankPasswords.get(centralBank));

			// transfer money to central bank account of this bank
			centralBank.transferMoney(from, toCentralBankAccountOfThisBank,
					amount, this.bankPasswords.get(centralBank), subject);

			// transfer money from central bank account of this bank to bank
			// account at target bank
			centralBank.transferMoney(toCentralBankAccountOfThisBank, to,
					amount, this.bankPasswords.get(centralBank), subject);
		}
	}

	@Transient
	private double getSumOfBorrowings(Currency currency) {
		double sumOfBorrowings = 0;
		for (BankAccount creditBankAccount : this.customerBankAccounts.values()) {
			if (creditBankAccount.getCurrency() == currency)
				if (creditBankAccount.getBalance() > 0)
					sumOfBorrowings += creditBankAccount.getBalance();
		}
		return sumOfBorrowings;
	}

	@Transient
	public void deposit(CentralBank caller, String password,
			BankAccount bankAccount, double amount) {

		this.assertBankAccountIsManagedByThisBank(bankAccount);
		this.assertPasswordOk(caller, password);
		if (amount < 0)
			throw new RuntimeException("amount must be >= 0");

		bankAccount.deposit(amount);
	}

	@Transient
	public void withdraw(CentralBank caller, String password,
			BankAccount bankAccount, double amount) {

		this.assertBankAccountIsManagedByThisBank(bankAccount);
		this.assertPasswordOk(caller, password);
		if (amount < 0)
			throw new RuntimeException("amount must be >= 0");

		bankAccount.withdraw(amount);
	}

	@Override
	@Transient
	public void onBankCloseCustomerAccount(BankAccount bankAccount) {
		if (this.transactionForeignCurrencyAccounts != null) {
			for (Entry<Currency, BankAccount> entry : this.transactionForeignCurrencyAccounts
					.entrySet()) {
				if (entry.getValue() == bankAccount)
					this.transactionForeignCurrencyAccounts.remove(entry
							.getKey());
			}
		}

		super.onBankCloseCustomerAccount(bankAccount);
	}

	protected class SettlementMarketEvent implements ISettlementEvent {
		@Override
		public void onEvent(GoodType goodType, double amount,
				double pricePerUnit, Currency currency) {
		}

		@Override
		public void onEvent(Currency commodityCurrency, double amount,
				double pricePerUnit, Currency currency) {
			CreditBank.this.foreignCurrencyPricingBehaviours.get(
					commodityCurrency).registerSelling(amount);
		}

		@Override
		public void onEvent(Property property, double totalPrice,
				Currency currency) {
		}
	}

	public class DailyInterestCalculationEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			CreditBank.this.assureTransactionsBankAccount();
			CreditBank.this.assureCentralBankAccount();

			for (BankAccount bankAccount : CreditBank.this.customerBankAccounts
					.values()) {
				if (bankAccount.getBalance() > 0) { // liability account ->
													// passive
					double monthlyInterest = bankAccount.getBalance()
							* CreditBank.this
									.calculateMonthlyNominalInterestRate(AgentFactory
											.getInstanceCentralBank(

											bankAccount.getCurrency())
											.getEffectiveKeyInterestRate());
					double dailyInterest = monthlyInterest / 30;
					try {
						CreditBank.this.transferMoney(
								CreditBank.this.transactionsBankAccount,
								bankAccount, dailyInterest,
								CreditBank.this.customerPasswords
										.get(CreditBank.this),
								"interest earned for customer");
					} catch (Exception e) {
						throw new RuntimeException(e.getMessage());
					}
				} else if (bankAccount.getBalance() < 0) { // asset account
															// ->
															// active
					double monthlyInterest = -1
							* bankAccount.getBalance()
							* CreditBank.this
									.calculateMonthlyNominalInterestRate(AgentFactory
											.getInstanceCentralBank(

											bankAccount.getCurrency())
											.getEffectiveKeyInterestRate() * 1.5);
					double dailyInterest = monthlyInterest / 30;
					try {
						CreditBank.this.transferMoney(bankAccount,
								CreditBank.this.transactionsBankAccount,
								dailyInterest,
								CreditBank.this.customerPasswords
										.get(bankAccount.getOwner()),
								"debt interest from customer");
					} catch (Exception e) {
						throw new RuntimeException(e.getMessage());
					}
				}
			}
		}
	}

	public class CheckMoneyReservesEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			CreditBank.this.assureTransactionsBankAccount();
			CreditBank.this.assureCentralBankAccount();

			for (Currency currency : CreditBank.this.offeredCurrencies) {
				CentralBank centralBank = AgentFactory
						.getInstanceCentralBank(currency);
				String centralBankPassword = CreditBank.this.bankPasswords
						.get(centralBank);

				BankAccount bankAccountAtCentralBank = centralBank
						.getBankAccount(CreditBank.this, centralBankPassword);
				double sumOfBorrowings = CreditBank.this
						.getSumOfBorrowings(currency);
				double moneyReserveGap = sumOfBorrowings
						* centralBank.getReserveRatio()
						- bankAccountAtCentralBank.getBalance();

				// not enough money deposited at central bank
				if (moneyReserveGap > 0) {
					// calculate number of bonds needed to deposit them at
					// central bank for credit

					List<FixedRateBond> bonds = new ArrayList<FixedRateBond>();

					/*
					 * issue bond; mega bond that covers complete
					 * moneyReserveGap; no split up per 100 currency units, as
					 * one mega bond takes less memory and CPU performance
					 */
					FixedRateBond bond = PropertyFactory
							.newInstanceFixedRateBond(
									currency,
									CreditBank.this.transactionsBankAccount,
									CreditBank.this.customerPasswords
											.get(CreditBank.this),
									moneyReserveGap,
									centralBank.getEffectiveKeyInterestRate() + 0.02);
					bonds.add(bond);

					PropertyRegister.getInstance().register(centralBank, bond);

					// obtain tender for bond
					centralBank.obtainTender(CreditBank.this, bonds,
							centralBankPassword);

					// remember issued bonds for balance sheet event
					CreditBank.this.issuedBonds.addAll(bonds);
				}

			}
		}
	}

	public class BalanceSheetPublicationEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			CreditBank.this.assureTransactionsBankAccount();
			CreditBank.this.assureCentralBankAccount();

			BalanceSheet balanceSheet = CreditBank.this
					.issueBasicBalanceSheet();

			// bank accounts of customers
			for (BankAccount bankAccount : CreditBank.this.customerBankAccounts
					.values()) {
				// TODO compare with referenceCurrency of balance sheet
				if (bankAccount.getBalance() > 0) // passive account
					balanceSheet.bankBorrowings += bankAccount.getBalance();
				else
					// active account
					balanceSheet.bankLoans += bankAccount.getBalance() * -1;
			}

			// --------------

			// list issued bonds on balance sheet
			Set<Bond> bondsToDelete = new HashSet<Bond>();
			for (Bond bond : CreditBank.this.issuedBonds) {
				if (!bond.isDeconstructed())
					balanceSheet.financialLiabilities += bond.getFaceValue();
				else
					bondsToDelete.add(bond);
			}

			// clean up list of bonds
			CreditBank.this.issuedBonds.removeAll(bondsToDelete);

			// publish
			Log.agent_onPublishBalanceSheet(CreditBank.this, balanceSheet);
		}
	}

	public class CurrencyArbitrageEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			CreditBank.this.assureCentralBankAccount();
			CreditBank.this.assureTransactionsBankAccount();
			CreditBank.this.assureTransactionsForeignCurrencyBankAccounts();

			int numberOfForeignCurrencies = CreditBank.this.transactionForeignCurrencyAccounts
					.keySet().size();

			if (numberOfForeignCurrencies > 0) {
				/*
				 * arbitrage on exchange markets
				 */
				for (Entry<Currency, BankAccount> entry : CreditBank.this.transactionForeignCurrencyAccounts
						.entrySet()) {
					Currency foreignCurrency = entry.getKey();
					BankAccount foreignCurrencyBankAccount = entry.getValue();

					// e.g. USD_in_EUR = 0.8
					double priceOfForeignCurrencyInLocalCurrency = MarketFactory
							.getInstance().getMarginalPrice(primaryCurrency,
									foreignCurrency);
					// e.g. EUR_in_USD = 0.8
					double priceOfLocalCurrencyInForeignCurrency = MarketFactory
							.getInstance().getMarginalPrice(foreignCurrency,
									primaryCurrency);
					if (!Double.isNaN(priceOfLocalCurrencyInForeignCurrency)
							&& !Double
									.isNaN(priceOfForeignCurrencyInLocalCurrency)) {
						// inverse_EUR_in_USD -> correct_USD_in_EUR = 1.25
						double correctPriceOfForeignCurrencyInLocalCurrency = 1.0 / priceOfLocalCurrencyInForeignCurrency;

						Log.log(CreditBank.this,
								primaryCurrency.getIso4217Code()
										+ "/"
										+ foreignCurrency.getIso4217Code()
										+ " = "
										+ Currency
												.round(priceOfForeignCurrencyInLocalCurrency)
										+ "; "
										+ foreignCurrency.getIso4217Code()
										+ "/"
										+ primaryCurrency.getIso4217Code()
										+ " = "
										+ Currency
												.round(priceOfLocalCurrencyInForeignCurrency)
										+ " -> correct "
										+ primaryCurrency.getIso4217Code()
										+ "/"
										+ foreignCurrency.getIso4217Code()
										+ " = "
										+ Currency
												.round(correctPriceOfForeignCurrencyInLocalCurrency));

						/*
						 * if the price of foreign currency denominated in local
						 * currency is lower, than the inverse price of the
						 * local currency denominated in foreign currency, then
						 * the foreign currency should be bought low and sold
						 * high
						 */
						if (MathUtil.lesser(
								priceOfForeignCurrencyInLocalCurrency,
								correctPriceOfForeignCurrencyInLocalCurrency)) {
							double budgetPerForeignCurrencyInPrimaryCurrency = (CreditBank.this.MAX_CREDIT + CreditBank.this.transactionsBankAccount
									.getBalance())
									/ (double) numberOfForeignCurrencies;

							if (MathUtil.greater(
									budgetPerForeignCurrencyInPrimaryCurrency,
									0)) {
								this.buyForeignCurrencyWithLocalCurrency(
										foreignCurrency,
										budgetPerForeignCurrencyInPrimaryCurrency,
										correctPriceOfForeignCurrencyInLocalCurrency,
										foreignCurrencyBankAccount);
							} else
								Log.log(CreditBank.this,
										"not enough budget for buying foreign currency");
						}
						/*
						 * if the price of foreign currency denominated in local
						 * currency is higher, than the inverse price of the
						 * local currency denominated in foreign currency, then
						 * the local currency should be bought low and sold high
						 */
						else if (MathUtil.greater(
								priceOfForeignCurrencyInLocalCurrency,
								correctPriceOfForeignCurrencyInLocalCurrency)) {
							this.buyLocalCurrencyWithForeignCurrency(
									foreignCurrency,
									correctPriceOfForeignCurrencyInLocalCurrency,
									foreignCurrencyBankAccount);
						}
					}
				}
			}
		}

		protected void buyForeignCurrencyWithLocalCurrency(
				Currency foreignCurrency, double budgetInLocalCurrency,
				double maxPricePerUnit, BankAccount foreignCurrencyBankAccount) {

			// buy foreign currency for low price
			MarketFactory.getInstance().buy(
					foreignCurrency,
					-1,
					budgetInLocalCurrency,
					maxPricePerUnit,
					CreditBank.this,
					CreditBank.this.transactionsBankAccount,
					CreditBank.this.bankPasswords
							.get(CreditBank.this.transactionsBankAccount
									.getManagingBank()),
					foreignCurrencyBankAccount);

			// and offer it for high price (the correct price)
			double amount = CreditBank.this.transactionForeignCurrencyAccounts
					.get(foreignCurrency).getBalance();
			MarketFactory.getInstance().placeSettlementSellingOffer(
					foreignCurrency,
					CreditBank.this,
					CreditBank.this.transactionsBankAccount,
					amount,
					maxPricePerUnit,
					foreignCurrencyBankAccount,
					CreditBank.this.bankPasswords
							.get(foreignCurrencyBankAccount.getManagingBank()),
					new SettlementMarketEvent());
		}

		protected void buyLocalCurrencyWithForeignCurrency(
				Currency foreignCurrency, double maxPricePerUnit,
				BankAccount foreignCurrencyBankAccount) {

			// buy local currency for low price with foreign currency
			MarketFactory.getInstance().buy(
					primaryCurrency,
					-1,
					foreignCurrencyBankAccount.getBalance(),
					maxPricePerUnit,
					CreditBank.this,
					foreignCurrencyBankAccount,
					CreditBank.this.bankPasswords
							.get(foreignCurrencyBankAccount.getManagingBank()),
					CreditBank.this.transactionsBankAccount);
		}
	}

	public class PrimaryCurrencyOfferEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			CreditBank.this.assureCentralBankAccount();
			CreditBank.this.assureTransactionsBankAccount();
			CreditBank.this.assureTransactionsForeignCurrencyBankAccounts();

			/*
			 * prepare pricing behaviours
			 */
			for (PricingBehaviour pricingBehaviour : CreditBank.this.foreignCurrencyPricingBehaviours
					.values()) {
				pricingBehaviour.nextPeriod();
			}

			int numberOfForeignCurrencies = CreditBank.this.transactionForeignCurrencyAccounts
					.keySet().size();
			if (numberOfForeignCurrencies > 0) {
				double budgetPerForeignCurrencyInPrimaryCurrency = (CreditBank.this.MAX_CREDIT + CreditBank.this.transactionsBankAccount
						.getBalance()) / (double) numberOfForeignCurrencies;
				if (MathUtil.greater(budgetPerForeignCurrencyInPrimaryCurrency,
						0)) {
					/*
					 * offer primary currency on exchange markets, denominated
					 * in foreign currency
					 */
					for (Entry<Currency, BankAccount> entry : CreditBank.this.transactionForeignCurrencyAccounts
							.entrySet()) {
						Currency foreignCurrency = entry.getKey();
						BankAccount foreignCurrencyBankAccount = entry
								.getValue();
						PricingBehaviour pricingBehaviour = CreditBank.this.foreignCurrencyPricingBehaviours
								.get(foreignCurrency);

						pricingBehaviour.setNewPrice();

						// remove existing offers
						MarketFactory.getInstance().removeAllSellingOffers(
								CreditBank.this, foreignCurrency,
								CreditBank.this.primaryCurrency);

						// calculate exchange rate
						double priceOfLocalCurrencyInForeignCurrency = MarketFactory
								.getInstance().getMarginalPrice(
										foreignCurrency,
										CreditBank.this.primaryCurrency);
						if (Double.isNaN(priceOfLocalCurrencyInForeignCurrency))
							priceOfLocalCurrencyInForeignCurrency = pricingBehaviour
									.getCurrentPrice();

						// offer money amount on the market
						MarketFactory
								.getInstance()
								.placeSettlementSellingOffer(
										foreignCurrency,
										CreditBank.this,
										CreditBank.this.transactionsBankAccount,
										budgetPerForeignCurrencyInPrimaryCurrency,
										pricingBehaviour.getCurrentPrice(),
										foreignCurrencyBankAccount,
										CreditBank.this.bankPasswords
												.get(foreignCurrencyBankAccount
														.getManagingBank()),
										new SettlementMarketEvent());
						pricingBehaviour
								.registerOfferedAmount(budgetPerForeignCurrencyInPrimaryCurrency);
					}
				}
			}
		}
	}
}