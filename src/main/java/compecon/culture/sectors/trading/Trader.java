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

package compecon.culture.sectors.trading;

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

import compecon.culture.PricingBehaviour;
import compecon.culture.markets.SettlementMarket.ISettlementEvent;
import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.BankAccount.BankAccountType;
import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.property.Property;
import compecon.culture.sectors.state.law.property.PropertyRegister;
import compecon.culture.sectors.state.law.security.equity.JointStockCompany;
import compecon.engine.AgentFactory;
import compecon.engine.MarketFactory;
import compecon.engine.jmx.Log;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.TimeSystem;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.MonthType;
import compecon.engine.util.MathUtil;
import compecon.nature.materia.GoodType;

/**
 * Agent type Trader buys and sells goods
 */
@Entity
public class Trader extends JointStockCompany {

	@Transient
	protected final double ARBITRAGE_MARGIN = 0.2;

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
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				arbitrageTradingEvent, -1, MonthType.EVERY, DayType.EVERY,
				TimeSystem.getInstance().suggestRandomHourType());

		// balance sheet publication
		ITimeSystemEvent balanceSheetPublicationEvent = new BalanceSheetPublicationEvent();
		this.timeSystemEvents.add(balanceSheetPublicationEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				balanceSheetPublicationEvent, -1, MonthType.EVERY,
				DayType.EVERY, BALANCE_SHEET_PUBLICATION_HOUR_TYPE);

		// pricing behaviours
		for (GoodType goodType : GoodType.values()) {
			if (!this.excludedGoodTypes.contains(goodType)) {
				double marketPrice = MarketFactory.getInstance()
						.getMarginalPrice(this.primaryCurrency, goodType);
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
			if (currency != this.primaryCurrency
					&& !this.goodsTradeBankAccounts.containsKey(currency)) {
				CreditBank foreignCurrencyCreditBank = AgentFactory
						.getRandomInstanceCreditBank(currency);
				String bankPassword = foreignCurrencyCreditBank
						.openCustomerAccount(this);
				this.bankPasswords.put(foreignCurrencyCreditBank, bankPassword);
				BankAccount bankAccount = foreignCurrencyCreditBank
						.openBankAccount(this, currency, this.bankPasswords
								.get(foreignCurrencyCreditBank),
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
								.getInstance().getMarginalPrice(
										primaryCurrency, goodType);
						// e.g. CAR_in_USD = 11
						double priceOfGoodTypeInForeignCurrency = MarketFactory
								.getInstance().getMarginalPrice(
										foreignCurrency, goodType);
						// e.g. exchange rate for EUR/USD = 1.0
						double priceOfForeignCurrencyInLocalCurrency = MarketFactory
								.getInstance().getMarginalPrice(
										primaryCurrency, foreignCurrency);

						if (Double.isNaN(priceOfGoodTypeInForeignCurrency)) {
							if (Log.isAgentSelectedByClient(Trader.this))
								Log.log(Trader.this,
										"priceOfGoodTypeInForeignCurrency is "
												+ priceOfGoodTypeInForeignCurrency);
						} else if (Double.isNaN(priceOfGoodTypeInLocalCurrency)) {
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

							if (MathUtil.greater(priceOfGoodTypeInLocalCurrency
									/ (1 + ARBITRAGE_MARGIN),
									importPriceOfGoodTypeInLocalCurrency)) {

								if (Log.isAgentSelectedByClient(Trader.this))
									Log.log(Trader.this,
											"1 "
													+ goodType
													+ " = "
													+ Currency
															.round(priceOfGoodTypeInLocalCurrency)
													+ " "
													+ primaryCurrency
															.getIso4217Code()
													+ "; "
													+ "1 "
													+ goodType
													+ " = "
													+ Currency
															.round(priceOfGoodTypeInForeignCurrency)
													+ " "
													+ foreignCurrency
															.getIso4217Code()
													+ "; "
													+ "1 "
													+ foreignCurrency
															.getIso4217Code()
													+ " = "
													+ Currency
															.round(priceOfForeignCurrencyInLocalCurrency)
													+ " "
													+ primaryCurrency
															.getIso4217Code()
													+ " -> import price of 1 "
													+ goodType
													+ " = "
													+ Currency
															.round(importPriceOfGoodTypeInLocalCurrency)
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
												Trader.this.bankPasswords
														.get(Trader.this.transactionsBankAccount
																.getManagingBank()),
												Trader.this.goodsTradeBankAccounts
														.get(foreignCurrency));

								/*
								 * buy goods of good type with foreign currency
								 */
								MarketFactory
										.getInstance()
										.buy(goodType,
												Double.NaN,
												foreignCurrencyBankAccount
														.getBalance(),
												priceOfGoodTypeInForeignCurrency,
												Trader.this,
												foreignCurrencyBankAccount,
												Trader.this
														.getBankPasswords()
														.get(foreignCurrencyBankAccount
																.getManagingBank()));
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

				pricingBehaviour.setNewPrice();
				MarketFactory.getInstance().removeAllSellingOffers(Trader.this,
						Trader.this.primaryCurrency, goodType);
				double amount = PropertyRegister.getInstance().getBalance(
						Trader.this, goodType);
				MarketFactory.getInstance()
						.placeSettlementSellingOffer(
								goodType,
								Trader.this,
								Trader.this.transactionsBankAccount,
								amount,
								Trader.this.goodTypePricingBehaviours.get(
										goodType).getCurrentPrice(),
								new SettlementMarketEvent());
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