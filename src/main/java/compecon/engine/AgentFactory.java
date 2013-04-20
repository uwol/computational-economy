package compecon.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import compecon.culture.sectors.agriculture.Farm;
import compecon.culture.sectors.financial.CentralBank;
import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.household.Household;
import compecon.culture.sectors.industry.Factory;
import compecon.culture.sectors.state.State;
import compecon.engine.dao.HibernateDAOFactory;
import compecon.nature.materia.GoodType;

public class AgentFactory {

	protected static Map<Currency, CentralBank> centralBanks = new HashMap<Currency, CentralBank>();

	protected static Map<Currency, State> states = new HashMap<Currency, State>();

	public static State getInstanceState(Currency currency) {
		if (!states.containsKey(currency)) {
			State state = new State();
			state.setLegislatedCurrency(currency);
			state.initialize();
			HibernateDAOFactory.getStateDAO().save(state);
			states.put(currency, state);
		}
		return states.get(currency);
	}

	public static CentralBank getInstanceCentralBank(Currency currency) {
		if (!centralBanks.containsKey(currency)) {
			CentralBank centralBank = new CentralBank();
			centralBank.setCoveredCurrency(currency);
			centralBank.initialize();
			HibernateDAOFactory.getCentralBankDAO().save(centralBank);
			centralBanks.put(currency, centralBank);
		}
		return centralBanks.get(currency);
	}

	public static CreditBank newInstanceCreditBank(
			Set<Currency> offeredCurrencies) {
		CreditBank creditBank = new CreditBank();
		creditBank.setOfferedCurrencies(offeredCurrencies);
		creditBank.initialize();
		HibernateDAOFactory.getCreditBankDAO().save(creditBank);
		return creditBank;
	}

	public static Farm newInstanceFarm() {
		Farm farm = new Farm();
		farm.initialize();
		HibernateDAOFactory.getFarmDAO().save(farm);
		return farm;
	}

	public static Factory newInstanceFactory(GoodType goodType) {
		Factory factory = new Factory();
		factory.setProducedGoodType(goodType);
		factory.initialize();
		HibernateDAOFactory.getFactoryDAO().save(factory);
		return factory;
	}

	public static Household newInstanceHousehold() {
		Household household = new Household();
		household.initialize();
		HibernateDAOFactory.getHouseholdDAO().save(household);
		return household;
	}
}
