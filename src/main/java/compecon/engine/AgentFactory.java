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
import compecon.engine.dao.DAOFactory;
import compecon.nature.materia.GoodType;

public class AgentFactory {

	public static State getInstanceState(Currency currency) {
		State state = DAOFactory.getStateDAO().findByCurrency(currency);
		if (state == null) {
			state = new State();
			state.setPrimaryCurrency(currency);
			state.initialize();
			DAOFactory.getStateDAO().save(state);
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
		}
		return centralBank;
	}

	public static CreditBank newInstanceCreditBank(
			Set<Currency> offeredCurrencies) {
		CreditBank creditBank = new CreditBank();
		creditBank.setOfferedCurrencies(offeredCurrencies);
		creditBank.setPrimaryCurrency(Currency.EURO);
		creditBank.initialize();
		DAOFactory.getCreditBankDAO().save(creditBank);
		return creditBank;
	}

	public static CreditBank getRandomInstanceCreditBank(Currency currency) {
		return DAOFactory.getCreditBankDAO().findRandom();
	}

	public static List<CreditBank> getAllCreditBanks(Currency currency) {
		return DAOFactory.getCreditBankDAO().findAll();
	}

	public static Factory newInstanceFactory(GoodType goodType) {
		Factory factory = new Factory();
		factory.setProducedGoodType(goodType);
		factory.setProductivity(10);
		factory.setPrimaryCurrency(Currency.EURO);
		factory.initialize();
		DAOFactory.getFactoryDAO().save(factory);
		return factory;
	}

	public static Household newInstanceHousehold() {
		Household household = new Household();
		household.setPrimaryCurrency(Currency.EURO);

		// consumption preferences; each GoodType has to be contained here, so
		// that the corresponding price on the market can come to an
		// equilibrium; preference for labour hour has to be high enough, so
		// that labour hour prices do not fall endlessly
		Map<GoodType, Double> preferences = new LinkedHashMap<GoodType, Double>();
		preferences.put(GoodType.LABOURHOUR, 0.2);
		preferences.put(GoodType.MEGACALORIE, 0.3);
		preferences.put(GoodType.KILOWATT, 0.2);
		preferences.put(GoodType.REALESTATE, 0.3);
		household.setPreferences(preferences);

		household.initialize();
		DAOFactory.getHouseholdDAO().save(household);
		return household;
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
	}
}
