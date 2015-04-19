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

import compecon.economy.materia.InputOutputModel;
import compecon.engine.dao.BankAccountDAO;
import compecon.engine.dao.CentralBankDAO;
import compecon.engine.dao.CreditBankDAO;
import compecon.engine.dao.FactoryDAO;
import compecon.engine.dao.GoodTypeOwnershipDAO;
import compecon.engine.dao.HouseholdDAO;
import compecon.engine.dao.MarketOrderDAO;
import compecon.engine.dao.PropertyDAO;
import compecon.engine.dao.StateDAO;
import compecon.engine.dao.TraderDAO;
import compecon.engine.dao.inmemory.impl.SequenceNumberGeneratorImpl;
import compecon.engine.factory.AgentFactory;
import compecon.engine.factory.BankAccountFactory;
import compecon.engine.factory.BudgetingBehaviourFactory;
import compecon.engine.factory.CentralBankFactory;
import compecon.engine.factory.CreditBankFactory;
import compecon.engine.factory.FactoryFactory;
import compecon.engine.factory.FixedRateBondFactory;
import compecon.engine.factory.GoodTypeOwnershipFactory;
import compecon.engine.factory.HouseholdFactory;
import compecon.engine.factory.MarketOrderFactory;
import compecon.engine.factory.PricingBehaviourFactory;
import compecon.engine.factory.ShareFactory;
import compecon.engine.factory.StateFactory;
import compecon.engine.factory.TraderFactory;
import compecon.engine.log.Log;
import compecon.engine.random.RandomNumberGenerator;
import compecon.engine.runner.SimulationRunner;
import compecon.engine.service.AgentService;
import compecon.engine.service.HardCashService;
import compecon.engine.service.PropertyService;
import compecon.engine.service.SettlementMarketService;
import compecon.engine.statistics.ModelRegistry;
import compecon.engine.timesystem.TimeSystem;

public class ApplicationContext {

	protected static ApplicationContext instance;

	// DAOs

	public static ApplicationContext getInstance() {
		if (instance == null) {
			instance = new ApplicationContext();
		}
		return instance;
	}

	public static void setInstance(final ApplicationContext instance) {
		ApplicationContext.instance = instance;
	}

	protected AgentFactory agentFactory;

	protected AgentService agentService;

	protected BankAccountDAO bankAccountDAO;

	protected BankAccountFactory bankAccountFactory;

	protected BudgetingBehaviourFactory budgetingBehaviourFactory;

	protected CentralBankDAO centralBankDAO;

	protected CentralBankFactory centralBankFactory;

	protected Configuration configuration;

	protected CreditBankDAO creditBankDAO;

	protected CreditBankFactory creditBankFactory;

	protected FactoryDAO factoryDAO;

	protected FactoryFactory factoryFactory;

	protected FixedRateBondFactory fixedRateBondFactory;

	protected GoodTypeOwnershipDAO goodTypeOwnershipDAO;

	protected GoodTypeOwnershipFactory goodTypeOwnershipFactory;

	protected HardCashService hardCashService;

	protected HouseholdDAO householdDAO;

	protected HouseholdFactory householdFactory;

	protected InputOutputModel inputOutputModel;

	protected Log log;

	protected MarketOrderDAO marketOrderDAO;

	protected MarketOrderFactory marketOrderFactory;

	protected SettlementMarketService marketService;

	protected ModelRegistry modelRegistry;

	protected PricingBehaviourFactory pricingBehaviourFactory;

	protected PropertyDAO propertyDAO;

	protected PropertyService propertyService;

	protected RandomNumberGenerator randomNumberGenerator;

	protected SequenceNumberGeneratorImpl sequenceNumberGenerator;

	protected ShareFactory shareFactory;

	protected SimulationRunner simulationRunner;

	protected StateDAO stateDAO;

	protected StateFactory stateFactory;

	protected TimeSystem timeSystem;

	protected TraderDAO traderDAO;

	protected TraderFactory traderFactory;

	private ApplicationContext() {
		super();
	}

	public AgentFactory getAgentFactory() {
		return agentFactory;
	}

	public AgentService getAgentService() {
		return agentService;
	}

	public BankAccountDAO getBankAccountDAO() {
		return bankAccountDAO;
	}

	public BankAccountFactory getBankAccountFactory() {
		return bankAccountFactory;
	}

	public BudgetingBehaviourFactory getBudgetingBehaviourFactory() {
		return budgetingBehaviourFactory;
	}

	public CentralBankDAO getCentralBankDAO() {
		return centralBankDAO;
	}

	public CentralBankFactory getCentralBankFactory() {
		return centralBankFactory;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public CreditBankDAO getCreditBankDAO() {
		return creditBankDAO;
	}

	public CreditBankFactory getCreditBankFactory() {
		return creditBankFactory;
	}

	public FactoryDAO getFactoryDAO() {
		return factoryDAO;
	}

	public FactoryFactory getFactoryFactory() {
		return factoryFactory;
	}

	public FixedRateBondFactory getFixedRateBondFactory() {
		return fixedRateBondFactory;
	}

	public GoodTypeOwnershipDAO getGoodTypeOwnershipDAO() {
		return goodTypeOwnershipDAO;
	}

	public GoodTypeOwnershipFactory getGoodTypeOwnershipFactory() {
		return goodTypeOwnershipFactory;
	}

	public HardCashService getHardCashService() {
		return hardCashService;
	}

	public HouseholdDAO getHouseholdDAO() {
		return householdDAO;
	}

	public HouseholdFactory getHouseholdFactory() {
		return householdFactory;
	}

	public InputOutputModel getInputOutputModel() {
		return inputOutputModel;
	}

	public Log getLog() {
		return log;
	}

	public MarketOrderDAO getMarketOrderDAO() {
		return marketOrderDAO;
	}

	public MarketOrderFactory getMarketOrderFactory() {
		return marketOrderFactory;
	}

	public SettlementMarketService getMarketService() {
		return marketService;
	}

	public ModelRegistry getModelRegistry() {
		return modelRegistry;
	}

	public PricingBehaviourFactory getPricingBehaviourFactory() {
		return pricingBehaviourFactory;
	}

	public PropertyDAO getPropertyDAO() {
		return propertyDAO;
	}

	public PropertyService getPropertyService() {
		return propertyService;
	}

	public RandomNumberGenerator getRandomNumberGenerator() {
		return randomNumberGenerator;
	}

	public SequenceNumberGeneratorImpl getSequenceNumberGenerator() {
		return sequenceNumberGenerator;
	}

	public ShareFactory getShareFactory() {
		return shareFactory;
	}

	public SimulationRunner getSimulationRunner() {
		return simulationRunner;
	}

	public StateDAO getStateDAO() {
		return stateDAO;
	}

	public StateFactory getStateFactory() {
		return stateFactory;
	}

	public TimeSystem getTimeSystem() {
		return timeSystem;
	}

	public TraderDAO getTraderDAO() {
		return traderDAO;
	}

	public TraderFactory getTraderFactory() {
		return traderFactory;
	}

	public void reset() {
		instance = null;
	}

	public void setAgentFactory(final AgentFactory agentFactory) {
		this.agentFactory = agentFactory;
	}

	public void setAgentService(final AgentService agentService) {
		this.agentService = agentService;
	}

	public void setBankAccountDAO(final BankAccountDAO bankAccountDAO) {
		this.bankAccountDAO = bankAccountDAO;
	}

	public void setBankAccountFactory(
			final BankAccountFactory bankAccountFactory) {
		this.bankAccountFactory = bankAccountFactory;
	}

	public void setBudgetingBehaviourFactory(
			final BudgetingBehaviourFactory budgetingBehaviourFactory) {
		this.budgetingBehaviourFactory = budgetingBehaviourFactory;
	}

	public void setCentralBankDAO(final CentralBankDAO centralBankDAO) {
		this.centralBankDAO = centralBankDAO;
	}

	public void setCentralBankFactory(
			final CentralBankFactory centralBankFactory) {
		this.centralBankFactory = centralBankFactory;
	}

	public void setConfiguration(final Configuration configuration) {
		this.configuration = configuration;
	}

	public void setCreditBankDAO(final CreditBankDAO creditBankDAO) {
		this.creditBankDAO = creditBankDAO;
	}

	public void setCreditBankFactory(final CreditBankFactory creditBankFactory) {
		this.creditBankFactory = creditBankFactory;
	}

	public void setFactoryDAO(final FactoryDAO factoryDAO) {
		this.factoryDAO = factoryDAO;
	}

	public void setFactoryFactory(final FactoryFactory factoryFactory) {
		this.factoryFactory = factoryFactory;
	}

	public void setFixedRateBondFactory(
			final FixedRateBondFactory fixedRateBondFactory) {
		this.fixedRateBondFactory = fixedRateBondFactory;
	}

	public void setGoodTypeOwnershipDAO(
			final GoodTypeOwnershipDAO goodTypeOwnershipDAO) {
		this.goodTypeOwnershipDAO = goodTypeOwnershipDAO;
	}

	public void setGoodTypeOwnershipFactory(
			final GoodTypeOwnershipFactory goodTypeOwnershipFactory) {
		this.goodTypeOwnershipFactory = goodTypeOwnershipFactory;
	}

	public void setHardCashService(final HardCashService hardCashService) {
		this.hardCashService = hardCashService;
	}

	public void setHouseholdDAO(final HouseholdDAO householdDAO) {
		this.householdDAO = householdDAO;
	}

	public void setHouseholdFactory(final HouseholdFactory householdFactory) {
		this.householdFactory = householdFactory;
	}

	public void setInputOutputModel(final InputOutputModel inputOutputModel) {
		this.inputOutputModel = inputOutputModel;
	}

	public void setLog(final Log log) {
		this.log = log;
	}

	public void setMarketOrderDAO(final MarketOrderDAO marketOrderDAO) {
		this.marketOrderDAO = marketOrderDAO;
	}

	public void setMarketOrderFactory(
			final MarketOrderFactory marketOrderFactory) {
		this.marketOrderFactory = marketOrderFactory;
	}

	public void setMarketService(final SettlementMarketService marketService) {
		this.marketService = marketService;
	}

	public void setModelRegistry(final ModelRegistry modelRegistry) {
		this.modelRegistry = modelRegistry;
	}

	public void setPricingBehaviourFactory(
			final PricingBehaviourFactory pricingBehaviourFactory) {
		this.pricingBehaviourFactory = pricingBehaviourFactory;
	}

	public void setPropertyDAO(final PropertyDAO propertyDAO) {
		this.propertyDAO = propertyDAO;
	}

	public void setPropertyService(final PropertyService propertyService) {
		this.propertyService = propertyService;
	}

	public void setRandomNumberGenerator(
			final RandomNumberGenerator randomNumberGenerator) {
		this.randomNumberGenerator = randomNumberGenerator;
	}

	public void setSequenceNumberGenerator(
			final SequenceNumberGeneratorImpl sequenceNumberGenerator) {
		this.sequenceNumberGenerator = sequenceNumberGenerator;
	}

	public void setShareFactory(final ShareFactory shareFactory) {
		this.shareFactory = shareFactory;
	}

	public void setSimulationRunner(final SimulationRunner simulationRunner) {
		this.simulationRunner = simulationRunner;
	}

	public void setStateDAO(final StateDAO stateDAO) {
		this.stateDAO = stateDAO;
	}

	public void setStateFactory(final StateFactory stateFactory) {
		this.stateFactory = stateFactory;
	}

	public void setTimeSystem(final TimeSystem timeSystem) {
		this.timeSystem = timeSystem;
	}

	public void setTraderDAO(final TraderDAO traderDAO) {
		this.traderDAO = traderDAO;
	}

	public void setTraderFactory(final TraderFactory traderFactory) {
		this.traderFactory = traderFactory;
	}
}
