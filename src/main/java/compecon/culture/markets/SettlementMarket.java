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

import compecon.culture.sectors.financial.Bank;
import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.property.IProperty;
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
				double pricePerUnit);

		public void onEvent(IProperty property, double amount,
				double pricePerUnit);
	}

	protected Map<Agent, ISettlementEvent> settlementEventListeners = new HashMap<Agent, ISettlementEvent>();

	public void placeSettlementSellingOffer(GoodType goodType, Agent offeror,
			Currency currency, BankAccount offerorsBankAcount, double amount,
			double pricePerUnit, ISettlementEvent settlementEvent) {
		if (amount > 0) {
			this.placeSellingOffer(goodType, offeror, currency,
					offerorsBankAcount, amount, pricePerUnit);
			this.settlementEventListeners.put(offeror, settlementEvent);
		}
	}

	public void placeSettlementSellingOffer(IProperty property, Agent offeror,
			Currency currency, BankAccount offerorsBankAcount, double amount,
			double pricePerUnit, ISettlementEvent settlementEvent) {
		if (amount > 0) {
			this.placeSellingOffer(property, offeror, currency,
					offerorsBankAcount, pricePerUnit);
			this.settlementEventListeners.put(offeror, settlementEvent);
		}
	}

	public void removeAllSellingOffers(Agent offeror) {
		super.removeAllSellingOffers(offeror);
		this.settlementEventListeners.remove(offeror);
	}

	/**
	 * @return priceAndAmount
	 */
	public Double[] buy(GoodType goodType, final double minAmount,
			final double maxAmount, final double maxTotalPrice,
			final double maxTotalPriceForMinAmount,
			final double maxPricePerUnit, Agent buyer,
			BankAccount buyersBankAccount, String buyersBankAccountPassword) {
		SortedMap<MarketOffer, Double> marketOffers = this
				.findBestFulfillmentSet(goodType,
						buyersBankAccount.getCurrency(), minAmount, maxAmount,
						maxTotalPrice, maxTotalPriceForMinAmount,
						maxPricePerUnit);
		Double[] priceAndAmount = this.buy(marketOffers, buyer,
				buyersBankAccount, buyersBankAccountPassword);
		if (priceAndAmount[1] > 0)
			Log.log(buyer,
					buyer + " bought " + MathUtil.round(priceAndAmount[1])
							+ " units of " + goodType + " for "
							+ Currency.round(priceAndAmount[0]) + " "
							+ buyersBankAccount.getCurrency().getIso4217Code());
		return priceAndAmount;
	}

	/**
	 * @return priceAndAmount
	 */
	public Double[] buy(Class<? extends IProperty> propertyClass,
			final double minAmount, final double maxAmount,
			final double maxTotalPrice, final double maxTotalPriceForMinAmount,
			final double maxPricePerUnit, Agent buyer,
			BankAccount buyersBankAccount, String buyersBankAccountPassword) {
		SortedMap<MarketOffer, Double> marketOffers = this
				.findBestFulfillmentSet(propertyClass,
						buyersBankAccount.getCurrency(), minAmount, maxAmount,
						maxTotalPrice, maxTotalPriceForMinAmount,
						maxPricePerUnit);
		Double[] priceAndAmount = this.buy(marketOffers, buyer,
				buyersBankAccount, buyersBankAccountPassword);
		if (priceAndAmount[1] > 0)
			Log.log(buyer,
					buyer + " bought " + MathUtil.round(priceAndAmount[1])
							+ " units of " + propertyClass.getName() + " for "
							+ Currency.round(priceAndAmount[0]) + " "
							+ buyersBankAccount.getCurrency().getIso4217Code());
		return priceAndAmount;
	}

	/**
	 * Currency is derived from buyers bank account
	 * 
	 * @return priceAndAmount
	 */
	private Double[] buy(SortedMap<MarketOffer, Double> marketOffers,
			Agent buyer, BankAccount buyersBankAccount,
			String buyersBankAccountPassword) {
		Double[] result = new Double[2];

		Bank buyersBank = buyersBankAccount.getManagingBank();
		PropertyRegister register = PropertyRegister.getInstance();

		double moneySpentSum = 0;
		double amountSum = 0;

		for (Entry<MarketOffer, Double> entry : marketOffers.entrySet()) {
			MarketOffer offer = entry.getKey();
			double amount = entry.getValue();

			// transfer money
			buyersBank.transferMoney(buyersBankAccount,
					offer.getOfferorsBankAcount(),
					amount * offer.getPricePerUnit(),
					buyersBankAccountPassword, "price for " + amount
							+ " units of " + offer.getProperty());

			// transfer ownership of property
			if (offer.getProperty() instanceof GoodType) {
				// GoodType
				register.transfer(offer.getOfferor(), buyer,
						(GoodType) offer.getProperty(), amount);
				if (this.settlementEventListeners.containsKey(offer
						.getOfferor())
						&& this.settlementEventListeners
								.get(offer.getOfferor()) != null)
					this.settlementEventListeners.get(offer.getOfferor())
							.onEvent((GoodType) offer.getProperty(), amount,
									offer.getPricePerUnit());

				// register market tick
				Log.market_onTick(offer.getPricePerUnit(),
						(GoodType) offer.getProperty(), offer.getCurrency(),
						amount);
			} else {
				// IProperty
				register.transfer(offer.getOfferor(), buyer,
						offer.getProperty(), amount);
				if (this.settlementEventListeners.containsKey(offer
						.getOfferor())
						&& this.settlementEventListeners
								.get(offer.getOfferor()) != null)
					this.settlementEventListeners.get(offer.getOfferor())
							.onEvent(offer.getProperty(), amount,
									offer.getPricePerUnit());

				// register market tick
				Log.market_onTick(offer.getPricePerUnit(), offer.getProperty(),
						offer.getCurrency(), amount);
			}

			offer.decrementAmount(amount);
			if (offer.getAmount() <= 0)
				this.removeSellingOffer(offer);

			moneySpentSum += amount * offer.getPricePerUnit();
			amountSum += amount;
		}

		result[0] = moneySpentSum;
		result[1] = amountSum;

		return result;
	}
}
