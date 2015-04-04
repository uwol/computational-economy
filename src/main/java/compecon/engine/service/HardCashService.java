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

package compecon.engine.service;

import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.financial.HardCashOwner;

public interface HardCashService {

	public double decrement(final HardCashOwner owner, final Currency currency,
			final double amount);

	public void deregister(final HardCashOwner owner);

	public double getBalance(final HardCashOwner owner, final Currency currency);

	public double increment(final HardCashOwner owner, final Currency currency,
			final double amount);
}
