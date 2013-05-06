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

import compecon.culture.markets.GoodTypeMarketOffer;
import compecon.culture.markets.PropertyMarketOffer;
import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.property.Property;
import compecon.engine.dao.DAOFactory;
import compecon.engine.util.HibernateUtil;
import compecon.nature.materia.GoodType;

public class MarketOfferFactory {
	public static GoodTypeMarketOffer newInstanceGoodTypeMarketOffer(
			GoodType goodType, Agent offeror, BankAccount offerorsBankAcount,
			double amount, double pricePerUnit, Currency currency) {

		GoodTypeMarketOffer marketOffer = new GoodTypeMarketOffer();
		marketOffer.setGoodType(goodType);
		marketOffer.setOfferor(offeror);
		marketOffer.setOfferorsBankAcount(offerorsBankAcount);
		marketOffer.setAmount(amount);
		marketOffer.setPricePerUnit(pricePerUnit);
		marketOffer.setCurrency(currency);

		DAOFactory.getGoodTypeMarketOfferDAO().save(marketOffer);
		HibernateUtil.flushSession();
		return marketOffer;
	}

	public static PropertyMarketOffer newInstancePropertyMarketOffer(
			Property property, Agent offeror, BankAccount offerorsBankAcount,
			double pricePerUnit, Currency currency) {
		PropertyMarketOffer marketOffer = new PropertyMarketOffer();
		marketOffer.setProperty(property);
		marketOffer.setOfferor(offeror);
		marketOffer.setOfferorsBankAcount(offerorsBankAcount);
		marketOffer.setPricePerUnit(pricePerUnit);
		marketOffer.setCurrency(currency);

		DAOFactory.getPropertyMarketOfferDAO().save(marketOffer);
		HibernateUtil.flushSession();
		return marketOffer;
	}

}
