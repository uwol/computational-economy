/*
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

package compecon.culture;

import compecon.culture.sectors.financial.Currency;
import compecon.engine.Agent;
import compecon.engine.AgentFactory;
import compecon.engine.Log;
import compecon.engine.MarketFactory;
import compecon.engine.util.MathUtil;
import compecon.nature.materia.GoodType;

/**
 * This behaviour controls the economical decisions of the agent, it is injected
 * into (thus compositions instead of inheritance).
 */
public class EconomicalBehaviour {

	protected final BudgetingBehaviour budgetingBehaviour;

	protected final PricingBehaviour pricingBehaviour;

	protected final GoodType producedGoodType;

	protected final Currency currency;

	protected final Agent agent;

	protected boolean periodDataInitialized = false;

	// the decision

	double[] prices_InPeriods = new double[10]; // x, x-1, x-2, x-3, ...

	// and the results of this decision

	double[] soldAmount_InPeriods = new double[10]; // x, x-1, x-2, x-3, ...

	double[] offeredAmount_InPeriods = new double[10]; // x, x-1, x-2, x-3,
														// ...

	public EconomicalBehaviour(Agent agent, GoodType producedGoodType,
			Currency currency) {
		this.agent = agent;
		this.producedGoodType = producedGoodType;
		this.currency = currency;

		this.budgetingBehaviour = new BudgetingBehaviour();
		this.pricingBehaviour = new PricingBehaviour();
	}

	public BudgetingBehaviour getBudgetingBehaviour() {
		return this.budgetingBehaviour;
	}

	public PricingBehaviour getPricingBehaviour() {
		return this.pricingBehaviour;
	}

	public void registerSelling(double numberOfProducts) {
		if (!Double.isNaN(numberOfProducts)
				&& !Double.isInfinite(numberOfProducts))
			this.soldAmount_InPeriods[0] += numberOfProducts;
	}

	public void registerOfferedAmount(double numberOfProducts) {
		if (!Double.isNaN(numberOfProducts)
				&& !Double.isInfinite(numberOfProducts))
			this.offeredAmount_InPeriods[0] += numberOfProducts;
	}

	public void assertPeriodDataInitialized() {
		if (!this.periodDataInitialized) {
			double marketPrice = MarketFactory.getInstance(this.currency)
					.getMarginalPrice(this.producedGoodType);
			if (!Double.isNaN(marketPrice) && !Double.isInfinite(marketPrice))
				this.prices_InPeriods[0] = marketPrice;
			else
				this.prices_InPeriods[0] = 10;

			this.periodDataInitialized = true;
		}
	}

	public void nextPeriod() {
		assertPeriodDataInitialized();

		// shift arrays -> a new period x, old period x becomes period x-1
		System.arraycopy(this.prices_InPeriods, 0, this.prices_InPeriods, 1,
				this.prices_InPeriods.length - 1);
		System.arraycopy(this.soldAmount_InPeriods, 0,
				this.soldAmount_InPeriods, 1,
				this.soldAmount_InPeriods.length - 1);
		System.arraycopy(this.offeredAmount_InPeriods, 0,
				this.offeredAmount_InPeriods, 1,
				this.offeredAmount_InPeriods.length - 1);

		this.prices_InPeriods[0] = 0;
		this.offeredAmount_InPeriods[0] = 0;
		this.soldAmount_InPeriods[0] = 0;
	}

	public boolean wasNothingSoldInLastPeriod() {
		return EconomicalBehaviour.this.offeredAmount_InPeriods[1] > 0
				&& MathUtil.equal(
						EconomicalBehaviour.this.soldAmount_InPeriods[1], 0);
	}

	/**
	 * This behaviour controls buying decisions. <br />
	 * The key interest rate influences the buying behaviour via a simulated
	 * transmission mechanism.
	 */
	public class BudgetingBehaviour {
		/*
		 * maxCredit defines the maximum additional debt the buyer is going to
		 * accept -> nexus with the monetary sphere
		 */
		public double calculateTransmissionBasedBudgetForPeriod(
				Currency currency, double bankAccountBalance,
				double maxTotalCredit) {

			/*
			 * transmission mechanism
			 */
			// when key interest rate is high -> agents borrow less money
			double transmissionDamper = calculateTransmissionDamper(currency);
			double creditBasedBudget = Math.max(0,
					(bankAccountBalance + maxTotalCredit)
							* (1 - transmissionDamper));
			Log.log(EconomicalBehaviour.this.agent,
					Currency.round(creditBasedBudget) + " "
							+ currency.getIso4217Code() + " budget = ("
							+ Currency.round(bankAccountBalance) + " "
							+ currency.getIso4217Code()
							+ " bankAccountBalance + " + maxTotalCredit + " "
							+ currency.getIso4217Code() + " maxCredit)" + " * "
							+ MathUtil.round(1 - transmissionDamper));
			return creditBasedBudget;
		}

		protected double calculateKeyInterestRateElasticity(Currency currency) {
			return 1 / AgentFactory.getInstanceCentralBank(currency)
					.getMaxEffectiveKeyInterestRate();
		}

		protected double calculateTransmissionDamper(Currency currency) {
			return this.calculateKeyInterestRateElasticity(currency)
					* AgentFactory.getInstanceCentralBank(currency)
							.getEffectiveKeyInterestRate();
		}
	}

	/**
	 * This behaviour controls pricing decisions.
	 */
	public class PricingBehaviour {

		public double setNewPrice() {
			EconomicalBehaviour.this.prices_InPeriods[0] = calculateNewPrice();
			return EconomicalBehaviour.this.prices_InPeriods[0];
		}

		protected double calculateNewPrice() {
			double oldPrice = EconomicalBehaviour.this.prices_InPeriods[1];

			String prefix = "offered "
					+ MathUtil.round(offeredAmount_InPeriods[1])
					+ " units and sold "
					+ MathUtil.round(soldAmount_InPeriods[1]) + " -> ";

			// nothing sold?
			if (EconomicalBehaviour.this.offeredAmount_InPeriods[1] > 0
					&& MathUtil
							.equal(EconomicalBehaviour.this.soldAmount_InPeriods[1],
									0)) {
				Log.log(agent, prefix + "sold nothing: lowering price");
				return calculateLowerPrice(oldPrice);
			}

			// everything sold?
			if (EconomicalBehaviour.this.offeredAmount_InPeriods[1] > 0
					&& MathUtil
							.equal(EconomicalBehaviour.this.soldAmount_InPeriods[1],
									EconomicalBehaviour.this.offeredAmount_InPeriods[1])) {
				Log.log(agent, prefix + "sold everything: raising price");
				return calculateHigherPrice(oldPrice);
			}

			// sold less?
			if (EconomicalBehaviour.this.offeredAmount_InPeriods[1] > 0
					&& MathUtil
							.greater(
									EconomicalBehaviour.this.soldAmount_InPeriods[2],
									0)
					&& MathUtil.lesser(
							EconomicalBehaviour.this.soldAmount_InPeriods[1],
							EconomicalBehaviour.this.soldAmount_InPeriods[2])
					&& (MathUtil
							.greaterEqual(
									EconomicalBehaviour.this.offeredAmount_InPeriods[1],
									EconomicalBehaviour.this.soldAmount_InPeriods[2]))) {
				Log.log(agent, prefix + "sold less: lowering price");
				return calculateLowerPrice(oldPrice);
			}

			// sold more?
			if (EconomicalBehaviour.this.offeredAmount_InPeriods[1] > 0
					&& MathUtil
							.greater(
									EconomicalBehaviour.this.soldAmount_InPeriods[2],
									0)
					&& MathUtil.greater(
							EconomicalBehaviour.this.soldAmount_InPeriods[1],
							EconomicalBehaviour.this.soldAmount_InPeriods[2])
					&& (MathUtil
							.greaterEqual(
									EconomicalBehaviour.this.offeredAmount_InPeriods[2],
									EconomicalBehaviour.this.soldAmount_InPeriods[1]))) {
				Log.log(agent, prefix + "sold more: raising price");
				return calculateHigherPrice(oldPrice);
			}

			Log.log(agent, prefix + " newPrice := oldPrice");
			return oldPrice;

		}

		public double getCurrentPrice() {
			return EconomicalBehaviour.this.prices_InPeriods[0];
		}

		protected double calculateHigherPrice(double price) {
			/**
			 * alternative 1.3: would overcompensate
			 * {@link #calculateLowerPrice(double)} -> prices drift slightly
			 * upwards -> deflation is more unlikely
			 */
			return price * 1.1;
		}

		/*
		 * {@link #calculateHigherPrice(double)}
		 */
		protected double calculateLowerPrice(double price) {
			return price / 1.1;
		}
	}

}
