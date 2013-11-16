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

package compecon.engine.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import compecon.economy.agent.Agent;
import compecon.economy.sectors.financial.CentralBank;
import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.financial.impl.CentralBankImpl;
import compecon.economy.sectors.financial.impl.CreditBankImpl;
import compecon.economy.sectors.household.Household;
import compecon.economy.sectors.household.impl.HouseholdImpl;
import compecon.economy.sectors.industry.Factory;
import compecon.economy.sectors.industry.impl.FactoryImpl;
import compecon.economy.sectors.state.State;
import compecon.economy.sectors.state.impl.StateImpl;
import compecon.economy.sectors.trading.Trader;
import compecon.economy.sectors.trading.impl.TraderImpl;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.service.AgentService;
import compecon.engine.util.HibernateUtil;
import compecon.materia.GoodType;
import compecon.math.intertemporal.impl.ModiglianiIntertemporalConsumptionFunction;
import compecon.math.production.ProductionFunction;

public class AgentServiceImpl implements AgentService {

	public final List<Class<? extends Agent>> agentTypes = new ArrayList<Class<? extends Agent>>();

	public AgentServiceImpl() {
		this.agentTypes.add(HouseholdImpl.class);
		this.agentTypes.add(CreditBankImpl.class);
		this.agentTypes.add(CentralBankImpl.class);
		this.agentTypes.add(StateImpl.class);
		this.agentTypes.add(FactoryImpl.class);
		this.agentTypes.add(TraderImpl.class);
	}

	public List<Class<? extends Agent>> getAgentTypes() {
		return this.agentTypes;
	}

	public State getInstanceState(final Currency currency) {
		State state = ApplicationContext.getInstance().getStateDAO()
				.findByCurrency(currency);
		if (state == null) {
			state = new StateImpl();
			if (!HibernateUtil.isActive())
				state.setId(ApplicationContext.getInstance()
						.getSequenceNumberGenerator().getNextId());

			state.setUtilityFunction(ApplicationContext.getInstance()
					.getInputOutputModel().getUtilityFunctionOfState());

			state.setPrimaryCurrency(currency);
			ApplicationContext.getInstance().getStateDAO().save(state);
			state.initialize();
			HibernateUtil.flushSession();
		}
		return state;
	}

	public CentralBank getInstanceCentralBank(final Currency currency) {
		CentralBank centralBank = ApplicationContext.getInstance()
				.getCentralBankDAO().findByCurrency(currency);
		if (centralBank == null) {
			centralBank = new CentralBankImpl();
			if (!HibernateUtil.isActive())
				centralBank.setId(ApplicationContext.getInstance()
						.getSequenceNumberGenerator().getNextId());
			centralBank.setPrimaryCurrency(currency);
			ApplicationContext.getInstance().getCentralBankDAO()
					.save(centralBank);
			centralBank.initialize();
			HibernateUtil.flushSession();
		}
		return centralBank;
	}

	public CreditBank newInstanceCreditBank(final Currency offeredCurrency) {
		Set<Currency> offeredCurrencies = new HashSet<Currency>();
		offeredCurrencies.add(offeredCurrency);
		return newInstanceCreditBank(offeredCurrencies, offeredCurrency);
	}

	public CreditBank newInstanceCreditBank(
			final Set<Currency> offeredCurrencies,
			final Currency primaryCurrency) {

		assert (offeredCurrencies.contains(primaryCurrency));

		CreditBankImpl creditBank = new CreditBankImpl();
		if (!HibernateUtil.isActive())
			creditBank.setId(ApplicationContext.getInstance()
					.getSequenceNumberGenerator().getNextId());
		creditBank.setPrimaryCurrency(primaryCurrency);
		ApplicationContext.getInstance().getCreditBankDAO().save(creditBank);
		creditBank.initialize();
		HibernateUtil.flushSession();
		return creditBank;
	}

	public CreditBank getRandomInstanceCreditBank(final Currency currency) {
		return ApplicationContext.getInstance().getCreditBankDAO()
				.findRandom(currency);
	}

	public List<CreditBank> getAllCreditBanks(Currency currency) {
		return ApplicationContext.getInstance().getCreditBankDAO()
				.findAllByCurrency(currency);
	}

	public Factory newInstanceFactory(final GoodType goodType,
			final Currency primaryCurrency) {
		FactoryImpl factory = new FactoryImpl();
		if (!HibernateUtil.isActive())
			factory.setId(ApplicationContext.getInstance()
					.getSequenceNumberGenerator().getNextId());
		factory.setProducedGoodType(goodType);
		factory.setPrimaryCurrency(primaryCurrency);
		factory.setReferenceCredit(ApplicationContext.getInstance()
				.getConfiguration().factoryConfig.getReferenceCredit());

		ProductionFunction productionFunction = ApplicationContext
				.getInstance().getInputOutputModel()
				.getProductionFunction(goodType);
		factory.setProductionFunction(productionFunction);

		ApplicationContext.getInstance().getFactoryDAO().save(factory);
		factory.initialize();
		HibernateUtil.flushSession();
		return factory;
	}

	public List<Factory> getAllFactories() {
		return ApplicationContext.getInstance().getFactoryDAO().findAll();
	}

	public Household newInstanceHousehold(final Currency primaryCurrency) {
		Household household = new HouseholdImpl();
		if (!HibernateUtil.isActive())
			household.setId(ApplicationContext.getInstance()
					.getSequenceNumberGenerator().getNextId());
		household.setPrimaryCurrency(primaryCurrency);

		household.setUtilityFunction(ApplicationContext.getInstance()
				.getInputOutputModel().getUtilityFunctionOfHousehold());

		// intertemporal preferences
		/*
		 * Map<Period, Double> intertemeporalPreferences = new HashMap<Period,
		 * Double>(); intertemeporalPreferences.put(Period.CURRENT, 0.5);
		 * intermeporalPreferences.put(Period.NEXT, 0.5);
		 * IntertemporalConsumptionFunction intertemporalConsumptionFunction =
		 * new CobbDouglasIntertemporalConsumptionFunction(
		 * intertemeporalPreferences);
		 */

		household
				.setIntertemporalConsumptionFunction(new ModiglianiIntertemporalConsumptionFunction());

		ApplicationContext.getInstance().getHouseholdDAO().save(household);
		household.initialize();
		HibernateUtil.flushSession();
		return household;
	}

	public Trader newInstanceTrader(final Currency primaryCurrency) {
		TraderImpl trader = new TraderImpl();
		if (!HibernateUtil.isActive())
			trader.setId(ApplicationContext.getInstance()
					.getSequenceNumberGenerator().getNextId());
		trader.setPrimaryCurrency(primaryCurrency);
		trader.setReferenceCredit(ApplicationContext.getInstance()
				.getConfiguration().traderConfig.getReferenceCredit());

		// excluded good types
		trader.getExcludedGoodTypes().add(GoodType.LABOURHOUR);
		for (GoodType goodType : GoodType.values()) {
			if (GoodType.Sector.TERTIARY.equals(goodType.getSector())) {
				trader.getExcludedGoodTypes().add(goodType);
			}
		}

		ApplicationContext.getInstance().getTraderDAO().save(trader);
		trader.initialize();
		HibernateUtil.flushSession();
		return trader;
	}

	public void deleteAgent(final Agent agent) {
		if (agent instanceof Household)
			ApplicationContext.getInstance().getHouseholdDAO()
					.delete((Household) agent);
		else if (agent instanceof State)
			ApplicationContext.getInstance().getStateDAO()
					.delete((State) agent);
		else if (agent instanceof CentralBank)
			ApplicationContext.getInstance().getCentralBankDAO()
					.delete((CentralBank) agent);
		else if (agent instanceof CreditBank)
			ApplicationContext.getInstance().getCreditBankDAO()
					.delete((CreditBank) agent);
		else if (agent instanceof Factory)
			ApplicationContext.getInstance().getFactoryDAO()
					.delete((Factory) agent);
		else if (agent instanceof Trader)
			ApplicationContext.getInstance().getTraderDAO()
					.delete((Trader) agent);
		HibernateUtil.flushSession();
	}
}
