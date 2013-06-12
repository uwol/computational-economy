package compecon.culture.markets;

import static org.junit.Assert.assertEquals;

import java.util.SortedMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import compecon.culture.markets.ordertypes.MarketOrder;
import compecon.culture.sectors.financial.CentralBank;
import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.household.Household;
import compecon.culture.sectors.industry.Factory;
import compecon.culture.sectors.state.law.property.PropertyRegister;
import compecon.culture.sectors.state.law.security.equity.JointStockCompany;
import compecon.culture.sectors.state.law.security.equity.Share;
import compecon.culture.sectors.trading.Trader;
import compecon.engine.AgentFactory;
import compecon.engine.MarketFactory;
import compecon.engine.dao.DAOFactory;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.util.HibernateUtil;
import compecon.nature.materia.GoodType;

public class MarketTest {

	double epsilon = 0.0001;

	CentralBank centralBank_EUR;
	CentralBank centralBank_USD;
	CreditBank creditBank1_EUR;
	CreditBank creditBank2_EUR;
	CreditBank creditBank1_USD;
	CreditBank creditBank2_USD;
	Household household1_EUR;
	Household household2_EUR;
	Factory factory1_WHEAT_EUR;
	Trader trader1_EUR;

	@Before
	public void setUp() {
		// init database connection

		HibernateUtil.openSession();

		centralBank_EUR = AgentFactory.getInstanceCentralBank(Currency.EURO);
		centralBank_USD = AgentFactory
				.getInstanceCentralBank(Currency.USDOLLAR);
		creditBank1_EUR = AgentFactory.newInstanceCreditBank(Currency.EURO);
		creditBank2_EUR = AgentFactory.newInstanceCreditBank(Currency.EURO);
		creditBank1_USD = AgentFactory.newInstanceCreditBank(Currency.USDOLLAR);
		creditBank2_USD = AgentFactory.newInstanceCreditBank(Currency.USDOLLAR);
		household1_EUR = AgentFactory.newInstanceHousehold(Currency.EURO);
		household2_EUR = AgentFactory.newInstanceHousehold(Currency.EURO);
		factory1_WHEAT_EUR = AgentFactory.newInstanceFactory(GoodType.WHEAT,
				Currency.EURO);
		trader1_EUR = AgentFactory.newInstanceTrader(Currency.EURO);

		centralBank_EUR.assureTransactionsBankAccount();
		centralBank_USD.assureTransactionsBankAccount();

		creditBank1_EUR.assureCentralBankAccount();
		creditBank1_EUR.assureTransactionsBankAccount();
		creditBank1_EUR.assureCurrencyTradeBankAccounts();
		creditBank2_EUR.assureCentralBankAccount();
		creditBank2_EUR.assureTransactionsBankAccount();
		creditBank2_EUR.assureCurrencyTradeBankAccounts();

		creditBank1_USD.assureCentralBankAccount();
		creditBank1_USD.assureTransactionsBankAccount();
		creditBank1_USD.assureCurrencyTradeBankAccounts();
		creditBank2_USD.assureCentralBankAccount();
		creditBank2_USD.assureTransactionsBankAccount();
		creditBank2_USD.assureCurrencyTradeBankAccounts();

		household1_EUR.assureTransactionsBankAccount();
		household2_EUR.assureTransactionsBankAccount();
		factory1_WHEAT_EUR.assureTransactionsBankAccount();
		trader1_EUR.assureTransactionsBankAccount();
		trader1_EUR.assureGoodsTradeBankAccounts();

		HibernateUtil.flushSession();
	}

	@After
	public void tearDown() {
		household1_EUR.deconstruct();
		trader1_EUR.deconstruct();
		household2_EUR.deconstruct();
		factory1_WHEAT_EUR.deconstruct();
		creditBank1_EUR.deconstruct();
		creditBank2_EUR.deconstruct();
		creditBank1_USD.deconstruct();
		creditBank2_USD.deconstruct();
		centralBank_EUR.deconstruct();
		centralBank_USD.deconstruct();

		HibernateUtil.flushSession();

		// close database conenction
		HibernateUtil.closeSession();
	}

	@Test
	public void offerGoodType() {
		// test market for good type

		Currency currency = Currency.EURO;
		GoodType goodType = GoodType.LABOURHOUR;

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
				.getInstance().findBestFulfillmentSet(currency, 20, -1, 3,
						goodType);
		assertEquals(1, marketOffers1.size());

		SortedMap<MarketOrder, Double> marketOffers2 = MarketFactory
				.getInstance().findBestFulfillmentSet(currency, 20, -1, 5,
						goodType);
		assertEquals(2, marketOffers2.size());

		MarketFactory.getInstance().buy(
				goodType,
				5,
				-1,
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

		MarketFactory.getInstance().removeAllSellingOffers(factory1_WHEAT_EUR);
		assertEquals(Double.NaN, DAOFactory.getMarketOrderDAO()
				.findMarginalPrice(currency, Share.class), epsilon);
	}

	@Test
	public void offerCurrency() {
		Currency currency = Currency.EURO;
		Currency commodityCurrency = Currency.USDOLLAR;

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
				.getInstance().findBestFulfillmentSet(currency, 20, -1, 1,
						commodityCurrency);
		assertEquals(1, marketOffers1.size());

		SortedMap<MarketOrder, Double> marketOffers2 = MarketFactory
				.getInstance().findBestFulfillmentSet(currency, 20, -1, 5,
						commodityCurrency);
		assertEquals(2, marketOffers2.size());

		MarketFactory.getInstance().buy(
				commodityCurrency,
				5,
				-1,
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
