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
import compecon.engine.jmx.Log;
import compecon.engine.util.ConfigurationUtil;
import compecon.engine.util.MathUtil;

/**
 * This behaviour controls pricing decisions. It is injected into an agent (thus
 * compositions instead of inheritance).
 */
public class PricingBehaviour {

	protected final Agent agent;

	protected final double initialPrice;

	protected final Currency denominatedInCurrency;

	protected final Object offeredObject;

	protected boolean periodDataInitialized = false;

	protected final double initialPriceChangeIncrement;

	protected double priceChangeIncrement;

	// the decision

	double[] prices_InPeriods = new double[10]; // x, x-1, x-2, x-3, ...

	// and the results of this decision

	double[] soldAmount_InPeriods = new double[10]; // x, x-1, x-2, x-3, ...

	double[] soldValue_InPeriods = new double[10]; // x, x-1, x-2, x-3, ...

	double[] offeredAmount_InPeriods = new double[10]; // x, x-1, x-2, x-3,
														// ...

	public PricingBehaviour(Agent agent, Object offeredObject,
			Currency denominatedInCurrency, double initialPrice,
			double priceChangeIncrement) {
		this.agent = agent;
		this.initialPrice = initialPrice;
		this.denominatedInCurrency = denominatedInCurrency;
		this.offeredObject = offeredObject;
		this.initialPriceChangeIncrement = priceChangeIncrement;
	}

	public PricingBehaviour(Agent agent, Object offeredObject,
			Currency denominatedInCurrency, double initialPrice) {
		this(agent, offeredObject, denominatedInCurrency, initialPrice,
				ConfigurationUtil.PricingBehaviourConfig
						.getDefaultPriceChangeIncrement());
	}

	public void assurePeriodDataInitialized() {
		if (!this.periodDataInitialized) {
			if (!Double.isNaN(initialPrice) && !Double.isInfinite(initialPrice))
				this.prices_InPeriods[0] = initialPrice;
			else
				this.prices_InPeriods[0] = ConfigurationUtil.PricingBehaviourConfig
						.getDefaultInitialPrice();

			this.periodDataInitialized = true;
		}
	}

	protected double calculateNewPrice() {
		double oldPrice = this.prices_InPeriods[1];

		String prefix = "offered " + MathUtil.round(offeredAmount_InPeriods[1])
				+ " units of " + offeredObject + " for "
				+ Currency.formatMoneySum(this.prices_InPeriods[1]) + " "
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
								+ Currency.formatMoneySum(newPrice) + " "
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
								+ Currency.formatMoneySum(newPrice) + " "
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
				Log.log(this.agent,
						prefix + "sold less (before: "
								+ MathUtil.round(this.soldAmount_InPeriods[2])
								+ ") -> lowering price to "
								+ Currency.formatMoneySum(newPrice) + " "
								+ this.denominatedInCurrency.getIso4217Code());
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
				Log.log(this.agent,
						prefix + "sold more (before: "
								+ MathUtil.round(this.soldAmount_InPeriods[2])
								+ ") -> raising price to "
								+ Currency.formatMoneySum(newPrice) + " "
								+ this.denominatedInCurrency.getIso4217Code());
			return newPrice;
		}

		if (Log.isAgentSelectedByClient(this.agent))
			Log.log(this.agent,
					prefix + " newPrice := oldPrice = "
							+ Currency.formatMoneySum(oldPrice) + " "
							+ this.denominatedInCurrency.getIso4217Code());
		return oldPrice;
	}

	protected double calculateHigherPrice(double price) {
		updatePriceChangeIncrement(true);
		// if the price is 0, multiplication does not work -> reset price
		if (MathUtil.lesserEqual(price, 0.0))
			return 0.1;
		return price * (1.0 + this.priceChangeIncrement);
	}

	/*
	 * {@link #calculateHigherPrice(double)}
	 */
	protected double calculateLowerPrice(double price) {
		updatePriceChangeIncrement(false);
		return price / (1.0 + this.priceChangeIncrement);
	}

	protected void updatePriceChangeIncrement(boolean raisingPrice) {
		if (MathUtil.lesserEqual(this.priceChangeIncrement, 0.0))
			this.priceChangeIncrement = this.initialPriceChangeIncrement;

		// price will rise after adaption of price increment
		if (raisingPrice) {
			// rising steadily since two periods
			if (MathUtil.greater(this.prices_InPeriods[1],
					this.prices_InPeriods[2])) {
				this.priceChangeIncrement = Math.min(
						this.initialPriceChangeIncrement,
						this.priceChangeIncrement * 1.1);
			}
			// oscillating
			else if (MathUtil.lesser(this.prices_InPeriods[1],
					this.prices_InPeriods[2])) {
				this.priceChangeIncrement = Math.min(
						this.initialPriceChangeIncrement,
						this.priceChangeIncrement / 1.1);
			}
		}
		// price will fall after adaption of price increment
		else {
			// falling steadily since two periods
			if (MathUtil.lesser(this.prices_InPeriods[1],
					this.prices_InPeriods[2])) {
				this.priceChangeIncrement = Math.min(
						this.initialPriceChangeIncrement,
						this.priceChangeIncrement * 1.1);
			}
			// oscillating
			else if (MathUtil.greater(this.prices_InPeriods[1],
					this.prices_InPeriods[2])) {
				this.priceChangeIncrement = Math.min(
						this.initialPriceChangeIncrement,
						this.priceChangeIncrement / 1.1);
			}
		}
	}

	public double getCurrentPrice() {
		return this.prices_InPeriods[0];
	}

	/**
	 * prices encompassing the current price, used for price differentiation.
	 */
	public double[] getCurrentPriceArray() {
		int numberOfPrices = ConfigurationUtil.PricingBehaviourConfig
				.getDefaultNumberOfPrices();
		double[] prices = new double[numberOfPrices];

		if (numberOfPrices == 1) {
			prices[0] = getCurrentPrice();
		} else {
			double minPrice = Math.max(0, getCurrentPrice()
					- this.priceChangeIncrement);
			double maxPrice = getCurrentPrice() + this.priceChangeIncrement;
			double maxMinPriceDifference = maxPrice - minPrice;
			double priceGap = maxMinPriceDifference / (numberOfPrices - 1.0);

			for (int i = 0; i < numberOfPrices; i++) {
				prices[i] = minPrice + priceGap * i;
			}
		}

		if (this.getCurrentPrice() < prices[0]
				|| this.getCurrentPrice() > prices[prices.length - 1])
			throw new RuntimeException("error in calculation");

		return prices;
	}

	public double getLastOfferedAmount() {
		return this.offeredAmount_InPeriods[1];
	}

	public double getLastSoldAmount() {
		return this.soldAmount_InPeriods[1];
	}

	public double getLastSoldValue() {
		return this.soldValue_InPeriods[1];
	}

	public void nextPeriod() {
		assurePeriodDataInitialized();

		// shift arrays -> a new period x, old period x becomes period x-1
		System.arraycopy(this.prices_InPeriods, 0, this.prices_InPeriods, 1,
				this.prices_InPeriods.length - 1);
		System.arraycopy(this.soldAmount_InPeriods, 0,
				this.soldAmount_InPeriods, 1,
				this.soldAmount_InPeriods.length - 1);
		System.arraycopy(this.soldValue_InPeriods, 0, this.soldValue_InPeriods,
				1, this.soldValue_InPeriods.length - 1);
		System.arraycopy(this.offeredAmount_InPeriods, 0,
				this.offeredAmount_InPeriods, 1,
				this.offeredAmount_InPeriods.length - 1);

		// copy price from last period to current period; important for having a
		// non-zero price every period
		this.prices_InPeriods[0] = this.prices_InPeriods[1];
		this.offeredAmount_InPeriods[0] = 0.0;
		this.soldAmount_InPeriods[0] = 0.0;
		this.soldValue_InPeriods[0] = 0.0;

		this.prices_InPeriods[0] = calculateNewPrice();
	}

	public void registerSelling(double numberOfProducts, double totalValue) {
		if (!Double.isNaN(numberOfProducts)
				&& !Double.isInfinite(numberOfProducts)) {
			this.soldAmount_InPeriods[0] += numberOfProducts;
			this.soldValue_InPeriods[0] += totalValue;
		}
	}

	public void registerOfferedAmount(double numberOfProducts) {
		if (!Double.isNaN(numberOfProducts)
				&& !Double.isInfinite(numberOfProducts))
			this.offeredAmount_InPeriods[0] += numberOfProducts;
	}
}
