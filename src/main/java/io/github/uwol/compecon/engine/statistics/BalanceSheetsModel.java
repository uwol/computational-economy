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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.github.uwol.compecon.economy.agent.Agent;
import io.github.uwol.compecon.economy.bookkeeping.impl.BalanceSheetDTO;
import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.sectors.financial.CentralBank;
import io.github.uwol.compecon.economy.sectors.financial.CreditBank;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.economy.sectors.household.Household;
import io.github.uwol.compecon.economy.sectors.industry.Factory;
import io.github.uwol.compecon.economy.sectors.state.State;
import io.github.uwol.compecon.economy.sectors.trading.Trader;

public class BalanceSheetsModel extends NotificationListenerModel {

	protected BalanceSheetDTO centralBankBalanceSheet;

	protected final Map<CreditBank, BalanceSheetDTO> creditBankBalanceSheets = new HashMap<CreditBank, BalanceSheetDTO>();

	protected final Map<GoodType, Map<Factory, BalanceSheetDTO>> factoryBalanceSheets = new HashMap<GoodType, Map<Factory, BalanceSheetDTO>>();

	protected final Map<Household, BalanceSheetDTO> householdBalanceSheets = new HashMap<Household, BalanceSheetDTO>();

	protected final Currency referenceCurrency;

	protected BalanceSheetDTO stateBalanceSheet;

	protected final Map<Trader, BalanceSheetDTO> traderBalanceSheets = new HashMap<Trader, BalanceSheetDTO>();

	public BalanceSheetsModel(final Currency referenceCurrency) {
		this.referenceCurrency = referenceCurrency;
		resetBalanceSheets();
	}

	public void agent_onPublishBalanceSheet(final Agent agent, final BalanceSheetDTO balanceSheet) {

		assert (referenceCurrency.equals(agent.getPrimaryCurrency())
				&& referenceCurrency.equals(balanceSheet.referenceCurrency));

		if (agent instanceof Household) {
			householdBalanceSheets.put((Household) agent, balanceSheet);
		} else if (agent instanceof Factory) {
			factoryBalanceSheets.get(((Factory) agent).getProducedGoodType()).put((Factory) agent, balanceSheet);
		} else if (agent instanceof Trader) {
			traderBalanceSheets.put((Trader) agent, balanceSheet);
		} else if (agent instanceof CreditBank) {
			creditBankBalanceSheets.put((CreditBank) agent, balanceSheet);
		} else if (agent instanceof CentralBank) {
			assert (centralBankBalanceSheet == null);
			centralBankBalanceSheet = balanceSheet;
		} else if (agent instanceof State) {
			assert (stateBalanceSheet == null);
			stateBalanceSheet = balanceSheet;
		} else {
			throw new RuntimeException("unexpected agent type");
		}
	}

	private void copyBalanceSheetValues(final BalanceSheetDTO from, final BalanceSheetDTO to) {
		if (from == null) {
			return;
		}

		// assets
		to.hardCash += from.hardCash;
		to.cashGiroShortTerm += from.cashGiroShortTerm;
		to.cashGiroLongTerm += from.cashGiroLongTerm;
		to.cashCentralBankShortTerm += from.cashCentralBankShortTerm;
		to.cashCentralBankLongTerm += from.cashCentralBankLongTerm;
		to.cashForeignCurrency += from.cashForeignCurrency;
		to.bonds += from.bonds;
		to.bankLoans += from.bankLoans;
		to.inventoryValue += from.inventoryValue;

		// add quantitative amount of inventory to national accounts balance
		// sheet
		for (final Entry<GoodType, Double> entry : from.inventoryQuantitative.entrySet()) {
			final GoodType goodType = entry.getKey();
			final double amount = entry.getValue();
			if (!to.inventoryQuantitative.containsKey(goodType)) {
				to.inventoryQuantitative.put(goodType, 0.0);
			}
			final double newAmount = to.inventoryQuantitative.get(goodType) + amount;
			to.inventoryQuantitative.put(goodType, newAmount);
		}

		// liabilities
		to.loansGiroShortTerm += from.loansGiroShortTerm;
		to.loansGiroLongTerm += from.loansGiroLongTerm;
		to.loansCentralBankShortTerm += from.loansCentralBankShortTerm;
		to.loansCentralBankLongTerm += from.loansCentralBankLongTerm;
		to.financialLiabilities += from.financialLiabilities;
		to.bankBorrowings += from.bankBorrowings;

		// equity
		to.issuedCapital.addAll(from.issuedCapital);
	}

	public BalanceSheetDTO getCentralBankNationalAccountsBalanceSheet() {
		final BalanceSheetDTO centralBankNationalAccountsBalanceSheet = new BalanceSheetDTO(referenceCurrency);
		copyBalanceSheetValues(centralBankBalanceSheet, centralBankNationalAccountsBalanceSheet);
		return centralBankNationalAccountsBalanceSheet;
	}

	/**
	 * aggregates balance sheets of credit banks
	 */
	public BalanceSheetDTO getCreditBankNationalAccountsBalanceSheet() {
		final BalanceSheetDTO creditBankNationalAccountsBalanceSheet = new BalanceSheetDTO(referenceCurrency);

		for (final BalanceSheetDTO balanceSheet : creditBankBalanceSheets.values()) {
			copyBalanceSheetValues(balanceSheet, creditBankNationalAccountsBalanceSheet);
		}

		return creditBankNationalAccountsBalanceSheet;
	}

	/**
	 * aggregates balance sheets of factories of all good types
	 */
	public BalanceSheetDTO getFactoryNationalAccountsBalanceSheet() {
		final BalanceSheetDTO factoryNationalAccountsBalanceSheet = new BalanceSheetDTO(referenceCurrency);

		for (final GoodType goodType : GoodType.values()) {
			copyBalanceSheetValues(getFactoryNationalAccountsBalanceSheet(goodType),
					factoryNationalAccountsBalanceSheet);
		}

		return factoryNationalAccountsBalanceSheet;
	}

	/**
	 * aggregates balance sheets of factories for good type
	 */
	public BalanceSheetDTO getFactoryNationalAccountsBalanceSheet(final GoodType goodType) {
		final BalanceSheetDTO factoryNationalAccountsBalanceSheet = new BalanceSheetDTO(referenceCurrency);

		for (final BalanceSheetDTO balanceSheet : factoryBalanceSheets.get(goodType).values()) {
			copyBalanceSheetValues(balanceSheet, factoryNationalAccountsBalanceSheet);
		}

		return factoryNationalAccountsBalanceSheet;
	}

	/**
	 * aggregates balance sheets of households
	 */
	public BalanceSheetDTO getHouseholdNationalAccountsBalanceSheet() {
		final BalanceSheetDTO householdNationalAccountsBalanceSheet = new BalanceSheetDTO(referenceCurrency);

		for (final BalanceSheetDTO balanceSheet : householdBalanceSheets.values()) {
			copyBalanceSheetValues(balanceSheet, householdNationalAccountsBalanceSheet);
		}

		return householdNationalAccountsBalanceSheet;
	}

	/**
	 * aggregates balance sheets of agents
	 */
	public BalanceSheetDTO getNationalAccountsBalanceSheet() {
		final BalanceSheetDTO nationalAccountsBalanceSheet = new BalanceSheetDTO(referenceCurrency);
		copyBalanceSheetValues(getHouseholdNationalAccountsBalanceSheet(), nationalAccountsBalanceSheet);
		copyBalanceSheetValues(getFactoryNationalAccountsBalanceSheet(), nationalAccountsBalanceSheet);
		copyBalanceSheetValues(getTraderNationalAccountsBalanceSheet(), nationalAccountsBalanceSheet);
		copyBalanceSheetValues(getCreditBankNationalAccountsBalanceSheet(), nationalAccountsBalanceSheet);
		copyBalanceSheetValues(centralBankBalanceSheet, nationalAccountsBalanceSheet);
		copyBalanceSheetValues(stateBalanceSheet, nationalAccountsBalanceSheet);
		return nationalAccountsBalanceSheet;
	}

	public Map<Class<? extends Agent>, BalanceSheetDTO> getNationalAccountsBalanceSheets() {
		final Map<Class<? extends Agent>, BalanceSheetDTO> nationalAccountsBalanceSheets = new HashMap<Class<? extends Agent>, BalanceSheetDTO>();
		nationalAccountsBalanceSheets.put(Household.class, getHouseholdNationalAccountsBalanceSheet());
		nationalAccountsBalanceSheets.put(Factory.class, getFactoryNationalAccountsBalanceSheet());
		nationalAccountsBalanceSheets.put(Trader.class, getTraderNationalAccountsBalanceSheet());
		nationalAccountsBalanceSheets.put(CreditBank.class, getCreditBankNationalAccountsBalanceSheet());
		nationalAccountsBalanceSheets.put(CentralBank.class, getCentralBankNationalAccountsBalanceSheet());
		nationalAccountsBalanceSheets.put(State.class, getStateNationalAccountsBalanceSheet());
		return nationalAccountsBalanceSheets;
	}

	public BalanceSheetDTO getStateNationalAccountsBalanceSheet() {
		final BalanceSheetDTO stateNationalAccountsBalanceSheet = new BalanceSheetDTO(referenceCurrency);
		copyBalanceSheetValues(stateBalanceSheet, stateNationalAccountsBalanceSheet);
		return stateNationalAccountsBalanceSheet;
	}

	/**
	 * aggregates balance sheets of traders
	 */
	public BalanceSheetDTO getTraderNationalAccountsBalanceSheet() {
		final BalanceSheetDTO traderNationalAccountsBalanceSheet = new BalanceSheetDTO(referenceCurrency);

		for (final BalanceSheetDTO balanceSheet : traderBalanceSheets.values()) {
			copyBalanceSheetValues(balanceSheet, traderNationalAccountsBalanceSheet);
		}

		return traderNationalAccountsBalanceSheet;
	}

	public void nextPeriod() {
		notifyListeners();
		resetBalanceSheets();
	}

	private void resetBalanceSheets() {
		householdBalanceSheets.clear();
		factoryBalanceSheets.clear();

		for (final GoodType goodType : GoodType.values()) {
			factoryBalanceSheets.put(goodType, new HashMap<Factory, BalanceSheetDTO>());
		}

		traderBalanceSheets.clear();
		creditBankBalanceSheets.clear();
		centralBankBalanceSheet = null;
		stateBalanceSheet = null;
	}
}
