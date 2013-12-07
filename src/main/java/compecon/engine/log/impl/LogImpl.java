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

package compecon.engine.log.impl;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import compecon.economy.agent.Agent;
import compecon.economy.behaviour.PricingBehaviour.PricingBehaviourNewPriceDecisionCause;
import compecon.economy.bookkeeping.impl.BalanceSheetDTO;
import compecon.economy.markets.MarketParticipant;
import compecon.economy.materia.GoodType;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.economy.sectors.industry.Factory;
import compecon.economy.sectors.state.State;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.log.Log;
import compecon.engine.statistics.ModelRegistry.IncomeSource;
import compecon.engine.timesystem.ITimeSystemEvent;
import compecon.math.ConvexFunction.ConvexFunctionTerminationCause;
import compecon.math.production.ConvexProductionFunction.ConvexProductionFunctionTerminationCause;
import compecon.math.util.MathUtil;

public class LogImpl implements Log {

	private Agent agentSelectedByClient;

	private Agent agentCurrentlyActive;

	// --------

	public boolean isAgentSelectedByClient(final Agent agent) {
		return agent != null && agentSelectedByClient == agent;
	}

	public boolean isAgentSelectedByClient(
			final MarketParticipant marketParticipant) {
		return marketParticipant != null
				&& agentSelectedByClient == marketParticipant;
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
		ApplicationContext.getInstance().getModelRegistry().nextPeriod();
	}

	public void notifyTimeSystem_nextHour(final Date date) {
		ApplicationContext.getInstance().getModelRegistry().nextHour();
	}

	// --------

	public synchronized void log(final Agent agent, final String message) {
		setAgentCurrentlyActive(agent);
		log(message);
	}

	public synchronized void log(final MarketParticipant marketParticipant,
			final String message) {
		if (marketParticipant instanceof Agent) {
			log((Agent) marketParticipant, message);
		}
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
			ApplicationContext
					.getInstance()
					.getModelRegistry()
					.getAgentDetailModel()
					.logAgentEvent(
							ApplicationContext.getInstance().getTimeSystem()
									.getCurrentDate(), message);
		}
	}

	public void agent_onConstruct(final Agent agent) {
		ApplicationContext.getInstance().getModelRegistry()
				.getAgentDetailModel().agent_onConstruct(agent);
		if (isAgentSelectedByClient(agent))
			log(agent, agent + " constructed");
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(agent.getPrimaryCurrency()).numberOfAgentsModel
				.agent_onConstruct(agent.getClass());
	}

	public void agent_onDeconstruct(final Agent agent) {
		ApplicationContext.getInstance().getModelRegistry()
				.getAgentDetailModel().agent_onDeconstruct(agent);
		if (isAgentSelectedByClient(agent))
			log(agent, agent + " deconstructed");
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(agent.getPrimaryCurrency()).numberOfAgentsModel
				.agent_onDeconstruct(agent.getClass());
		if (agentCurrentlyActive == agent)
			agentCurrentlyActive = null;
		if (agentSelectedByClient == agent)
			agentSelectedByClient = null;
	}

	public void agent_onPublishBalanceSheet(final Agent agent,
			final BalanceSheetDTO balanceSheet) {
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(balanceSheet.referenceCurrency).balanceSheetsModel
				.agent_onPublishBalanceSheet(agent, balanceSheet);

		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(balanceSheet.referenceCurrency).moneySupplyM0Model
				.add(balanceSheet.hardCash);
		// TODO: what about money in the private banking system? -> M1
		// definition
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(balanceSheet.referenceCurrency).moneySupplyM1Model
				.add(balanceSheet.hardCash + balanceSheet.cashGiroShortTerm);
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(balanceSheet.referenceCurrency).moneySupplyM2Model
				.add(balanceSheet.hardCash + balanceSheet.cashGiroShortTerm
						+ balanceSheet.cashGiroLongTerm);
	}

	public void agent_CreditUtilization(final Agent agent,
			final double creditUtilization, final double creditCapacity) {
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(agent.getPrimaryCurrency()).creditUtilizationRateModel
				.add(creditUtilization, creditCapacity);
	}

	public void agent_onCalculateOutputMaximizingInputsIterative(
			final double budget, final double moneySpent,
			final ConvexFunctionTerminationCause terminationCause) {
		if (agentCurrentlyActive != null) {
			assert (agentCurrentlyActive instanceof Household || agentCurrentlyActive instanceof State);

			ApplicationContext
					.getInstance()
					.getModelRegistry()
					.getNationalEconomyModel(
							agentCurrentlyActive.getPrimaryCurrency()).householdsModel.convexFunctionTerminationCauseModels
					.get(terminationCause).add(moneySpent);
			ApplicationContext
					.getInstance()
					.getModelRegistry()
					.getNationalEconomyModel(
							agentCurrentlyActive.getPrimaryCurrency()).householdsModel.budgetModel
					.add(budget);
		}
	}

	// --------

	private void agent_onUtility(final Agent agent, final Currency currency,
			final Map<GoodType, Double> bundleOfGoodsToConsume,
			final double utility) {
		if (this.isAgentSelectedByClient(agent)) {
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

			this.log(agent, log);
		}

		if (!ApplicationContext.getInstance().getTimeSystem()
				.isInitializationPhase()) {
			ApplicationContext.getInstance().getModelRegistry()
					.getNationalEconomyModel(currency).totalUtilityOutputModel
					.add(utility);
		}
	}

	public void household_onIncomeWageDividendConsumptionSaving(
			final Currency currency, final double income,
			final double consumptionAmount, final double savingAmount,
			final double wage, final double dividend) {

		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.consumptionModel
				.add(consumptionAmount);
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.incomeModel
				.add(income);
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.consumptionRateModel
				.add(consumptionAmount, income);
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.consumptionIncomeRatioModel
				.add(consumptionAmount, income);
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.savingModel
				.add(savingAmount);
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.savingRateModel
				.add(savingAmount, income);
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.wageModel
				.add(wage);
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.dividendModel
				.add(dividend);
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.incomeSourceModel
				.add(IncomeSource.WAGE, wage);
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.incomeSourceModel
				.add(IncomeSource.DIVIDEND, dividend);
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.incomeDistributionModel
				.add(income);
	}

	public void household_onUtility(final Household household,
			final Currency currency,
			final Map<GoodType, Double> bundleOfGoodsToConsume,
			final double utility) {
		this.agent_onUtility(household, currency, bundleOfGoodsToConsume,
				utility);
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.utilityModel.utilityOutputModel
				.add(utility);
		for (Entry<GoodType, Double> entry : bundleOfGoodsToConsume.entrySet()) {
			ApplicationContext.getInstance().getModelRegistry()
					.getNationalEconomyModel(currency).householdsModel.utilityModel.utilityInputModels
					.get(entry.getKey()).add(entry.getValue());
		}
	}

	public void household_AmountSold(final Currency currency,
			final double labourHoursSold) {
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency)
				.getPricingBehaviourModel(GoodType.LABOURHOUR).soldModel
				.add(labourHoursSold);
	}

	public void household_onOfferResult(final Currency currency,
			final double labourHoursOffered, final double labourHourCapacity) {
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency)
				.getPricingBehaviourModel(GoodType.LABOURHOUR).offerModel
				.add(labourHoursOffered);
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).householdsModel.labourHourCapacityModel
				.add(labourHourCapacity);
	}

	// --------

	public void factory_onProduction(final Factory factory,
			final Currency currency, final GoodType outputGoodType,
			final double output, final Map<GoodType, Double> inputs) {
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency)
				.getIndustryModel(outputGoodType).outputModel.add(output);
		for (Entry<GoodType, Double> input : inputs.entrySet()) {
			ApplicationContext.getInstance().getModelRegistry()
					.getNationalEconomyModel(currency)
					.getIndustryModel(outputGoodType).inputModels.get(
					input.getKey()).add(input.getValue());
		}
	}

	public void factory_AmountSold(final Currency currency,
			final GoodType outputGoodType, final double amountSold) {
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency)
				.getPricingBehaviourModel(outputGoodType).soldModel
				.add(amountSold);
	}

	public void factory_onOfferGoodType(final Currency currency,
			final GoodType outputGoodType, final double amountOffered,
			final double inventory) {
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency)
				.getPricingBehaviourModel(outputGoodType).offerModel
				.add(amountOffered);
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency)
				.getIndustryModel(outputGoodType).inventoryModel.add(inventory);
	}

	public void factory_onCalculateProfitMaximizingProductionFactorsIterative(
			final double budget, final double moneySpent,
			final ConvexProductionFunctionTerminationCause terminationCause) {
		if (agentCurrentlyActive != null) {
			assert (agentCurrentlyActive instanceof Factory);

			Currency currency = agentCurrentlyActive.getPrimaryCurrency();
			ApplicationContext
					.getInstance()
					.getModelRegistry()
					.getNationalEconomyModel(currency)
					.getIndustryModel(
							((Factory) agentCurrentlyActive)
									.getProducedGoodType()).convexProductionFunctionTerminationCauseModels
					.get(terminationCause).add(moneySpent);
			ApplicationContext
					.getInstance()
					.getModelRegistry()
					.getNationalEconomyModel(currency)
					.getIndustryModel(
							((Factory) agentCurrentlyActive)
									.getProducedGoodType()).budgetModel
					.add(budget);
		}
	}

	// --------

	public void state_onUtility(final State state, final Currency currency,
			final Map<GoodType, Double> bundleOfGoodsToConsume,
			final double utility) {
		this.agent_onUtility(state, currency, bundleOfGoodsToConsume, utility);
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).stateModel.utilityModel.utilityOutputModel
				.add(utility);
		for (Entry<GoodType, Double> entry : bundleOfGoodsToConsume.entrySet()) {
			ApplicationContext.getInstance().getModelRegistry()
					.getNationalEconomyModel(currency).stateModel.utilityModel.utilityInputModels
					.get(entry.getKey()).add(entry.getValue());
		}
	}

	public void bank_onTransfer(final BankAccount from, final BankAccount to,
			final Currency currency, final double value, final String subject) {
		// only if this is a transfer between agents; alternatively it could be
		// a transfer between bank accounts of this agent
		if (from.getOwner() != to.getOwner()) {
			ApplicationContext.getInstance().getModelRegistry()
					.getNationalEconomyModel(currency).monetaryTransactionsModel
					.bank_onTransfer(from.getOwner().getClass(), to.getOwner()
							.getClass(), currency, value);
			ApplicationContext.getInstance().getModelRegistry()
					.getNationalEconomyModel(currency).moneyCirculationModel
					.add(value);
		}

		if (isAgentSelectedByClient(from.getOwner())) {
			String message = " --- " + Currency.formatMoneySum(value) + " "
					+ currency.getIso4217Code() + " ---> " + to + ": "
					+ subject;
			ApplicationContext
					.getInstance()
					.getModelRegistry()
					.getAgentDetailModel()
					.logBankAccountEvent(
							ApplicationContext.getInstance().getTimeSystem()
									.getCurrentDate(), from, message);
		}
		if (isAgentSelectedByClient(to.getOwner())) {
			String message = " <--- " + Currency.formatMoneySum(value) + " "
					+ currency.getIso4217Code() + " --- " + from + ": "
					+ subject;
			ApplicationContext
					.getInstance()
					.getModelRegistry()
					.getAgentDetailModel()
					.logBankAccountEvent(
							ApplicationContext.getInstance().getTimeSystem()
									.getCurrentDate(), to, message);
		}
	}

	// --------

	public void centralBank_KeyInterestRate(final Currency currency,
			final double keyInterestRate) {
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).keyInterestRateModel
				.add(keyInterestRate);
	}

	public void centralBank_PriceIndex(final Currency currency,
			final double priceIndex) {
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).priceIndexModel
				.add(priceIndex);
	}

	// --------

	public void market_onTick(final double pricePerUnit,
			final GoodType goodType, final Currency currency,
			final double amount) {
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).pricesModel.market_onTick(
				pricePerUnit, goodType, currency, amount);
	}

	public void market_onTick(final double pricePerUnit,
			final Currency commodityCurrency, final Currency currency,
			final double amount) {
		ApplicationContext.getInstance().getModelRegistry()
				.getNationalEconomyModel(currency).pricesModel.market_onTick(
				pricePerUnit, commodityCurrency, currency, amount);
	}

	// --------

	public void pricingBehaviour_onCalculateNewPrice(final Agent agent,
			final PricingBehaviourNewPriceDecisionCause decisionCause,
			final double weight) {
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
			ApplicationContext.getInstance().getModelRegistry()
					.getNationalEconomyModel(currency)
					.getPricingBehaviourModel(goodType).pricingBehaviourPriceDecisionCauseModels
					.get(decisionCause).add(weight);
			ApplicationContext.getInstance().getModelRegistry()
					.getNationalEconomyModel(currency)
					.getPricingBehaviourModel(goodType).pricingBehaviourAveragePriceDecisionCauseModel
					.add(weight);
		}
	}
}
