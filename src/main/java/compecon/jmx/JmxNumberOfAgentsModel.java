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

package compecon.jmx;

import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.engine.applicationcontext.ApplicationContext;

public class JmxNumberOfAgentsModel implements JmxNumberOfAgentsModelMBean {

	@Override
	public int getNumberOfHouseholdsEuro() {
		return ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(Currency.EURO).numberOfAgentsModel
				.getNumberOfAgents(Household.class);
	}

	@Override
	public int getNumberOfHouseholdsUsdollar() {
		return ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(Currency.USDOLLAR).numberOfAgentsModel
				.getNumberOfAgents(Household.class);
	}

	@Override
	public int getNumberOfHouseholdsYen() {
		return ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(Currency.YEN).numberOfAgentsModel
				.getNumberOfAgents(Household.class);
	}

}
