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
import compecon.materia.GoodType;

/**
 * This is a main method for sequently starting multiple simulations without a
 * dashboard. The goal is ceteris paribus to determine system parameters, which
 * maximize a metric, e. g. household utility.
 */
public class OptimizationRunner {

	public static void main(String[] args) {

		ConfigurationUtil.HouseholdConfig.number.put(Currency.USDOLLAR, 0);
		ConfigurationUtil.HouseholdConfig.number.put(Currency.YEN, 0);

		for (GoodType goodType : GoodType.values()) {
			ConfigurationUtil.FactoryConfig.number.get(Currency.USDOLLAR).put(
					goodType, 0);
			ConfigurationUtil.FactoryConfig.number.get(Currency.YEN).put(
					goodType, 0);
		}

		ConfigurationUtil.TraderConfig.number.put(Currency.USDOLLAR, 0);
		ConfigurationUtil.TraderConfig.number.put(Currency.YEN, 0);

		double highestTotalUtility = 0.0;
		double optimalI = -1;

		for (double i = 0.01; i < 0.5; i += 0.03) {
			for (int repetition = 0; repetition < 3; repetition++) {
				ConfigurationUtil.PricingBehaviourConfig.defaultPriceChangeIncrementExplicit = i;

				System.out
						.println("starting simulation run for defaultPriceChangeIncrement: "
								+ ConfigurationUtil.PricingBehaviourConfig.defaultPriceChangeIncrementExplicit);

				Simulation simulation = new Simulation(false,
						new GregorianCalendar(2001, 7, 1).getTime());
				simulation.run();
				double totalUtility = simulation.getModelRegistry()
						.getNationalEconomyModel(Currency.EURO).totalUtilityOutputModel
						.getValue();

				System.out
						.println("simulation run finished for defaultPriceChangeIncrement: "
								+ ConfigurationUtil.PricingBehaviourConfig.defaultPriceChangeIncrementExplicit
								+ " with totalUtility: " + totalUtility);

				if (totalUtility > highestTotalUtility) {
					System.out.println("total utility improved");
					highestTotalUtility = totalUtility;
					optimalI = ConfigurationUtil.PricingBehaviourConfig.defaultPriceChangeIncrementExplicit;
				}
			}
		}

		System.out.println("best simulation run had total utility: "
				+ highestTotalUtility + " with defaultPriceChangeIncrement: "
				+ optimalI);
	}
}
