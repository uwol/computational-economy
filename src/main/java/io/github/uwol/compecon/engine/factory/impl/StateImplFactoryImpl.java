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

import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.economy.sectors.state.State;
import io.github.uwol.compecon.economy.sectors.state.impl.StateImpl;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.factory.StateFactory;
import io.github.uwol.compecon.engine.util.HibernateUtil;

public class StateImplFactoryImpl implements StateFactory {

	@Override
	public void deleteState(final State agent) {
		ApplicationContext.getInstance().getStateDAO().delete(agent);
		HibernateUtil.flushSession();
	}

	@Override
	public State newInstanceState(final Currency currency) {
		assert (currency != null);

		final StateImpl state = new StateImpl();

		if (!HibernateUtil.isActive()) {
			state.setId(ApplicationContext.getInstance().getSequenceNumberGenerator().getNextId());
		}

		state.setPrimaryCurrency(currency);
		ApplicationContext.getInstance().getStateDAO().save(state);
		state.initialize();
		HibernateUtil.flushSession();
		return state;
	}

}
