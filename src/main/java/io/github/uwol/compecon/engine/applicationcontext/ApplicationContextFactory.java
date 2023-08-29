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

package io.github.uwol.compecon.engine.applicationcontext;

import java.io.IOException;

import io.github.uwol.compecon.economy.materia.InputOutputModel;
import io.github.uwol.compecon.economy.materia.impl.InputOutputModelInterdependenciesImpl;
import io.github.uwol.compecon.economy.materia.impl.InputOutputModelMinimalImpl;
import io.github.uwol.compecon.economy.materia.impl.InputOutputModelNoDependenciesImpl;
import io.github.uwol.compecon.economy.materia.impl.InputOutputModelTestingImpl;
import io.github.uwol.compecon.engine.dao.inmemory.impl.SequenceNumberGeneratorImpl;
import io.github.uwol.compecon.engine.factory.impl.AgentImplFactoryImpl;
import io.github.uwol.compecon.engine.factory.impl.BankAccountImplFactoryImpl;
import io.github.uwol.compecon.engine.factory.impl.BudgetingBehaviourFactoryImpl;
import io.github.uwol.compecon.engine.factory.impl.CentralBankImplFactoryImpl;
import io.github.uwol.compecon.engine.factory.impl.CreditBankImplFactoryImpl;
import io.github.uwol.compecon.engine.factory.impl.FactoryImplFactoryImpl;
import io.github.uwol.compecon.engine.factory.impl.FixedRateBondImplFactoryImpl;
import io.github.uwol.compecon.engine.factory.impl.GoodTypeOwnershipImplFactoryImpl;
import io.github.uwol.compecon.engine.factory.impl.HouseholdImplFactoryImpl;
import io.github.uwol.compecon.engine.factory.impl.MarketOrderImplFactoryImpl;
import io.github.uwol.compecon.engine.factory.impl.PricingBehaviourFactoryImpl;
import io.github.uwol.compecon.engine.factory.impl.ShareImplFactoryImpl;
import io.github.uwol.compecon.engine.factory.impl.StateImplFactoryImpl;
import io.github.uwol.compecon.engine.factory.impl.TraderImplFactoryImpl;
import io.github.uwol.compecon.engine.log.impl.LogImpl;
import io.github.uwol.compecon.engine.random.impl.DeterministicNumberGeneratorImpl;
import io.github.uwol.compecon.engine.runner.impl.SimulationRunnerImpl;
import io.github.uwol.compecon.engine.service.impl.AgentServiceImpl;
import io.github.uwol.compecon.engine.service.impl.HardCashServiceImpl;
import io.github.uwol.compecon.engine.service.impl.PropertyServiceImpl;
import io.github.uwol.compecon.engine.service.impl.SettlementMarketServiceImpl;
import io.github.uwol.compecon.engine.statistics.ModelRegistry;
import io.github.uwol.compecon.engine.timesystem.impl.TimeSystemImpl;

public class ApplicationContextFactory {

	/**
	 * Configures the application context with in-memory DAOs.
	 */
	public static void configureInMemoryApplicationContext(final String configurationPropertiesFilename)
			throws IOException {

		configureMinimalApplicationContext(configurationPropertiesFilename);

		// in-memory DAOs

		ApplicationContext.getInstance()
				.setBankAccountDAO(new io.github.uwol.compecon.engine.dao.inmemory.impl.BankAccountDAOImpl());
		ApplicationContext.getInstance()
				.setCentralBankDAO(new io.github.uwol.compecon.engine.dao.inmemory.impl.CentralBankDAOImpl());
		ApplicationContext.getInstance()
				.setCreditBankDAO(new io.github.uwol.compecon.engine.dao.inmemory.impl.CreditBankDAOImpl());
		ApplicationContext.getInstance().setGoodTypeOwnershipDAO(
				new io.github.uwol.compecon.engine.dao.inmemory.impl.GoodTypeOwnershipDAOImpl());
		ApplicationContext.getInstance()
				.setHouseholdDAO(new io.github.uwol.compecon.engine.dao.inmemory.impl.HouseholdDAOImpl());
		ApplicationContext.getInstance()
				.setFactoryDAO(new io.github.uwol.compecon.engine.dao.inmemory.impl.FactoryDAOImpl());
		ApplicationContext.getInstance()
				.setMarketOrderDAO(new io.github.uwol.compecon.engine.dao.inmemory.impl.MarketOrderDAOImpl());
		ApplicationContext.getInstance()
				.setPropertyDAO(new io.github.uwol.compecon.engine.dao.inmemory.impl.PropertyDAOImpl());
		ApplicationContext.getInstance()
				.setStateDAO(new io.github.uwol.compecon.engine.dao.inmemory.impl.StateDAOImpl());
		ApplicationContext.getInstance()
				.setTraderDAO(new io.github.uwol.compecon.engine.dao.inmemory.impl.TraderDAOImpl());
	}

	protected static void configureMinimalApplicationContext(final String configurationPropertiesFilename)
			throws IOException {
		// reset application context
		ApplicationContext.getInstance().reset();

		ApplicationContext.getInstance().setRandomNumberGenerator(new DeterministicNumberGeneratorImpl());
		ApplicationContext.getInstance().setSequenceNumberGenerator(new SequenceNumberGeneratorImpl());

		/*
		 * factory classes
		 */
		ApplicationContext.getInstance().setAgentFactory(new AgentImplFactoryImpl());
		ApplicationContext.getInstance().setBankAccountFactory(new BankAccountImplFactoryImpl());
		ApplicationContext.getInstance().setBudgetingBehaviourFactory(new BudgetingBehaviourFactoryImpl());
		ApplicationContext.getInstance().setCentralBankFactory(new CentralBankImplFactoryImpl());
		ApplicationContext.getInstance().setCreditBankFactory(new CreditBankImplFactoryImpl());
		ApplicationContext.getInstance().setFactoryFactory(new FactoryImplFactoryImpl());
		ApplicationContext.getInstance().setFixedRateBondFactory(new FixedRateBondImplFactoryImpl());
		ApplicationContext.getInstance().setGoodTypeOwnershipFactory(new GoodTypeOwnershipImplFactoryImpl());
		ApplicationContext.getInstance().setHouseholdFactory(new HouseholdImplFactoryImpl());
		ApplicationContext.getInstance().setMarketOrderFactory(new MarketOrderImplFactoryImpl());
		ApplicationContext.getInstance().setPricingBehaviourFactory(new PricingBehaviourFactoryImpl());
		ApplicationContext.getInstance().setShareFactory(new ShareImplFactoryImpl());
		ApplicationContext.getInstance().setStateFactory(new StateImplFactoryImpl());
		ApplicationContext.getInstance().setTraderFactory(new TraderImplFactoryImpl());

		/*
		 * services
		 */
		ApplicationContext.getInstance().setAgentService(new AgentServiceImpl());
		ApplicationContext.getInstance().setHardCashService(new HardCashServiceImpl());
		ApplicationContext.getInstance().setPropertyService(new PropertyServiceImpl());
		ApplicationContext.getInstance().setMarketService(new SettlementMarketServiceImpl());

		ApplicationContext.getInstance().setTimeSystem(new TimeSystemImpl(2001));

		/*
		 * configuration
		 */
		final Configuration configuration = new Configuration(configurationPropertiesFilename);
		ApplicationContext.getInstance().setConfiguration(configuration);

		/*
		 * input-output model
		 */
		final InputOutputModel inputOutputModel;

		switch (configuration.inputOutputModelConfig.getInputOutputModelSetting()) {
		case InputOutputModelMinimal:
			inputOutputModel = new InputOutputModelMinimalImpl();
			break;
		case InputOutputModelTesting:
			inputOutputModel = new InputOutputModelTestingImpl();
			break;
		case InputOutputModelNoDependencies:
			inputOutputModel = new InputOutputModelNoDependenciesImpl();
			break;
		case InputOutputModelInterdependencies:
			inputOutputModel = new InputOutputModelInterdependenciesImpl();
			break;
		default:
			throw new IllegalStateException("inputOutputModel not set");
		}

		ApplicationContext.getInstance().setInputOutputModel(inputOutputModel);

		/*
		 * model registry
		 */
		ApplicationContext.getInstance().setModelRegistry(new ModelRegistry(inputOutputModel));
		ApplicationContext.getInstance().setLog(new LogImpl());

		/*
		 * simulation runner
		 */
		ApplicationContext.getInstance().setSimulationRunner(new SimulationRunnerImpl());
	}
}
