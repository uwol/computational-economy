/*
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

package compecon.engine.jmx;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.hibernate.jmx.StatisticsService;

import compecon.engine.jmx.mbean.JmxAgents;
import compecon.engine.util.HibernateUtil;

public class Registration {
	public static void init() throws MalformedObjectNameException,
			InstanceAlreadyExistsException, MBeanRegistrationException,
			NotCompliantMBeanException {

		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

		ObjectName agentsObjectName = new ObjectName("compecon.jmx:type=Agents");
		JmxAgents agentsMBean = new JmxAgents();
		mBeanServer.registerMBean(agentsMBean, agentsObjectName);

		StatisticsService statsMBean = new StatisticsService();
		statsMBean.setSessionFactory(HibernateUtil.getSessionFactory());
		statsMBean.setStatisticsEnabled(true);
		mBeanServer.registerMBean(statsMBean, new ObjectName(
				"Hibernate:application=Statistics"));
	}
}
