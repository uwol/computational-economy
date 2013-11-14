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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.economy.agent.Agent;
import compecon.economy.behaviour.PricingBehaviour;
import compecon.economy.behaviour.impl.PricingBehaviourImpl;
import compecon.economy.bookkeeping.impl.BalanceSheetDTO;
import compecon.economy.markets.SettlementMarket.ISettlementEvent;
import compecon.economy.property.Property;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
import compecon.economy.sectors.financial.CentralBank;
import compecon.economy.sectors.financial.CentralBankCustomer;
import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.economy.sectors.state.State;
import compecon.economy.security.debt.Bond;
import compecon.economy.security.debt.FixedRateBond;
import compecon.economy.security.debt.impl.BondImpl;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.timesystem.ITimeSystemEvent;
import compecon.engine.timesystem.impl.DayType;
import compecon.engine.timesystem.impl.HourType;
import compecon.engine.timesystem.impl.MonthType;
import compecon.engine.util.MathUtil;
import compecon.materia.GoodType;

/**
 * Agent type credit bank manages bank accounts, creates money by credit and
 * follows minimum reserve requirements of central banks.
 */
@Entity
public class CreditBankImpl extends BankImpl implements CreditBank,
		CentralBankCustomer {

	/**
	 * bank account for basic daily transactions with the central bank -> money
	 * transfers to other banks
	 */
	@OneToOne(targetEntity = BankAccountImpl.class)
	@JoinColumn(name = "bankAccountCbTransactions_id")
	@Index(name = "IDX_CB_BA_CBTRANSACTIONS")
	protected BankAccount bankAccountCentralBankTransactions;

	/**
	 * bank account for money reserves in the central bank
	 */
	@OneToOne(targetEntity = BankAccountImpl.class)
	@JoinColumn(name = "bankAccountCbMoneyReserves_id")
	@Index(name = "IDX_CB_BA_CBMONEYRESERVES")
	protected BankAccount bankAccountCentralBankMoneyReserves;

	@OneToMany(targetEntity = BankAccountImpl.class)
	@JoinTable(name = "CreditBank_ForeignCurrencyBankAccounts", joinColumns = @JoinColumn(name = "creditBank_id"), inverseJoinColumns = @JoinColumn(name = "bankAccount_id"))
	@MapKeyEnumerated
	protected Map<Currency, BankAccount> bankAccountsCurrencyTrade = new HashMap<Currency, BankAccount>();

	@Transient
	protected Map<Currency, PricingBehaviour> localCurrencyPricingBehaviours = new HashMap<Currency, PricingBehaviour>();

	@OneToMany(targetEntity = BondImpl.class)
	@JoinTable(name = "CreditBank_IssuedBonds", joinColumns = @JoinColumn(name = "creditBank_id"), inverseJoinColumns = @JoinColumn(name = "bond_id"))
	protected Set<Bond> issuedBonds = new HashSet<Bond>();

	@Override
	public void initialize() {
		super.initialize();

		// trade currencies on exchange markets
		final ITimeSystemEvent currencyTradeEvent = new CurrencyTradeEvent();
		this.timeSystemEvents.add(currencyTradeEvent);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(
						currencyTradeEvent,
						-1,
						MonthType.EVERY,
						DayType.EVERY,
						ApplicationContext.getInstance().getTimeSystem()
								.suggestRandomHourType());

		// calculate interest on customers bank accounts
		final ITimeSystemEvent interestCalculationEvent = new DailyInterestCalculationEvent();
		this.timeSystemEvents.add(interestCalculationEvent);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(interestCalculationEvent, -1, MonthType.EVERY,
						DayType.EVERY, HourType.HOUR_02);

		// check money reserves at the central bank
		final ITimeSystemEvent checkMoneyReservesEvent = new CheckMoneyReservesEvent();
		this.timeSystemEvents.add(checkMoneyReservesEvent);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(checkMoneyReservesEvent, -1, MonthType.EVERY,
						DayType.EVERY, HourType.HOUR_12);

		// bonds trading
		// should happen every hour, so that money flow is distributed over the
		// period, leading to less volatility on markets
		final ITimeSystemEvent bondsTradingEvent = new BondsTradingEvent();
		this.timeSystemEvents.add(bondsTradingEvent);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEventEvery(bondsTradingEvent, -1, MonthType.EVERY,
						DayType.EVERY, HourType.EVERY);

		// pricing behaviours
		for (Currency foreignCurrency : Currency.values()) {
			if (!this.primaryCurrency.equals(foreignCurrency)) {
				// price of local currency in foreign currency
				double initialPriceOfLocalCurrencyInForeignCurrency = ApplicationContext
						.getInstance().getMarketFactory().getMarket()
						.getPrice(foreignCurrency, this.primaryCurrency);
				if (Double.isNaN(initialPriceOfLocalCurrencyInForeignCurrency))
					initialPriceOfLocalCurrencyInForeignCurrency = 1.0;
				this.localCurrencyPricingBehaviours.put(
						foreignCurrency,
						new PricingBehaviourImpl(this, this.primaryCurrency,
								foreignCurrency,
								initialPriceOfLocalCurrencyInForeignCurrency,
								ApplicationContext.getInstance()
										.getConfiguration().creditBankConfig
										.getPriceChangeIncrement()));
			}
		}
	}

	@Transient
	public void deconstruct() {
		super.deconstruct();

		for (Bond bond : this.issuedBonds) {
			ApplicationContext.getInstance().getPropertyFactory()
					.deleteProperty(bond);
		}

		this.bankAccountCentralBankMoneyReserves = null;
		this.bankAccountCentralBankTransactions = null;
		this.bankAccountsCurrencyTrade = null;
		this.bankAccountBondLoan = null;

		// deregister from banks
		for (Currency currency : Currency.values()) {
			ApplicationContext.getInstance().getAgentFactory()
					.getInstanceCentralBank(currency)
					.closeCustomerAccount(this);
		}
	}

	/*
	 * accessors
	 */

	public BankAccount getBankAccountCentralBankMoneyReserves() {
		return bankAccountCentralBankMoneyReserves;
	}

	public BankAccount getBankAccountCentralBankTransactions() {
		return bankAccountCentralBankTransactions;
	}

	public Map<Currency, BankAccount> getBankAccountsCurrencyTrade() {
		return bankAccountsCurrencyTrade;
	}

	public Map<Currency, PricingBehaviour> getLocalCurrencyPricingBehaviours() {
		return localCurrencyPricingBehaviours;
	}

	public Set<Bond> getIssuedBonds() {
		return issuedBonds;
	}

	public void setBankAccountCentralBankMoneyReserves(
			BankAccount bankAccountCentralBankMoneyReserves) {
		this.bankAccountCentralBankMoneyReserves = bankAccountCentralBankMoneyReserves;
	}

	public void setBankAccountCentralBankTransactions(
			BankAccount bankAccountCentralBankTransactions) {
		this.bankAccountCentralBankTransactions = bankAccountCentralBankTransactions;
	}

	public void setBankAccountsCurrencyTrade(
			Map<Currency, BankAccount> bankAccountsCurrencyTrade) {
		this.bankAccountsCurrencyTrade = bankAccountsCurrencyTrade;
	}

	public void setLocalCurrencyPricingBehaviours(
			Map<Currency, PricingBehaviour> localCurrencyPricingBehaviours) {
		this.localCurrencyPricingBehaviours = localCurrencyPricingBehaviours;
	}

	public void setIssuedBonds(Set<Bond> issuedBonds) {
		this.issuedBonds = issuedBonds;
	}

	/*
	 * assertions
	 */

	@Transient
	protected void assertCurrencyIsOffered(Currency currency) {
		assert (this.primaryCurrency.equals(currency));
	}

	@Transient
	public void assureBankAccountCentralBankMoneyReserves() {
		if (this.isDeconstructed)
			return;

		if (this.bankAccountCentralBankMoneyReserves == null) {
			// initialize bank account at central bank
			this.bankAccountCentralBankMoneyReserves = ApplicationContext
					.getInstance()
					.getAgentFactory()
					.getInstanceCentralBank(this.primaryCurrency)
					.openBankAccount(this, this.primaryCurrency, true,
							"central bank money reserves", TermType.LONG_TERM,
							MoneyType.CENTRALBANK_MONEY);
		}
	}

	@Transient
	public void assureBankAccountCentralBankTransactions() {
		if (this.isDeconstructed)
			return;

		if (this.bankAccountCentralBankTransactions == null) {
			// initialize bank accounts at central banks
			this.bankAccountCentralBankTransactions = ApplicationContext
					.getInstance()
					.getAgentFactory()
					.getInstanceCentralBank(this.primaryCurrency)
					.openBankAccount(this, this.primaryCurrency, true,
							"central bank transactions", TermType.SHORT_TERM,
							MoneyType.DEPOSITS);
		}
	}

	@Transient
	@Override
	public void assureBankAccountTransactions() {
		if (this.isDeconstructed)
			return;

		this.assureSelfCustomerAccount();

		if (this.bankAccountTransactions == null) {
			/*
			 * initialize the banks own bank account and open a customer account
			 * at this new bank, so that this bank can transfer money from its
			 * own bank account
			 */
			this.bankAccountTransactions = this.primaryBank.openBankAccount(
					this, this.primaryCurrency, true, "transactions",
					TermType.SHORT_TERM, MoneyType.DEPOSITS);
		}
	}

	@Transient
	public void assureBankAccountsCurrencyTrade() {
		if (this.isDeconstructed)
			return;

		this.assureSelfCustomerAccount();

		/*
		 * for all currencies
		 */
		for (Currency currency : Currency.values()) {
			// if there is no bank account for this currency, yet
			if (!this.bankAccountsCurrencyTrade.containsKey(currency)) {

				// foreign currency?
				if (!this.primaryCurrency.equals(currency)) {
					CreditBank foreignCurrencyCreditBank = ApplicationContext
							.getInstance().getAgentFactory()
							.getRandomInstanceCreditBank(currency);
					if (foreignCurrencyCreditBank != null) {
						BankAccount bankAccount = foreignCurrencyCreditBank
								.openBankAccount(this, currency, true,
										"currency trade (foreign)",
										TermType.SHORT_TERM, MoneyType.DEPOSITS);
						this.bankAccountsCurrencyTrade.put(currency,
								bankAccount);
					}
				}

				// local currency
				else {
					this.bankAccountsCurrencyTrade.put(this.primaryCurrency,
							CreditBankImpl.this.openBankAccount(this,
									this.primaryCurrency, true,
									"currency trade (local)",
									TermType.SHORT_TERM, MoneyType.DEPOSITS));
				}
			}
		}
	}

	/*
	 * business logic
	 */

	@Transient
	public void closeCustomerAccount(Agent customer) {
		this.assureBankAccountTransactions();

		// each customer bank account ...
		for (BankAccount bankAccount : ApplicationContext.getInstance()
				.getBankAccountDAO().findAll(this, customer)) {
			// transactions bank account can be null when tearing down the
			// simulation; has to be checked every iteration
			if (this.bankAccountTransactions != null) {
				if (bankAccount != this.bankAccountTransactions) {
					// on closing has to be evened up to 0, so that no money is
					// lost in the monetary system
					if (bankAccount.getBalance() >= 0) {
						this.transferMoney(bankAccount,
								this.bankAccountTransactions,
								bankAccount.getBalance(),
								"evening-up of closed bank account");
					} else {
						this.transferMoney(this.bankAccountTransactions,
								bankAccount, -1.0 * bankAccount.getBalance(),
								"evening-up of closed bank account");
					}
				}
				customer.onBankCloseBankAccount(bankAccount);
			}
		}

		// convert profit to dividends
		if (this.bankAccountTransactions != null) {
			this.transferBankAccountBalanceToDividendBankAccount(this.bankAccountTransactions);
		}

		ApplicationContext.getInstance().getBankAccountDAO()
				.deleteAllBankAccounts(this, customer);
	}

	@Transient
	public void deposit(BankAccount bankAccount, double amount) {

		this.assertBankAccountIsManagedByThisBank(bankAccount);
		assert (amount >= 0.0);

		bankAccount.deposit(amount);
	}

	@Transient
	public void depositCash(Agent client, BankAccount to, double amount,
			Currency currency) {
		this.assertIsCustomerOfThisBank(client);
		this.assertBankAccountIsManagedByThisBank(to);
		this.assertCurrencyIsOffered(currency);

		// transfer money
		ApplicationContext.getInstance().getHardCashRegister()
				.decrement(client, currency, amount);
		to.deposit(amount);
	}

	@Transient
	private double getSumOfBorrowings(Currency currency) {
		double sumOfBorrowings = 0;
		for (BankAccount creditBankAccount : ApplicationContext.getInstance()
				.getBankAccountDAO()
				.findAllBankAccountsManagedByBank(CreditBankImpl.this)) {
			if (creditBankAccount.getCurrency() == currency)
				if (creditBankAccount.getBalance() > 0.0)
					sumOfBorrowings += creditBankAccount.getBalance();
		}
		return sumOfBorrowings;
	}

	@Override
	@Transient
	protected BalanceSheetDTO issueBalanceSheet() {
		this.assureBankAccountCentralBankTransactions();
		this.assureBankAccountCentralBankMoneyReserves();
		this.assureBankAccountsCurrencyTrade();
		this.assureBankAccountBondLoan();

		final BalanceSheetDTO balanceSheet = super.issueBalanceSheet();

		// deposits owned by this credit bank in the central bank for
		// transactions
		balanceSheet
				.addBankAccountBalance(this.bankAccountCentralBankTransactions);

		// deposits owned by this credit bank in the central bank for money
		// reserves
		balanceSheet
				.addBankAccountBalance(this.bankAccountCentralBankMoneyReserves);

		// balances of foreign currency bank accounts
		for (Entry<Currency, BankAccount> bankAccountEntry : this.bankAccountsCurrencyTrade
				.entrySet()) {
			double priceOfForeignCurrencyInLocalCurrency = ApplicationContext
					.getInstance().getMarketFactory().getMarket()
					.getPrice(primaryCurrency, bankAccountEntry.getKey());
			if (!Double.isNaN(priceOfForeignCurrencyInLocalCurrency)) {
				double valueOfForeignCurrencyInLocalCurrency = bankAccountEntry
						.getValue().getBalance()
						* priceOfForeignCurrencyInLocalCurrency;
				balanceSheet.cashForeignCurrency += valueOfForeignCurrencyInLocalCurrency;
			}
		}

		// --------------

		// list issued bonds on balance sheet
		for (Bond bond : this.issuedBonds) {
			if (!bond.isDeconstructed() && !bond.getOwner().equals(this)) {
				balanceSheet.financialLiabilities += bond.getFaceValue();
			}
		}

		// remove deconstructed bonds
		final Set<Bond> bondsToDelete = new HashSet<Bond>();
		for (Bond bond : this.issuedBonds) {
			if (bond.isDeconstructed()) {
				bondsToDelete.add(bond);
			}
		}
		this.issuedBonds.removeAll(bondsToDelete);

		return balanceSheet;
	}

	@Override
	@Transient
	public void onBankCloseBankAccount(final BankAccount bankAccount) {
		if (this.bankAccountCentralBankTransactions != null
				&& this.bankAccountCentralBankTransactions == bankAccount) {
			this.bankAccountCentralBankTransactions = null;
		}

		if (this.bankAccountCentralBankMoneyReserves != null
				&& this.bankAccountCentralBankMoneyReserves == bankAccount) {
			this.bankAccountCentralBankMoneyReserves = null;
		}

		if (this.bankAccountsCurrencyTrade != null) {
			for (Entry<Currency, BankAccount> entry : new HashMap<Currency, BankAccount>(
					this.bankAccountsCurrencyTrade).entrySet()) {
				if (entry.getValue() == bankAccount) {
					this.bankAccountsCurrencyTrade.remove(entry.getKey());
				}
			}
		}

		super.onBankCloseBankAccount(bankAccount);
	}

	@Transient
	public void transferMoney(final BankAccount from, final BankAccount to,
			final double amount, final String subject) {
		this.assureBankAccountCentralBankTransactions();
		this.assertIsCustomerOfThisBank(from.getOwner());
		this.assertBankAccountIsManagedByThisBank(from);

		assert (amount >= 0.0);
		assert (from.getCurrency().equals(to.getCurrency()));
		assert (from.getBalance() >= amount || from.getOverdraftPossible());

		this.assertIdenticalMoneyType(from, to);

		assert (MathUtil.equal(
				this.bankAccountCentralBankTransactions.getBalance(), 0.0));

		// no Exception for identical bank accounts, as this correctly
		// might happen in case of bonds etc.
		if (from != to) {
			getLog().bank_onTransfer(from, to, from.getCurrency(), amount,
					subject);

			final double fromBalanceBefore = from.getBalance();
			final double toBalanceBefore = to.getBalance();

			// is the money flowing internally in this bank?
			if (to.getManagingBank() == this && from.getManagingBank() == this) {
				// transfer money internally
				from.withdraw(amount);
				to.deposit(amount);
			} else { // transfer to another bank
				CentralBank centralBank = ApplicationContext.getInstance()
						.getAgentFactory()
						.getInstanceCentralBank(from.getCurrency());

				// transfer money to central bank account of this bank
				centralBank.transferMoney(from,
						this.bankAccountCentralBankTransactions, amount,
						subject);

				// transfer money from central bank account of this bank to bank
				// account at target bank
				centralBank.transferMoney(
						this.bankAccountCentralBankTransactions, to, amount,
						subject);
			}

			assert (MathUtil.equal(
					this.bankAccountCentralBankTransactions.getBalance(), 0.0));
			assert (fromBalanceBefore - amount == from.getBalance());
			assert (toBalanceBefore + amount == to.getBalance());
		}
	}

	@Transient
	public void withdraw(BankAccount bankAccount, double amount) {

		this.assertBankAccountIsManagedByThisBank(bankAccount);
		assert (amount >= 0.0);

		bankAccount.withdraw(amount);
	}

	@Transient
	public double withdrawCash(Agent client, BankAccount from, double amount,
			Currency currency) {
		this.assertIsCustomerOfThisBank(client);
		this.assertBankAccountIsManagedByThisBank(from);
		this.assertCurrencyIsOffered(currency);

		// transfer money
		from.withdraw(amount);
		return ApplicationContext.getInstance().getHardCashRegister()
				.increment(client, currency, amount);
	}

	protected class SettlementMarketEvent implements ISettlementEvent {
		@Override
		public void onEvent(GoodType goodType, double amount,
				double pricePerUnit, Currency currency) {
		}

		@Override
		public void onEvent(Currency commodityCurrency, double amount,
				double pricePerUnit, Currency currency) {
			if (CreditBankImpl.this.primaryCurrency.equals(commodityCurrency)) {
				CreditBankImpl.this.localCurrencyPricingBehaviours
						.get(currency).registerSelling(amount,
								amount * pricePerUnit);
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
			CreditBankImpl.this.assureBankAccountInterestTransactions();
			CreditBankImpl.this.assureBankAccountCentralBankTransactions();

			final double monthlyInterestRate = MathUtil
					.calculateMonthlyNominalInterestRate(ApplicationContext
							.getInstance()
							.getAgentFactory()
							.getInstanceCentralBank(
									CreditBankImpl.this.primaryCurrency)
							.getEffectiveKeyInterestRate());
			final double dailyInterestRate = monthlyInterestRate / 30.0;

			for (BankAccount bankAccount : ApplicationContext.getInstance()
					.getBankAccountDAO()
					.findAllBankAccountsManagedByBank(CreditBankImpl.this)) {
				if (bankAccount.getOwner() != CreditBankImpl.this) {
					assert (CreditBankImpl.this.primaryCurrency
							.equals(bankAccount.getCurrency()));

					final double dailyInterest = bankAccount.getBalance()
							* dailyInterestRate;

					// liability account & positive interest rate or asset
					// account & negative interest rate
					if (dailyInterest > 0.0) {
						CreditBankImpl.this
								.transferMoney(
										CreditBankImpl.this.bankAccountInterestTransactions,
										bankAccount, dailyInterest,
										"interest earned for customer");
					}
					// asset account & positive interest rate or liability
					// account & negative interest rate
					else if (dailyInterest < 0.0) {
						// credit banks add margin on key interest rate
						final double absMarginDailyInterest = -1.0
								* dailyInterest * 1.5;
						CreditBankImpl.this
								.transferMoney(
										bankAccount,
										CreditBankImpl.this.bankAccountInterestTransactions,
										absMarginDailyInterest,
										"debt interest from customer");
					}
				}
			}
		}
	}

	public class CheckMoneyReservesEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			CreditBankImpl.this.assureBankAccountCentralBankMoneyReserves();

			CentralBank centralBank = ApplicationContext
					.getInstance()
					.getAgentFactory()
					.getInstanceCentralBank(CreditBankImpl.this.primaryCurrency);

			double sumOfBorrowings = CreditBankImpl.this
					.getSumOfBorrowings(CreditBankImpl.this.primaryCurrency);
			double moneyReserveGap = sumOfBorrowings
					* centralBank.getReserveRatio()
					- CreditBankImpl.this.bankAccountCentralBankMoneyReserves
							.getBalance();

			// not enough money deposited at central bank
			if (moneyReserveGap > 0.0) {
				// calculate number of bonds needed to deposit them at
				// central bank for credit

				final List<FixedRateBond> bonds = new ArrayList<FixedRateBond>();
				final BankAccount bankAccountCentralBankMoneyReserves = CreditBankImpl.this.bankAccountCentralBankMoneyReserves;

				/*
				 * issue bond; mega bond that covers complete moneyReserveGap;
				 * no split up per 100 currency units, as one mega bond takes
				 * less memory and CPU performance
				 */
				final FixedRateBond bond = ApplicationContext
						.getInstance()
						.getPropertyFactory()
						.newInstanceFixedRateBond(
								CreditBankImpl.this,
								CreditBankImpl.this.primaryCurrency,
								bankAccountCentralBankMoneyReserves,
								bankAccountCentralBankMoneyReserves,
								moneyReserveGap,
								centralBank.getEffectiveKeyInterestRate() + 0.02);
				bonds.add(bond);

				// obtain tender for bond
				final double balanceBefore = bankAccountCentralBankMoneyReserves
						.getBalance();
				centralBank.obtainTender(bankAccountCentralBankMoneyReserves,
						bonds);

				assert (balanceBefore + moneyReserveGap == bankAccountCentralBankMoneyReserves
						.getBalance());

				// remember issued bonds for balance sheet event
				CreditBankImpl.this.issuedBonds.addAll(bonds);
			}
		}
	}

	public class CurrencyTradeEvent implements ITimeSystemEvent {

		@Override
		public void onEvent() {
			CreditBankImpl.this.assureBankAccountsCurrencyTrade();

			// the primary currency is one of the keys of this collection of
			// bank accounts -> -1
			int numberOfForeignCurrencies = CreditBankImpl.this.bankAccountsCurrencyTrade
					.keySet().size() - 1;
			if (numberOfForeignCurrencies > 0.0) {
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
			double priceOfFirstCurrencyInSecondCurrency = ApplicationContext
					.getInstance().getMarketFactory().getMarket()
					.getPrice(secondCurrency, firstCurrency);
			// e.g. EUR_in_USD = 0.8
			double priceOfSecondCurrencyInFirstCurrency = ApplicationContext
					.getInstance().getMarketFactory().getMarket()
					.getPrice(firstCurrency, secondCurrency);

			if (Double.isNaN(priceOfSecondCurrencyInFirstCurrency)) {
				return priceOfFirstCurrencyInSecondCurrency;
			} else if (Double.isNaN(priceOfFirstCurrencyInSecondCurrency)) {
				return Double.NaN;
			} else {
				// inverse_EUR_in_USD -> correct_USD_in_EUR = 1.25
				double correctPriceOfFirstCurrencyInSecondCurrency = 1.0 / priceOfSecondCurrencyInFirstCurrency;

				if (getLog().isAgentSelectedByClient(CreditBankImpl.this))
					getLog().log(
							CreditBankImpl.this,
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
			int numberOfForeignCurrencies = CreditBankImpl.this.bankAccountsCurrencyTrade
					.keySet().size() - 1;
			if (numberOfForeignCurrencies > 0) {

				double budgetForCurrencyTradingPerCurrency_InPrimaryCurrency = calculateLocalCurrencyBudgetForCurrencyTrading()
						/ (double) numberOfForeignCurrencies;

				/*
				 * arbitrage on exchange markets
				 */
				for (Entry<Currency, BankAccount> entry : CreditBankImpl.this.bankAccountsCurrencyTrade
						.entrySet()) {

					BankAccount localCurrencyTradeBankAccount = CreditBankImpl.this.bankAccountsCurrencyTrade
							.get(CreditBankImpl.this.primaryCurrency);

					/*
					 * trade between local and foreign currencies
					 */
					if (!CreditBankImpl.this.primaryCurrency.equals(entry
							.getKey())) {
						Currency foreignCurrency = entry.getKey();
						BankAccount foreignCurrencyBankAccount = entry
								.getValue();

						double realPriceOfForeignCurrencyInLocalCurrency = ApplicationContext
								.getInstance().getMarketFactory().getMarket()
								.getPrice(primaryCurrency, foreignCurrency);
						double correctPriceOfForeignCurrencyInLocalCurrency = calculateCalculatoryPriceOfFirstCurrencyInSecondCurrency(
								foreignCurrency, primaryCurrency);

						if (MathUtil
								.lesserEqual(
										budgetForCurrencyTradingPerCurrency_InPrimaryCurrency,
										0)) {
							if (getLog().isAgentSelectedByClient(
									CreditBankImpl.this))
								getLog().log(
										CreditBankImpl.this,
										CurrencyTradeEvent.class,
										"-> no arbitrage with "
												+ foreignCurrency
														.getIso4217Code()
												+ ", since budgetForCurrencyTrading is "
												+ MathUtil
														.round(budgetForCurrencyTradingPerCurrency_InPrimaryCurrency));
						} else if (Double
								.isNaN(correctPriceOfForeignCurrencyInLocalCurrency)) {
							getLog().log(
									CreditBankImpl.this,
									CurrencyTradeEvent.class,
									"-> no arbitrage with "
											+ foreignCurrency.getIso4217Code()
											+ ", since correct price of foreign currency is "
											+ correctPriceOfForeignCurrencyInLocalCurrency);
						} else if (MathUtil
								.lesser(correctPriceOfForeignCurrencyInLocalCurrency
										/ (1.0 + ApplicationContext
												.getInstance()
												.getConfiguration().creditBankConfig
												.getMinArbitrageMargin()),
										realPriceOfForeignCurrencyInLocalCurrency)) {
							if (getLog().isAgentSelectedByClient(
									CreditBankImpl.this))
								getLog().log(
										CreditBankImpl.this,
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
							ApplicationContext
									.getInstance()
									.getMarketFactory()
									.getMarket()
									.buy(foreignCurrency,
											Double.NaN,
											budgetForCurrencyTradingPerCurrency_InPrimaryCurrency,
											correctPriceOfForeignCurrencyInLocalCurrency
													/ (1.0 + ApplicationContext
															.getInstance()
															.getConfiguration().creditBankConfig
															.getMinArbitrageMargin()),
											CreditBankImpl.this,
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
			for (BankAccount foreignCurrencyBankAccount : CreditBankImpl.this.bankAccountsCurrencyTrade
					.values()) {
				if (!CreditBankImpl.this.primaryCurrency
						.equals(foreignCurrencyBankAccount.getCurrency())) {
					/*
					 * buy local currency for foreign currency
					 */

					Currency localCurrency = CreditBankImpl.this.primaryCurrency;
					BankAccount localCurrencyBankAccount = CreditBankImpl.this.bankAccountsCurrencyTrade
							.get(localCurrency);

					if (MathUtil.greater(
							foreignCurrencyBankAccount.getBalance(), 0)) {
						// buy local currency for foreign currency
						ApplicationContext
								.getInstance()
								.getMarketFactory()
								.getMarket()
								.buy(localCurrency,
										Double.NaN,
										foreignCurrencyBankAccount.getBalance(),
										Double.NaN, CreditBankImpl.this,
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
			for (PricingBehaviour pricingBehaviour : CreditBankImpl.this.localCurrencyPricingBehaviours
					.values()) {
				pricingBehaviour.nextPeriod();
			}

			/*
			 * offer local currency on exchange markets, denominated in foreign
			 * currency
			 */

			double totalLocalCurrencyBudgetForCurrencyTrading = calculateLocalCurrencyBudgetForCurrencyTrading();

			// if there is no budget in local currency left for offering
			// against foreign currency
			if (MathUtil.lesserEqual(
					totalLocalCurrencyBudgetForCurrencyTrading, 0)) {
				if (getLog().isAgentSelectedByClient(CreditBankImpl.this))
					getLog().log(
							CreditBankImpl.this,
							CurrencyTradeEvent.class,
							"not offering "
									+ CreditBankImpl.this.primaryCurrency
											.getIso4217Code()
									+ " for foreign currencies, as budget / amount to offer is "
									+ Currency
											.formatMoneySum(totalLocalCurrencyBudgetForCurrencyTrading)
									+ " "
									+ CreditBankImpl.this.primaryCurrency
											.getIso4217Code());
			} else {
				int numberOfForeignCurrencies = CreditBankImpl.this.bankAccountsCurrencyTrade
						.keySet().size() - 1;
				double partialLocalCurrencyBudgetForCurrency = totalLocalCurrencyBudgetForCurrencyTrading
						/ (double) numberOfForeignCurrencies;
				// for each foreign currency bank account / foreign currency
				for (BankAccount foreignCurrencyBankAccount : CreditBankImpl.this.bankAccountsCurrencyTrade
						.values()) {
					if (!CreditBankImpl.this.primaryCurrency
							.equals(foreignCurrencyBankAccount.getCurrency())) {
						/*
						 * offer local currency for foreign currency
						 */
						Currency localCurrency = CreditBankImpl.this.primaryCurrency;
						Currency foreignCurrency = foreignCurrencyBankAccount
								.getCurrency();
						BankAccount localCurrencyBankAccount = CreditBankImpl.this.bankAccountsCurrencyTrade
								.get(localCurrency);
						PricingBehaviour pricingBehaviour = CreditBankImpl.this.localCurrencyPricingBehaviours
								.get(foreignCurrency);

						// calculate exchange rate
						double pricingBehaviourPriceOfLocalCurrencyInForeignCurrency = pricingBehaviour
								.getCurrentPrice();

						if (Double
								.isNaN(pricingBehaviourPriceOfLocalCurrencyInForeignCurrency)) {
							pricingBehaviourPriceOfLocalCurrencyInForeignCurrency = ApplicationContext
									.getInstance().getMarketFactory()
									.getMarket()
									.getPrice(foreignCurrency, localCurrency);
							if (getLog().isAgentSelectedByClient(
									CreditBankImpl.this))
								getLog().log(
										CreditBankImpl.this,
										CurrencyTradeEvent.class,
										"could not calculate price for "
												+ localCurrency + " in "
												+ foreignCurrency
												+ " -> using market price");
						}

						// remove existing offers
						ApplicationContext
								.getInstance()
								.getMarketFactory()
								.getMarket()
								.removeAllSellingOffers(CreditBankImpl.this,
										foreignCurrency, localCurrency);

						// offer money amount on the market
						ApplicationContext
								.getInstance()
								.getMarketFactory()
								.getMarket()
								.placeSettlementSellingOffer(
										localCurrency,
										CreditBankImpl.this,
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
			for (BankAccount foreignCurrencyBankAccount : CreditBankImpl.this.bankAccountsCurrencyTrade
					.values()) {
				if (!CreditBankImpl.this.primaryCurrency
						.equals(foreignCurrencyBankAccount.getCurrency())) {
					/*
					 * offer foreign currency for local currency
					 */
					Currency localCurrency = CreditBankImpl.this.primaryCurrency;
					Currency foreignCurrency = foreignCurrencyBankAccount
							.getCurrency();
					BankAccount localCurrencyBankAccount = CreditBankImpl.this.bankAccountsCurrencyTrade
							.get(localCurrency);

					// determine price of foreign currency
					double priceOfForeignCurrencyInLocalCurrency = ApplicationContext
							.getInstance().getMarketFactory().getMarket()
							.getPrice(localCurrency, foreignCurrency);
					if (Double.isNaN(priceOfForeignCurrencyInLocalCurrency))
						priceOfForeignCurrencyInLocalCurrency = 1.0;

					// remove existing offers
					ApplicationContext
							.getInstance()
							.getMarketFactory()
							.getMarket()
							.removeAllSellingOffers(CreditBankImpl.this,
									localCurrency, foreignCurrency);

					// offer money amount on the market
					ApplicationContext
							.getInstance()
							.getMarketFactory()
							.getMarket()
							.placeSettlementSellingOffer(
									foreignCurrency,
									CreditBankImpl.this,
									localCurrencyBankAccount,
									foreignCurrencyBankAccount.getBalance(),
									priceOfForeignCurrencyInLocalCurrency / 1.001,
									foreignCurrencyBankAccount, null);
				}
			}
		}

		private double calculateLocalCurrencyBudgetForCurrencyTrading() {
			CreditBankImpl.this.assureBankAccountsCurrencyTrade();

			// division by 2 so that the period-wise the budget converges to max
			// credit. This ensures, that in each period there is budget left to
			// quote on the currency markets
			return (ApplicationContext.getInstance().getConfiguration().creditBankConfig
					.getMaxCreditForCurrencyTrading() + CreditBankImpl.this.bankAccountsCurrencyTrade
					.get(CreditBankImpl.this.primaryCurrency).getBalance()) / 2.0;
		}
	}

	public class BondsTradingEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			CreditBankImpl.this.assureBankAccountBondLoan();
			CreditBankImpl.this.assureBankAccountInterestTransactions();

			/*
			 * the credit bank is doing fractional reserve banking -> buy bonds
			 * for passive bank accounts
			 */

			final double balanceSumOfPassiveBankAccounts = calculateBalanceSumOfPassiveSavingBankAccounts();

			final double faceValueSumOfBonds = calculateFaceValueSumOfOwnedBonds();

			// TODO money reserves; Basel 3
			final double difference = balanceSumOfPassiveBankAccounts
					- faceValueSumOfBonds;

			if (getLog().isAgentSelectedByClient(CreditBankImpl.this))
				getLog().log(
						CreditBankImpl.this,
						BondsTradingEvent.class,
						"sumOfPassiveBankAccounts = "
								+ Currency
										.formatMoneySum(balanceSumOfPassiveBankAccounts)
								+ " "
								+ CreditBankImpl.this.primaryCurrency
										.getIso4217Code()
								+ "; faceValueSumOfBonds = "
								+ Currency.formatMoneySum(faceValueSumOfBonds)
								+ " "
								+ CreditBankImpl.this.primaryCurrency
										.getIso4217Code()
								+ " => difference = "
								+ Currency.formatMoneySum(difference)
								+ " "
								+ CreditBankImpl.this.primaryCurrency
										.getIso4217Code());

			if (MathUtil.greater(difference, 0.0)) {
				final double balanceBeforeTransaction = CreditBankImpl.this.bankAccountBondLoan
						.getBalance();
				final FixedRateBond fixedRateBond = ApplicationContext
						.getInstance()
						.getStateDAO()
						.findByCurrency(CreditBankImpl.this.primaryCurrency)
						.obtainBond(difference,
								CreditBankImpl.this.bankAccountBondLoan);
				assert (fixedRateBond.getOwner() == CreditBankImpl.this);
				fixedRateBond
						.setFaceValueToBankAccount(CreditBankImpl.this.bankAccountBondLoan);
				fixedRateBond
						.setCouponToBankAccount(CreditBankImpl.this.bankAccountInterestTransactions);

				assert (balanceBeforeTransaction - difference == CreditBankImpl.this.bankAccountBondLoan
						.getBalance());

				// ApplicationContext.getInstance().getMarketFactory().getInstance().buy(FixedRateBond.class,
				// Double.NaN, difference, Double.NaN, CreditBank.this,
				// CreditBank.this.transactionsBankAccount);
			}
		}

		protected double calculateBalanceSumOfPassiveSavingBankAccounts() {
			// bank accounts of non-banks managed by this bank
			double balanceSumOfPassiveBankAccounts = 0.0;

			for (BankAccount bankAccount : ApplicationContext.getInstance()
					.getBankAccountDAO()
					.findAllBankAccountsManagedByBank(CreditBankImpl.this)) {

				assert (bankAccount.getCurrency()
						.equals(CreditBankImpl.this.primaryCurrency));

				if (bankAccount.getBalance() > 0.0
						&& TermType.LONG_TERM.equals(bankAccount.getTermType())) { // passive
																					// account
					// temporary assertion; TODO remove
					assert (bankAccount.getOwner() instanceof Household);
					balanceSumOfPassiveBankAccounts += bankAccount.getBalance();
				}
			}

			assert (balanceSumOfPassiveBankAccounts == 0.0 || ApplicationContext
					.getInstance().getConfiguration().householdConfig
					.getRetirementSaving());
			return balanceSumOfPassiveBankAccounts;
		}

		protected double calculateFaceValueSumOfOwnedBonds() {
			// bonds bought from other agents
			double faceValueSumOfBonds = 0.0;

			for (Property property : ApplicationContext.getInstance()
					.getPropertyRegister()
					.getProperties(CreditBankImpl.this, FixedRateBond.class)) {
				assert (property instanceof FixedRateBond);
				FixedRateBond bond = (FixedRateBond) property;
				assert (bond.getOwner() == CreditBankImpl.this);

				// if the bond is not issued by this bank -> is not an unsold
				// bond
				if (!bond.isDeconstructed()
						&& bond.getIssuer() != CreditBankImpl.this) {
					// currently only state bonds are bought by credit banks;
					// TODO can and should be modified
					assert (bond.getFaceValueFromBankAccount().getOwner() instanceof State);
					faceValueSumOfBonds += ((FixedRateBond) property)
							.getFaceValue();
				}
			}

			assert (faceValueSumOfBonds == 0.0 || ApplicationContext
					.getInstance().getConfiguration().householdConfig
					.getRetirementSaving());
			return faceValueSumOfBonds;
		}
	}
}