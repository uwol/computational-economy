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
		modelRegistry.getNumberOfAgentsModel(agent.getPrimaryCurrency())
				.agent_onConstruct(agent.getClass());
	}

	public void agent_onDeconstruct(final Agent agent) {
		modelRegistry.agentDetailModel.agent_onDeconstruct(agent);
		if (isAgentSelectedByClient(agent))
			log(agent, agent + " deconstructed");
		modelRegistry.getNumberOfAgentsModel(agent.getPrimaryCurrency())
				.agent_onDeconstruct(agent.getClass());
		if (agentCurrentlyActive == agent)
			agentCurrentlyActive = null;
		if (agentSelectedByClient == agent)
			agentSelectedByClient = null;
	}

	public void agent_onPublishBalanceSheet(final Agent agent,
			final BalanceSheet balanceSheet) {
		modelRegistry.getBalanceSheetsModel(balanceSheet.referenceCurrency)
				.agent_onPublishBalanceSheet(agent, balanceSheet);

		modelRegistry.getMoneySupplyM0Model(balanceSheet.referenceCurrency)
				.add(balanceSheet.hardCash);
		// TODO: what about money in the private banking system? -> M1
		// definition
		modelRegistry.getMoneySupplyM1Model(balanceSheet.referenceCurrency)
				.add(balanceSheet.cashShortTerm + balanceSheet.hardCash);
		modelRegistry.getMoneySupplyM2Model(balanceSheet.referenceCurrency)
				.add(balanceSheet.hardCash + balanceSheet.cashShortTerm
						+ balanceSheet.cashLongTerm);
	}

	public void agent_CreditUtilization(final Agent agent,
			final double creditUtilization, final double creditCapacity) {
		modelRegistry.getCreditUtilizationRateModel(agent.getPrimaryCurrency())
				.add(creditUtilization, creditCapacity);
	}

	public void agent_onCalculateOutputMaximizingInputsIterative(
			final double budget, final double moneySpent,
			final ConvexFunctionTerminationCause terminationCause) {
		if (agentCurrentlyActive != null) {
			assert (agentCurrentlyActive instanceof Household);

			modelRegistry
					.getConvexFunctionTerminationCauseModel(
							agentCurrentlyActive.getPrimaryCurrency(),
							terminationCause).add(budget - moneySpent);
			modelRegistry.getHouseholdModel(agentCurrentlyActive
					.getPrimaryCurrency()).budgetModel.add(budget);
		}
	}

	// --------

	public void household_onIncomeWageDividendConsumptionSaving(
			final Currency currency, final double income,
			final double consumptionAmount, final double savingAmount,
			final double wage, final double dividend) {

		modelRegistry.getConsumptionModel(currency).add(consumptionAmount);
		modelRegistry.getIncomeModel(currency).add(income);
		modelRegistry.getConsumptionRateModel(currency).add(consumptionAmount,
				income);
		modelRegistry.getConsumptionIncomeRatioModel(currency).add(
				consumptionAmount, income);
		modelRegistry.getSavingModel(currency).add(savingAmount);
		modelRegistry.getSavingRateModel(currency).add(savingAmount, income);
		modelRegistry.getWageModel(currency).add(wage);
		modelRegistry.getDividendModel(currency).add(dividend);
		modelRegistry.getIncomeSourceModel(currency).add(IncomeSource.WAGE,
				wage);
		modelRegistry.getIncomeSourceModel(currency).add(IncomeSource.DIVIDEND,
				dividend);
		modelRegistry.getIncomeDistributionModel(currency).add(income);
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
		modelRegistry.getUtilityModel(currency).utilityOutputModel.add(utility);
		if (!timeSystem.isInitializationPhase()) {
			modelRegistry.getUtilityModel(currency).totalUtilityOutputModel
					.add(utility);
		}
		for (Entry<GoodType, Double> entry : bundleOfGoodsToConsume.entrySet()) {
			modelRegistry.getUtilityModel(currency).utilityInputModels.get(
					entry.getKey()).add(entry.getValue());
		}
	}

	public void household_LabourHourCapacity(final Currency currency,
			final double labourHourCapacity) {
		modelRegistry.getLabourHourCapacityModel(currency).add(
				labourHourCapacity);
	}

	public void household_onLabourHourExhaust(final Currency currency,
			final double amount) {
		modelRegistry.getFactoryProductionModel(currency, GoodType.LABOURHOUR).outputModel
				.add(amount);
	}

	// --------

	public void factory_onProduction(final Factory factory,
			final Currency currency, final GoodType outputGoodType,
			final double output, final Map<GoodType, Double> inputs) {
		modelRegistry.getFactoryProductionModel(currency, outputGoodType).outputModel
				.add(output);
		for (Entry<GoodType, Double> input : inputs.entrySet()) {
			modelRegistry.getFactoryProductionModel(currency, outputGoodType).inputModels
					.get(input.getKey()).add(input.getValue());
		}
	}

	public void factory_onCalculateProfitMaximizingProductionFactorsIterative(
			final double budget, final double moneySpent,
			final ConvexProductionFunctionTerminationCause terminationCause) {
		if (agentCurrentlyActive != null) {
			assert (agentCurrentlyActive instanceof Factory);

			modelRegistry.getFactoryProductionModel(
					agentCurrentlyActive.getPrimaryCurrency(),
					((Factory) agentCurrentlyActive).getProducedGoodType()).convexProductionFunctionTerminationCauseModels
					.get(terminationCause).add(budget - moneySpent);
			modelRegistry.getFactoryProductionModel(
					agentCurrentlyActive.getPrimaryCurrency(),
					((Factory) agentCurrentlyActive).getProducedGoodType()).budgetModel
					.add(budget);
		}
	}

	// --------

	public void bank_onTransfer(final BankAccount from, final BankAccount to,
			final Currency currency, final double value, final String subject) {
		modelRegistry.getMonetaryTransactionsModel(currency).bank_onTransfer(
				from.getOwner().getClass(), to.getOwner().getClass(), currency,
				value);
		modelRegistry.getMoneyCirculationModel(currency).add(value);
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
		modelRegistry.getKeyInterestRateModel(currency).add(keyInterestRate);
	}

	public void centralBank_PriceIndex(final Currency currency,
			final double priceIndex) {
		modelRegistry.getPriceIndexModel(currency).add(priceIndex);
	}

	// --------

	public void market_onTick(final double pricePerUnit,
			final GoodType goodType, final Currency currency,
			final double amount) {
		modelRegistry.getPricesModel(currency).market_onTick(pricePerUnit,
				goodType, currency, amount);
	}

	public void market_onTick(final double pricePerUnit,
			final Currency commodityCurrency, final Currency currency,
			final double amount) {
		modelRegistry.getPricesModel(currency).market_onTick(pricePerUnit,
				commodityCurrency, currency, amount);
	}
}
