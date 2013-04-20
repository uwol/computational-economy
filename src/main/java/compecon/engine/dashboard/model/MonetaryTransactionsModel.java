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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import compecon.culture.sectors.agriculture.Farm;
import compecon.culture.sectors.financial.CentralBank;
import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.household.Household;
import compecon.culture.sectors.industry.Factory;
import compecon.culture.sectors.state.State;
import compecon.engine.Agent;

public class MonetaryTransactionsModel {

	public class MonetaryTransactionsTableModel extends AbstractTableModel {

		public final Currency referenceCurrency;

		protected Object[][] transientTableData = new Object[6][7];

		public MonetaryTransactionsTableModel(Currency referenceCurrency) {
			this.referenceCurrency = referenceCurrency;
		}

		@Override
		public int getColumnCount() {
			return transientTableData[0].length;
		}

		@Override
		public int getRowCount() {
			return transientTableData.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int colIndex) {
			return this.transientTableData[rowIndex][colIndex];
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return "from / to";
			return MonetaryTransactionsModel.this.agentTypes.get(
					columnIndex - 1).getSimpleName();
		}
	}

	// stores transaction values in a type-safe way
	protected Map<Currency, Map<Class<? extends Agent>, PeriodDataAccumulatorSet<Class<? extends Agent>>>> adjacencyMatrix = new HashMap<Currency, Map<Class<? extends Agent>, PeriodDataAccumulatorSet<Class<? extends Agent>>>>();

	// stores transaction values compatible to JTables, is filled each period by
	// nextPeriod from adjacencyMatrix
	protected Map<Currency, MonetaryTransactionsTableModel> monetaryTransactionsTableModels = new HashMap<Currency, MonetaryTransactionsTableModel>();

	protected List<Class<? extends Agent>> agentTypes = new ArrayList<Class<? extends Agent>>();

	public MonetaryTransactionsModel() {

		this.agentTypes.add(Household.class);
		this.agentTypes.add(Farm.class);
		this.agentTypes.add(Factory.class);
		this.agentTypes.add(CreditBank.class);
		this.agentTypes.add(CentralBank.class);
		this.agentTypes.add(State.class);

		for (Currency currency : Currency.values()) {
			// initialize data structure for currency
			Map<Class<? extends Agent>, PeriodDataAccumulatorSet<Class<? extends Agent>>> adjacencyMatrixForCurrency = new HashMap<Class<? extends Agent>, PeriodDataAccumulatorSet<Class<? extends Agent>>>();
			// from
			for (Class<? extends Agent> agentTypeFrom : this.agentTypes) {
				// to
				PeriodDataAccumulatorSet<Class<? extends Agent>> periodDataAccumulatorSet = new PeriodDataAccumulatorSet<Class<? extends Agent>>();
				for (Class<? extends Agent> agentTypeTo : this.agentTypes) {
					periodDataAccumulatorSet.add(agentTypeTo, 0);
				}

				adjacencyMatrixForCurrency.put(agentTypeFrom,
						periodDataAccumulatorSet);
			}
			this.adjacencyMatrix.put(currency, adjacencyMatrixForCurrency);

			this.monetaryTransactionsTableModels.put(currency,
					new MonetaryTransactionsTableModel(currency));
		}
	}

	public void nextPeriod() {
		for (Currency currency : Currency.values()) {
			if (this.adjacencyMatrix.containsKey(currency)) {
				// source data model
				Map<Class<? extends Agent>, PeriodDataAccumulatorSet<Class<? extends Agent>>> adjacencyMatrixForCurrency = this.adjacencyMatrix
						.get(currency);

				// target data model
				MonetaryTransactionsTableModel monetaryTransactionsTableModel = this.monetaryTransactionsTableModels
						.get(currency);

				// for all agent types as sources of monetary transactions ->
				// rows
				for (int i = 0; i < this.agentTypes.size(); i++) {
					Class<? extends Agent> agentTypeFrom = this.agentTypes
							.get(i);
					PeriodDataAccumulatorSet<Class<? extends Agent>> adjacencyMatrixForCurrencyAndFromAgentType = adjacencyMatrixForCurrency
							.get(agentTypeFrom);

					// row name
					monetaryTransactionsTableModel.transientTableData[i][0] = agentTypeFrom
							.getSimpleName();

					// for all agent types as destinations of monetary
					// transactions
					// ->
					// columns
					for (int j = 0; j < this.agentTypes.size(); j++) {
						Class<? extends Agent> agentTypeTo = this.agentTypes
								.get(j);
						PeriodDataAccumulator periodTransactionVolum = adjacencyMatrixForCurrencyAndFromAgentType
								.getPeriodDataAccumulators().get(agentTypeTo);
						monetaryTransactionsTableModel.transientTableData[i][j + 1] = Currency
								.round(periodTransactionVolum.getAmount());
						periodTransactionVolum.reset();
					}
				}
				monetaryTransactionsTableModel.fireTableDataChanged();
			}
		}
	}

	public void bank_onTransfer(Class<? extends Agent> from,
			Class<? extends Agent> to, Currency currency, double value) {
		Map<Class<? extends Agent>, PeriodDataAccumulatorSet<Class<? extends Agent>>> adjacencyMatrixForCurrency = this.adjacencyMatrix
				.get(currency);
		adjacencyMatrixForCurrency.get(from).add(to, value);
	}

	public Map<Currency, MonetaryTransactionsTableModel> getMonetaryTransactionsTableModels() {
		return this.monetaryTransactionsTableModels;
	}
}
