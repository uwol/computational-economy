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

package compecon.engine;

import compecon.culture.markets.ordertypes.MarketOrder;
import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.property.Property;
import compecon.engine.dao.DAOFactory;
import compecon.engine.util.HibernateUtil;
import compecon.nature.materia.GoodType;

public class MarketOrderFactory {
	public static MarketOrder newInstanceGoodTypeMarketOrder(GoodType goodType,
			Agent offeror, BankAccount offerorsBankAcount, double amount,
			double pricePerUnit, Currency currency) {

		MarketOrder marketOrder = new MarketOrder();
		marketOrder.setGoodType(goodType);
		marketOrder.setOfferor(offeror);
		marketOrder.setOfferorsBankAcount(offerorsBankAcount);
		marketOrder.setAmount(amount);
		marketOrder.setPricePerUnit(pricePerUnit);
		marketOrder.setCurrency(currency);

		DAOFactory.getMarketOrderDAO().save(marketOrder);
		HibernateUtil.flushSession();
		return marketOrder;
	}

	public static MarketOrder newInstancePropertyMarketOrder(Property property,
			Agent offeror, BankAccount offerorsBankAcount, double pricePerUnit,
			Currency currency) {
		MarketOrder marketOrder = new MarketOrder();
		marketOrder.setProperty(property);
		marketOrder.setOfferor(offeror);
		marketOrder.setOfferorsBankAcount(offerorsBankAcount);
		marketOrder.setPricePerUnit(pricePerUnit);
		marketOrder.setCurrency(currency);

		DAOFactory.getMarketOrderDAO().save(marketOrder);
		HibernateUtil.flushSession();
		return marketOrder;
	}

}
