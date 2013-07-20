package compecon.engine.dao.hibernate;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import compecon.culture.sectors.state.law.property.Property;
import compecon.culture.sectors.state.law.property.PropertyOwnership;
import compecon.engine.Agent;
import compecon.engine.dao.DAOFactory.IPropertyOwnershipDAO;

public class PropertyOwnershipDAO extends HibernateDAO<PropertyOwnership>
		implements IPropertyOwnershipDAO {

	@SuppressWarnings("unchecked")
	@Override
	public List<PropertyOwnership> findAllByAgent(Agent agent) {
		return (List<PropertyOwnership>) getSession()
				.createCriteria(PropertyOwnership.class)
				.add(Restrictions.eq("agent", agent)).list();
	}

	@Override
	public PropertyOwnership findFirstByAgent(Agent agent) {
		Object object = getSession().createCriteria(PropertyOwnership.class)
				.add(Restrictions.eq("agent", agent)).uniqueResult();
		if (object == null)
			return null;
		return (PropertyOwnership) object;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Agent> findOwners(Property property) {
		String hql = "SELECT a FROM Agent a, PropertyOwnership po WHERE po.agent = a AND :property MEMBER OF po.ownedProperties";
		return getSession().createQuery(hql).setEntity("property", property)
				.list();
	}

}
