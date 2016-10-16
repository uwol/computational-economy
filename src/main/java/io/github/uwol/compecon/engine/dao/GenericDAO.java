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

package io.github.uwol.compecon.engine.dao;

import java.util.List;

public interface GenericDAO<T> {

	/**
	 * WARNING: Should only be called from factory classes, which ensure a
	 * subsequent Hibernate flush.
	 */
	public void delete(final T entity);

	public T find(final int id);

	public List<T> findAll();

	public T findRandom();

	/**
	 * WARNING: Should only be called from factory classes, which ensure a
	 * subsequent Hibernate flush.
	 */
	public void merge(final T entity);

	/**
	 * WARNING: Should only be called from factory classes, which ensure a
	 * subsequent Hibernate flush.
	 */
	public void save(final T entity);
}
