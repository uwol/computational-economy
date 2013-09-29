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

package compecon;

import java.util.GregorianCalendar;

import compecon.economy.sectors.financial.Currency;
import compecon.engine.Simulation;
import compecon.engine.util.ConfigurationUtil;

/**
 * This is a main method for sequently starting multiple simulations without a
 * dashboard. The goal is ceteris paribus to determine system parameters, which
 * maximize a metric, e. g. household utility.
 */
public class OptimizationRunner {

	public static void main(String[] args) {

		ConfigurationUtil.HouseholdConfig.number.put(Currency.USDOLLAR, 0);
		ConfigurationUtil.HouseholdConfig.number.put(Currency.YEN, 0);

		ConfigurationUtil.FactoryConfig.numberPerGoodType.put(
				Currency.USDOLLAR, 0);
		ConfigurationUtil.FactoryConfig.numberPerGoodType.put(Currency.YEN, 0);

		ConfigurationUtil.TraderConfig.number.put(Currency.USDOLLAR, 0);
		ConfigurationUtil.TraderConfig.number.put(Currency.YEN, 0);

		double highestTotalUtility = 0.0;
		int optimalI = -1;

		for (int i = 1; i < 30; i++) {
			for (int repetition = 0; repetition < 3; repetition++) {
				ConfigurationUtil.PricingBehaviourConfig.defaultNumberOfPrices = i;

				System.out
						.println("starting simulation run for defaultNumberOfPrices: "
								+ ConfigurationUtil.PricingBehaviourConfig.defaultNumberOfPrices);

				Simulation simulation = new Simulation(false,
						new GregorianCalendar(2001, 7, 1).getTime());
				simulation.run();
				double totalUtility = simulation.getModelRegistry()
						.getUtilityModel(Currency.EURO).getTotalOutputModel()
						.getValue();

				System.out
						.println("simulation run finished for defaultNumberOfPrices: "
								+ ConfigurationUtil.PricingBehaviourConfig.defaultNumberOfPrices
								+ " with totalUtility: " + totalUtility);

				if (totalUtility > highestTotalUtility) {
					System.out.println("total utility improved");
					highestTotalUtility = totalUtility;
					optimalI = ConfigurationUtil.PricingBehaviourConfig.defaultNumberOfPrices;
				}
			}
		}

		System.out.println("best simulation run had total utility: "
				+ highestTotalUtility + " with defaultNumberOfPrices: "
				+ optimalI);
	}
}
