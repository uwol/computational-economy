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

	@Before
	public void setUp() {
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
		creditBank1_EURO.assureTransactionsForeignCurrencyBankAccounts();
		creditBank2_EURO.assureCentralBankAccount();
		creditBank2_EURO.assureTransactionsBankAccount();
		creditBank2_EURO.assureTransactionsForeignCurrencyBankAccounts();

		creditBank1_USD.assureCentralBankAccount();
		creditBank1_USD.assureTransactionsBankAccount();
		creditBank1_USD.assureTransactionsForeignCurrencyBankAccounts();
		creditBank2_USD.assureCentralBankAccount();
		creditBank2_USD.assureTransactionsBankAccount();
		creditBank2_USD.assureTransactionsForeignCurrencyBankAccounts();

		household1_EURO.assureTransactionsBankAccount();
		household2_EURO.assureTransactionsBankAccount();
		factory1_WHEAT_EURO.assureTransactionsBankAccount();
		trader1_EURO.assureTransactionsBankAccount();
		trader1_EURO.assureTransactionsForeignCurrencyBankAccounts();

		HibernateUtil.flushSession();
	}

	@After
	public void tearDown() {
		household1_EURO.deconstruct();
		trader1_EURO.deconstruct();
		household2_EURO.deconstruct();
		factory1_WHEAT_EURO.deconstruct();
		creditBank1_EURO.deconstruct();
		creditBank2_EURO.deconstruct();
		creditBank1_USD.deconstruct();
		creditBank2_USD.deconstruct();
		centralBank_EURO.deconstruct();
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

		MarketFactory.getInstance().placeSellingOffer(goodType,
				household1_EURO, household1_EURO.getTransactionsBankAccount(),
				10, 5);
		MarketFactory.getInstance().placeSellingOffer(goodType,
				household2_EURO, household2_EURO.getTransactionsBankAccount(),
				10, 4);
		assertEquals(
				4.0,
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						goodType), epsilon);

		MarketFactory.getInstance().removeAllSellingOffers(household2_EURO);
		assertEquals(
				5.0,
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						goodType), epsilon);

		MarketFactory.getInstance().placeSellingOffer(goodType,
				household2_EURO, household2_EURO.getTransactionsBankAccount(),
				10, 3);
		assertEquals(
				3.0,
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						goodType), epsilon);

		MarketFactory.getInstance().removeAllSellingOffers(household2_EURO,
				currency, goodType);
		assertEquals(
				5.0,
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						goodType), epsilon);

		MarketFactory.getInstance().placeSellingOffer(goodType,
				household2_EURO, household2_EURO.getTransactionsBankAccount(),
				10, 3);
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
				factory1_WHEAT_EURO,
				factory1_WHEAT_EURO.getTransactionsBankAccount(),
				factory1_WHEAT_EURO.getBankPasswords().get(
						factory1_WHEAT_EURO.getPrimaryBank()));
		assertEquals(
				5,
				PropertyRegister.getInstance().getBalance(factory1_WHEAT_EURO,
						goodType), epsilon);
	}

	@Test
	public void offerProperty() {
		Currency currency = Currency.EURO;

		assertEquals(Double.NaN, DAOFactory.getMarketOrderDAO()
				.findMarginalPrice(currency, Share.class), epsilon);

		for (ITimeSystemEvent timeSystemEvent : factory1_WHEAT_EURO
				.getTimeSystemEvents()) {
			if (timeSystemEvent instanceof JointStockCompany.OfferSharesEvent)
				timeSystemEvent.onEvent();
		}

		assertEquals(
				0.0,
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						Share.class), epsilon);

		MarketFactory.getInstance().removeAllSellingOffers(factory1_WHEAT_EURO);
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
				creditBank1_EURO,
				creditBank1_EURO.getTransactionsBankAccount(),
				10,
				2,
				creditBank1_EURO.getTransactionForeignCurrencyAccounts().get(
						commodityCurrency),
				creditBank1_EURO.getBankPasswords().get(
						creditBank1_EURO
								.getTransactionForeignCurrencyAccounts()
								.get(commodityCurrency).getManagingBank()));
		MarketFactory.getInstance().placeSellingOffer(
				commodityCurrency,
				creditBank2_EURO,
				creditBank2_EURO.getTransactionsBankAccount(),
				10,
				3,
				creditBank2_EURO.getTransactionForeignCurrencyAccounts().get(
						commodityCurrency),
				creditBank2_EURO.getBankPasswords().get(
						creditBank2_EURO
								.getTransactionForeignCurrencyAccounts()
								.get(commodityCurrency).getManagingBank()));
		assertEquals(
				2.0,
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						commodityCurrency), epsilon);

		MarketFactory.getInstance().removeAllSellingOffers(creditBank1_EURO);
		assertEquals(
				3,
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						commodityCurrency), epsilon);

		MarketFactory.getInstance().placeSellingOffer(
				commodityCurrency,
				creditBank1_EURO,
				creditBank1_EURO.getTransactionsBankAccount(),
				10,
				1,
				creditBank1_EURO.getTransactionForeignCurrencyAccounts().get(
						commodityCurrency),
				creditBank1_EURO.getBankPasswords().get(
						creditBank1_EURO
								.getTransactionForeignCurrencyAccounts()
								.get(commodityCurrency).getManagingBank()));
		assertEquals(
				1.0,
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						commodityCurrency), epsilon);

		MarketFactory.getInstance().removeAllSellingOffers(creditBank1_EURO,
				currency, commodityCurrency);
		assertEquals(
				3.0,
				DAOFactory.getMarketOrderDAO().findMarginalPrice(currency,
						commodityCurrency), epsilon);

		MarketFactory.getInstance().placeSellingOffer(
				commodityCurrency,
				creditBank1_EURO,
				creditBank1_EURO.getTransactionsBankAccount(),
				10,
				1,
				creditBank1_EURO.getTransactionForeignCurrencyAccounts().get(
						commodityCurrency),
				creditBank1_EURO.getBankPasswords().get(
						creditBank1_EURO
								.getTransactionForeignCurrencyAccounts()
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
				trader1_EURO,
				trader1_EURO.getTransactionsBankAccount(),
				trader1_EURO.getBankPasswords().get(
						trader1_EURO.getTransactionsBankAccount()
								.getManagingBank()),
				trader1_EURO.getTransactionForeignCurrencyAccounts().get(
						commodityCurrency));

		assertEquals(5, trader1_EURO.getTransactionForeignCurrencyAccounts()
				.get(commodityCurrency).getBalance(), epsilon);
	}
}
