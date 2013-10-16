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

package compecon.economy;

import compecon.economy.sectors.Agent;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.AgentFactory;
import compecon.engine.Simulation;
import compecon.engine.statistics.Log;
import compecon.engine.util.ConfigurationUtil;

/**
 * This behaviour controls buying decisions.It is injected into an agent (thus
 * compositions instead of inheritance). <br />
 * The key interest rate influences the buying behaviour via a simulated
 * transmission mechanism.
 */
public class BudgetingBehaviour {

	protected final Agent agent;

	protected double lastMaxCredit = Double.NaN;

	public BudgetingBehaviour(Agent agent) {
		this.agent = agent;
	}

	/*
	 * maxCredit defines the maximum additional debt the buyer is going to
	 * accept -> nexus with the monetary sphere
	 */
	public double calculateTransmissionBasedBudgetForPeriod(Currency currency,
			double bankAccountBalance, double referenceCredit) {

		/*
		 * set / adjust reference credit
		 */

		if (Double.isNaN(this.lastMaxCredit)) {
			this.lastMaxCredit = referenceCredit;
		}

		double keyInterestRate = AgentFactory.getInstanceCentralBank(currency)
				.getEffectiveKeyInterestRate();
		double internalRateOfReturn = ConfigurationUtil.BudgetingBehaviour
				.getInternalRateOfReturn();
		double keyInterestRateTransmissionDamper = ConfigurationUtil.BudgetingBehaviour
				.getKeyInterestRateTransmissionDamper();
		lastMaxCredit = lastMaxCredit
				* (1.0 + ((internalRateOfReturn - keyInterestRate) / keyInterestRateTransmissionDamper));

		/*
		 * transmission mechanism
		 */

		double creditBasedBudget = Math.max(0.0, bankAccountBalance
				+ this.lastMaxCredit);

		if (getLog().isAgentSelectedByClient(BudgetingBehaviour.this.agent))
			getLog().log(
					BudgetingBehaviour.this.agent,
					Currency.formatMoneySum(creditBasedBudget) + " "
							+ currency.getIso4217Code() + " budget = ("
							+ Currency.formatMoneySum(bankAccountBalance) + " "
							+ currency.getIso4217Code()
							+ " bankAccountBalance + "
							+ Currency.formatMoneySum(lastMaxCredit) + " "
							+ currency.getIso4217Code() + " maxCredit)");
		return creditBasedBudget;
	}

	public double getCreditBasedBudgetCapacity() {
		return lastMaxCredit;
	}

	private Log getLog() {
		return Simulation.getInstance().getLog();
	}
}
