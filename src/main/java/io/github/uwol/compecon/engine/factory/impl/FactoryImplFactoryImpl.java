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

package io.github.uwol.compecon.engine.factory.impl;

import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.economy.sectors.industry.Factory;
import io.github.uwol.compecon.economy.sectors.industry.impl.FactoryImpl;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.factory.FactoryFactory;
import io.github.uwol.compecon.engine.util.HibernateUtil;
import io.github.uwol.compecon.math.production.ProductionFunction;

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
