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

import compecon.dashboard.Dashboard;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.applicationcontext.ApplicationContextFactory;
import compecon.engine.util.HibernateUtil;
import compecon.jmx.JMXRegistration;

/**
 * This is the regular main method for starting a simulation with a dashboard.
 */
public class DashboardRunner {

	public static void main(String[] args) throws IOException {
		/*
		 * setup
		 */
		final String configurationPropertiesFilename = System.getProperty(
				"configuration.properties",
				"nodependencies.configuration.properties");

		if (HibernateUtil.isActive()) {
			ApplicationContextFactory
					.configureHibernateApplicationContext(configurationPropertiesFilename);
		} else {
			ApplicationContextFactory
					.configureInMemoryApplicationContext(configurationPropertiesFilename);
		}

		new Dashboard();

		HibernateUtil.openSession();
		JMXRegistration.init();

		/*
		 * run simulation
		 */
		ApplicationContext.getInstance().getAgentFactory()
				.constructAgentsFromConfiguration();
		ApplicationContext.getInstance().getRunner().run(null);
		ApplicationContext.getInstance().getAgentFactory().deconstructAgents();

		/*
		 * tear down
		 */
		JMXRegistration.close();
		HibernateUtil.flushSession();
		HibernateUtil.closeSession();
		ApplicationContext.getInstance().reset();
	}
}
