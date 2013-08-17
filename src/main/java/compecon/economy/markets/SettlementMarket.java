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

package compecon.economy.markets;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import compecon.economy.markets.ordertypes.MarketOrder;
import compecon.economy.sectors.financial.Bank;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.law.property.Property;
import compecon.economy.sectors.state.law.property.PropertyRegister;
import compecon.engine.Agent;
import compecon.engine.jmx.Log;
import compecon.engine.util.MathUtil;
import compecon.materia.GoodType;

/**
 * The settlement market is a special market that transfers ownership of offered
 * goods and money, automatically.
 */
public class SettlementMarket extends Market {

	public interface ISettlementEvent {
		public void onEvent(GoodType goodType, double amount,
				double pricePerUnit, Currency currency);

		public void onEvent(Currency commodityCurrency, double amount,
				double pricePerUnit, Currency currency);

		public void onEvent(Property property, double pricePerUnit,
				Currency currency);
	}

	protected Map<Agent, ISettlementEvent> settlementEventListeners = new HashMap<Agent, ISettlementEvent>();

	public void placeSettlementSellingOffer(GoodType goodType, Agent offeror,
			BankAccount offerorsBankAcount, double amount, double pricePerUnit,
			ISettlementEvent settlementEvent) {
		if (amount > 0) {
			this.placeSellingOffer(goodType, offeror, offerorsBankAcount,
					amount, pricePerUnit);
			if (settlementEvent != null)
				this.settlementEventListeners.put(offeror, settlementEvent);
		}
	}

	/**
	 * Place settlement selling offer for a certain amount of money.
	 * 
	 * @param commodityCurrency
	 *            Currency of money to be offered, e.g. EURO.
	 * @param offeror
	 * @param offerorsBankAcount
	 *            Bank account of offeror; offerorsBankAcount.currency (e.g.
	 *            USD) != commodityCurrency (e.g. EURO)
	 * @param amount
	 *            Money amount
	 * @param pricePerUnit
	 * @param settlementEvent
	 */
	public void placeSettlementSellingOffer(Currency commodityCurrency,
			Agent offeror, BankAccount offerorsBankAcount, double amount,
			double pricePerUnit,
			BankAccount commodityCurrencyOfferorsBankAcount,
			String commodityCurrencyOfferorsBankAcountPassword,
			ISettlementEvent settlementEvent) {
		if (amount > 0) {
			this.placeSellingOffer(commodityCurrency, offeror,
					offerorsBankAcount, amount, pricePerUnit,
					commodityCurrencyOfferorsBankAcount,
					commodityCurrencyOfferorsBankAcountPassword);
			if (settlementEvent != null)
				this.settlementEventListeners.put(offeror, settlementEvent);
		}
	}

	public void placeSettlementSellingOffer(Property property, Agent offeror,
			BankAccount offerorsBankAcount, double pricePerUnit,
			ISettlementEvent settlementEvent) {
		this.placeSellingOffer(property, offeror, offerorsBankAcount,
				pricePerUnit);
		if (settlementEvent != null)
			this.settlementEventListeners.put(offeror, settlementEvent);
	}

	public void removeAllSellingOffers(Agent offeror) {
		super.removeAllSellingOffers(offeror);
		this.settlementEventListeners.remove(offeror);
	}

	public double[] buy(final GoodType goodType, final double maxAmount,
			final double maxTotalPrice, final double maxPricePerUnit,
			final Agent buyer, final BankAccount buyersBankAccount,
			final String buyersBankAccountPassword) {
		return this.buy(goodType, null, null, maxAmount, maxTotalPrice,
				maxPricePerUnit, goodType.getWholeNumber(), buyer,
				buyersBankAccount, buyersBankAccountPassword, null);
	}

	/**
	 * Buy a foreign currency with another currency
	 * 
	 * @param commodityCurrency
	 *            Currency to buy
	 * @param maxAmount
	 *            Amount to buy
	 * @param maxTotalPrice
	 *            Max amount to pay in local currency
	 * @param maxPricePerUnit
	 *            Max price of foreign currency in local currency
	 * @param buyer
	 * @param buyersBankAccount
	 * @param buyersBankAccountPassword
	 * @param buyersBankAccountForCommodityCurrency
	 *            Bank account that should receive the bought foreign currency
	 */
	public double[] buy(final Currency commodityCurrency,
			final double maxAmount, final double maxTotalPrice,
			final double maxPricePerUnit, final Agent buyer,
			final BankAccount buyersBankAccount,
			final String buyersBankAccountPassword,
			final BankAccount buyersBankAccountForCommodityCurrency) {
		return this.buy(null, commodityCurrency, null, maxAmount,
				maxTotalPrice, maxPricePerUnit, false, buyer,
				buyersBankAccount, buyersBankAccountPassword,
				buyersBankAccountForCommodityCurrency);
	}

	public double[] buy(final Class<? extends Property> propertyClass,
			final double maxAmount, final double maxTotalPrice,
			final double maxPricePerUnit, final Agent buyer,
			final BankAccount buyersBankAccount,
			final String buyersBankAccountPassword) {
		return this.buy(null, null, propertyClass, maxAmount, maxTotalPrice,
				maxPricePerUnit, true, buyer, buyersBankAccount,
				buyersBankAccountPassword, null);
	}

	protected double[] buy(final GoodType goodType,
			final Currency commodityCurrency,
			final Class<? extends Property> propertyClass,
			final double maxAmount, final double maxTotalPrice,
			final double maxPricePerUnit, final boolean wholeNumber,
			final Agent buyer, final BankAccount buyersBankAccount,
			final String buyersBankAccountPassword,
			final BankAccount buyersBankAccountForCommodityCurrency) {

		SortedMap<MarketOrder, Double> marketOffers = this
				.findBestFulfillmentSet(buyersBankAccount.getCurrency(),
						maxAmount, maxTotalPrice, maxPricePerUnit, wholeNumber,
						goodType, commodityCurrency, propertyClass);

		Bank buyersBank = buyersBankAccount.getManagingBank();
		PropertyRegister register = PropertyRegister.getInstance();

		double moneySpentSum = 0;
		double amountSum = 0;
		double[] priceAndAmount = new double[2];

		for (Entry<MarketOrder, Double> entry : marketOffers.entrySet()) {
			MarketOrder marketOffer = entry.getKey();
			double amount = entry.getValue();

			// is the offeror' bank account is identical to the buyer's bank
			// account
			if (buyersBankAccount == marketOffer.getOfferorsBankAcount()) {
				continue;
			}

			// is the offeror is identical to the buyer
			if (buyersBankAccount.getOwner() == marketOffer
					.getOfferorsBankAcount().getOwner()) {
				continue;
			}

			// transfer money
			buyersBank.transferMoney(buyersBankAccount,
					marketOffer.getOfferorsBankAcount(),
					amount * marketOffer.getPricePerUnit(),
					buyersBankAccountPassword,
					"price for " + MathUtil.round(amount) + " units of "
							+ marketOffer.getCommodity());

			// transfer ownership
			switch (marketOffer.getCommodityType()) {
			case GOODTYPE:
				register.transferGoodTypeAmount(marketOffer.getOfferor(),
						buyer, marketOffer.getGoodType(), amount);
				if (this.settlementEventListeners.containsKey(marketOffer
						.getOfferor())
						&& this.settlementEventListeners.get(marketOffer
								.getOfferor()) != null)
					this.settlementEventListeners.get(marketOffer.getOfferor())
							.onEvent(
									marketOffer.getGoodType(),
									amount,
									marketOffer.getPricePerUnit(),
									marketOffer.getOfferorsBankAcount()
											.getCurrency());
				// register market tick
				Log.market_onTick(marketOffer.getPricePerUnit(), marketOffer
						.getGoodType(), marketOffer.getOfferorsBankAcount()
						.getCurrency(), amount);
				break;
			case CURRENCY:
				Bank bank = marketOffer
						.getCommodityCurrencyOfferorsBankAccount()
						.getManagingBank();
				bank.transferMoney(
						marketOffer.getCommodityCurrencyOfferorsBankAccount(),
						buyersBankAccountForCommodityCurrency,
						amount,
						marketOffer
								.getCommodityCurrencyOfferorsBankAccountPassword(),
						"transfer of " + Currency.formatMoneySum(amount)
								+ " units of commoditycurrency "
								+ marketOffer.getCommodity());
				if (this.settlementEventListeners.containsKey(marketOffer
						.getOfferor())
						&& this.settlementEventListeners.get(marketOffer
								.getOfferor()) != null)
					this.settlementEventListeners.get(marketOffer.getOfferor())
							.onEvent(
									marketOffer.getCommodityCurrency(),
									amount,
									marketOffer.getPricePerUnit(),
									marketOffer.getOfferorsBankAcount()
											.getCurrency());
				// register market tick
				Log.market_onTick(marketOffer.getPricePerUnit(), marketOffer
						.getCommodityCurrency(), marketOffer
						.getOfferorsBankAcount().getCurrency(), amount);
				break;
			case PROPERTY:
				register.transferProperty(marketOffer.getOfferor(), buyer,
						marketOffer.getProperty());
				if (this.settlementEventListeners.containsKey(marketOffer
						.getOfferor())
						&& this.settlementEventListeners.get(marketOffer
								.getOfferor()) != null)
					this.settlementEventListeners.get(marketOffer.getOfferor())
							.onEvent(
									marketOffer.getProperty(),
									marketOffer.getPricePerUnit(),
									marketOffer.getOfferorsBankAcount()
											.getCurrency());
				break;
			default:
				throw new RuntimeException("CommodityType unknown");
			}

			marketOffer.decrementAmount(amount);
			if (marketOffer.getAmount() <= 0)
				this.removeSellingOffer(marketOffer);

			moneySpentSum += amount * marketOffer.getPricePerUnit();
			amountSum += amount;
		}

		priceAndAmount[0] = moneySpentSum;
		priceAndAmount[1] = amountSum;

		if (Log.isAgentSelectedByClient(buyer)) {
			if (priceAndAmount[1] > 0) {
				Log.log(buyer,
						"bought "
								+ MathUtil.round(priceAndAmount[1])
								+ " units of "
								+ this.determineCommodityName(goodType,
										commodityCurrency, propertyClass)
								+ " for "
								+ Currency.formatMoneySum(priceAndAmount[0])
								+ " "
								+ buyersBankAccount.getCurrency()
										.getIso4217Code()
								+ " under constraints [maxAmount: "
								+ MathUtil.round(maxAmount)
								+ ", maxTotalPrice: "
								+ Currency.formatMoneySum(maxTotalPrice)
								+ " "
								+ buyersBankAccount.getCurrency()
										.getIso4217Code()
								+ ", maxPricePerUnit: "
								+ Currency.formatMoneySum(maxPricePerUnit)
								+ " "
								+ buyersBankAccount.getCurrency()
										.getIso4217Code() + "]");
			} else {
				Log.log(buyer,
						"cannot buy "
								+ this.determineCommodityName(goodType,
										commodityCurrency, propertyClass)
								+ ", since no matching offers for "
								+ this.determineCommodityName(goodType,
										commodityCurrency, propertyClass)
								+ " under constraints [maxAmount: "
								+ MathUtil.round(maxAmount)
								+ ", maxTotalPrice: "
								+ Currency.formatMoneySum(maxTotalPrice)
								+ " "
								+ buyersBankAccount.getCurrency()
										.getIso4217Code()
								+ ", maxPricePerUnit: "
								+ Currency.formatMoneySum(maxPricePerUnit)
								+ " "
								+ buyersBankAccount.getCurrency()
										.getIso4217Code() + "]");
			}
		}

		return priceAndAmount;
	}

	private String determineCommodityName(GoodType goodType,
			Currency commodityCurrency, Class<? extends Property> propertyClass) {
		if (commodityCurrency != null)
			return commodityCurrency.getIso4217Code();
		if (propertyClass != null)
			return propertyClass.getSimpleName();
		return goodType.toString();
	}
}
