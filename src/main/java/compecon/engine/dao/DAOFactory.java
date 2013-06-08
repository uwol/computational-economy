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

import compecon.culture.markets.ordertypes.MarketOrder;
import compecon.culture.sectors.financial.Bank;
import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.CentralBank;
import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.household.Household;
import compecon.culture.sectors.industry.Factory;
import compecon.culture.sectors.state.State;
import compecon.culture.sectors.state.law.property.Property;
import compecon.culture.sectors.trading.Trader;
import compecon.engine.Agent;
import compecon.engine.util.ConfigurationUtil;
import compecon.nature.materia.GoodType;

public class DAOFactory {

	protected static IBankAccountDAO bankAccountDAO;

	protected static ICentralBankDAO centralBankDAO;

	protected static ICreditBankDAO creditBankDAO;

	protected static IHouseholdDAO householdDAO;

	protected static IFactoryDAO factoryDAO;

	protected static IMarketOrderDAO marketOrderDAO;

	protected static IPropertyDAO propertyDAO;

	protected static IStateDAO stateDAO;

	protected static ITraderDAO traderDAO;

	static {
		if (ConfigurationUtil.getActivateDb()) {
			bankAccountDAO = new compecon.engine.dao.hibernate.BankAccountDAO();
			centralBankDAO = new compecon.engine.dao.hibernate.CentralBankDAO();
			creditBankDAO = new compecon.engine.dao.hibernate.CreditBankDAO();
			householdDAO = new compecon.engine.dao.hibernate.HouseholdDAO();
			factoryDAO = new compecon.engine.dao.hibernate.FactoryDAO();
			marketOrderDAO = new compecon.engine.dao.hibernate.MarketOrderDAO();
			propertyDAO = new compecon.engine.dao.hibernate.PropertyDAO();
			stateDAO = new compecon.engine.dao.hibernate.StateDAO();
			traderDAO = new compecon.engine.dao.hibernate.TraderDAO();
		} else {
			bankAccountDAO = new compecon.engine.dao.inmemory.BankAccountDAO();
			centralBankDAO = new compecon.engine.dao.inmemory.CentralBankDAO();
			creditBankDAO = new compecon.engine.dao.inmemory.CreditBankDAO();
			householdDAO = new compecon.engine.dao.inmemory.HouseholdDAO();
			factoryDAO = new compecon.engine.dao.inmemory.FactoryDAO();
			marketOrderDAO = new compecon.engine.dao.inmemory.MarketOrderDAO();
			propertyDAO = new compecon.engine.dao.inmemory.PropertyDAO();
			stateDAO = new compecon.engine.dao.inmemory.StateDAO();
			traderDAO = new compecon.engine.dao.inmemory.TraderDAO();
		}
	}

	// ---------------------------------

	public static IBankAccountDAO getBankAccountDAO() {
		return bankAccountDAO;
	}

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

	public static IMarketOrderDAO getMarketOrderDAO() {
		return marketOrderDAO;
	}

	public static IPropertyDAO getPropertyDAO() {
		return propertyDAO;
	}

	public static IStateDAO getStateDAO() {
		return stateDAO;
	}

	public static ITraderDAO getTraderDAO() {
		return traderDAO;
	}

	// ---------------------------------

	public static interface IBankAccountDAO extends IGenericDAO<BankAccount> {
		public List<BankAccount> findAllBankAccountsManagedByBank(
				Bank managingBank);

		public List<BankAccount> findAllBankAccountsOfAgent(Agent owner);

		public List<BankAccount> findAll(Bank managingBank, Agent owner);

		public List<BankAccount> findAll(Bank managingBank, Agent owner,
				Currency currency);

		public void deleteAllBankAccounts(Bank managingBank);

		public void deleteAllBankAccounts(Bank managingBank, Agent owner);
	}

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

	public static interface IMarketOrderDAO extends IGenericDAO<MarketOrder> {
		public void deleteAllSellingOrders(Agent offeror);

		public void deleteAllSellingOrders(Agent offeror, Currency currency,
				GoodType goodType);

		public void deleteAllSellingOrders(Agent offeror, Currency currency,
				Currency commodityCurrency);

		public void deleteAllSellingOrders(Agent offeror, Currency currency,
				Class<? extends Property> propertyClass);

		public double findMarginalPrice(Currency currency, GoodType goodType);

		public double findMarginalPrice(Currency currency,
				Currency commodityCurrency);

		public double findMarginalPrice(Currency currency,
				Class<? extends Property> propertyClass);

		public Iterator<MarketOrder> getIterator(Currency currency,
				GoodType goodType);

		public Iterator<MarketOrder> getIterator(Currency currency,
				Currency commodityCurrency);

		public Iterator<MarketOrder> getIterator(Currency currency,
				Class<? extends Property> propertyClass);
	}

	public static interface IPropertyDAO extends IGenericDAO<Property> {
	}

	public static interface IStateDAO extends IGenericDAO<State> {
		public State findByCurrency(Currency currency);
	}

	public static interface ITraderDAO extends IGenericDAO<Trader> {
	}
}
