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

package io.github.uwol.compecon.engine.statistics;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.github.uwol.compecon.economy.agent.Agent;
import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.property.Property;
import io.github.uwol.compecon.economy.sectors.financial.BankAccount;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.log.Log;

public class AgentDetailModel extends NotificationListenerModel {

	public class AgentLog {

		private final String logTitle;

		private final LinkedList<String> rows = new LinkedList<String>();

		protected final int ROWS_TO_STORE = 200;

		public AgentLog(final String logTitle) {
			this.logTitle = logTitle;
		}

		public String getLogTitle() {
			return logTitle;
		}

		public LinkedList<String> getRows() {
			return rows;
		}

		public void log(final String row) {
			rows.add(row);

			if (rows.size() > ROWS_TO_STORE) {
				rows.poll();
			}
		}

		@Override
		public String toString() {
			return logTitle;
		}
	}

	protected AgentLog agentLog = new AgentLog("Agent");

	protected ArrayList<Agent> agents = new ArrayList<Agent>();

	protected Map<BankAccount, AgentLog> bankAccountLogs = new HashMap<BankAccount, AgentLog>();

	protected AgentLog currentLog = agentLog;

	protected DateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public void agent_onConstruct(final Agent agent) {
		agents.add(agent);
		notifyListeners();
	}

	public void agent_onDeconstruct(final Agent agent) {
		agents.remove(agent);
		notifyListeners();
	}

	public List<Agent> getAgents() {
		return agents;
	}

	public List<BankAccount> getBankAccountsOfCurrentAgent() {
		final Agent agent = getLog().getAgentSelectedByClient();

		if (agent != null) {
			return ApplicationContext.getInstance().getBankAccountDAO().findAllBankAccountsOfAgent(agent);
		}

		return new ArrayList<BankAccount>();
	}

	public AgentLog getCurrentLog() {
		return currentLog;
	}

	public Map<GoodType, Double> getGoodsOfCurrentAgent() {
		final Agent agent = getLog().getAgentSelectedByClient();

		if (agent != null) {
			return ApplicationContext.getInstance().getPropertyService().getGoodTypeBalances(agent);
		}

		return new HashMap<GoodType, Double>();
	}

	private Log getLog() {
		return ApplicationContext.getInstance().getLog();
	}

	public List<AgentLog> getLogsOfCurrentAgent() {
		final List<AgentLog> logs = new ArrayList<AgentLog>();
		logs.add(agentLog);

		for (final AgentLog bankAccountLog : bankAccountLogs.values()) {
			logs.add(bankAccountLog);
		}

		return logs;
	}

	public List<Property> getPropertiesOfCurrentAgent() {
		final Agent agent = getLog().getAgentSelectedByClient();

		if (agent != null) {
			return ApplicationContext.getInstance().getPropertyService().findAllPropertiesOfPropertyOwner(agent);
		}

		return new ArrayList<Property>();
	}

	public void logAgentEvent(final Date date, final String message) {
		agentLog.log(iso8601DateFormat.format(date) + "     " + message);
		notifyListeners();
	}

	public void logBankAccountEvent(final Date date, final BankAccount bankAccount, final String message) {
		bankAccountLogs.get(bankAccount).log(iso8601DateFormat.format(date) + "     " + message);
		notifyListeners();
	}

	public void setCurrentAgent(final Integer agentId) {
		final Agent selectedAgent = agents.get(agentId);
		getLog().setAgentSelectedByClient(selectedAgent);

		agentLog = new AgentLog("Agent");
		bankAccountLogs.clear();

		for (final BankAccount bankAccount : getBankAccountsOfCurrentAgent()) {
			bankAccountLogs.put(bankAccount, new AgentLog(
					bankAccount.getName() + " [" + bankAccount.getId() + ", " + bankAccount.getCurrency() + "]"));
		}

		currentLog = agentLog;
		notifyListeners();
	}

	public void setCurrentLog(final AgentLog log) {
		currentLog = log;
		notifyListeners();
	}
}
