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

package compecon.engine.statistics.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import compecon.economy.sectors.Agent;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.state.law.property.Property;
import compecon.economy.sectors.state.law.property.PropertyRegister;
import compecon.engine.Simulation;
import compecon.engine.dao.DAOFactory;
import compecon.engine.statistics.Log;
import compecon.materia.GoodType;

public class AgentDetailModel extends NotificationListenerModel {

	public class AgentLog {

		protected final int ROWS_TO_STORE = 200;

		private String logTitle;

		private LinkedList<String> rows = new LinkedList<String>();

		public AgentLog(String logTitle) {
			this.logTitle = logTitle;
		}

		public String getLogTitle() {
			return logTitle;
		}

		public void log(String row) {
			this.rows.add(row);
			if (this.rows.size() > ROWS_TO_STORE)
				this.rows.poll();
		}

		public LinkedList<String> getRows() {
			return rows;
		}

		@Override
		public String toString() {
			return this.logTitle;
		}
	}

	protected DateFormat iso8601DateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	protected ArrayList<Agent> agents = new ArrayList<Agent>();

	protected AgentLog agentLog = new AgentLog("Agent");

	protected AgentLog currentLog = agentLog;

	protected Map<BankAccount, AgentLog> bankAccountLogs = new HashMap<BankAccount, AgentLog>();

	public void agent_onConstruct(Agent agent) {
		this.agents.add(agent);
		this.notifyListeners();
	}

	public void agent_onDeconstruct(Agent agent) {
		this.agents.remove(agent);
		this.notifyListeners();
	}

	public void logAgentEvent(Date date, String message) {
		this.agentLog.log(iso8601DateFormat.format(date) + "     " + message);
		this.notifyListeners();
	}

	public void logBankAccountEvent(Date date, BankAccount bankAccount,
			String message) {
		this.bankAccountLogs.get(bankAccount).log(
				iso8601DateFormat.format(date) + "     " + message);
		this.notifyListeners();
	}

	public List<Agent> getAgents() {
		return this.agents;
	}

	public List<AgentLog> getLogsOfCurrentAgent() {
		final List<AgentLog> logs = new ArrayList<AgentLog>();
		logs.add(this.agentLog);
		for (AgentLog bankAccountLog : this.bankAccountLogs.values())
			logs.add(bankAccountLog);
		return logs;
	}

	public List<BankAccount> getBankAccountsOfCurrentAgent() {
		final Agent agent = getLog().getAgentSelectedByClient();
		if (agent != null)
			return DAOFactory.getBankAccountDAO().findAllBankAccountsOfAgent(
					agent);
		return new ArrayList<BankAccount>();
	}

	public Map<GoodType, Double> getGoodsOfCurrentAgent() {
		Agent agent = getLog().getAgentSelectedByClient();
		if (agent != null)
			return PropertyRegister.getInstance().getBalance(agent);
		return new HashMap<GoodType, Double>();
	}

	public List<Property> getPropertiesOfCurrentAgent() {
		Agent agent = getLog().getAgentSelectedByClient();
		if (agent != null)
			return PropertyRegister.getInstance().getProperties(agent);
		return new ArrayList<Property>();
	}

	public AgentLog getCurrentLog() {
		return this.currentLog;
	}

	public void setCurrentLog(AgentLog log) {
		this.currentLog = log;
		this.notifyListeners();
	}

	public void setCurrentAgent(final Integer agentId) {
		final Agent selectedAgent = this.agents.get(agentId);
		getLog().setAgentSelectedByClient(selectedAgent);

		this.agentLog = new AgentLog("Agent");
		this.bankAccountLogs.clear();

		for (BankAccount bankAccount : this.getBankAccountsOfCurrentAgent()) {
			this.bankAccountLogs.put(
					bankAccount,
					new AgentLog(bankAccount.getName() + " ["
							+ bankAccount.getId() + ", "
							+ bankAccount.getCurrency() + "]"));
		}

		this.currentLog = agentLog;
		this.notifyListeners();
	}

	private Log getLog() {
		return Simulation.getInstance().getLog();
	}
}
