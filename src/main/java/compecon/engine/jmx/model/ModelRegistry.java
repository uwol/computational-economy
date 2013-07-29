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

import compecon.culture.sectors.financial.Currency;
import compecon.engine.jmx.model.generic.PeriodDataAccumulatorTimeSeriesModel;
import compecon.engine.jmx.model.generic.PeriodDataQuotientTimeSeriesModel;
import compecon.engine.jmx.model.generic.TimeSeriesModel;
import compecon.nature.materia.GoodType;

public class ModelRegistry {

	protected final static ControlModel controlModel = new ControlModel();

	protected final static NumberOfAgentsModel numberOfAgentsModel = new NumberOfAgentsModel();

	protected final static PeriodDataAccumulatorTimeSeriesModel<Currency> utilityModel = new PeriodDataAccumulatorTimeSeriesModel<Currency>(
			Currency.values(), " utility");

	// aggregates

	protected final static PeriodDataAccumulatorTimeSeriesModel<GoodType> capacityModel = new PeriodDataAccumulatorTimeSeriesModel<GoodType>(
			GoodType.values(), " cap.");

	protected final static PeriodDataAccumulatorTimeSeriesModel<GoodType> effectiveProductionOutputModel = new PeriodDataAccumulatorTimeSeriesModel<GoodType>(
			GoodType.values());

	protected final static TimeSeriesModel<Currency> keyInterestRateModel = new TimeSeriesModel<Currency>(
			Currency.values());

	protected final static PeriodDataAccumulatorTimeSeriesModel<Currency> moneySupplyM0Model = new PeriodDataAccumulatorTimeSeriesModel<Currency>(
			Currency.values(), " M0");

	protected final static PeriodDataAccumulatorTimeSeriesModel<Currency> moneySupplyM1Model = new PeriodDataAccumulatorTimeSeriesModel<Currency>(
			Currency.values(), " M1");

	protected final static PeriodDataAccumulatorTimeSeriesModel<Currency> moneySupplyM2Model = new PeriodDataAccumulatorTimeSeriesModel<Currency>(
			Currency.values(), " M2");

	protected final static TimeSeriesModel<Currency> priceIndexModel = new TimeSeriesModel<Currency>(
			Currency.values());

	// prices

	protected final static PricesModel pricesModel = new PricesModel();

	// consumption and saving

	protected final static PeriodDataAccumulatorTimeSeriesModel<Currency> consumptionModel = new PeriodDataAccumulatorTimeSeriesModel<Currency>(
			Currency.values(), " con.");

	protected final static PeriodDataAccumulatorTimeSeriesModel<Currency> incomeModel = new PeriodDataAccumulatorTimeSeriesModel<Currency>(
			Currency.values(), " inc.");

	protected final static PeriodDataQuotientTimeSeriesModel<Currency> consumptionRateModel = new PeriodDataQuotientTimeSeriesModel<Currency>(
			Currency.values());

	protected final static PeriodDataQuotientTimeSeriesModel<Currency> consumptionIncomeRatioModel = new PeriodDataQuotientTimeSeriesModel<Currency>(
			Currency.values());

	protected final static PeriodDataAccumulatorTimeSeriesModel<Currency> savingModel = new PeriodDataAccumulatorTimeSeriesModel<Currency>(
			Currency.values());

	protected final static PeriodDataAccumulatorTimeSeriesModel<Currency> wageModel = new PeriodDataAccumulatorTimeSeriesModel<Currency>(
			Currency.values(), " wage");

	protected final static PeriodDataAccumulatorTimeSeriesModel<Currency> dividendModel = new PeriodDataAccumulatorTimeSeriesModel<Currency>(
			Currency.values(), " div.");

	// agent details

	protected final static AgentDetailModel agentDetailModel = new AgentDetailModel();

	// national balances

	protected final static BalanceSheetsModel balanceSheetsModel;

	protected final static MonetaryTransactionsModel monetaryTransactionsModel = new MonetaryTransactionsModel();

	static {
		balanceSheetsModel = new BalanceSheetsModel(moneySupplyM0Model,
				moneySupplyM1Model, moneySupplyM2Model);
	}

	/*
	 * next period
	 */

	public static void nextPeriod() {
		agentDetailModel.notifyListeners();
		balanceSheetsModel.nextPeriod();
		capacityModel.nextPeriod();
		consumptionModel.nextPeriod();
		consumptionRateModel.nextPeriod();
		dividendModel.nextPeriod();
		effectiveProductionOutputModel.nextPeriod();
		incomeModel.nextPeriod();
		monetaryTransactionsModel.nextPeriod();
		moneySupplyM0Model.nextPeriod();
		moneySupplyM1Model.nextPeriod();
		moneySupplyM2Model.nextPeriod();
		pricesModel.nextPeriod();
		savingModel.nextPeriod();
		utilityModel.nextPeriod();
		wageModel.nextPeriod();
	}

	/*
	 * accessors
	 */

	public static AgentDetailModel getAgentDetailModel() {
		return agentDetailModel;
	}

	public static BalanceSheetsModel getBalanceSheetsModel() {
		return balanceSheetsModel;
	}

	public static PeriodDataAccumulatorTimeSeriesModel<GoodType> getCapacityModel() {
		return capacityModel;
	}

	public static PeriodDataAccumulatorTimeSeriesModel<Currency> getConsumptionModel() {
		return consumptionModel;
	}

	public static PeriodDataQuotientTimeSeriesModel<Currency> getConsumptionRateModel() {
		return consumptionRateModel;
	}

	public static PeriodDataQuotientTimeSeriesModel<Currency> getConsumptionIncomeRatioModel() {
		return consumptionIncomeRatioModel;
	}

	public static ControlModel getControlModel() {
		return controlModel;
	}

	public static PeriodDataAccumulatorTimeSeriesModel<Currency> getDividendModel() {
		return dividendModel;
	}

	public static PeriodDataAccumulatorTimeSeriesModel<GoodType> getEffectiveProductionOutputModel() {
		return effectiveProductionOutputModel;
	}

	public static PeriodDataAccumulatorTimeSeriesModel<Currency> getIncomeModel() {
		return incomeModel;
	}

	public static TimeSeriesModel<Currency> getKeyInterestRateModel() {
		return keyInterestRateModel;
	}

	public static MonetaryTransactionsModel getMonetaryTransactionsModel() {
		return monetaryTransactionsModel;
	}

	public static PeriodDataAccumulatorTimeSeriesModel<Currency> getMoneySupplyM0Model() {
		return moneySupplyM0Model;
	}

	public static PeriodDataAccumulatorTimeSeriesModel<Currency> getMoneySupplyM1Model() {
		return moneySupplyM1Model;
	}

	public static PeriodDataAccumulatorTimeSeriesModel<Currency> getMoneySupplyM2Model() {
		return moneySupplyM2Model;
	}

	public static PeriodDataAccumulatorTimeSeriesModel<Currency> getUtilityModel() {
		return utilityModel;
	}

	public static NumberOfAgentsModel getNumberOfAgentsModel() {
		return numberOfAgentsModel;
	}

	public static PricesModel getPricesModel() {
		return pricesModel;
	}

	public static TimeSeriesModel<Currency> getPriceIndexModel() {
		return priceIndexModel;
	}

	public static PeriodDataAccumulatorTimeSeriesModel<Currency> getSavingModel() {
		return savingModel;
	}

	public static PeriodDataAccumulatorTimeSeriesModel<Currency> getWageModel() {
		return wageModel;
	}

}
