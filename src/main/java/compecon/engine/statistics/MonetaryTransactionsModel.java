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

package compecon.engine.statistics;

import java.util.HashMap;
import java.util.Map;

import compecon.economy.agent.Agent;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.statistics.accumulator.PeriodDataAccumulator;

public class MonetaryTransactionsModel extends NotificationListenerModel {

	// stores transaction values in a type-safe way
	protected Map<Class<? extends Agent>, Map<Class<? extends Agent>, PeriodDataAccumulator>> adjacencyMatrix = new HashMap<Class<? extends Agent>, Map<Class<? extends Agent>, PeriodDataAccumulator>>();

	public MonetaryTransactionsModel() {
		// from
		for (Class<? extends Agent> agentTypeFrom : ApplicationContext
				.getInstance().getAgentFactory().getAgentTypes()) {
			Map<Class<? extends Agent>, PeriodDataAccumulator> toMap = new HashMap<Class<? extends Agent>, PeriodDataAccumulator>();
			this.adjacencyMatrix.put(agentTypeFrom, toMap);

			// to
			for (Class<? extends Agent> agentTypeTo : ApplicationContext
					.getInstance().getAgentFactory().getAgentTypes()) {
				toMap.put(agentTypeTo, new PeriodDataAccumulator());
			}
		}
	}

	public void nextPeriod() {
		this.notifyListeners();

		for (Class<? extends Agent> agentTypeFrom : ApplicationContext
				.getInstance().getAgentFactory().getAgentTypes()) {
			for (Class<? extends Agent> agentTypeTo : ApplicationContext
					.getInstance().getAgentFactory().getAgentTypes()) {
				this.adjacencyMatrix.get(agentTypeFrom).get(agentTypeTo)
						.reset();
			}
		}
	}

	public void bank_onTransfer(Class<? extends Agent> from,
			Class<? extends Agent> to, Currency currency, double value) {
		this.adjacencyMatrix.get(from).get(to).add(value);
	}

	public Map<Class<? extends Agent>, Map<Class<? extends Agent>, PeriodDataAccumulator>> getAdjacencyMatrix() {
		return adjacencyMatrix;
	}
}
