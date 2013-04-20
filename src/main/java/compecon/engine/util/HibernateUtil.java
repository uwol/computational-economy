package compecon.engine.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistryBuilder;

public class HibernateUtil {

	private static final SessionFactory sessionFactory = buildSessionFactory();

	private static Session session;

	private static SessionFactory buildSessionFactory() {
		Configuration configuration = new Configuration();
		configuration.configure("hibernate.cfg.xml");
		ServiceRegistryBuilder serviceRegistryBuilder = new ServiceRegistryBuilder()
				.applySettings(configuration.getProperties());
		SessionFactory sessionFactory = configuration
				.buildSessionFactory(serviceRegistryBuilder
						.buildServiceRegistry());
		return sessionFactory;
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/*
	 * Non-transactional session
	 */
	public static void openSession() {
		// open session independent from transaction contexts
		session = sessionFactory.openSession();
	}

	public static void closeSession() {
		// close session independent from transaction contexts
		session.close();
	}

	public static Session getSession() {
		return session;
	}

	/*
	 * CurrentSession -> transactional
	 */
	public static Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	public static void closeCurrentSession() {
		HibernateUtil.getCurrentSession().close();
	}

	public static Session beginTransaction() {
		Session hibernateSession = HibernateUtil.getCurrentSession();
		hibernateSession.beginTransaction();
		return hibernateSession;
	}

	public static void commitTransaction() {
		HibernateUtil.getCurrentSession().getTransaction().commit();
	}

	public static void rollbackTransaction() {
		HibernateUtil.getCurrentSession().getTransaction().rollback();
	}
}
