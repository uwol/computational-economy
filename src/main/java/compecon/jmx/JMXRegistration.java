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

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.hibernate.jmx.StatisticsService;

import compecon.engine.util.HibernateUtil;

public class JMXRegistration {

	private static MBeanServer mBeanServer = ManagementFactory
			.getPlatformMBeanServer();

	private static Map<ObjectName, Object> mBeans = new HashMap<ObjectName, Object>();

	public static void init() {
		mBeans.clear();
		try {
			mBeans.put(new ObjectName("compecon.jmx:type=NumberOfAgentsModel"),
					new JmxNumberOfAgentsModel());
			mBeans.put(new ObjectName("compecon.jmx:type=TimeSystemModel"),
					new JmxTimeSystemModel());

			// hibernate stats mbean
			StatisticsService statsMBean = new StatisticsService();
			statsMBean.setSessionFactory(HibernateUtil.getSessionFactory());
			statsMBean.setStatisticsEnabled(true);
			mBeans.put(new ObjectName("Hibernate:application=Statistics"),
					statsMBean);

			for (Entry<ObjectName, Object> entry : mBeans.entrySet()) {
				mBeanServer.registerMBean(entry.getValue(), entry.getKey());
			}
		} catch (MalformedObjectNameException | InstanceAlreadyExistsException
				| MBeanRegistrationException | NotCompliantMBeanException e) {
			e.printStackTrace();
		}
	}

	public static void close() {
		for (Entry<ObjectName, Object> entry : mBeans.entrySet()) {
			try {
				mBeanServer.unregisterMBean(entry.getKey());
			} catch (MBeanRegistrationException | InstanceNotFoundException e) {
				e.printStackTrace();
			}
		}
		mBeans.clear();
	}
}
