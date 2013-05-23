package compecon.culture.markets;

import static org.junit.Assert.assertEquals;

import java.util.SortedMap;

import org.junit.Test;

import compecon.culture.markets.ordertypes.MarketOrder;
import compecon.culture.sectors.financial.CentralBank;
import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.household.Household;
import compecon.culture.sectors.industry.Factory;
import compecon.culture.sectors.state.law.property.PropertyRegister;
import compecon.culture.sectors.state.law.security.equity.Share;
import compecon.engine.AgentFactory;
import compecon.engine.MarketFactory;
import compecon.engine.dao.DAOFactory;
import compecon.engine.util.HibernateUtil;
import compecon.nature.materia.GoodType;

public class MarketTest {
	@Test
	public void offerGoodTypeAndDeleteOffer() {

		double epsilon = 0.0001;

		Currency currency = Currency.EURO;
		GoodType goodType = GoodType.LABOURHOUR;

		// init database connection

		HibernateUtil.openSession();

		CentralBank centralBank = AgentFactory.getInstanceCentralBank(currency);
		centralBank.assertTransactionsBankAccount();
		CreditBank creditBank = AgentFactory.newInstanceCreditBank(currency);
		creditBank.assertCentralBankAccount();
		creditBank.assertTransactionsBankAccount();
		Household household1 = AgentFactory.newInstanceHousehold(Currency.EURO);
		household1.assertTransactionsBankAccount();
		Household household2 = AgentFactory.newInstanceHousehold(Currency.EURO);
		household2.assertTransactionsBankAccount();

		Factory factory1 = AgentFactory.newInstanceFactory(GoodType.WHEAT,
				Currency.EURO);
		factory1.assertTransactionsBankAccount();

		// test market for good type

		assertEquals(
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						goodType), Double.NaN, epsilon);

		MarketFactory.getInstance().placeSellingOffer(goodType, household1,
				household1.getTransactionsBankAccount(), 10, 5, currency);
		MarketFactory.getInstance().placeSellingOffer(goodType, household2,
				household2.getTransactionsBankAccount(), 10, 4, currency);
		assertEquals(
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						goodType), 4.0, epsilon);

		MarketFactory.getInstance().removeAllSellingOffers(household2);
		assertEquals(
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						goodType), 5.0, epsilon);

		MarketFactory.getInstance().placeSellingOffer(goodType, household2,
				household2.getTransactionsBankAccount(), 10, 3, currency);
		assertEquals(
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						goodType), 3.0, epsilon);

		MarketFactory.getInstance().removeAllSellingOffers(household2,
				currency, goodType);
		assertEquals(
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						goodType), 5.0, epsilon);

		MarketFactory.getInstance().placeSellingOffer(goodType, household2,
				household2.getTransactionsBankAccount(), 10, 3, currency);
		assertEquals(
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						goodType), 3.0, epsilon);

		SortedMap<MarketOrder, Double> marketOffers1 = MarketFactory
				.getInstance().findBestFulfillmentSet(goodType, currency, 20,
						-1, 3);
		assertEquals(marketOffers1.size(), 1);

		SortedMap<MarketOrder, Double> marketOffers2 = MarketFactory
				.getInstance().findBestFulfillmentSet(goodType, currency, 20,
						-1, 5);
		assertEquals(marketOffers2.size(), 2);

		MarketFactory.getInstance().buy(goodType, currency, 5, -1, 8, factory1,
				factory1.getTransactionsBankAccount(),
				factory1.getBankPasswords().get(factory1.getPrimaryBank()));
		assertEquals(
				PropertyRegister.getInstance().getBalance(factory1, goodType),
				5, epsilon);

		// test market for properties

		MarketFactory.getInstance().placeSellingOffer(new Share(), household1,
				household1.getTransactionsBankAccount(), 4, currency);
		MarketFactory.getInstance().placeSellingOffer(new Share(), household2,
				household2.getTransactionsBankAccount(), 3, currency);
		assertEquals(
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						Share.class), 3.0, epsilon);

		MarketFactory.getInstance().removeAllSellingOffers(household2);
		assertEquals(
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						Share.class), 4.0, epsilon);

		// close database conenction
		HibernateUtil.closeSession();
	}
}
