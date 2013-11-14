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

package compecon.engine.factory;

import compecon.economy.agent.Agent;
import compecon.economy.markets.MarketOrder;
import compecon.economy.property.Property;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.Currency;
import compecon.materia.GoodType;

public interface MarketOrderFactory {

	public MarketOrder newInstanceGoodTypeMarketOrder(final GoodType goodType,
			final Agent offeror, final BankAccount offerorsBankAcount,
			final double amount, final double pricePerUnit);

	public MarketOrder newInstanceCurrencyMarketOrder(
			final Currency currencyToBeOffered, final Agent offeror,
			final BankAccount offerorsBankAcount, final double amount,
			final double pricePerUnit,
			final BankAccount commodityCurrencyOfferorsBankAcount);

	public MarketOrder newInstancePropertyMarketOrder(final Property property,
			final Agent offeror, final BankAccount offerorsBankAcount,
			final double pricePerUnit);

}
