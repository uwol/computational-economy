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
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyEnumerated;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import compecon.culture.PricingBehaviour;
import compecon.culture.markets.SettlementMarket.ISettlementEvent;
import compecon.culture.sectors.financial.BankAccount;
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
	protected final int MAX_CREDIT = 1000;

	@Transient
	protected Map<GoodType, PricingBehaviour> goodTypePricingBehaviours = new HashMap<GoodType, PricingBehaviour>();

	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "Trader_ForeignCurrencyBankAccounts", joinColumns = @JoinColumn(name = "trader_id"), inverseJoinColumns = @JoinColumn(name = "bankAccount_id"))
	@MapKeyEnumerated
	protected Map<Currency, BankAccount> transactionForeignCurrencyAccounts = new HashMap<Currency, BankAccount>();

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

		for (GoodType goodType : GoodType.values()) {
			double marketPrice = MarketFactory.getInstance().getMarginalPrice(
					this.primaryCurrency, goodType);
			this.goodTypePricingBehaviours.put(goodType, new PricingBehaviour(
					this, goodType, this.primaryCurrency, marketPrice));
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

	public Map<Currency, BankAccount> getTransactionForeignCurrencyAccounts() {
		return transactionForeignCurrencyAccounts;
	}

	public void setTransactionForeignCurrencyAccounts(
			Map<Currency, BankAccount> transactionForeignCurrencyAccounts) {
		this.transactionForeignCurrencyAccounts = transactionForeignCurrencyAccounts;
	}

	/*
	 * assertions
	 */

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

	/*
	 * business logic
	 */
	protected class SettlementMarketEvent implements ISettlementEvent {
		@Override
		public void onEvent(GoodType goodType, double amount,
				double pricePerUnit, Currency currency) {
			Trader.this.assureTransactionsBankAccount();
			Trader.this.goodTypePricingBehaviours.get(goodType)
					.registerSelling(amount);
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
			Trader.this.assureTransactionsForeignCurrencyBankAccounts();

			/*
			 * prepare pricing behaviours
			 */
			for (PricingBehaviour pricingBehaviour : Trader.this.goodTypePricingBehaviours
					.values()) {
				pricingBehaviour.nextPeriod();
			}

			int numberOfForeignCurrencies = Trader.this.transactionForeignCurrencyAccounts
					.keySet().size();
			if (numberOfForeignCurrencies > 0) {
				double budgetPerForeignCurrencyInLocalCurrency = (Trader.this.MAX_CREDIT + Trader.this.transactionsBankAccount
						.getBalance()) / (double) numberOfForeignCurrencies;

				/*
				 * for each currency / economy
				 */
				for (Entry<Currency, BankAccount> entry : Trader.this.transactionForeignCurrencyAccounts
						.entrySet()) {
					Currency foreignCurrency = entry.getKey();
					BankAccount foreignCurrencyBankAccount = entry.getValue();

					if (budgetPerForeignCurrencyInLocalCurrency > 0) {
						/*
						 * buy foreign currency with local currency
						 */
						MarketFactory
								.getInstance()
								.buy(foreignCurrency,
										budgetPerForeignCurrencyInLocalCurrency,
										-1,
										-1,
										Trader.this,
										Trader.this.transactionsBankAccount,
										Trader.this.bankPasswords
												.get(Trader.this.transactionsBankAccount
														.getManagingBank()),
										Trader.this.transactionForeignCurrencyAccounts
												.get(foreignCurrency));
					}

					double budgetPerGoodTypeInForeignCurrency = foreignCurrencyBankAccount
							.getBalance()
							/ (double) (GoodType.values().length - 1);
					for (GoodType goodType : GoodType.values()) {
						if (!GoodType.LABOURHOUR.equals(goodType)) {
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

							if (!Double.isNaN(priceOfGoodTypeInForeignCurrency)
									&& !Double
											.isNaN(priceOfGoodTypeInLocalCurrency)
									&& !Double
											.isNaN(priceOfForeignCurrencyInLocalCurrency)) {
								// inverse_CAR_in_USD -> correct_CAR_in_EUR =
								// 1.25
								double importPriceOfGoodTypeInLocalCurrency = priceOfGoodTypeInForeignCurrency
										* priceOfForeignCurrencyInLocalCurrency;

								Log.log(Trader.this,
										primaryCurrency.getIso4217Code()
												+ "/"
												+ goodType
												+ " = "
												+ Currency
														.round(priceOfGoodTypeInLocalCurrency)
												+ "; "
												+ foreignCurrency
														.getIso4217Code()
												+ "/"
												+ goodType
												+ " = "
												+ Currency
														.round(priceOfGoodTypeInForeignCurrency)
												+ "; "
												+ primaryCurrency
														.getIso4217Code()
												+ "/"
												+ foreignCurrency
														.getIso4217Code()
												+ " = "
												+ Currency
														.round(priceOfForeignCurrencyInLocalCurrency)
												+ " -> import price "
												+ primaryCurrency
														.getIso4217Code()
												+ "/"
												+ goodType
												+ " = "
												+ Currency
														.round(importPriceOfGoodTypeInLocalCurrency));

								if (MathUtil.greater(
										priceOfGoodTypeInLocalCurrency,
										importPriceOfGoodTypeInLocalCurrency)) {
									/*
									 * buy goods with foreign currency
									 */
									MarketFactory
											.getInstance()
											.buy(goodType,
													-1,
													budgetPerGoodTypeInForeignCurrency,
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

					/*
					 * refresh prices / offer in local currency
					 */
					for (GoodType goodType : GoodType.values()) {
						Trader.this.goodTypePricingBehaviours.get(goodType)
								.setNewPrice();
						MarketFactory.getInstance().removeAllSellingOffers(
								Trader.this, Trader.this.primaryCurrency,
								goodType);
						double amount = PropertyRegister.getInstance()
								.getBalance(Trader.this, goodType);
						MarketFactory.getInstance()
								.placeSettlementSellingOffer(
										goodType,
										Trader.this,
										Trader.this.transactionsBankAccount,
										amount,
										Trader.this.goodTypePricingBehaviours
												.get(goodType)
												.getCurrentPrice(),
										new SettlementMarketEvent());
						Trader.this.goodTypePricingBehaviours.get(goodType)
								.registerOfferedAmount(amount);
					}
				}
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