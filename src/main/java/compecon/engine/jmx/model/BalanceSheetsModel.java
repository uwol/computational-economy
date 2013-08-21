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

import compecon.economy.sectors.financial.Bank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.engine.Agent;
import compecon.engine.AgentFactory;
import compecon.engine.jmx.model.timeseries.PeriodDataAccumulatorTimeSeriesModel;
import compecon.materia.GoodType;

public class BalanceSheetsModel extends NotificationListenerModel {

	protected Map<Class<? extends Agent>, BalanceSheet> nationalAccountsBalanceSheets = new HashMap<Class<? extends Agent>, BalanceSheet>();

	protected final Currency referenceCurrency;

	protected final PeriodDataAccumulatorTimeSeriesModel moneySupplyM0Model;

	protected final PeriodDataAccumulatorTimeSeriesModel moneySupplyM1Model;

	protected final PeriodDataAccumulatorTimeSeriesModel moneySupplyM2Model;

	public BalanceSheetsModel(Currency referenceCurrency,
			PeriodDataAccumulatorTimeSeriesModel moneySupplyM0Model,
			PeriodDataAccumulatorTimeSeriesModel moneySupplyM1Model,
			PeriodDataAccumulatorTimeSeriesModel moneySupplyM2Model) {

		this.referenceCurrency = referenceCurrency;
		this.moneySupplyM0Model = moneySupplyM0Model;
		this.moneySupplyM1Model = moneySupplyM1Model;
		this.moneySupplyM2Model = moneySupplyM2Model;
		this.resetNationalAccountsBalanceSheets();
	}

	public void agent_onPublishBalanceSheet(Agent agent,
			BalanceSheet balanceSheet) {
		if (!referenceCurrency.equals(agent.getPrimaryCurrency())
				|| !referenceCurrency.equals(balanceSheet.referenceCurrency))
			throw new RuntimeException("mismatching currencies");

		BalanceSheet nationalAccountsBalanceSheet = this.nationalAccountsBalanceSheets
				.get(agent.getClass());

		// assets
		nationalAccountsBalanceSheet.hardCash += balanceSheet.hardCash;
		nationalAccountsBalanceSheet.cashShortTerm += balanceSheet.cashShortTerm;
		nationalAccountsBalanceSheet.cashLongTerm += balanceSheet.cashLongTerm;
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

		if (!(agent instanceof Bank)) {
			this.moneySupplyM0Model.add(balanceSheet.hardCash);
			this.moneySupplyM1Model.add(balanceSheet.cashShortTerm
					+ balanceSheet.hardCash);
			this.moneySupplyM2Model.add(balanceSheet.hardCash
					+ balanceSheet.cashShortTerm + balanceSheet.cashLongTerm);
		}
	}

	private void resetNationalAccountsBalanceSheets() {
		this.nationalAccountsBalanceSheets.clear();

		for (Class<? extends Agent> agentType : AgentFactory.agentTypes) {
			this.nationalAccountsBalanceSheets.put(agentType, new BalanceSheet(
					this.referenceCurrency));
		}
	}

	public void nextPeriod() {
		this.notifyListeners();
		this.resetNationalAccountsBalanceSheets();
	}

	public Map<Class<? extends Agent>, BalanceSheet> getNationalAccountsBalanceSheet() {
		return this.nationalAccountsBalanceSheets;
	}
}
