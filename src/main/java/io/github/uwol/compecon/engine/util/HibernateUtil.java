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

package io.github.uwol.compecon.engine.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistryBuilder;

public class HibernateUtil {

	private static Boolean isActive;

	private static Session session;

	private static final SessionFactory sessionFactory = buildSessionFactory();

	public static Session beginTransaction() {
		if (HibernateUtil.isActive()) {
			final Session hibernateSession = HibernateUtil.getCurrentSession();
			hibernateSession.beginTransaction();
			return hibernateSession;
		}

		return null;
	}

	private static SessionFactory buildSessionFactory() {
		if (HibernateUtil.isActive()) {
			final Configuration configuration = new Configuration();
			configuration.configure("hibernate.cfg.xml");
			final ServiceRegistryBuilder serviceRegistryBuilder = new ServiceRegistryBuilder()
					.applySettings(configuration.getProperties());
			final SessionFactory sessionFactory = configuration
					.buildSessionFactory(serviceRegistryBuilder
							.buildServiceRegistry());
			return sessionFactory;
		}

		return null;
	}

	public static void clearSession() {
		if (HibernateUtil.isActive()) {
			session.clear();
		}
	}

	public static void closeCurrentSession() {
		if (HibernateUtil.isActive()) {
			HibernateUtil.getCurrentSession().close();
		}
	}

	public static void closeSession() {
		if (HibernateUtil.isActive()) {
			// close session independent from transaction contexts
			session.close();
		}
	}

	public static void commitTransaction() {
		if (HibernateUtil.isActive()) {
			HibernateUtil.getCurrentSession().getTransaction().commit();
		}
	}

	public static void flushCurrentSession() {
		if (HibernateUtil.isActive()) {
			HibernateUtil.getCurrentSession().flush();
		}
	}

	public static void flushSession() {
		if (HibernateUtil.isActive()) {
			session.flush();
		}
	}

	/*
	 * CurrentSession -> transactional
	 */
	public static Session getCurrentSession() {
		if (HibernateUtil.isActive()) {
			return sessionFactory.getCurrentSession();
		}

		return null;
	}

	public static Session getSession() {
		return session;
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public static boolean isActive() {
		if (isActive == null) {
			final String dbActive = System.getProperty("dbActive");
			if (dbActive != null) {
				isActive = Boolean.parseBoolean(dbActive);
			} else {
				isActive = false;
			}
		}

		return isActive;
	}

	/*
	 * Non-transactional session
	 */
	public static void openSession() {
		if (HibernateUtil.isActive()) {
			// open session independent from transaction contexts
			session = sessionFactory.openSession();
		}
	}

	public static void rollbackTransaction() {
		if (HibernateUtil.isActive()) {
			HibernateUtil.getCurrentSession().getTransaction().rollback();
		}
	}
}
