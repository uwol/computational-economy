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

package compecon.engine.dao.inmemory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import compecon.engine.dao.IGenericDAO;

public abstract class InMemoryDAO<T> implements IGenericDAO<T> {

	protected BiMap<Integer, T> instancesByIds = HashBiMap.create();

	protected int lastId = 0;

	protected Random randomizer = new Random();

	@Override
	public synchronized T find(int id) {
		if (this.instancesByIds.containsKey(id))
			return this.instancesByIds.get(id);
		return null;
	}

	@Override
	public synchronized T findRandom() {
		List<Integer> keys = new ArrayList<Integer>(
				this.instancesByIds.keySet());
		int id = keys.get(this.randomizer.nextInt(keys.size()));
		return this.instancesByIds.get(id);
	}

	@Override
	public synchronized List<T> findAll() {
		return new ArrayList<T>(this.instancesByIds.values());
	}

	@Override
	public synchronized void save(T entity) {
		this.instancesByIds.put(this.lastId, entity);
		this.lastId++;
	}

	@Override
	public synchronized void merge(T entity) {
		// in-memory entities are never dirty -> no merge necessary
	}

	@Override
	public synchronized void delete(T entity) {
		this.instancesByIds.inverse().remove(entity);
	}
}
