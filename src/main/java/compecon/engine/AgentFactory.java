/*
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

import compecon.culture.sectors.financial.CentralBank;
import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.household.Household;
import compecon.culture.sectors.industry.Factory;
import compecon.culture.sectors.state.State;
import compecon.culture.sectors.trading.Trader;
import compecon.engine.dao.DAOFactory;
import compecon.engine.util.HibernateUtil;
import compecon.nature.materia.GoodType;
import compecon.nature.materia.InputOutputModel;
import compecon.nature.math.production.IProductionFunction;
import compecon.nature.math.utility.CobbDouglasUtilityFunction;
import compecon.nature.math.utility.IUtilityFunction;

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
			state.setPrimaryCurrency(currency);
			state.initialize();
			DAOFactory.getStateDAO().save(state);
			HibernateUtil.flushSession();
		}
		return state;
	}

	public static CentralBank getInstanceCentralBank(Currency currency) {
		CentralBank centralBank = DAOFactory.getCentralBankDAO()
				.findByCurrency(currency);
		if (centralBank == null) {
			centralBank = new CentralBank();
			centralBank.setPrimaryCurrency(currency);
			centralBank.initialize();
			DAOFactory.getCentralBankDAO().save(centralBank);
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
		creditBank.setOfferedCurrencies(offeredCurrencies);
		creditBank.setPrimaryCurrency(primaryCurrency);
		creditBank.initialize();
		DAOFactory.getCreditBankDAO().save(creditBank);
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
		factory.setProducedGoodType(goodType);
		factory.setPrimaryCurrency(primaryCurrency);

		IProductionFunction productionFunction = InputOutputModel
				.getProductionFunction(goodType);
		factory.setProductionFunction(productionFunction);

		factory.initialize();
		DAOFactory.getFactoryDAO().save(factory);
		HibernateUtil.flushSession();
		return factory;
	}

	public static List<Factory> getAllFactories() {
		return DAOFactory.getFactoryDAO().findAll();
	}

	public static Household newInstanceHousehold(Currency primaryCurrency) {
		Household household = new Household();
		household.setPrimaryCurrency(primaryCurrency);

		// consumption preferences; each GoodType has to be contained here, so
		// that the corresponding price on the market can come to an
		// equilibrium; preference for labour hour has to be high enough, so
		// that labour hour prices do not fall endlessly
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

		household.initialize();
		DAOFactory.getHouseholdDAO().save(household);
		HibernateUtil.flushSession();
		return household;
	}

	public static Trader newInstanceTrader(Currency primaryCurrency) {
		Trader trader = new Trader();
		trader.setPrimaryCurrency(primaryCurrency);

		trader.initialize();
		DAOFactory.getTraderDAO().save(trader);
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
