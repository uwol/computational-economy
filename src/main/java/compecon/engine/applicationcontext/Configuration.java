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

package compecon.engine.applicationcontext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import compecon.economy.materia.GoodType;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.timesystem.impl.HourType;

public class Configuration {

	public final AgentConfig agentConfig = new AgentConfig();

	public final BudgetingBehaviourConfig budgetingBehaviourConfig = new BudgetingBehaviourConfig();
	public final CentralBankConfig centralBankConfig = new CentralBankConfig();
	public final CreditBankConfig creditBankConfig = new CreditBankConfig();
	public final DashboardConfig dashboardConfig = new DashboardConfig();
	public final FactoryConfig factoryConfig = new FactoryConfig();
	public final HouseholdConfig householdConfig = new HouseholdConfig();
	public final InputOutputModelConfig inputOutputModelConfig = new InputOutputModelConfig();
	public final JointStockCompanyConfig jointStockCompanyConfig = new JointStockCompanyConfig();
	public final MathConfig mathConfig = new MathConfig();
	public final PricingBehaviourConfig pricingBehaviourConfig = new PricingBehaviourConfig();
	public final StateConfig stateConfig = new StateConfig();
	public final TimeSystemConfig timeSystemConfig = new TimeSystemConfig();
	public final TraderConfig traderConfig = new TraderConfig();

	public class AgentConfig {

		public HourType balanceSheetPublicationHourType;

		public HourType getBalanceSheetPublicationHourType() {
			if (balanceSheetPublicationHourType == null)
				balanceSheetPublicationHourType = HourType.valueOf(configFile
						.getProperty("agent.balanceSheetPublicationHourType"));
			return balanceSheetPublicationHourType;
		}
	}

	public class BudgetingBehaviourConfig {

		public Double internalRateOfReturn;

		public Double keyInterestRateTransmissionDamper;

		public double getInternalRateOfReturn() {
			if (internalRateOfReturn == null)
				internalRateOfReturn = Double
						.parseDouble(configFile
								.getProperty("budgetingBehaviour.internalRateOfReturn"));
			return internalRateOfReturn;
		}

		public double getKeyInterestRateTransmissionDamper() {
			if (keyInterestRateTransmissionDamper == null)
				keyInterestRateTransmissionDamper = Double
						.parseDouble(configFile
								.getProperty("budgetingBehaviour.keyInterestRateTransmissionDamper"));
			return keyInterestRateTransmissionDamper;
		}
	}

	public class CentralBankConfig {

		public class StatisticalOfficeConfig {
			/**
			 * constraint: sum of weights has to be 1.0
			 */
			public Map<GoodType, Double> priceIndexWeights = new HashMap<GoodType, Double>();

			public double getPriceIndexWeight(GoodType goodType) {
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

		public final StatisticalOfficeConfig statisticalOfficeConfig = new StatisticalOfficeConfig();

		public Double reserveRatio;

		public Double inflationTarget;

		public Map<Currency, Integer> number = new HashMap<Currency, Integer>();

		public Double targetPriceIndex;

		public Boolean allowNegativeKeyInterestRate;

		public double getReserveRatio() {
			if (reserveRatio == null)
				reserveRatio = Double.parseDouble(configFile
						.getProperty("centralBank.reserveRatio"));
			return reserveRatio;
		}

		public double getInflationTarget() {
			if (inflationTarget == null)
				inflationTarget = Double.parseDouble(configFile
						.getProperty("centralBank.inflationTarget"));
			return inflationTarget;
		}

		public int getNumber(Currency currency) {
			if (!number.containsKey(currency))
				number.put(
						currency,
						Integer.parseInt(configFile.getProperty("centralBank."
								+ currency + ".number")));
			assert (number.get(currency) == 0 || number.get(currency) == 1);
			return number.get(currency);
		}

		public double getTargetPriceIndex() {
			if (targetPriceIndex == null)
				targetPriceIndex = Double.parseDouble(configFile
						.getProperty("centralBank.targetPriceIndex"));
			return targetPriceIndex;
		}

		public boolean getAllowNegativeKeyInterestRate() {
			if (allowNegativeKeyInterestRate == null)
				allowNegativeKeyInterestRate = Boolean
						.parseBoolean(configFile
								.getProperty("centralBank.allowNegativeKeyInterestRate"));
			return allowNegativeKeyInterestRate;
		}
	}

	public class CreditBankConfig {

		public Map<Currency, Integer> number = new HashMap<Currency, Integer>();

		public Double priceChangeIncrement;

		public Double maxCreditForCurrencyTrading;

		public Double minArbitrageMargin;

		public int getNumber(Currency currency) {
			if (!number.containsKey(currency))
				number.put(
						currency,
						Integer.parseInt(configFile.getProperty("creditBank."
								+ currency + ".number")));
			return number.get(currency);
		}

		public double getPriceChangeIncrement() {
			if (priceChangeIncrement == null)
				priceChangeIncrement = Double.parseDouble(configFile
						.getProperty("creditBank.priceChangeIncrement"));
			return priceChangeIncrement;
		}

		public double getMaxCreditForCurrencyTrading() {
			if (maxCreditForCurrencyTrading == null)
				maxCreditForCurrencyTrading = Double.parseDouble(configFile
						.getProperty("creditBank.maxCreditForCurrencyTrading"));
			return maxCreditForCurrencyTrading;
		}

		public double getMinArbitrageMargin() {
			if (minArbitrageMargin == null)
				minArbitrageMargin = Double.parseDouble(configFile
						.getProperty("creditBank.minArbitrageMargin"));
			return minArbitrageMargin;
		}
	}

	public class DashboardConfig {
		public Integer logNumberOfAgentsLogSize;

		public int getLogNumberOfAgentsLogSize() {
			if (logNumberOfAgentsLogSize == null)
				logNumberOfAgentsLogSize = Integer.parseInt(configFile
						.getProperty("dashboard.log.numberOfAgentsLogSize"));
			return logNumberOfAgentsLogSize;
		}
	}

	public class FactoryConfig {

		public Double margin;

		public Map<Currency, Map<GoodType, Integer>> number = new HashMap<Currency, Map<GoodType, Integer>>();

		public Double referenceCredit;

		{
			for (Currency currency : Currency.values())
				number.put(currency, new HashMap<GoodType, Integer>());
		}

		public double getMargin() {
			if (margin == null)
				margin = Double.parseDouble(configFile
						.getProperty("factory.margin"));
			return margin;
		}

		public int getNumber(Currency currency, GoodType goodType) {
			if (!number.get(currency).containsKey(goodType))
				number.get(currency).put(
						goodType,
						Integer.parseInt(configFile.getProperty("factory."
								+ currency + "." + goodType + ".number")));
			return number.get(currency).get(goodType);
		}

		public double getReferenceCredit() {
			if (referenceCredit == null)
				referenceCredit = Double.parseDouble(configFile
						.getProperty("factory.referenceCredit"));
			return referenceCredit;
		}
	}

	public class HouseholdConfig {

		public Integer daysWithoutUtilityUntilDestructor;

		public Integer lifespanInDays;

		public Double maxPricePerUnitMultiplier;

		public Integer newHouseholdEveryXDays;

		public Integer newHouseholdFromAgeInDays;

		public Map<Currency, Integer> number = new HashMap<Currency, Integer>();

		public Integer numberOfLabourHoursPerDay;

		public Double requiredUtilityPerDay;

		public Integer retirementAgeInDays;

		public Boolean retirementSaving;

		public int getDaysWithoutUtilityUntilDestructor() {
			if (daysWithoutUtilityUntilDestructor == null)
				daysWithoutUtilityUntilDestructor = Integer
						.parseInt(configFile
								.getProperty("household.daysWithoutUtilityUntilDestructor"));
			return daysWithoutUtilityUntilDestructor;
		}

		public int getLifespanInDays() {
			if (lifespanInDays == null)
				lifespanInDays = Integer.parseInt(configFile
						.getProperty("household.lifespanInDays"));
			return lifespanInDays;
		}

		public double getMaxPricePerUnitMultiplier() {
			if (maxPricePerUnitMultiplier == null)
				maxPricePerUnitMultiplier = Double.parseDouble(configFile
						.getProperty("household.maxPricePerUnitMultiplier"));
			return maxPricePerUnitMultiplier;
		}

		public int getNewHouseholdEveryXDays() {
			if (newHouseholdEveryXDays == null)
				newHouseholdEveryXDays = Integer.parseInt(configFile
						.getProperty("household.newHouseholdEveryXDays"));
			return newHouseholdEveryXDays;
		}

		public int getNewHouseholdFromAgeInDays() {
			if (newHouseholdFromAgeInDays == null)
				newHouseholdFromAgeInDays = Integer.parseInt(configFile
						.getProperty("household.newHouseholdFromAgeInDays"));
			return newHouseholdFromAgeInDays;
		}

		public int getNumber(Currency currency) {
			if (!number.containsKey(currency))
				number.put(
						currency,
						Integer.parseInt(configFile.getProperty("household."
								+ currency + ".number")));
			return number.get(currency);
		}

		public int getNumberOfLabourHoursPerDay() {
			if (numberOfLabourHoursPerDay == null)
				numberOfLabourHoursPerDay = Integer.parseInt(configFile
						.getProperty("household.numberOfLabourHoursPerDay"));
			return numberOfLabourHoursPerDay;
		}

		public double getRequiredUtilityPerDay() {
			if (requiredUtilityPerDay == null)
				requiredUtilityPerDay = Double.parseDouble(configFile
						.getProperty("household.requiredUtilityPerDay"));
			return requiredUtilityPerDay;
		}

		public int getRetirementAgeInDays() {
			if (retirementAgeInDays == null)
				retirementAgeInDays = Integer.parseInt(configFile
						.getProperty("household.retirementAgeInDays"));
			return retirementAgeInDays;
		}

		public boolean getRetirementSaving() {
			if (retirementSaving == null)
				retirementSaving = Boolean.parseBoolean(configFile
						.getProperty("household.retirementSaving"));
			return retirementSaving;
		}
	}

	public enum InputOutputModelConfigSetting {
		InputOutputModelMinimal, InputOutputModelSegmented, InputOutputModelInterdependencies
	}

	public class InputOutputModelConfig {

		public InputOutputModelConfigSetting inputOutputModelSetup;

		public InputOutputModelConfigSetting getInputOutputModelSetup() {
			if (inputOutputModelSetup == null)
				inputOutputModelSetup = InputOutputModelConfigSetting
						.valueOf(configFile
								.getProperty("inputOutputModel.setup"));
			assert (inputOutputModelSetup != null);
			return inputOutputModelSetup;
		}
	}

	public class JointStockCompanyConfig {

		public Integer initialNumberOfShares;

		public int getInitialNumberOfShares() {
			if (initialNumberOfShares == null)
				initialNumberOfShares = Integer
						.parseInt(configFile
								.getProperty("jointStockCompany.initialNumberOfShares"));
			return initialNumberOfShares;
		}
	}

	public class MathConfig {

		public Double initializationValueForInputFactorsNonZero;

		public Integer numberOfIterations;

		public double getInitializationValue() {
			if (initializationValueForInputFactorsNonZero == null)
				initializationValueForInputFactorsNonZero = Double
						.parseDouble(configFile
								.getProperty("math.initializationValue"));
			return initializationValueForInputFactorsNonZero;
		}

		public int getNumberOfIterations() {
			if (numberOfIterations == null)
				numberOfIterations = Integer.parseInt(configFile
						.getProperty("math.numberOfIterations"));
			return numberOfIterations;
		}
	}

	public class PricingBehaviourConfig {

		public Double defaultPriceChangeIncrementExplicit;

		public Double defaultPriceChangeIncrementImplicit;

		public Integer defaultNumberOfPrices;

		public Double defaultInitialPrice;

		public double getDefaultPriceChangeIncrementExplicit() {
			if (defaultPriceChangeIncrementExplicit == null)
				defaultPriceChangeIncrementExplicit = Double
						.parseDouble(configFile
								.getProperty("pricingBehaviour.defaultPriceChangeIncrementExplicit"));
			return defaultPriceChangeIncrementExplicit;
		}

		public double getDefaultPriceChangeIncrementImplicit() {
			if (defaultPriceChangeIncrementImplicit == null)
				defaultPriceChangeIncrementImplicit = Double
						.parseDouble(configFile
								.getProperty("pricingBehaviour.defaultPriceChangeIncrementImplicit"));
			return defaultPriceChangeIncrementImplicit;
		}

		public int getDefaultNumberOfPrices() {
			if (defaultNumberOfPrices == null)
				defaultNumberOfPrices = Integer.parseInt(configFile
						.getProperty("pricingBehaviour.defaultNumberOfPrices"));
			return defaultNumberOfPrices;
		}

		public double getDefaultInitialPrice() {
			if (defaultInitialPrice == null)
				defaultInitialPrice = Double.parseDouble(configFile
						.getProperty("pricingBehaviour.defaultInitialPrice"));
			return defaultInitialPrice;
		}
	}

	public class StateConfig {

		public Double bondMargin;

		public Map<Currency, Integer> number = new HashMap<Currency, Integer>();

		public double getBondMargin() {
			if (bondMargin == null)
				bondMargin = Double.parseDouble(configFile
						.getProperty("state.bondMargin"));
			return bondMargin;
		}

		public int getNumber(Currency currency) {
			if (!number.containsKey(currency))
				number.put(
						currency,
						Integer.parseInt(configFile.getProperty("state."
								+ currency + ".number")));
			assert (number.get(currency) == 0 || number.get(currency) == 1);
			return number.get(currency);
		}
	}

	public class TimeSystemConfig {

		public Integer initializationPhaseInDays;

		public int getInitializationPhaseInDays() {
			if (initializationPhaseInDays == null)
				initializationPhaseInDays = Integer.parseInt(configFile
						.getProperty("timeSystem.initializationPhaseInDays"));
			return initializationPhaseInDays;
		}
	}

	public class TraderConfig {

		public Map<Currency, Integer> number = new HashMap<Currency, Integer>();

		public Double arbitrageMargin;

		public Double referenceCredit;

		public int getNumber(Currency currency) {
			if (!number.containsKey(currency))
				number.put(
						currency,
						Integer.parseInt(configFile.getProperty("trader."
								+ currency + ".number")));
			return number.get(currency);
		}

		public double getArbitrageMargin() {
			if (arbitrageMargin == null)
				arbitrageMargin = Double.parseDouble(configFile
						.getProperty("trader.arbitrageMargin"));
			return arbitrageMargin;
		}

		public double getReferenceCredit() {
			if (referenceCredit == null)
				referenceCredit = Double.parseDouble(configFile
						.getProperty("trader.referenceCredit"));
			return referenceCredit;
		}
	}

	protected final Properties configFile = new Properties();

	public Configuration(final String configFilename) {
		try {
			this.configFile.load(Configuration.class.getClassLoader()
					.getResourceAsStream(configFilename));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
