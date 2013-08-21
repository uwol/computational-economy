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
import compecon.engine.AgentFactory;
import compecon.engine.jmx.model.timeseries.PeriodDataAccumulatorTimeSeriesModel;
import compecon.engine.time.TimeSystem;

public class NumberOfAgentsModel extends NotificationListenerModel {

	protected final Currency referenceCurrency;

	protected final Map<Class<? extends Agent>, Integer> numberOfAgents = new HashMap<Class<? extends Agent>, Integer>();

	protected final Map<Class<? extends Agent>, PeriodDataAccumulatorTimeSeriesModel> timeSeriesModel = new HashMap<Class<? extends Agent>, PeriodDataAccumulatorTimeSeriesModel>();

	public NumberOfAgentsModel(Currency referenceCurrency) {
		this.referenceCurrency = referenceCurrency;
		for (Class<? extends Agent> agentType : AgentFactory.agentTypes) {
			this.timeSeriesModel
					.put(agentType, new PeriodDataAccumulatorTimeSeriesModel(
							referenceCurrency.getIso4217Code() + " "
									+ agentType.getSimpleName()));
		}
	}

	public void agent_onConstruct(Class<? extends Agent> agentType) {
		// store number of agents
		int numberOfAgentsForAgentType = 0;
		if (this.numberOfAgents.containsKey(agentType))
			numberOfAgentsForAgentType = this.numberOfAgents.get(agentType);
		numberOfAgentsForAgentType++;
		this.numberOfAgents.put(agentType, numberOfAgentsForAgentType);

		this.notifyListeners();
	}

	public void agent_onDeconstruct(Class<? extends Agent> agentType) {
		int numberOfAgentsForAgentType = 0;
		if (this.numberOfAgents.containsKey(agentType))
			numberOfAgentsForAgentType = this.numberOfAgents.get(agentType);
		numberOfAgentsForAgentType--;
		this.numberOfAgents.put(agentType, numberOfAgentsForAgentType);

		this.notifyListeners();
	}

	public int getNumberOfAgents(Class<? extends Agent> agentType) {
		if (this.numberOfAgents.containsKey(agentType))
			return numberOfAgents.get(agentType);
		return 0;
	}

	public TimeSeries getNumberOfAgentsTimeSeries(
			Class<? extends Agent> agentType) {
		return this.timeSeriesModel.get(agentType).getTimeSeries();
	}

	public void nextPeriod() {
		for (Entry<Class<? extends Agent>, Integer> entry : numberOfAgents
				.entrySet()) {
			this.timeSeriesModel
					.get(entry.getKey())
					.getTimeSeries()
					.addOrUpdate(
							new Day(TimeSystem.getInstance().getCurrentDate()),
							entry.getValue());
		}
	}
}
