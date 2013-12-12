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
	private HashMap<HardCashOwner, HashMap<Currency, Double>> balances = new HashMap<HardCashOwner, HashMap<Currency, Double>>();

	public double getBalance(final HardCashOwner owner, final Currency currency) {
		this.assureAgentHasBalances(owner);

		HashMap<Currency, Double> balancesForIAgent = this.balances.get(owner);
		double balance = 0;
		if (balancesForIAgent.containsKey(currency))
			balance = balancesForIAgent.get(currency);

		return balance;
	}

	public double increment(final HardCashOwner owner, final Currency currency,
			final double amount) {
		this.assureAgentHasBalances(owner);

		assert (amount > 0.0);

		double oldBalance = this.getBalance(owner, currency);
		double newBalance = oldBalance + amount;
		this.balances.get(owner).put(currency, newBalance);
		return newBalance;
	}

	public double decrement(final HardCashOwner owner, final Currency currency,
			final double amount) {
		this.assureAgentHasBalances(owner);

		assert (amount >= 0.0);

		double oldBalance = this.getBalance(owner, currency);

		assert (oldBalance >= amount);

		double newBalance = oldBalance - amount;
		this.balances.get(owner).put(currency, newBalance);
		return newBalance;
	}

	private void assureAgentHasBalances(final HardCashOwner owner) {
		if (!this.balances.containsKey(owner))
			this.balances.put(owner, new HashMap<Currency, Double>());
	}

	/*
	 * deregister
	 */
	public void deregister(final HardCashOwner owner) {
		this.balances.remove(owner); // TODO transfer to other agent?
	}
}
