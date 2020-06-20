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

package io.github.uwol.compecon.engine.dao.inmemory.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.dao.GenericDAO;

public abstract class AbstractInMemoryDAOImpl<T> implements GenericDAO<T> {

	protected BiMap<Integer, T> instancesByIds = HashBiMap.create();

	protected int lastId = 0;

	@Override
	public synchronized void delete(final T entity) {
		this.instancesByIds.inverse().remove(entity);
	}

	@Override
	public synchronized T find(final int id) {
		return this.instancesByIds.get(id);
	}

	@Override
	public synchronized List<T> findAll() {
		return new ArrayList<T>(this.instancesByIds.values());
	}

	@Override
	public synchronized T findRandom() {
		final List<Integer> keys = new ArrayList<Integer>(this.instancesByIds.keySet());
		final int index = ApplicationContext.getInstance().getRandomNumberGenerator().nextInt(keys.size());
		final int id = keys.get(index);
		return this.instancesByIds.get(id);
	}

	@Override
	public synchronized void merge(final T entity) {
		// in-memory entities are never dirty -> no merge necessary
	}

	@Override
	public synchronized void save(final T entity) {
		this.instancesByIds.put(this.lastId, entity);
		this.lastId++;
	}
}
