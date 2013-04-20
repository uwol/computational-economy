package compecon.engine.dao;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.criterion.Restrictions;

import compecon.culture.sectors.agriculture.Farm;
import compecon.culture.sectors.financial.CentralBank;
import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.household.Household;
import compecon.culture.sectors.industry.Factory;
import compecon.culture.sectors.state.State;

public class HibernateDAOFactory {

	protected static Map<Class, GenericHibernateDAO> daos = new HashMap<Class, GenericHibernateDAO>();

	protected static GenericHibernateDAO getDAO(Class daoClass) {
		if (!daos.containsKey(daoClass)) {
			try {
				daos.put(daoClass, (GenericHibernateDAO) daoClass.newInstance());
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
		}
		return daos.get(daoClass);
	}

	// ---------------------------------

	public static CentralBankDAOHibernate getCentralBankDAO() {
		return (CentralBankDAOHibernate) getDAO(CentralBankDAOHibernate.class);
	}

	public static CreditBankDAOHibernate getCreditBankDAO() {
		return (CreditBankDAOHibernate) getDAO(CreditBankDAOHibernate.class);
	}

	public static HouseholdDAOHibernate getHouseholdDAO() {
		return (HouseholdDAOHibernate) getDAO(HouseholdDAOHibernate.class);
	}

	public static FactoryDAOHibernate getFactoryDAO() {
		return (FactoryDAOHibernate) getDAO(FactoryDAOHibernate.class);
	}

	public static FarmDAOHibernate getFarmDAO() {
		return (FarmDAOHibernate) getDAO(FarmDAOHibernate.class);
	}

	public static StateDAOHibernate getStateDAO() {
		return (StateDAOHibernate) getDAO(StateDAOHibernate.class);
	}

	// ---------------------------------

	public static class CentralBankDAOHibernate extends
			GenericHibernateDAO<CentralBank, Long> {
		public CentralBank findByCurrency(Currency currency) {
			return (CentralBank) getSession().createCriteria(CentralBank.class)
					.add(Restrictions.eq("coveredCurrency", currency))
					// .setCacheable(true)
					.uniqueResult();
		}
	}

	public static class CreditBankDAOHibernate extends
			GenericHibernateDAO<CreditBank, Long> {
	}

	public static class HouseholdDAOHibernate extends
			GenericHibernateDAO<Household, Long> {
	}

	public static class FactoryDAOHibernate extends
			GenericHibernateDAO<Factory, Long> {
	}

	public static class FarmDAOHibernate extends
			GenericHibernateDAO<Farm, Long> {
	}

	public static class StateDAOHibernate extends
			GenericHibernateDAO<State, Long> {
		public State findByCurrency(Currency currency) {
			return (State) getSession().createCriteria(State.class)
					.add(Restrictions.eq("legislatedCurrency", currency))
					.uniqueResult();
		}
	}
}
