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

import java.io.IOException;
import java.util.GregorianCalendar;

import compecon.economy.materia.GoodType;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.applicationcontext.ApplicationContextFactory;
import compecon.engine.util.HibernateUtil;
import compecon.jmx.JMXRegistration;

/**
 * This is a main method for sequently starting multiple simulations without a
 * dashboard. The goal is ceteris paribus to determine system parameters, which
 * maximize a metric, e. g. household utility.
 */
public class OptimizationRunner {

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

				System.out.println("simulation run finished for i: " + i
						+ " with totalUtility: " + totalUtility);

				if (totalUtility > highestTotalUtility) {
					System.out.println("total utility improved");
					highestTotalUtility = totalUtility;
					maxI = i;
				}
			}
		}

		System.out.println("max simulation run had total utility: "
				+ highestTotalUtility + " with i: " + maxI);
	}

	protected static void overwriteConfiguration(final double i) {
		System.out.println("overwriting configuration");

		/*
		 * overwrite default configuration.
		 */
		ApplicationContext.getInstance().getConfiguration().householdConfig.number
				.put(Currency.USDOLLAR, 0);
		ApplicationContext.getInstance().getConfiguration().householdConfig.number
				.put(Currency.YEN, 0);

		for (final GoodType goodType : GoodType.values()) {
			ApplicationContext.getInstance().getConfiguration().factoryConfig.number
					.get(Currency.USDOLLAR).put(goodType, 0);
			ApplicationContext.getInstance().getConfiguration().factoryConfig.number
					.get(Currency.YEN).put(goodType, 0);
		}

		ApplicationContext.getInstance().getConfiguration().traderConfig.number
				.put(Currency.USDOLLAR, 0);
		ApplicationContext.getInstance().getConfiguration().traderConfig.number
				.put(Currency.YEN, 0);

		/*
		 * set values for iteration
		 */
		ApplicationContext.getInstance().getConfiguration().pricingBehaviourConfig.defaultPriceChangeIncrementExplicit = i;
	}

	protected static double runSimulationIteration(final double i)
			throws IOException {
		/*
		 * setup
		 */
		final String configurationPropertiesFilename = System.getProperty(
				"configuration.properties",
				"interdependencies.configuration.properties");

		if (HibernateUtil.isActive()) {
			ApplicationContextFactory
					.configureHibernateApplicationContext(configurationPropertiesFilename);
		} else {
			ApplicationContextFactory
					.configureInMemoryApplicationContext(configurationPropertiesFilename);
		}

		overwriteConfiguration(i);

		HibernateUtil.openSession();
		JMXRegistration.init();

		/*
		 * run simulation
		 */
		ApplicationContext.getInstance().getAgentFactory()
				.constructAgentsFromConfiguration();
		ApplicationContext.getInstance().getRunner()
				.run(new GregorianCalendar(2000, 7, 1).getTime());
		ApplicationContext.getInstance().getAgentFactory().deconstructAgents();

		final double totalUtility = ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(Currency.EURO).totalUtilityOutputModel
				.getValue();

		/*
		 * reset application context
		 */
		JMXRegistration.close();
		HibernateUtil.flushSession();
		HibernateUtil.closeSession();
		ApplicationContext.getInstance().reset();

		return totalUtility;
	}
}
