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

package compecon;

import compecon.culture.sectors.financial.CentralBank;
import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.household.Household;
import compecon.culture.sectors.industry.Factory;
import compecon.culture.sectors.trading.Trader;
import compecon.engine.AgentFactory;
import compecon.engine.dao.DAOFactory;
import compecon.engine.util.HibernateUtil;
import compecon.nature.materia.GoodType;

public abstract class CompEconTestSupport {
	protected void setUp() {
		// init database connection

		HibernateUtil.openSession();

		for (Currency currency : Currency.values()) {
			AgentFactory.getInstanceCentralBank(currency);
			AgentFactory.newInstanceCreditBank(currency);
			AgentFactory.newInstanceCreditBank(currency);
			AgentFactory.newInstanceFactory(GoodType.WHEAT, currency);
			AgentFactory.newInstanceHousehold(currency);
			AgentFactory.newInstanceHousehold(currency);
			AgentFactory.newInstanceTrader(currency);
		}

		for (CentralBank centralBank : DAOFactory.getCentralBankDAO().findAll()) {
			centralBank.assureTransactionsBankAccount();
		}

		for (CreditBank creditBank : DAOFactory.getCreditBankDAO().findAll()) {
			creditBank.assureCentralBankAccount();
			creditBank.assureTransactionsBankAccount();
			creditBank.assureCurrencyTradeBankAccounts();
		}

		for (Factory factory : DAOFactory.getFactoryDAO().findAll()) {
			factory.assureTransactionsBankAccount();
		}

		for (Household household : DAOFactory.getHouseholdDAO().findAll()) {
			household.assureTransactionsBankAccount();
			household.assureSavingsBankAccount();
		}

		for (Trader trader : DAOFactory.getTraderDAO().findAll()) {
			trader.assureTransactionsBankAccount();
			trader.assureGoodsTradeBankAccounts();
		}

		HibernateUtil.flushSession();
	}

	protected void tearDown() {
		for (Household household : DAOFactory.getHouseholdDAO().findAll()) {
			household.deconstruct();
		}

		for (Trader trader : DAOFactory.getTraderDAO().findAll()) {
			trader.deconstruct();
		}

		for (Factory factory : DAOFactory.getFactoryDAO().findAll()) {
			factory.deconstruct();
		}

		for (CreditBank creditBank : DAOFactory.getCreditBankDAO().findAll()) {
			creditBank.deconstruct();
		}

		for (CentralBank centralBank : DAOFactory.getCentralBankDAO().findAll()) {
			centralBank.deconstruct();
		}

		HibernateUtil.flushSession();

		// close database conenction
		HibernateUtil.closeSession();
	}
}
