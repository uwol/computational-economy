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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import compecon.culture.sectors.financial.CentralBank;
import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.household.Household;
import compecon.culture.sectors.industry.Factory;
import compecon.culture.sectors.state.State;
import compecon.culture.sectors.trading.Trader;
import compecon.engine.Agent;

public class MonetaryTransactionsModel extends Model {

	// stores transaction values in a type-safe way
	protected Map<Currency, Map<Class<? extends Agent>, PeriodDataAccumulatorSet<Class<? extends Agent>>>> adjacencyMatrix = new HashMap<Currency, Map<Class<? extends Agent>, PeriodDataAccumulatorSet<Class<? extends Agent>>>>();

	protected List<Class<? extends Agent>> agentTypes = new ArrayList<Class<? extends Agent>>();

	public MonetaryTransactionsModel() {

		this.agentTypes.add(Household.class);
		this.agentTypes.add(Factory.class);
		this.agentTypes.add(CreditBank.class);
		this.agentTypes.add(CentralBank.class);
		this.agentTypes.add(State.class);
		this.agentTypes.add(Trader.class);

		for (Currency currency : Currency.values()) {
			// initialize data structure for currency
			Map<Class<? extends Agent>, PeriodDataAccumulatorSet<Class<? extends Agent>>> adjacencyMatrixForCurrency = new HashMap<Class<? extends Agent>, PeriodDataAccumulatorSet<Class<? extends Agent>>>();
			// from
			for (Class<? extends Agent> agentTypeFrom : this.agentTypes) {
				// to
				PeriodDataAccumulatorSet<Class<? extends Agent>> periodDataAccumulatorSet = new PeriodDataAccumulatorSet<Class<? extends Agent>>();
				for (Class<? extends Agent> agentTypeTo : this.agentTypes) {
					periodDataAccumulatorSet.add(agentTypeTo, 0);
				}

				adjacencyMatrixForCurrency.put(agentTypeFrom,
						periodDataAccumulatorSet);
			}
			this.adjacencyMatrix.put(currency, adjacencyMatrixForCurrency);

		}
	}

	public void nextPeriod() {
		this.notifyListeners();
	}

	public void bank_onTransfer(Class<? extends Agent> from,
			Class<? extends Agent> to, Currency currency, double value) {
		Map<Class<? extends Agent>, PeriodDataAccumulatorSet<Class<? extends Agent>>> adjacencyMatrixForCurrency = this.adjacencyMatrix
				.get(currency);
		adjacencyMatrixForCurrency.get(from).add(to, value);
	}

	public List<Class<? extends Agent>> getAgentTypes() {
		return agentTypes;
	}

	public Map<Currency, Map<Class<? extends Agent>, PeriodDataAccumulatorSet<Class<? extends Agent>>>> getAdjacencyMatrix() {
		return adjacencyMatrix;
	}
}
