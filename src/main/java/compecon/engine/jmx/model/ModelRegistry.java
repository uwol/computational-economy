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
import java.util.Set;

import compecon.economy.sectors.financial.Currency;
import compecon.engine.jmx.model.ModelRegistry.NationalEconomyModel.GoodTypeProductionModel;
import compecon.engine.jmx.model.ModelRegistry.NationalEconomyModel.UtilityModel;
import compecon.engine.jmx.model.timeseries.PeriodDataAccumulatorTimeSeriesModel;
import compecon.engine.jmx.model.timeseries.PeriodDataPercentageTimeSeriesModel;
import compecon.engine.jmx.model.timeseries.PeriodDataQuotientTimeSeriesModel;
import compecon.materia.GoodType;
import compecon.materia.InputOutputModel;
import compecon.math.production.IProductionFunction;

public class ModelRegistry {

	public enum IncomeSource {
		WAGE, DIVIDEND
	}

	public static class NationalEconomyModel {

		/**
		 * model for collecting statistics about production input and output of
		 * factories
		 */
		public static class GoodTypeProductionModel {
			protected final Currency referenceCurrency;

			protected final GoodType outputGoodType;

			protected final PeriodDataAccumulatorTimeSeriesModel outputModel;

			protected final Map<GoodType, PeriodDataAccumulatorTimeSeriesModel> inputModels = new HashMap<GoodType, PeriodDataAccumulatorTimeSeriesModel>();

			public GoodTypeProductionModel(Currency referenceCurrency,
					GoodType outputGoodType) {
				this.referenceCurrency = referenceCurrency;
				this.outputGoodType = outputGoodType;

				this.outputModel = new PeriodDataAccumulatorTimeSeriesModel(
						referenceCurrency.getIso4217Code() + " "
								+ outputGoodType + " output");

				if (!GoodType.LABOURHOUR.equals(outputGoodType)) {
					IProductionFunction productionFunction = InputOutputModel
							.getProductionFunction(outputGoodType);
					for (GoodType inputGoodType : productionFunction
							.getInputGoodTypes()) {
						inputModels.put(inputGoodType,
								new PeriodDataAccumulatorTimeSeriesModel(
										referenceCurrency.getIso4217Code()
												+ " " + inputGoodType
												+ " input"));
					}
				}
			}

			public PeriodDataAccumulatorTimeSeriesModel getOutputModel() {
				return this.outputModel;
			}

			public Set<GoodType> getInputGoodTypes() {
				return this.inputModels.keySet();
			}

			public PeriodDataAccumulatorTimeSeriesModel getInputModel(
					GoodType inputGoodType) {
				return this.inputModels.get(inputGoodType);
			}

			public void nextPeriod() {
				this.outputModel.nextPeriod();
				for (PeriodDataAccumulatorTimeSeriesModel periodDataAccumulatorTimeSeriesModel : this.inputModels
						.values()) {
					periodDataAccumulatorTimeSeriesModel.nextPeriod();
				}
			}
		}

		/**
		 * model for collecting statistics about utiltiy of households
		 */
		public static class UtilityModel {

			protected final Currency referenceCurrency;

			protected final PeriodDataAccumulatorTimeSeriesModel utilityOutputModel;

			protected final Map<GoodType, PeriodDataAccumulatorTimeSeriesModel> utilityInputModels = new HashMap<GoodType, PeriodDataAccumulatorTimeSeriesModel>();

			public UtilityModel(Currency referenceCurrency) {
				this.referenceCurrency = referenceCurrency;
				this.utilityOutputModel = new PeriodDataAccumulatorTimeSeriesModel(
						referenceCurrency.getIso4217Code() + " utility");
				for (GoodType goodType : InputOutputModel
						.getUtilityFunctionForHousehold().getInputGoodTypes()) {
					this.utilityInputModels.put(goodType,
							new PeriodDataAccumulatorTimeSeriesModel(
									referenceCurrency.getIso4217Code() + " "
											+ goodType + " input"));
				}
			}

			public PeriodDataAccumulatorTimeSeriesModel getOutputModel() {
				return this.utilityOutputModel;
			}

			public Set<GoodType> getInputGoodTypes() {
				return this.utilityInputModels.keySet();
			}

			public PeriodDataAccumulatorTimeSeriesModel getInputModel(
					GoodType inputGoodType) {
				return this.utilityInputModels.get(inputGoodType);
			}

			public void nextPeriod() {
				this.utilityOutputModel.nextPeriod();
				for (PeriodDataAccumulatorTimeSeriesModel periodDataAccumulatorTimeSeriesModel : this.utilityInputModels
						.values()) {
					periodDataAccumulatorTimeSeriesModel.nextPeriod();
				}
			}
		}

		public NationalEconomyModel(Currency referenceCurrency) {
			this.referenceCurrency = referenceCurrency;

			for (GoodType goodType : GoodType.values()) {
				this.goodTypeProductionModels
						.put(goodType, new GoodTypeProductionModel(
								referenceCurrency, goodType));
			}

			this.numberOfAgentsModel = new NumberOfAgentsModel(
					referenceCurrency);
			this.consumptionModel = new PeriodDataAccumulatorTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " consumption");
			this.consumptionRateModel = new PeriodDataQuotientTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " consumption rate");
			this.consumptionIncomeRatioModel = new PeriodDataQuotientTimeSeriesModel(
					"");
			this.dividendModel = new PeriodDataAccumulatorTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " dividend");
			this.incomeModel = new PeriodDataAccumulatorTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " income");
			this.incomeDistributionModel = new PeriodDataDistributionModel(
					this.referenceCurrency);
			this.incomeSourceModel = new PeriodDataPercentageTimeSeriesModel<IncomeSource>(
					IncomeSource.values(), referenceCurrency.getIso4217Code()
							+ " income source");
			this.savingModel = new PeriodDataAccumulatorTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " saving");
			this.savingRateModel = new PeriodDataQuotientTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " saving rate");
			this.wageModel = new PeriodDataAccumulatorTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " wage");
			this.labourHourCapacityModel = new PeriodDataAccumulatorTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " "
							+ GoodType.LABOURHOUR + " cap.");
			this.pricesModel = new PricesModel();
			this.utilityModel = new UtilityModel(this.referenceCurrency);
			this.keyInterestRateModel = new PeriodDataAccumulatorTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " key interest rate");
			this.moneySupplyM0Model = new PeriodDataAccumulatorTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " M0");
			this.moneySupplyM1Model = new PeriodDataAccumulatorTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " M1");
			this.moneySupplyM2Model = new PeriodDataAccumulatorTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " M2");
			this.priceIndexModel = new PeriodDataAccumulatorTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " price index");
			this.balanceSheetsModel = new BalanceSheetsModel(referenceCurrency);
			this.monetaryTransactionsModel = new MonetaryTransactionsModel();
		}

		public final Currency referenceCurrency;

		// agents

		public final NumberOfAgentsModel numberOfAgentsModel;

		// households: consumption and saving

		public final PeriodDataAccumulatorTimeSeriesModel consumptionModel;
		public final PeriodDataQuotientTimeSeriesModel consumptionRateModel;
		public final PeriodDataQuotientTimeSeriesModel consumptionIncomeRatioModel;
		public final PeriodDataAccumulatorTimeSeriesModel dividendModel;
		public final PeriodDataAccumulatorTimeSeriesModel incomeModel;
		public final PeriodDataDistributionModel incomeDistributionModel;
		public final PeriodDataPercentageTimeSeriesModel<IncomeSource> incomeSourceModel;
		public final PeriodDataAccumulatorTimeSeriesModel savingModel;
		public final PeriodDataQuotientTimeSeriesModel savingRateModel;
		public final PeriodDataAccumulatorTimeSeriesModel wageModel;
		public final UtilityModel utilityModel;

		// industries

		public final PeriodDataAccumulatorTimeSeriesModel labourHourCapacityModel;
		public final Map<GoodType, GoodTypeProductionModel> goodTypeProductionModels = new HashMap<GoodType, GoodTypeProductionModel>();

		// prices

		public final PricesModel pricesModel;

		// money

		public final PeriodDataAccumulatorTimeSeriesModel keyInterestRateModel;
		public final PeriodDataAccumulatorTimeSeriesModel moneySupplyM0Model;
		public final PeriodDataAccumulatorTimeSeriesModel moneySupplyM1Model;
		public final PeriodDataAccumulatorTimeSeriesModel moneySupplyM2Model;
		public final PeriodDataAccumulatorTimeSeriesModel priceIndexModel;

		// national balances

		public final BalanceSheetsModel balanceSheetsModel;
		public final MonetaryTransactionsModel monetaryTransactionsModel;

		public void nextPeriod() {
			for (GoodTypeProductionModel goodTypeProductionModel : this.goodTypeProductionModels
					.values()) {
				goodTypeProductionModel.nextPeriod();
			}

			balanceSheetsModel.nextPeriod();
			labourHourCapacityModel.nextPeriod();
			consumptionModel.nextPeriod();
			consumptionRateModel.nextPeriod();
			dividendModel.nextPeriod();
			incomeModel.nextPeriod();
			incomeSourceModel.nextPeriod();
			incomeDistributionModel.nextPeriod();
			keyInterestRateModel.nextPeriod();
			monetaryTransactionsModel.nextPeriod();
			moneySupplyM0Model.nextPeriod();
			moneySupplyM1Model.nextPeriod();
			moneySupplyM2Model.nextPeriod();
			numberOfAgentsModel.nextPeriod();
			pricesModel.nextPeriod();
			priceIndexModel.nextPeriod();
			savingModel.nextPeriod();
			savingRateModel.nextPeriod();
			utilityModel.nextPeriod();
			wageModel.nextPeriod();
		}
	}

	protected final static ControlModel controlModel = new ControlModel();

	protected final static AgentDetailModel agentDetailModel = new AgentDetailModel();

	protected final static Map<Currency, NationalEconomyModel> nationalEconomyModels = new HashMap<Currency, NationalEconomyModel>();

	static {
		for (Currency currency : Currency.values())
			nationalEconomyModels.put(currency, new NationalEconomyModel(
					currency));
	}

	/*
	 * next period
	 */

	public static void nextPeriod() {
		agentDetailModel.notifyListeners();

		for (NationalEconomyModel nationalEconomyModel : nationalEconomyModels
				.values()) {
			nationalEconomyModel.nextPeriod();
		}
	}

	/*
	 * accessors
	 */

	public static AgentDetailModel getAgentDetailModel() {
		return agentDetailModel;
	}

	public static BalanceSheetsModel getBalanceSheetsModel(Currency currency) {
		return nationalEconomyModels.get(currency).balanceSheetsModel;
	}

	public static PeriodDataAccumulatorTimeSeriesModel getLabourHourCapacityModel(
			Currency currency) {
		return nationalEconomyModels.get(currency).labourHourCapacityModel;
	}

	public static PeriodDataAccumulatorTimeSeriesModel getConsumptionModel(
			Currency currency) {
		return nationalEconomyModels.get(currency).consumptionModel;
	}

	public static PeriodDataQuotientTimeSeriesModel getConsumptionRateModel(
			Currency currency) {
		return nationalEconomyModels.get(currency).consumptionRateModel;
	}

	public static PeriodDataQuotientTimeSeriesModel getConsumptionIncomeRatioModel(
			Currency currency) {
		return nationalEconomyModels.get(currency).consumptionIncomeRatioModel;
	}

	public static ControlModel getControlModel() {
		return controlModel;
	}

	public static PeriodDataAccumulatorTimeSeriesModel getDividendModel(
			Currency currency) {
		return nationalEconomyModels.get(currency).dividendModel;
	}

	public static GoodTypeProductionModel getGoodTypeProductionModel(
			Currency currency, GoodType outputGoodType) {
		return nationalEconomyModels.get(currency).goodTypeProductionModels
				.get(outputGoodType);
	}

	public static PeriodDataAccumulatorTimeSeriesModel getIncomeModel(
			Currency currency) {
		return nationalEconomyModels.get(currency).incomeModel;
	}

	public static PeriodDataDistributionModel getIncomeDistributionModel(
			Currency currency) {
		return nationalEconomyModels.get(currency).incomeDistributionModel;
	}

	public static PeriodDataPercentageTimeSeriesModel<IncomeSource> getIncomeSourceModel(
			Currency currency) {
		return nationalEconomyModels.get(currency).incomeSourceModel;
	}

	public static PeriodDataAccumulatorTimeSeriesModel getKeyInterestRateModel(
			Currency currency) {
		return nationalEconomyModels.get(currency).keyInterestRateModel;
	}

	public static MonetaryTransactionsModel getMonetaryTransactionsModel(
			Currency currency) {
		return nationalEconomyModels.get(currency).monetaryTransactionsModel;
	}

	public static PeriodDataAccumulatorTimeSeriesModel getMoneySupplyM0Model(
			Currency currency) {
		return nationalEconomyModels.get(currency).moneySupplyM0Model;
	}

	public static PeriodDataAccumulatorTimeSeriesModel getMoneySupplyM1Model(
			Currency currency) {
		return nationalEconomyModels.get(currency).moneySupplyM1Model;
	}

	public static PeriodDataAccumulatorTimeSeriesModel getMoneySupplyM2Model(
			Currency currency) {
		return nationalEconomyModels.get(currency).moneySupplyM2Model;
	}

	public static UtilityModel getUtilityModel(Currency currency) {
		return nationalEconomyModels.get(currency).utilityModel;
	}

	public static NumberOfAgentsModel getNumberOfAgentsModel(Currency currency) {
		return nationalEconomyModels.get(currency).numberOfAgentsModel;
	}

	public static PricesModel getPricesModel(Currency currency) {
		return nationalEconomyModels.get(currency).pricesModel;
	}

	public static PeriodDataAccumulatorTimeSeriesModel getPriceIndexModel(
			Currency currency) {
		return nationalEconomyModels.get(currency).priceIndexModel;
	}

	public static PeriodDataAccumulatorTimeSeriesModel getSavingModel(
			Currency currency) {
		return nationalEconomyModels.get(currency).savingModel;
	}

	public static PeriodDataQuotientTimeSeriesModel getSavingRateModel(
			Currency currency) {
		return nationalEconomyModels.get(currency).savingRateModel;
	}

	public static PeriodDataAccumulatorTimeSeriesModel getWageModel(
			Currency currency) {
		return nationalEconomyModels.get(currency).wageModel;
	}

}
