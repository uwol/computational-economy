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

package compecon.engine.util;

import java.io.IOException;
import java.util.Properties;

public class ConfigurationUtil {

	public static class BudgetingBehaviour {
		public static double getInternalRateOfReturn() {
			return Double.parseDouble(configFile
					.getProperty("budgetingBehaviour.internalRateOfReturn"));
		}

		public static double getKeyInterestRateTransmissionDamper() {
			return Double
					.parseDouble(configFile
							.getProperty("budgetingBehaviour.keyInterestRateTransmissionDamper"));
		}
	}

	public static class CentralBankConfig {
		public static double getReserveRatio() {
			return Double.parseDouble(configFile
					.getProperty("centralBank.reserveRatio"));
		}

		public static double getInflationTarget() {
			return Double.parseDouble(configFile
					.getProperty("centralBank.inflationTarget"));
		}

		public static double getTargetPriceIndex() {
			return Double.parseDouble(configFile
					.getProperty("centralBank.targetPriceIndex"));
		}
	}

	public static class CreditBankConfig {
		public static int getNumberPerCurrency() {
			return Integer.parseInt(configFile
					.getProperty("creditBank.numberPerCurrency"));
		}

		public static double getPriceChangeIncrement() {
			return Double.parseDouble(configFile
					.getProperty("creditBank.priceChangeIncrement"));
		}

		public static double getMaxCreditForCurrencyTrading() {
			return Double.parseDouble(configFile
					.getProperty("creditBank.maxCreditForCurrencyTrading"));
		}

		public static double getMinArbitrageMargin() {
			return Double.parseDouble(configFile
					.getProperty("creditBank.minArbitrageMargin"));
		}
	}

	public static class DashboardConfig {
		public static int getLogNumberOfAgents() {
			return Integer.parseInt(configFile
					.getProperty("dashboard.log.numberOfAgents"));
		}
	}

	public static class DbConfig {
		public static boolean getActivateDb() {
			return Boolean
					.parseBoolean(configFile.getProperty("db.activatedb"));
		}
	}

	public static class FactoryConfig {
		public static int getNumberPerGoodTypeAndCurrency() {
			return Integer.parseInt(configFile
					.getProperty("factory.numberPerGoodTypeAndCurrency"));
		}

		public static double getReferenceCredit() {
			return Double.parseDouble(configFile
					.getProperty("factory.referenceCredit"));
		}

		public static double getMargin() {
			return Double.parseDouble(configFile.getProperty("factory.margin"));
		}
	}

	public static class HouseholdConfig {
		public static int getNumberPerCurrency() {
			return Integer.parseInt(configFile
					.getProperty("household.numberPerCurrency"));
		}

		public static int getNumberOfLabourHoursPerDay() {
			return Integer.parseInt(configFile
					.getProperty("household.numberOfLabourHoursPerDay"));
		}

		public static int getLifespanInDays() {
			return Integer.parseInt(configFile
					.getProperty("household.lifespanInDays"));
		}

		public static int getRetirementAgeInDays() {
			return Integer.parseInt(configFile
					.getProperty("household.retirementAgeInDays"));
		}

		public static int getNewHouseholdFromAgeInDays() {
			return Integer.parseInt(configFile
					.getProperty("household.newHouseholdFromAgeInDays"));
		}

		public static int getNewHouseholdEveryXDays() {
			return Integer.parseInt(configFile
					.getProperty("household.newHouseholdEveryXDays"));
		}

		public static double getRequiredUtilityPerDay() {
			return Double.parseDouble(configFile
					.getProperty("household.requiredUtilityPerDay"));
		}

		public static int getDaysWithoutUtilityUntilDestructor() {
			return Integer
					.parseInt(configFile
							.getProperty("household.daysWithoutUtilityUntilDestructor"));
		}
	}

	public static class MathConfig {
		public static int getNumberOfIterations() {
			int numberOfIterations = Integer.parseInt(configFile
					.getProperty("math.numberOfIterations"));
			assert (numberOfIterations > 0);
			return numberOfIterations;
		}
	}

	public static class PricingBehaviourConfig {
		public static double getDefaultPriceChangeIncrement() {
			return Double
					.parseDouble(configFile
							.getProperty("pricingBehaviour.defaultPriceChangeIncrement"));
		}

		public static int getDefaultNumberOfPrices() {
			int numberOfPrices = Integer.parseInt(configFile
					.getProperty("pricingBehaviour.defaultNumberOfPrices"));
			assert (numberOfPrices > 0);
			return numberOfPrices;
		}

		public static double getDefaultInitialPrice() {
			return Double.parseDouble(configFile
					.getProperty("pricingBehaviour.defaultInitialPrice"));
		}
	}

	public static class TimeSystemConfig {
		public static int getInitializationPhaseInDays() {
			return Integer.parseInt(configFile
					.getProperty("timeSystem.initializationPhaseInDays"));
		}
	}

	public static class TraderConfig {
		public static int getNumberPerCurrency() {
			return Integer.parseInt(configFile
					.getProperty("trader.numberPerCurrency"));
		}

		public static double getArbitrageMargin() {
			return Double.parseDouble(configFile
					.getProperty("trader.arbitrageMargin"));
		}
	}

	protected static Properties configFile = new Properties();

	static {
		try {
			configFile.load(ConfigurationUtil.class.getClassLoader()
					.getResourceAsStream("configuration.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
