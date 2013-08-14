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

package compecon.engine.dao.hibernate;

import java.util.Iterator;

import org.hibernate.ScrollableResults;

/**
 * look-ahead implementation
 */
public class HibernateIterator<T> implements Iterator<T> {

	protected ScrollableResults itemCursor;

	private boolean didNext = false;
	private boolean hasNext = false;

	public HibernateIterator(ScrollableResults itemCursor) {
		super();
		this.itemCursor = itemCursor;
	}

	@Override
	public boolean hasNext() {
		if (!didNext) {
			hasNext = itemCursor.next();
			didNext = true;
		}
		return hasNext;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T next() {
		if (!didNext) {
			itemCursor.next();
		}
		didNext = false;

		Object object = itemCursor.get(0);
		return (T) object;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
