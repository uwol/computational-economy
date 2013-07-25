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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.state.law.property.Property;
import compecon.culture.sectors.state.law.property.PropertyRegister;
import compecon.engine.Agent;
import compecon.engine.dao.DAOFactory;
import compecon.engine.jmx.Log;
import compecon.nature.materia.GoodType;

public class AgentDetailModel extends Model {

	protected BankAccount currentBankAccount = null;

	protected final int MESSAGES_TO_STORE = 200;

	protected DateFormat iso8601DateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	protected ArrayList<Agent> agents = new ArrayList<Agent>();

	protected LinkedList<String> messagesForAgentEvents = new LinkedList<String>();

	protected Map<BankAccount, LinkedList<String>> messagesForBankAccountEvents = new HashMap<BankAccount, LinkedList<String>>();

	public void agent_onConstruct(Agent agent) {
		this.agents.add(agent);
		this.notifyListeners();
	}

	public void agent_onDeconstruct(Agent agent) {
		this.agents.remove(agent);
		this.notifyListeners();
	}

	public void logAgentEvent(Date date, String message) {
		messagesForAgentEvents.add(iso8601DateFormat.format(date) + "     "
				+ message);
		if (messagesForAgentEvents.size() > MESSAGES_TO_STORE)
			messagesForAgentEvents.poll();
	}

	public void logBankAccountEvent(Date date, BankAccount bankAccount,
			String message) {
		if (!this.messagesForBankAccountEvents.containsKey(bankAccount))
			this.messagesForBankAccountEvents.put(bankAccount,
					new LinkedList<String>());

		Queue<String> messagesForBankAccount = this.messagesForBankAccountEvents
				.get(bankAccount);
		messagesForBankAccount.add(iso8601DateFormat.format(date) + "     "
				+ message);
		if (messagesForBankAccount.size() > MESSAGES_TO_STORE)
			messagesForBankAccount.poll();

		this.notifyListeners();
	}

	public void setCurrentAgent(Integer agentId) {
		Agent selectedAgent = this.agents.get(agentId);
		this.messagesForAgentEvents.clear();
		this.messagesForBankAccountEvents.clear();
		this.currentBankAccount = null;
		Log.setAgentSelectedByClient(selectedAgent);
		this.notifyListeners();
	}

	public void setCurrentBankAccount(Integer bankAccountId) {
		if (bankAccountId >= 0) {
			List<BankAccount> bankAccounts = this
					.getBankAccountsOfCurrentAgent();
			this.currentBankAccount = bankAccounts.get(bankAccountId);
		}
		this.notifyListeners();
	}

	public void resetCurrentBankAccount() {
		this.currentBankAccount = null;
		this.notifyListeners();
	}

	public LinkedList<String> getMessages() {
		if (this.currentBankAccount != null) {
			if (this.messagesForBankAccountEvents
					.containsKey(currentBankAccount)) {
				return this.messagesForBankAccountEvents
						.get(currentBankAccount);
			} else
				return new LinkedList<String>();
		} else
			return this.messagesForAgentEvents;
	}

	public List<Agent> getAgents() {
		return this.agents;
	}

	public List<BankAccount> getBankAccountsOfCurrentAgent() {
		Agent agent = Log.getAgentSelectedByClient();
		if (agent != null)
			return DAOFactory.getBankAccountDAO().findAllBankAccountsOfAgent(
					agent);
		return new ArrayList<BankAccount>();
	}

	public Map<GoodType, Double> getGoodsOfCurrentAgent() {
		Agent agent = Log.getAgentSelectedByClient();
		if (agent != null)
			return PropertyRegister.getInstance().getBalance(agent);
		return new HashMap<GoodType, Double>();
	}

	public List<Property> getPropertiesOfCurrentAgent() {
		Agent agent = Log.getAgentSelectedByClient();
		if (agent != null)
			return PropertyRegister.getInstance().getProperties(agent);
		return new ArrayList<Property>();
	}
}
