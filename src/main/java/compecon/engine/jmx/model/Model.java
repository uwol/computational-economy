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

package compecon.engine.jmx.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Model {

	public interface IModelListener {
		public void notifyListener();
	}

	protected List<IModelListener> listeners = new ArrayList<IModelListener>();

	protected Model() {
	}

	public void registerListener(IModelListener listener) {
		this.listeners.add(listener);
	}

	public void notifyListeners() {
		for (IModelListener listener : this.listeners)
			listener.notifyListener();
	}
}
