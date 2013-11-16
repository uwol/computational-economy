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

import compecon.engine.dao.inmemory.impl.SequenceNumberGeneratorImpl;
import compecon.engine.log.impl.LogImpl;
import compecon.engine.service.impl.AgentServiceImpl;
import compecon.engine.service.impl.BankAccountServiceImpl;
import compecon.engine.service.impl.GoodTypeOwnershipServiceImpl;
import compecon.engine.service.impl.HardCashServiceImpl;
import compecon.engine.service.impl.MarketOrderServiceImpl;
import compecon.engine.service.impl.PropertyServiceImpl;
import compecon.engine.service.impl.SettlementMarketServiceImpl;
import compecon.engine.statistics.ModelRegistry;
import compecon.engine.timesystem.impl.TimeSystem;
import compecon.materia.impl.InputOutputModelInterdependenciesImpl;
import compecon.materia.impl.InputOutputModelMinimalImpl;
import compecon.materia.impl.InputOutputModelSegementedImpl;

public class ApplicationContextFactory {

	public final static String defaultConfigFilename = "interdependencies.configuration.properties";

	protected static void configureMinimalApplicationContext() {
		ApplicationContext.getInstance().reset();

		/*
		 * configuration
		 */
		String configurationPropertiesFilename = System
				.getProperty("configuration.properties");
		if (configurationPropertiesFilename == null
				|| configurationPropertiesFilename.isEmpty()) {
			// if no configuration properties are set via VM args use
			// default configuration properties
			configurationPropertiesFilename = defaultConfigFilename;
		}

		ApplicationContext.getInstance().setConfiguration(
				new Configuration(configurationPropertiesFilename));

		/*
		 * services
		 */
		ApplicationContext.getInstance()
				.setAgentService(new AgentServiceImpl());
		ApplicationContext.getInstance().setBankAccountService(
				new BankAccountServiceImpl());
		ApplicationContext.getInstance().setGoodTypeOwnershipService(
				new GoodTypeOwnershipServiceImpl());
		ApplicationContext.getInstance().setHardCashService(
				new HardCashServiceImpl());
		ApplicationContext.getInstance().setMarketOrderService(
				new MarketOrderServiceImpl());
		ApplicationContext.getInstance().setPropertyService(
				new PropertyServiceImpl());
		ApplicationContext.getInstance().setMarketService(
				new SettlementMarketServiceImpl());

		/*
		 * input-output model
		 */
		switch (ApplicationContext.getInstance().getConfiguration().inputOutputModelConfig
				.getInputOutputModelSetup()) {
		case InputOutputModelMinimal:
			ApplicationContext.getInstance().setInputOutputModel(
					new InputOutputModelMinimalImpl());
			break;
		case InputOutputModelSegmented:
			ApplicationContext.getInstance().setInputOutputModel(
					new InputOutputModelSegementedImpl());
			break;
		case InputOutputModelInterdependencies:
			ApplicationContext.getInstance().setInputOutputModel(
					new InputOutputModelInterdependenciesImpl());
			break;
		default:
			break;
		}

		ApplicationContext.getInstance().setSequenceNumberGenerator(
				new SequenceNumberGeneratorImpl());
		ApplicationContext.getInstance().setTimeSystem(new TimeSystem(2000));
		ApplicationContext.getInstance().setLog(new LogImpl());
		ApplicationContext.getInstance().setModelRegistry(new ModelRegistry());

	}

	public static void configureInMemoryApplicationContext() {
		ApplicationContext.getInstance().reset();

		configureMinimalApplicationContext();

		ApplicationContext.getInstance().setBankAccountDAO(
				new compecon.engine.dao.inmemory.impl.BankAccountDAOImpl());
		ApplicationContext.getInstance().setCentralBankDAO(
				new compecon.engine.dao.inmemory.impl.CentralBankDAOImpl());
		ApplicationContext.getInstance().setCreditBankDAO(
				new compecon.engine.dao.inmemory.impl.CreditBankDAOImpl());
		ApplicationContext
				.getInstance()
				.setGoodTypeOwnershipDAO(
						new compecon.engine.dao.inmemory.impl.GoodTypeOwnershipDAOImpl());
		ApplicationContext.getInstance().setHouseholdDAO(
				new compecon.engine.dao.inmemory.impl.HouseholdDAOImpl());
		ApplicationContext.getInstance().setFactoryDAO(
				new compecon.engine.dao.inmemory.impl.FactoryDAOImpl());
		ApplicationContext.getInstance().setMarketOrderDAO(
				new compecon.engine.dao.inmemory.impl.MarketOrderDAOImpl());
		ApplicationContext.getInstance().setPropertyDAO(
				new compecon.engine.dao.inmemory.impl.PropertyDAOImpl());
		ApplicationContext.getInstance().setStateDAO(
				new compecon.engine.dao.inmemory.impl.StateDAOImpl());
		ApplicationContext.getInstance().setTraderDAO(
				new compecon.engine.dao.inmemory.impl.TraderDAOImpl());
	}

	public static void configureHibernateApplicationContext() {
		ApplicationContext.getInstance().reset();

		configureMinimalApplicationContext();

		ApplicationContext.getInstance().setBankAccountDAO(
				new compecon.engine.dao.hibernate.impl.BankAccountDAOImpl());
		ApplicationContext.getInstance().setCentralBankDAO(
				new compecon.engine.dao.hibernate.impl.CentralBankDAOImpl());
		ApplicationContext.getInstance().setCreditBankDAO(
				new compecon.engine.dao.hibernate.impl.CreditBankDAOImpl());
		ApplicationContext
				.getInstance()
				.setGoodTypeOwnershipDAO(
						new compecon.engine.dao.hibernate.impl.GoodTypeOwnershipDAOImpl());
		ApplicationContext.getInstance().setHouseholdDAO(
				new compecon.engine.dao.hibernate.impl.HouseholdDAOImpl());
		ApplicationContext.getInstance().setFactoryDAO(
				new compecon.engine.dao.hibernate.impl.FactoryDAOImpl());
		ApplicationContext.getInstance().setMarketOrderDAO(
				new compecon.engine.dao.hibernate.impl.MarketOrderDAOImpl());
		ApplicationContext.getInstance().setPropertyDAO(
				new compecon.engine.dao.hibernate.impl.PropertyDAOImpl());
		ApplicationContext.getInstance().setStateDAO(
				new compecon.engine.dao.hibernate.impl.StateDAOImpl());
		ApplicationContext.getInstance().setTraderDAO(
				new compecon.engine.dao.hibernate.impl.TraderDAOImpl());
	}

}
