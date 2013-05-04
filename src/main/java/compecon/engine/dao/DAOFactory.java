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

package compecon.engine.dao;

import java.util.Iterator;
import java.util.List;

import compecon.culture.markets.GoodTypeMarketOffer;
import compecon.culture.markets.PropertyMarketOffer;
import compecon.culture.sectors.financial.CentralBank;
import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.household.Household;
import compecon.culture.sectors.industry.Factory;
import compecon.culture.sectors.state.State;
import compecon.culture.sectors.state.law.property.Property;
import compecon.engine.Agent;
import compecon.engine.util.ConfigurationUtil;
import compecon.nature.materia.GoodType;

public class DAOFactory {

	protected static ICentralBankDAO centralBankDAO;

	protected static ICreditBankDAO creditBankDAO;

	protected static IHouseholdDAO householdDAO;

	protected static IFactoryDAO factoryDAO;

	protected static IGoodTypeMarketOfferDAO goodTypeMarketOfferDAO;

	protected static IPropertyDAO propertyDAO;

	protected static IPropertyMarketOfferDAO propertyMarketOfferDAO;

	protected static IStateDAO stateDAO;

	static {
		if (ConfigurationUtil.getActivateDb()) {
			centralBankDAO = new compecon.engine.dao.hibernate.CentralBankDAO();
			creditBankDAO = new compecon.engine.dao.hibernate.CreditBankDAO();
			householdDAO = new compecon.engine.dao.hibernate.HouseholdDAO();
			factoryDAO = new compecon.engine.dao.hibernate.FactoryDAO();
			goodTypeMarketOfferDAO = new compecon.engine.dao.hibernate.GoodTypeMarketOfferDAO();
			propertyDAO = new compecon.engine.dao.hibernate.PropertyDAO();
			propertyMarketOfferDAO = new compecon.engine.dao.hibernate.PropertyMarketOfferDAO();
			stateDAO = new compecon.engine.dao.hibernate.StateDAO();
		} else {
			centralBankDAO = new compecon.engine.dao.inmemory.CentralBankDAO();
			creditBankDAO = new compecon.engine.dao.inmemory.CreditBankDAO();
			householdDAO = new compecon.engine.dao.inmemory.HouseholdDAO();
			factoryDAO = new compecon.engine.dao.inmemory.FactoryDAO();
			goodTypeMarketOfferDAO = new compecon.engine.dao.inmemory.GoodTypeMarketOfferDAO();
			propertyDAO = new compecon.engine.dao.inmemory.PropertyDAO();
			propertyMarketOfferDAO = new compecon.engine.dao.inmemory.PropertyMarketOfferDAO();

			stateDAO = new compecon.engine.dao.inmemory.StateDAO();
		}
	}

	// ---------------------------------

	public static ICentralBankDAO getCentralBankDAO() {
		return centralBankDAO;
	}

	public static ICreditBankDAO getCreditBankDAO() {
		return creditBankDAO;
	}

	public static IHouseholdDAO getHouseholdDAO() {
		return householdDAO;
	}

	public static IFactoryDAO getFactoryDAO() {
		return factoryDAO;
	}

	public static IGoodTypeMarketOfferDAO getGoodTypeMarketOfferDAO() {
		return goodTypeMarketOfferDAO;
	}

	public static IPropertyDAO getPropertyDAO() {
		return propertyDAO;
	}

	public static IPropertyMarketOfferDAO getPropertyMarketOfferDAO() {
		return propertyMarketOfferDAO;
	}

	public static IStateDAO getStateDAO() {
		return stateDAO;
	}

	// ---------------------------------

	public static interface ICentralBankDAO extends IGenericDAO<CentralBank> {
		public CentralBank findByCurrency(Currency currency);
	}

	public static interface ICreditBankDAO extends IGenericDAO<CreditBank> {
		public CreditBank findRandom(Currency currency);

		public List<CreditBank> findAll(Currency currency);
	}

	public static interface IHouseholdDAO extends IGenericDAO<Household> {
	}

	public static interface IFactoryDAO extends IGenericDAO<Factory> {
	}

	public static interface IGoodTypeMarketOfferDAO extends
			IGenericDAO<GoodTypeMarketOffer> {
		public void deleteAllSellingOffers(Agent offeror);

		public void deleteAllSellingOffers(Agent offeror, Currency currency,
				GoodType goodType);

		public double findMarginalPrice(Currency currency, GoodType goodType);

		public Iterator<GoodTypeMarketOffer> getIterator(GoodType goodType,
				Currency currency);
	}

	public static interface IPropertyDAO extends IGenericDAO<Property> {
	}

	public static interface IPropertyMarketOfferDAO extends
			IGenericDAO<PropertyMarketOffer> {
		public void deleteAllSellingOffers(Agent offeror);

		public void deleteAllSellingOffers(Agent offeror, Currency currency,
				Class<? extends Property> propertyClass);

		public double findMarginalPrice(Currency currency,
				Class<? extends Property> propertyClass);

		public Iterator<PropertyMarketOffer> getIterator(
				Class<? extends Property> propertyClass, Currency currency);
	}

	public static interface IStateDAO extends IGenericDAO<State> {
		public State findByCurrency(Currency currency);
	}

}
