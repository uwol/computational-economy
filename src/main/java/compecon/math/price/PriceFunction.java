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

package compecon.math.price;

import compecon.economy.materia.GoodType;

public interface PriceFunction {

	/**
	 * analytical price function parameters in [intervalLeftBoundary,
	 * intervalRightBoundary[ <br />
	 * <br />
	 * p(x) = coefficientXPower0 * x^0 + coefficientXPowerMinus1 + x^-1 <br />
	 * = coefficientXPower0 + coefficientXPowerMinus1 / x <br />
	 * | x in [intervalLeftBoundary, intervalRightBoundary]
	 * 
	 * @see compecon.engine.service.impl.MarketServiceImpl#getAnalyticalPriceFunctionConfigs(Currency,
	 *      GoodType, double)
	 */
	public class PriceFunctionConfig {

		public final double intervalLeftBoundary;

		public final double intervalRightBoundary;

		/**
		 * i. e. pricePerUnit
		 */
		public final double coefficientXPower0;

		/**
		 * i. e. budget spent for preceeding offers - budget needed to buy the
		 * amount offered in those preceeding offers, when buying with
		 * pricePerUnit of current market offer -> negative value of money saved
		 * because preceeding offers were cheaper than the current one in the
		 * price step function <br />
		 * <br />
		 * to be divided by x
		 */
		public final double coefficientXPowerMinus1;

		public PriceFunctionConfig(double intervalLeftBoundary,
				double intervalRightBoundary, double coefficientXPower0,
				double coefficientXPowerMinus1) {
			this.intervalLeftBoundary = intervalLeftBoundary;
			this.intervalRightBoundary = intervalRightBoundary;
			this.coefficientXPower0 = coefficientXPower0;
			this.coefficientXPowerMinus1 = coefficientXPowerMinus1;
		}

		public String toString() {
			return coefficientXPower0 + " + " + coefficientXPowerMinus1
					+ "/x | x in [" + intervalLeftBoundary + ", "
					+ intervalRightBoundary + "]";
		}
	}

	/**
	 * price per unit for numberOfGoods
	 */
	public double getPrice(double numberOfGoods);

	/**
	 * marginal price for numberOfGoods
	 */
	public double getMarginalPrice(double numberOfGoods);

	/**
	 * @return coefficients of polynomial price function; exponent = position in
	 *         array
	 */
	public PriceFunctionConfig[] getAnalyticalPriceFunctionParameters(
			double maxBudget);
}
