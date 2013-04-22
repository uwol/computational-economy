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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import compecon.culture.sectors.agriculture.Farm;
import compecon.culture.sectors.financial.CentralBank;
import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.household.Household;
import compecon.culture.sectors.industry.Factory;
import compecon.culture.sectors.state.State;
import compecon.engine.dao.DAOFactory;
import compecon.engine.util.ConfigurationUtil;
import compecon.nature.materia.GoodType;

public class AgentFactory {

	private static Random random = new Random();

	protected static Map<Currency, State> states = new HashMap<Currency, State>();

	protected static Map<Currency, CentralBank> centralBanks = new HashMap<Currency, CentralBank>();

	protected static Map<Currency, List<CreditBank>> creditBanks = new HashMap<Currency, List<CreditBank>>();

	public static State getInstanceState(Currency currency) {
		if (!states.containsKey(currency)) {
			State state = new State();
			state.setPrimaryCurrency(currency);
			state.initialize();
			DAOFactory.getStateDAO().save(state);
			states.put(currency, state);
		}
		return states.get(currency);
	}

	public static CentralBank getInstanceCentralBank(Currency currency) {
		if (!centralBanks.containsKey(currency)) {
			CentralBank centralBank = new CentralBank();
			centralBank.setPrimaryCurrency(currency);
			centralBank.initialize();
			DAOFactory.getCentralBankDAO().save(centralBank);
			centralBanks.put(currency, centralBank);
		}
		return centralBanks.get(currency);
	}

	public static CreditBank newInstanceCreditBank(
			Set<Currency> offeredCurrencies) {
		CreditBank creditBank = new CreditBank();
		creditBank.setOfferedCurrencies(offeredCurrencies);
		creditBank.setPrimaryCurrency(Currency.EURO);
		creditBank.initialize();
		DAOFactory.getCreditBankDAO().save(creditBank);
		for (Currency currency : offeredCurrencies) {
			if (!creditBanks.containsKey(currency)) {
				creditBanks.put(currency, new ArrayList<CreditBank>());
			}
			creditBanks.get(currency).add(creditBank);
		}
		return creditBank;
	}

	public static CreditBank getRandomInstanceCreditBank(Currency currency) {
		if (ConfigurationUtil.getActivateDb()) {
			return DAOFactory.getCreditBankDAO().findRandom();
		} else
			return creditBanks.get(currency).get(
					random.nextInt(creditBanks.get(currency).size()));
	}

	public static List<CreditBank> getAllCreditBanks(Currency currency) {
		if (ConfigurationUtil.getActivateDb()) {
			return DAOFactory.getCreditBankDAO().findAll();
		} else
			return creditBanks.get(currency);
	}

	public static Farm newInstanceFarm() {
		Farm farm = new Farm();
		farm.setPrimaryCurrency(Currency.EURO);
		farm.initialize();
		DAOFactory.getFarmDAO().save(farm);
		return farm;
	}

	public static Factory newInstanceFactory(GoodType goodType) {
		Factory factory = new Factory();
		factory.setProducedGoodType(goodType);
		factory.setPrimaryCurrency(Currency.EURO);
		factory.initialize();
		DAOFactory.getFactoryDAO().save(factory);
		return factory;
	}

	public static Household newInstanceHousehold() {
		Household household = new Household();
		household.setPrimaryCurrency(Currency.EURO);
		household.initialize();
		DAOFactory.getHouseholdDAO().save(household);
		return household;
	}
}
