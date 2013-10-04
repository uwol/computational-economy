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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import compecon.economy.PricingBehaviour;
import compecon.economy.markets.SettlementMarket.ISettlementEvent;
import compecon.economy.sectors.Agent;
import compecon.economy.sectors.financial.BankAccount.BankAccountType;
import compecon.economy.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.economy.sectors.state.law.property.HardCashRegister;
import compecon.economy.sectors.state.law.property.Property;
import compecon.economy.sectors.state.law.property.PropertyRegister;
import compecon.economy.sectors.state.law.security.debt.Bond;
import compecon.economy.sectors.state.law.security.debt.FixedRateBond;
import compecon.engine.AgentFactory;
import compecon.engine.MarketFactory;
import compecon.engine.PropertyFactory;
import compecon.engine.Simulation;
import compecon.engine.dao.DAOFactory;
import compecon.engine.statistics.Log;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.HourType;
import compecon.engine.time.calendar.MonthType;
import compecon.engine.util.ConfigurationUtil;
import compecon.engine.util.MathUtil;
import compecon.materia.GoodType;

/**
 * Agent type credit bank manages bank accounts, creates money by credit and
 * follows minimum reserve requirements of central banks.
 */
@Entity
public class CreditBank extends Bank implements ICentralBankCustomer {

	@Transient
	private boolean centralBankAccountsInitialized = false;

	@Transient
	protected Map<Currency, PricingBehaviour> localCurrencyPricingBehaviours = new HashMap<Currency, PricingBehaviour>();

	@ElementCollection
	@CollectionTable(name = "CreditBank_OfferedCurrencies", joinColumns = @JoinColumn(name = "creditbank_id"))
	@Column(name = "offeredcurrency")
	protected Set<Currency> offeredCurrencies = new HashSet<Currency>();

	@OneToMany
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
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(
						currencyTradeEvent,
						-1,
						MonthType.EVERY,
						DayType.EVERY,
						Simulation.getInstance().getTimeSystem()
								.suggestRandomHourType());

		// calculate interest on customers bank accounts
		ITimeSystemEvent interestCalculationEvent = new DailyInterestCalculationEvent();
		this.timeSystemEvents.add(interestCalculationEvent);
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(interestCalculationEvent, -1, MonthType.EVERY,
						DayType.EVERY, HourType.HOUR_02);

		// check money reserves at the central bank
		ITimeSystemEvent checkMoneyReservesEvent = new CheckMoneyReservesEvent();
		this.timeSystemEvents.add(checkMoneyReservesEvent);
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(checkMoneyReservesEvent, -1, MonthType.EVERY,
						DayType.EVERY, HourType.HOUR_12);

		// balance sheet publication
		ITimeSystemEvent balanceSheetPublicationEvent = new BalanceSheetPublicationEvent();
		this.timeSystemEvents.add(balanceSheetPublicationEvent);
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(balanceSheetPublicationEvent, -1, MonthType.EVERY,
						DayType.EVERY, BALANCE_SHEET_PUBLICATION_HOUR_TYPE);

		// bonds trading
		ITimeSystemEvent bondsTradeEvent = new BondsTradeEvent();
		this.timeSystemEvents.add(bondsTradeEvent);
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(
						bondsTradeEvent,
						-1,
						MonthType.EVERY,
						DayType.EVERY,
						Simulation.getInstance().getTimeSystem()
								.suggestRandomHourType());

		// pricing behaviours
		for (Currency foreignCurrency : Currency.values()) {
			if (!this.primaryCurrency.equals(foreignCurrency)) {
				// price of local currency in foreign currency
				double initialPriceOfLocalCurrencyInForeignCurrency = MarketFactory
						.getInstance().getPrice(foreignCurrency,
								this.primaryCurrency);
				if (Double.isNaN(initialPriceOfLocalCurrencyInForeignCurrency))
					initialPriceOfLocalCurrencyInForeignCurrency = 1.0;
				this.localCurrencyPricingBehaviours.put(
						foreignCurrency,
						new PricingBehaviour(this, this.primaryCurrency,
								foreignCurrency,
								initialPriceOfLocalCurrencyInForeignCurrency,
								ConfigurationUtil.CreditBankConfig
										.getPriceChangeIncrement()));
			}
		}
	}

	@Transient
	public void deconstruct() {
		super.deconstruct();

		for (Bond bond : this.issuedBonds) {
			PropertyFactory.deleteProperty(bond);
		}
		this.currencyTradeBankAccounts = null;

		// deregister from banks
		for (Currency currency : Currency.values()) {
			AgentFactory.getInstanceCentralBank(currency).closeCustomerAccount(
					this);
		}
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
					this, this.primaryCurrency, "transactions account",
					BankAccountType.GIRO);
		}
	}

	@Transient
	public void assureCentralBankAccount() {
		if (this.isDeconstructed)
			return;

		if (!this.centralBankAccountsInitialized) {
			// initialize bank accounts at central banks
			for (Currency currency : offeredCurrencies) {
				AgentFactory.getInstanceCentralBank(currency).openBankAccount(
						this, currency, "central bank account",
						BankAccountType.GIRO);
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
						BankAccount bankAccount = foreignCurrencyCreditBank
								.openBankAccount(this, currency,
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
									"currency trade (local)",
									BankAccountType.GIRO));
				}
			}
		}
	}

	@Transient
	protected void assertCurrencyIsOffered(Currency currency) {
		assert (this.getOfferedCurrencies().contains(currency));
	}

	/*
	 * business logic
	 */

	@Transient
	public void depositCash(Agent client, BankAccount to, double amount,
			Currency currency) {
		this.assertIsCustomerOfThisBank(client);
		this.assertBankAccountIsManagedByThisBank(to);
		this.assertCurrencyIsOffered(currency);

		// transfer money
		HardCashRegister.getInstance().decrement(client, currency, amount);
		to.deposit(amount);
	}

	@Transient
	public double withdrawCash(Agent client, BankAccount from, double amount,
			Currency currency) {
		this.assertIsCustomerOfThisBank(client);
		this.assertBankAccountIsManagedByThisBank(from);
		this.assertCurrencyIsOffered(currency);

		// transfer money
		from.withdraw(amount);
		return HardCashRegister.getInstance().increment(client, currency,
				amount);
	}

	@Transient
	public void transferMoney(BankAccount from, BankAccount to, double amount,
			String subject) {
		this.transferMoney(from, to, amount, subject, false);
	}

	@Transient
	protected void transferMoney(BankAccount from, BankAccount to,
			double amount, String subject, boolean negativeAmountOK) {
		this.assureCentralBankAccount();
		this.assertIsCustomerOfThisBank(from.getOwner());
		this.assertBankAccountIsManagedByThisBank(from);

		assert (negativeAmountOK || amount >= 0);
		assert (from.getCurrency().equals(to.getCurrency()));
		assert (from.getBalance() - amount >= 0.0 || from
				.getOverdraftPossible());

		// no Exception for identical bank accounts, as this correctly
		// might happen in case of bonds etc.
		if (from != to) {
			Log.bank_onTransfer(from, to, from.getCurrency(), amount, subject);

			// is the money flowing internally in this bank?
			if (to.getManagingBank() == this && from.getManagingBank() == this) {
				// transfer money internally
				from.withdraw(amount);
				to.deposit(amount);
			} else { // transfer to another bank
				CentralBank centralBank = AgentFactory
						.getInstanceCentralBank(from.getCurrency());

				// central bank account of this credit bank
				BankAccount toCentralBankAccountOfThisBank = centralBank
						.getBankAccounts(this).get(0);

				// transfer money to central bank account of this bank
				centralBank.transferMoney(from, toCentralBankAccountOfThisBank,
						amount, subject);

				// transfer money from central bank account of this bank to bank
				// account at target bank
				centralBank.transferMoney(toCentralBankAccountOfThisBank, to,
						amount, subject);
			}
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
	public void deposit(CentralBank caller, BankAccount bankAccount,
			double amount) {

		this.assertBankAccountIsManagedByThisBank(bankAccount);
		assert (amount >= 0.0);

		bankAccount.deposit(amount);
	}

	@Transient
	public void withdraw(CentralBank caller, BankAccount bankAccount,
			double amount) {

		this.assertBankAccountIsManagedByThisBank(bankAccount);
		assert (amount >= 0.0);

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
		return (ConfigurationUtil.CreditBankConfig
				.getMaxCreditForCurrencyTrading() + this.currencyTradeBankAccounts
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
						.registerSelling(amount, amount * pricePerUnit);
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
						CreditBank.this.transferMoney(
								CreditBank.this.transactionsBankAccount,
								bankAccount, dailyInterest,
								"interest earned for customer");
					}
					// asset account + positive interest rate or liability
					// account + negative interest rate
					else if (dailyInterest < 0) {
						// credit banks add margin on key interest rate
						dailyInterest = -1 * dailyInterest * 1.5;
						CreditBank.this.transferMoney(bankAccount,
								CreditBank.this.transactionsBankAccount,
								dailyInterest, "debt interest from customer");
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

				BankAccount bankAccountAtCentralBank = centralBank
						.getBankAccounts(CreditBank.this).get(0);
				double sumOfBorrowings = CreditBank.this
						.getSumOfBorrowings(currency);
				double moneyReserveGap = sumOfBorrowings
						* centralBank.getReserveRatio()
						- bankAccountAtCentralBank.getBalance();

				// not enough money deposited at central bank
				if (moneyReserveGap > 0.0) {
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
									CreditBank.this,
									currency,
									CreditBank.this.transactionsBankAccount,
									moneyReserveGap,
									centralBank.getEffectiveKeyInterestRate() + 0.02);
					bonds.add(bond);

					// obtain tender for bond
					centralBank.obtainTender(CreditBank.this, bonds);

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
				assert (bankAccount.getCurrency()
						.equals(CreditBank.this.primaryCurrency));

				if (bankAccount.getBalance() > 0.0) // passive account
					balanceSheet.bankBorrowings += bankAccount.getBalance();
				else
					// active account
					balanceSheet.bankLoans += bankAccount.getBalance() * -1.0;
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
					.getInstance().getPrice(secondCurrency, firstCurrency);
			// e.g. EUR_in_USD = 0.8
			double priceOfSecondCurrencyInFirstCurrency = MarketFactory
					.getInstance().getPrice(firstCurrency, secondCurrency);

			if (Double.isNaN(priceOfSecondCurrencyInFirstCurrency)) {
				return priceOfFirstCurrencyInSecondCurrency;
			} else if (Double.isNaN(priceOfFirstCurrencyInSecondCurrency)) {
				return Double.NaN;
			} else {
				// inverse_EUR_in_USD -> correct_USD_in_EUR = 1.25
				double correctPriceOfFirstCurrencyInSecondCurrency = 1.0 / priceOfSecondCurrencyInFirstCurrency;

				if (Log.isAgentSelectedByClient(CreditBank.this))
					Log.log(CreditBank.this,
							CurrencyTradeEvent.class,
							"on markets 1 "
									+ secondCurrency.getIso4217Code()
									+ " = "
									+ Currency
											.formatMoneySum(priceOfSecondCurrencyInFirstCurrency)
									+ " "
									+ firstCurrency.getIso4217Code()

									+ " -> correct price of 1 "
									+ firstCurrency.getIso4217Code()
									+ " = "
									+ Currency
											.formatMoneySum(correctPriceOfFirstCurrencyInSecondCurrency)
									+ " "
									+ secondCurrency.getIso4217Code()

									+ "; on markets "

									+ "1 "
									+ firstCurrency.getIso4217Code()
									+ " = "
									+ Currency
											.formatMoneySum(priceOfFirstCurrencyInSecondCurrency)
									+ " " + secondCurrency.getIso4217Code());
				return correctPriceOfFirstCurrencyInSecondCurrency;
			}
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
								.getInstance().getPrice(primaryCurrency,
										foreignCurrency);
						double correctPriceOfForeignCurrencyInLocalCurrency = calculateCalculatoryPriceOfFirstCurrencyInSecondCurrency(
								foreignCurrency, primaryCurrency);

						if (MathUtil
								.lesserEqual(
										budgetForCurrencyTradingPerCurrency_InPrimaryCurrency,
										0)) {
							if (Log.isAgentSelectedByClient(CreditBank.this))
								Log.log(CreditBank.this,
										CurrencyTradeEvent.class,
										"-> no arbitrage with "
												+ foreignCurrency
														.getIso4217Code()
												+ ", since budgetForCurrencyTrading is "
												+ MathUtil
														.round(budgetForCurrencyTradingPerCurrency_InPrimaryCurrency));
						} else if (Double
								.isNaN(correctPriceOfForeignCurrencyInLocalCurrency)) {
							Log.log(CreditBank.this,
									CurrencyTradeEvent.class,
									"-> no arbitrage with "
											+ foreignCurrency.getIso4217Code()
											+ ", since correct price of foreign currency is "
											+ correctPriceOfForeignCurrencyInLocalCurrency);
						} else if (MathUtil
								.lesser(correctPriceOfForeignCurrencyInLocalCurrency
										/ (1 + ConfigurationUtil.CreditBankConfig
												.getMinArbitrageMargin()),
										realPriceOfForeignCurrencyInLocalCurrency)) {
							if (Log.isAgentSelectedByClient(CreditBank.this))
								Log.log(CreditBank.this,
										CurrencyTradeEvent.class,
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
													/ (1 + ConfigurationUtil.CreditBankConfig
															.getMinArbitrageMargin()),
											CreditBank.this,
											localCurrencyTradeBankAccount,
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

					if (MathUtil.greater(
							foreignCurrencyBankAccount.getBalance(), 0)) {
						// buy local currency for foreign currency
						MarketFactory.getInstance().buy(localCurrency,
								Double.NaN,
								foreignCurrencyBankAccount.getBalance(),
								Double.NaN, CreditBank.this,
								foreignCurrencyBankAccount,
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
							CurrencyTradeEvent.class,
							"not offering "
									+ CreditBank.this.primaryCurrency
											.getIso4217Code()
									+ " for foreign currencies, as budget / amount to offer is "
									+ Currency
											.formatMoneySum(totalLocalCurrencyBudgetForCurrencyTrading)
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
						double pricingBehaviourPriceOfLocalCurrencyInForeignCurrency = pricingBehaviour
								.getCurrentPrice();

						if (Double
								.isNaN(pricingBehaviourPriceOfLocalCurrencyInForeignCurrency)) {
							pricingBehaviourPriceOfLocalCurrencyInForeignCurrency = MarketFactory
									.getInstance().getPrice(foreignCurrency,
											localCurrency);
							if (Log.isAgentSelectedByClient(CreditBank.this))
								Log.log(CreditBank.this,
										CurrencyTradeEvent.class,
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
						MarketFactory
								.getInstance()
								.placeSettlementSellingOffer(
										localCurrency,
										CreditBank.this,
										foreignCurrencyBankAccount,
										partialLocalCurrencyBudgetForCurrency,
										pricingBehaviourPriceOfLocalCurrencyInForeignCurrency,
										localCurrencyBankAccount,
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
					 * offer foreign currency for local currency
					 */
					Currency localCurrency = CreditBank.this.primaryCurrency;
					Currency foreignCurrency = foreignCurrencyBankAccount
							.getCurrency();
					BankAccount localCurrencyBankAccount = CreditBank.this.currencyTradeBankAccounts
							.get(localCurrency);

					// determine price of foreign currency
					double priceOfForeignCurrencyInLocalCurrency = MarketFactory
							.getInstance().getPrice(localCurrency,
									foreignCurrency);
					if (Double.isNaN(priceOfForeignCurrencyInLocalCurrency))
						priceOfForeignCurrencyInLocalCurrency = 1;

					// remove existing offers
					MarketFactory.getInstance().removeAllSellingOffers(
							CreditBank.this, localCurrency, foreignCurrency);

					// offer money amount on the market
					MarketFactory.getInstance().placeSettlementSellingOffer(
							foreignCurrency, CreditBank.this,
							localCurrencyBankAccount,
							foreignCurrencyBankAccount.getBalance(),
							priceOfForeignCurrencyInLocalCurrency / (1.001),
							foreignCurrencyBankAccount, null);
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
				assert (bankAccount.getCurrency()
						.equals(CreditBank.this.primaryCurrency));

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
				assert (property instanceof FixedRateBond);

				faceValueSumOfBonds += ((FixedRateBond) property)
						.getFaceValue();
			}

			// TODO money reserves; Basel 3
			double difference = sumOfPassiveBankAccounts - faceValueSumOfBonds;

			if (Log.isAgentSelectedByClient(CreditBank.this))
				Log.log(CreditBank.this,
						BondsTradeEvent.class,
						"sumOfPassiveBankAccounts = "
								+ Currency
										.formatMoneySum(sumOfPassiveBankAccounts)
								+ " "
								+ CreditBank.this.primaryCurrency
										.getIso4217Code()
								+ "; faceValueSumOfBonds = "
								+ Currency.formatMoneySum(faceValueSumOfBonds)
								+ " "
								+ CreditBank.this.primaryCurrency
										.getIso4217Code()
								+ " => difference = "
								+ Currency.formatMoneySum(difference)
								+ " "
								+ CreditBank.this.primaryCurrency
										.getIso4217Code());

			if (MathUtil.greater(difference, 0.0)) {
				MarketFactory.getInstance().buy(FixedRateBond.class,
						Double.NaN, difference, Double.NaN, CreditBank.this,
						CreditBank.this.transactionsBankAccount);
			}
		}
	}
}