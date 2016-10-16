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

import io.github.uwol.compecon.economy.agent.Agent;
import io.github.uwol.compecon.economy.behaviour.PricingBehaviour.PricingBehaviourNewPriceDecisionCause;
import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.materia.InputOutputModel;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.statistics.timeseries.PeriodDataAccumulatorTimeSeriesModel;
import io.github.uwol.compecon.engine.statistics.timeseries.PeriodDataPercentageTimeSeriesModel;
import io.github.uwol.compecon.engine.statistics.timeseries.PeriodDataQuotientTimeSeriesModel;
import io.github.uwol.compecon.math.ConvexFunction.ConvexFunctionTerminationCause;
import io.github.uwol.compecon.math.production.ProductionFunction;
import io.github.uwol.compecon.math.production.ConvexProductionFunction.ConvexProductionFunctionTerminationCause;

public class ModelRegistry {

	public enum IncomeSource {
		DIVIDEND, TRANSFERS, WAGE
	}

	public class NationalEconomyModel {

		/**
		 * model for collecting statistics about households
		 */
		public class HouseholdsModel {

			public final PeriodDataAccumulatorTimeSeriesModel budgetModel = new PeriodDataAccumulatorTimeSeriesModel(
					"Budget");

			public final PeriodDataQuotientTimeSeriesModel consumptionIncomeRatioModel;
			public final PeriodDataAccumulatorTimeSeriesModel consumptionModel;
			public final PeriodDataQuotientTimeSeriesModel consumptionRateModel;
			/**
			 * Models metering causes of terminations in convex production
			 * functions, e. g. marginal costs exceeding marginal revenue.
			 */
			public final Map<ConvexFunctionTerminationCause, PeriodDataAccumulatorTimeSeriesModel> convexFunctionTerminationCauseModels = new HashMap<ConvexFunctionTerminationCause, PeriodDataAccumulatorTimeSeriesModel>();
			public final Currency currency;
			public final PeriodDataAccumulatorTimeSeriesModel dividendModel;
			public final PeriodDataAccumulatorTimeSeriesModel governmentTransfersModel;
			public final PeriodDataDistributionModel incomeDistributionModel;
			public final PeriodDataAccumulatorTimeSeriesModel incomeModel;
			public final PeriodDataPercentageTimeSeriesModel<IncomeSource> incomeSourceModel;
			public final PeriodDataAccumulatorTimeSeriesModel labourHourCapacityModel;
			public final PeriodDataAccumulatorTimeSeriesModel retiredModel;
			public final PeriodDataAccumulatorTimeSeriesModel savingModel;
			public final PeriodDataQuotientTimeSeriesModel savingRateModel;
			public final UtilityModel utilityModel;
			public final PeriodDataAccumulatorTimeSeriesModel wageModel;

			public HouseholdsModel(final Currency currency,
					final InputOutputModel inputOutputModel) {
				this.currency = currency;

				for (final ConvexFunctionTerminationCause cause : ConvexFunctionTerminationCause
						.values()) {
					convexFunctionTerminationCauseModels.put(
							cause,
							new PeriodDataAccumulatorTimeSeriesModel(cause
									.toString()));
				}

				consumptionModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " consumption");
				consumptionRateModel = new PeriodDataQuotientTimeSeriesModel(
						currency.getIso4217Code() + " consumption rate");
				consumptionIncomeRatioModel = new PeriodDataQuotientTimeSeriesModel(
						"");
				dividendModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " dividend");
				governmentTransfersModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " government transfers");
				incomeModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " income");
				incomeDistributionModel = new PeriodDataDistributionModel(
						this.currency);
				incomeSourceModel = new PeriodDataPercentageTimeSeriesModel<IncomeSource>(
						IncomeSource.values(), currency.getIso4217Code()
								+ " income source");
				labourHourCapacityModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " " + GoodType.LABOURHOUR
								+ " cap.");
				retiredModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " retired Households");
				savingModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " saving");
				savingRateModel = new PeriodDataQuotientTimeSeriesModel(
						currency.getIso4217Code() + " saving rate");
				wageModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " wage");
				utilityModel = new UtilityModel(this.currency, inputOutputModel);
			}

			public void nextPeriod() {
				for (final PeriodDataAccumulatorTimeSeriesModel periodDataAccumulatorTimeSeriesModel : convexFunctionTerminationCauseModels
						.values()) {
					periodDataAccumulatorTimeSeriesModel.nextPeriod();
				}

				budgetModel.nextPeriod();
				consumptionModel.nextPeriod();
				consumptionRateModel.nextPeriod();
				dividendModel.nextPeriod();
				governmentTransfersModel.nextPeriod();
				incomeModel.nextPeriod();
				incomeSourceModel.nextPeriod();
				incomeDistributionModel.nextPeriod();
				labourHourCapacityModel.nextPeriod();
				retiredModel.nextPeriod();
				savingModel.nextPeriod();
				savingRateModel.nextPeriod();
				utilityModel.nextPeriod();
				wageModel.nextPeriod();
			}
		}

		/**
		 * model for collecting statistics about production input and output of
		 * factories
		 */
		public class IndustryModel {

			public final PeriodDataAccumulatorTimeSeriesModel budgetModel = new PeriodDataAccumulatorTimeSeriesModel(
					"Budget");

			public final PeriodDataAccumulatorTimeSeriesModel capitalDepreciationModel;

			/**
			 * Models metering causes of terminations in convex production
			 * functions, e. g. marginal costs exceeding marginal revenue.
			 */
			public final Map<ConvexProductionFunctionTerminationCause, PeriodDataAccumulatorTimeSeriesModel> convexProductionFunctionTerminationCauseModels = new HashMap<ConvexProductionFunctionTerminationCause, PeriodDataAccumulatorTimeSeriesModel>();

			public final Currency currency;

			public final GoodType goodType;

			public final Map<GoodType, PeriodDataAccumulatorTimeSeriesModel> inputModels = new HashMap<GoodType, PeriodDataAccumulatorTimeSeriesModel>();

			public final PeriodDataAccumulatorTimeSeriesModel inventoryModel;

			public final PeriodDataAccumulatorTimeSeriesModel outputModel;

			public IndustryModel(final Currency currency,
					final GoodType goodType,
					final InputOutputModel inputOutputModel,
					final ProductionFunction productionFunction) {
				assert (!GoodType.LABOURHOUR.equals(goodType));
				assert (productionFunction != null);

				this.currency = currency;
				this.goodType = goodType;

				capitalDepreciationModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " " + goodType
								+ " capital depreciation");
				outputModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " " + goodType + " output");
				inventoryModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " " + goodType
								+ " inventory");

				for (final GoodType inputGoodType : productionFunction
						.getInputGoodTypes()) {
					inputModels.put(
							inputGoodType,
							new PeriodDataAccumulatorTimeSeriesModel(currency
									.getIso4217Code()
									+ " "
									+ inputGoodType
									+ " input"));
				}

				for (final ConvexProductionFunctionTerminationCause cause : ConvexProductionFunctionTerminationCause
						.values()) {
					convexProductionFunctionTerminationCauseModels.put(
							cause,
							new PeriodDataAccumulatorTimeSeriesModel(cause
									.toString()));
				}
			}

			public void nextPeriod() {
				capitalDepreciationModel.nextPeriod();
				outputModel.nextPeriod();
				budgetModel.nextPeriod();
				inventoryModel.nextPeriod();

				for (final PeriodDataAccumulatorTimeSeriesModel periodDataAccumulatorTimeSeriesModel : inputModels
						.values()) {
					periodDataAccumulatorTimeSeriesModel.nextPeriod();
				}

				for (final PeriodDataAccumulatorTimeSeriesModel periodDataAccumulatorTimeSeriesModel : convexProductionFunctionTerminationCauseModels
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

			/**
			 * Models metering causes of terminations in convex production
			 * functions, e. g. marginal costs exceeding marginal revenue.
			 */
			public final Map<PricingBehaviourNewPriceDecisionCause, PeriodDataAccumulatorTimeSeriesModel> pricingBehaviourPriceDecisionCauseModels = new HashMap<PricingBehaviourNewPriceDecisionCause, PeriodDataAccumulatorTimeSeriesModel>();

			public final PeriodDataAccumulatorTimeSeriesModel soldModel;

			public PricingBehaviourModel(final Currency currency,
					final GoodType goodType) {
				this.currency = currency;
				this.goodType = goodType;

				offerModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " " + goodType + " offered");
				soldModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " " + goodType
								+ " sold (+1 period)");
				pricingBehaviourAveragePriceDecisionCauseModel = new PeriodDataAccumulatorTimeSeriesModel(
						"Average Decision");

				for (final PricingBehaviourNewPriceDecisionCause cause : PricingBehaviourNewPriceDecisionCause
						.values()) {
					pricingBehaviourPriceDecisionCauseModels.put(
							cause,
							new PeriodDataAccumulatorTimeSeriesModel(cause
									.toString()));
				}
			}

			public void nextPeriod() {
				pricingBehaviourAveragePriceDecisionCauseModel.nextPeriod();
				offerModel.nextPeriod();
				soldModel.nextPeriod();

				for (final PeriodDataAccumulatorTimeSeriesModel periodDataAccumulatorTimeSeriesModel : pricingBehaviourPriceDecisionCauseModels
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

			// currently not used
			public final UtilityModel utilityModel;

			public StateModel(final Currency currency,
					final InputOutputModel inputOutputModel) {
				this.currency = currency;

				utilityModel = new UtilityModel(this.currency, inputOutputModel);
			}

			public void nextPeriod() {
				utilityModel.nextPeriod();
			}
		}

		/**
		 * model for collecting statistics about utility of households and
		 * states
		 */
		public class UtilityModel {

			public final Currency currency;

			public final Map<GoodType, PeriodDataAccumulatorTimeSeriesModel> utilityInputModels = new HashMap<GoodType, PeriodDataAccumulatorTimeSeriesModel>();

			public final PeriodDataAccumulatorTimeSeriesModel utilityOutputModel;

			public UtilityModel(final Currency currency,
					final InputOutputModel inputOutputModel) {
				this.currency = currency;
				utilityOutputModel = new PeriodDataAccumulatorTimeSeriesModel(
						currency.getIso4217Code() + " utility");

				for (final GoodType goodType : inputOutputModel
						.getUtilityFunctionOfHousehold().getInputGoodTypes()) {
					utilityInputModels.put(
							goodType,
							new PeriodDataAccumulatorTimeSeriesModel(currency
									.getIso4217Code()
									+ " "
									+ goodType
									+ " input"));
				}
			}

			public void nextPeriod() {
				utilityOutputModel.nextPeriod();

				for (final PeriodDataAccumulatorTimeSeriesModel periodDataAccumulatorTimeSeriesModel : utilityInputModels
						.values()) {
					periodDataAccumulatorTimeSeriesModel.nextPeriod();
				}
			}
		}

		public final BalanceSheetsModel balanceSheetsModel;

		/*
		 * agents
		 */

		// credit
		public final PeriodDataQuotientTimeSeriesModel creditUtilizationRateModel;

		/*
		 * households: consumption and saving
		 */

		public final Currency currency;

		/*
		 * industries
		 */

		public final HouseholdsModel householdsModel;

		/*
		 * state
		 */

		public final Map<GoodType, IndustryModel> industryModels = new HashMap<GoodType, IndustryModel>();

		/*
		 * prices
		 */

		public final PeriodDataAccumulatorTimeSeriesModel keyInterestRateModel;

		public final MarketDepthModel marketDepthModel;

		public final MonetaryTransactionsModel monetaryTransactionsModel;

		/*
		 * money
		 */

		public final PeriodDataAccumulatorTimeSeriesModel moneyCirculationModel;
		public final PeriodDataAccumulatorTimeSeriesModel moneySupplyM0Model;
		public final PeriodDataAccumulatorTimeSeriesModel moneySupplyM1Model;
		public final PeriodDataAccumulatorTimeSeriesModel moneySupplyM2Model;
		public final PeriodDataQuotientTimeSeriesModel moneyVelocityModel;
		public final Map<Class<? extends Agent>, PeriodDataAccumulatorTimeSeriesModel> numberOfAgentsModels = new HashMap<Class<? extends Agent>, PeriodDataAccumulatorTimeSeriesModel>();
		public final PeriodDataAccumulatorTimeSeriesModel priceIndexModel;

		public final PricesModel pricesModel;

		/*
		 * national balances
		 */

		public final Map<GoodType, PricingBehaviourModel> pricingBehaviourModels = new HashMap<GoodType, PricingBehaviourModel>();
		public final StateModel stateModel;

		/**
		 * total utility sum of all periods; measurement starts after
		 * initialization phase; TODO double overflow possible, group
		 * statistical models that are filled after initialization phase
		 */
		public final PeriodDataAccumulatorTimeSeriesModel totalUtilityOutputModel;

		public NationalEconomyModel(final Currency currency,
				final InputOutputModel inputOutputModel) {
			this.currency = currency;

			for (final GoodType goodType : GoodType.values()) {
				// labour hours are logged via household model
				if (!GoodType.LABOURHOUR.equals(goodType)) {
					final ProductionFunction productionFunction = inputOutputModel
							.getProductionFunction(goodType);

					// only good types that can be produced form an industry
					if (productionFunction != null) {
						industryModels.put(goodType, new IndustryModel(
								currency, goodType, inputOutputModel,
								productionFunction));
					}
				}
			}

			for (final GoodType goodType : GoodType.values()) {
				pricingBehaviourModels.put(goodType, new PricingBehaviourModel(
						currency, goodType));
			}

			householdsModel = new HouseholdsModel(currency, inputOutputModel);
			stateModel = new StateModel(currency, inputOutputModel);

			for (final Class<? extends Agent> agentType : ApplicationContext
					.getInstance().getAgentFactory().getAgentTypes()) {
				numberOfAgentsModels.put(
						agentType,
						new PeriodDataAccumulatorTimeSeriesModel(currency
								.getIso4217Code()
								+ " "
								+ agentType.getSimpleName()));
			}

			pricesModel = new PricesModel();
			marketDepthModel = new MarketDepthModel();
			keyInterestRateModel = new PeriodDataAccumulatorTimeSeriesModel(
					currency.getIso4217Code() + " key interest rate");
			moneySupplyM0Model = new PeriodDataAccumulatorTimeSeriesModel(
					currency.getIso4217Code() + " M0");
			moneySupplyM1Model = new PeriodDataAccumulatorTimeSeriesModel(
					currency.getIso4217Code() + " M1");
			moneySupplyM2Model = new PeriodDataAccumulatorTimeSeriesModel(
					currency.getIso4217Code() + " M2");
			moneyCirculationModel = new PeriodDataAccumulatorTimeSeriesModel(
					currency.getIso4217Code() + " money circulation");
			moneyVelocityModel = new PeriodDataQuotientTimeSeriesModel(
					currency.getIso4217Code() + " money velocity");
			priceIndexModel = new PeriodDataAccumulatorTimeSeriesModel(
					currency.getIso4217Code() + " price index");
			creditUtilizationRateModel = new PeriodDataQuotientTimeSeriesModel(
					currency.getIso4217Code() + " credit util. rate");
			balanceSheetsModel = new BalanceSheetsModel(currency);
			monetaryTransactionsModel = new MonetaryTransactionsModel();

			totalUtilityOutputModel = new PeriodDataAccumulatorTimeSeriesModel(
					currency.getIso4217Code() + " total utility");
		}

		public IndustryModel getIndustryModel(final GoodType goodType) {
			return industryModels.get(goodType);
		}

		public PricingBehaviourModel getPricingBehaviourModel(
				final GoodType goodType) {
			return pricingBehaviourModels.get(goodType);
		}

		public void nextPeriod() {
			moneyVelocityModel.add(moneyCirculationModel.getValue(),
					moneySupplyM1Model.getValue());

			for (final IndustryModel goodTypeProductionModel : industryModels
					.values()) {
				goodTypeProductionModel.nextPeriod();
			}

			for (final PricingBehaviourModel pricingBehaviourModel : pricingBehaviourModels
					.values()) {
				pricingBehaviourModel.nextPeriod();
			}

			householdsModel.nextPeriod();
			balanceSheetsModel.nextPeriod();
			creditUtilizationRateModel.nextPeriod();
			keyInterestRateModel.nextPeriod();
			marketDepthModel.nextPeriod();
			monetaryTransactionsModel.nextPeriod();
			moneySupplyM0Model.nextPeriod();
			moneySupplyM1Model.nextPeriod();
			moneySupplyM2Model.nextPeriod();
			moneyCirculationModel.nextPeriod();
			moneyVelocityModel.nextPeriod();

			for (final PeriodDataAccumulatorTimeSeriesModel numberOfAgentsModel : numberOfAgentsModels
					.values()) {
				numberOfAgentsModel.nextPeriod();
			}

			pricesModel.nextPeriod();
			priceIndexModel.nextPeriod();
			stateModel.nextPeriod();
		}
	}

	protected final AgentDetailModel agentDetailModel = new AgentDetailModel();

	protected final Map<Currency, NationalEconomyModel> nationalEconomyModels = new HashMap<Currency, NationalEconomyModel>();

	protected final TimeSystemModel timeSystemModel = new TimeSystemModel();

	/**
	 * Requires the input-output model to be set in the application context.
	 */
	public ModelRegistry(final InputOutputModel inputOutputModel) {
		for (final Currency currency : Currency.values()) {
			nationalEconomyModels.put(currency, new NationalEconomyModel(
					currency, inputOutputModel));
		}
	}

	public AgentDetailModel getAgentDetailModel() {
		return agentDetailModel;
	}

	public NationalEconomyModel getNationalEconomyModel(final Currency currency) {
		return nationalEconomyModels.get(currency);
	}

	public TimeSystemModel getTimeSystemModel() {
		return timeSystemModel;
	}

	public void nextHour() {
		timeSystemModel.nextHour();
	}

	public void nextPeriod() {
		agentDetailModel.notifyListeners();

		for (final NationalEconomyModel nationalEconomyModel : nationalEconomyModels
				.values()) {
			nationalEconomyModel.nextPeriod();
		}
	}

}
