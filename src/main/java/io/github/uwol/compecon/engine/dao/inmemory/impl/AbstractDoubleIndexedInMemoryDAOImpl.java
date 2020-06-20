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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractDoubleIndexedInMemoryDAOImpl<K, V> extends AbstractIndexedInMemoryDAOImpl<K, V> {

	private final Map<K, List<V>> indexedInstances = new HashMap<K, List<V>>();

	private final Map<V, List<K>> instanceIndexedKeys = new HashMap<V, List<K>>();

	/*
	 * get instances for key
	 */

	@Override
	public synchronized void delete(final V instance) {
		final List<K> secondKeys = getSecondKeysForInstance(instance);
		if (secondKeys != null) {
			for (final K secondKey : new ArrayList<K>(secondKeys)) {
				final List<V> indexedInstanceForKey = this.indexedInstances.get(secondKey);
				if (indexedInstanceForKey != null) {
					indexedInstanceForKey.remove(instance);
					if (indexedInstanceForKey.isEmpty()) {
						this.indexedInstances.remove(secondKey);
					}
				}

				final List<K> instanceIndexedKeysForInstance = this.instanceIndexedKeys.get(instance);
				if (instanceIndexedKeysForInstance != null) {
					instanceIndexedKeysForInstance.remove(secondKey);
					if (instanceIndexedKeysForInstance.isEmpty()) {
						this.instanceIndexedKeys.remove(instance);
					}
				}
			}
		}

		super.delete(instance);
	}

	protected synchronized List<K> getFirstKeysForInstance(final V instance) {
		return super.getKeysForInstance(instance);
	}

	protected synchronized List<V> getInstancesForFirstKey(final K firstKey) {
		return super.getInstancesForKey(firstKey);
	}

	protected synchronized List<V> getInstancesForSecondKey(final K secondKey) {
		return this.indexedInstances.get(secondKey);
	}

	/*
	 * actions
	 */

	protected synchronized List<K> getSecondKeysForInstance(final V instance) {
		return this.instanceIndexedKeys.get(instance);
	}

	protected synchronized void save(final K firstKey, final K secondKey, final V instance) {
		if (secondKey != null && instance != null) {
			// store the value
			List<V> indexedInstancesForKey = this.indexedInstances.get(secondKey);
			if (indexedInstancesForKey == null) {
				indexedInstancesForKey = new ArrayList<V>();
				this.indexedInstances.put(secondKey, indexedInstancesForKey);
			}
			indexedInstancesForKey.add(instance);

			// store the key
			List<K> instanceIndexedKeysForInstance = this.instanceIndexedKeys.get(instance);
			if (instanceIndexedKeysForInstance == null) {
				instanceIndexedKeysForInstance = new ArrayList<K>();
				this.instanceIndexedKeys.put(instance, instanceIndexedKeysForInstance);
			}
			instanceIndexedKeysForInstance.add(secondKey);
		}

		super.save(firstKey, instance);
	}
}
