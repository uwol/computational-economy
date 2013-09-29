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

package compecon.economy.sectors.state.law.property;

import java.util.HashMap;

import compecon.economy.sectors.Agent;
import compecon.economy.sectors.financial.Currency;

public class HardCashRegister {

	private static HardCashRegister instance;

	private HashMap<Agent, HashMap<Currency, Double>> balances = new HashMap<Agent, HashMap<Currency, Double>>();

	private HardCashRegister() {
		super();
	}

	public static HardCashRegister getInstance() {
		if (instance == null)
			instance = new HardCashRegister();
		return instance;
	}

	public double getBalance(Agent agent, Currency currency) {
		this.assureAgentHasBalances(agent);

		HashMap<Currency, Double> balancesForIAgent = this.balances.get(agent);
		double balance = 0;
		if (balancesForIAgent.containsKey(currency))
			balance = balancesForIAgent.get(currency);

		return balance;
	}

	public double increment(Agent agent, Currency currency, double amount) {
		this.assureAgentHasBalances(agent);

		assert (amount > 0.0);

		double oldBalance = this.getBalance(agent, currency);
		double newBalance = oldBalance + amount;
		this.balances.get(agent).put(currency, newBalance);
		return newBalance;
	}

	public double decrement(Agent agent, Currency currency, double amount) {
		this.assureAgentHasBalances(agent);

		assert (amount >= 0.0);

		double oldBalance = this.getBalance(agent, currency);

		assert (oldBalance >= amount);

		double newBalance = oldBalance - amount;
		this.balances.get(agent).put(currency, newBalance);
		return newBalance;
	}

	private void assureAgentHasBalances(Agent agent) {
		if (!this.balances.containsKey(agent))
			this.balances.put(agent, new HashMap<Currency, Double>());
	}

	/*
	 * deregister
	 */
	public void deregister(Agent agent) {
		this.balances.remove(agent); // TODO transfer to other agent?
	}
}
