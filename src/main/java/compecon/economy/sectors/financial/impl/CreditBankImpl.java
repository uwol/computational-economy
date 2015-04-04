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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import compecon.economy.behaviour.PricingBehaviour;
import compecon.economy.behaviour.impl.PricingBehaviourImpl;
import compecon.economy.bookkeeping.impl.BalanceSheetDTO;
import compecon.economy.materia.GoodType;
import compecon.economy.property.Property;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.sectors.financial.BankCustomer;
import compecon.economy.sectors.financial.CentralBank;
import compecon.economy.sectors.financial.CentralBankCustomer;
import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.economy.sectors.state.State;
import compecon.economy.security.debt.FixedRateBond;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.timesystem.TimeSystemEvent;
import compecon.engine.timesystem.impl.DayType;
import compecon.engine.timesystem.impl.HourType;
import compecon.engine.timesystem.impl.MonthType;
import compecon.math.util.MathUtil;

/**
 * Agent type credit bank manages bank accounts, creates money by credit and
 * follows minimum reserve requirements of central banks.
 */
@Entity
public class CreditBankImpl extends BankImpl implements CreditBank,
		CentralBankCustomer {

	public class BondsTradingEvent implements TimeSystemEvent {
		protected double calculateBalanceSumOfPassiveSavingBankAccounts() {
			// bank accounts of non-banks managed by this bank
			double balanceSumOfPassiveBankAccounts = 0.0;

			for (final BankAccount bankAccount : ApplicationContext
					.getInstance().getBankAccountDAO()
					.findAllBankAccountsManagedByBank(CreditBankImpl.this)) {

				assert (bankAccount.getCurrency()
						.equals(CreditBankImpl.this.primaryCurrency));

				if (bankAccount.getBalance() > 0.0
						&& TermType.LONG_TERM.equals(bankAccount.getTermType())) { // passive
																					// account
					// temporary assertion
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

			for (final Property property : ApplicationContext
					.getInstance()
					.getPropertyService()
					.findAllPropertiesOfPropertyOwner(CreditBankImpl.this,
							FixedRateBond.class)) {
				assert (property instanceof FixedRateBond);
				final FixedRateBond bond = (FixedRateBond) property;
				assert (bond.getOwner() == CreditBankImpl.this);

				// if the bond is not issued by this bank -> is not an unsold
				// bond
				if (!bond.isDeconstructed()
						&& bond.getIssuer() != CreditBankImpl.this) {
					// currently only state bonds are bought by credit banks;
					// TODO can and should be modified
					assert (bond.getIssuer() instanceof State);
					faceValueSumOfBonds += ((FixedRateBond) property)
							.getFaceValue();
				}
			}

			assert (faceValueSumOfBonds == 0.0 || ApplicationContext
					.getInstance().getConfiguration().householdConfig
					.getRetirementSaving());
			return faceValueSumOfBonds;
		}

		@Override
		public boolean isDeconstructed() {
			return CreditBankImpl.this.isDeconstructed;
		}

		@Override
		public void onEvent() {
			assureBankAccountBondLoan();
			assureBankAccountInterestTransactions();

			/*
			 * transfer received coupons to dividends bank account, so that
			 * money is not hoarded
			 */
			transferBankAccountBalanceToDividendBankAccount(bankAccountInterestTransactions);

			/*
			 * the credit bank is doing fractional reserve banking -> buy bonds
			 * for passive bank accounts
			 */

			final double balanceSumOfPassiveBankAccounts = calculateBalanceSumOfPassiveSavingBankAccounts();

			final double faceValueSumOfBonds = calculateFaceValueSumOfOwnedBonds();

			// TODO money reserves; Basel 3
			final double difference = balanceSumOfPassiveBankAccounts
					- faceValueSumOfBonds;

			if (getLog().isAgentSelectedByClient(CreditBankImpl.this)) {
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
			}

			if (MathUtil.greater(difference, 0.0)) {
				final double balanceBeforeTransaction = CreditBankImpl.this.bankAccountBondLoan
						.getBalance();
				final FixedRateBond fixedRateBond = ApplicationContext
						.getInstance()
						.getAgentService()
						.findState(CreditBankImpl.this.primaryCurrency)
						.obtainBond(difference, CreditBankImpl.this,
								getBankAccountBondLoanDelegate());
				assert (fixedRateBond.getOwner() == CreditBankImpl.this);

				fixedRateBond
						.setFaceValueToBankAccountDelegate(getBankAccountBondLoanDelegate());
				fixedRateBond
						.setCouponToBankAccountDelegate(getBankAccountInterestTransactionsDelegate());

				assert (balanceBeforeTransaction - difference == CreditBankImpl.this.bankAccountBondLoan
						.getBalance());

				// ApplicationContext.getInstance().getMarketService().getInstance().buy(FixedRateBond.class,
				// Double.NaN, difference, Double.NaN, CreditBank.this,
				// CreditBank.this.transactionsBankAccount);
			}
		}
	}

	public class CheckMoneyReservesEvent implements TimeSystemEvent {
		@Override
		public boolean isDeconstructed() {
			return CreditBankImpl.this.isDeconstructed;
		}

		@Override
		public void onEvent() {
			assureBankAccountCentralBankMoneyReserves();

			final CentralBank centralBank = ApplicationContext.getInstance()
					.getAgentService()
					.findCentralBank(CreditBankImpl.this.primaryCurrency);

			final double sumOfBorrowings = getSumOfBorrowings(CreditBankImpl.this.primaryCurrency);
			final double moneyReserveGap = sumOfBorrowings
					* centralBank.getReserveRatio()
					- bankAccountCentralBankMoneyReserves.getBalance();

			// not enough money deposited at central bank
			if (moneyReserveGap > 0.0) {
				// calculate number of bonds needed to deposit them at
				// central bank for credit

				/*
				 * issue bond; mega bond that covers complete moneyReserveGap;
				 * no split up per 100 currency units, as one mega bond takes
				 * less memory and CPU performance
				 */
				final FixedRateBond bond = ApplicationContext
						.getInstance()
						.getFixedRateBondFactory()
						.newInstanceFixedRateBond(
								CreditBankImpl.this,
								CreditBankImpl.this,
								CreditBankImpl.this.primaryCurrency,
								getBankAccountCentralBankMoneyReservesDelegate(),
								getBankAccountCentralBankMoneyReservesDelegate(),
								moneyReserveGap,
								centralBank.getEffectiveKeyInterestRate() + 0.02);

				final List<FixedRateBond> bonds = new ArrayList<FixedRateBond>();
				bonds.add(bond);

				// obtain tender for bond
				final double balanceBefore = bankAccountCentralBankMoneyReserves
						.getBalance();
				centralBank.obtainTender(bankAccountCentralBankMoneyReserves,
						bonds);

				assert (balanceBefore + moneyReserveGap == bankAccountCentralBankMoneyReserves
						.getBalance());
			}
		}
	}

	public class CurrencyTradeEvent implements TimeSystemEvent {
		protected void buyForeignCurrencyForArbitrage() {
			final int numberOfForeignCurrencies = bankAccountsCurrencyTrade
					.keySet().size() - 1;
			if (numberOfForeignCurrencies > 0) {

				final double budgetForCurrencyTradingPerCurrency_InPrimaryCurrency = calculateLocalCurrencyBudgetForCurrencyTrading()
						/ numberOfForeignCurrencies;

				/*
				 * arbitrage on exchange markets
				 */
				for (final Currency currency : bankAccountsCurrencyTrade
						.keySet()) {

					/*
					 * trade between local and foreign currencies
					 */
					if (!CreditBankImpl.this.primaryCurrency.equals(currency)) {
						final Currency foreignCurrency = currency;

						final double realPriceOfForeignCurrencyInLocalCurrency = ApplicationContext
								.getInstance()
								.getMarketService()
								.getMarginalMarketPrice(primaryCurrency,
										foreignCurrency);
						final double correctPriceOfForeignCurrencyInLocalCurrency = calculateCalculatoryPriceOfFirstCurrencyInSecondCurrency(
								foreignCurrency, primaryCurrency);

						if (MathUtil
								.lesserEqual(
										budgetForCurrencyTradingPerCurrency_InPrimaryCurrency,
										0)) {
							if (getLog().isAgentSelectedByClient(
									CreditBankImpl.this)) {
								getLog().log(
										CreditBankImpl.this,
										CurrencyTradeEvent.class,
										"-> no arbitrage with "
												+ foreignCurrency
														.getIso4217Code()
												+ ", since budgetForCurrencyTrading is "
												+ MathUtil
														.round(budgetForCurrencyTradingPerCurrency_InPrimaryCurrency));
							}
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
									CreditBankImpl.this)) {
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
							}
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
									.getMarketService()
									.buy(foreignCurrency,
											Double.NaN,
											budgetForCurrencyTradingPerCurrency_InPrimaryCurrency,
											correctPriceOfForeignCurrencyInLocalCurrency
													/ (1.0 + ApplicationContext
															.getInstance()
															.getConfiguration().creditBankConfig
															.getMinArbitrageMargin()),
											CreditBankImpl.this,
											getBankAccountCurrencyTradeDelegate(CreditBankImpl.this.primaryCurrency),
											getBankAccountCurrencyTradeDelegate(foreignCurrency));
						}
					}
				}
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
				final Currency firstCurrency, final Currency secondCurrency) {
			// e.g. USD_in_EUR = 0.8
			final double priceOfFirstCurrencyInSecondCurrency = ApplicationContext
					.getInstance().getMarketService()
					.getMarginalMarketPrice(secondCurrency, firstCurrency);
			// e.g. EUR_in_USD = 0.8
			final double priceOfSecondCurrencyInFirstCurrency = ApplicationContext
					.getInstance().getMarketService()
					.getMarginalMarketPrice(firstCurrency, secondCurrency);

			if (Double.isNaN(priceOfSecondCurrencyInFirstCurrency)) {
				return priceOfFirstCurrencyInSecondCurrency;
			} else if (Double.isNaN(priceOfFirstCurrencyInSecondCurrency)) {
				return Double.NaN;
			} else {
				// inverse_EUR_in_USD -> correct_USD_in_EUR = 1.25
				final double correctPriceOfFirstCurrencyInSecondCurrency = 1.0 / priceOfSecondCurrencyInFirstCurrency;

				if (getLog().isAgentSelectedByClient(CreditBankImpl.this)) {
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
				}
				return correctPriceOfFirstCurrencyInSecondCurrency;
			}
		}

		private double calculateLocalCurrencyBudgetForCurrencyTrading() {
			assureBankAccountsCurrencyTrade();

			// division by 2 so that the period-wise the budget converges to max
			// credit. This ensures, that in each period there is budget left to
			// quote on the currency markets
			return (ApplicationContext.getInstance().getConfiguration().creditBankConfig
					.getMaxCreditForCurrencyTrading() + bankAccountsCurrencyTrade
					.get(CreditBankImpl.this.primaryCurrency).getBalance()) / 2.0;
		}

		@Override
		public boolean isDeconstructed() {
			return CreditBankImpl.this.isDeconstructed;
		}

		protected void offerForeignCurrencies() {
			// for each foreign currency bank account / foreign currency
			for (final BankAccount foreignCurrencyBankAccount : bankAccountsCurrencyTrade
					.values()) {
				if (!CreditBankImpl.this.primaryCurrency
						.equals(foreignCurrencyBankAccount.getCurrency())) {
					/*
					 * offer foreign currency for local currency
					 */
					final Currency localCurrency = CreditBankImpl.this.primaryCurrency;
					final Currency foreignCurrency = foreignCurrencyBankAccount
							.getCurrency();

					// determine price of foreign currency
					double priceOfForeignCurrencyInLocalCurrency = ApplicationContext
							.getInstance()
							.getMarketService()
							.getMarginalMarketPrice(localCurrency,
									foreignCurrency);
					if (Double.isNaN(priceOfForeignCurrencyInLocalCurrency)) {
						priceOfForeignCurrencyInLocalCurrency = 1.0;
					}

					// remove existing offers
					ApplicationContext
							.getInstance()
							.getMarketService()
							.removeAllSellingOffers(CreditBankImpl.this,
									localCurrency, foreignCurrency);

					// offer money amount on the market
					ApplicationContext
							.getInstance()
							.getMarketService()
							.placeSellingOffer(
									foreignCurrency,
									CreditBankImpl.this,
									getBankAccountCurrencyTradeDelegate(localCurrency),
									foreignCurrencyBankAccount.getBalance(),
									priceOfForeignCurrencyInLocalCurrency / 1.001,
									getBankAccountCurrencyTradeDelegate(foreignCurrency));
				}
			}
		}

		protected void offerLocalCurrency() {
			/*
			 * prepare pricing behaviours
			 */
			for (final PricingBehaviour pricingBehaviour : localCurrencyPricingBehaviours
					.values()) {
				pricingBehaviour.nextPeriod();
			}

			/*
			 * offer local currency on exchange markets, denominated in foreign
			 * currency
			 */

			final double totalLocalCurrencyBudgetForCurrencyTrading = calculateLocalCurrencyBudgetForCurrencyTrading();

			// if there is no budget in local currency left for offering
			// against foreign currency
			if (MathUtil.lesserEqual(
					totalLocalCurrencyBudgetForCurrencyTrading, 0)) {
				if (getLog().isAgentSelectedByClient(CreditBankImpl.this)) {
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
				}
			} else {
				final int numberOfForeignCurrencies = bankAccountsCurrencyTrade
						.keySet().size() - 1;
				final double partialLocalCurrencyBudgetForCurrency = totalLocalCurrencyBudgetForCurrencyTrading
						/ numberOfForeignCurrencies;
				// for each foreign currency bank account / foreign currency
				for (final BankAccount foreignCurrencyBankAccount : bankAccountsCurrencyTrade
						.values()) {
					if (!CreditBankImpl.this.primaryCurrency
							.equals(foreignCurrencyBankAccount.getCurrency())) {
						/*
						 * offer local currency for foreign currency
						 */
						final Currency localCurrency = CreditBankImpl.this.primaryCurrency;
						final Currency foreignCurrency = foreignCurrencyBankAccount
								.getCurrency();
						final PricingBehaviour pricingBehaviour = localCurrencyPricingBehaviours
								.get(foreignCurrency);

						// calculate exchange rate
						double pricingBehaviourPriceOfLocalCurrencyInForeignCurrency = pricingBehaviour
								.getCurrentPrice();

						if (Double
								.isNaN(pricingBehaviourPriceOfLocalCurrencyInForeignCurrency)) {
							pricingBehaviourPriceOfLocalCurrencyInForeignCurrency = ApplicationContext
									.getInstance()
									.getMarketService()
									.getMarginalMarketPrice(foreignCurrency,
											localCurrency);
							if (getLog().isAgentSelectedByClient(
									CreditBankImpl.this)) {
								getLog().log(
										CreditBankImpl.this,
										CurrencyTradeEvent.class,
										"could not calculate price for "
												+ localCurrency + " in "
												+ foreignCurrency
												+ " -> using market price");
							}
						}

						// remove existing offers
						ApplicationContext
								.getInstance()
								.getMarketService()
								.removeAllSellingOffers(CreditBankImpl.this,
										foreignCurrency, localCurrency);

						// offer money amount on the market
						ApplicationContext
								.getInstance()
								.getMarketService()
								.placeSellingOffer(
										localCurrency,
										CreditBankImpl.this,
										getBankAccountCurrencyTradeDelegate(foreignCurrency),
										partialLocalCurrencyBudgetForCurrency,
										pricingBehaviourPriceOfLocalCurrencyInForeignCurrency,
										getBankAccountCurrencyTradeDelegate(localCurrency));

						pricingBehaviour
								.registerOfferedAmount(partialLocalCurrencyBudgetForCurrency);
					}
				}
			}
		}

		@Override
		public void onEvent() {
			assureBankAccountsCurrencyTrade();

			// the primary currency is one of the keys of this collection of
			// bank accounts -> -1
			final int numberOfForeignCurrencies = bankAccountsCurrencyTrade
					.keySet().size() - 1;
			if (numberOfForeignCurrencies > 0.0) {
				buyForeignCurrencyForArbitrage();
				rebuyLocalCurrency();
				offerLocalCurrency();
				offerForeignCurrencies();
			}
		}

		protected void rebuyLocalCurrency() {
			/*
			 * buy local currency on exchange markets, denominated in foreign
			 * currency
			 */
			// for each foreign currency bank account / foreign currency
			for (final Currency currency : bankAccountsCurrencyTrade.keySet()) {
				if (!CreditBankImpl.this.primaryCurrency.equals(currency)) {
					final Currency localCurrency = CreditBankImpl.this.primaryCurrency;
					final Currency foreignCurrency = currency;
					final double foreignCurrencyBankAccountBalance = getBankAccountCurrencyTradeDelegate(
							foreignCurrency).getBankAccount().getBalance();
					/*
					 * buy local currency for foreign currency
					 */

					if (MathUtil
							.greater(foreignCurrencyBankAccountBalance, 0.0)) {
						// buy local currency for foreign currency
						ApplicationContext
								.getInstance()
								.getMarketService()
								.buy(localCurrency,
										Double.NaN,
										foreignCurrencyBankAccountBalance,
										Double.NaN,
										CreditBankImpl.this,
										getBankAccountCurrencyTradeDelegate(foreignCurrency),
										getBankAccountCurrencyTradeDelegate(localCurrency));
					}
				}
			}
		}
	}

	public class DailyInterestCalculationEvent implements TimeSystemEvent {
		@Override
		public boolean isDeconstructed() {
			return CreditBankImpl.this.isDeconstructed;
		}

		@Override
		public void onEvent() {
			assureBankAccountInterestTransactions();
			assureBankAccountCentralBankTransactions();

			final double monthlyInterestRate = MathUtil
					.calculateMonthlyNominalInterestRate(ApplicationContext
							.getInstance()
							.getAgentService()
							.findCentralBank(
									CreditBankImpl.this.primaryCurrency)
							.getEffectiveKeyInterestRate());
			final double dailyInterestRate = monthlyInterestRate / 30.0;

			for (final BankAccount bankAccount : ApplicationContext
					.getInstance().getBankAccountDAO()
					.findAllBankAccountsManagedByBank(CreditBankImpl.this)) {
				if (bankAccount.getOwner() != CreditBankImpl.this) {
					assert (CreditBankImpl.this.primaryCurrency
							.equals(bankAccount.getCurrency()));

					final double dailyInterest = bankAccount.getBalance()
							* dailyInterestRate;

					// liability account & positive interest rate or asset
					// account & negative interest rate
					if (dailyInterest > 0.0) {
						transferMoney(
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
						transferMoney(
								bankAccount,
								CreditBankImpl.this.bankAccountInterestTransactions,
								absMarginDailyInterest,
								"debt interest from customer");
					}
				}
			}
		}
	}

	/**
	 * bank account for money reserves in the central bank
	 */
	@OneToOne(targetEntity = BankAccountImpl.class)
	@JoinColumn(name = "bankAccountCbMoneyReserves_id")
	@Index(name = "IDX_CB_BA_CBMONEYRESERVES")
	protected BankAccount bankAccountCentralBankMoneyReserves;

	@Transient
	protected final BankAccountDelegate bankAccountCentralBankMoneyReservesDelegate = new BankAccountDelegate() {
		@Override
		public BankAccount getBankAccount() {
			CreditBankImpl.this.assureBankAccountCentralBankMoneyReserves();
			return bankAccountCentralBankMoneyReserves;
		}

		@Override
		public void onTransfer(final double amount) {
		}
	};

	/**
	 * bank account for basic daily transactions with the central bank -> money
	 * transfers to other banks
	 */
	@OneToOne(targetEntity = BankAccountImpl.class)
	@JoinColumn(name = "bankAccountCbTransactions_id")
	@Index(name = "IDX_CB_BA_CBTRANSACTIONS")
	protected BankAccount bankAccountCentralBankTransactions;

	@Transient
	protected final BankAccountDelegate bankAccountCentralBankTransactionsDelegate = new BankAccountDelegate() {
		@Override
		public BankAccount getBankAccount() {
			CreditBankImpl.this.assureBankAccountCentralBankTransactions();
			return bankAccountCentralBankTransactions;
		}

		@Override
		public void onTransfer(final double amount) {
		}
	};

	@OneToMany(targetEntity = BankAccountImpl.class)
	@JoinTable(name = "CreditBank_ForeignCurrencyBankAccounts", joinColumns = @JoinColumn(name = "creditBank_id"), inverseJoinColumns = @JoinColumn(name = "bankAccount_id"))
	@Cascade(value = { CascadeType.DELETE })
	@MapKeyEnumerated
	protected Map<Currency, BankAccount> bankAccountsCurrencyTrade = new HashMap<Currency, BankAccount>();

	/*
	 * accessors
	 */

	@Transient
	protected Map<Currency, BankAccountDelegate> bankAccountsCurrencyTradeDelegate = new HashMap<Currency, BankAccountDelegate>();

	@Transient
	protected Map<Currency, PricingBehaviour> localCurrencyPricingBehaviours = new HashMap<Currency, PricingBehaviour>();

	@Override
	@Transient
	protected void assertCurrencyIsOffered(final Currency currency) {
		assert (primaryCurrency.equals(currency));
	}

	@Transient
	public void assureBankAccountCentralBankMoneyReserves() {
		if (isDeconstructed) {
			return;
		}

		if (bankAccountCentralBankMoneyReserves == null) {
			// initialize bank account at central bank
			bankAccountCentralBankMoneyReserves = ApplicationContext
					.getInstance()
					.getAgentService()
					.findCentralBank(primaryCurrency)
					.openBankAccount(this, primaryCurrency, true,
							"central bank money reserves", TermType.LONG_TERM,
							MoneyType.CENTRALBANK_MONEY);
		}
	}

	@Transient
	public void assureBankAccountCentralBankTransactions() {
		if (isDeconstructed) {
			return;
		}

		if (bankAccountCentralBankTransactions == null) {
			// initialize bank accounts at central banks
			bankAccountCentralBankTransactions = ApplicationContext
					.getInstance()
					.getAgentService()
					.findCentralBank(primaryCurrency)
					.openBankAccount(this, primaryCurrency, true,
							"central bank transactions", TermType.SHORT_TERM,
							MoneyType.DEPOSITS);
		}
	}

	@Transient
	public void assureBankAccountsCurrencyTrade() {
		if (isDeconstructed) {
			return;
		}

		/*
		 * for all currencies
		 */
		for (final Currency currency : Currency.values()) {
			// if there is no bank account for this currency, yet
			if (!bankAccountsCurrencyTrade.containsKey(currency)) {

				// foreign currency?
				if (!primaryCurrency.equals(currency)) {
					final CreditBank foreignCurrencyCreditBank = ApplicationContext
							.getInstance().getAgentService()
							.findRandomCreditBank(currency);
					if (foreignCurrencyCreditBank != null) {
						final BankAccount bankAccount = foreignCurrencyCreditBank
								.openBankAccount(this, currency, true,
										"currency trade (foreign)",
										TermType.SHORT_TERM, MoneyType.DEPOSITS);
						bankAccountsCurrencyTrade.put(currency, bankAccount);
					}
				}

				// local currency
				else {
					bankAccountsCurrencyTrade.put(primaryCurrency,
							CreditBankImpl.this.openBankAccount(this,
									primaryCurrency, true,
									"currency trade (local)",
									TermType.SHORT_TERM, MoneyType.DEPOSITS));
				}
			}
		}
	}

	@Transient
	@Override
	public void assureBankAccountTransactions() {
		if (isDeconstructed) {
			return;
		}

		if (bankAccountTransactions == null) {
			/*
			 * initialize the banks own bank account and open a customer account
			 * at this new bank, so that this bank can transfer money from its
			 * own bank account
			 */
			bankAccountTransactions = getPrimaryBank().openBankAccount(this,
					primaryCurrency, true, "transactions", TermType.SHORT_TERM,
					MoneyType.DEPOSITS);
		}
	}

	@Override
	@Transient
	public void closeCustomerAccount(final BankCustomer customer) {
		assureBankAccountTransactions();

		// for each customer bank account ...
		for (final BankAccount bankAccount : ApplicationContext.getInstance()
				.getBankAccountDAO().findAll(this, customer)) {
			/*
			 * transfer balance
			 */
			// when tearing down the simulation, the transactions bank account
			// might be null, if the credit bank is already deconstructed
			// has to be checked each in iteration
			if (!isDeconstructed && bankAccountTransactions != null) {
				if (bankAccount != bankAccountTransactions) {
					// on closing has to be evened up to 0, so that no money
					// is lost in the monetary system
					if (bankAccount.getBalance() >= 0) {
						transferMoney(bankAccount, bankAccountTransactions,
								bankAccount.getBalance(),
								"evening-up of closed bank account");
					} else {
						transferMoney(bankAccountTransactions, bankAccount,
								-1.0 * bankAccount.getBalance(),
								"evening-up of closed bank account");
					}
				}
			}
			// inform customer
			customer.onBankCloseBankAccount(bankAccount);
		}

		// convert profit to dividends
		if (bankAccountTransactions != null) {
			transferBankAccountBalanceToDividendBankAccount(bankAccountTransactions);
		}

		ApplicationContext.getInstance().getBankAccountFactory()
				.deleteAllBankAccounts(this, customer);
	}

	/*
	 * assertions
	 */

	@Override
	@Transient
	public void deconstruct() {
		super.deconstruct();

		ApplicationContext.getInstance().getCreditBankFactory()
				.deleteCreditBank(this);
	}

	@Override
	@Transient
	public void deposit(final BankAccount bankAccount, final double amount) {

		assertBankAccountIsManagedByThisBank(bankAccount);
		assert (amount >= 0.0);

		bankAccount.deposit(amount);
	}

	@Override
	@Transient
	public void depositCash(final BankCustomer customer, final BankAccount to,
			final double amount, final Currency currency) {
		assertIsCustomerOfThisBank(customer);
		assertBankAccountIsManagedByThisBank(to);
		assertCurrencyIsOffered(currency);

		// transfer money
		ApplicationContext.getInstance().getHardCashService()
				.decrement(customer, currency, amount);
		to.deposit(amount);
	}

	public BankAccount getBankAccountCentralBankMoneyReserves() {
		return bankAccountCentralBankMoneyReserves;
	}

	@Override
	@Transient
	public BankAccountDelegate getBankAccountCentralBankMoneyReservesDelegate() {
		return bankAccountCentralBankMoneyReservesDelegate;
	}

	/*
	 * business logic
	 */

	public BankAccount getBankAccountCentralBankTransactions() {
		return bankAccountCentralBankTransactions;
	}

	@Override
	@Transient
	public BankAccountDelegate getBankAccountCentralBankTransactionsDelegate() {
		return bankAccountCentralBankTransactionsDelegate;
	}

	@Override
	@Transient
	public BankAccountDelegate getBankAccountCurrencyTradeDelegate(
			final Currency currency) {
		return bankAccountsCurrencyTradeDelegate.get(currency);
	}

	public Map<Currency, BankAccount> getBankAccountsCurrencyTrade() {
		return bankAccountsCurrencyTrade;
	}

	public Map<Currency, PricingBehaviour> getLocalCurrencyPricingBehaviours() {
		return localCurrencyPricingBehaviours;
	}

	@Transient
	private double getSumOfBorrowings(final Currency currency) {
		double sumOfBorrowings = 0;
		for (final BankAccount creditBankAccount : ApplicationContext
				.getInstance().getBankAccountDAO()
				.findAllBankAccountsManagedByBank(CreditBankImpl.this)) {
			if (creditBankAccount.getCurrency() == currency) {
				if (creditBankAccount.getBalance() > 0.0) {
					sumOfBorrowings += creditBankAccount.getBalance();
				}
			}
		}
		return sumOfBorrowings;
	}

	@Override
	public void initialize() {
		super.initialize();

		// trade currencies on exchange markets
		final TimeSystemEvent currencyTradeEvent = new CurrencyTradeEvent();
		timeSystemEvents.add(currencyTradeEvent);
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
		final TimeSystemEvent interestCalculationEvent = new DailyInterestCalculationEvent();
		timeSystemEvents.add(interestCalculationEvent);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(interestCalculationEvent, -1, MonthType.EVERY,
						DayType.EVERY, HourType.HOUR_02);

		// check money reserves at the central bank
		final TimeSystemEvent checkMoneyReservesEvent = new CheckMoneyReservesEvent();
		timeSystemEvents.add(checkMoneyReservesEvent);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(checkMoneyReservesEvent, -1, MonthType.EVERY,
						DayType.EVERY, HourType.HOUR_12);

		// bonds trading
		// should happen every hour, so that money flow is distributed over the
		// period, leading to less volatility on markets
		final TimeSystemEvent bondsTradingEvent = new BondsTradingEvent();
		timeSystemEvents.add(bondsTradingEvent);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEventEvery(bondsTradingEvent, -1, MonthType.EVERY,
						DayType.EVERY, HourType.EVERY);

		// pricing behaviours
		for (final Currency foreignCurrency : Currency.values()) {
			if (!primaryCurrency.equals(foreignCurrency)) {
				// price of local currency in foreign currency
				double initialPriceOfLocalCurrencyInForeignCurrency = ApplicationContext
						.getInstance()
						.getMarketService()
						.getMarginalMarketPrice(foreignCurrency,
								primaryCurrency);
				if (Double.isNaN(initialPriceOfLocalCurrencyInForeignCurrency)) {
					initialPriceOfLocalCurrencyInForeignCurrency = 1.0;
				}
				localCurrencyPricingBehaviours.put(
						foreignCurrency,
						new PricingBehaviourImpl(this, primaryCurrency,
								foreignCurrency,
								initialPriceOfLocalCurrencyInForeignCurrency,
								ApplicationContext.getInstance()
										.getConfiguration().creditBankConfig
										.getPriceChangeIncrement()));
			}
		}

		// initialize currency trade bank account delegates
		for (final Currency currency : Currency.values()) {
			final BankAccountDelegate delegate = new BankAccountDelegate() {
				@Override
				public BankAccount getBankAccount() {
					CreditBankImpl.this.assureBankAccountsCurrencyTrade();
					return bankAccountsCurrencyTrade.get(currency);
				}

				@Override
				public void onTransfer(final double amount) {
				}
			};
			bankAccountsCurrencyTradeDelegate.put(currency, delegate);
		}
	}

	@Override
	@Transient
	protected BalanceSheetDTO issueBalanceSheet() {
		assureBankAccountCentralBankTransactions();
		assureBankAccountCentralBankMoneyReserves();
		assureBankAccountsCurrencyTrade();
		assureBankAccountBondLoan();

		final BalanceSheetDTO balanceSheet = super.issueBalanceSheet();

		// deposits owned by this credit bank in the central bank for
		// transactions
		balanceSheet.addBankAccountBalance(bankAccountCentralBankTransactions);

		// deposits owned by this credit bank in the central bank for money
		// reserves
		balanceSheet.addBankAccountBalance(bankAccountCentralBankMoneyReserves);

		// balances of foreign currency bank accounts
		for (final Entry<Currency, BankAccount> bankAccountEntry : bankAccountsCurrencyTrade
				.entrySet()) {
			final double priceOfForeignCurrencyInLocalCurrency = ApplicationContext
					.getInstance()
					.getMarketService()
					.getMarginalMarketPrice(primaryCurrency,
							bankAccountEntry.getKey());
			if (!Double.isNaN(priceOfForeignCurrencyInLocalCurrency)) {
				final double valueOfForeignCurrencyInLocalCurrency = bankAccountEntry
						.getValue().getBalance()
						* priceOfForeignCurrencyInLocalCurrency;
				balanceSheet.cashForeignCurrency += valueOfForeignCurrencyInLocalCurrency;
			}
		}

		return balanceSheet;
	}

	@Override
	@Transient
	public void onBankCloseBankAccount(final BankAccount bankAccount) {
		if (bankAccountCentralBankTransactions != null
				&& bankAccountCentralBankTransactions == bankAccount) {
			bankAccountCentralBankTransactions = null;
		}

		if (bankAccountCentralBankMoneyReserves != null
				&& bankAccountCentralBankMoneyReserves == bankAccount) {
			bankAccountCentralBankMoneyReserves = null;
		}

		if (bankAccountsCurrencyTrade != null) {
			for (final Entry<Currency, BankAccount> entry : new HashMap<Currency, BankAccount>(
					bankAccountsCurrencyTrade).entrySet()) {
				if (entry.getValue() == bankAccount) {
					bankAccountsCurrencyTrade.remove(entry.getKey());
				}
			}
		}

		super.onBankCloseBankAccount(bankAccount);
	}

	@Override
	public void onMarketSettlement(final Currency commodityCurrency,
			final double amount, final double pricePerUnit,
			final Currency currency) {
		// pricing behaviour only for local currency
		if (CreditBankImpl.this.primaryCurrency.equals(commodityCurrency)) {
			CreditBankImpl.this.localCurrencyPricingBehaviours.get(currency)
					.registerSelling(amount, amount * pricePerUnit);
		}
	}

	@Override
	public void onMarketSettlement(final GoodType goodType,
			final double amount, final double pricePerUnit,
			final Currency currency) {
	}

	@Override
	public void onMarketSettlement(final Property property,
			final double totalPrice, final Currency currency) {
	}

	public void setBankAccountCentralBankMoneyReserves(
			final BankAccount bankAccountCentralBankMoneyReserves) {
		this.bankAccountCentralBankMoneyReserves = bankAccountCentralBankMoneyReserves;
	}

	public void setBankAccountCentralBankTransactions(
			final BankAccount bankAccountCentralBankTransactions) {
		this.bankAccountCentralBankTransactions = bankAccountCentralBankTransactions;
	}

	public void setBankAccountsCurrencyTrade(
			final Map<Currency, BankAccount> bankAccountsCurrencyTrade) {
		this.bankAccountsCurrencyTrade = bankAccountsCurrencyTrade;
	}

	public void setLocalCurrencyPricingBehaviours(
			final Map<Currency, PricingBehaviour> localCurrencyPricingBehaviours) {
		this.localCurrencyPricingBehaviours = localCurrencyPricingBehaviours;
	}

	@Override
	@Transient
	public void transferMoney(final BankAccount from, final BankAccount to,
			final double amount, final String subject) {
		assert (!isDeconstructed);

		assureBankAccountCentralBankTransactions();
		assertIsCustomerOfThisBank(from.getOwner());
		assertBankAccountIsManagedByThisBank(from);

		assert (amount >= 0.0);
		assert (from.getCurrency().equals(to.getCurrency()));
		assert (from.getBalance() >= amount || from.getOverdraftPossible());

		assertIdenticalMoneyType(from, to);

		assert (MathUtil.equal(bankAccountCentralBankTransactions.getBalance(),
				0.0));

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
				final CentralBank centralBank = ApplicationContext
						.getInstance().getAgentService()
						.findCentralBank(from.getCurrency());

				// transfer money to central bank account of this bank
				centralBank.transferMoney(from,
						bankAccountCentralBankTransactions, amount, subject);

				// transfer money from central bank account of this bank to bank
				// account at target bank
				centralBank.transferMoney(bankAccountCentralBankTransactions,
						to, amount, subject);
			}

			assert (MathUtil.equal(
					bankAccountCentralBankTransactions.getBalance(), 0.0));
			assert (fromBalanceBefore - amount == from.getBalance());
			assert (toBalanceBefore + amount == to.getBalance());
		}
	}

	@Override
	@Transient
	public void withdraw(final BankAccount bankAccount, final double amount) {

		assertBankAccountIsManagedByThisBank(bankAccount);
		assert (amount >= 0.0);

		bankAccount.withdraw(amount);
	}

	@Override
	@Transient
	public double withdrawCash(final BankCustomer customer,
			final BankAccount from, final double amount, final Currency currency) {
		assertIsCustomerOfThisBank(customer);
		assertBankAccountIsManagedByThisBank(from);
		assertCurrencyIsOffered(currency);

		// transfer money
		from.withdraw(amount);
		return ApplicationContext.getInstance().getHardCashService()
				.increment(customer, currency, amount);
	}
}