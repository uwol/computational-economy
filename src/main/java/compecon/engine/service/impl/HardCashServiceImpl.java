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

package compecon.engine.service.impl;

import java.util.HashMap;

import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.financial.HardCashOwner;
import compecon.engine.service.HardCashService;

public class HardCashServiceImpl implements HardCashService {

	// TODO Services have to be stateless, move state into DAO / database
	private final HashMap<HardCashOwner, HashMap<Currency, Double>> balances = new HashMap<HardCashOwner, HashMap<Currency, Double>>();

	private void assureAgentHasBalances(final HardCashOwner owner) {
		if (!balances.containsKey(owner)) {
			balances.put(owner, new HashMap<Currency, Double>());
		}
	}

	@Override
	public double decrement(final HardCashOwner owner, final Currency currency,
			final double amount) {
		assureAgentHasBalances(owner);

		assert (amount >= 0.0);

		final double oldBalance = getBalance(owner, currency);

		assert (oldBalance >= amount);

		final double newBalance = oldBalance - amount;
		balances.get(owner).put(currency, newBalance);
		return newBalance;
	}

	/*
	 * deregister
	 */
	@Override
	public void deregister(final HardCashOwner owner) {
		balances.remove(owner); // TODO transfer to other agent?
	}

	@Override
	public double getBalance(final HardCashOwner owner, final Currency currency) {
		assureAgentHasBalances(owner);

		final HashMap<Currency, Double> balancesForIAgent = balances.get(owner);
		double balance = 0;
		if (balancesForIAgent.containsKey(currency)) {
			balance = balancesForIAgent.get(currency);
		}

		return balance;
	}

	@Override
	public double increment(final HardCashOwner owner, final Currency currency,
			final double amount) {
		assureAgentHasBalances(owner);

		assert (amount > 0.0);

		final double oldBalance = getBalance(owner, currency);
		final double newBalance = oldBalance + amount;
		balances.get(owner).put(currency, newBalance);
		return newBalance;
	}
}
