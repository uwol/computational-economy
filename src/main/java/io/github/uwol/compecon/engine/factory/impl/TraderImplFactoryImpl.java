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
import io.github.uwol.compecon.economy.sectors.trading.Trader;
import io.github.uwol.compecon.economy.sectors.trading.impl.TraderImpl;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.factory.TraderFactory;
import io.github.uwol.compecon.engine.util.HibernateUtil;

public class TraderImplFactoryImpl implements TraderFactory {

	@Override
	public void deleteTrader(final Trader agent) {
		ApplicationContext.getInstance().getTraderDAO().delete(agent);
		HibernateUtil.flushSession();
	}

	@Override
	public Trader newInstanceTrader(final Currency primaryCurrency) {
		assert (primaryCurrency != null);

		final TraderImpl trader = new TraderImpl();

		if (!HibernateUtil.isActive()) {
			trader.setId(ApplicationContext.getInstance().getSequenceNumberGenerator().getNextId());
		}

		trader.setPrimaryCurrency(primaryCurrency);
		trader.setReferenceCredit(
				ApplicationContext.getInstance().getConfiguration().traderConfig.getReferenceCredit());

		// excluded good types
		trader.getExcludedGoodTypes().add(GoodType.LABOURHOUR);
		for (final GoodType goodType : GoodType.values()) {
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
