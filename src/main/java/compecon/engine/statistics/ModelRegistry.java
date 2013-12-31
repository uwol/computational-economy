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

package compecon.engine.statistics;

import java.util.HashMap;
import java.util.Map;

import compecon.economy.behaviour.PricingBehaviour.PricingBehaviourNewPriceDecisionCause;
import compecon.economy.materia.GoodType;
import compecon.economy.materia.InputOutputModel;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.statistics.timeseries.PeriodDataAccumulatorTimeSeriesModel;
import compecon.engine.statistics.timeseries.PeriodDataPercentageTimeSeriesModel;
import compecon.engine.statistics.timeseries.PeriodDataQuotientTimeSeriesModel;
import compecon.math.ConvexFunction.ConvexFunctionTerminationCause;
import compecon.math.production.ConvexProductionFunction.ConvexProductionFunctionTerminationCause;
import compecon.math.production.ProductionFunction;

public class ModelRegistry {

	public enum IncomeSource {
		WAGE, DIVIDEND
	}

	public class NationalEconomyModel {

		/**
		 * model for collecting statistics about households
		 */
		public class HouseholdsModel {

			public final Currency currency;

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

			public HouseholdsModel(final Currency currency,
					final InputOutputModel inputOutputModel) {
				this.currency = currency;

				for (ConvexFunctionTerminationCause cause : ConvexFunctionTerminationCause
						.values()) {
					convexFunctionTerminationCauseModels.put(
							cause,
							new PeriodDataAccumulatorTimeSeriesModel(cause
									.toString()));
				}

				this.consumptionModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " consumption");
				this.consumptionRateModel = new PeriodDataQuotientTimeSeriesModel(
						currency.getIso4217Code() + " consumption rate");
				this.consumptionIncomeRatioModel = new PeriodDataQuotientTimeSeriesModel(
						"");
				this.dividendModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " dividend");
				this.incomeModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " income");
				this.incomeDistributionModel = new PeriodDataDistributionModel(
						this.currency);
				this.incomeSourceModel = new PeriodDataPercentageTimeSeriesModel<IncomeSource>(
						IncomeSource.values(), currency.getIso4217Code()
								+ " income source");
				this.labourHourCapacityModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " " + GoodType.LABOURHOUR
								+ " cap.");

				this.savingModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " saving");
				this.savingRateModel = new PeriodDataQuotientTimeSeriesModel(
						currency.getIso4217Code() + " saving rate");
				this.wageModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " wage");
				this.utilityModel = new UtilityModel(this.currency,
						inputOutputModel);
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

			public final Currency currency;

			public final GoodType goodType;

			public final PeriodDataAccumulatorTimeSeriesModel outputModel;

			public final Map<GoodType, PeriodDataAccumulatorTimeSeriesModel> inputModels = new HashMap<GoodType, PeriodDataAccumulatorTimeSeriesModel>();

			public final PeriodDataAccumulatorTimeSeriesModel budgetModel = new PeriodDataAccumulatorTimeSeriesModel(
					"Budget");

			public final PeriodDataAccumulatorTimeSeriesModel inventoryModel;

			/**
			 * Models metering causes of terminations in convex production
			 * functions, e. g. marginal costs exceeding marginal revenue.
			 */
			public final Map<ConvexProductionFunctionTerminationCause, PeriodDataAccumulatorTimeSeriesModel> convexProductionFunctionTerminationCauseModels = new HashMap<ConvexProductionFunctionTerminationCause, PeriodDataAccumulatorTimeSeriesModel>();

			public IndustryModel(final Currency currency,
					final GoodType goodType,
					final InputOutputModel inputOutputModel) {
				assert (!GoodType.LABOURHOUR.equals(goodType));

				this.currency = currency;
				this.goodType = goodType;

				this.outputModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " " + goodType + " output");
				this.inventoryModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " " + goodType
								+ " inventory");

				final ProductionFunction productionFunction = inputOutputModel
						.getProductionFunction(goodType);
				for (GoodType inputGoodType : productionFunction
						.getInputGoodTypes()) {
					inputModels.put(
							inputGoodType,
							new PeriodDataAccumulatorTimeSeriesModel(currency
									.getIso4217Code()
									+ " "
									+ inputGoodType
									+ " input"));
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
				this.inventoryModel.nextPeriod();

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

		/**
		 * model for collecting statistics about the state of this national
		 * economy
		 */
		public class StateModel {

			public final Currency currency;

			public final UtilityModel utilityModel;

			public StateModel(final Currency currency,
					final InputOutputModel inputOutputModel) {
				this.currency = currency;

				this.utilityModel = new UtilityModel(this.currency,
						inputOutputModel);
			}

			public void nextPeriod() {
				this.utilityModel.nextPeriod();
			}
		}

		/**
		 * model for collecting statistics about utility of households and
		 * states
		 */
		public class UtilityModel {

			public final Currency currency;

			public final PeriodDataAccumulatorTimeSeriesModel utilityOutputModel;

			public final Map<GoodType, PeriodDataAccumulatorTimeSeriesModel> utilityInputModels = new HashMap<GoodType, PeriodDataAccumulatorTimeSeriesModel>();

			public UtilityModel(final Currency currency,
					final InputOutputModel inputOutputModel) {
				this.currency = currency;
				this.utilityOutputModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " utility");
				for (GoodType goodType : inputOutputModel
						.getUtilityFunctionOfHousehold().getInputGoodTypes()) {
					this.utilityInputModels.put(
							goodType,
							new PeriodDataAccumulatorTimeSeriesModel(currency
									.getIso4217Code()
									+ " "
									+ goodType
									+ " input"));
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

		public class PricingBehaviourModel {

			public final Currency currency;

			public final GoodType goodType;

			public final PeriodDataAccumulatorTimeSeriesModel offerModel;

			public final PeriodDataAccumulatorTimeSeriesModel pricingBehaviourAveragePriceDecisionCauseModel;

			public final PeriodDataAccumulatorTimeSeriesModel soldModel;

			/**
			 * Models metering causes of terminations in convex production
			 * functions, e. g. marginal costs exceeding marginal revenue.
			 */
			public final Map<PricingBehaviourNewPriceDecisionCause, PeriodDataAccumulatorTimeSeriesModel> pricingBehaviourPriceDecisionCauseModels = new HashMap<PricingBehaviourNewPriceDecisionCause, PeriodDataAccumulatorTimeSeriesModel>();

			public PricingBehaviourModel(final Currency currency,
					final GoodType goodType) {
				this.currency = currency;
				this.goodType = goodType;

				this.offerModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " " + goodType + " offered");
				this.soldModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " " + goodType
								+ " sold (+1 period)");
				this.pricingBehaviourAveragePriceDecisionCauseModel = new PeriodDataAccumulatorTimeSeriesModel(
						"Average Decision");

				for (PricingBehaviourNewPriceDecisionCause cause : PricingBehaviourNewPriceDecisionCause
						.values()) {
					pricingBehaviourPriceDecisionCauseModels.put(
							cause,
							new PeriodDataAccumulatorTimeSeriesModel(cause
									.toString()));
				}

			}

			public void nextPeriod() {
				this.pricingBehaviourAveragePriceDecisionCauseModel
						.nextPeriod();
				this.offerModel.nextPeriod();
				this.soldModel.nextPeriod();

				for (PeriodDataAccumulatorTimeSeriesModel periodDataAccumulatorTimeSeriesModel : this.pricingBehaviourPriceDecisionCauseModels
						.values()) {
					periodDataAccumulatorTimeSeriesModel.nextPeriod();
				}
			}
		}

		public final Currency currency;

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
		 * state
		 */

		public final StateModel stateModel;

		/*
		 * prices
		 */

		public final PricesModel pricesModel;

		public final MarketDepthModel marketDepthModel;

		public final Map<GoodType, PricingBehaviourModel> pricingBehaviourModels = new HashMap<GoodType, PricingBehaviourModel>();

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

		/**
		 * total utility sum of all periods; measurement starts after
		 * initialization phase; TODO double overflow possible, group
		 * statistical models that are filled after initialization phase
		 */
		public final PeriodDataAccumulatorTimeSeriesModel totalUtilityOutputModel;

		public NationalEconomyModel(final Currency currency,
				final InputOutputModel inputOutputModel) {
			this.currency = currency;

			for (GoodType goodType : GoodType.values()) {
				// labour hours are logged via household model
				if (!GoodType.LABOURHOUR.equals(goodType)) {
					if (inputOutputModel.getProductionFunction(goodType) != null) {
						this.industryModels.put(goodType, new IndustryModel(
								currency, goodType, inputOutputModel));
					}
				}
			}

			for (GoodType goodType : GoodType.values()) {
				this.pricingBehaviourModels.put(goodType,
						new PricingBehaviourModel(currency, goodType));
			}

			this.householdsModel = new HouseholdsModel(currency,
					inputOutputModel);
			this.stateModel = new StateModel(currency, inputOutputModel);
			this.numberOfAgentsModel = new NumberOfAgentsModel(currency);
			this.pricesModel = new PricesModel();
			this.marketDepthModel = new MarketDepthModel();
			this.keyInterestRateModel = new PeriodDataAccumulatorTimeSeriesModel(
					currency.getIso4217Code() + " key interest rate");
			this.moneySupplyM0Model = new PeriodDataAccumulatorTimeSeriesModel(
					currency.getIso4217Code() + " M0");
			this.moneySupplyM1Model = new PeriodDataAccumulatorTimeSeriesModel(
					currency.getIso4217Code() + " M1");
			this.moneySupplyM2Model = new PeriodDataAccumulatorTimeSeriesModel(
					currency.getIso4217Code() + " M2");
			this.moneyCirculationModel = new PeriodDataAccumulatorTimeSeriesModel(
					currency.getIso4217Code() + " money circulation");
			this.moneyVelocityModel = new PeriodDataQuotientTimeSeriesModel(
					currency.getIso4217Code() + " money velocity");
			this.priceIndexModel = new PeriodDataAccumulatorTimeSeriesModel(
					currency.getIso4217Code() + " price index");
			this.creditUtilizationRateModel = new PeriodDataQuotientTimeSeriesModel(
					currency.getIso4217Code() + " credit util. rate");
			this.balanceSheetsModel = new BalanceSheetsModel(currency);
			this.monetaryTransactionsModel = new MonetaryTransactionsModel();

			this.totalUtilityOutputModel = new PeriodDataAccumulatorTimeSeriesModel(
					currency.getIso4217Code() + " total utility");
		}

		public IndustryModel getIndustryModel(GoodType goodType) {
			return industryModels.get(goodType);
		}

		public PricingBehaviourModel getPricingBehaviourModel(GoodType goodType) {
			return pricingBehaviourModels.get(goodType);
		}

		public void nextPeriod() {
			moneyVelocityModel.add(moneyCirculationModel.getValue(),
					moneySupplyM1Model.getValue());

			for (IndustryModel goodTypeProductionModel : this.industryModels
					.values()) {
				goodTypeProductionModel.nextPeriod();
			}

			for (PricingBehaviourModel pricingBehaviourModel : this.pricingBehaviourModels
					.values()) {
				pricingBehaviourModel.nextPeriod();
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
			this.stateModel.nextPeriod();
		}
	}

	protected final AgentDetailModel agentDetailModel = new AgentDetailModel();

	protected final Map<Currency, NationalEconomyModel> nationalEconomyModels = new HashMap<Currency, NationalEconomyModel>();

	protected final TimeSystemModel timeSystemModel = new TimeSystemModel();

	/**
	 * Requires the input-output model to be set in the application context.
	 */
	public ModelRegistry(final InputOutputModel inputOutputModel) {
		for (Currency currency : Currency.values())
			nationalEconomyModels.put(currency, new NationalEconomyModel(
					currency, inputOutputModel));
	}

	public AgentDetailModel getAgentDetailModel() {
		return agentDetailModel;
	}

	public NationalEconomyModel getNationalEconomyModel(Currency currency) {
		return this.nationalEconomyModels.get(currency);
	}

	public TimeSystemModel getTimeSystemModel() {
		return this.timeSystemModel;
	}

	public void nextHour() {
		this.timeSystemModel.nextHour();
	}

	public void nextPeriod() {
		agentDetailModel.notifyListeners();

		for (NationalEconomyModel nationalEconomyModel : nationalEconomyModels
				.values()) {
			nationalEconomyModel.nextPeriod();
		}
	}

}
