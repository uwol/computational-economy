package compecon.engine.factory.impl;

import compecon.economy.agent.Agent;
import compecon.economy.markets.impl.MarketOrderImpl;
import compecon.economy.materia.GoodType;
import compecon.economy.property.Property;
import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.factory.MarketOrderFactory;
import compecon.engine.util.HibernateUtil;

public class MarketOrderImplFactoryImpl implements MarketOrderFactory {

	public MarketOrderImpl newInstanceGoodTypeMarketOrder(
			final GoodType goodType, final Agent offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double amount, final double pricePerUnit) {

		MarketOrderImpl marketOrder = new MarketOrderImpl();
		marketOrder.setCurrency(offerorsBankAcountDelegate.getBankAccount()
				.getCurrency());
		marketOrder.setGoodType(goodType);
		marketOrder.setOfferor(offeror);
		marketOrder.setOfferorsBankAcountDelegate(offerorsBankAcountDelegate);
		marketOrder.setAmount(amount);
		marketOrder.setPricePerUnit(pricePerUnit);

		ApplicationContext.getInstance().getMarketOrderDAO().save(marketOrder);
		HibernateUtil.flushSession();
		return marketOrder;
	}

	public MarketOrderImpl newInstanceCurrencyMarketOrder(
			final Currency currencyToBeOffered,
			final Agent offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double amount,
			final double pricePerUnit,
			final BankAccountDelegate commodityCurrencyOfferorsBankAcountDelegate) {

		MarketOrderImpl marketOrder = new MarketOrderImpl();
		marketOrder.setCurrency(offerorsBankAcountDelegate.getBankAccount()
				.getCurrency());
		marketOrder.setCommodityCurrency(currencyToBeOffered);
		marketOrder.setOfferor(offeror);
		marketOrder.setOfferorsBankAcountDelegate(offerorsBankAcountDelegate);
		marketOrder.setAmount(amount);
		marketOrder.setPricePerUnit(pricePerUnit);
		marketOrder
				.setCommodityCurrencyOfferorsBankAccountDelegate(commodityCurrencyOfferorsBankAcountDelegate);

		ApplicationContext.getInstance().getMarketOrderDAO().save(marketOrder);
		HibernateUtil.flushSession();
		return marketOrder;
	}

	public MarketOrderImpl newInstancePropertyMarketOrder(
			final Property property, final Agent offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double pricePerUnit) {
		MarketOrderImpl marketOrder = new MarketOrderImpl();
		marketOrder.setCurrency(offerorsBankAcountDelegate.getBankAccount()
				.getCurrency());
		marketOrder.setProperty(property);
		marketOrder.setOfferor(offeror);
		marketOrder.setOfferorsBankAcountDelegate(offerorsBankAcountDelegate);
		marketOrder.setAmount(1);
		marketOrder.setPricePerUnit(pricePerUnit);

		ApplicationContext.getInstance().getMarketOrderDAO().save(marketOrder);
		HibernateUtil.flushSession();
		return marketOrder;
	}
}
