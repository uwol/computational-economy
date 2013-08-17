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

package compecon.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import compecon.Simulation;
import compecon.economy.sectors.financial.CentralBank;
import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.economy.sectors.industry.Factory;
import compecon.economy.sectors.state.State;
import compecon.economy.sectors.trading.Trader;
import compecon.engine.dao.DAOFactory;
import compecon.engine.util.ConfigurationUtil;
import compecon.engine.util.HibernateUtil;
import compecon.materia.GoodType;
import compecon.materia.InputOutputModel;
import compecon.math.intertemporal.consumption.IntertemporalConsumptionFunction;
import compecon.math.intertemporal.consumption.ModiglianiIntertemporalConsumptionFunction;
import compecon.math.production.IProductionFunction;
import compecon.math.utility.CobbDouglasUtilityFunction;
import compecon.math.utility.IUtilityFunction;

public class AgentFactory {

	public final static List<Class<? extends Agent>> agentTypes = new ArrayList<Class<? extends Agent>>();

	static {
		agentTypes.add(Household.class);
		agentTypes.add(CreditBank.class);
		agentTypes.add(CentralBank.class);
		agentTypes.add(State.class);
		agentTypes.add(Factory.class);
		agentTypes.add(Trader.class);
	}

	public static State getInstanceState(Currency currency) {
		State state = DAOFactory.getStateDAO().findByCurrency(currency);
		if (state == null) {
			state = new State();
			if (!ConfigurationUtil.DbConfig.getActivateDb())
				state.setId(Simulation.getNextId());

			Map<GoodType, Double> preferences = new LinkedHashMap<GoodType, Double>();
			preferences.put(GoodType.LABOURHOUR, 0.3);
			preferences.put(GoodType.KILOWATT, 0.2);
			preferences.put(GoodType.REALESTATE, 0.2);
			preferences.put(GoodType.GOLD, 0.3);
			IUtilityFunction utilityFunction = new CobbDouglasUtilityFunction(
					preferences, 1);
			state.setUtilityFunction(utilityFunction);

			state.setPrimaryCurrency(currency);
			DAOFactory.getStateDAO().save(state);
			state.initialize();
			HibernateUtil.flushSession();
		}
		return state;
	}

	public static CentralBank getInstanceCentralBank(Currency currency) {
		CentralBank centralBank = DAOFactory.getCentralBankDAO()
				.findByCurrency(currency);
		if (centralBank == null) {
			centralBank = new CentralBank();
			if (!ConfigurationUtil.DbConfig.getActivateDb())
				centralBank.setId(Simulation.getNextId());
			centralBank.setPrimaryCurrency(currency);
			DAOFactory.getCentralBankDAO().save(centralBank);
			centralBank.initialize();
			HibernateUtil.flushSession();
		}
		return centralBank;
	}

	public static CreditBank newInstanceCreditBank(Currency offeredCurrency) {
		Set<Currency> offeredCurrencies = new HashSet<Currency>();
		offeredCurrencies.add(offeredCurrency);
		return newInstanceCreditBank(offeredCurrencies, offeredCurrency);
	}

	public static CreditBank newInstanceCreditBank(
			Set<Currency> offeredCurrencies, Currency primaryCurrency) {
		if (!offeredCurrencies.contains(primaryCurrency))
			throw new RuntimeException("primaryCurrency " + primaryCurrency
					+ " not contained in offeredCurrencies");

		CreditBank creditBank = new CreditBank();
		if (!ConfigurationUtil.DbConfig.getActivateDb())
			creditBank.setId(Simulation.getNextId());
		creditBank.setOfferedCurrencies(offeredCurrencies);
		creditBank.setPrimaryCurrency(primaryCurrency);
		DAOFactory.getCreditBankDAO().save(creditBank);
		creditBank.initialize();
		HibernateUtil.flushSession();
		return creditBank;
	}

	public static CreditBank getRandomInstanceCreditBank(Currency currency) {
		return DAOFactory.getCreditBankDAO().findRandom(currency);
	}

	public static List<CreditBank> getAllCreditBanks(Currency currency) {
		return DAOFactory.getCreditBankDAO().findAllByCurrency(currency);
	}

	public static Factory newInstanceFactory(GoodType goodType,
			Currency primaryCurrency) {
		Factory factory = new Factory();
		if (!ConfigurationUtil.DbConfig.getActivateDb())
			factory.setId(Simulation.getNextId());
		factory.setProducedGoodType(goodType);
		factory.setPrimaryCurrency(primaryCurrency);
		factory.setReferenceCredit(100000);

		IProductionFunction productionFunction = InputOutputModel
				.getProductionFunction(goodType);
		factory.setProductionFunction(productionFunction);

		DAOFactory.getFactoryDAO().save(factory);
		factory.initialize();
		HibernateUtil.flushSession();
		return factory;
	}

	public static List<Factory> getAllFactories() {
		return DAOFactory.getFactoryDAO().findAll();
	}

	public static Household newInstanceHousehold(Currency primaryCurrency) {
		Household household = new Household();
		if (!ConfigurationUtil.DbConfig.getActivateDb())
			household.setId(Simulation.getNextId());
		household.setPrimaryCurrency(primaryCurrency);

		// consumption preferences; each GoodType has to be contained here (at
		// least transitively via the input-output-model), so that the
		// corresponding price on the market
		// can come to an equilibrium; preference for labour hour has to be high
		// enough, so that labour hour prices do not fall endlessly
		Map<GoodType, Double> preferences = new LinkedHashMap<GoodType, Double>();
		preferences.put(GoodType.LABOURHOUR, 0.2);
		preferences.put(GoodType.WHEAT, 0.2);
		preferences.put(GoodType.KILOWATT, 0.1);
		preferences.put(GoodType.CAR, 0.2);
		preferences.put(GoodType.REALESTATE, 0.2);
		preferences.put(GoodType.GOLD, 0.1);
		IUtilityFunction utilityFunction = new CobbDouglasUtilityFunction(
				preferences, 1);
		household.setUtilityFunction(utilityFunction);

		// intertemporal preferences
		/*
		 * Map<Period, Double> intermeporalPreferences = new HashMap<Period,
		 * Double>(); intermeporalPreferences.put(Period.CURRENT, 0.5);
		 * intermeporalPreferences.put(Period.NEXT, 0.5);
		 * IntertemporalConsumptionFunction intertemporalConsumptionFunction =
		 * new CobbDouglasIntertemporalConsumptionFunction(
		 * intermeporalPreferences);
		 */

		IntertemporalConsumptionFunction intertemporalConsumptionFunction = new ModiglianiIntertemporalConsumptionFunction();
		household
				.setIntertemporalConsumptionFunction(intertemporalConsumptionFunction);

		DAOFactory.getHouseholdDAO().save(household);
		household.initialize();
		HibernateUtil.flushSession();
		return household;
	}

	public static Trader newInstanceTrader(Currency primaryCurrency) {
		Trader trader = new Trader();
		if (!ConfigurationUtil.DbConfig.getActivateDb())
			trader.setId(Simulation.getNextId());
		trader.setPrimaryCurrency(primaryCurrency);
		trader.setReferenceCredit(10000);

		// excluded good types
		trader.getExcludedGoodTypes().add(GoodType.LABOURHOUR);

		DAOFactory.getTraderDAO().save(trader);
		trader.initialize();
		HibernateUtil.flushSession();
		return trader;
	}

	public static void deleteAgent(Agent agent) {
		if (agent instanceof Household)
			DAOFactory.getHouseholdDAO().delete((Household) agent);
		else if (agent instanceof State)
			DAOFactory.getStateDAO().delete((State) agent);
		else if (agent instanceof CentralBank)
			DAOFactory.getCentralBankDAO().delete((CentralBank) agent);
		else if (agent instanceof CreditBank)
			DAOFactory.getCreditBankDAO().delete((CreditBank) agent);
		else if (agent instanceof Factory)
			DAOFactory.getFactoryDAO().delete((Factory) agent);
		else if (agent instanceof Trader)
			DAOFactory.getTraderDAO().delete((Trader) agent);
	}
}
