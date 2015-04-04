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

	@Override
	public void deleteFactory(final Factory agent) {
		ApplicationContext.getInstance().getFactoryDAO().delete(agent);
		HibernateUtil.flushSession();
	}

	@Override
	public Factory newInstanceFactory(final GoodType goodType,
			final Currency primaryCurrency) {
		assert (goodType != null);
		assert (primaryCurrency != null);

		final FactoryImpl factory = new FactoryImpl();
		if (!HibernateUtil.isActive()) {
			factory.setId(ApplicationContext.getInstance()
					.getSequenceNumberGenerator().getNextId());
		}
		factory.setProducedGoodType(goodType);
		factory.setPrimaryCurrency(primaryCurrency);
		factory.setReferenceCredit(ApplicationContext.getInstance()
				.getConfiguration().factoryConfig.getReferenceCredit());

		final ProductionFunction productionFunction = ApplicationContext
				.getInstance().getInputOutputModel()
				.getProductionFunction(goodType);

		assert (productionFunction != null) : "no production function defined for good type "
				+ goodType;

		factory.setProductionFunction(productionFunction);

		ApplicationContext.getInstance().getFactoryDAO().save(factory);
		factory.initialize();
		HibernateUtil.flushSession();
		return factory;
	}
}
