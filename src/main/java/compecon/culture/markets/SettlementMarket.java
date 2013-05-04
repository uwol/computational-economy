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

package compecon.culture.markets;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;

import compecon.culture.sectors.financial.Bank;
import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.property.Property;
import compecon.culture.sectors.state.law.property.PropertyRegister;
import compecon.engine.Agent;
import compecon.engine.Log;
import compecon.engine.util.MathUtil;
import compecon.nature.materia.GoodType;

/**
 * The settlement market is a special market that transfers ownership of offered
 * goods and money, automatically.
 */
public class SettlementMarket extends Market {

	public interface ISettlementEvent {
		public void onEvent(GoodType goodType, double amount,
				double pricePerUnit, Currency currency);

		public void onEvent(Property property, double pricePerUnit,
				Currency currency);
	}

	protected Map<Agent, ISettlementEvent> settlementEventListeners = new HashMap<Agent, ISettlementEvent>();

	public void placeSettlementSellingOffer(GoodType goodType, Agent offeror,
			BankAccount offerorsBankAcount, double amount, double pricePerUnit,
			Currency currency, ISettlementEvent settlementEvent) {
		if (amount > 0) {
			this.placeSellingOffer(goodType, offeror, offerorsBankAcount,
					amount, pricePerUnit, currency);
			this.settlementEventListeners.put(offeror, settlementEvent);
		}
	}

	public void placeSettlementSellingOffer(Property property, Agent offeror,
			BankAccount offerorsBankAcount, double amount, double pricePerUnit,
			Currency currency, ISettlementEvent settlementEvent) {
		if (amount > 0) {
			this.placeSellingOffer(property, offeror, offerorsBankAcount,
					pricePerUnit, currency);
			this.settlementEventListeners.put(offeror, settlementEvent);
		}
	}

	public void removeAllSellingOffers(Agent offeror) {
		super.removeAllSellingOffers(offeror);
		this.settlementEventListeners.remove(offeror);
	}

	public Double[] buy(GoodType goodType, Currency currency,
			final double maxAmount, final double maxTotalPrice,
			final double maxPricePerUnit, Agent buyer,
			BankAccount buyersBankAccount, String buyersBankAccountPassword) {

		SortedMap<GoodTypeMarketOffer, Double> marketOffers = this
				.findBestFulfillmentSet(goodType, currency, maxAmount,
						maxTotalPrice, maxPricePerUnit);

		Bank buyersBank = buyersBankAccount.getManagingBank();
		PropertyRegister register = PropertyRegister.getInstance();

		double moneySpentSum = 0;
		double amountSum = 0;
		Double[] priceAndAmount = new Double[2];

		for (Entry<GoodTypeMarketOffer, Double> entry : marketOffers.entrySet()) {
			GoodTypeMarketOffer marketOffer = entry.getKey();
			double amount = entry.getValue();

			// transfer money
			buyersBank.transferMoney(buyersBankAccount,
					marketOffer.getOfferorsBankAcount(),
					amount * marketOffer.getPricePerUnit(),
					buyersBankAccountPassword, "price for " + amount
							+ " units of " + marketOffer.getGoodType());

			// GoodType
			register.transfer(marketOffer.getOfferor(), buyer,
					marketOffer.getGoodType(), amount);
			if (this.settlementEventListeners.containsKey(marketOffer
					.getOfferor())
					&& this.settlementEventListeners.get(marketOffer
							.getOfferor()) != null)
				this.settlementEventListeners.get(marketOffer.getOfferor())
						.onEvent(marketOffer.getGoodType(), amount,
								marketOffer.getPricePerUnit(),
								marketOffer.getCurrency());

			// register market tick
			Log.market_onTick(marketOffer.getPricePerUnit(),
					marketOffer.getGoodType(), marketOffer.getCurrency(),
					amount);

			marketOffer.decrementAmount(amount);
			if (marketOffer.getAmount() <= 0)
				this.removeSellingOffer(marketOffer);

			moneySpentSum += amount * marketOffer.getPricePerUnit();
			amountSum += amount;
		}

		priceAndAmount[0] = moneySpentSum;
		priceAndAmount[1] = amountSum;

		if (priceAndAmount[1] > 0)
			Log.log(buyer,
					buyer + " bought " + MathUtil.round(priceAndAmount[1])
							+ " units of " + goodType + " for "
							+ Currency.round(priceAndAmount[0]) + " "
							+ buyersBankAccount.getCurrency().getIso4217Code());

		return priceAndAmount;
	}

	public Double[] buy(Class<? extends Property> propertyClass,
			Currency currency, final double maxAmount,
			final double maxTotalPrice, final double maxPricePerUnit,
			Agent buyer, BankAccount buyersBankAccount,
			String buyersBankAccountPassword) {

		SortedSet<PropertyMarketOffer> marketOffers = this
				.findBestFulfillmentSet(propertyClass, currency, maxAmount,
						maxTotalPrice, maxPricePerUnit);

		Bank buyersBank = buyersBankAccount.getManagingBank();
		PropertyRegister register = PropertyRegister.getInstance();

		double moneySpentSum = 0;
		double amountSum = 0;
		Double[] priceAndAmount = new Double[2];

		for (PropertyMarketOffer marketOffer : marketOffers) {

			// transfer money
			buyersBank.transferMoney(buyersBankAccount,
					marketOffer.getOfferorsBankAcount(),
					marketOffer.getPricePerUnit(), buyersBankAccountPassword,
					"price for " + marketOffer.getProperty());

			// IProperty
			register.transfer(marketOffer.getOfferor(), buyer,
					marketOffer.getProperty(), 1);
			if (this.settlementEventListeners.containsKey(marketOffer
					.getOfferor())
					&& this.settlementEventListeners.get(marketOffer
							.getOfferor()) != null)
				this.settlementEventListeners.get(marketOffer.getOfferor())
						.onEvent(marketOffer.getProperty(),
								marketOffer.getPricePerUnit(),
								marketOffer.getCurrency());

			this.removeSellingOffer(marketOffer);

			moneySpentSum += marketOffer.getPricePerUnit();
			amountSum += 1;
		}

		priceAndAmount[0] = moneySpentSum;
		priceAndAmount[1] = amountSum;

		if (priceAndAmount[1] > 0)
			Log.log(buyer,
					buyer + " bought " + MathUtil.round(priceAndAmount[1])
							+ " units of " + propertyClass.getName() + " for "
							+ Currency.round(priceAndAmount[0]) + " "
							+ buyersBankAccount.getCurrency().getIso4217Code());

		return priceAndAmount;
	}
}
