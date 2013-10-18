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

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import compecon.economy.PricingBehaviour.PricingBehaviourNewPriceDecisionCause;
import compecon.economy.sectors.Agent;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.economy.sectors.industry.Factory;
import compecon.economy.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.engine.statistics.model.ModelRegistry;
import compecon.engine.statistics.model.ModelRegistry.IncomeSource;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.TimeSystem;
import compecon.engine.util.MathUtil;
import compecon.materia.GoodType;
import compecon.math.ConvexFunction.ConvexFunctionTerminationCause;
import compecon.math.production.ConvexProductionFunction.ConvexProductionFunctionTerminationCause;

public class Log {

	protected final ModelRegistry modelRegistry;

	protected final TimeSystem timeSystem;

	private Agent agentSelectedByClient;

	private Agent agentCurrentlyActive;

	public Log(final ModelRegistry modelRegistry, final TimeSystem timeSystem) {
		this.modelRegistry = modelRegistry;
		this.timeSystem = timeSystem;
	}

	// --------

	public boolean isAgentSelectedByClient(final Agent agent) {
		return agent != null && agentSelectedByClient == agent;
	}

	public Agent getAgentSelectedByClient() {
		return agentSelectedByClient;
	}

	public void setAgentSelectedByClient(final Agent agent) {
		agentSelectedByClient = agent;
	}

	public void setAgentCurrentlyActive(final Agent agent) {
		agentCurrentlyActive = agent;
	}

	// --------

	public void notifyTimeSystem_nextDay(final Date date) {
		modelRegistry.nextPeriod();
	}

	// --------

	public synchronized void log(final Agent agent, final String message) {
		setAgentCurrentlyActive(agent);
		log(message);
	}

	public synchronized void log(final Agent agent,
			final Class<? extends ITimeSystemEvent> eventClass,
			final String message) {
		setAgentCurrentlyActive(agent);
		log(eventClass.getSimpleName() + ": " + message);
	}

	public void log(final String message) {
		if (agentCurrentlyActive != null
				&& agentSelectedByClient == agentCurrentlyActive) {
			modelRegistry.agentDetailModel.logAgentEvent(
					timeSystem.getCurrentDate(), message);
		}
	}

	public void agent_onConstruct(final Agent agent) {
		modelRegistry.agentDetailModel.agent_onConstruct(agent);
		if (isAgentSelectedByClient(agent))
			log(agent, agent + " constructed");
		modelRegistry.getNationalEconomyModel(agent.getPrimaryCurrency()).numberOfAgentsModel
				.agent_onConstruct(agent.getClass());
	}

	public void agent_onDeconstruct(final Agent agent) {
		modelRegistry.agentDetailModel.agent_onDeconstruct(agent);
		if (isAgentSelectedByClient(agent))
			log(agent, agent + " deconstructed");
		modelRegistry.getNationalEconomyModel(agent.getPrimaryCurrency()).numberOfAgentsModel
				.agent_onDeconstruct(agent.getClass());
		if (agentCurrentlyActive == agent)
			agentCurrentlyActive = null;
		if (agentSelectedByClient == agent)
			agentSelectedByClient = null;
	}

	public void agent_onPublishBalanceSheet(final Agent agent,
			final BalanceSheet balanceSheet) {
		modelRegistry.getNationalEconomyModel(balanceSheet.referenceCurrency).balanceSheetsModel
				.agent_onPublishBalanceSheet(agent, balanceSheet);

		modelRegistry.getNationalEconomyModel(balanceSheet.referenceCurrency).moneySupplyM0Model
				.add(balanceSheet.hardCash);
		// TODO: what about money in the private banking system? -> M1
		// definition
		modelRegistry.getNationalEconomyModel(balanceSheet.referenceCurrency).moneySupplyM1Model
				.add(balanceSheet.cashShortTerm + balanceSheet.hardCash);
		modelRegistry.getNationalEconomyModel(balanceSheet.referenceCurrency).moneySupplyM2Model
				.add(balanceSheet.hardCash + balanceSheet.cashShortTerm
						+ balanceSheet.cashLongTerm);
	}

	public void agent_CreditUtilization(final Agent agent,
			final double creditUtilization, final double creditCapacity) {
		modelRegistry.getNationalEconomyModel(agent.getPrimaryCurrency()).creditUtilizationRateModel
				.add(creditUtilization, creditCapacity);
	}

	public void agent_onCalculateOutputMaximizingInputsIterative(
			final double budget, final double moneySpent,
			final ConvexFunctionTerminationCause terminationCause) {
		if (agentCurrentlyActive != null) {
			assert (agentCurrentlyActive instanceof Household);

			modelRegistry.getNationalEconomyModel(agentCurrentlyActive
					.getPrimaryCurrency()).householdsModel.convexFunctionTerminationCauseModels
					.get(terminationCause).add(budget - moneySpent);
			modelRegistry.getNationalEconomyModel(agentCurrentlyActive
					.getPrimaryCurrency()).householdsModel.budgetModel
					.add(budget);
		}
	}

	public void pricingBehaviour_onCalculateNewPrice(final Agent agent,
			final PricingBehaviourNewPriceDecisionCause decisionCause) {
		GoodType goodType;
		if (agent instanceof Household) {
			goodType = GoodType.LABOURHOUR;
		} else if (agent instanceof Factory) {
			goodType = ((Factory) agent).getProducedGoodType();
		} else {
			goodType = null;
		}

		if (goodType != null) {
			Currency currency = agent.getPrimaryCurrency();
			modelRegistry.getNationalEconomyModel(currency)
					.getPricingBehaviourModel(goodType).pricingBehaviourNewPriceDecisionCauseModels
					.get(decisionCause).add(1.0);
		}
	}

	// --------

	public void household_onIncomeWageDividendConsumptionSaving(
			final Currency currency, final double income,
			final double consumptionAmount, final double savingAmount,
			final double wage, final double dividend) {

		modelRegistry.getNationalEconomyModel(currency).householdsModel.consumptionModel
				.add(consumptionAmount);
		modelRegistry.getNationalEconomyModel(currency).householdsModel.incomeModel
				.add(income);
		modelRegistry.getNationalEconomyModel(currency).householdsModel.consumptionRateModel
				.add(consumptionAmount, income);
		modelRegistry.getNationalEconomyModel(currency).householdsModel.consumptionIncomeRatioModel
				.add(consumptionAmount, income);
		modelRegistry.getNationalEconomyModel(currency).householdsModel.savingModel
				.add(savingAmount);
		modelRegistry.getNationalEconomyModel(currency).householdsModel.savingRateModel
				.add(savingAmount, income);
		modelRegistry.getNationalEconomyModel(currency).householdsModel.wageModel
				.add(wage);
		modelRegistry.getNationalEconomyModel(currency).householdsModel.dividendModel
				.add(dividend);
		modelRegistry.getNationalEconomyModel(currency).householdsModel.incomeSourceModel
				.add(IncomeSource.WAGE, wage);
		modelRegistry.getNationalEconomyModel(currency).householdsModel.incomeSourceModel
				.add(IncomeSource.DIVIDEND, dividend);
		modelRegistry.getNationalEconomyModel(currency).householdsModel.incomeDistributionModel
				.add(income);
	}

	public void household_onUtility(final Household household,
			final Currency currency,
			final Map<GoodType, Double> bundleOfGoodsToConsume,
			final double utility) {
		if (this.isAgentSelectedByClient(household)) {
			String log = "consumed ";
			int i = 0;
			for (Entry<GoodType, Double> entry : bundleOfGoodsToConsume
					.entrySet()) {
				log += MathUtil.round(entry.getValue()) + " " + entry.getKey();
				if (i < bundleOfGoodsToConsume.size() - 1)
					log += ", ";
				i++;
			}
			log += " -> " + MathUtil.round(utility) + " utility";

			this.log(household, log);
		}
		modelRegistry.getNationalEconomyModel(currency).householdsModel.utilityModel.utilityOutputModel
				.add(utility);
		if (!timeSystem.isInitializationPhase()) {
			modelRegistry.getNationalEconomyModel(currency).householdsModel.utilityModel.totalUtilityOutputModel
					.add(utility);
		}
		for (Entry<GoodType, Double> entry : bundleOfGoodsToConsume.entrySet()) {
			modelRegistry.getNationalEconomyModel(currency).householdsModel.utilityModel.utilityInputModels
					.get(entry.getKey()).add(entry.getValue());
		}
	}

	public void household_onOfferResult(final Currency currency,
			final double labourHoursOffered, final double labourHoursSold,
			final double labourHourCapacity) {
		modelRegistry.getNationalEconomyModel(currency)
				.getPricingBehaviourModel(GoodType.LABOURHOUR).offerModel
				.add(labourHoursOffered);
		modelRegistry.getNationalEconomyModel(currency)
				.getPricingBehaviourModel(GoodType.LABOURHOUR).soldModel
				.add(labourHoursSold);
		modelRegistry.getNationalEconomyModel(currency).householdsModel.labourHourCapacityModel
				.add(labourHourCapacity);
	}

	// --------

	public void factory_onProduction(final Factory factory,
			final Currency currency, final GoodType outputGoodType,
			final double output, final Map<GoodType, Double> inputs) {
		modelRegistry.getNationalEconomyModel(currency).getIndustryModel(
				outputGoodType).outputModel.add(output);
		for (Entry<GoodType, Double> input : inputs.entrySet()) {
			modelRegistry.getNationalEconomyModel(currency).getIndustryModel(
					outputGoodType).inputModels.get(input.getKey()).add(
					input.getValue());
		}
	}

	public void factory_onOfferResult(final Currency currency,
			final GoodType outputGoodType, final double amountOffered,
			final double amountSold, final double inventory) {
		modelRegistry.getNationalEconomyModel(currency)
				.getPricingBehaviourModel(outputGoodType).offerModel
				.add(amountOffered);
		modelRegistry.getNationalEconomyModel(currency)
				.getPricingBehaviourModel(outputGoodType).soldModel
				.add(amountSold);
		modelRegistry.getNationalEconomyModel(currency).getIndustryModel(
				outputGoodType).inventoryModel.add(inventory);
	}

	public void factory_onCalculateProfitMaximizingProductionFactorsIterative(
			final double budget, final double moneySpent,
			final ConvexProductionFunctionTerminationCause terminationCause) {
		if (agentCurrentlyActive != null) {
			assert (agentCurrentlyActive instanceof Factory);

			Currency currency = agentCurrentlyActive.getPrimaryCurrency();
			modelRegistry.getNationalEconomyModel(currency).getIndustryModel(
					((Factory) agentCurrentlyActive).getProducedGoodType()).convexProductionFunctionTerminationCauseModels
					.get(terminationCause).add(budget - moneySpent);
			modelRegistry.getNationalEconomyModel(currency).getIndustryModel(
					((Factory) agentCurrentlyActive).getProducedGoodType()).budgetModel
					.add(budget);
		}
	}

	// --------

	public void bank_onTransfer(final BankAccount from, final BankAccount to,
			final Currency currency, final double value, final String subject) {
		modelRegistry.getNationalEconomyModel(currency).monetaryTransactionsModel
				.bank_onTransfer(from.getOwner().getClass(), to.getOwner()
						.getClass(), currency, value);
		modelRegistry.getNationalEconomyModel(currency).moneyCirculationModel
				.add(value);
		if (isAgentSelectedByClient(from.getOwner())) {
			String message = " --- " + Currency.formatMoneySum(value) + " "
					+ currency.getIso4217Code() + " ---> " + to + ": "
					+ subject;
			modelRegistry.agentDetailModel.logBankAccountEvent(
					timeSystem.getCurrentDate(), from, message);
		}
		if (isAgentSelectedByClient(to.getOwner())) {
			String message = " <--- " + Currency.formatMoneySum(value) + " "
					+ currency.getIso4217Code() + " --- " + from + ": "
					+ subject;
			modelRegistry.agentDetailModel.logBankAccountEvent(
					timeSystem.getCurrentDate(), to, message);
		}
	}

	// --------

	public void centralBank_KeyInterestRate(final Currency currency,
			final double keyInterestRate) {
		modelRegistry.getNationalEconomyModel(currency).keyInterestRateModel
				.add(keyInterestRate);
	}

	public void centralBank_PriceIndex(final Currency currency,
			final double priceIndex) {
		modelRegistry.getNationalEconomyModel(currency).priceIndexModel
				.add(priceIndex);
	}

	// --------

	public void market_onTick(final double pricePerUnit,
			final GoodType goodType, final Currency currency,
			final double amount) {
		modelRegistry.getNationalEconomyModel(currency).pricesModel
				.market_onTick(pricePerUnit, goodType, currency, amount);
	}

	public void market_onTick(final double pricePerUnit,
			final Currency commodityCurrency, final Currency currency,
			final double amount) {
		modelRegistry.getNationalEconomyModel(currency).pricesModel
				.market_onTick(pricePerUnit, commodityCurrency, currency,
						amount);
	}
}
