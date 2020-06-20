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

package io.github.uwol.compecon.economy.sectors.trading.impl;

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

import io.github.uwol.compecon.economy.behaviour.BudgetingBehaviour;
import io.github.uwol.compecon.economy.bookkeeping.impl.BalanceSheetDTO;
import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.property.Property;
import io.github.uwol.compecon.economy.sectors.financial.BankAccount;
import io.github.uwol.compecon.economy.sectors.financial.BankAccount.MoneyType;
import io.github.uwol.compecon.economy.sectors.financial.BankAccount.TermType;
import io.github.uwol.compecon.economy.sectors.financial.BankAccountDelegate;
import io.github.uwol.compecon.economy.sectors.financial.CreditBank;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.economy.sectors.financial.impl.BankAccountImpl;
import io.github.uwol.compecon.economy.sectors.trading.Trader;
import io.github.uwol.compecon.economy.security.equity.impl.JointStockCompanyImpl;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.timesystem.TimeSystemEvent;
import io.github.uwol.compecon.engine.timesystem.impl.DayType;
import io.github.uwol.compecon.engine.timesystem.impl.MonthType;
import io.github.uwol.compecon.math.util.MathUtil;

/**
 * Agent type Trader imports goods and sells them on his domestic market.
 * Traders are price takers.
 */
@Entity
public class TraderImpl extends JointStockCompanyImpl implements Trader {

	public class ArbitrageTradingEvent implements TimeSystemEvent {

		protected void buyGoodsForArbitrage() {
			final int numberOfForeignCurrencies = bankAccountsGoodTrade.keySet().size();

			final double budget = budgetingBehaviour.calculateTransmissionBasedBudgetForPeriod(
					TraderImpl.this.bankAccountTransactions.getCurrency(),
					TraderImpl.this.bankAccountTransactions.getBalance(), TraderImpl.this.referenceCredit);

			final double budgetPerForeignCurrencyInLocalCurrency = budget / numberOfForeignCurrencies;

			if (MathUtil.greater(budgetPerForeignCurrencyInLocalCurrency, 0.0)) {
				/*
				 * for each currency / economy
				 */
				for (final Currency currency : bankAccountsGoodTrade.keySet()) {
					if (!TraderImpl.this.primaryCurrency.equals(currency)) {
						final Currency localCurrency = TraderImpl.this.primaryCurrency;
						final Currency foreignCurrency = currency;

						/*
						 * determine the budget (local currency) for this good type, that can be spent
						 * for buying foreign currency
						 */
						final double budgetPerGoodTypeAndForeignCurrencyInLocalCurrency = budgetPerForeignCurrencyInLocalCurrency
								/ (GoodType.values().length - excludedGoodTypes.size());

						/*
						 * for each good type
						 */
						for (final GoodType goodType : GoodType.values()) {
							if (!excludedGoodTypes.contains(goodType)) {

								// e.g. CAR_in_EUR = 10
								final double priceOfGoodTypeInLocalCurrency = ApplicationContext.getInstance()
										.getMarketService().getMarginalMarketPrice(localCurrency, goodType);
								// e.g. CAR_in_USD = 11
								final double priceOfGoodTypeInForeignCurrency = ApplicationContext.getInstance()
										.getMarketService().getMarginalMarketPrice(foreignCurrency, goodType);
								// e.g. exchange rate for EUR/USD = 1.0
								final double priceOfForeignCurrencyInLocalCurrency = ApplicationContext.getInstance()
										.getMarketService().getMarginalMarketPrice(localCurrency, foreignCurrency);

								if (Double.isNaN(priceOfGoodTypeInForeignCurrency)) {
									if (getLog().isAgentSelectedByClient(TraderImpl.this)) {
										getLog().log(TraderImpl.this, "priceOfGoodTypeInForeignCurrency is %s",
												priceOfGoodTypeInForeignCurrency);
									}
								} else if (Double.isNaN(priceOfGoodTypeInLocalCurrency)) {
									if (getLog().isAgentSelectedByClient(TraderImpl.this)) {
										getLog().log(TraderImpl.this, "priceOfGoodTypeInLocalCurrency is %s",
												priceOfGoodTypeInLocalCurrency);
									}
								} else if (Double.isNaN(priceOfForeignCurrencyInLocalCurrency)) {
									if (getLog().isAgentSelectedByClient(TraderImpl.this)) {
										getLog().log(TraderImpl.this, "priceOfForeignCurrencyInLocalCurrency is %s",
												priceOfForeignCurrencyInLocalCurrency);
									}
								} else {
									// inverse_CAR_in_USD -> correct_CAR_in_EUR
									// = 1.25
									final double importPriceOfGoodTypeInLocalCurrency = priceOfGoodTypeInForeignCurrency
											* priceOfForeignCurrencyInLocalCurrency;

									if (MathUtil.greater(
											priceOfGoodTypeInLocalCurrency / (1.0
													+ ApplicationContext.getInstance().getConfiguration().traderConfig
															.getArbitrageMargin()),
											importPriceOfGoodTypeInLocalCurrency)) {

										if (getLog().isAgentSelectedByClient(TraderImpl.this)) {
											getLog().log(TraderImpl.this,
													"1 %s = %s %s; 1 %s = %s %s; 1 %s = %s %s -> import price of 1 %s = %s %s -> importing %s",
													goodType, Currency.formatMoneySum(priceOfGoodTypeInLocalCurrency),
													primaryCurrency, goodType,
													Currency.formatMoneySum(priceOfGoodTypeInForeignCurrency),
													foreignCurrency, foreignCurrency,
													Currency.formatMoneySum(priceOfForeignCurrencyInLocalCurrency),
													primaryCurrency, goodType,
													Currency.formatMoneySum(importPriceOfGoodTypeInLocalCurrency),
													primaryCurrency, goodType);
										}

										/*
										 * buy foreign currency with local currency
										 */
										ApplicationContext.getInstance().getMarketService().buy(foreignCurrency,
												Double.NaN, budgetPerGoodTypeAndForeignCurrencyInLocalCurrency,
												priceOfForeignCurrencyInLocalCurrency, TraderImpl.this,
												getBankAccountTransactionsDelegate(),
												getBankAccountGoodsTradeDelegate(foreignCurrency));

										/*
										 * buy goods of good type with foreign currency
										 */
										ApplicationContext.getInstance().getMarketService().buy(goodType, Double.NaN,
												getBankAccountGoodsTradeDelegate(foreignCurrency).getBankAccount()
														.getBalance(),
												priceOfGoodTypeInForeignCurrency, TraderImpl.this,
												getBankAccountGoodsTradeDelegate(foreignCurrency));
									}
								}
							}
						}
					}
				}
			}
		}

		@Override
		public boolean isDeconstructed() {
			return TraderImpl.this.isDeconstructed;
		}

		protected void offerGoods() {
			/*
			 * for each good type offer owned goods at market price -> price taker
			 */
			for (final GoodType goodType : GoodType.values()) {
				if (!excludedGoodTypes.contains(goodType)) {
					ApplicationContext.getInstance().getMarketService().removeAllSellingOffers(TraderImpl.this,
							TraderImpl.this.primaryCurrency, goodType);

					final double amount = ApplicationContext.getInstance().getPropertyService()
							.getGoodTypeBalance(TraderImpl.this, goodType);
					final double marketPrice = ApplicationContext.getInstance().getMarketService()
							.getMarginalMarketPrice(TraderImpl.this.primaryCurrency, goodType);

					ApplicationContext.getInstance().getMarketService().placeSellingOffer(goodType, TraderImpl.this,
							getBankAccountTransactionsDelegate(), amount, marketPrice);
				}
			}
		}

		@Override
		public void onEvent() {
			assureBankAccountTransactions();
			assureBankAccountsGoodTrade();

			transferBankAccountBalanceToDividendBankAccount(TraderImpl.this.bankAccountTransactions);

			buyGoodsForArbitrage();
			offerGoods();
		}
	}

	@OneToMany(targetEntity = BankAccountImpl.class)
	@JoinTable(name = "Trader_BankAccountsGoodTrade", joinColumns = @JoinColumn(name = "trader_id"), inverseJoinColumns = @JoinColumn(name = "bankAccount_id"))
	@MapKeyEnumerated
	protected Map<Currency, BankAccount> bankAccountsGoodTrade = new HashMap<Currency, BankAccount>();

	@Transient
	protected Map<Currency, BankAccountDelegate> bankAccountsGoodTradeDelegate = new HashMap<Currency, BankAccountDelegate>();

	@Transient
	protected BudgetingBehaviour budgetingBehaviour;

	@Transient
	protected Set<GoodType> excludedGoodTypes = new HashSet<GoodType>();

	@Transient
	public void assureBankAccountsGoodTrade() {
		if (isDeconstructed) {
			return;
		}

		for (final Currency currency : Currency.values()) {
			if (!currency.equals(primaryCurrency) && !bankAccountsGoodTrade.containsKey(currency)) {
				final CreditBank foreignCurrencyCreditBank = ApplicationContext.getInstance().getAgentService()
						.findRandomCreditBank(currency);
				final BankAccount bankAccount = foreignCurrencyCreditBank.openBankAccount(this, currency, true,
						"foreign currency", TermType.SHORT_TERM, MoneyType.DEPOSITS);
				bankAccountsGoodTrade.put(currency, bankAccount);
			}
		}
	}

	@Override
	public void deconstruct() {
		super.deconstruct();

		ApplicationContext.getInstance().getTraderFactory().deleteTrader(this);
	}

	@Override
	@Transient
	public BankAccountDelegate getBankAccountGoodsTradeDelegate(final Currency currency) {
		return bankAccountsGoodTradeDelegate.get(currency);
	}

	public Map<Currency, BankAccount> getBankAccountsGoodTrade() {
		return bankAccountsGoodTrade;
	}

	public Set<GoodType> getExcludedGoodTypes() {
		return excludedGoodTypes;
	}

	@Override
	public void initialize() {
		super.initialize();

		// arbitrage trading event every hour
		final TimeSystemEvent arbitrageTradingEvent = new ArbitrageTradingEvent();
		timeSystemEvents.add(arbitrageTradingEvent);
		ApplicationContext.getInstance().getTimeSystem().addEvent(arbitrageTradingEvent, -1, MonthType.EVERY,
				DayType.EVERY, ApplicationContext.getInstance().getTimeSystem().suggestRandomHourType());

		// initialize good trade bank account delegates
		for (final Currency currency : Currency.values()) {
			final BankAccountDelegate delegate = new BankAccountDelegate() {
				@Override
				public BankAccount getBankAccount() {
					TraderImpl.this.assureBankAccountsGoodTrade();
					return bankAccountsGoodTrade.get(currency);
				}

				@Override
				public void onTransfer(final double amount) {
				}
			};
			bankAccountsGoodTradeDelegate.put(currency, delegate);
		}

		budgetingBehaviour = ApplicationContext.getInstance().getBudgetingBehaviourFactory()
				.newInstanceBudgetingBehaviour(this);
	}

	@Override
	@Transient
	protected BalanceSheetDTO issueBalanceSheet() {
		assureBankAccountsGoodTrade();

		final BalanceSheetDTO balanceSheet = super.issueBalanceSheet();

		// add balances of foreign currency bank accounts
		for (final Entry<Currency, BankAccount> bankAccountEntry : bankAccountsGoodTrade.entrySet()) {
			final double priceOfForeignCurrencyInLocalCurrency = ApplicationContext.getInstance().getMarketService()
					.getMarginalMarketPrice(primaryCurrency, bankAccountEntry.getKey());
			if (!Double.isNaN(priceOfForeignCurrencyInLocalCurrency)) {
				final double valueOfForeignCurrencyInLocalCurrency = bankAccountEntry.getValue().getBalance()
						* priceOfForeignCurrencyInLocalCurrency;
				balanceSheet.cashForeignCurrency += valueOfForeignCurrencyInLocalCurrency;
			}
		}

		return balanceSheet;
	}

	@Override
	@Transient
	public void onBankCloseBankAccount(final BankAccount bankAccount) {
		if (bankAccountsGoodTrade != null) {
			for (final Entry<Currency, BankAccount> entry : new HashMap<Currency, BankAccount>(bankAccountsGoodTrade)
					.entrySet()) {
				if (entry.getValue() == bankAccount) {
					bankAccountsGoodTrade.remove(entry.getKey());
				}
			}
		}

		super.onBankCloseBankAccount(bankAccount);
	}

	@Override
	public void onMarketSettlement(final Currency commodityCurrency, final double amount, final double pricePerUnit,
			final Currency currency) {
	}

	@Override
	public void onMarketSettlement(final GoodType goodType, final double amount, final double pricePerUnit,
			final Currency currency) {
		TraderImpl.this.assureBankAccountTransactions();
	}

	@Override
	public void onMarketSettlement(final Property property, final double totalPrice, final Currency currency) {
	}

	public void setBankAccountsGoodTrade(final Map<Currency, BankAccount> bankAccountsGoodsTrade) {
		bankAccountsGoodTrade = bankAccountsGoodsTrade;
	}

	public void setExcludedGoodTypes(final Set<GoodType> excludedGoodTypes) {
		this.excludedGoodTypes = excludedGoodTypes;
	}
}