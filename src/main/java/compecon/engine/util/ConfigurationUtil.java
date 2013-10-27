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
import compecon.materia.GoodType;

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

		public static class StatisticalOfficeConfig {
			/**
			 * constraint: sum of weights has to be 1.0
			 */
			public static Map<GoodType, Double> priceIndexWeights = new HashMap<GoodType, Double>();

			public static double getPriceIndexWeight(GoodType goodType) {
				if (!priceIndexWeights.containsKey(goodType)) {
					String priceIndexWeightProperty = configFile
							.getProperty("centralBank.statisticalOffice.priceIndexWeights."
									+ goodType);
					if (priceIndexWeightProperty != null) {
						priceIndexWeights.put(goodType,
								Double.parseDouble(priceIndexWeightProperty));
					} else {
						priceIndexWeights.put(goodType, 0.0);
					}
				}
				return priceIndexWeights.get(goodType);
			}
		}

		public static Double reserveRatio;

		public static Double inflationTarget;

		public static Map<Currency, Integer> number = new HashMap<Currency, Integer>();

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

		public static int getNumber(Currency currency) {
			if (!number.containsKey(currency))
				number.put(
						currency,
						Integer.parseInt(configFile.getProperty("centralBank."
								+ currency + ".number")));
			assert (number.get(currency) == 0 || number.get(currency) == 1);
			return number.get(currency);
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

		public static Map<Currency, Map<GoodType, Integer>> number = new HashMap<Currency, Map<GoodType, Integer>>();

		public static Double referenceCredit;

		static {
			for (Currency currency : Currency.values())
				number.put(currency, new HashMap<GoodType, Integer>());
		}

		public static double getMargin() {
			if (margin == null)
				margin = Double.parseDouble(configFile
						.getProperty("factory.margin"));
			return margin;
		}

		public static int getNumber(Currency currency, GoodType goodType) {
			if (!number.get(currency).containsKey(goodType))
				number.get(currency).put(
						goodType,
						Integer.parseInt(configFile.getProperty("factory."
								+ currency + "." + goodType + ".number")));
			return number.get(currency).get(goodType);
		}

		public static double getReferenceCredit() {
			if (referenceCredit == null)
				referenceCredit = Double.parseDouble(configFile
						.getProperty("factory.referenceCredit"));
			return referenceCredit;
		}
	}

	public static class HouseholdConfig {

		public static Integer daysWithoutUtilityUntilDestructor;

		public static Integer lifespanInDays;

		public static Double maxPricePerUnitMultiplier;

		public static Integer newHouseholdEveryXDays;

		public static Integer newHouseholdFromAgeInDays;

		public static Map<Currency, Integer> number = new HashMap<Currency, Integer>();

		public static Integer numberOfLabourHoursPerDay;

		public static Double requiredUtilityPerDay;

		public static Integer retirementAgeInDays;

		public static Boolean retirementSaving;

		public static int getDaysWithoutUtilityUntilDestructor() {
			if (daysWithoutUtilityUntilDestructor == null)
				daysWithoutUtilityUntilDestructor = Integer
						.parseInt(configFile
								.getProperty("household.daysWithoutUtilityUntilDestructor"));
			return daysWithoutUtilityUntilDestructor;
		}

		public static int getLifespanInDays() {
			if (lifespanInDays == null)
				lifespanInDays = Integer.parseInt(configFile
						.getProperty("household.lifespanInDays"));
			return lifespanInDays;
		}

		public static double getMaxPricePerUnitMultiplier() {
			if (maxPricePerUnitMultiplier == null)
				maxPricePerUnitMultiplier = Double.parseDouble(configFile
						.getProperty("household.maxPricePerUnitMultiplier"));
			return maxPricePerUnitMultiplier;
		}

		public static int getNewHouseholdEveryXDays() {
			if (newHouseholdEveryXDays == null)
				newHouseholdEveryXDays = Integer.parseInt(configFile
						.getProperty("household.newHouseholdEveryXDays"));
			return newHouseholdEveryXDays;
		}

		public static int getNewHouseholdFromAgeInDays() {
			if (newHouseholdFromAgeInDays == null)
				newHouseholdFromAgeInDays = Integer.parseInt(configFile
						.getProperty("household.newHouseholdFromAgeInDays"));
			return newHouseholdFromAgeInDays;
		}

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

		public static double getRequiredUtilityPerDay() {
			if (requiredUtilityPerDay == null)
				requiredUtilityPerDay = Double.parseDouble(configFile
						.getProperty("household.requiredUtilityPerDay"));
			return requiredUtilityPerDay;
		}

		public static int getRetirementAgeInDays() {
			if (retirementAgeInDays == null)
				retirementAgeInDays = Integer.parseInt(configFile
						.getProperty("household.retirementAgeInDays"));
			return retirementAgeInDays;
		}

		public static boolean getRetirementSaving() {
			if (retirementSaving == null)
				retirementSaving = Boolean.parseBoolean(configFile
						.getProperty("household.retirementSaving"));
			return retirementSaving;
		}
	}

	public static class InputOutputModelConfig {

		public enum InputOutputModelConfigSetting {
			InputOutputModelMinimal, InputOutputModelSegmented, InputOutputModelInterdependencies
		}

		public static InputOutputModelConfigSetting inputOutputModelSetup;

		public static InputOutputModelConfigSetting getInputOutputModelSetup() {
			if (inputOutputModelSetup == null)
				inputOutputModelSetup = InputOutputModelConfigSetting
						.valueOf(configFile
								.getProperty("inputOutputModel.setup"));
			assert (inputOutputModelSetup != null);
			return inputOutputModelSetup;
		}
	}

	public static class MathConfig {

		public static Double initializationValueForInputFactorsNonZero;

		public static Integer numberOfIterations;

		public static double getInitializationValueForInputFactorsNonZero() {
			if (initializationValueForInputFactorsNonZero == null)
				initializationValueForInputFactorsNonZero = Double
						.parseDouble(configFile
								.getProperty("math.initializationValueForInputFactorsNonZero"));
			return initializationValueForInputFactorsNonZero;
		}

		public static int getNumberOfIterations() {
			if (numberOfIterations == null)
				numberOfIterations = Integer.parseInt(configFile
						.getProperty("math.numberOfIterations"));
			return numberOfIterations;
		}
	}

	public static class PricingBehaviourConfig {

		public static Double defaultPriceChangeIncrementExplicit;

		public static Double defaultPriceChangeIncrementImplicit;

		public static Integer defaultNumberOfPrices;

		public static Double defaultInitialPrice;

		public static double getDefaultPriceChangeIncrementExplicit() {
			if (defaultPriceChangeIncrementExplicit == null)
				defaultPriceChangeIncrementExplicit = Double
						.parseDouble(configFile
								.getProperty("pricingBehaviour.defaultPriceChangeIncrementExplicit"));
			return defaultPriceChangeIncrementExplicit;
		}

		public static double getDefaultPriceChangeIncrementImplicit() {
			if (defaultPriceChangeIncrementImplicit == null)
				defaultPriceChangeIncrementImplicit = Double
						.parseDouble(configFile
								.getProperty("pricingBehaviour.defaultPriceChangeIncrementImplicit"));
			return defaultPriceChangeIncrementImplicit;
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

	public static class StateConfig {
		public static Map<Currency, Integer> number = new HashMap<Currency, Integer>();

		public static int getNumber(Currency currency) {
			if (!number.containsKey(currency))
				number.put(
						currency,
						Integer.parseInt(configFile.getProperty("state."
								+ currency + ".number")));
			assert (number.get(currency) == 0 || number.get(currency) == 1);
			return number.get(currency);
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
			String configurationProperties = System
					.getProperty("configuration.properties");
			if (configurationProperties == null
					|| configurationProperties.isEmpty()) {
				// if no configuration properties are set via VM args use
				// default configuration properties
				configurationProperties = "configuration.properties";
			}
			configFile.load(ConfigurationUtil.class.getClassLoader()
					.getResourceAsStream(configurationProperties));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
