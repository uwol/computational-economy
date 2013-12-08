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

package compecon.engine.dao.inmemory.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractIndexedInMemoryDAOImpl<K, V> extends
		InMemoryDAOImpl<V> {

	private Map<K, List<V>> indexedInstances = new HashMap<K, List<V>>();

	private Map<V, List<K>> instanceIndexedKeys = new HashMap<V, List<K>>();

	private synchronized void assureInitializedDataStructure(K key, V instance) {
		if (key != null && instance != null) {
			if (!this.indexedInstances.containsKey(key))
				this.indexedInstances.put(key, new ArrayList<V>());

			if (!this.instanceIndexedKeys.containsKey(instance))
				this.instanceIndexedKeys.put(instance, new ArrayList<K>());
		}
	}

	/*
	 * get instances for key
	 */

	protected synchronized List<V> getInstancesForKey(K key) {
		if (this.indexedInstances.containsKey(key))
			return this.indexedInstances.get(key);
		// has to return null, as the calling DAO method should return a new
		// collection anyway, not this one
		return null;
	}

	protected synchronized List<K> getKeysForInstance(V instance) {
		if (this.instanceIndexedKeys.containsKey(instance))
			return this.instanceIndexedKeys.get(instance);
		// has to return null, as the calling DAO method should return a new
		// collection anyway, not this one
		return null;
	}

	/*
	 * actions
	 */

	protected synchronized void save(K key, V instance) {
		this.assureInitializedDataStructure(key, instance);

		if (key != null) {
			this.indexedInstances.get(key).add(instance);
			this.instanceIndexedKeys.get(instance).add(key);
		}
		super.save(instance);
	}

	public synchronized void delete(V instance) {
		final List<K> keys = getKeysForInstance(instance);
		if (keys != null) {
			for (K key : new ArrayList<K>(keys)) {
				if (this.indexedInstances.containsKey(key)) {
					this.indexedInstances.get(key).remove(instance);
					if (this.indexedInstances.get(key).isEmpty())
						this.indexedInstances.remove(key);
				}

				if (this.instanceIndexedKeys.containsKey(instance)) {
					this.instanceIndexedKeys.get(instance).remove(key);
					if (this.instanceIndexedKeys.get(instance).isEmpty())
						this.instanceIndexedKeys.remove(instance);
				}
			}
		}

		super.delete(instance);
	}
}
