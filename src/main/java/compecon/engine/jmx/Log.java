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

package compecon.engine.jmx;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.economy.sectors.industry.Factory;
import compecon.economy.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.engine.Agent;
import compecon.engine.jmx.model.ModelRegistry;
import compecon.engine.jmx.model.ModelRegistry.IncomeSource;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.TimeSystem;
import compecon.engine.util.MathUtil;
import compecon.materia.GoodType;

public class Log {

	private static Agent agentSelectedByClient;

	private static Agent agentCurrentlyActive;

	// --------

	public static boolean isAgentSelectedByClient(Agent agent) {
		return agent != null && agentSelectedByClient == agent;
	}

	public static Agent getAgentSelectedByClient() {
		return agentSelectedByClient;
	}

	public static void setAgentSelectedByClient(Agent agent) {
		agentSelectedByClient = agent;
	}

	public static void setAgentCurrentlyActive(Agent agent) {
		agentCurrentlyActive = agent;
	}

	// --------

	public static void notifyTimeSystem_nextDay(Date date) {
		ModelRegistry.nextPeriod();
	}

	// --------

	public static synchronized void log(Agent agent, String message) {
		setAgentCurrentlyActive(agent);
		log(message);
	}

	public static synchronized void log(Agent agent,
			Class<? extends ITimeSystemEvent> eventClass, String message) {
		setAgentCurrentlyActive(agent);
		log(eventClass.getSimpleName() + ": " + message);
	}

	public static void log(String message) {
		if (agentCurrentlyActive != null
				&& agentSelectedByClient == agentCurrentlyActive)
			ModelRegistry.getAgentDetailModel().logAgentEvent(
					TimeSystem.getInstance().getCurrentDate(), message);
	}

	public static void agent_onConstruct(Agent agent) {
		ModelRegistry.getAgentDetailModel().agent_onConstruct(agent);
		if (isAgentSelectedByClient(agent))
			log(agent, agent + " constructed");
		ModelRegistry.getNumberOfAgentsModel(agent.getPrimaryCurrency())
				.agent_onConstruct(agent.getClass());
	}

	public static void agent_onDeconstruct(Agent agent) {
		ModelRegistry.getAgentDetailModel().agent_onDeconstruct(agent);
		if (isAgentSelectedByClient(agent))
			log(agent, agent + " deconstructed");
		ModelRegistry.getNumberOfAgentsModel(agent.getPrimaryCurrency())
				.agent_onDeconstruct(agent.getClass());
	}

	public static void agent_onPublishBalanceSheet(Agent agent,
			BalanceSheet balanceSheet) {
		ModelRegistry.getBalanceSheetsModel(balanceSheet.referenceCurrency)
				.agent_onPublishBalanceSheet(agent, balanceSheet);
	}

	// --------

	public static void household_onIncomeWageDividendConsumptionSaving(
			Currency currency, double income, double consumptionAmount,
			double savingAmount, double wage, double dividend) {

		ModelRegistry.getConsumptionModel(currency).add(consumptionAmount);
		ModelRegistry.getIncomeModel(currency).add(income);
		ModelRegistry.getConsumptionRateModel(currency).add(consumptionAmount,
				income);
		ModelRegistry.getConsumptionIncomeRatioModel(currency).add(
				consumptionAmount, income);
		ModelRegistry.getSavingModel(currency).add(savingAmount);
		ModelRegistry.getSavingRateModel(currency).add(savingAmount, income);
		ModelRegistry.getWageModel(currency).add(wage);
		ModelRegistry.getDividendModel(currency).add(dividend);
		ModelRegistry.getIncomeSourceModel(currency).add(IncomeSource.WAGE,
				wage);
		ModelRegistry.getIncomeSourceModel(currency).add(IncomeSource.DIVIDEND,
				dividend);
		ModelRegistry.getIncomeDistributionModel(currency).add(income);
	}

	public static void household_onUtility(Household household,
			Currency currency, Map<GoodType, Double> bundleOfGoodsToConsume,
			double utility) {
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
		ModelRegistry.getUtilityModel(currency).getOutputModel().add(utility);
		for (Entry<GoodType, Double> entry : bundleOfGoodsToConsume.entrySet()) {
			ModelRegistry.getUtilityModel(currency)
					.getInputModel(entry.getKey()).add(entry.getValue());
		}
	}

	public static void household_LabourHourCapacity(Currency currency,
			double labourHourCapacity) {
		ModelRegistry.getLabourHourCapacityModel(currency).add(
				labourHourCapacity);
	}

	public static void household_onLabourHourExhaust(Currency currency,
			double amount) {
		ModelRegistry.getGoodTypeProductionModel(currency, GoodType.LABOURHOUR)
				.getOutputModel().add(amount);
	}

	// --------

	public static void factory_onProduction(Factory factory, Currency currency,
			GoodType outputGoodType, double output, Map<GoodType, Double> inputs) {
		ModelRegistry.getGoodTypeProductionModel(currency, outputGoodType)
				.getOutputModel().add(output);
		for (Entry<GoodType, Double> input : inputs.entrySet()) {
			ModelRegistry.getGoodTypeProductionModel(currency, outputGoodType)
					.getInputModel(input.getKey()).add(input.getValue());
		}
	}

	// --------

	public static void bank_onTransfer(BankAccount from, BankAccount to,
			Currency currency, double value, String subject) {
		ModelRegistry.getMonetaryTransactionsModel(currency).bank_onTransfer(
				from.getOwner().getClass(), to.getOwner().getClass(), currency,
				value);
		if (isAgentSelectedByClient(from.getOwner())) {
			String message = " --- " + Currency.formatMoneySum(value) + " "
					+ currency.getIso4217Code() + " ---> " + to + ": "
					+ subject;
			ModelRegistry.getAgentDetailModel().logBankAccountEvent(
					TimeSystem.getInstance().getCurrentDate(), from, message);
		}
		if (isAgentSelectedByClient(to.getOwner())) {
			String message = " <--- " + Currency.formatMoneySum(value) + " "
					+ currency.getIso4217Code() + " --- " + from + ": "
					+ subject;
			ModelRegistry.getAgentDetailModel().logBankAccountEvent(
					TimeSystem.getInstance().getCurrentDate(), to, message);
		}
	}

	// --------

	public static void centralBank_KeyInterestRate(Currency currency,
			double keyInterestRate) {
		ModelRegistry.getKeyInterestRateModel(currency).add(keyInterestRate);
	}

	public static void centralBank_PriceIndex(Currency currency,
			double priceIndex) {
		ModelRegistry.getPriceIndexModel(currency).add(priceIndex);
	}

	// --------

	public static void market_onTick(double pricePerUnit, GoodType goodType,
			Currency currency, double amount) {
		ModelRegistry.getPricesModel(currency).market_onTick(pricePerUnit,
				goodType, currency, amount);
	}

	public static void market_onTick(double pricePerUnit,
			Currency commodityCurrency, Currency currency, double amount) {
		ModelRegistry.getPricesModel(currency).market_onTick(pricePerUnit,
				commodityCurrency, currency, amount);
	}
}
