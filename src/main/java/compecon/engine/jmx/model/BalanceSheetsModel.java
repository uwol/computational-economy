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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.engine.Agent;
import compecon.engine.AgentFactory;
import compecon.nature.materia.GoodType;

public class BalanceSheetsModel extends Model {

	protected Map<Agent, BalanceSheet> balanceSheets = new HashMap<Agent, BalanceSheet>();

	protected Map<Currency, Map<Class<? extends Agent>, BalanceSheet>> nationalAccountsBalanceSheets;

	protected final PeriodDataAccumulatorTimeSeriesModel<Currency> moneySupplyM0Model;

	protected final PeriodDataAccumulatorTimeSeriesModel<Currency> moneySupplyM1Model;

	public BalanceSheetsModel(
			PeriodDataAccumulatorTimeSeriesModel<Currency> moneySupplyM0Model,
			PeriodDataAccumulatorTimeSeriesModel<Currency> moneySupplyM1Model) {

		this.moneySupplyM0Model = moneySupplyM0Model;
		this.moneySupplyM1Model = moneySupplyM1Model;
		this.resetNationalAccountsBalanceSheets();
	}

	public void agent_onPublishBalanceSheet(Agent agent,
			BalanceSheet balanceSheet) {
		this.balanceSheets.put(agent, balanceSheet);

		BalanceSheet nationalAccountsBalanceSheet = this.nationalAccountsBalanceSheets
				.get(balanceSheet.referenceCurrency).get(agent.getClass());

		// assets
		nationalAccountsBalanceSheet.hardCash += balanceSheet.hardCash;
		nationalAccountsBalanceSheet.cash += balanceSheet.cash;
		nationalAccountsBalanceSheet.bonds += balanceSheet.bonds;
		nationalAccountsBalanceSheet.bankLoans += balanceSheet.bankLoans;

		for (Entry<GoodType, Double> entry : balanceSheet.inventory.entrySet()) {
			// initialize
			if (!nationalAccountsBalanceSheet.inventory.containsKey(entry
					.getKey()))
				nationalAccountsBalanceSheet.inventory.put(entry.getKey(), 0.0);

			// store amount
			Double oldValue = nationalAccountsBalanceSheet.inventory.get(entry
					.getKey());
			Double newValue = oldValue + entry.getValue();
			nationalAccountsBalanceSheet.inventory
					.put(entry.getKey(), newValue);
		}

		// liabilities
		nationalAccountsBalanceSheet.loans += balanceSheet.loans;
		nationalAccountsBalanceSheet.financialLiabilities += balanceSheet.financialLiabilities;
		nationalAccountsBalanceSheet.bankBorrowings += balanceSheet.bankBorrowings;

		// equity
		nationalAccountsBalanceSheet.issuedCapital
				.addAll(balanceSheet.issuedCapital);

		this.moneySupplyM0Model.add(balanceSheet.referenceCurrency,
				balanceSheet.hardCash);
		this.moneySupplyM1Model.add(balanceSheet.referenceCurrency,
				balanceSheet.bankBorrowings + balanceSheet.hardCash);
	}

	public void notifyAgent_onDeconstruct(Agent agent) {
		this.balanceSheets.remove(agent);
	}

	private void resetNationalAccountsBalanceSheets() {
		this.nationalAccountsBalanceSheets = new HashMap<Currency, Map<Class<? extends Agent>, BalanceSheet>>();

		for (Currency currency : Currency.values()) {
			Map<Class<? extends Agent>, BalanceSheet> balanceSheetsForAgentTypes = new HashMap<Class<? extends Agent>, BalanceSheet>();
			for (Class<? extends Agent> agentType : AgentFactory.agentTypes) {
				balanceSheetsForAgentTypes.put(agentType, new BalanceSheet(
						currency));
			}

			this.nationalAccountsBalanceSheets.put(currency,
					balanceSheetsForAgentTypes);
		}
	}

	public void nextPeriod() {
		this.notifyListeners();
		this.resetNationalAccountsBalanceSheets();
	}

	public Map<Agent, BalanceSheet> getBalanceSheets() {
		return balanceSheets;
	}

	public Map<Currency, Map<Class<? extends Agent>, BalanceSheet>> getNationalAccountsBalanceSheets() {
		return nationalAccountsBalanceSheets;
	}
}
