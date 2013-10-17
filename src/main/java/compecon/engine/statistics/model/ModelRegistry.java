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

import compecon.economy.sectors.financial.Currency;
import compecon.engine.statistics.model.ModelRegistry.NationalEconomyModel.HouseholdsModel.UtilityModel;
import compecon.engine.statistics.model.ModelRegistry.NationalEconomyModel.IndustryModel;
import compecon.engine.statistics.model.timeseries.PeriodDataAccumulatorTimeSeriesModel;
import compecon.engine.statistics.model.timeseries.PeriodDataPercentageTimeSeriesModel;
import compecon.engine.statistics.model.timeseries.PeriodDataQuotientTimeSeriesModel;
import compecon.materia.GoodType;
import compecon.materia.InputOutputModel;
import compecon.math.ConvexFunction.ConvexFunctionTerminationCause;
import compecon.math.production.ConvexProductionFunction.ConvexProductionFunctionTerminationCause;
import compecon.math.production.IProductionFunction;

public class ModelRegistry {

	public enum IncomeSource {
		WAGE, DIVIDEND
	}

	public class NationalEconomyModel {

		/**
		 * model for collecting statistics about households
		 */
		public class HouseholdsModel {

			/**
			 * model for collecting statistics about utiltiy of households
			 */
			public class UtilityModel {

				public final Currency referenceCurrency;

				public final PeriodDataAccumulatorTimeSeriesModel utilityOutputModel;

				/**
				 * total utility sum of all periods; measurement starts after
				 * initialization phase; FIXME double overflow possible, group
				 * statistical models that are filled after initialization phase
				 */
				public final PeriodDataAccumulatorTimeSeriesModel totalUtilityOutputModel;

				public final Map<GoodType, PeriodDataAccumulatorTimeSeriesModel> utilityInputModels = new HashMap<GoodType, PeriodDataAccumulatorTimeSeriesModel>();

				public UtilityModel(Currency referenceCurrency) {
					this.referenceCurrency = referenceCurrency;
					this.utilityOutputModel = new PeriodDataAccumulatorTimeSeriesModel(
							referenceCurrency.getIso4217Code() + " utility");
					this.totalUtilityOutputModel = new PeriodDataAccumulatorTimeSeriesModel(
							referenceCurrency.getIso4217Code()
									+ " total utility");
					for (GoodType goodType : InputOutputModel
							.getUtilityFunctionForHousehold()
							.getInputGoodTypes()) {
						this.utilityInputModels.put(goodType,
								new PeriodDataAccumulatorTimeSeriesModel(
										referenceCurrency.getIso4217Code()
												+ " " + goodType + " input"));
					}
				}

				public void nextPeriod() {
					this.utilityOutputModel.nextPeriod();
					for (PeriodDataAccumulatorTimeSeriesModel periodDataAccumulatorTimeSeriesModel : this.utilityInputModels
							.values()) {
						periodDataAccumulatorTimeSeriesModel.nextPeriod();
					}
				}
			}

			public final Currency referenceCurrency;

			public final PeriodDataAccumulatorTimeSeriesModel budgetModel = new PeriodDataAccumulatorTimeSeriesModel(
					"Budget");
			/**
			 * Models metering causes of terminations in convex production
			 * functions, e. g. marginal costs exceeding marginal revenue.
			 */
			public final Map<ConvexFunctionTerminationCause, PeriodDataAccumulatorTimeSeriesModel> convexFunctionTerminationCauseModels = new HashMap<ConvexFunctionTerminationCause, PeriodDataAccumulatorTimeSeriesModel>();
			public final PeriodDataAccumulatorTimeSeriesModel consumptionModel;
			public final PeriodDataQuotientTimeSeriesModel consumptionRateModel;
			public final PeriodDataQuotientTimeSeriesModel consumptionIncomeRatioModel;
			public final PeriodDataAccumulatorTimeSeriesModel dividendModel;
			public final PeriodDataAccumulatorTimeSeriesModel incomeModel;
			public final PeriodDataDistributionModel incomeDistributionModel;
			public final PeriodDataPercentageTimeSeriesModel<IncomeSource> incomeSourceModel;
			public final PeriodDataAccumulatorTimeSeriesModel labourHourCapacityModel;
			public final PeriodDataAccumulatorTimeSeriesModel savingModel;
			public final PeriodDataQuotientTimeSeriesModel savingRateModel;
			public final PeriodDataAccumulatorTimeSeriesModel wageModel;
			public final UtilityModel utilityModel;

			public HouseholdsModel(Currency referenceCurrency) {
				this.referenceCurrency = referenceCurrency;

				for (ConvexFunctionTerminationCause cause : ConvexFunctionTerminationCause
						.values()) {
					convexFunctionTerminationCauseModels.put(
							cause,
							new PeriodDataAccumulatorTimeSeriesModel(cause
									.toString()));
				}

				this.consumptionModel = new PeriodDataAccumulatorTimeSeriesModel(
						referenceCurrency.getIso4217Code() + " consumption");
				this.consumptionRateModel = new PeriodDataQuotientTimeSeriesModel(
						referenceCurrency.getIso4217Code()
								+ " consumption rate");
				this.consumptionIncomeRatioModel = new PeriodDataQuotientTimeSeriesModel(
						"");
				this.dividendModel = new PeriodDataAccumulatorTimeSeriesModel(
						referenceCurrency.getIso4217Code() + " dividend");
				this.incomeModel = new PeriodDataAccumulatorTimeSeriesModel(
						referenceCurrency.getIso4217Code() + " income");
				this.incomeDistributionModel = new PeriodDataDistributionModel(
						this.referenceCurrency);
				this.incomeSourceModel = new PeriodDataPercentageTimeSeriesModel<IncomeSource>(
						IncomeSource.values(),
						referenceCurrency.getIso4217Code() + " income source");
				this.labourHourCapacityModel = new PeriodDataAccumulatorTimeSeriesModel(
						referenceCurrency.getIso4217Code() + " "
								+ GoodType.LABOURHOUR + " cap.");

				this.savingModel = new PeriodDataAccumulatorTimeSeriesModel(
						referenceCurrency.getIso4217Code() + " saving");
				this.savingRateModel = new PeriodDataQuotientTimeSeriesModel(
						referenceCurrency.getIso4217Code() + " saving rate");
				this.wageModel = new PeriodDataAccumulatorTimeSeriesModel(
						referenceCurrency.getIso4217Code() + " wage");
				this.utilityModel = new UtilityModel(this.referenceCurrency);
			}

			public void nextPeriod() {
				for (PeriodDataAccumulatorTimeSeriesModel periodDataAccumulatorTimeSeriesModel : this.convexFunctionTerminationCauseModels
						.values()) {
					periodDataAccumulatorTimeSeriesModel.nextPeriod();
				}
				this.budgetModel.nextPeriod();
				this.consumptionModel.nextPeriod();
				this.consumptionRateModel.nextPeriod();
				this.dividendModel.nextPeriod();
				this.incomeModel.nextPeriod();
				this.incomeSourceModel.nextPeriod();
				this.incomeDistributionModel.nextPeriod();
				this.labourHourCapacityModel.nextPeriod();
				this.savingModel.nextPeriod();
				this.savingRateModel.nextPeriod();
				this.utilityModel.nextPeriod();
				this.wageModel.nextPeriod();
			}
		}

		/**
		 * model for collecting statistics about production input and output of
		 * factories
		 */
		public class IndustryModel {

			public final Currency referenceCurrency;

			public final GoodType outputGoodType;

			public final PeriodDataAccumulatorTimeSeriesModel outputModel;

			public final PeriodDataAccumulatorTimeSeriesModel offerModel;

			public final Map<GoodType, PeriodDataAccumulatorTimeSeriesModel> inputModels = new HashMap<GoodType, PeriodDataAccumulatorTimeSeriesModel>();

			public final PeriodDataAccumulatorTimeSeriesModel budgetModel = new PeriodDataAccumulatorTimeSeriesModel(
					"Budget");

			/**
			 * Models metering causes of terminations in convex production
			 * functions, e. g. marginal costs exceeding marginal revenue.
			 */
			public final Map<ConvexProductionFunctionTerminationCause, PeriodDataAccumulatorTimeSeriesModel> convexProductionFunctionTerminationCauseModels = new HashMap<ConvexProductionFunctionTerminationCause, PeriodDataAccumulatorTimeSeriesModel>();

			public IndustryModel(Currency referenceCurrency,
					GoodType outputGoodType) {
				this.referenceCurrency = referenceCurrency;
				this.outputGoodType = outputGoodType;

				this.offerModel = new PeriodDataAccumulatorTimeSeriesModel(
						referenceCurrency.getIso4217Code() + " "
								+ outputGoodType + " offered");
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

				for (ConvexProductionFunctionTerminationCause cause : ConvexProductionFunctionTerminationCause
						.values()) {
					convexProductionFunctionTerminationCauseModels.put(
							cause,
							new PeriodDataAccumulatorTimeSeriesModel(cause
									.toString()));
				}
			}

			public void nextPeriod() {
				this.outputModel.nextPeriod();
				this.budgetModel.nextPeriod();
				this.offerModel.nextPeriod();
				for (PeriodDataAccumulatorTimeSeriesModel periodDataAccumulatorTimeSeriesModel : this.inputModels
						.values()) {
					periodDataAccumulatorTimeSeriesModel.nextPeriod();
				}
				for (PeriodDataAccumulatorTimeSeriesModel periodDataAccumulatorTimeSeriesModel : this.convexProductionFunctionTerminationCauseModels
						.values()) {
					periodDataAccumulatorTimeSeriesModel.nextPeriod();
				}
			}
		}

		public final Currency referenceCurrency;

		/*
		 * agents
		 */

		public final NumberOfAgentsModel numberOfAgentsModel;

		/*
		 * households: consumption and saving
		 */

		public final HouseholdsModel householdsModel;

		/*
		 * industries
		 */

		public final Map<GoodType, IndustryModel> industryModels = new HashMap<GoodType, IndustryModel>();

		/*
		 * prices
		 */

		public final PricesModel pricesModel;

		public final MarketDepthModel marketDepthModel;

		/*
		 * money
		 */

		public final PeriodDataAccumulatorTimeSeriesModel keyInterestRateModel;
		public final PeriodDataAccumulatorTimeSeriesModel moneySupplyM0Model;
		public final PeriodDataAccumulatorTimeSeriesModel moneySupplyM1Model;
		public final PeriodDataAccumulatorTimeSeriesModel moneySupplyM2Model;
		public final PeriodDataAccumulatorTimeSeriesModel moneyCirculationModel;
		public final PeriodDataQuotientTimeSeriesModel moneyVelocityModel;
		public final PeriodDataAccumulatorTimeSeriesModel priceIndexModel;

		// credit
		public final PeriodDataQuotientTimeSeriesModel creditUtilizationRateModel;

		/*
		 * national balances
		 */

		public final BalanceSheetsModel balanceSheetsModel;
		public final MonetaryTransactionsModel monetaryTransactionsModel;

		public NationalEconomyModel(Currency referenceCurrency) {
			this.referenceCurrency = referenceCurrency;

			for (GoodType goodType : GoodType.values()) {
				this.industryModels.put(goodType, new IndustryModel(
						referenceCurrency, goodType));
			}

			this.householdsModel = new HouseholdsModel(referenceCurrency);
			this.numberOfAgentsModel = new NumberOfAgentsModel(
					referenceCurrency);
			this.pricesModel = new PricesModel();
			this.marketDepthModel = new MarketDepthModel();
			this.keyInterestRateModel = new PeriodDataAccumulatorTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " key interest rate");
			this.moneySupplyM0Model = new PeriodDataAccumulatorTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " M0");
			this.moneySupplyM1Model = new PeriodDataAccumulatorTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " M1");
			this.moneySupplyM2Model = new PeriodDataAccumulatorTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " M2");
			this.moneyCirculationModel = new PeriodDataAccumulatorTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " money circulation");
			this.moneyVelocityModel = new PeriodDataQuotientTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " money velocity");
			this.priceIndexModel = new PeriodDataAccumulatorTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " price index");
			this.creditUtilizationRateModel = new PeriodDataQuotientTimeSeriesModel(
					referenceCurrency.getIso4217Code() + " credit util. rate");
			this.balanceSheetsModel = new BalanceSheetsModel(referenceCurrency);
			this.monetaryTransactionsModel = new MonetaryTransactionsModel();
		}

		public void nextPeriod() {
			moneyVelocityModel.add(moneyCirculationModel.getValue(),
					moneySupplyM1Model.getValue());

			for (IndustryModel goodTypeProductionModel : this.industryModels
					.values()) {
				goodTypeProductionModel.nextPeriod();
			}

			this.householdsModel.nextPeriod();
			this.balanceSheetsModel.nextPeriod();
			this.creditUtilizationRateModel.nextPeriod();
			this.keyInterestRateModel.nextPeriod();
			this.marketDepthModel.nextPeriod();
			this.monetaryTransactionsModel.nextPeriod();
			this.moneySupplyM0Model.nextPeriod();
			this.moneySupplyM1Model.nextPeriod();
			this.moneySupplyM2Model.nextPeriod();
			this.moneyCirculationModel.nextPeriod();
			this.moneyVelocityModel.nextPeriod();
			this.numberOfAgentsModel.nextPeriod();
			this.pricesModel.nextPeriod();
			this.priceIndexModel.nextPeriod();
		}
	}

	public final ControlModel controlModel = new ControlModel();

	public final AgentDetailModel agentDetailModel = new AgentDetailModel();

	protected final Map<Currency, NationalEconomyModel> nationalEconomyModels = new HashMap<Currency, NationalEconomyModel>();

	public ModelRegistry() {
		for (Currency currency : Currency.values())
			nationalEconomyModels.put(currency, new NationalEconomyModel(
					currency));
	}

	/*
	 * next period
	 */

	public void nextPeriod() {
		agentDetailModel.notifyListeners();

		for (NationalEconomyModel nationalEconomyModel : nationalEconomyModels
				.values()) {
			nationalEconomyModel.nextPeriod();
		}
	}

	/*
	 * accessors
	 */

	public NationalEconomyModel getNationalEconomyModel(Currency currency) {
		return this.nationalEconomyModels.get(currency);
	}

	public IndustryModel getIndustryModel(Currency currency,
			GoodType outputGoodType) {
		return nationalEconomyModels.get(currency).industryModels
				.get(outputGoodType);
	}

	public UtilityModel getUtilityModel(Currency currency) {
		return nationalEconomyModels.get(currency).householdsModel.utilityModel;
	}

	public PeriodDataAccumulatorTimeSeriesModel getWageModel(Currency currency) {
		return nationalEconomyModels.get(currency).householdsModel.wageModel;
	}

}
