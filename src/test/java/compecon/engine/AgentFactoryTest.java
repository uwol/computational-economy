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

import org.junit.Test;

import compecon.culture.sectors.financial.CentralBank;
import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.household.Household;
import compecon.culture.sectors.industry.Factory;
import compecon.culture.sectors.trading.Trader;
import compecon.engine.util.HibernateUtil;
import compecon.nature.materia.GoodType;

public class AgentFactoryTest {
	double epsilon = 0.0001;

	CentralBank centralBank_EURO;
	CentralBank centralBank_USD;
	CreditBank creditBank1_EURO;
	CreditBank creditBank2_EURO;
	CreditBank creditBank1_USD;
	CreditBank creditBank2_USD;
	Household household1_EURO;
	Household household2_EURO;
	Factory factory1_WHEAT_EURO;
	Trader trader1_EURO;

	@Test
	public void createAndDeleteAgents() {
		// init database connection

		HibernateUtil.openSession();

		centralBank_EURO = AgentFactory.getInstanceCentralBank(Currency.EURO);
		centralBank_USD = AgentFactory
				.getInstanceCentralBank(Currency.USDOLLAR);
		creditBank1_EURO = AgentFactory.newInstanceCreditBank(Currency.EURO);
		creditBank2_EURO = AgentFactory.newInstanceCreditBank(Currency.EURO);
		creditBank1_USD = AgentFactory.newInstanceCreditBank(Currency.USDOLLAR);
		creditBank2_USD = AgentFactory.newInstanceCreditBank(Currency.USDOLLAR);
		household1_EURO = AgentFactory.newInstanceHousehold(Currency.EURO);
		household2_EURO = AgentFactory.newInstanceHousehold(Currency.EURO);
		factory1_WHEAT_EURO = AgentFactory.newInstanceFactory(GoodType.WHEAT,
				Currency.EURO);
		trader1_EURO = AgentFactory.newInstanceTrader(Currency.EURO);

		centralBank_EURO.assureTransactionsBankAccount();
		centralBank_USD.assureTransactionsBankAccount();

		creditBank1_EURO.assureCentralBankAccount();
		creditBank1_EURO.assureTransactionsBankAccount();
		creditBank1_EURO.assureCurrencyTradeBankAccounts();
		creditBank2_EURO.assureCentralBankAccount();
		creditBank2_EURO.assureTransactionsBankAccount();
		creditBank2_EURO.assureCurrencyTradeBankAccounts();

		creditBank1_USD.assureCentralBankAccount();
		creditBank1_USD.assureTransactionsBankAccount();
		creditBank1_USD.assureCurrencyTradeBankAccounts();
		creditBank2_USD.assureCentralBankAccount();
		creditBank2_USD.assureTransactionsBankAccount();
		creditBank2_USD.assureCurrencyTradeBankAccounts();

		trader1_EURO.assureTransactionsBankAccount();
		trader1_EURO.assureGoodsTradeBankAccounts();

		factory1_WHEAT_EURO.assureTransactionsBankAccount();

		household1_EURO.assureTransactionsBankAccount();
		household2_EURO.assureTransactionsBankAccount();

		HibernateUtil.flushSession();

		household1_EURO.deconstruct();
		household2_EURO.deconstruct();
		factory1_WHEAT_EURO.deconstruct();
		trader1_EURO.deconstruct();
		creditBank1_EURO.deconstruct();
		HibernateUtil.flushSession();
		creditBank2_EURO.deconstruct();
		creditBank1_USD.deconstruct();
		creditBank2_USD.deconstruct();
		centralBank_EURO.deconstruct();
		centralBank_USD.deconstruct();

		HibernateUtil.flushSession();

		// close database conenction
		HibernateUtil.closeSession();
	}
}
