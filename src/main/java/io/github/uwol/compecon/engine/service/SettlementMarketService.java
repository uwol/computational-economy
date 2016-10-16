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

package io.github.uwol.compecon.engine.service;

import io.github.uwol.compecon.economy.markets.MarketParticipant;
import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.property.Property;
import io.github.uwol.compecon.economy.sectors.financial.BankAccountDelegate;
import io.github.uwol.compecon.economy.sectors.financial.Currency;

public interface SettlementMarketService extends MarketService {

	/**
	 * @return total price and total amount
	 */
	public double[] buy(final Class<? extends Property> propertyClass,
			final double maxAmount, final double maxTotalPrice,
			final double maxPricePerUnit, final MarketParticipant buyer,
			final BankAccountDelegate buyersBankAccountDelegate);

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
	 * @return total price and total amount
	 */
	public double[] buy(
			final Currency commodityCurrency,
			final double maxAmount,
			final double maxTotalPrice,
			final double maxPricePerUnit,
			final MarketParticipant buyer,
			final BankAccountDelegate buyersBankAccountDelegate,
			final BankAccountDelegate buyersBankAccountForCommodityCurrencyDelegate);

	/**
	 * @return total price and total amount
	 */
	public double[] buy(final GoodType goodType, final double maxAmount,
			final double maxTotalPrice, final double maxPricePerUnit,
			final MarketParticipant buyer,
			final BankAccountDelegate buyersBankAccountDelegate);
}
