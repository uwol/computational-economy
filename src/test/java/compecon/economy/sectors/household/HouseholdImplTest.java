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

package compecon.economy.sectors.household;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import compecon.CompEconTestSupport;
import compecon.economy.materia.GoodType;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.impl.HouseholdImpl;
import compecon.economy.sectors.industry.Factory;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.timesystem.TimeSystemEvent;

public class HouseholdImplTest extends CompEconTestSupport {

	@Before
	public void setup() throws IOException {
		super.setUpApplicationContext(testConfigurationPropertiesFilename);
		ApplicationContext.getInstance().getAgentFactory()
				.constructAgentsFromConfiguration();
		ApplicationContext.getInstance().getConfiguration().householdConfig.retirementSaving = false;
	}

	@Override
	@After
	public void tearDown() {
		super.tearDown();
	}

	@Test
	public void testDailyLifeEvent() {
		final Currency currency = Currency.EURO;

		final Household household1_EUR = ApplicationContext.getInstance()
				.getAgentService().findHouseholds(currency).get(0);
		final Household household2_EUR = ApplicationContext.getInstance()
				.getAgentService().findHouseholds(currency).get(1);

		final Factory factory1_EUR = ApplicationContext.getInstance()
				.getAgentService().findFactories(currency, GoodType.WHEAT)
				.get(0);
		final Factory factory2_EUR = ApplicationContext.getInstance()
				.getAgentService().findFactories(currency, GoodType.COAL)
				.get(0);

		// provide money to household 1
		household2_EUR
				.getBankAccountTransactionsDelegate()
				.getBankAccount()
				.getManagingBank()
				.transferMoney(
						household2_EUR.getBankAccountTransactionsDelegate()
								.getBankAccount(),
						household1_EUR.getBankAccountTransactionsDelegate()
								.getBankAccount(), 10.0, "");

		assertEquals(10.0, household1_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getBalance(), epsilon);

		// provide WHEAT on good market
		ApplicationContext.getInstance().getPropertyService()
				.incrementGoodTypeAmount(factory1_EUR, GoodType.WHEAT, 20.0);
		ApplicationContext
				.getInstance()
				.getMarketService()
				.placeSellingOffer(GoodType.WHEAT, factory1_EUR,
						factory1_EUR.getBankAccountTransactionsDelegate(),
						20.0, 1.0);

		// provide COAL on good market
		ApplicationContext.getInstance().getPropertyService()
				.incrementGoodTypeAmount(factory2_EUR, GoodType.COAL, 20.0);
		ApplicationContext
				.getInstance()
				.getMarketService()
				.placeSellingOffer(GoodType.COAL, factory2_EUR,
						factory2_EUR.getBankAccountTransactionsDelegate(),
						20.0, 1.0);

		final int currentAge = household1_EUR.getAgeInDays();

		for (final TimeSystemEvent timeSystemEvent : household1_EUR
				.getTimeSystemEvents()) {
			if (timeSystemEvent instanceof HouseholdImpl.DailyLifeEvent) {
				// household 1 buys goods from factories and consumes them
				timeSystemEvent.onEvent();
			}
		}

		assertEquals(currentAge + 1, household1_EUR.getAgeInDays());

		assertEquals(0.0, household1_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getBalance(), epsilon);

		assertEquals(15.0,
				ApplicationContext.getInstance().getPropertyService()
						.getGoodTypeBalance(factory1_EUR, GoodType.WHEAT),
				epsilon);
		assertEquals(5.0, factory1_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getBalance(), epsilon);

		assertEquals(15.0,
				ApplicationContext.getInstance().getPropertyService()
						.getGoodTypeBalance(factory2_EUR, GoodType.COAL),
				epsilon);
		assertEquals(5.0, factory2_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getBalance(), epsilon);

		assertEquals(
				1.552464255,
				ApplicationContext.getInstance().getModelRegistry()
						.getNationalEconomyModel(currency).householdsModel.utilityModel.utilityOutputModel
						.getValue(), epsilon);
	}
}
