/*
Copyright (C) 2015 u.wol@wwu.de

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

package io.github.uwol.compecon.simulation.minimal.impl;

import java.util.Date;
import java.util.List;

import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.economy.sectors.industry.Factory;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.timesystem.TimeSystemEvent;

public class ExogenousShockEvent implements TimeSystemEvent {

	protected void contraction() {
		final List<Factory> factories = ApplicationContext.getInstance()
				.getAgentService().findFactories(Currency.EURO, GoodType.WHEAT);

		for (final Factory factory : factories) {
			final double productivity = factory.getProductionFunction()
					.getProductivity();
			factory.getProductionFunction()
					.setProductivity(productivity / 1.05);
		}
	}

	protected void growth() {
		final List<Factory> factories = ApplicationContext.getInstance()
				.getAgentService().findFactories(Currency.EURO, GoodType.WHEAT);

		for (final Factory factory : factories) {
			final double productivity = factory.getProductionFunction()
					.getProductivity();
			factory.getProductionFunction()
					.setProductivity(productivity * 1.05);
		}
	}

	@Override
	public boolean isDeconstructed() {
		return false;
	}

	@Override
	public void onEvent() {
		final Date date = ApplicationContext.getInstance().getTimeSystem()
				.getCurrentDate();

		contraction();
	}
}
