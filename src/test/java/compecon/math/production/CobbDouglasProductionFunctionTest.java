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

package compecon.math.production;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import compecon.CompEconTestSupport;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.engine.MarketFactory;
import compecon.engine.dao.DAOFactory;
import compecon.materia.GoodType;
import compecon.math.price.FixedPriceFunction;
import compecon.math.price.IPriceFunction;

public class CobbDouglasProductionFunctionTest extends CompEconTestSupport {

	final int numberOfIterations = 2000;

	@Before
	public void setUp() {
		super.setUp();
	}

	@After
	public void tearDown() {
		super.tearDown();
	}

	@Test
	public void testCalculateProductionOutputWithFixedPrices() {
		/*
		 * prepare function
		 */
		Map<GoodType, Double> exponents = new HashMap<GoodType, Double>();
		exponents.put(GoodType.KILOWATT, 0.4);
		exponents.put(GoodType.WHEAT, 0.6);
		CobbDouglasProductionFunction cobbDouglasProductionFunction = new CobbDouglasProductionFunction(
				1.0, exponents);

		/*
		 * maximize output under budget restriction
		 */
		Map<GoodType, IPriceFunction> priceFunctions = new HashMap<GoodType, IPriceFunction>();
		priceFunctions.put(GoodType.KILOWATT, new FixedPriceFunction(1.0));
		priceFunctions.put(GoodType.WHEAT, new FixedPriceFunction(2.0));

		double budget = 10.0;

		Map<GoodType, Double> optimalInputsIterative = cobbDouglasProductionFunction
				.calculateProfitMaximizingProductionFactorsIterative(10.0,
						priceFunctions, budget, Double.NaN, numberOfIterations,
						0.0);

		/*
		 * assert inputs
		 */
		assertEquals(4.0, optimalInputsIterative.get(GoodType.KILOWATT),
				epsilon);
		assertEquals(3.0, optimalInputsIterative.get(GoodType.WHEAT), epsilon);

		/*
		 * assert output
		 */
		assertEquals(3.36586,
				cobbDouglasProductionFunction
						.calculateOutput(optimalInputsIterative), epsilon);

		/*
		 * assert marginal outputs
		 */
		assertEquals(0.336586,
				cobbDouglasProductionFunction.calculateMarginalOutput(
						optimalInputsIterative, GoodType.KILOWATT), epsilon);
		assertEquals(0.673173,
				cobbDouglasProductionFunction.calculateMarginalOutput(
						optimalInputsIterative, GoodType.WHEAT), epsilon);
	}

	@Test
	public void testCalculateProductionOutputWithMarketPrices() {
		Currency currency = Currency.EURO;

		Household household1_EUR = DAOFactory.getHouseholdDAO()
				.findAllByCurrency(currency).get(0);
		Household household2_EUR = DAOFactory.getHouseholdDAO()
				.findAllByCurrency(currency).get(1);

		MarketFactory.getInstance().placeSellingOffer(GoodType.KILOWATT,
				household1_EUR, household1_EUR.getTransactionsBankAccount(),
				10, 2);
		MarketFactory.getInstance().placeSellingOffer(GoodType.KILOWATT,
				household2_EUR, household2_EUR.getTransactionsBankAccount(),
				10, 1);
		MarketFactory.getInstance().placeSellingOffer(GoodType.WHEAT,
				household1_EUR, household1_EUR.getTransactionsBankAccount(),
				10, 1);

		/*
		 * prepare function
		 */
		Map<GoodType, Double> exponents = new HashMap<GoodType, Double>();
		exponents.put(GoodType.KILOWATT, 0.4);
		exponents.put(GoodType.WHEAT, 0.6);
		CobbDouglasProductionFunction cobbDouglasProductionFunction = new CobbDouglasProductionFunction(
				1.0, exponents);

		/*
		 * maximize output under budget restriction
		 */
		Map<GoodType, IPriceFunction> priceFunctions = new HashMap<GoodType, IPriceFunction>();
		priceFunctions.put(GoodType.KILOWATT, MarketFactory.getInstance()
				.getMarketPriceFunction(currency, GoodType.KILOWATT));
		priceFunctions.put(GoodType.WHEAT, MarketFactory.getInstance()
				.getMarketPriceFunction(currency, GoodType.WHEAT));

		double budget = 50.0;

		Map<GoodType, Double> optimalInputsIterative = cobbDouglasProductionFunction
				.calculateProfitMaximizingProductionFactorsIterative(10.0,
						priceFunctions, budget, Double.NaN, numberOfIterations,
						0.0);

		/*
		 * assert inputs
		 */
		assertEquals(20.0, optimalInputsIterative.get(GoodType.KILOWATT),
				epsilon * 2.0);
		assertEquals(10.0, optimalInputsIterative.get(GoodType.WHEAT),
				epsilon * 2.0);

		/*
		 * assert output
		 */
		assertEquals(13.2,
				cobbDouglasProductionFunction
						.calculateOutput(optimalInputsIterative), epsilon);

		/*
		 * assert marginal outputs
		 */
		assertEquals(0.264,
				cobbDouglasProductionFunction.calculateMarginalOutput(
						optimalInputsIterative, GoodType.KILOWATT), epsilon);
		assertEquals(0.7914,
				cobbDouglasProductionFunction.calculateMarginalOutput(
						optimalInputsIterative, GoodType.WHEAT), epsilon);
	}
}
