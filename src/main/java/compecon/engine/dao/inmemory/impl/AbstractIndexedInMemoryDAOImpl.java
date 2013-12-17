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
		AbstractInMemoryDAOImpl<V> {

	private Map<K, List<V>> indexedInstances = new HashMap<K, List<V>>();

	private Map<V, List<K>> instanceIndexedKeys = new HashMap<V, List<K>>();

	/*
	 * get instances for key
	 */

	protected synchronized List<V> getInstancesForKey(K key) {
		return this.indexedInstances.get(key);
	}

	protected synchronized List<K> getKeysForInstance(V instance) {
		return this.instanceIndexedKeys.get(instance);
	}

	/*
	 * actions
	 */

	protected synchronized void save(K key, V instance) {
		if (key != null && instance != null) {
			// store the value
			List<V> indexedInstancesForKey = this.indexedInstances.get(key);
			if (indexedInstancesForKey == null) {
				indexedInstancesForKey = new ArrayList<V>();
				this.indexedInstances.put(key, indexedInstancesForKey);
			}
			indexedInstancesForKey.add(instance);

			// store the key
			List<K> instanceIndexedKeysForInstance = this.instanceIndexedKeys
					.get(instance);
			if (instanceIndexedKeysForInstance == null) {
				instanceIndexedKeysForInstance = new ArrayList<K>();
				this.instanceIndexedKeys.put(instance,
						instanceIndexedKeysForInstance);
			}
			instanceIndexedKeysForInstance.add(key);
		}

		super.save(instance);
	}

	public synchronized void delete(V instance) {
		final List<K> keys = getKeysForInstance(instance);
		if (keys != null) {
			for (K key : new ArrayList<K>(keys)) {
				final List<V> indexedInstancesForKey = this.indexedInstances
						.get(key);
				if (indexedInstancesForKey != null) {
					indexedInstancesForKey.remove(instance);
					if (indexedInstancesForKey.isEmpty()) {
						this.indexedInstances.remove(key);
					}
				}

				final List<K> instanceIndexedKeysForInstance = this.instanceIndexedKeys
						.get(instance);
				if (instanceIndexedKeysForInstance != null) {
					instanceIndexedKeysForInstance.remove(key);
					if (instanceIndexedKeysForInstance.isEmpty())
						this.instanceIndexedKeys.remove(instance);
				}
			}
		}

		super.delete(instance);
	}
}
