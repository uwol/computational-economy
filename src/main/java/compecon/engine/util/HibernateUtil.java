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

package compecon.engine.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistryBuilder;

public class HibernateUtil {

	private static final SessionFactory sessionFactory = buildSessionFactory();

	private static Session session;

	private static SessionFactory buildSessionFactory() {
		if (ConfigurationUtil.DbConfig.getActivateDb()) {
			Configuration configuration = new Configuration();
			configuration.configure("hibernate.cfg.xml");
			ServiceRegistryBuilder serviceRegistryBuilder = new ServiceRegistryBuilder()
					.applySettings(configuration.getProperties());
			SessionFactory sessionFactory = configuration
					.buildSessionFactory(serviceRegistryBuilder
							.buildServiceRegistry());
			return sessionFactory;
		}
		return null;
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/*
	 * Non-transactional session
	 */
	public static void openSession() {
		if (ConfigurationUtil.DbConfig.getActivateDb()) {
			// open session independent from transaction contexts
			session = sessionFactory.openSession();
		}
	}

	public static void clearSession() {
		if (ConfigurationUtil.DbConfig.getActivateDb())
			session.clear();
	}

	public static void closeSession() {
		if (ConfigurationUtil.DbConfig.getActivateDb()) {
			// close session independent from transaction contexts
			session.close();
		}
	}

	public static Session getSession() {
		return session;
	}

	public static void flushSession() {
		if (ConfigurationUtil.DbConfig.getActivateDb())
			session.flush();
	}

	/*
	 * CurrentSession -> transactional
	 */
	public static Session getCurrentSession() {
		if (ConfigurationUtil.DbConfig.getActivateDb())
			return sessionFactory.getCurrentSession();
		return null;
	}

	public static void closeCurrentSession() {
		if (ConfigurationUtil.DbConfig.getActivateDb())
			HibernateUtil.getCurrentSession().close();

	}

	public static void flushCurrentSession() {
		if (ConfigurationUtil.DbConfig.getActivateDb())
			HibernateUtil.getCurrentSession().flush();

	}

	public static Session beginTransaction() {
		if (ConfigurationUtil.DbConfig.getActivateDb()) {
			Session hibernateSession = HibernateUtil.getCurrentSession();
			hibernateSession.beginTransaction();
			return hibernateSession;
		}
		return null;
	}

	public static void commitTransaction() {
		if (ConfigurationUtil.DbConfig.getActivateDb())
			HibernateUtil.getCurrentSession().getTransaction().commit();
	}

	public static void rollbackTransaction() {
		if (ConfigurationUtil.DbConfig.getActivateDb())
			HibernateUtil.getCurrentSession().getTransaction().rollback();
	}
}
