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

package compecon.economy.behaviour.impl;

import compecon.economy.agent.impl.AgentImpl;
import compecon.economy.behaviour.PricingBehaviour;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.log.Log;
import compecon.math.util.MathUtil;

/**
 * This behaviour controls pricing decisions. It is injected into an agent (thus
 * compositions instead of inheritance).
 */
public class PricingBehaviourImpl implements PricingBehaviour {

	protected final AgentImpl agent;

	protected final Currency denominatedInCurrency;

	protected final double initialPrice;

	protected final double initialPriceChangeIncrement;

	double[] offeredAmount_InPeriods = new double[10]; // x, x-1, x-2, x-3,
														// ...

	protected final Object offeredObject;

	protected boolean periodDataInitialized = false;

	// the decision

	protected double priceChangeIncrement;

	// and the results of this decision

	double[] prices_InPeriods = new double[10]; // x, x-1, x-2, x-3, ...

	double[] soldAmount_InPeriods = new double[10]; // x, x-1, x-2, x-3, ...

	double[] soldValue_InPeriods = new double[10]; // x, x-1, x-2, x-3, ...

	public PricingBehaviourImpl(final AgentImpl agent,
			final Object offeredObject, final Currency denominatedInCurrency,
			final double initialPrice) {
		this(
				agent,
				offeredObject,
				denominatedInCurrency,
				initialPrice,
				ApplicationContext.getInstance().getConfiguration().pricingBehaviourConfig
						.getDefaultPriceChangeIncrementExplicit());
	}

	public PricingBehaviourImpl(final AgentImpl agent,
			final Object offeredObject, final Currency denominatedInCurrency,
			final double initialPrice, final double priceChangeIncrement) {
		this.agent = agent;
		this.initialPrice = initialPrice;
		this.denominatedInCurrency = denominatedInCurrency;
		this.offeredObject = offeredObject;
		initialPriceChangeIncrement = priceChangeIncrement;
	}

	public void assurePeriodDataInitialized() {
		if (!periodDataInitialized) {
			if (!Double.isNaN(initialPrice) && !Double.isInfinite(initialPrice)) {
				prices_InPeriods[0] = initialPrice;
			} else {
				prices_InPeriods[0] = ApplicationContext.getInstance()
						.getConfiguration().pricingBehaviourConfig
						.getDefaultInitialPrice();
			}

			periodDataInitialized = true;
		}
	}

	protected double calculateHigherPriceExplicit(final double price) {
		updatePriceChangeIncrement(true);
		// if the price is 0.0, multiplication does not work -> reset price
		if (MathUtil.lesserEqual(price, 0.0)) {
			return 0.0001;
		}
		return price * (1.0 + priceChangeIncrement);
	}

	protected double calculateHigherPriceImplicit(final double price) {
		return price
				* (1.0 + ApplicationContext.getInstance().getConfiguration().pricingBehaviourConfig
						.getDefaultPriceChangeIncrementImplicit());
	}

	/*
	 * {@link #calculateHigherPrice(double)}
	 */
	protected double calculateLowerPriceExplicit(final double price) {
		updatePriceChangeIncrement(false);
		return price / (1.0 + priceChangeIncrement);
	}

	protected double calculateNewPrice() {
		final double oldPrice = prices_InPeriods[1];

		final String prefix = "offered "
				+ MathUtil.round(offeredAmount_InPeriods[1]) + " units of "
				+ offeredObject + " for "
				+ Currency.formatMoneySum(prices_InPeriods[1]) + " "
				+ denominatedInCurrency.getIso4217Code()
				+ " per unit and sold "
				+ MathUtil.round(soldAmount_InPeriods[1]) + " units -> ";

		final double offeredAmountInLastPeriod = offeredAmount_InPeriods[1];
		final double offeredAmountInPenultimatePeriod = offeredAmount_InPeriods[2];

		final double soldAmountInLastPeriod = soldAmount_InPeriods[1];
		final double soldAmountInPenultimatePeriod = soldAmount_InPeriods[2];

		// nothing sold?
		if (MathUtil.greater(offeredAmountInLastPeriod, 0.0)
				&& MathUtil.lesserEqual(soldAmountInLastPeriod, 0.0)) {
			final double newPrice = calculateLowerPriceExplicit(oldPrice);
			getLog().pricingBehaviour_onCalculateNewPrice(agent,
					PricingBehaviourNewPriceDecisionCause.SOLD_NOTHING,
					-1.0 * priceChangeIncrement);
			if (getLog().isAgentSelectedByClient(agent)) {
				getLog().log(agent,
						"%s sold nothing -> lowering price to %s %s", prefix,
						Currency.formatMoneySum(newPrice),
						denominatedInCurrency);
			}
			return newPrice;
		}

		// everything sold?
		if (MathUtil.greater(offeredAmountInLastPeriod, 0.0)
				&& MathUtil.equal(soldAmountInLastPeriod,
						offeredAmountInLastPeriod)) {
			final double newPrice = calculateHigherPriceExplicit(oldPrice);
			getLog().pricingBehaviour_onCalculateNewPrice(agent,
					PricingBehaviourNewPriceDecisionCause.SOLD_EVERYTHING,
					priceChangeIncrement);
			if (getLog().isAgentSelectedByClient(agent)) {
				getLog().log(agent,
						"%s sold everything -> raising price to %s %s", prefix,
						Currency.formatMoneySum(newPrice),
						denominatedInCurrency);
			}
			return newPrice;
		}

		// sold less?
		if (
		// something was offered last period
		MathUtil.greater(offeredAmountInLastPeriod, 0.0)
		// and something was sold in the penultimate period
				&& MathUtil.greater(soldAmountInPenultimatePeriod, 0.0)
				// and there was sold less in last period than in the
				// penultimate period
				&& MathUtil.lesser(soldAmountInLastPeriod,
						soldAmountInPenultimatePeriod)
				// and there was offered more in last period than sold in
				// penultimate period -> there was a chance in the last period
				// to outperform the sold amount in the penultimate
				// period
				&& MathUtil.greaterEqual(offeredAmountInLastPeriod,
						soldAmountInPenultimatePeriod)) {
			final double newPrice = calculateLowerPriceExplicit(oldPrice);
			getLog().pricingBehaviour_onCalculateNewPrice(agent,
					PricingBehaviourNewPriceDecisionCause.SOLD_LESS,
					-1.0 * priceChangeIncrement);
			if (getLog().isAgentSelectedByClient(agent)) {
				getLog().log(agent,
						"%s sold less (before: %s) -> lowering price to %s %s",
						prefix, MathUtil.round(soldAmountInPenultimatePeriod),
						Currency.formatMoneySum(newPrice),
						denominatedInCurrency);
			}
			return newPrice;
		}

		// sold more?
		if (
		// something was offered last period
		MathUtil.greater(offeredAmountInLastPeriod, 0.0)
		// and something was sold in the penultimate period
				&& MathUtil.greater(soldAmountInPenultimatePeriod, 0.0)
				// and there was sold more in last period than in the
				// penultimate period
				&& MathUtil.greater(soldAmountInLastPeriod,
						soldAmountInPenultimatePeriod)
				// and there was offered more in the penultimate period than
				// sold in the last period -> there was a chance in the
				// penultimate period to outperform the sold amount in the last
				// period
				&& MathUtil.greaterEqual(offeredAmountInPenultimatePeriod,
						soldAmountInLastPeriod)) {
			final double newPrice = calculateHigherPriceExplicit(oldPrice);
			getLog().pricingBehaviour_onCalculateNewPrice(agent,
					PricingBehaviourNewPriceDecisionCause.SOLD_MORE,
					priceChangeIncrement);
			if (getLog().isAgentSelectedByClient(agent)) {
				getLog().log(agent,
						"%s sold more (before: %s) -> raising price to %s %s",
						prefix, MathUtil.round(soldAmountInPenultimatePeriod),
						Currency.formatMoneySum(newPrice),
						denominatedInCurrency);
			}
			return newPrice;
		}

		if (getLog().isAgentSelectedByClient(agent)) {
			getLog().log(agent, "%s newPrice := oldPrice = %s %s", prefix,
					Currency.formatMoneySum(oldPrice), denominatedInCurrency);
		}
		getLog().pricingBehaviour_onCalculateNewPrice(
				agent,
				PricingBehaviourNewPriceDecisionCause.IMPLICIT_RAISE,
				ApplicationContext.getInstance().getConfiguration().pricingBehaviourConfig
						.getDefaultPriceChangeIncrementImplicit());
		// implicit pricing pressure -> inducing 100% credit utilization
		return calculateHigherPriceImplicit(oldPrice);
	}

	@Override
	public double getCurrentPrice() {
		return prices_InPeriods[0];
	}

	/**
	 * prices encompassing the current price, used for price differentiation.
	 */
	@Override
	public double[] getCurrentPriceArray() {
		final int numberOfPrices = ApplicationContext.getInstance()
				.getConfiguration().pricingBehaviourConfig
				.getDefaultNumberOfPrices();
		assert (numberOfPrices > 0);
		final double[] prices = new double[numberOfPrices];

		if (numberOfPrices == 1) {
			prices[0] = getCurrentPrice();
		} else {
			final double minPrice = Math.max(0, getCurrentPrice()
					- priceChangeIncrement);
			final double maxPrice = getCurrentPrice() + priceChangeIncrement;
			final double maxMinPriceDifference = maxPrice - minPrice;
			final double priceGap = maxMinPriceDifference
					/ (numberOfPrices - 1.0);

			for (int i = 0; i < numberOfPrices; i++) {
				prices[i] = minPrice + priceGap * i;
			}
		}

		assert (getCurrentPrice() >= prices[0] && getCurrentPrice() <= prices[prices.length - 1]);

		return prices;
	}

	@Override
	public double getLastOfferedAmount() {
		return offeredAmount_InPeriods[1];
	}

	@Override
	public double getLastSoldAmount() {
		return soldAmount_InPeriods[1];
	}

	@Override
	public double getLastSoldValue() {
		return soldValue_InPeriods[1];
	}

	private Log getLog() {
		return ApplicationContext.getInstance().getLog();
	}

	@Override
	public void nextPeriod() {
		assurePeriodDataInitialized();

		// shift arrays -> a new period x, old period x becomes period x-1
		System.arraycopy(prices_InPeriods, 0, prices_InPeriods, 1,
				prices_InPeriods.length - 1);
		System.arraycopy(soldAmount_InPeriods, 0, soldAmount_InPeriods, 1,
				soldAmount_InPeriods.length - 1);
		System.arraycopy(soldValue_InPeriods, 0, soldValue_InPeriods, 1,
				soldValue_InPeriods.length - 1);
		System.arraycopy(offeredAmount_InPeriods, 0, offeredAmount_InPeriods,
				1, offeredAmount_InPeriods.length - 1);

		// copy price from last period to current period; important for having a
		// non-zero price every period
		prices_InPeriods[0] = prices_InPeriods[1];
		offeredAmount_InPeriods[0] = 0.0;
		soldAmount_InPeriods[0] = 0.0;
		soldValue_InPeriods[0] = 0.0;

		prices_InPeriods[0] = calculateNewPrice();
	}

	@Override
	public void registerOfferedAmount(final double numberOfProducts) {
		if (!Double.isNaN(numberOfProducts)
				&& !Double.isInfinite(numberOfProducts)) {
			offeredAmount_InPeriods[0] += numberOfProducts;
		}
	}

	@Override
	public void registerSelling(final double numberOfProducts,
			final double totalValue) {
		if (!Double.isNaN(numberOfProducts)
				&& !Double.isInfinite(numberOfProducts)) {
			soldAmount_InPeriods[0] += numberOfProducts;
			soldValue_InPeriods[0] += totalValue;
		}
	}

	protected void updatePriceChangeIncrement(final boolean raisingPrice) {
		if (MathUtil.lesserEqual(priceChangeIncrement, 0.0)) {
			priceChangeIncrement = initialPriceChangeIncrement;
		}

		final double priceInLastPeriod = prices_InPeriods[1];
		final double priceInPenultimatePeriod = prices_InPeriods[2];

		// price will rise after adaption of price increment
		if (raisingPrice) {
			// rising steadily since two periods
			if (MathUtil.greater(priceInLastPeriod, priceInPenultimatePeriod)) {
				priceChangeIncrement = Math.min(initialPriceChangeIncrement,
						priceChangeIncrement * 1.1);
			}
			// oscillating
			else if (MathUtil.lesser(priceInLastPeriod,
					priceInPenultimatePeriod)) {
				priceChangeIncrement = Math.min(initialPriceChangeIncrement,
						priceChangeIncrement / 1.1);
			}
		}
		// price will fall after adaption of price increment
		else {
			// falling steadily since two periods
			if (MathUtil.lesser(priceInLastPeriod, priceInPenultimatePeriod)) {
				priceChangeIncrement = Math.min(initialPriceChangeIncrement,
						priceChangeIncrement * 1.1);
			}
			// oscillating
			else if (MathUtil.greater(priceInLastPeriod,
					priceInPenultimatePeriod)) {
				priceChangeIncrement = Math.min(initialPriceChangeIncrement,
						priceChangeIncrement / 1.1);
			}
		}
	}
}
