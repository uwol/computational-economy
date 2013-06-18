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
import compecon.engine.jmx.Log;
import compecon.engine.util.MathUtil;

/**
 * This behaviour controls pricing decisions. It is injected into an agent (thus
 * compositions instead of inheritance).
 */
public class PricingBehaviour {

	protected final Agent agent;

	protected final double initialPrice;

	protected final Currency denominatedInCurrency;

	protected final Object offeredGoodOrCurrency;

	protected boolean periodDataInitialized = false;

	protected final double initialPriceChangeIncrement;

	protected double priceChangeIncrement;

	// the decision

	double[] prices_InPeriods = new double[10]; // x, x-1, x-2, x-3, ...

	// and the results of this decision

	double[] soldAmount_InPeriods = new double[10]; // x, x-1, x-2, x-3, ...

	double[] offeredAmount_InPeriods = new double[10]; // x, x-1, x-2, x-3,
														// ...

	public PricingBehaviour(Agent agent, Object offeredGoodOrCurrency,
			Currency denominatedInCurrency, double initialPrice,
			double initialPriceChangeIncrement) {
		this.agent = agent;
		this.initialPrice = initialPrice;
		this.denominatedInCurrency = denominatedInCurrency;
		this.offeredGoodOrCurrency = offeredGoodOrCurrency;
		this.initialPriceChangeIncrement = initialPriceChangeIncrement;
	}

	public PricingBehaviour(Agent agent, Object offeredGoodOrCurrency,
			Currency denominatedInCurrency, double initialPrice) {
		this(agent, offeredGoodOrCurrency, denominatedInCurrency, initialPrice,
				0.1);
	}

	public void assurePeriodDataInitialized() {
		if (!this.periodDataInitialized) {
			if (!Double.isNaN(initialPrice) && !Double.isInfinite(initialPrice))
				this.prices_InPeriods[0] = initialPrice;
			else
				this.prices_InPeriods[0] = 10;

			this.periodDataInitialized = true;
		}
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

	public void nextPeriod() {
		assurePeriodDataInitialized();

		// shift arrays -> a new period x, old period x becomes period x-1
		System.arraycopy(this.prices_InPeriods, 0, this.prices_InPeriods, 1,
				this.prices_InPeriods.length - 1);
		System.arraycopy(this.soldAmount_InPeriods, 0,
				this.soldAmount_InPeriods, 1,
				this.soldAmount_InPeriods.length - 1);
		System.arraycopy(this.offeredAmount_InPeriods, 0,
				this.offeredAmount_InPeriods, 1,
				this.offeredAmount_InPeriods.length - 1);

		// copy price from last period to current period; important for having a
		// non-zero price every period
		this.prices_InPeriods[0] = this.prices_InPeriods[1];
		this.offeredAmount_InPeriods[0] = 0;
		this.soldAmount_InPeriods[0] = 0;
	}

	public boolean wasNothingSoldInLastPeriod() {
		return this.offeredAmount_InPeriods[1] > 0
				&& MathUtil.equal(this.soldAmount_InPeriods[1], 0);
	}

	public double setNewPrice() {
		this.prices_InPeriods[0] = calculateNewPrice();
		return this.prices_InPeriods[0];
	}

	protected double calculateNewPrice() {
		double oldPrice = this.prices_InPeriods[1];

		String prefix = "offered " + MathUtil.round(offeredAmount_InPeriods[1])
				+ " units of " + offeredGoodOrCurrency + " for "
				+ Currency.round(this.prices_InPeriods[1]) + " "
				+ this.denominatedInCurrency.getIso4217Code()
				+ " per unit and sold "
				+ MathUtil.round(soldAmount_InPeriods[1]) + " units -> ";

		// nothing sold?
		if (MathUtil.greater(this.offeredAmount_InPeriods[1], 0)
				&& MathUtil.lesserEqual(this.soldAmount_InPeriods[1], 0)) {
			double newPrice = calculateLowerPrice(oldPrice);
			if (Log.isAgentSelectedByClient(this.agent))
				Log.log(this.agent,
						prefix + "sold nothing -> lowering price to "
								+ Currency.round(newPrice) + " "
								+ this.denominatedInCurrency.getIso4217Code());
			return newPrice;
		}

		// everything sold?
		if (MathUtil.greater(this.offeredAmount_InPeriods[1], 0)
				&& MathUtil.equal(this.soldAmount_InPeriods[1],
						this.offeredAmount_InPeriods[1])) {
			double newPrice = calculateHigherPrice(oldPrice);
			if (Log.isAgentSelectedByClient(this.agent))
				Log.log(this.agent,
						prefix + "sold everything -> raising price to "
								+ Currency.round(newPrice) + " "
								+ this.denominatedInCurrency.getIso4217Code());
			return newPrice;
		}

		// sold less?
		if (MathUtil.greater(this.offeredAmount_InPeriods[1], 0)
				&& MathUtil.greater(this.soldAmount_InPeriods[2], 0)
				&& MathUtil.lesser(this.soldAmount_InPeriods[1],
						this.soldAmount_InPeriods[2])
				&& (MathUtil.greaterEqual(this.offeredAmount_InPeriods[1],
						this.soldAmount_InPeriods[2]))) {
			double newPrice = calculateLowerPrice(oldPrice);
			if (Log.isAgentSelectedByClient(this.agent))
				Log.log(this.agent, prefix + "sold less (before: "
						+ this.soldAmount_InPeriods[2]
						+ ")-> lowering price to " + Currency.round(newPrice)
						+ " " + this.denominatedInCurrency.getIso4217Code());
			return newPrice;
		}

		// sold more?
		if (MathUtil.greater(this.offeredAmount_InPeriods[1], 0)
				&& MathUtil.greater(this.soldAmount_InPeriods[2], 0)
				&& MathUtil.greater(this.soldAmount_InPeriods[1],
						this.soldAmount_InPeriods[2])
				&& (MathUtil.greaterEqual(this.offeredAmount_InPeriods[2],
						this.soldAmount_InPeriods[1]))) {
			double newPrice = calculateHigherPrice(oldPrice);
			if (Log.isAgentSelectedByClient(this.agent))
				Log.log(this.agent, prefix + "sold more (before: "
						+ this.soldAmount_InPeriods[2]
						+ ")-> raising price to " + Currency.round(newPrice)
						+ " " + this.denominatedInCurrency.getIso4217Code());
			return newPrice;
		}

		if (Log.isAgentSelectedByClient(this.agent))
			Log.log(this.agent,
					prefix + " newPrice := oldPrice = "
							+ Currency.round(oldPrice) + " "
							+ this.denominatedInCurrency.getIso4217Code());
		return oldPrice;

	}

	public double getCurrentPrice() {
		return this.prices_InPeriods[0];
	}

	protected double calculateHigherPrice(double price) {
		// calculate price change increment
		if (MathUtil
				.greater(this.prices_InPeriods[1], this.prices_InPeriods[2])) {
			this.priceChangeIncrement = this.priceChangeIncrement * 1.3;
		} else {
			this.priceChangeIncrement = this.initialPriceChangeIncrement;
		}

		// if the price is 0, multiplication does not work -> reset price
		if (MathUtil.lesserEqual(price, 0))
			return 0.1;
		/**
		 * alternative 1.2: would overcompensate
		 * {@link #calculateLowerPrice(double)} -> prices drift slightly upwards
		 * -> deflation is more unlikely
		 */
		return price * (1 + this.priceChangeIncrement);
	}

	/*
	 * {@link #calculateHigherPrice(double)}
	 */
	protected double calculateLowerPrice(double price) {
		// calculate price change decrement
		if (MathUtil.lesser(this.prices_InPeriods[1], this.prices_InPeriods[2])) {
			// slight asymmetry downwards
			this.priceChangeIncrement = this.priceChangeIncrement * 1.4;
		} else {
			this.priceChangeIncrement = this.initialPriceChangeIncrement;
		}

		return price / (1 + this.priceChangeIncrement);
	}
}
