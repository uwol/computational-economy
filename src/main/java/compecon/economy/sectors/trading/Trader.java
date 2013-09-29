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

package compecon.economy.sectors.trading;

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

import compecon.economy.PricingBehaviour;
import compecon.economy.markets.SettlementMarket.ISettlementEvent;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.BankAccount.BankAccountType;
import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.law.property.Property;
import compecon.economy.sectors.state.law.property.PropertyRegister;
import compecon.economy.sectors.state.law.security.equity.JointStockCompany;
import compecon.engine.AgentFactory;
import compecon.engine.MarketFactory;
import compecon.engine.Simulation;
import compecon.engine.statistics.Log;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.MonthType;
import compecon.engine.util.ConfigurationUtil;
import compecon.engine.util.MathUtil;
import compecon.materia.GoodType;

/**
 * Agent type Trader buys and sells goods
 */
@Entity
public class Trader extends JointStockCompany {

	@Transient
	protected Set<GoodType> excludedGoodTypes = new HashSet<GoodType>();

	@Transient
	protected final Map<GoodType, PricingBehaviour> goodTypePricingBehaviours = new HashMap<GoodType, PricingBehaviour>();

	@OneToMany
	@JoinTable(name = "Trader_ForeignCurrencyBankAccounts", joinColumns = @JoinColumn(name = "trader_id"), inverseJoinColumns = @JoinColumn(name = "bankAccount_id"))
	@MapKeyEnumerated
	protected Map<Currency, BankAccount> goodsTradeBankAccounts = new HashMap<Currency, BankAccount>();

	@Override
	public void initialize() {
		super.initialize();

		// arbitrage trading event every hour
		ITimeSystemEvent arbitrageTradingEvent = new ArbitrageTradingEvent();
		this.timeSystemEvents.add(arbitrageTradingEvent);
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(
						arbitrageTradingEvent,
						-1,
						MonthType.EVERY,
						DayType.EVERY,
						Simulation.getInstance().getTimeSystem()
								.suggestRandomHourType());

		// balance sheet publication
		ITimeSystemEvent balanceSheetPublicationEvent = new BalanceSheetPublicationEvent();
		this.timeSystemEvents.add(balanceSheetPublicationEvent);
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(balanceSheetPublicationEvent, -1, MonthType.EVERY,
						DayType.EVERY, BALANCE_SHEET_PUBLICATION_HOUR_TYPE);

		// pricing behaviours
		for (GoodType goodType : GoodType.values()) {
			if (!this.excludedGoodTypes.contains(goodType)) {
				double marketPrice = MarketFactory.getInstance().getPrice(
						this.primaryCurrency, goodType);
				this.goodTypePricingBehaviours.put(goodType,
						new PricingBehaviour(this, goodType,
								this.primaryCurrency, marketPrice));
			}
		}
	}

	@Transient
	public void deconstruct() {
		super.deconstruct();

		this.goodsTradeBankAccounts = null;
	}

	/*
	 * accessors
	 */

	public Set<GoodType> getExcludedGoodTypes() {
		return excludedGoodTypes;
	}

	public Map<Currency, BankAccount> getTransactionForeignCurrencyAccounts() {
		return goodsTradeBankAccounts;
	}

	public void setExcludedGoodTypes(Set<GoodType> excludedGoodTypes) {
		this.excludedGoodTypes = excludedGoodTypes;
	}

	public void setTransactionForeignCurrencyAccounts(
			Map<Currency, BankAccount> transactionForeignCurrencyAccounts) {
		this.goodsTradeBankAccounts = transactionForeignCurrencyAccounts;
	}

	/*
	 * assertions
	 */

	@Transient
	public void assureGoodsTradeBankAccounts() {
		if (this.isDeconstructed)
			return;

		for (Currency currency : Currency.values()) {
			if (!currency.equals(this.primaryCurrency)
					&& !this.goodsTradeBankAccounts.containsKey(currency)) {
				CreditBank foreignCurrencyCreditBank = AgentFactory
						.getRandomInstanceCreditBank(currency);
				BankAccount bankAccount = foreignCurrencyCreditBank
						.openBankAccount(this, currency,
								"foreign currency account",
								BankAccountType.GIRO);
				this.goodsTradeBankAccounts.put(currency, bankAccount);
			}
		}
	}

	@Override
	@Transient
	public void onBankCloseBankAccount(BankAccount bankAccount) {
		if (this.goodsTradeBankAccounts != null) {
			for (Entry<Currency, BankAccount> entry : new HashMap<Currency, BankAccount>(
					this.goodsTradeBankAccounts).entrySet()) {
				if (entry.getValue() == bankAccount)
					this.goodsTradeBankAccounts.remove(entry.getKey());
			}
		}

		super.onBankCloseBankAccount(bankAccount);
	}

	/*
	 * business logic
	 */
	protected class SettlementMarketEvent implements ISettlementEvent {
		@Override
		public void onEvent(GoodType goodType, double amount,
				double pricePerUnit, Currency currency) {
			Trader.this.assureTransactionsBankAccount();
			Trader.this.goodTypePricingBehaviours.get(goodType)
					.registerSelling(amount, amount * pricePerUnit);
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
			Trader.this.assureTransactionsBankAccount();
			Trader.this.assureGoodsTradeBankAccounts();

			this.buyGoodsForArbitrage();
			this.offerGoods();
		}

		protected void buyGoodsForArbitrage() {
			int numberOfForeignCurrencies = Trader.this.goodsTradeBankAccounts
					.keySet().size();
			double budgetPerForeignCurrencyInLocalCurrency = (Trader.this.referenceCredit + Trader.this.transactionsBankAccount
					.getBalance()) / (double) numberOfForeignCurrencies;

			if (MathUtil.greater(budgetPerForeignCurrencyInLocalCurrency, 0.0)) {
				/*
				 * for each currency / economy
				 */
				for (Entry<Currency, BankAccount> entry : Trader.this.goodsTradeBankAccounts
						.entrySet()) {
					Currency foreignCurrency = entry.getKey();
					BankAccount foreignCurrencyBankAccount = entry.getValue();

					/*
					 * determine the budget (local currency) for this good type,
					 * that can be spent for buying foreign currency
					 */
					double budgetPerGoodTypeAndForeignCurrencyInLocalCurrency = budgetPerForeignCurrencyInLocalCurrency
							/ (GoodType.values().length - Trader.this.excludedGoodTypes
									.size());

					/*
					 * for each good type
					 */
					for (GoodType goodType : GoodType.values()) {
						if (!Trader.this.excludedGoodTypes.contains(goodType)) {

							// e.g. CAR_in_EUR = 10
							double priceOfGoodTypeInLocalCurrency = MarketFactory
									.getInstance().getPrice(primaryCurrency,
											goodType);
							// e.g. CAR_in_USD = 11
							double priceOfGoodTypeInForeignCurrency = MarketFactory
									.getInstance().getPrice(foreignCurrency,
											goodType);
							// e.g. exchange rate for EUR/USD = 1.0
							double priceOfForeignCurrencyInLocalCurrency = MarketFactory
									.getInstance().getPrice(primaryCurrency,
											foreignCurrency);

							if (Double.isNaN(priceOfGoodTypeInForeignCurrency)) {
								if (Log.isAgentSelectedByClient(Trader.this))
									Log.log(Trader.this,
											"priceOfGoodTypeInForeignCurrency is "
													+ priceOfGoodTypeInForeignCurrency);
							} else if (Double
									.isNaN(priceOfGoodTypeInLocalCurrency)) {
								if (Log.isAgentSelectedByClient(Trader.this))
									Log.log(Trader.this,
											"priceOfGoodTypeInLocalCurrency is "
													+ priceOfGoodTypeInLocalCurrency);
							} else if (Double
									.isNaN(priceOfForeignCurrencyInLocalCurrency)) {
								if (Log.isAgentSelectedByClient(Trader.this))
									Log.log(Trader.this,
											"priceOfForeignCurrencyInLocalCurrency is "
													+ priceOfForeignCurrencyInLocalCurrency);
							} else {
								// inverse_CAR_in_USD -> correct_CAR_in_EUR =
								// 1.25
								double importPriceOfGoodTypeInLocalCurrency = priceOfGoodTypeInForeignCurrency
										* priceOfForeignCurrencyInLocalCurrency;

								if (MathUtil
										.greater(
												priceOfGoodTypeInLocalCurrency
														/ (1 + ConfigurationUtil.TraderConfig
																.getArbitrageMargin()),
												importPriceOfGoodTypeInLocalCurrency)) {

									if (Log.isAgentSelectedByClient(Trader.this))
										Log.log(Trader.this,
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
									 * buy foreign currency with local currency
									 */
									MarketFactory
											.getInstance()
											.buy(foreignCurrency,
													Double.NaN,
													budgetPerGoodTypeAndForeignCurrencyInLocalCurrency,
													priceOfForeignCurrencyInLocalCurrency,
													Trader.this,
													Trader.this.transactionsBankAccount,
													Trader.this.goodsTradeBankAccounts
															.get(foreignCurrency));

									/*
									 * buy goods of good type with foreign
									 * currency
									 */
									MarketFactory.getInstance().buy(
											goodType,
											Double.NaN,
											foreignCurrencyBankAccount
													.getBalance(),
											priceOfGoodTypeInForeignCurrency,
											Trader.this,
											foreignCurrencyBankAccount);
								}
							}
						}
					}
				}
			}
		}

		protected void offerGoods() {
			/*
			 * prepare pricing behaviours
			 */
			for (PricingBehaviour pricingBehaviour : Trader.this.goodTypePricingBehaviours
					.values()) {
				pricingBehaviour.nextPeriod();
			}

			/*
			 * refresh prices / offer in local currency
			 */
			for (Entry<GoodType, PricingBehaviour> entry : Trader.this.goodTypePricingBehaviours
					.entrySet()) {
				GoodType goodType = entry.getKey();
				PricingBehaviour pricingBehaviour = entry.getValue();

				MarketFactory.getInstance().removeAllSellingOffers(Trader.this,
						Trader.this.primaryCurrency, goodType);
				double amount = PropertyRegister.getInstance().getBalance(
						Trader.this, goodType);
				double[] prices = pricingBehaviour.getCurrentPriceArray();
				for (double price : prices) {
					MarketFactory.getInstance().placeSettlementSellingOffer(
							goodType, Trader.this,
							Trader.this.transactionsBankAccount,
							amount / prices.length, price,
							new SettlementMarketEvent());
				}
				Trader.this.goodTypePricingBehaviours.get(goodType)
						.registerOfferedAmount(amount);
			}
		}
	}

	public class BalanceSheetPublicationEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			Trader.this.assureTransactionsBankAccount();

			Log.agent_onPublishBalanceSheet(Trader.this,
					Trader.this.issueBasicBalanceSheet());
		}
	}

}