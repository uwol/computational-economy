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

import compecon.dashboard.Dashboard;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.applicationcontext.ApplicationContextFactory;
import compecon.engine.runner.impl.AbstractConfigurationRunnerImpl;
import compecon.engine.util.HibernateUtil;
import compecon.jmx.JMXRegistration;

/**
 * This is the regular main method for starting a simulation with a dashboard.
 */
public class DashboardRunner extends AbstractConfigurationRunnerImpl {

	public static void main(String[] args) {
		/*
		 * setup
		 */
		if (HibernateUtil.isActive()) {
			ApplicationContextFactory.configureHibernateApplicationContext();
		} else {
			ApplicationContextFactory.configureInMemoryApplicationContext();
		}
		new Dashboard();

		HibernateUtil.openSession();
		JMXRegistration.init();

		/*
		 * run simulation
		 */

		ApplicationContext.getInstance().setRunner(new DashboardRunner());
		ApplicationContext.getInstance().getRunner().run(null);

		/*
		 * tear down
		 */
		JMXRegistration.close();
		HibernateUtil.flushSession();
		HibernateUtil.closeSession();
		ApplicationContext.getInstance().reset();
	}
}
