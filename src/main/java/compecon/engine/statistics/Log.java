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
import compecon.engine.Simulation;
import compecon.engine.statistics.model.ModelRegistry.IncomeSource;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.util.MathUtil;
import compecon.materia.GoodType;

public class Log {

	private static Agent agentSelectedByClient;

	private static Agent agentCurrentlyActive;

	// --------

	public static boolean isAgentSelectedByClient(final Agent agent) {
		return agent != null && agentSelectedByClient == agent;
	}

	public static Agent getAgentSelectedByClient() {
		return agentSelectedByClient;
	}

	public static void setAgentSelectedByClient(final Agent agent) {
		agentSelectedByClient = agent;
	}

	public static void setAgentCurrentlyActive(final Agent agent) {
		agentCurrentlyActive = agent;
	}

	// --------

	public static void notifyTimeSystem_nextDay(final Date date) {
		Simulation.getInstance().getModelRegistry().nextPeriod();
	}

	// --------

	public static synchronized void log(final Agent agent, final String message) {
		setAgentCurrentlyActive(agent);
		log(message);
	}

	public static synchronized void log(final Agent agent,
			final Class<? extends ITimeSystemEvent> eventClass,
			final String message) {
		setAgentCurrentlyActive(agent);
		log(eventClass.getSimpleName() + ": " + message);
	}

	public static void log(final String message) {
		if (agentCurrentlyActive != null
				&& agentSelectedByClient == agentCurrentlyActive)
			Simulation
					.getInstance()
					.getModelRegistry()
					.getAgentDetailModel()
					.logAgentEvent(
							Simulation.getInstance().getTimeSystem()
									.getCurrentDate(), message);
	}

	public static void agent_onConstruct(final Agent agent) {
		Simulation.getInstance().getModelRegistry().getAgentDetailModel()
				.agent_onConstruct(agent);
		if (isAgentSelectedByClient(agent))
			log(agent, agent + " constructed");
		Simulation.getInstance().getModelRegistry()
				.getNumberOfAgentsModel(agent.getPrimaryCurrency())
				.agent_onConstruct(agent.getClass());
	}

	public static void agent_onDeconstruct(final Agent agent) {
		Simulation.getInstance().getModelRegistry().getAgentDetailModel()
				.agent_onDeconstruct(agent);
		if (isAgentSelectedByClient(agent))
			log(agent, agent + " deconstructed");
		Simulation.getInstance().getModelRegistry()
				.getNumberOfAgentsModel(agent.getPrimaryCurrency())
				.agent_onDeconstruct(agent.getClass());
		if (agentCurrentlyActive == agent)
			agentCurrentlyActive = null;
		if (agentSelectedByClient == agent)
			agentSelectedByClient = null;
	}

	public static void agent_onPublishBalanceSheet(final Agent agent,
			final BalanceSheet balanceSheet) {
		Simulation.getInstance().getModelRegistry()
				.getBalanceSheetsModel(balanceSheet.referenceCurrency)
				.agent_onPublishBalanceSheet(agent, balanceSheet);

		Simulation.getInstance().getModelRegistry()
				.getMoneySupplyM0Model(balanceSheet.referenceCurrency)
				.add(balanceSheet.hardCash);
		// TODO: what about money in the private banking system? -> M1
		// definition
		Simulation.getInstance().getModelRegistry()
				.getMoneySupplyM1Model(balanceSheet.referenceCurrency)
				.add(balanceSheet.cashShortTerm + balanceSheet.hardCash);
		Simulation
				.getInstance()
				.getModelRegistry()
				.getMoneySupplyM2Model(balanceSheet.referenceCurrency)
				.add(balanceSheet.hardCash + balanceSheet.cashShortTerm
						+ balanceSheet.cashLongTerm);
	}

	public static void agent_CreditUtilization(final Agent agent,
			final double creditUtilization, final double creditCapacity) {
		Simulation.getInstance().getModelRegistry()
				.getCreditUtilizationRateModel(agent.getPrimaryCurrency())
				.add(creditUtilization, creditCapacity);
	}

	// --------

	public static void household_onIncomeWageDividendConsumptionSaving(
			final Currency currency, final double income,
			final double consumptionAmount, final double savingAmount,
			final double wage, final double dividend) {

		Simulation.getInstance().getModelRegistry()
				.getConsumptionModel(currency).add(consumptionAmount);
		Simulation.getInstance().getModelRegistry().getIncomeModel(currency)
				.add(income);
		Simulation.getInstance().getModelRegistry()
				.getConsumptionRateModel(currency)
				.add(consumptionAmount, income);
		Simulation.getInstance().getModelRegistry()
				.getConsumptionIncomeRatioModel(currency)
				.add(consumptionAmount, income);
		Simulation.getInstance().getModelRegistry().getSavingModel(currency)
				.add(savingAmount);
		Simulation.getInstance().getModelRegistry()
				.getSavingRateModel(currency).add(savingAmount, income);
		Simulation.getInstance().getModelRegistry().getWageModel(currency)
				.add(wage);
		Simulation.getInstance().getModelRegistry().getDividendModel(currency)
				.add(dividend);
		Simulation.getInstance().getModelRegistry()
				.getIncomeSourceModel(currency).add(IncomeSource.WAGE, wage);
		Simulation.getInstance().getModelRegistry()
				.getIncomeSourceModel(currency)
				.add(IncomeSource.DIVIDEND, dividend);
		Simulation.getInstance().getModelRegistry()
				.getIncomeDistributionModel(currency).add(income);
	}

	public static void household_onUtility(final Household household,
			final Currency currency,
			final Map<GoodType, Double> bundleOfGoodsToConsume,
			final double utility) {
		if (Log.isAgentSelectedByClient(household)) {
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

			Log.log(household, log);
		}
		Simulation.getInstance().getModelRegistry().getUtilityModel(currency)
				.getOutputModel().add(utility);
		if (!Simulation.getInstance().getTimeSystem().isInitializationPhase()) {
			Simulation.getInstance().getModelRegistry()
					.getUtilityModel(currency).getTotalOutputModel()
					.add(utility);
		}
		for (Entry<GoodType, Double> entry : bundleOfGoodsToConsume.entrySet()) {
			Simulation.getInstance().getModelRegistry()
					.getUtilityModel(currency).getInputModel(entry.getKey())
					.add(entry.getValue());
		}
	}

	public static void household_LabourHourCapacity(final Currency currency,
			final double labourHourCapacity) {
		Simulation.getInstance().getModelRegistry()
				.getLabourHourCapacityModel(currency).add(labourHourCapacity);
	}

	public static void household_onLabourHourExhaust(final Currency currency,
			final double amount) {
		Simulation.getInstance().getModelRegistry()
				.getGoodTypeProductionModel(currency, GoodType.LABOURHOUR)
				.getOutputModel().add(amount);
	}

	// --------

	public static void factory_onProduction(final Factory factory,
			final Currency currency, final GoodType outputGoodType,
			final double output, final Map<GoodType, Double> inputs) {
		Simulation.getInstance().getModelRegistry()
				.getGoodTypeProductionModel(currency, outputGoodType)
				.getOutputModel().add(output);
		for (Entry<GoodType, Double> input : inputs.entrySet()) {
			Simulation.getInstance().getModelRegistry()
					.getGoodTypeProductionModel(currency, outputGoodType)
					.getInputModel(input.getKey()).add(input.getValue());
		}
	}

	// --------

	public static void bank_onTransfer(final BankAccount from,
			final BankAccount to, final Currency currency, final double value,
			final String subject) {
		Simulation
				.getInstance()
				.getModelRegistry()
				.getMonetaryTransactionsModel(currency)
				.bank_onTransfer(from.getOwner().getClass(),
						to.getOwner().getClass(), currency, value);
		Simulation.getInstance().getModelRegistry()
				.getMoneyCirculationModel(currency).add(value);
		if (isAgentSelectedByClient(from.getOwner())) {
			String message = " --- " + Currency.formatMoneySum(value) + " "
					+ currency.getIso4217Code() + " ---> " + to + ": "
					+ subject;
			Simulation
					.getInstance()
					.getModelRegistry()
					.getAgentDetailModel()
					.logBankAccountEvent(
							Simulation.getInstance().getTimeSystem()
									.getCurrentDate(), from, message);
		}
		if (isAgentSelectedByClient(to.getOwner())) {
			String message = " <--- " + Currency.formatMoneySum(value) + " "
					+ currency.getIso4217Code() + " --- " + from + ": "
					+ subject;
			Simulation
					.getInstance()
					.getModelRegistry()
					.getAgentDetailModel()
					.logBankAccountEvent(
							Simulation.getInstance().getTimeSystem()
									.getCurrentDate(), to, message);
		}
	}

	// --------

	public static void centralBank_KeyInterestRate(final Currency currency,
			final double keyInterestRate) {
		Simulation.getInstance().getModelRegistry()
				.getKeyInterestRateModel(currency).add(keyInterestRate);
	}

	public static void centralBank_PriceIndex(final Currency currency,
			final double priceIndex) {
		Simulation.getInstance().getModelRegistry()
				.getPriceIndexModel(currency).add(priceIndex);
	}

	// --------

	public static void market_onTick(final double pricePerUnit,
			final GoodType goodType, final Currency currency,
			final double amount) {
		Simulation.getInstance().getModelRegistry().getPricesModel(currency)
				.market_onTick(pricePerUnit, goodType, currency, amount);
	}

	public static void market_onTick(final double pricePerUnit,
			final Currency commodityCurrency, final Currency currency,
			final double amount) {
		Simulation
				.getInstance()
				.getModelRegistry()
				.getPricesModel(currency)
				.market_onTick(pricePerUnit, commodityCurrency, currency,
						amount);
	}
}
