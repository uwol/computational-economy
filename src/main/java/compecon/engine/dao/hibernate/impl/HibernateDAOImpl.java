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

package compecon.engine.dao.hibernate.impl;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Random;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;

import compecon.engine.dao.GenericDAO;
import compecon.engine.util.HibernateUtil;

public class HibernateDAOImpl<T> implements GenericDAO<T> {

	private Class<T> persistentClass;

	@SuppressWarnings("unchecked")
	public HibernateDAOImpl() {
		this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	protected Session getSession() {
		return HibernateUtil.getSession();
	}

	public Class<T> getPersistentClass() {
		return persistentClass;
	}

	@SuppressWarnings("unchecked")
	public T find(int id) {
		return (T) getSession().load(getPersistentClass(), id);
	}

	@SuppressWarnings("unchecked")
	public T findRandom() {
		Criteria crit = getSession().createCriteria(persistentClass);
		crit.setProjection(Projections.rowCount());
		int count = ((Number) crit.uniqueResult()).intValue();
		if (0 != count) {
			int index = new Random().nextInt(count);
			crit = getSession().createCriteria(persistentClass);
			return (T) crit.setFirstResult(index).setMaxResults(1)
					.uniqueResult();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<T> findAll() {
		return getSession().createCriteria(getPersistentClass()).list();
	}

	public void save(T entity) {
		getSession().saveOrUpdate(entity);
	}

	public void merge(T entity) {
		getSession().merge(entity);
	}

	public void delete(T entity) {
		getSession().delete(entity);
		// getSession().evict(entity);
	}
}
