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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import compecon.economy.sectors.Agent;
import compecon.economy.sectors.financial.CentralBank;
import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.economy.sectors.industry.Factory;
import compecon.economy.sectors.state.State;
import compecon.economy.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.economy.sectors.trading.Trader;
import compecon.materia.GoodType;

public class BalanceSheetsModel extends NotificationListenerModel {

	protected final Currency referenceCurrency;

	protected final Map<Household, BalanceSheet> householdBalanceSheets = new HashMap<Household, BalanceSheet>();

	protected final Map<GoodType, Map<Factory, BalanceSheet>> factoryBalanceSheets = new HashMap<GoodType, Map<Factory, BalanceSheet>>();

	protected final Map<Trader, BalanceSheet> traderBalanceSheets = new HashMap<Trader, BalanceSheet>();

	protected final Map<CreditBank, BalanceSheet> creditBankBalanceSheets = new HashMap<CreditBank, BalanceSheet>();

	protected BalanceSheet centralBankBalanceSheet;

	protected BalanceSheet stateBalanceSheet;

	public BalanceSheetsModel(Currency referenceCurrency) {
		this.referenceCurrency = referenceCurrency;
		this.resetBalanceSheets();
	}

	public void agent_onPublishBalanceSheet(Agent agent,
			BalanceSheet balanceSheet) {

		assert (referenceCurrency.equals(agent.getPrimaryCurrency()) && referenceCurrency
				.equals(balanceSheet.referenceCurrency));

		if (agent instanceof Household)
			this.householdBalanceSheets.put((Household) agent, balanceSheet);
		else if (agent instanceof Factory)
			this.factoryBalanceSheets.get(
					((Factory) agent).getProducedGoodType()).put(
					(Factory) agent, balanceSheet);
		else if (agent instanceof Trader)
			this.traderBalanceSheets.put((Trader) agent, balanceSheet);
		else if (agent instanceof CreditBank)
			this.creditBankBalanceSheets.put((CreditBank) agent, balanceSheet);
		else if (agent instanceof CentralBank
				&& this.centralBankBalanceSheet == null)
			this.centralBankBalanceSheet = balanceSheet;
		else if (agent instanceof State && this.stateBalanceSheet == null)
			this.stateBalanceSheet = balanceSheet;
		else
			throw new RuntimeException("unexpected agent type");
	}

	private void resetBalanceSheets() {
		this.householdBalanceSheets.clear();
		this.factoryBalanceSheets.clear();
		for (GoodType goodType : GoodType.values())
			this.factoryBalanceSheets.put(goodType,
					new HashMap<Factory, BalanceSheet>());
		this.traderBalanceSheets.clear();
		this.creditBankBalanceSheets.clear();
		this.centralBankBalanceSheet = null;
		this.stateBalanceSheet = null;
	}

	public void nextPeriod() {
		this.notifyListeners();
		this.resetBalanceSheets();
	}

	/**
	 * aggregates balance sheets of households
	 */
	public BalanceSheet getHouseholdNationalAccountsBalanceSheet() {
		BalanceSheet householdNationalAccountsBalanceSheet = new BalanceSheet(
				this.referenceCurrency);
		for (BalanceSheet balanceSheet : this.householdBalanceSheets.values())
			copyBalanceSheetValues(balanceSheet,
					householdNationalAccountsBalanceSheet);
		return householdNationalAccountsBalanceSheet;
	}

	/**
	 * aggregates balance sheets of factories for good type
	 */
	public BalanceSheet getFactoryNationalAccountsBalanceSheet(GoodType goodType) {
		BalanceSheet factoryNationalAccountsBalanceSheet = new BalanceSheet(
				this.referenceCurrency);
		for (BalanceSheet balanceSheet : this.factoryBalanceSheets
				.get(goodType).values())
			copyBalanceSheetValues(balanceSheet,
					factoryNationalAccountsBalanceSheet);
		return factoryNationalAccountsBalanceSheet;
	}

	/**
	 * aggregates balance sheets of factories of all good types
	 */
	public BalanceSheet getFactoryNationalAccountsBalanceSheet() {
		BalanceSheet factoryNationalAccountsBalanceSheet = new BalanceSheet(
				this.referenceCurrency);
		for (GoodType goodType : GoodType.values()) {
			copyBalanceSheetValues(
					getFactoryNationalAccountsBalanceSheet(goodType),
					factoryNationalAccountsBalanceSheet);
		}
		return factoryNationalAccountsBalanceSheet;
	}

	/**
	 * aggregates balance sheets of traders
	 */
	public BalanceSheet getTraderNationalAccountsBalanceSheet() {
		BalanceSheet traderNationalAccountsBalanceSheet = new BalanceSheet(
				this.referenceCurrency);
		for (BalanceSheet balanceSheet : this.traderBalanceSheets.values())
			copyBalanceSheetValues(balanceSheet,
					traderNationalAccountsBalanceSheet);
		return traderNationalAccountsBalanceSheet;
	}

	/**
	 * aggregates balance sheets of credit banks
	 */
	public BalanceSheet getCreditBankNationalAccountsBalanceSheet() {
		BalanceSheet creaditBankationalAccountsBalanceSheet = new BalanceSheet(
				this.referenceCurrency);
		for (BalanceSheet balanceSheet : this.creditBankBalanceSheets.values())
			copyBalanceSheetValues(balanceSheet,
					creaditBankationalAccountsBalanceSheet);
		return creaditBankationalAccountsBalanceSheet;
	}

	public BalanceSheet getCentralBankNationalAccountsBalanceSheet() {
		return this.centralBankBalanceSheet;
	}

	public BalanceSheet getStateNationalAccountsBalanceSheet() {
		return this.stateBalanceSheet;
	}

	/**
	 * aggregates balance sheets of agents
	 */
	public BalanceSheet getNationalAccountsBalanceSheet() {
		BalanceSheet nationalAccountsBalanceSheet = new BalanceSheet(
				this.referenceCurrency);
		copyBalanceSheetValues(getHouseholdNationalAccountsBalanceSheet(),
				nationalAccountsBalanceSheet);
		copyBalanceSheetValues(getFactoryNationalAccountsBalanceSheet(),
				nationalAccountsBalanceSheet);
		copyBalanceSheetValues(getTraderNationalAccountsBalanceSheet(),
				nationalAccountsBalanceSheet);
		copyBalanceSheetValues(getCreditBankNationalAccountsBalanceSheet(),
				nationalAccountsBalanceSheet);
		copyBalanceSheetValues(centralBankBalanceSheet,
				nationalAccountsBalanceSheet);
		copyBalanceSheetValues(stateBalanceSheet, nationalAccountsBalanceSheet);
		return nationalAccountsBalanceSheet;
	}

	public Map<Class<? extends Agent>, BalanceSheet> getNationalAccountsBalanceSheets() {
		Map<Class<? extends Agent>, BalanceSheet> nationalAccountsBalanceSheets = new HashMap<Class<? extends Agent>, BalanceSheet>();
		nationalAccountsBalanceSheets.put(Household.class,
				getHouseholdNationalAccountsBalanceSheet());
		nationalAccountsBalanceSheets.put(Factory.class,
				getFactoryNationalAccountsBalanceSheet());
		nationalAccountsBalanceSheets.put(Trader.class,
				getTraderNationalAccountsBalanceSheet());
		nationalAccountsBalanceSheets.put(CreditBank.class,
				getCreditBankNationalAccountsBalanceSheet());
		nationalAccountsBalanceSheets.put(CentralBank.class,
				getCentralBankNationalAccountsBalanceSheet());
		nationalAccountsBalanceSheets.put(State.class,
				getStateNationalAccountsBalanceSheet());
		return nationalAccountsBalanceSheets;
	}

	private void copyBalanceSheetValues(BalanceSheet from, BalanceSheet to) {
		// assets
		to.hardCash += from.hardCash;
		to.cashShortTerm += from.cashShortTerm;
		to.cashShortTermForeignCurrency += from.cashShortTermForeignCurrency;
		to.cashLongTerm += from.cashLongTerm;
		to.bonds += from.bonds;
		to.bankLoans += from.bankLoans;
		to.inventoryValue += from.inventoryValue;

		// add quantitative amount of inventory to national accounts balance
		// sheet
		for (Entry<GoodType, Double> entry : from.inventoryQuantitative
				.entrySet()) {
			GoodType goodType = entry.getKey();
			double amount = entry.getValue();
			if (!to.inventoryQuantitative.containsKey(goodType))
				to.inventoryQuantitative.put(goodType, 0.0);
			double newAmount = to.inventoryQuantitative.get(goodType) + amount;
			to.inventoryQuantitative.put(goodType, newAmount);
		}

		// liabilities
		to.loans += from.loans;
		to.financialLiabilities += from.financialLiabilities;
		to.bankBorrowings += from.bankBorrowings;

		// equity
		to.issuedCapital.addAll(from.issuedCapital);
	}
}
