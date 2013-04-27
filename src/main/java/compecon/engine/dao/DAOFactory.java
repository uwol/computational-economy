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

import compecon.culture.sectors.financial.CentralBank;
import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.household.Household;
import compecon.culture.sectors.industry.Factory;
import compecon.culture.sectors.state.State;
import compecon.engine.util.ConfigurationUtil;

public class DAOFactory {

	protected static ICentralBankDAO centralBankDAO;

	protected static ICreditBankDAO creditBankDAO;

	protected static IHouseholdDAO householdDAO;

	protected static IFactoryDAO factoryDAO;

	protected static IStateDAO stateDAO;

	static {
		if (ConfigurationUtil.getActivateDb()) {
			centralBankDAO = new compecon.engine.dao.hibernate.CentralBankDAO();
			creditBankDAO = new compecon.engine.dao.hibernate.CreditBankDAO();
			householdDAO = new compecon.engine.dao.hibernate.HouseholdDAO();
			factoryDAO = new compecon.engine.dao.hibernate.FactoryDAO();
			stateDAO = new compecon.engine.dao.hibernate.StateDAO();
		} else {
			centralBankDAO = new compecon.engine.dao.noaction.CentralBankDAO();
			creditBankDAO = new compecon.engine.dao.noaction.CreditBankDAO();
			householdDAO = new compecon.engine.dao.noaction.HouseholdDAO();
			factoryDAO = new compecon.engine.dao.noaction.FactoryDAO();
			stateDAO = new compecon.engine.dao.noaction.StateDAO();
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

	public static IStateDAO getStateDAO() {
		return stateDAO;
	}

	// ---------------------------------

	public static interface ICentralBankDAO extends
			IGenericDAO<CentralBank, Long> {
		public CentralBank findByCurrency(Currency currency);
	}

	public static interface ICreditBankDAO extends
			IGenericDAO<CreditBank, Long> {
	}

	public static interface IHouseholdDAO extends IGenericDAO<Household, Long> {
	}

	public static interface IFactoryDAO extends IGenericDAO<Factory, Long> {
	}

	public static interface IStateDAO extends IGenericDAO<State, Long> {
		public State findByCurrency(Currency currency);
	}
}
