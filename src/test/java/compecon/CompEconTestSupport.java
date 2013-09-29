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

package compecon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Map.Entry;

import compecon.economy.sectors.financial.CentralBank;
import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.economy.sectors.industry.Factory;
import compecon.economy.sectors.trading.Trader;
import compecon.engine.AgentFactory;
import compecon.engine.Simulation;
import compecon.engine.dao.DAOFactory;
import compecon.engine.util.HibernateUtil;
import compecon.materia.GoodType;
import compecon.math.Function;
import compecon.math.price.IPriceFunction;

public abstract class CompEconTestSupport {

	protected final double epsilon = 0.01;

	public void assertOutputIsOptimalUnderBudget(
			final Function<GoodType> function, final double budgetRestriction,
			final Map<GoodType, IPriceFunction> priceFunctions,
			final Map<GoodType, Double> referenceBundleOfInputs) {

		Map<GoodType, Double> rangeScanBundleOfInputs = function
				.calculateOutputMaximizingInputsByRangeScan(priceFunctions,
						budgetRestriction);

		// check that budget restriction is not violated
		double sumOfCostsOfOptimalBundleOfInputs = 0.0;
		for (Entry<GoodType, Double> inputEntry : rangeScanBundleOfInputs
				.entrySet()) {
			sumOfCostsOfOptimalBundleOfInputs += priceFunctions.get(
					inputEntry.getKey()).getPrice(inputEntry.getValue())
					* inputEntry.getValue();
		}

		// optimalBundleOfInputs violates the budget restriction
		assert (sumOfCostsOfOptimalBundleOfInputs <= budgetRestriction);

		// check that budget restriction is not violated
		double sumOfCostsOfReferenceBundleOfInputs = 0.0;
		for (Entry<GoodType, Double> inputEntry : referenceBundleOfInputs
				.entrySet()) {
			sumOfCostsOfReferenceBundleOfInputs += priceFunctions.get(
					inputEntry.getKey()).getPrice(inputEntry.getValue())
					* inputEntry.getValue();
		}

		// referenceBundleOfInputs violates the budget restriction?
		assert (sumOfCostsOfReferenceBundleOfInputs <= budgetRestriction);

		assertTrue(function.f(rangeScanBundleOfInputs) <= function
				.f(referenceBundleOfInputs));
	}

	/**
	 * in an optimum partial derivatives per price have to be identical
	 */
	public void assertPartialDerivativesPerPriceAreEqual(
			final Function<GoodType> function,
			final Map<GoodType, Double> bundleOfInputs,
			final Map<GoodType, IPriceFunction> priceFunctions) {
		Map<GoodType, Double> partialDerivatives = function
				.partialDerivatives(bundleOfInputs);
		for (Entry<GoodType, Double> outerPartialDerivativeEntry : partialDerivatives
				.entrySet()) {
			IPriceFunction outerPriceFunction = priceFunctions
					.get(outerPartialDerivativeEntry.getKey());
			double outerMarginalPrice = outerPriceFunction
					.getMarginalPrice(bundleOfInputs
							.get(outerPartialDerivativeEntry.getKey()));
			if (!Double.isNaN(outerMarginalPrice)) {
				for (Entry<GoodType, Double> innerPartialDerivativeEntry : partialDerivatives
						.entrySet()) {
					IPriceFunction innerPriceFunction = priceFunctions
							.get(innerPartialDerivativeEntry.getKey());
					double innerMarginalPrice = innerPriceFunction
							.getMarginalPrice(bundleOfInputs
									.get(innerPartialDerivativeEntry.getKey()));
					if (!Double.isNaN(innerMarginalPrice)) {
						double innerPartialDerivativePerPrice = innerPartialDerivativeEntry
								.getValue() / innerMarginalPrice;
						double outerPartialDerivativePerPrice = outerPartialDerivativeEntry
								.getValue() / outerMarginalPrice;
						assertEquals(innerPartialDerivativePerPrice,
								outerPartialDerivativePerPrice, epsilon);
					}
				}
			}
		}
	}

	protected void setUp() {
		// init global (non-running) simulation object, so that models, log etc.
		// exist
		new Simulation(false, null);

		// init database connection
		HibernateUtil.openSession();

		for (Currency currency : Currency.values()) {
			AgentFactory.getInstanceCentralBank(currency);
			AgentFactory.newInstanceCreditBank(currency);
			AgentFactory.newInstanceCreditBank(currency);
			AgentFactory.newInstanceFactory(GoodType.WHEAT, currency);
			AgentFactory.newInstanceHousehold(currency);
			AgentFactory.newInstanceHousehold(currency);
			AgentFactory.newInstanceTrader(currency);
		}

		for (CentralBank centralBank : DAOFactory.getCentralBankDAO().findAll()) {
			centralBank.assureTransactionsBankAccount();
		}

		for (CreditBank creditBank : DAOFactory.getCreditBankDAO().findAll()) {
			creditBank.assureCentralBankAccount();
			creditBank.assureTransactionsBankAccount();
			creditBank.assureCurrencyTradeBankAccounts();
		}

		for (Factory factory : DAOFactory.getFactoryDAO().findAll()) {
			factory.assureTransactionsBankAccount();
		}

		for (Household household : DAOFactory.getHouseholdDAO().findAll()) {
			household.assureTransactionsBankAccount();
			household.assureSavingsBankAccount();
		}

		for (Trader trader : DAOFactory.getTraderDAO().findAll()) {
			trader.assureTransactionsBankAccount();
			trader.assureGoodsTradeBankAccounts();
		}

		HibernateUtil.flushSession();
	}

	protected void tearDown() {
		for (Household household : DAOFactory.getHouseholdDAO().findAll()) {
			household.deconstruct();
		}

		for (Trader trader : DAOFactory.getTraderDAO().findAll()) {
			trader.deconstruct();
		}

		for (Factory factory : DAOFactory.getFactoryDAO().findAll()) {
			factory.deconstruct();
		}

		for (CreditBank creditBank : DAOFactory.getCreditBankDAO().findAll()) {
			creditBank.deconstruct();
		}

		for (CentralBank centralBank : DAOFactory.getCentralBankDAO().findAll()) {
			centralBank.deconstruct();
		}

		HibernateUtil.flushSession();

		// close database conenction
		HibernateUtil.closeSession();
	}
}
