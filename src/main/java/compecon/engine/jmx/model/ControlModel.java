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

package compecon.engine.jmx.model;

import java.util.List;

import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.industry.Factory;
import compecon.engine.AgentFactory;
import compecon.materia.GoodType;

public class ControlModel extends NotificationListenerModel {

	public void initEconomicGrowth(Currency currency) {
		List<Factory> factories = AgentFactory.getAllFactories();
		for (Factory factory : factories) {
			if (currency.equals(factory.getPrimaryCurrency())) {
				double productivity = factory.getProductionFunction()
						.getProductivity();
				factory.getProductionFunction().setProductivity(
						productivity * 1.5);
			}
		}
	}

	public void initHouseholds(Currency currency) {
		for (int i = 0; i < 100; i++)
			AgentFactory.newInstanceHousehold(currency);
	}

	public void initCarFactory(Currency currency) {
		AgentFactory.newInstanceFactory(GoodType.STEEL, currency);
	}

	public void deficitSpending(Currency currency) {
		AgentFactory.getInstanceState(currency).doDeficitSpending();
	}
}
