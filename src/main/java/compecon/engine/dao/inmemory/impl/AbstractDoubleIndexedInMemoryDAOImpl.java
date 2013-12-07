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

public abstract class AbstractDoubleIndexedInMemoryDAOImpl<K, V> extends
		AbstractIndexedInMemoryDAOImpl<K, V> {

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

	protected synchronized List<V> getInstancesForFirstKey(K firstKey) {
		return super.getInstancesForKey(firstKey);
	}

	protected synchronized List<V> getInstancesForSecondKey(K secondKey) {
		if (this.indexedInstances.containsKey(secondKey))
			return this.indexedInstances.get(secondKey);
		// has to return null, as the calling DAO method should return a new
		// collection anyway, not this one
		return null;
	}

	protected synchronized List<K> getFirstKeysForInstance(V instance) {
		return super.getKeysForInstance(instance);
	}

	protected synchronized List<K> getSecondKeysForInstance(V instance) {
		if (this.instanceIndexedKeys.containsKey(instance))
			return this.instanceIndexedKeys.get(instance);
		// has to return null, as the calling DAO method should return a new
		// collection anyway, not this one
		return null;
	}

	/*
	 * actions
	 */

	protected synchronized void save(K firstKey, K secondKey, V instance) {
		this.assureInitializedDataStructure(secondKey, instance);

		if (secondKey != null) {
			this.indexedInstances.get(secondKey).add(instance);
			this.instanceIndexedKeys.get(instance).add(secondKey);
		}
		super.save(firstKey, instance);
	}

	public synchronized void delete(V instance) {
		List<K> secondKeys = getSecondKeysForInstance(instance);
		if (secondKeys != null) {
			for (K secondKey : new ArrayList<K>(secondKeys)) {
				if (this.indexedInstances.containsKey(secondKey)) {
					this.indexedInstances.get(secondKey).remove(instance);
					if (this.indexedInstances.get(secondKey).isEmpty())
						this.indexedInstances.remove(secondKey);
				}

				if (this.instanceIndexedKeys.containsKey(instance)) {
					this.instanceIndexedKeys.get(instance).remove(secondKey);
					if (this.instanceIndexedKeys.get(instance).isEmpty())
						this.instanceIndexedKeys.remove(instance);
				}
			}
		}

		super.delete(instance);
	}
}
