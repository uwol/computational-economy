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

package compecon.economy.sectors.trading.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import compecon.economy.behaviour.BudgetingBehaviour;
import compecon.economy.behaviour.impl.BudgetingBehaviourImpl;
import compecon.economy.bookkeeping.impl.BalanceSheetDTO;
import compecon.economy.property.Property;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.financial.impl.BankAccountImpl;
import compecon.economy.sectors.trading.Trader;
import compecon.economy.security.equity.impl.JointStockCompanyImpl;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.service.SettlementMarketService.SettlementEvent;
import compecon.engine.timesystem.ITimeSystemEvent;
import compecon.engine.timesystem.impl.DayType;
import compecon.engine.timesystem.impl.MonthType;
import compecon.engine.util.MathUtil;
import compecon.materia.GoodType;

/**
 * Agent type Trader imports goods and sells them on his domestic market.
 * Traders are price takers.
 */
@Entity
public class TraderImpl extends JointStockCompanyImpl implements Trader {

	@Transient
	protected BudgetingBehaviour budgetingBehaviour;

	@Transient
	protected Set<GoodType> excludedGoodTypes = new HashSet<GoodType>();

	@OneToMany(targetEntity = BankAccountImpl.class)
	@JoinTable(name = "Trader_BankAccountsGoodTrade", joinColumns = @JoinColumn(name = "trader_id"), inverseJoinColumns = @JoinColumn(name = "bankAccount_id"))
	@MapKeyEnumerated
	protected Map<Currency, BankAccount> bankAccountsGoodTrade = new HashMap<Currency, BankAccount>();

	@Override
	public void initialize() {
		super.initialize();

		// arbitrage trading event every hour
		final ITimeSystemEvent arbitrageTradingEvent = new ArbitrageTradingEvent();
		this.timeSystemEvents.add(arbitrageTradingEvent);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(
						arbitrageTradingEvent,
						-1,
						MonthType.EVERY,
						DayType.EVERY,
						ApplicationContext.getInstance().getTimeSystem()
								.suggestRandomHourType());

		this.budgetingBehaviour = new BudgetingBehaviourImpl(this);
	}

	/*
	 * accessors
	 */

	public Map<Currency, BankAccount> getBankAccountsGoodTrade() {
		return bankAccountsGoodTrade;
	}

	public Set<GoodType> getExcludedGoodTypes() {
		return excludedGoodTypes;
	}

	public void setBankAccountsGoodTrade(
			Map<Currency, BankAccount> bankAccountsGoodsTrade) {
		this.bankAccountsGoodTrade = bankAccountsGoodsTrade;
	}

	public void setExcludedGoodTypes(Set<GoodType> excludedGoodTypes) {
		this.excludedGoodTypes = excludedGoodTypes;
	}

	/*
	 * assertions
	 */

	@Transient
	public void assureBankAccountsGoodTrade() {
		if (this.isDeconstructed)
			return;

		for (Currency currency : Currency.values()) {
			if (!currency.equals(this.primaryCurrency)
					&& !this.bankAccountsGoodTrade.containsKey(currency)) {
				CreditBank foreignCurrencyCreditBank = ApplicationContext
						.getInstance().getAgentService()
						.getRandomInstanceCreditBank(currency);
				BankAccount bankAccount = foreignCurrencyCreditBank
						.openBankAccount(this, currency, true,
								"foreign currency", TermType.SHORT_TERM,
								MoneyType.DEPOSITS);
				this.bankAccountsGoodTrade.put(currency, bankAccount);
			}
		}
	}

	/*
	 * business logic
	 */

	@Override
	@Transient
	protected BalanceSheetDTO issueBalanceSheet() {
		this.assureBankAccountsGoodTrade();

		final BalanceSheetDTO balanceSheet = super.issueBalanceSheet();

		// add balances of foreign currency bank accounts
		for (Entry<Currency, BankAccount> bankAccountEntry : this.bankAccountsGoodTrade
				.entrySet()) {
			double priceOfForeignCurrencyInLocalCurrency = ApplicationContext
					.getInstance().getMarketService()
					.getPrice(primaryCurrency, bankAccountEntry.getKey());
			if (!Double.isNaN(priceOfForeignCurrencyInLocalCurrency)) {
				double valueOfForeignCurrencyInLocalCurrency = bankAccountEntry
						.getValue().getBalance()
						* priceOfForeignCurrencyInLocalCurrency;
				balanceSheet.cashForeignCurrency += valueOfForeignCurrencyInLocalCurrency;
			}
		}

		return balanceSheet;
	}

	@Transient
	public BankAccountDelegate getBankAccountGoodsTradeDelegate(
			final Currency currency) {
		final BankAccountDelegate delegate = new BankAccountDelegate() {
			@Override
			public BankAccount getBankAccount() {
				TraderImpl.this.assureBankAccountsGoodTrade();
				return TraderImpl.this.bankAccountsGoodTrade.get(currency);
			}
		};
		return delegate;
	}

	@Override
	@Transient
	public void onBankCloseBankAccount(BankAccount bankAccount) {
		if (this.bankAccountsGoodTrade != null) {
			for (Entry<Currency, BankAccount> entry : new HashMap<Currency, BankAccount>(
					this.bankAccountsGoodTrade).entrySet()) {
				if (entry.getValue() == bankAccount) {
					this.bankAccountsGoodTrade.remove(entry.getKey());
				}
			}
		}

		super.onBankCloseBankAccount(bankAccount);
	}

	protected class SettlementMarketEvent implements SettlementEvent {
		@Override
		public void onEvent(GoodType goodType, double amount,
				double pricePerUnit, Currency currency) {
			TraderImpl.this.assureBankAccountTransactions();
		}

		@Override
		public void onEvent(Currency commodityCurrency, double amount,
				double pricePerUnit, Currency currency) {
		}

		@Override
		public void onEvent(Property property, double totalPrice,
				Currency currency) {
		}
	}

	public class ArbitrageTradingEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			TraderImpl.this.assureBankAccountTransactions();
			TraderImpl.this.assureBankAccountsGoodTrade();

			TraderImpl.this
					.transferBankAccountBalanceToDividendBankAccount(TraderImpl.this.bankAccountTransactions);

			this.buyGoodsForArbitrage();
			this.offerGoods();
		}

		protected void buyGoodsForArbitrage() {
			int numberOfForeignCurrencies = TraderImpl.this.bankAccountsGoodTrade
					.keySet().size();

			double budget = TraderImpl.this.budgetingBehaviour
					.calculateTransmissionBasedBudgetForPeriod(
							TraderImpl.this.bankAccountTransactions
									.getCurrency(),
							TraderImpl.this.bankAccountTransactions
									.getBalance(),
							TraderImpl.this.referenceCredit);

			double budgetPerForeignCurrencyInLocalCurrency = budget
					/ (double) numberOfForeignCurrencies;

			if (MathUtil.greater(budgetPerForeignCurrencyInLocalCurrency, 0.0)) {
				/*
				 * for each currency / economy
				 */
				for (Currency currency : TraderImpl.this.bankAccountsGoodTrade
						.keySet()) {
					if (!TraderImpl.this.primaryCurrency.equals(currency)) {
						final Currency localCurrency = TraderImpl.this.primaryCurrency;
						final Currency foreignCurrency = currency;

						/*
						 * determine the budget (local currency) for this good
						 * type, that can be spent for buying foreign currency
						 */
						double budgetPerGoodTypeAndForeignCurrencyInLocalCurrency = budgetPerForeignCurrencyInLocalCurrency
								/ (double) (GoodType.values().length - TraderImpl.this.excludedGoodTypes
										.size());

						/*
						 * for each good type
						 */
						for (GoodType goodType : GoodType.values()) {
							if (!TraderImpl.this.excludedGoodTypes
									.contains(goodType)) {

								// e.g. CAR_in_EUR = 10
								double priceOfGoodTypeInLocalCurrency = ApplicationContext
										.getInstance().getMarketService()
										.getPrice(localCurrency, goodType);
								// e.g. CAR_in_USD = 11
								double priceOfGoodTypeInForeignCurrency = ApplicationContext
										.getInstance().getMarketService()
										.getPrice(foreignCurrency, goodType);
								// e.g. exchange rate for EUR/USD = 1.0
								double priceOfForeignCurrencyInLocalCurrency = ApplicationContext
										.getInstance()
										.getMarketService()
										.getPrice(localCurrency,
												foreignCurrency);

								if (Double
										.isNaN(priceOfGoodTypeInForeignCurrency)) {
									if (getLog().isAgentSelectedByClient(
											TraderImpl.this))
										getLog().log(
												TraderImpl.this,
												"priceOfGoodTypeInForeignCurrency is "
														+ priceOfGoodTypeInForeignCurrency);
								} else if (Double
										.isNaN(priceOfGoodTypeInLocalCurrency)) {
									if (getLog().isAgentSelectedByClient(
											TraderImpl.this))
										getLog().log(
												TraderImpl.this,
												"priceOfGoodTypeInLocalCurrency is "
														+ priceOfGoodTypeInLocalCurrency);
								} else if (Double
										.isNaN(priceOfForeignCurrencyInLocalCurrency)) {
									if (getLog().isAgentSelectedByClient(
											TraderImpl.this))
										getLog().log(
												TraderImpl.this,
												"priceOfForeignCurrencyInLocalCurrency is "
														+ priceOfForeignCurrencyInLocalCurrency);
								} else {
									// inverse_CAR_in_USD -> correct_CAR_in_EUR
									// =
									// 1.25
									double importPriceOfGoodTypeInLocalCurrency = priceOfGoodTypeInForeignCurrency
											* priceOfForeignCurrencyInLocalCurrency;

									if (MathUtil
											.greater(
													priceOfGoodTypeInLocalCurrency
															/ (1.0 + ApplicationContext
																	.getInstance()
																	.getConfiguration().traderConfig
																	.getArbitrageMargin()),
													importPriceOfGoodTypeInLocalCurrency)) {

										if (getLog().isAgentSelectedByClient(
												TraderImpl.this))
											getLog().log(
													TraderImpl.this,
													"1 "
															+ goodType
															+ " = "
															+ Currency
																	.formatMoneySum(priceOfGoodTypeInLocalCurrency)
															+ " "
															+ primaryCurrency
																	.getIso4217Code()
															+ "; "
															+ "1 "
															+ goodType
															+ " = "
															+ Currency
																	.formatMoneySum(priceOfGoodTypeInForeignCurrency)
															+ " "
															+ foreignCurrency
																	.getIso4217Code()
															+ "; "
															+ "1 "
															+ foreignCurrency
																	.getIso4217Code()
															+ " = "
															+ Currency
																	.formatMoneySum(priceOfForeignCurrencyInLocalCurrency)
															+ " "
															+ primaryCurrency
																	.getIso4217Code()
															+ " -> import price of 1 "
															+ goodType
															+ " = "
															+ Currency
																	.formatMoneySum(importPriceOfGoodTypeInLocalCurrency)
															+ " "
															+ primaryCurrency
																	.getIso4217Code()
															+ " -> importing "
															+ goodType);

										/*
										 * buy foreign currency with local
										 * currency
										 */
										ApplicationContext
												.getInstance()
												.getMarketService()
												.buy(foreignCurrency,
														Double.NaN,
														budgetPerGoodTypeAndForeignCurrencyInLocalCurrency,
														priceOfForeignCurrencyInLocalCurrency,
														TraderImpl.this,
														getBankAccountTransactionsDelegate(),
														getBankAccountGoodsTradeDelegate(foreignCurrency));

										/*
										 * buy goods of good type with foreign
										 * currency
										 */
										ApplicationContext
												.getInstance()
												.getMarketService()
												.buy(goodType,
														Double.NaN,
														getBankAccountGoodsTradeDelegate(
																foreignCurrency)
																.getBankAccount()
																.getBalance(),
														priceOfGoodTypeInForeignCurrency,
														TraderImpl.this,
														getBankAccountGoodsTradeDelegate(foreignCurrency));
									}
								}
							}
						}
					}
				}
			}
		}

		protected void offerGoods() {
			/*
			 * for each good type offer owned goods at market price -> price
			 * taker
			 */
			for (GoodType goodType : GoodType.values()) {
				if (!TraderImpl.this.excludedGoodTypes.contains(goodType)) {
					ApplicationContext
							.getInstance()
							.getMarketService()
							.removeAllSellingOffers(TraderImpl.this,
									TraderImpl.this.primaryCurrency, goodType);
					double amount = ApplicationContext.getInstance()
							.getPropertyService()
							.getBalance(TraderImpl.this, goodType);
					double marketPrice = ApplicationContext
							.getInstance()
							.getMarketService()
							.getPrice(TraderImpl.this.primaryCurrency, goodType);
					ApplicationContext
							.getInstance()
							.getMarketService()
							.placeSettlementSellingOffer(goodType,
									TraderImpl.this,
									getBankAccountTransactionsDelegate(),
									amount, marketPrice,
									new SettlementMarketEvent());
				}
			}
		}
	}
}