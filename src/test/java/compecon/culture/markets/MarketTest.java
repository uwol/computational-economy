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

package compecon.culture.markets;

import static org.junit.Assert.assertEquals;

import java.util.SortedMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import compecon.CompEconTestSupport;
import compecon.culture.markets.ordertypes.MarketOrder;
import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.household.Household;
import compecon.culture.sectors.industry.Factory;
import compecon.culture.sectors.state.law.property.PropertyRegister;
import compecon.culture.sectors.state.law.security.equity.JointStockCompany;
import compecon.culture.sectors.state.law.security.equity.Share;
import compecon.culture.sectors.trading.Trader;
import compecon.engine.MarketFactory;
import compecon.engine.dao.DAOFactory;
import compecon.engine.time.ITimeSystemEvent;
import compecon.nature.materia.GoodType;

public class MarketTest extends CompEconTestSupport {

	final double epsilon = 0.0001;

	@Before
	public void setUp() {
		super.setUp();
	}

	@After
	public void tearDown() {
		super.tearDown();
	}

	@Test
	public void offerGoodType() {
		// test market for good type
		Currency currency = Currency.EURO;
		GoodType goodType = GoodType.LABOURHOUR;

		Household household1_EUR = DAOFactory.getHouseholdDAO()
				.findAllByCurrency(currency).get(0);
		Household household2_EUR = DAOFactory.getHouseholdDAO()
				.findAllByCurrency(currency).get(1);
		Factory factory1_WHEAT_EUR = DAOFactory.getFactoryDAO()
				.findAllByCurrency(currency).get(0);

		assertEquals(Double.NaN, DAOFactory.getMarketOrderDAO()
				.findMarginalPrice(currency, goodType), epsilon);

		MarketFactory.getInstance().placeSellingOffer(goodType, household1_EUR,
				household1_EUR.getTransactionsBankAccount(), 10, 5);
		MarketFactory.getInstance().placeSellingOffer(goodType, household2_EUR,
				household2_EUR.getTransactionsBankAccount(), 10, 4);
		assertEquals(
				4.0,
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						goodType), epsilon);

		MarketFactory.getInstance().removeAllSellingOffers(household2_EUR);
		assertEquals(
				5.0,
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						goodType), epsilon);

		MarketFactory.getInstance().placeSellingOffer(goodType, household2_EUR,
				household2_EUR.getTransactionsBankAccount(), 10, 3);
		assertEquals(
				3.0,
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						goodType), epsilon);

		MarketFactory.getInstance().removeAllSellingOffers(household2_EUR,
				currency, goodType);
		assertEquals(
				5.0,
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						goodType), epsilon);

		MarketFactory.getInstance().placeSellingOffer(goodType, household2_EUR,
				household2_EUR.getTransactionsBankAccount(), 10, 3);
		assertEquals(
				3.0,
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						goodType), epsilon);

		SortedMap<MarketOrder, Double> marketOffers1 = MarketFactory
				.getInstance().findBestFulfillmentSet(currency, 20, Double.NaN,
						3, goodType);
		assertEquals(1, marketOffers1.size());

		SortedMap<MarketOrder, Double> marketOffers2 = MarketFactory
				.getInstance().findBestFulfillmentSet(currency, 20, Double.NaN,
						5, goodType);
		assertEquals(2, marketOffers2.size());

		MarketFactory.getInstance().buy(
				goodType,
				5,
				Double.NaN,
				8,
				factory1_WHEAT_EUR,
				factory1_WHEAT_EUR.getTransactionsBankAccount(),
				factory1_WHEAT_EUR.getBankPasswords().get(
						factory1_WHEAT_EUR.getPrimaryBank()));
		assertEquals(
				5,
				PropertyRegister.getInstance().getBalance(factory1_WHEAT_EUR,
						goodType), epsilon);
		assertEquals(-15.0, factory1_WHEAT_EUR.getTransactionsBankAccount()
				.getBalance(), epsilon);
	}

	@Test
	public void offerProperty() {
		Currency currency = Currency.EURO;

		Factory factory1_WHEAT_EUR = DAOFactory.getFactoryDAO()
				.findAllByCurrency(currency).get(0);
		Household household1_EUR = DAOFactory.getHouseholdDAO()
				.findAllByCurrency(currency).get(0);

		assertEquals(Double.NaN, DAOFactory.getMarketOrderDAO()
				.findMarginalPrice(currency, Share.class), epsilon);

		for (ITimeSystemEvent timeSystemEvent : factory1_WHEAT_EUR
				.getTimeSystemEvents()) {
			if (timeSystemEvent instanceof JointStockCompany.OfferSharesEvent)
				timeSystemEvent.onEvent();
		}

		assertEquals(
				0.0,
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						Share.class), epsilon);
		assertEquals(
				3,
				PropertyRegister.getInstance()
						.getProperties(factory1_WHEAT_EUR, Share.class).size());

		MarketFactory.getInstance().buy(
				Share.class,
				1,
				Double.NaN,
				Double.NaN,
				household1_EUR,
				household1_EUR.getTransactionsBankAccount(),
				household1_EUR.getBankPasswords().get(
						household1_EUR.getTransactionsBankAccount()
								.getManagingBank()));

		assertEquals(
				2,
				PropertyRegister.getInstance()
						.getProperties(factory1_WHEAT_EUR, Share.class).size());
		assertEquals(
				1,
				PropertyRegister.getInstance()
						.getProperties(household1_EUR, Share.class).size());

		MarketFactory.getInstance().removeAllSellingOffers(factory1_WHEAT_EUR);
		assertEquals(Double.NaN, DAOFactory.getMarketOrderDAO()
				.findMarginalPrice(currency, Share.class), epsilon);
	}

	@Test
	public void offerCurrency() {
		Currency currency = Currency.EURO;
		Currency commodityCurrency = Currency.USDOLLAR;

		CreditBank creditBank1_EUR = DAOFactory.getCreditBankDAO()
				.findAllByCurrency(currency).get(0);
		CreditBank creditBank2_EUR = DAOFactory.getCreditBankDAO()
				.findAllByCurrency(currency).get(1);
		Trader trader1_EUR = DAOFactory.getTraderDAO()
				.findAllByCurrency(currency).get(0);

		assertEquals(Double.NaN, DAOFactory.getMarketOrderDAO()
				.findMarginalPrice(currency, commodityCurrency), epsilon);

		MarketFactory.getInstance().placeSellingOffer(
				commodityCurrency,
				creditBank1_EUR,
				creditBank1_EUR.getTransactionsBankAccount(),
				10,
				2,
				creditBank1_EUR.getCurrencyTradeBankAccounts().get(
						commodityCurrency),
				creditBank1_EUR.getBankPasswords().get(
						creditBank1_EUR.getCurrencyTradeBankAccounts()
								.get(commodityCurrency).getManagingBank()));

		MarketFactory.getInstance().placeSellingOffer(
				commodityCurrency,
				creditBank2_EUR,
				creditBank2_EUR.getTransactionsBankAccount(),
				10,
				3,
				creditBank2_EUR.getCurrencyTradeBankAccounts().get(
						commodityCurrency),
				creditBank2_EUR.getBankPasswords().get(
						creditBank2_EUR.getCurrencyTradeBankAccounts()
								.get(commodityCurrency).getManagingBank()));
		assertEquals(
				2.0,
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						commodityCurrency), epsilon);

		MarketFactory.getInstance().removeAllSellingOffers(creditBank1_EUR);
		assertEquals(
				3,
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						commodityCurrency), epsilon);

		MarketFactory.getInstance().placeSellingOffer(
				commodityCurrency,
				creditBank1_EUR,
				creditBank1_EUR.getTransactionsBankAccount(),
				10,
				1,
				creditBank1_EUR.getCurrencyTradeBankAccounts().get(
						commodityCurrency),
				creditBank1_EUR.getBankPasswords().get(
						creditBank1_EUR.getCurrencyTradeBankAccounts()
								.get(commodityCurrency).getManagingBank()));
		assertEquals(
				1.0,
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						commodityCurrency), epsilon);

		MarketFactory.getInstance().removeAllSellingOffers(creditBank1_EUR,
				currency, commodityCurrency);
		assertEquals(
				3.0,
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						commodityCurrency), epsilon);

		MarketFactory.getInstance().placeSellingOffer(
				commodityCurrency,
				creditBank1_EUR,
				creditBank1_EUR.getTransactionsBankAccount(),
				10,
				1,
				creditBank1_EUR.getCurrencyTradeBankAccounts().get(
						commodityCurrency),
				creditBank1_EUR.getBankPasswords().get(
						creditBank1_EUR.getCurrencyTradeBankAccounts()
								.get(commodityCurrency).getManagingBank()));
		assertEquals(
				1.0,
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						commodityCurrency), epsilon);

		SortedMap<MarketOrder, Double> marketOffers1 = MarketFactory
				.getInstance().findBestFulfillmentSet(currency, 20, Double.NaN,
						1, commodityCurrency);
		assertEquals(1, marketOffers1.size());

		SortedMap<MarketOrder, Double> marketOffers2 = MarketFactory
				.getInstance().findBestFulfillmentSet(currency, 20, Double.NaN,
						5, commodityCurrency);
		assertEquals(2, marketOffers2.size());

		MarketFactory.getInstance().buy(
				commodityCurrency,
				5,
				Double.NaN,
				8,
				trader1_EUR,
				trader1_EUR.getTransactionsBankAccount(),
				trader1_EUR.getBankPasswords().get(
						trader1_EUR.getTransactionsBankAccount()
								.getManagingBank()),
				trader1_EUR.getTransactionForeignCurrencyAccounts().get(
						commodityCurrency));
		assertEquals(-5.0, trader1_EUR.getTransactionsBankAccount()
				.getBalance(), epsilon);
		assertEquals(5.0, trader1_EUR.getTransactionForeignCurrencyAccounts()
				.get(commodityCurrency).getBalance(), epsilon);
	}
}
