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

package compecon.engine.jmx.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;

import compecon.economy.sectors.financial.Currency;
import compecon.engine.Agent;
import compecon.engine.jmx.model.generic.PeriodDataTimeSeriesModel;
import compecon.engine.time.TimeSystem;

public class NumberOfAgentsModel extends Model {

	Map<Currency, Map<Class<? extends Agent>, Integer>> numberOfAgents = new HashMap<Currency, Map<Class<? extends Agent>, Integer>>();

	Map<Currency, PeriodDataTimeSeriesModel<Class<? extends Agent>>> timeSeries = new HashMap<Currency, PeriodDataTimeSeriesModel<Class<? extends Agent>>>();

	public NumberOfAgentsModel() {
		for (Currency currency : Currency.values()) {
			this.numberOfAgents.put(currency,
					new HashMap<Class<? extends Agent>, Integer>());
			this.timeSeries.put(currency,
					new PeriodDataTimeSeriesModel<Class<? extends Agent>>());
		}
	}

	public void agent_onConstruct(Currency currency,
			Class<? extends Agent> agentType) {
		// store number of agents
		int numberOfAgentsForAgentType = 0;
		if (this.numberOfAgents.get(currency).containsKey(agentType))
			numberOfAgentsForAgentType = this.numberOfAgents.get(currency).get(
					agentType);
		numberOfAgentsForAgentType++;
		this.numberOfAgents.get(currency).put(agentType,
				numberOfAgentsForAgentType);

		this.notifyListeners();
	}

	public void agent_onDeconstruct(Currency currency,
			Class<? extends Agent> agentType) {
		int numberOfAgentsForAgentType = 0;
		if (this.numberOfAgents.get(currency).containsKey(agentType))
			numberOfAgentsForAgentType = this.numberOfAgents.get(currency).get(
					agentType);
		numberOfAgentsForAgentType--;
		this.numberOfAgents.get(currency).put(agentType,
				numberOfAgentsForAgentType);

		this.notifyListeners();
	}

	public int getNumberOfAgents(Currency currency,
			Class<? extends Agent> agentType) {
		if (this.numberOfAgents.get(currency).containsKey(agentType))
			return numberOfAgents.get(currency).get(agentType);
		return 0;
	}

	public TimeSeries getNumberOfAgentsTimeSeries(Currency currency,
			Class<? extends Agent> agentType) {
		return this.timeSeries.get(currency).getTimeSeries(agentType);
	}

	public void nextPeriod() {
		for (Currency currency : this.numberOfAgents.keySet()) {
			Map<Class<? extends Agent>, Integer> numberOfAgentsInCurrency = this.numberOfAgents
					.get(currency);
			for (Entry<Class<? extends Agent>, Integer> entry : numberOfAgentsInCurrency
					.entrySet()) {
				this.timeSeries
						.get(currency)
						.getTimeSeries(entry.getKey())
						.addOrUpdate(
								new Day(TimeSystem.getInstance()
										.getCurrentDate()), entry.getValue());
			}
		}
	}
}
