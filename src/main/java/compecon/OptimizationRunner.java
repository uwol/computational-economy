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
import compecon.engine.runner.impl.AbstractConfigurationRunnerImpl;
import compecon.engine.util.HibernateUtil;
import compecon.jmx.JMXRegistration;

/**
 * This is a main method for sequently starting multiple simulations without a
 * dashboard. The goal is ceteris paribus to determine system parameters, which
 * maximize a metric, e. g. household utility.
 */
public class OptimizationRunner extends AbstractConfigurationRunnerImpl {

	public static void main(String[] args) throws IOException {

		double highestTotalUtility = 0.0;
		double optimalI = -1;

		/*
		 * iterate
		 */
		for (double i = 0.01; i < 0.5; i += 0.03) {
			for (int repetition = 0; repetition < 3; repetition++) {
				/*
				 * setup
				 */
				final String configurationPropertiesFilename = System
						.getProperty("configuration.properties",
								"interdependencies.configuration.properties");

				if (HibernateUtil.isActive()) {
					ApplicationContextFactory
							.configureHibernateApplicationContext(configurationPropertiesFilename);
				} else {
					ApplicationContextFactory
							.configureInMemoryApplicationContext(configurationPropertiesFilename);
				}

				ApplicationContext.getInstance().getConfiguration().pricingBehaviourConfig.defaultPriceChangeIncrementExplicit = i;

				overwriteConfiguration();
				HibernateUtil.openSession();
				JMXRegistration.init();

				/*
				 * run simulation
				 */
				System.out
						.println("starting simulation run for defaultPriceChangeIncrement: "
								+ ApplicationContext.getInstance()
										.getConfiguration().pricingBehaviourConfig.defaultPriceChangeIncrementExplicit);

				ApplicationContext.getInstance().setRunner(
						new OptimizationRunner());
				ApplicationContext.getInstance().getRunner()
						.run(new GregorianCalendar(2000, 7, 1).getTime());

				final double totalUtility = ApplicationContext.getInstance()
						.getModelRegistry()
						.getNationalEconomyModel(Currency.EURO).totalUtilityOutputModel
						.getValue();

				System.out
						.println("simulation run finished for defaultPriceChangeIncrement: "
								+ ApplicationContext.getInstance()
										.getConfiguration().pricingBehaviourConfig.defaultPriceChangeIncrementExplicit
								+ " with totalUtility: " + totalUtility);

				if (totalUtility > highestTotalUtility) {
					System.out.println("total utility improved");
					highestTotalUtility = totalUtility;
					optimalI = ApplicationContext.getInstance()
							.getConfiguration().pricingBehaviourConfig.defaultPriceChangeIncrementExplicit;
				}

				/*
				 * reset application context
				 */
				JMXRegistration.close();
				HibernateUtil.flushSession();
				HibernateUtil.closeSession();
				ApplicationContext.getInstance().reset();
			}
		}

		System.out.println("best simulation run had total utility: "
				+ highestTotalUtility + " with defaultPriceChangeIncrement: "
				+ optimalI);
	}

	public static void overwriteConfiguration() {
		/*
		 * overwrite default configuration.
		 */
		ApplicationContext.getInstance().getConfiguration().householdConfig.number
				.put(Currency.USDOLLAR, 0);
		ApplicationContext.getInstance().getConfiguration().householdConfig.number
				.put(Currency.YEN, 0);

		for (GoodType goodType : GoodType.values()) {
			ApplicationContext.getInstance().getConfiguration().factoryConfig.number
					.get(Currency.USDOLLAR).put(goodType, 0);
			ApplicationContext.getInstance().getConfiguration().factoryConfig.number
					.get(Currency.YEN).put(goodType, 0);
		}

		ApplicationContext.getInstance().getConfiguration().traderConfig.number
				.put(Currency.USDOLLAR, 0);
		ApplicationContext.getInstance().getConfiguration().traderConfig.number
				.put(Currency.YEN, 0);
	}
}
