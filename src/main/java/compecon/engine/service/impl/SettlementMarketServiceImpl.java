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

package compecon.engine.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import compecon.economy.agent.Agent;
import compecon.economy.markets.MarketOrder;
import compecon.economy.property.Property;
import compecon.economy.sectors.financial.Bank;
import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.service.SettlementMarketService;
import compecon.engine.util.MathUtil;
import compecon.materia.GoodType;

/**
 * The settlement market is a special market that transfers ownership of offered
 * goods and money, automatically.
 */
public class SettlementMarketServiceImpl extends MarketServiceImpl implements
		SettlementMarketService {

	protected Map<Agent, SettlementEvent> settlementEventListeners = new HashMap<Agent, SettlementEvent>();

	public void placeSettlementSellingOffer(final GoodType goodType,
			final Agent offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double amount, final double pricePerUnit,
			final SettlementEvent settlementEvent) {
		if (amount > 0) {
			this.placeSellingOffer(goodType, offeror,
					offerorsBankAcountDelegate, amount, pricePerUnit);
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
	public void placeSettlementSellingOffer(
			final Currency commodityCurrency,
			final Agent offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double amount,
			final double pricePerUnit,
			final BankAccountDelegate commodityCurrencyOfferorsBankAcountDelegate,
			final SettlementEvent settlementEvent) {
		if (amount > 0) {
			this.placeSellingOffer(commodityCurrency, offeror,
					offerorsBankAcountDelegate, amount, pricePerUnit,
					commodityCurrencyOfferorsBankAcountDelegate);
			if (settlementEvent != null)
				this.settlementEventListeners.put(offeror, settlementEvent);
		}
	}

	public void placeSettlementSellingOffer(final Property property,
			final Agent offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double pricePerUnit, final SettlementEvent settlementEvent) {
		this.placeSellingOffer(property, offeror, offerorsBankAcountDelegate,
				pricePerUnit);
		if (settlementEvent != null)
			this.settlementEventListeners.put(offeror, settlementEvent);
	}

	public void removeAllSellingOffers(final Agent offeror) {
		super.removeAllSellingOffers(offeror);
		this.settlementEventListeners.remove(offeror);
	}

	public double[] buy(final GoodType goodType, final double maxAmount,
			final double maxTotalPrice, final double maxPricePerUnit,
			final Agent buyer,
			final BankAccountDelegate buyersBankAccountDelegate) {
		return this.buy(goodType, null, null, maxAmount, maxTotalPrice,
				maxPricePerUnit, goodType.getWholeNumber(), buyer,
				buyersBankAccountDelegate, null);
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
	 * @param buyersBankAccountForCommodityCurrency
	 *            Bank account that should receive the bought foreign currency
	 */
	public double[] buy(
			final Currency commodityCurrency,
			final double maxAmount,
			final double maxTotalPrice,
			final double maxPricePerUnit,
			final Agent buyer,
			final BankAccountDelegate buyersBankAccountDelegate,
			final BankAccountDelegate buyersBankAccountForCommodityCurrencyDelegate) {
		return this.buy(null, commodityCurrency, null, maxAmount,
				maxTotalPrice, maxPricePerUnit, false, buyer,
				buyersBankAccountDelegate,
				buyersBankAccountForCommodityCurrencyDelegate);
	}

	public double[] buy(final Class<? extends Property> propertyClass,
			final double maxAmount, final double maxTotalPrice,
			final double maxPricePerUnit, final Agent buyer,
			final BankAccountDelegate buyersBankAccountDelegate) {
		return this.buy(null, null, propertyClass, maxAmount, maxTotalPrice,
				maxPricePerUnit, true, buyer, buyersBankAccountDelegate, null);
	}

	/**
	 * @return total price and total amount
	 */
	protected double[] buy(
			final GoodType goodType,
			final Currency commodityCurrency,
			final Class<? extends Property> propertyClass,
			final double maxAmount,
			final double maxTotalPrice,
			final double maxPricePerUnit,
			final boolean wholeNumber,
			final Agent buyer,
			final BankAccountDelegate buyersBankAccountDelegate,
			final BankAccountDelegate buyersBankAccountForCommodityCurrencyDelegate) {

		final SortedMap<MarketOrder, Double> marketOffers = this
				.findBestFulfillmentSet(buyersBankAccountDelegate
						.getBankAccount().getCurrency(), maxAmount,
						maxTotalPrice, maxPricePerUnit, wholeNumber, goodType,
						commodityCurrency, propertyClass);

		Bank buyersBank = buyersBankAccountDelegate.getBankAccount()
				.getManagingBank();

		double moneySpentSum = 0;
		double amountSum = 0;
		double[] priceAndAmount = new double[2];

		for (Entry<MarketOrder, Double> entry : marketOffers.entrySet()) {
			MarketOrder marketOffer = entry.getKey();
			double amount = entry.getValue();

			// is the offeror' bank account is identical to the buyer's bank
			// account
			if (buyersBankAccountDelegate.getBankAccount() == marketOffer
					.getOfferorsBankAcountDelegate().getBankAccount()) {
				continue;
			}

			// is the offeror is identical to the buyer
			if (buyersBankAccountDelegate.getBankAccount().getOwner() == marketOffer
					.getOfferorsBankAcountDelegate().getBankAccount()
					.getOwner()) {
				continue;
			}

			// transfer money
			buyersBank.transferMoney(
					buyersBankAccountDelegate.getBankAccount(), marketOffer
							.getOfferorsBankAcountDelegate().getBankAccount(),
					amount * marketOffer.getPricePerUnit(), "price for "
							+ MathUtil.round(amount) + " units of "
							+ marketOffer.getCommodity());

			// transfer ownership
			switch (marketOffer.getCommodityType()) {
			case GOODTYPE:
				ApplicationContext
						.getInstance()
						.getPropertyService()
						.transferGoodTypeAmount(marketOffer.getOfferor(),
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
									marketOffer.getOfferorsBankAcountDelegate()
											.getBankAccount().getCurrency());
				// register market tick
				getLog().market_onTick(
						marketOffer.getPricePerUnit(),
						marketOffer.getGoodType(),
						marketOffer.getOfferorsBankAcountDelegate()
								.getBankAccount().getCurrency(), amount);
				break;
			case CURRENCY:
				Bank bank = marketOffer
						.getCommodityCurrencyOfferorsBankAccountDelegate()
						.getBankAccount().getManagingBank();
				bank.transferMoney(
						marketOffer
								.getCommodityCurrencyOfferorsBankAccountDelegate()
								.getBankAccount(),
						buyersBankAccountForCommodityCurrencyDelegate
								.getBankAccount(),
						amount,
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
									marketOffer.getOfferorsBankAcountDelegate()
											.getBankAccount().getCurrency());
				// register market tick
				getLog().market_onTick(
						marketOffer.getPricePerUnit(),
						marketOffer.getCommodityCurrency(),
						marketOffer.getOfferorsBankAcountDelegate()
								.getBankAccount().getCurrency(), amount);
				break;
			case PROPERTY:
				ApplicationContext
						.getInstance()
						.getPropertyService()
						.transferProperty(marketOffer.getOfferor(), buyer,
								marketOffer.getProperty());
				if (this.settlementEventListeners.containsKey(marketOffer
						.getOfferor())
						&& this.settlementEventListeners.get(marketOffer
								.getOfferor()) != null)
					this.settlementEventListeners.get(marketOffer.getOfferor())
							.onEvent(
									marketOffer.getProperty(),
									marketOffer.getPricePerUnit(),
									marketOffer.getOfferorsBankAcountDelegate()
											.getBankAccount().getCurrency());
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

		if (getLog().isAgentSelectedByClient(buyer)) {
			if (priceAndAmount[1] > 0) {
				getLog().log(
						buyer,
						"bought "
								+ MathUtil.round(priceAndAmount[1])
								+ " units of "
								+ this.determineCommodityName(goodType,
										commodityCurrency, propertyClass)
								+ " for "
								+ Currency.formatMoneySum(priceAndAmount[0])
								+ " "
								+ buyersBankAccountDelegate.getBankAccount()
										.getCurrency().getIso4217Code()
								+ " under constraints [maxAmount: "
								+ MathUtil.round(maxAmount)
								+ ", maxTotalPrice: "
								+ Currency.formatMoneySum(maxTotalPrice)
								+ " "
								+ buyersBankAccountDelegate.getBankAccount()
										.getCurrency().getIso4217Code()
								+ ", maxPricePerUnit: "
								+ Currency.formatMoneySum(maxPricePerUnit)
								+ " "
								+ buyersBankAccountDelegate.getBankAccount()
										.getCurrency().getIso4217Code() + "]");
			} else {
				getLog().log(
						buyer,
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
								+ buyersBankAccountDelegate.getBankAccount()
										.getCurrency().getIso4217Code()
								+ ", maxPricePerUnit: "
								+ Currency.formatMoneySum(maxPricePerUnit)
								+ " "
								+ buyersBankAccountDelegate.getBankAccount()
										.getCurrency().getIso4217Code() + "]");
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
