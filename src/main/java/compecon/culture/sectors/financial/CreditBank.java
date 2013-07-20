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
import compecon.culture.sectors.financial.BankAccount.BankAccountType;
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
import compecon.engine.dao.DAOFactory;
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
public class CreditBank extends Bank implements ICentralBankCustomer {

	@Transient
	private boolean centralBankAccountsInitialized = false;

	@Transient
	protected final double MAX_CREDIT_FOR_CURRENCY_TRADING = 100000;

	@Transient
	protected final double MIN_ARBITRAGE_MARGIN = 0.03;

	@Transient
	protected final double MIN_OFFER_MARGIN = 0.01;

	@Transient
	protected Map<Currency, PricingBehaviour> localCurrencyPricingBehaviours = new HashMap<Currency, PricingBehaviour>();

	@ElementCollection
	@CollectionTable(name = "CreditBank_OfferedCurrencies", joinColumns = @JoinColumn(name = "creditbank_id"))
	@Column(name = "offeredcurrency")
	protected Set<Currency> offeredCurrencies = new HashSet<Currency>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "CreditBank_IssuedBonds", joinColumns = @JoinColumn(name = "creditBank_id"), inverseJoinColumns = @JoinColumn(name = "bond_id"))
	protected Set<Bond> issuedBonds = new HashSet<Bond>();

	@OneToMany
	@JoinTable(name = "CreditBank_ForeignCurrencyBankAccounts", joinColumns = @JoinColumn(name = "creditBank_id"), inverseJoinColumns = @JoinColumn(name = "bankAccount_id"))
	@MapKeyEnumerated
	protected Map<Currency, BankAccount> currencyTradeBankAccounts = new HashMap<Currency, BankAccount>();

	@Override
	public void initialize() {
		super.initialize();

		// trade currencies on exchange markets
		ITimeSystemEvent currencyTradeEvent = new CurrencyTradeEvent();
		this.timeSystemEvents.add(currencyTradeEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				currencyTradeEvent, -1, MonthType.EVERY, DayType.EVERY,
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

		// bonds trading
		ITimeSystemEvent bondsTradeEvent = new BondsTradeEvent();
		this.timeSystemEvents.add(bondsTradeEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(bondsTradeEvent,
				-1, MonthType.EVERY, DayType.EVERY,
				TimeSystem.getInstance().suggestRandomHourType());

		// pricing behaviours
		for (Currency foreignCurrency : Currency.values()) {
			if (!this.primaryCurrency.equals(foreignCurrency)) {
				// price of local currency in foreign currency
				double initialPriceOfLocalCurrencyInForeignCurrency = MarketFactory
						.getInstance().getMarginalPrice(foreignCurrency,
								this.primaryCurrency);
				if (Double.isNaN(initialPriceOfLocalCurrencyInForeignCurrency))
					initialPriceOfLocalCurrencyInForeignCurrency = 1.0;
				this.localCurrencyPricingBehaviours.put(foreignCurrency,
						new PricingBehaviour(this, this.primaryCurrency,
								foreignCurrency,
								initialPriceOfLocalCurrencyInForeignCurrency,
								0.01));
			}
		}
	}

	@Transient
	public void deconstruct() {
		super.deconstruct();

		this.currencyTradeBankAccounts = null;
	}

	/*
	 * accessors
	 */

	public Map<Currency, BankAccount> getCurrencyTradeBankAccounts() {
		return currencyTradeBankAccounts;
	}

	public Set<Bond> getIssuedBonds() {
		return issuedBonds;
	}

	public Set<Currency> getOfferedCurrencies() {
		return offeredCurrencies;
	}

	public void setCurrencyTradeBankAccounts(
			Map<Currency, BankAccount> transactionForeignCurrencyAccounts) {
		this.currencyTradeBankAccounts = transactionForeignCurrencyAccounts;
	}

	public void setIssuedBonds(Set<Bond> issuedBonds) {
		this.issuedBonds = issuedBonds;
	}

	public void setOfferedCurrencies(Set<Currency> offeredCurrencies) {
		this.offeredCurrencies = offeredCurrencies;
	}

	/*
	 * assertions
	 */

	@Transient
	@Override
	public void assureTransactionsBankAccount() {
		if (this.isDeconstructed)
			return;

		this.assureSelfCustomerAccount();

		if (this.transactionsBankAccount == null) {
			/*
			 * initialize the banks own bank account and open a customer account
			 * at this new bank, so that this bank can transfer money from its
			 * own bank account
			 */
			this.transactionsBankAccount = this.primaryBank.openBankAccount(
					this, this.primaryCurrency,
					this.bankPasswords.get(this.primaryBank),
					"transactions account", BankAccountType.GIRO);
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
						this, currency, centralBankPassword,
						"central bank account", BankAccountType.GIRO);
				this.bankPasswords.put(
						AgentFactory.getInstanceCentralBank(currency),
						centralBankPassword);
			}
			this.centralBankAccountsInitialized = true;
		}
	}

	@Transient
	public void assureCurrencyTradeBankAccounts() {
		if (this.isDeconstructed)
			return;

		this.assureSelfCustomerAccount();

		/*
		 * for all currencies
		 */
		for (Currency currency : Currency.values()) {
			// if there is no bank account for this currency, yet
			if (!this.currencyTradeBankAccounts.containsKey(currency)) {

				// foreign currency?
				if (!this.primaryCurrency.equals(currency)) {
					CreditBank foreignCurrencyCreditBank = AgentFactory
							.getRandomInstanceCreditBank(currency);
					if (foreignCurrencyCreditBank != null) {
						String bankPassword = foreignCurrencyCreditBank
								.openCustomerAccount(this);
						this.bankPasswords.put(foreignCurrencyCreditBank,
								bankPassword);
						BankAccount bankAccount = foreignCurrencyCreditBank
								.openBankAccount(
										this,
										currency,
										this.bankPasswords
												.get(foreignCurrencyCreditBank),
										"currency trade (foreign) account",
										BankAccountType.GIRO);
						this.currencyTradeBankAccounts.put(currency,
								bankAccount);
					}
				}

				// local currency
				else {
					this.currencyTradeBankAccounts.put(this.primaryCurrency,
							CreditBank.this.openBankAccount(this,
									this.primaryCurrency,
									this.bankPasswords.get(CreditBank.this),
									"currency trade (local)",
									BankAccountType.GIRO));
				}
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
		Log.bank_onTransfer(from, to, from.getCurrency(), amount, subject);
		this.transferMoney(from, to, amount, password, subject, false);
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

		if (!from.getCurrency().equals(to.getCurrency()))
			throw new RuntimeException(
					"both bank accounts must have the same currency");

		if (from == to)
			throw new RuntimeException("the bank accounts are identical");

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
					.getBankAccounts(this, this.bankPasswords.get(centralBank))
					.get(0);

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
		for (BankAccount creditBankAccount : DAOFactory.getBankAccountDAO()
				.findAllBankAccountsManagedByBank(CreditBank.this)) {
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
	public void onBankCloseBankAccount(BankAccount bankAccount) {
		if (this.currencyTradeBankAccounts != null) {
			for (Entry<Currency, BankAccount> entry : new HashMap<Currency, BankAccount>(
					this.currencyTradeBankAccounts).entrySet()) {
				if (entry.getValue() == bankAccount)
					this.currencyTradeBankAccounts.remove(entry.getKey());
			}
		}

		super.onBankCloseBankAccount(bankAccount);
	}

	protected double calculateLocalCurrencyBudgetForCurrencyTrading() {
		this.assureCurrencyTradeBankAccounts();

		// division by 2 so that the period-wise the budget converges to max
		// credit. This ensures, that in each period there is budget left to
		// quote on the currency markets
		return (CreditBank.this.MAX_CREDIT_FOR_CURRENCY_TRADING + this.currencyTradeBankAccounts
				.get(this.primaryCurrency).getBalance()) / 2;
	}

	protected class SettlementMarketEvent implements ISettlementEvent {
		@Override
		public void onEvent(GoodType goodType, double amount,
				double pricePerUnit, Currency currency) {
		}

		@Override
		public void onEvent(Currency commodityCurrency, double amount,
				double pricePerUnit, Currency currency) {
			if (CreditBank.this.primaryCurrency.equals(commodityCurrency)) {
				CreditBank.this.localCurrencyPricingBehaviours.get(currency)
						.registerSelling(amount);
			}
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

			for (BankAccount bankAccount : DAOFactory.getBankAccountDAO()
					.findAllBankAccountsManagedByBank(CreditBank.this)) {
				if (bankAccount.getOwner() != CreditBank.this) {
					double monthlyInterest = bankAccount.getBalance()
							* CreditBank.this
									.calculateMonthlyNominalInterestRate(AgentFactory
											.getInstanceCentralBank(

											bankAccount.getCurrency())
											.getEffectiveKeyInterestRate());
					double dailyInterest = monthlyInterest / 30;

					// liability account + positive interest rate or asset
					// account +
					// negative interest rate
					if (dailyInterest > 0) {
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
					}
					// asset account + positive interest rate or liability
					// account + negative interest rate
					else if (dailyInterest < 0) {
						try {
							// credit banks add margin on key interest rate
							dailyInterest = -1 * dailyInterest * 1.5;
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
						.getBankAccounts(CreditBank.this, centralBankPassword)
						.get(0);
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

					PropertyRegister.getInstance().registerProperty(
							centralBank, bond);

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

			// bank accounts managed by this bank
			for (BankAccount bankAccount : DAOFactory.getBankAccountDAO()
					.findAllBankAccountsManagedByBank(CreditBank.this)) {
				if (bankAccount.getCurrency() != CreditBank.this.primaryCurrency)
					throw new RuntimeException("incorrect currency");

				if (bankAccount.getBalance() > 0) // passive account
					balanceSheet.bankBorrowings += bankAccount.getBalance();
				else
					// active account
					balanceSheet.bankLoans += bankAccount.getBalance() * -1;
			}

			// TODO: bank accounts of banks

			// TODO: bank accounts of this bank at other banks including foreign
			// currency

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

	public class CurrencyTradeEvent implements ITimeSystemEvent {

		@Override
		public void onEvent() {
			CreditBank.this.assureCurrencyTradeBankAccounts();

			// the primary currency is one of the keys of this collection of
			// bank accounts -> -1
			int numberOfForeignCurrencies = CreditBank.this.currencyTradeBankAccounts
					.keySet().size() - 1;
			if (numberOfForeignCurrencies > 0) {
				this.buyForeignCurrencyForArbitrage();
				this.rebuyLocalCurrency();
				this.offerLocalCurrency();
				this.offerForeignCurrencies();
			}
		}

		/**
		 * @param firstCurrency
		 *            Currency to calculate the inverse price for, e. g. USD
		 * @param secondCurrency
		 *            Currency, to use for calculating the price of first
		 *            currency, e. g. EUR
		 */
		private double calculateCalculatoryPriceOfFirstCurrencyInSecondCurrency(
				Currency firstCurrency, Currency secondCurrency) {
			// e.g. USD_in_EUR = 0.8
			double priceOfFirstCurrencyInSecondCurrency = MarketFactory
					.getInstance().getMarginalPrice(secondCurrency,
							firstCurrency);
			// e.g. EUR_in_USD = 0.8
			double priceOfSecondCurrencyInFirstCurrency = MarketFactory
					.getInstance().getMarginalPrice(firstCurrency,
							secondCurrency);

			if (Double.isNaN(priceOfSecondCurrencyInFirstCurrency)) {
				return priceOfFirstCurrencyInSecondCurrency;
			} else if (Double.isNaN(priceOfFirstCurrencyInSecondCurrency)) {
				return Double.NaN;
			} else {
				// inverse_EUR_in_USD -> correct_USD_in_EUR = 1.25
				double correctPriceOfFirstCurrencyInSecondCurrency = 1.0 / priceOfSecondCurrencyInFirstCurrency;

				if (Log.isAgentSelectedByClient(CreditBank.this))
					Log.log(CreditBank.this,
							"on markets 1 "
									+ secondCurrency.getIso4217Code()
									+ " = "
									+ Currency
											.round(priceOfSecondCurrencyInFirstCurrency)
									+ " "
									+ firstCurrency.getIso4217Code()

									+ " -> correct price of 1 "
									+ firstCurrency.getIso4217Code()
									+ " = "
									+ Currency
											.round(correctPriceOfFirstCurrencyInSecondCurrency)
									+ " "
									+ secondCurrency.getIso4217Code()

									+ "; on markets "

									+ "1 "
									+ firstCurrency.getIso4217Code()
									+ " = "
									+ Currency
											.round(priceOfFirstCurrencyInSecondCurrency)
									+ " " + secondCurrency.getIso4217Code());
				return correctPriceOfFirstCurrencyInSecondCurrency;
			}
		}

		/**
		 * number in [0, 1] with 1 as max dampening and 0 as no dampening
		 */
		private double calculateCurrencyPriceBuyingDamper(
				double marketPriceOfCurrency, double correctPriceOfCurrency) {
			double priceDifference = Math.max(0, marketPriceOfCurrency
					- correctPriceOfCurrency);
			double relativePriceDifference = priceDifference
					/ correctPriceOfCurrency;
			return 1.0 - Math.pow(2, -5 * relativePriceDifference);
		}

		protected void buyForeignCurrencyForArbitrage() {
			int numberOfForeignCurrencies = CreditBank.this.currencyTradeBankAccounts
					.keySet().size() - 1;
			if (numberOfForeignCurrencies > 0) {

				double budgetForCurrencyTradingPerCurrency_InPrimaryCurrency = CreditBank.this
						.calculateLocalCurrencyBudgetForCurrencyTrading()
						/ (double) numberOfForeignCurrencies;

				/*
				 * arbitrage on exchange markets
				 */
				for (Entry<Currency, BankAccount> entry : CreditBank.this.currencyTradeBankAccounts
						.entrySet()) {

					BankAccount localCurrencyTradeBankAccount = CreditBank.this.currencyTradeBankAccounts
							.get(CreditBank.this.primaryCurrency);

					/*
					 * trade between local and foreign currencies
					 */
					if (!CreditBank.this.primaryCurrency.equals(entry.getKey())) {
						Currency foreignCurrency = entry.getKey();
						BankAccount foreignCurrencyBankAccount = entry
								.getValue();

						double realPriceOfForeignCurrencyInLocalCurrency = MarketFactory
								.getInstance().getMarginalPrice(
										primaryCurrency, foreignCurrency);
						double correctPriceOfForeignCurrencyInLocalCurrency = calculateCalculatoryPriceOfFirstCurrencyInSecondCurrency(
								foreignCurrency, primaryCurrency);

						if (MathUtil
								.lesserEqual(
										budgetForCurrencyTradingPerCurrency_InPrimaryCurrency,
										0)) {
							if (Log.isAgentSelectedByClient(CreditBank.this))
								Log.log(CreditBank.this,
										"-> no arbitrage with "
												+ foreignCurrency
														.getIso4217Code()
												+ ", since budgetForCurrencyTrading is "
												+ MathUtil
														.round(budgetForCurrencyTradingPerCurrency_InPrimaryCurrency));
						} else if (Double
								.isNaN(correctPriceOfForeignCurrencyInLocalCurrency)) {
							Log.log(CreditBank.this,
									"-> no arbitrage with "
											+ foreignCurrency.getIso4217Code()
											+ ", since correct price of foreign currency is "
											+ correctPriceOfForeignCurrencyInLocalCurrency);
						} else if (MathUtil.lesser(
								correctPriceOfForeignCurrencyInLocalCurrency
										/ (1 + MIN_ARBITRAGE_MARGIN),
								realPriceOfForeignCurrencyInLocalCurrency)) {
							if (Log.isAgentSelectedByClient(CreditBank.this))
								Log.log(CreditBank.this,
										"-> no arbitrage with "
												+ foreignCurrency
														.getIso4217Code()
												+ ", since price of "
												+ foreignCurrency
														.getIso4217Code()
												+ " is too high");
						} else {
							/*
							 * if the price of foreign currency denominated in
							 * local currency is lower, than the inverse price
							 * of the local currency denominated in foreign
							 * currency, then the foreign currency should be
							 * bought low and sold high
							 */
							MarketFactory
									.getInstance()
									.buy(foreignCurrency,
											Double.NaN,
											budgetForCurrencyTradingPerCurrency_InPrimaryCurrency,
											correctPriceOfForeignCurrencyInLocalCurrency
													/ (1 + MIN_ARBITRAGE_MARGIN),
											CreditBank.this,
											localCurrencyTradeBankAccount,
											CreditBank.this.bankPasswords
													.get(localCurrencyTradeBankAccount
															.getManagingBank()),
											foreignCurrencyBankAccount);
						}
					}
				}
			}
		}

		protected void rebuyLocalCurrency() {
			/*
			 * buy local currency on exchange markets, denominated in foreign
			 * currency
			 */
			// for each foreign currency bank account / foreign currency
			for (BankAccount foreignCurrencyBankAccount : CreditBank.this.currencyTradeBankAccounts
					.values()) {
				if (!CreditBank.this.primaryCurrency
						.equals(foreignCurrencyBankAccount.getCurrency())) {
					/*
					 * buy local currency for foreign currency
					 */

					Currency localCurrency = CreditBank.this.primaryCurrency;
					BankAccount localCurrencyBankAccount = CreditBank.this.currencyTradeBankAccounts
							.get(localCurrency);

					double realPriceOfForeignCurrencyInLocalCurrency = MarketFactory
							.getInstance().getMarginalPrice(primaryCurrency,
									foreignCurrencyBankAccount.getCurrency());
					double correctPriceOfLocalCurrencyInForeignCurrency = calculateCalculatoryPriceOfFirstCurrencyInSecondCurrency(
							localCurrency,
							foreignCurrencyBankAccount.getCurrency());

					double currencyPriceBuyingDamper = this
							.calculateCurrencyPriceBuyingDamper(
									realPriceOfForeignCurrencyInLocalCurrency,
									correctPriceOfLocalCurrencyInForeignCurrency);

					if (MathUtil.greater(
							foreignCurrencyBankAccount.getBalance(), 0)) {
						// buy local currency for foreign currency
						MarketFactory.getInstance().buy(
								localCurrency,
								Double.NaN,
								(1 - currencyPriceBuyingDamper)
										* foreignCurrencyBankAccount
												.getBalance(),
								Double.NaN,
								CreditBank.this,
								foreignCurrencyBankAccount,
								CreditBank.this.bankPasswords
										.get(foreignCurrencyBankAccount
												.getManagingBank()),
								localCurrencyBankAccount);
					}
				}
			}
		}

		protected void offerLocalCurrency() {
			/*
			 * prepare pricing behaviours
			 */
			for (PricingBehaviour pricingBehaviour : CreditBank.this.localCurrencyPricingBehaviours
					.values()) {
				pricingBehaviour.nextPeriod();
			}

			/*
			 * offer local currency on exchange markets, denominated in foreign
			 * currency
			 */

			double totalLocalCurrencyBudgetForCurrencyTrading = CreditBank.this
					.calculateLocalCurrencyBudgetForCurrencyTrading();

			// if there is no budget in local currency left for offering
			// against foreign currency
			if (MathUtil.lesserEqual(
					totalLocalCurrencyBudgetForCurrencyTrading, 0)) {
				if (Log.isAgentSelectedByClient(CreditBank.this))
					Log.log(CreditBank.this,
							"not offering "
									+ CreditBank.this.primaryCurrency
											.getIso4217Code()
									+ " for foreign currencies, as budget / amount to offer is "
									+ Currency
											.round(totalLocalCurrencyBudgetForCurrencyTrading)
									+ " "
									+ CreditBank.this.primaryCurrency
											.getIso4217Code());
			} else {
				int numberOfForeignCurrencies = CreditBank.this.currencyTradeBankAccounts
						.keySet().size() - 1;
				double partialLocalCurrencyBudgetForCurrency = totalLocalCurrencyBudgetForCurrencyTrading
						/ (double) numberOfForeignCurrencies;
				// for each foreign currency bank account / foreign currency
				for (BankAccount foreignCurrencyBankAccount : CreditBank.this.currencyTradeBankAccounts
						.values()) {
					if (!CreditBank.this.primaryCurrency
							.equals(foreignCurrencyBankAccount.getCurrency())) {
						/*
						 * offer local currency for foreign currency
						 */
						Currency localCurrency = CreditBank.this.primaryCurrency;
						Currency foreignCurrency = foreignCurrencyBankAccount
								.getCurrency();
						BankAccount localCurrencyBankAccount = CreditBank.this.currencyTradeBankAccounts
								.get(localCurrency);
						PricingBehaviour pricingBehaviour = CreditBank.this.localCurrencyPricingBehaviours
								.get(foreignCurrency);

						// calculate exchange rate
						pricingBehaviour.setNewPrice();
						double priceOfLocalCurrencyInForeignCurrency = pricingBehaviour
								.getCurrentPrice();
						if (Double.isNaN(priceOfLocalCurrencyInForeignCurrency)) {
							priceOfLocalCurrencyInForeignCurrency = MarketFactory
									.getInstance().getMarginalPrice(
											foreignCurrency, localCurrency);
							if (Log.isAgentSelectedByClient(CreditBank.this))
								Log.log(CreditBank.this,
										"could not calculate price for "
												+ localCurrency + " in "
												+ foreignCurrency
												+ " -> using market price");
						}

						// remove existing offers
						MarketFactory.getInstance()
								.removeAllSellingOffers(CreditBank.this,
										foreignCurrency, localCurrency);

						// offer money amount on the market
						MarketFactory.getInstance()
								.placeSettlementSellingOffer(
										localCurrency,
										CreditBank.this,
										foreignCurrencyBankAccount,
										partialLocalCurrencyBudgetForCurrency,
										priceOfLocalCurrencyInForeignCurrency,
										localCurrencyBankAccount,
										CreditBank.this.bankPasswords
												.get(localCurrencyBankAccount
														.getManagingBank()),
										new SettlementMarketEvent());

						pricingBehaviour
								.registerOfferedAmount(partialLocalCurrencyBudgetForCurrency);
					}
				}
			}
		}

		protected void offerForeignCurrencies() {
			// for each foreign currency bank account / foreign currency
			for (BankAccount foreignCurrencyBankAccount : CreditBank.this.currencyTradeBankAccounts
					.values()) {
				if (!CreditBank.this.primaryCurrency
						.equals(foreignCurrencyBankAccount.getCurrency())) {
					/*
					 * offer local currency for foreign currency
					 */
					Currency localCurrency = CreditBank.this.primaryCurrency;
					Currency foreignCurrency = foreignCurrencyBankAccount
							.getCurrency();
					BankAccount localCurrencyBankAccount = CreditBank.this.currencyTradeBankAccounts
							.get(localCurrency);

					// determine price of foreign currency
					double priceOfForeignCurrencyInLocalCurrency = MarketFactory
							.getInstance().getMarginalPrice(localCurrency,
									foreignCurrency);
					if (Double.isNaN(priceOfForeignCurrencyInLocalCurrency))
						priceOfForeignCurrencyInLocalCurrency = 1;

					// remove existing offers
					MarketFactory.getInstance().removeAllSellingOffers(
							CreditBank.this, localCurrency, foreignCurrency);

					// offer money amount on the market
					MarketFactory.getInstance().placeSettlementSellingOffer(
							foreignCurrency,
							CreditBank.this,
							localCurrencyBankAccount,
							foreignCurrencyBankAccount.getBalance(),
							priceOfForeignCurrencyInLocalCurrency
									* (1 + MIN_OFFER_MARGIN),
							foreignCurrencyBankAccount,
							CreditBank.this.bankPasswords
									.get(foreignCurrencyBankAccount
											.getManagingBank()), null);
				}
			}
		}
	}

	public class BondsTradeEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			CreditBank.this.assureTransactionsBankAccount();

			/*
			 * the credit bank is doing fractional reserve banking -> buy bonds
			 * for passive bank accounts
			 */

			// bank accounts of non-banks managed by this bank
			double sumOfPassiveBankAccounts = 0.0;

			for (BankAccount bankAccount : DAOFactory.getBankAccountDAO()
					.findAllBankAccountsManagedByBank(CreditBank.this)) {
				if (bankAccount.getCurrency() != CreditBank.this.primaryCurrency)
					throw new RuntimeException("incorrect currency");

				if (!(bankAccount.getOwner() instanceof Bank)) {
					if (bankAccount.getBalance() > 0
							&& BankAccountType.SAVINGS.equals(bankAccount
									.getBankAccountType())) // passive account
						sumOfPassiveBankAccounts += bankAccount.getBalance();
				}
			}

			// bonds bought from other agents
			double faceValueSumOfBonds = 0.0;

			for (Property property : PropertyRegister.getInstance()
					.getProperties(CreditBank.this, FixedRateBond.class)) {
				if (!(property instanceof FixedRateBond))
					throw new RuntimeException("not a bond");

				faceValueSumOfBonds += ((FixedRateBond) property)
						.getFaceValue();
			}

			// TODO money reserves; Basel 3
			double difference = sumOfPassiveBankAccounts - faceValueSumOfBonds;

			if (Log.isAgentSelectedByClient(CreditBank.this))
				Log.log(CreditBank.this,
						"sumOfPassiveBankAccounts = "
								+ Currency.round(sumOfPassiveBankAccounts)
								+ " "
								+ CreditBank.this.primaryCurrency
										.getIso4217Code()
								+ "; faceValueSumOfBonds = "
								+ Currency.round(faceValueSumOfBonds)
								+ " "
								+ CreditBank.this.primaryCurrency
										.getIso4217Code()
								+ " => difference = "
								+ Currency.round(difference)
								+ " "
								+ CreditBank.this.primaryCurrency
										.getIso4217Code());

			if (MathUtil.greater(difference, 0.0)) {
				MarketFactory.getInstance().buy(FixedRateBond.class,
						Double.NaN, difference, Double.NaN, CreditBank.this,
						CreditBank.this.transactionsBankAccount,
						CreditBank.this.bankPasswords.get(CreditBank.this));
			}
		}
	}
}