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

package compecon.economy.sectors.industry;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import compecon.CompEconTestSupport;
import compecon.economy.materia.GoodType;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.economy.sectors.industry.impl.FactoryImpl;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.timesystem.TimeSystemEvent;

public class FactoryImplTest extends CompEconTestSupport {

	@Before
	public void setup() throws IOException {
		super.setUpApplicationContext(testConfigurationPropertiesFilename);
		ApplicationContext.getInstance().getAgentFactory()
				.constructAgentsFromConfiguration();
	}

	@Override
	@After
	public void tearDown() {
		super.tearDown();
	}

	@Test
	public void testProductionEvent() {
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

		// provide LABOURHOUR on good market
		ApplicationContext
				.getInstance()
				.getMarketService()
				.placeSellingOffer(GoodType.LABOURHOUR, household1_EUR,
						household1_EUR.getBankAccountTransactionsDelegate(),
						16.0, 1.0);
		ApplicationContext
				.getInstance()
				.getMarketService()
				.placeSellingOffer(GoodType.LABOURHOUR, household2_EUR,
						household2_EUR.getBankAccountTransactionsDelegate(),
						16.0, 1.0);

		for (final TimeSystemEvent timeSystemEvent : factory1_EUR
				.getTimeSystemEvents()) {
			if (timeSystemEvent instanceof FactoryImpl.ProductionEvent) {
				// factory 1 buys LABOURHOUR from households and produces WHEAT
				timeSystemEvent.onEvent();
			}
		}

		assertEquals(0.0, ApplicationContext.getInstance().getPropertyService()
				.getGoodTypeBalance(factory1_EUR, GoodType.LABOURHOUR), epsilon);
		assertEquals(-32.0, factory1_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getBalance(), epsilon);

		assertEquals(0.0, ApplicationContext.getInstance().getPropertyService()
				.getGoodTypeBalance(household1_EUR, GoodType.LABOURHOUR),
				epsilon);
		assertEquals(16.0, household1_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getBalance(), epsilon);

		assertEquals(0.0, ApplicationContext.getInstance().getPropertyService()
				.getGoodTypeBalance(household2_EUR, GoodType.LABOURHOUR),
				epsilon);
		assertEquals(16.0, household2_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getBalance(), epsilon);

		assertEquals(475.6828460010886,
				ApplicationContext.getInstance().getModelRegistry()
						.getNationalEconomyModel(currency).industryModels
						.get(GoodType.WHEAT).outputModel.getValue(), epsilon);
	}

	@Test
	public void testProductionEventWithCapital() {
		// deactivate capital depreciation
		ApplicationContext.getInstance().getConfiguration().factoryConfig.capitalDepreciationRatioPerPeriod = 0.0;

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

		// provide LABOURHOUR on good market
		ApplicationContext
				.getInstance()
				.getMarketService()
				.placeSellingOffer(GoodType.LABOURHOUR, household1_EUR,
						household1_EUR.getBankAccountTransactionsDelegate(),
						16.0, 1.0);
		ApplicationContext
				.getInstance()
				.getMarketService()
				.placeSellingOffer(GoodType.LABOURHOUR, household2_EUR,
						household2_EUR.getBankAccountTransactionsDelegate(),
						16.0, 1.0);

		// provide capital MACHINE to factory
		ApplicationContext.getInstance().getPropertyService()
				.incrementGoodTypeAmount(factory1_EUR, GoodType.MACHINE, 10.0);

		for (final TimeSystemEvent timeSystemEvent : factory1_EUR
				.getTimeSystemEvents()) {
			if (timeSystemEvent instanceof FactoryImpl.ProductionEvent) {
				// factory 1 buys LABOURHOUR from households and produces WHEAT
				timeSystemEvent.onEvent();
			}
		}

		assertEquals(10.0,
				ApplicationContext.getInstance().getPropertyService()
						.getGoodTypeBalance(factory1_EUR, GoodType.MACHINE),
				epsilon);
		assertEquals(0.0, ApplicationContext.getInstance().getPropertyService()
				.getGoodTypeBalance(factory1_EUR, GoodType.LABOURHOUR), epsilon);
		assertEquals(-32.0, factory1_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getBalance(), epsilon);

		assertEquals(0.0, ApplicationContext.getInstance().getPropertyService()
				.getGoodTypeBalance(household1_EUR, GoodType.LABOURHOUR),
				epsilon);
		assertEquals(16.0, household1_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getBalance(), epsilon);

		assertEquals(0.0, ApplicationContext.getInstance().getPropertyService()
				.getGoodTypeBalance(household2_EUR, GoodType.LABOURHOUR),
				epsilon);
		assertEquals(16.0, household2_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getBalance(), epsilon);

		assertEquals(514.6970863360276,
				ApplicationContext.getInstance().getModelRegistry()
						.getNationalEconomyModel(currency).industryModels
						.get(GoodType.WHEAT).outputModel.getValue(), epsilon);
	}

	@Test
	public void testProductionEventWithInventory() {
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

		// provide LABOURHOUR on good market
		ApplicationContext
				.getInstance()
				.getMarketService()
				.placeSellingOffer(GoodType.LABOURHOUR, household1_EUR,
						household1_EUR.getBankAccountTransactionsDelegate(),
						16.0, 1.0);
		ApplicationContext
				.getInstance()
				.getMarketService()
				.placeSellingOffer(GoodType.LABOURHOUR, household2_EUR,
						household2_EUR.getBankAccountTransactionsDelegate(),
						16.0, 1.0);

		// provide LABOURHOUR "stocks" to factory 1
		ApplicationContext
				.getInstance()
				.getPropertyService()
				.incrementGoodTypeAmount(factory1_EUR, GoodType.LABOURHOUR,
						10.0);

		for (final TimeSystemEvent timeSystemEvent : factory1_EUR
				.getTimeSystemEvents()) {
			if (timeSystemEvent instanceof FactoryImpl.ProductionEvent) {
				// factory 1 buys LABOURHOUR from households and produces WHEAT
				timeSystemEvent.onEvent();
			}
		}

		assertEquals(0.0, ApplicationContext.getInstance().getPropertyService()
				.getGoodTypeBalance(factory1_EUR, GoodType.LABOURHOUR), epsilon);
		assertEquals(-32.0, factory1_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getBalance(), epsilon);

		assertEquals(0.0, ApplicationContext.getInstance().getPropertyService()
				.getGoodTypeBalance(household1_EUR, GoodType.LABOURHOUR),
				epsilon);
		assertEquals(16.0, household1_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getBalance(), epsilon);

		assertEquals(0.0, ApplicationContext.getInstance().getPropertyService()
				.getGoodTypeBalance(household2_EUR, GoodType.LABOURHOUR),
				epsilon);
		assertEquals(16.0, household2_EUR.getBankAccountTransactionsDelegate()
				.getBankAccount().getBalance(), epsilon);

		assertEquals(567.651831072576,
				ApplicationContext.getInstance().getModelRegistry()
						.getNationalEconomyModel(currency).industryModels
						.get(GoodType.WHEAT).outputModel.getValue(), epsilon);
	}
}
