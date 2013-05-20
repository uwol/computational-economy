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

package compecon.engine.jmx.mbean;

import compecon.culture.sectors.financial.CentralBank;
import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.household.Household;
import compecon.culture.sectors.industry.Factory;
import compecon.culture.sectors.state.State;
import compecon.engine.jmx.model.ModelRegistry;

public class JmxAgents implements JmxAgentsMBean {

	public int getNumberOfHouseholds() {
		return ModelRegistry.getNumberOfAgentsModel().getNumberOfAgents()
				.get(Household.class);
	}

	public int getNumberOfFactories() {
		return ModelRegistry.getNumberOfAgentsModel().getNumberOfAgents()
				.get(Factory.class);
	}

	public int getNumberOfCreditBanks() {
		return ModelRegistry.getNumberOfAgentsModel().getNumberOfAgents()
				.get(CreditBank.class);
	}

	public int getNumberOfCentralBanks() {
		return ModelRegistry.getNumberOfAgentsModel().getNumberOfAgents()
				.get(CentralBank.class);
	}

	public int getNumberOfStates() {
		return ModelRegistry.getNumberOfAgentsModel().getNumberOfAgents()
				.get(State.class);
	}
}
