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

package compecon.engine.dashboard.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

import compecon.culture.sectors.financial.CentralBank;
import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.household.Household;
import compecon.culture.sectors.industry.Factory;
import compecon.culture.sectors.state.State;
import compecon.culture.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.engine.Agent;
import compecon.nature.materia.GoodType;

public class BalanceSheetsModel {

	public class NationalAccountsTableModel extends AbstractTableModel {

		public final Currency referenceCurrency;

		public final static int SIDE_ACTIVE = 0;
		public final static int SIDE_PASSIVE = 5;

		public final static int POSITION_HARD_CASH = 0;
		public final static int POSITION_CASH = 1;
		public final static int POSITION_BONDS = 2;
		public final static int POSITION_BANK_LOANS = 3;
		public final static int POSITION_GOODTYPE_LABOURHOUR = 5;
		public final static int POSITION_GOODTYPE_MEGACALORIE = 6;
		public final static int POSITION_GOODTYPE_KILOWATT = 7;

		public final static int POSITION_LOANS = 1;
		public final static int POSITION_FIN_LIABLITIES = 2;
		public final static int POSITION_BANK_BORROWINGS = 3;

		public final static int AGENTTYPE_HOUSEHOLD = 0;
		public final static int AGENTTYPE_FACTORY = 1;
		public final static int AGENTTYPE_CREDITBANK = 2;
		public final static int AGENTTYPE_CENTRALBANK = 3;
		public final static int AGENTTYPE_STATE = 4;

		protected final String columnNames[] = { "Active Account",
				"Agent Type", "Value", "Currency", "", "Passive Account",
				"Agent Type", "Value", "Currency" };

		protected final String[] agentTypeNames = { "Household", "Factory",
				"Credit Bank", "Central Bank", "State" };

		protected final String[] activePositionNames = { "Hard Cash", "Cash",
				"Fin. Assets (Bonds)", "Bank Loans", "", "Labour Hour",
				"Mega Calorie", "Kilo Watt" };

		protected final String[] passivePositionNames = { "", "Loans",
				"Fin. Liabilities (Bonds)", "Bank Borrowings", "", "", "", "" };

		protected Object[][] cells;

		public NationalAccountsTableModel(Currency referenceCurrency) {
			this.referenceCurrency = referenceCurrency;

			this.cells = new Object[this.getRowCount()][this.getColumnCount()];

			for (int i = 0; i < Math.max(this.activePositionNames.length,
					this.passivePositionNames.length); i++) {
				// position name
				cells[i * this.agentTypeNames.length][SIDE_ACTIVE] = this.activePositionNames[i];
				cells[i * this.agentTypeNames.length][SIDE_PASSIVE] = this.passivePositionNames[i];

				for (int j = 0; j < this.agentTypeNames.length; j++) {
					// agent type name
					if (this.activePositionNames[i] != "")
						cells[i * this.agentTypeNames.length + j][SIDE_ACTIVE + 1] = this.agentTypeNames[j];
					if (this.passivePositionNames[i] != "")
						cells[i * this.agentTypeNames.length + j][SIDE_PASSIVE + 1] = this.agentTypeNames[j];

					// currency name
					if (this.activePositionNames[i] != "") {
						if (i < 4)
							cells[i * this.agentTypeNames.length + j][SIDE_ACTIVE + 3] = this.referenceCurrency
									.getIso4217Code();
						else
							cells[i * this.agentTypeNames.length + j][SIDE_ACTIVE + 3] = "Units";
					}
					if (this.passivePositionNames[i] != "")
						cells[i * this.agentTypeNames.length + j][SIDE_PASSIVE + 3] = this.referenceCurrency
								.getIso4217Code();

				}
			}
		}

		@Override
		public int getColumnCount() {
			return this.columnNames.length;
		}

		@Override
		public int getRowCount() {
			return this.agentTypeNames.length
					* Math.max(this.activePositionNames.length,
							this.passivePositionNames.length);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return this.cells[rowIndex][columnIndex];
		}

		@Override
		public String getColumnName(int columnIndex) {
			return this.columnNames[columnIndex];
		}

		public void setValue(final int sideNr, final int positionTypeNr,
				final int agentTypeNr, final Currency currency,
				final double value) {
			if (!currency.equals(this.referenceCurrency))
				return;

			int rowNumber = calculateRowNumber(positionTypeNr, agentTypeNr);
			this.cells[rowNumber][sideNr + 2] = Currency.round(value);
			fireTableCellUpdated(rowNumber, sideNr + 2);
		}

		public int calculateRowNumber(final int positionTypeNr,
				final int agentTypeNr) {
			return (positionTypeNr * agentTypeNames.length) + agentTypeNr;
		}
	}

	protected Map<Agent, BalanceSheet> balanceSheets = new HashMap<Agent, BalanceSheet>();

	protected Map<Currency, Map<Class<? extends Agent>, BalanceSheet>> nationalAccountsBalanceSheets;

	protected final Map<Currency, NationalAccountsTableModel> nationalAccountsTableModels = new HashMap<Currency, NationalAccountsTableModel>();

	protected final PeriodDataAccumulatorTimeSeriesModel<Currency> moneySupplyM0Model;

	protected final PeriodDataAccumulatorTimeSeriesModel<Currency> moneySupplyM1Model;

	public BalanceSheetsModel(
			PeriodDataAccumulatorTimeSeriesModel<Currency> moneySupplyM0Model,
			PeriodDataAccumulatorTimeSeriesModel<Currency> moneySupplyM1Model) {

		for (Currency currency : Currency.values())
			this.nationalAccountsTableModels.put(currency,
					new NationalAccountsTableModel(currency));

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

			//
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
			balanceSheetsForAgentTypes.put(Household.class, new BalanceSheet(
					currency));
			balanceSheetsForAgentTypes.put(CreditBank.class, new BalanceSheet(
					currency));
			balanceSheetsForAgentTypes.put(CentralBank.class, new BalanceSheet(
					currency));
			balanceSheetsForAgentTypes.put(State.class, new BalanceSheet(
					currency));
			balanceSheetsForAgentTypes.put(Factory.class, new BalanceSheet(
					currency));

			this.nationalAccountsBalanceSheets.put(currency,
					balanceSheetsForAgentTypes);
		}
	}

	public void nextPeriod() {
		for (Entry<Currency, Map<Class<? extends Agent>, BalanceSheet>> balanceSheetsForAgentTypes : this.nationalAccountsBalanceSheets
				.entrySet()) {
			Currency currency = balanceSheetsForAgentTypes.getKey();
			for (Entry<Class<? extends Agent>, BalanceSheet> balanceSheetEntry : balanceSheetsForAgentTypes
					.getValue().entrySet()) {
				Class<? extends Agent> agentType = balanceSheetEntry.getKey();
				BalanceSheet balanceSheet = balanceSheetEntry.getValue();

				NationalAccountsTableModel nationalAccountsTableModel = this.nationalAccountsTableModels
						.get(currency);

				int agentTypeNr = -1;
				if (agentType.equals(Household.class))
					agentTypeNr = NationalAccountsTableModel.AGENTTYPE_HOUSEHOLD;
				else if (agentType.equals(CreditBank.class))
					agentTypeNr = NationalAccountsTableModel.AGENTTYPE_CREDITBANK;
				else if (agentType.equals(CentralBank.class))
					agentTypeNr = NationalAccountsTableModel.AGENTTYPE_CENTRALBANK;
				else if (agentType.equals(State.class))
					agentTypeNr = NationalAccountsTableModel.AGENTTYPE_STATE;
				else if (agentType.equals(Factory.class))
					agentTypeNr = NationalAccountsTableModel.AGENTTYPE_FACTORY;

				// active
				nationalAccountsTableModel.setValue(
						NationalAccountsTableModel.SIDE_ACTIVE,
						NationalAccountsTableModel.POSITION_HARD_CASH,
						agentTypeNr, currency,
						Currency.round(balanceSheet.hardCash));
				nationalAccountsTableModel.setValue(
						NationalAccountsTableModel.SIDE_ACTIVE,
						NationalAccountsTableModel.POSITION_CASH, agentTypeNr,
						currency, Currency.round(balanceSheet.cash));
				nationalAccountsTableModel.setValue(
						NationalAccountsTableModel.SIDE_ACTIVE,
						NationalAccountsTableModel.POSITION_BONDS, agentTypeNr,
						currency, Currency.round(balanceSheet.bonds));
				nationalAccountsTableModel.setValue(
						NationalAccountsTableModel.SIDE_ACTIVE,
						NationalAccountsTableModel.POSITION_BANK_LOANS,
						agentTypeNr, currency,
						Currency.round(balanceSheet.bankLoans));
				if (balanceSheet.inventory.containsKey(GoodType.LABOURHOUR))
					nationalAccountsTableModel
							.setValue(
									NationalAccountsTableModel.SIDE_ACTIVE,
									NationalAccountsTableModel.POSITION_GOODTYPE_LABOURHOUR,
									agentTypeNr, currency,
									balanceSheet.inventory
											.get(GoodType.LABOURHOUR));
				if (balanceSheet.inventory.containsKey(GoodType.MEGACALORIE))
					nationalAccountsTableModel
							.setValue(
									NationalAccountsTableModel.SIDE_ACTIVE,
									NationalAccountsTableModel.POSITION_GOODTYPE_MEGACALORIE,
									agentTypeNr, currency,
									balanceSheet.inventory
											.get(GoodType.MEGACALORIE));
				if (balanceSheet.inventory.containsKey(GoodType.KILOWATT))
					nationalAccountsTableModel
							.setValue(
									NationalAccountsTableModel.SIDE_ACTIVE,
									NationalAccountsTableModel.POSITION_GOODTYPE_KILOWATT,
									agentTypeNr, currency,
									balanceSheet.inventory
											.get(GoodType.KILOWATT));

				// passive
				nationalAccountsTableModel.setValue(
						NationalAccountsTableModel.SIDE_PASSIVE,
						NationalAccountsTableModel.POSITION_LOANS, agentTypeNr,
						currency, Currency.round(balanceSheet.loans));
				nationalAccountsTableModel.setValue(
						NationalAccountsTableModel.SIDE_PASSIVE,
						NationalAccountsTableModel.POSITION_FIN_LIABLITIES,
						agentTypeNr, currency,
						Currency.round(balanceSheet.financialLiabilities));
				nationalAccountsTableModel.setValue(
						NationalAccountsTableModel.SIDE_PASSIVE,
						NationalAccountsTableModel.POSITION_BANK_BORROWINGS,
						agentTypeNr, currency,
						Currency.round(balanceSheet.bankBorrowings));
			}
		}

		this.resetNationalAccountsBalanceSheets();
	}

	public Map<Currency, NationalAccountsTableModel> getNationalAccountsTableModels() {
		return this.nationalAccountsTableModels;
	}
}
