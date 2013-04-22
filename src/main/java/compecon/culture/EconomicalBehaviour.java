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
import compecon.culture.sectors.state.law.property.PropertyRegister;
import compecon.engine.Agent;
import compecon.engine.AgentFactory;
import compecon.engine.Log;
import compecon.engine.MarketFactory;
import compecon.engine.util.MathUtil;
import compecon.nature.materia.GoodType;
import compecon.nature.production.CompositeProductionFunction;

/**
 * This behaviour controls the economical decisions of the agent, it is injected
 * into (thus compositions instead of inheritance).
 */
public class EconomicalBehaviour {

	protected final BudgetingBehaviour budgetingBehaviour;

	protected final ProductionBehaviour productionBehaviour;

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
			CompositeProductionFunction productionFunction, Currency currency) {
		this.agent = agent;
		this.producedGoodType = producedGoodType;
		this.currency = currency;

		this.budgetingBehaviour = new BudgetingBehaviour();
		if (productionFunction != null)
			this.productionBehaviour = new ProductionBehaviour(
					productionFunction);
		else
			this.productionBehaviour = null;
		this.pricingBehaviour = new PricingBehaviour();
	}

	public BudgetingBehaviour getBudgetingBehaviour() {
		return this.budgetingBehaviour;
	}

	public ProductionBehaviour getProductionBehaviour() {
		return this.productionBehaviour;
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
					.getMarginalPrice(
							this.producedGoodType,
							this.agent.getTransactionsBankAccount()
									.getCurrency());
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
	 * This behaviour controls production decisions.
	 */
	public class ProductionBehaviour {

		protected CompositeProductionFunction productionFunction;

		public ProductionBehaviour(
				CompositeProductionFunction productionFunction) {
			this.productionFunction = productionFunction;
		}

		public double calculateProfitableAmountOfLabourHourInput() {
			double maxOutput = this.calculateOptimalOutput();

			// maximum labour hour input
			final double maxLabourHourInputPerProductionCycle = this.productionFunction
					.calculateMaxLabourHourInputPerProductionCycle();
			Log

			.log(agent,
					"maxLabourHourInputPerProductionCycle := "
							+ MathUtil
									.round(maxLabourHourInputPerProductionCycle)
							+ " " + GoodType.LABOURHOUR);

			// check for estimated cost per labour hour
			double pricePerLabourHour = MarketFactory.getInstance(
					EconomicalBehaviour.this.currency).getMarginalPrice(
					GoodType.LABOURHOUR,
					EconomicalBehaviour.this.agent.getTransactionsBankAccount()
							.getCurrency());
			if (Double.isNaN(pricePerLabourHour)
					|| Double.isInfinite(pricePerLabourHour)) {
				Log

				.log(agent,
						"pricePerLabourHour = "
								+ MathUtil.round(pricePerLabourHour)
								+ " -> numberOfLabourHours := "
								+ MathUtil
										.round(maxLabourHourInputPerProductionCycle)
								+ " " + GoodType.LABOURHOUR);
				return maxLabourHourInputPerProductionCycle;
			}

			// check for estimated revenue per unit
			double estMarginalRevenue = MarketFactory.getInstance(
					EconomicalBehaviour.this.currency).getMarginalPrice(
					producedGoodType,
					EconomicalBehaviour.this.agent.getTransactionsBankAccount()
							.getCurrency());
			if (Double.isNaN(estMarginalRevenue)
					|| Double.isInfinite(estMarginalRevenue)) {
				Log

				.log(agent,
						"deltaEstRevenue = "
								+ MathUtil.round(estMarginalRevenue)
								+ " -> numberOfLabourHours := "
								+ MathUtil
										.round(maxLabourHourInputPerProductionCycle)
								+ " " + GoodType.LABOURHOUR);
				return maxLabourHourInputPerProductionCycle;
			}

			// maximize profit
			int lastProfitableNumberOfLabourHours = 0;
			double lastProfitableMarginalCost = 0;

			for (int numberOfLabourHours = 0; numberOfLabourHours < this.productionFunction
					.calculateMaxLabourHourInputPerProductionCycle(); numberOfLabourHours++) {

				// wanted output can be restricted
				double totalOutput = this.productionFunction
						.calculateOutput(numberOfLabourHours);
				if (maxOutput > 0 && totalOutput > maxOutput) {
					Log

					.log(agent,
							MathUtil.round(totalOutput)
									+ " "
									+ EconomicalBehaviour.this.producedGoodType
									+ "("
									+ MathUtil.round(numberOfLabourHours)
									+ " "
									+ GoodType.LABOURHOUR
									+ ")"
									+ " > "
									+ MathUtil.round(maxOutput)
									+ " maxOutput "
									+ EconomicalBehaviour.this.producedGoodType
									+ " -> "
									+ MathUtil
											.round(lastProfitableNumberOfLabourHours)
									+ " " + GoodType.LABOURHOUR);
					break;
				}

				// marginal output could be 0
				double marginalOutput = this.productionFunction
						.calculateMarginalOutput(numberOfLabourHours);
				if (marginalOutput == 0) {
					Log

					.log(agent,
							MathUtil.round(marginalOutput)
									+ " delta"
									+ EconomicalBehaviour.this.producedGoodType
									+ "("
									+ MathUtil.round(numberOfLabourHours)
									+ " "
									+ GoodType.LABOURHOUR
									+ ")"
									+ " -> "
									+ MathUtil
											.round(lastProfitableNumberOfLabourHours)
									+ " " + GoodType.LABOURHOUR);
					break;
				}

				// revenue of unit has to be greater than marginal costs of unit
				double marginalCost = pricePerLabourHour / marginalOutput;
				if (estMarginalRevenue < marginalCost) {
					Log

					.log(agent,
							MathUtil.round(lastProfitableMarginalCost)
									+ " deltaCost"
									+ "("
									+ MathUtil
											.round(lastProfitableNumberOfLabourHours)
									+ " "
									+ GoodType.LABOURHOUR
									+ ")"
									+ " <= "
									+ MathUtil.round(estMarginalRevenue)
									+ " deltaEstRevenue"
									+ " < "
									+ MathUtil.round(marginalCost)
									+ " deltaCost"
									+ "("
									+ MathUtil.round(numberOfLabourHours)
									+ " "
									+ GoodType.LABOURHOUR
									+ ")"
									+ " -> "
									+ MathUtil
											.round(lastProfitableNumberOfLabourHours)
									+ " " + GoodType.LABOURHOUR);
					break;
				}

				lastProfitableMarginalCost = marginalCost;
				lastProfitableNumberOfLabourHours = numberOfLabourHours;
			}

			return lastProfitableNumberOfLabourHours;
		}

		/**
		 * This calculation is central for market equilibrium, as it restricts
		 * supply -> over supply is avoided -> price deterioration is avoided ->
		 * input factor LABOURHOUR is cost-effective -> households earn money ->
		 * household buy goods -> money circulation is preserved
		 */
		public double calculateOptimalOutput() {
			return calculateOptimalOutputBasedOnInventory();
		}

		protected double calculateOptimalOutputBasedOnInventory() {
			return Math
					.max(0.0,
							((this.productionFunction
									.calculateMaxOutputPerProductionCycle() * 10.0) - PropertyRegister
									.getInstance().getBalance(agent,
											producedGoodType)) / 1.0);

		}

		protected double calculateOptimalOutputBasedOnSalesVolume() {
			if (EconomicalBehaviour.this.wasNothingSoldInLastPeriod())
				return 0.0;
			else
				return this.productionFunction
						.calculateMaxOutputPerProductionCycle();

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
					&& EconomicalBehaviour.this.soldAmount_InPeriods[2] > 0
					&& EconomicalBehaviour.this.soldAmount_InPeriods[1] < EconomicalBehaviour.this.soldAmount_InPeriods[2]
					&& (EconomicalBehaviour.this.offeredAmount_InPeriods[1] > EconomicalBehaviour.this.soldAmount_InPeriods[2] || MathUtil
							.equal(EconomicalBehaviour.this.offeredAmount_InPeriods[1],
									EconomicalBehaviour.this.soldAmount_InPeriods[2]))) {
				Log.log(agent, prefix + "sold less: lowering price");
				return calculateLowerPrice(oldPrice);
			}

			// sold more?
			if (EconomicalBehaviour.this.offeredAmount_InPeriods[1] > 0
					&& EconomicalBehaviour.this.soldAmount_InPeriods[2] > 0
					&& EconomicalBehaviour.this.soldAmount_InPeriods[1] > EconomicalBehaviour.this.soldAmount_InPeriods[2]
					&& (EconomicalBehaviour.this.offeredAmount_InPeriods[2] > EconomicalBehaviour.this.soldAmount_InPeriods[1] || MathUtil
							.equal(EconomicalBehaviour.this.offeredAmount_InPeriods[2],
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

		/**
		 * overcompensates {@link #calculateLowerPrice(double)} -> prices drift
		 * slightly upwards -> deflation is more unlikely
		 */
		protected double calculateHigherPrice(double price) {
			return price * 1.3;
		}

		/**
		 * {@link #calculateHigherPrice(double)}
		 */
		protected double calculateLowerPrice(double price) {
			return price / 1.2;
		}
	}

}
