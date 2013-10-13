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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import compecon.economy.sectors.financial.Currency;

public class ConfigurationUtil {

	public static class BudgetingBehaviour {

		public static Double internalRateOfReturn;

		public static Double keyInterestRateTransmissionDamper;

		public static double getInternalRateOfReturn() {
			if (internalRateOfReturn == null)
				internalRateOfReturn = Double
						.parseDouble(configFile
								.getProperty("budgetingBehaviour.internalRateOfReturn"));
			return internalRateOfReturn;
		}

		public static double getKeyInterestRateTransmissionDamper() {
			if (keyInterestRateTransmissionDamper == null)
				keyInterestRateTransmissionDamper = Double
						.parseDouble(configFile
								.getProperty("budgetingBehaviour.keyInterestRateTransmissionDamper"));
			return keyInterestRateTransmissionDamper;
		}
	}

	public static class CentralBankConfig {

		public static Double reserveRatio;

		public static Double inflationTarget;

		public static Double targetPriceIndex;

		public static Boolean allowNegativeKeyInterestRate;

		public static double getReserveRatio() {
			if (reserveRatio == null)
				reserveRatio = Double.parseDouble(configFile
						.getProperty("centralBank.reserveRatio"));
			return reserveRatio;
		}

		public static double getInflationTarget() {
			if (inflationTarget == null)
				inflationTarget = Double.parseDouble(configFile
						.getProperty("centralBank.inflationTarget"));
			return inflationTarget;
		}

		public static double getTargetPriceIndex() {
			if (targetPriceIndex == null)
				targetPriceIndex = Double.parseDouble(configFile
						.getProperty("centralBank.targetPriceIndex"));
			return targetPriceIndex;
		}

		public static boolean getAllowNegativeKeyInterestRate() {
			if (allowNegativeKeyInterestRate == null)
				allowNegativeKeyInterestRate = Boolean
						.parseBoolean(configFile
								.getProperty("centralBank.allowNegativeKeyInterestRate"));
			return allowNegativeKeyInterestRate;
		}
	}

	public static class CreditBankConfig {

		public static Map<Currency, Integer> number = new HashMap<Currency, Integer>();

		public static Double priceChangeIncrement;

		public static Double maxCreditForCurrencyTrading;

		public static Double minArbitrageMargin;

		public static int getNumber(Currency currency) {
			if (!number.containsKey(currency))
				number.put(
						currency,
						Integer.parseInt(configFile.getProperty("creditBank."
								+ currency + ".number")));
			return number.get(currency);
		}

		public static double getPriceChangeIncrement() {
			if (priceChangeIncrement == null)
				priceChangeIncrement = Double.parseDouble(configFile
						.getProperty("creditBank.priceChangeIncrement"));
			return priceChangeIncrement;
		}

		public static double getMaxCreditForCurrencyTrading() {
			if (maxCreditForCurrencyTrading == null)
				maxCreditForCurrencyTrading = Double.parseDouble(configFile
						.getProperty("creditBank.maxCreditForCurrencyTrading"));
			return maxCreditForCurrencyTrading;
		}

		public static double getMinArbitrageMargin() {
			if (minArbitrageMargin == null)
				minArbitrageMargin = Double.parseDouble(configFile
						.getProperty("creditBank.minArbitrageMargin"));
			return minArbitrageMargin;
		}
	}

	public static class DashboardConfig {
		public static Integer logNumberOfAgentsLogSize;

		public static int getLogNumberOfAgentsLogSize() {
			if (logNumberOfAgentsLogSize == null)
				logNumberOfAgentsLogSize = Integer.parseInt(configFile
						.getProperty("dashboard.log.numberOfAgentsLogSize"));
			return logNumberOfAgentsLogSize;
		}
	}

	public static class DbConfig {

		public static Boolean activateDb;

		public static boolean getActivateDb() {
			if (activateDb == null)
				activateDb = Boolean.parseBoolean(configFile
						.getProperty("db.activatedb"));
			return activateDb;
		}
	}

	public static class FactoryConfig {

		public static Double margin;

		public static Map<Currency, Integer> numberPerGoodType = new HashMap<Currency, Integer>();

		public static Double referenceCredit;

		public static double getMargin() {
			if (margin == null)
				margin = Double.parseDouble(configFile
						.getProperty("factory.margin"));
			return margin;
		}

		public static int getNumberPerGoodType(Currency currency) {
			if (!numberPerGoodType.containsKey(currency))
				numberPerGoodType.put(
						currency,
						Integer.parseInt(configFile.getProperty("factory."
								+ currency + ".numberPerGoodType")));
			return numberPerGoodType.get(currency);
		}

		public static double getReferenceCredit() {
			if (referenceCredit == null)
				referenceCredit = Double.parseDouble(configFile
						.getProperty("factory.referenceCredit"));
			return referenceCredit;
		}
	}

	public static class HouseholdConfig {

		public static Map<Currency, Integer> number = new HashMap<Currency, Integer>();

		public static Integer numberOfLabourHoursPerDay;

		public static Integer lifespanInDays;

		public static Integer retirementAgeInDays;

		public static Integer newHouseholdFromAgeInDays;

		public static Integer newHouseholdEveryXDays;

		public static Double requiredUtilityPerDay;

		public static Integer daysWithoutUtilityUntilDestructor;

		public static Double maxPricePerUnitMultiplier;

		public static int getNumber(Currency currency) {
			if (!number.containsKey(currency))
				number.put(
						currency,
						Integer.parseInt(configFile.getProperty("household."
								+ currency + ".number")));
			return number.get(currency);
		}

		public static int getNumberOfLabourHoursPerDay() {
			if (numberOfLabourHoursPerDay == null)
				numberOfLabourHoursPerDay = Integer.parseInt(configFile
						.getProperty("household.numberOfLabourHoursPerDay"));
			return numberOfLabourHoursPerDay;
		}

		public static int getLifespanInDays() {
			if (lifespanInDays == null)
				lifespanInDays = Integer.parseInt(configFile
						.getProperty("household.lifespanInDays"));
			return lifespanInDays;
		}

		public static int getRetirementAgeInDays() {
			if (retirementAgeInDays == null)
				retirementAgeInDays = Integer.parseInt(configFile
						.getProperty("household.retirementAgeInDays"));
			return retirementAgeInDays;
		}

		public static int getNewHouseholdFromAgeInDays() {
			if (newHouseholdFromAgeInDays == null)
				newHouseholdFromAgeInDays = Integer.parseInt(configFile
						.getProperty("household.newHouseholdFromAgeInDays"));
			return newHouseholdFromAgeInDays;
		}

		public static int getNewHouseholdEveryXDays() {
			if (newHouseholdEveryXDays == null)
				newHouseholdEveryXDays = Integer.parseInt(configFile
						.getProperty("household.newHouseholdEveryXDays"));
			return newHouseholdEveryXDays;
		}

		public static double getRequiredUtilityPerDay() {
			if (requiredUtilityPerDay == null)
				requiredUtilityPerDay = Double.parseDouble(configFile
						.getProperty("household.requiredUtilityPerDay"));
			return requiredUtilityPerDay;
		}

		public static int getDaysWithoutUtilityUntilDestructor() {
			if (daysWithoutUtilityUntilDestructor == null)
				daysWithoutUtilityUntilDestructor = Integer
						.parseInt(configFile
								.getProperty("household.daysWithoutUtilityUntilDestructor"));
			return daysWithoutUtilityUntilDestructor;
		}

		public static double getMaxPricePerUnitMultiplier() {
			if (maxPricePerUnitMultiplier == null)
				maxPricePerUnitMultiplier = Double.parseDouble(configFile
						.getProperty("household.maxPricePerUnitMultiplier"));
			return maxPricePerUnitMultiplier;
		}
	}

	public static class MathConfig {

		public static Integer numberOfIterations;

		public static int getNumberOfIterations() {
			if (numberOfIterations == null)
				numberOfIterations = Integer.parseInt(configFile
						.getProperty("math.numberOfIterations"));
			return numberOfIterations;
		}
	}

	public static class PricingBehaviourConfig {

		public static Double defaultPriceChangeIncrement;

		public static Integer defaultNumberOfPrices;

		public static Double defaultInitialPrice;

		public static double getDefaultPriceChangeIncrement() {
			if (defaultPriceChangeIncrement == null)
				defaultPriceChangeIncrement = Double
						.parseDouble(configFile
								.getProperty("pricingBehaviour.defaultPriceChangeIncrement"));
			return defaultPriceChangeIncrement;
		}

		public static int getDefaultNumberOfPrices() {
			if (defaultNumberOfPrices == null)
				defaultNumberOfPrices = Integer.parseInt(configFile
						.getProperty("pricingBehaviour.defaultNumberOfPrices"));
			return defaultNumberOfPrices;
		}

		public static double getDefaultInitialPrice() {
			if (defaultInitialPrice == null)
				defaultInitialPrice = Double.parseDouble(configFile
						.getProperty("pricingBehaviour.defaultInitialPrice"));
			return defaultInitialPrice;
		}
	}

	public static class TimeSystemConfig {

		public static Integer initializationPhaseInDays;

		public static int getInitializationPhaseInDays() {
			if (initializationPhaseInDays == null)
				initializationPhaseInDays = Integer.parseInt(configFile
						.getProperty("timeSystem.initializationPhaseInDays"));
			return initializationPhaseInDays;
		}
	}

	public static class TraderConfig {

		public static Map<Currency, Integer> number = new HashMap<Currency, Integer>();

		public static Double arbitrageMargin;

		public static Double referenceCredit;

		public static int getNumber(Currency currency) {
			if (!number.containsKey(currency))
				number.put(
						currency,
						Integer.parseInt(configFile.getProperty("trader."
								+ currency + ".number")));
			return number.get(currency);
		}

		public static double getArbitrageMargin() {
			if (arbitrageMargin == null)
				arbitrageMargin = Double.parseDouble(configFile
						.getProperty("trader.arbitrageMargin"));
			return arbitrageMargin;
		}

		public static double getReferenceCredit() {
			if (referenceCredit == null)
				referenceCredit = Double.parseDouble(configFile
						.getProperty("trader.referenceCredit"));
			return referenceCredit;
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
