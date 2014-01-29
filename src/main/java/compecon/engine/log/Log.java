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

package compecon.engine.log;

import java.util.Date;
import java.util.Map;

import compecon.economy.agent.Agent;
import compecon.economy.behaviour.PricingBehaviour.PricingBehaviourNewPriceDecisionCause;
import compecon.economy.bookkeeping.impl.BalanceSheetDTO;
import compecon.economy.markets.MarketParticipant;
import compecon.economy.materia.GoodType;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.BankCustomer;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.economy.sectors.industry.Factory;
import compecon.economy.sectors.state.State;
import compecon.engine.timesystem.TimeSystemEvent;
import compecon.math.ConvexFunction.ConvexFunctionTerminationCause;
import compecon.math.production.ConvexProductionFunction.ConvexProductionFunctionTerminationCause;

public interface Log {

	public boolean isAgentSelectedByClient(final Agent agent);

	public boolean isAgentSelectedByClient(final BankCustomer bankCustomer);

	public boolean isAgentSelectedByClient(
			final MarketParticipant marketParticipant);

	public Agent getAgentSelectedByClient();

	public void setAgentSelectedByClient(final Agent agent);

	public void setAgentCurrentlyActive(final Agent agent);

	public void notifyTimeSystem_nextDay(final Date date);

	public void notifyTimeSystem_nextHour(final Date date);

	public void log(final Agent agent, final String message);

	public void log(final BankCustomer bankCustomer, final String message);

	public void log(final MarketParticipant marketParticipant,
			final String message);

	public void log(final Agent agent,
			final Class<? extends TimeSystemEvent> eventClass,
			final String message);

	public void log(final String message);

	public void agent_onConstruct(final Agent agent);

	public void agent_onDeconstruct(final Agent agent);

	public void agent_onLifesign(final Agent agent);

	public void agent_onPublishBalanceSheet(final Agent agent,
			final BalanceSheetDTO balanceSheet);

	public void agent_CreditUtilization(final Agent agent,
			final double creditUtilization, final double creditCapacity);

	public void agent_onCalculateOutputMaximizingInputsIterative(
			final double budget, final double moneySpent,
			final ConvexFunctionTerminationCause terminationCause);

	public void household_onIncomeWageDividendTransfersConsumptionSaving(
			final Currency currency, final double income,
			final double consumptionAmount, final double savingAmount,
			final double wage, final double dividend,
			final double governmentTransfers);

	public void household_onUtility(final Household household,
			final Currency currency,
			final Map<GoodType, Double> bundleOfGoodsToConsume,
			final double utility);

	public void household_onRetired(final Household household);

	public void household_AmountSold(final Currency currency,
			final double labourHoursSold);

	public void household_onOfferResult(final Currency currency,
			final double labourHoursOffered, final double labourHourCapacity);

	public void factory_onProduction(final Factory factory,
			final Currency currency, final GoodType outputGoodType,
			final double output, final Map<GoodType, Double> inputs);

	public void factory_AmountSold(final Currency currency,
			final GoodType outputGoodType, final double amountSold);

	public void factory_onOfferGoodType(final Currency currency,
			final GoodType outputGoodType, final double amountOffered,
			final double inventory);

	public void factory_onCalculateProfitMaximizingProductionFactorsIterative(
			final double budget, final double moneySpent,
			final ConvexProductionFunctionTerminationCause terminationCause);

	public void factory_onCapitalDepreciation(final Factory factory,
			final GoodType capital, final double depreciation);

	public void state_onUtility(final State state, final Currency currency,
			final Map<GoodType, Double> bundleOfGoodsToConsume,
			final double utility);

	public void bank_onTransfer(final BankAccount from, final BankAccount to,
			final Currency currency, final double value, final String subject);

	public void centralBank_KeyInterestRate(final Currency currency,
			final double keyInterestRate);

	public void centralBank_PriceIndex(final Currency currency,
			final double priceIndex);

	public void market_onTick(final double pricePerUnit,
			final GoodType goodType, final Currency currency,
			final double amount);

	public void market_onTick(final double pricePerUnit,
			final Currency commodityCurrency, final Currency currency,
			final double amount);

	public void pricingBehaviour_onCalculateNewPrice(final Agent agent,
			final PricingBehaviourNewPriceDecisionCause decisionCause,
			final double weight);
}
