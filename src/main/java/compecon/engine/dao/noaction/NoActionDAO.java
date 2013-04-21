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

package compecon.engine.dao.noaction;

import java.io.Serializable;
import java.util.List;

import compecon.engine.dao.IGenericDAO;

public abstract class NoActionDAO<T, ID extends Serializable> implements
		IGenericDAO<T, ID> {

	@Override
	public T find(Serializable id) {
		return null;
	}

	@Override
	public T findRandom() {
		return null;
	}

	@Override
	public List<T> findAll() {
		return null;
	}

	@Override
	public void save(T entity) {
	}

	@Override
	public void merge(T entity) {
	}

	@Override
	public void delete(T entity) {
	}
}
