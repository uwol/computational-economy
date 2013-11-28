package compecon.engine.factory.impl;

import compecon.economy.materia.GoodType;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.trading.Trader;
import compecon.economy.sectors.trading.impl.TraderImpl;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.factory.TraderFactory;
import compecon.engine.util.HibernateUtil;

public class TraderImplFactoryImpl implements TraderFactory {

	@Override
	public void deleteTrader(Trader agent) {
		ApplicationContext.getInstance().getTraderDAO().delete((Trader) agent);
		HibernateUtil.flushSession();
	}

	public Trader newInstanceTrader(final Currency primaryCurrency) {
		TraderImpl trader = new TraderImpl();
		if (!HibernateUtil.isActive())
			trader.setId(ApplicationContext.getInstance()
					.getSequenceNumberGenerator().getNextId());
		trader.setPrimaryCurrency(primaryCurrency);
		trader.setReferenceCredit(ApplicationContext.getInstance()
				.getConfiguration().traderConfig.getReferenceCredit());

		// excluded good types
		trader.getExcludedGoodTypes().add(GoodType.LABOURHOUR);
		for (GoodType goodType : GoodType.values()) {
			if (GoodType.Sector.TERTIARY.equals(goodType.getSector())) {
				trader.getExcludedGoodTypes().add(goodType);
			}
		}

		ApplicationContext.getInstance().getTraderDAO().save(trader);
		trader.initialize();
		HibernateUtil.flushSession();
		return trader;
	}
}
