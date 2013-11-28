package compecon.engine.factory.impl;

import compecon.economy.materia.GoodType;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.industry.Factory;
import compecon.economy.sectors.industry.impl.FactoryImpl;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.factory.FactoryFactory;
import compecon.engine.util.HibernateUtil;
import compecon.math.production.ProductionFunction;

public class FactoryImplFactoryImpl implements FactoryFactory {

	public void deleteFactory(final Factory agent) {
		ApplicationContext.getInstance().getFactoryDAO()
				.delete((Factory) agent);
		HibernateUtil.flushSession();
	}

	public Factory newInstanceFactory(final GoodType goodType,
			final Currency primaryCurrency) {
		FactoryImpl factory = new FactoryImpl();
		if (!HibernateUtil.isActive())
			factory.setId(ApplicationContext.getInstance()
					.getSequenceNumberGenerator().getNextId());
		factory.setProducedGoodType(goodType);
		factory.setPrimaryCurrency(primaryCurrency);
		factory.setReferenceCredit(ApplicationContext.getInstance()
				.getConfiguration().factoryConfig.getReferenceCredit());

		ProductionFunction productionFunction = ApplicationContext
				.getInstance().getInputOutputModel()
				.getProductionFunction(goodType);
		factory.setProductionFunction(productionFunction);

		ApplicationContext.getInstance().getFactoryDAO().save(factory);
		factory.initialize();
		HibernateUtil.flushSession();
		return factory;
	}
}
