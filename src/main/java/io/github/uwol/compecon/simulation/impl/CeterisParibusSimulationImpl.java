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

package io.github.uwol.compecon.simulation.impl;

import java.io.IOException;
import java.util.GregorianCalendar;

import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContextFactory;
import io.github.uwol.compecon.jmx.JMXRegistration;

/**
 * This is a main method for sequently starting multiple simulations without a
 * dashboard. The goal is to determine system parameters ceteris paribus, which
 * maximize a metric, e. g. household utility.
 */
public class CeterisParibusSimulationImpl {

	public static void main(final String[] args) throws IOException {

		double highestTotalUtility = 0.0;
		double maxI = -1;

		/*
		 * iterate
		 */
		for (double i = 0.01; i < 0.5; i += 0.03) {
			for (int repetition = 0; repetition < 3; repetition++) {
				System.out.println("starting simulation run for i: " + i);

				final double totalUtility = runSimulationIteration(i);

				System.out.println("simulation run finished for i: " + i + " with totalUtility: " + totalUtility);

				if (totalUtility > highestTotalUtility) {
					System.out.println("total utility improved");
					highestTotalUtility = totalUtility;
					maxI = i;
				}
			}
		}

		System.out.println("max simulation run had total utility: " + highestTotalUtility + " with i: " + maxI);
	}

	protected static void overwriteConfiguration(final double i) {
		System.out.println("overwriting configuration");

		/*
		 * overwrite default configuration.
		 */
		ApplicationContext.getInstance().getConfiguration().householdConfig.number.put(Currency.USDOLLAR, 0);
		ApplicationContext.getInstance().getConfiguration().householdConfig.number.put(Currency.YEN, 0);

		for (final GoodType goodType : GoodType.values()) {
			ApplicationContext.getInstance().getConfiguration().factoryConfig.number.get(Currency.USDOLLAR)
					.put(goodType, 0);
			ApplicationContext.getInstance().getConfiguration().factoryConfig.number.get(Currency.YEN).put(goodType, 0);
		}

		ApplicationContext.getInstance().getConfiguration().traderConfig.number.put(Currency.USDOLLAR, 0);
		ApplicationContext.getInstance().getConfiguration().traderConfig.number.put(Currency.YEN, 0);

		/*
		 * set values for iteration
		 */
		ApplicationContext.getInstance()
				.getConfiguration().pricingBehaviourConfig.defaultPriceChangeIncrementExplicit = i;
	}

	protected static double runSimulationIteration(final double i) throws IOException {
		/*
		 * setup
		 */
		final String configurationPropertiesFilename = System.getProperty("configuration.properties",
				"interdependencies.configuration.properties");

		ApplicationContextFactory.configureInMemoryApplicationContext(configurationPropertiesFilename);

		overwriteConfiguration(i);

		JMXRegistration.init();

		/*
		 * run simulation
		 */
		ApplicationContext.getInstance().getAgentFactory().constructAgentsFromConfiguration();
		ApplicationContext.getInstance().getSimulationRunner().run(new GregorianCalendar(2000, 7, 1).getTime());
		ApplicationContext.getInstance().getAgentFactory().deconstructAgents();

		final double totalUtility = ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(Currency.EURO).totalUtilityOutputModel.getValue();

		/*
		 * reset application context
		 */
		JMXRegistration.close();
		ApplicationContext.getInstance().reset();

		return totalUtility;
	}
}
