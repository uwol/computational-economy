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

package io.github.uwol.compecon.engine.statistics;

import java.util.ArrayList;
import java.util.List;

public abstract class NotificationListenerModel {

	public interface ModelListener {
		public void notifyListener();
	}

	protected List<ModelListener> listeners = new ArrayList<ModelListener>();

	protected NotificationListenerModel() {
	}

	public void notifyListeners() {
		for (final ModelListener listener : listeners) {
			listener.notifyListener();
		}
	}

	public void registerListener(final ModelListener listener) {
		listeners.add(listener);
	}
}
