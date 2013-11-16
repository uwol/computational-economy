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
import compecon.engine.log.Log;
import compecon.engine.runner.SimulationRunner;
import compecon.engine.service.AgentService;
import compecon.engine.service.BankAccountService;
import compecon.engine.service.GoodTypeOwnershipService;
import compecon.engine.service.HardCashService;
import compecon.engine.service.MarketOrderService;
import compecon.engine.service.PropertyService;
import compecon.engine.service.SettlementMarketService;
import compecon.engine.statistics.ModelRegistry;
import compecon.engine.timesystem.ITimeSystem;
import compecon.materia.InputOutputModel;

public class ApplicationContext {

	protected static ApplicationContext instance;

	// DAOs

	protected BankAccountDAO bankAccountDAO;

	protected CentralBankDAO centralBankDAO;

	protected CreditBankDAO creditBankDAO;

	protected GoodTypeOwnershipDAO goodTypeOwnershipDAO;

	protected HouseholdDAO householdDAO;

	protected FactoryDAO factoryDAO;

	protected MarketOrderDAO marketOrderDAO;

	protected PropertyDAO propertyDAO;

	protected StateDAO stateDAO;

	protected TraderDAO traderDAO;

	// services

	protected AgentService agentService;

	protected BankAccountService bankAccountService;

	protected GoodTypeOwnershipService goodTypeOwnershipService;

	protected HardCashService hardCashService;

	protected SettlementMarketService marketService;

	protected MarketOrderService marketOrderService;

	protected PropertyService propertyService;

	// system objects

	protected Configuration configuration;

	protected InputOutputModel inputOutputModel;

	protected Log log;

	protected ModelRegistry modelRegistry;

	protected SimulationRunner runner;

	protected SequenceNumberGeneratorImpl sequenceNumberGenerator;

	protected ITimeSystem timeSystem;

	private ApplicationContext() {
		super();
	}

	public static ApplicationContext getInstance() {
		if (instance == null)
			instance = new ApplicationContext();
		return instance;
	}

	/*
	 * reset
	 */

	public void reset() {
		instance = null;
	}

	public BankAccountDAO getBankAccountDAO() {
		return bankAccountDAO;
	}

	public CentralBankDAO getCentralBankDAO() {
		return centralBankDAO;
	}

	public CreditBankDAO getCreditBankDAO() {
		return creditBankDAO;
	}

	public GoodTypeOwnershipDAO getGoodTypeOwnershipDAO() {
		return goodTypeOwnershipDAO;
	}

	public HouseholdDAO getHouseholdDAO() {
		return householdDAO;
	}

	public FactoryDAO getFactoryDAO() {
		return factoryDAO;
	}

	public MarketOrderDAO getMarketOrderDAO() {
		return marketOrderDAO;
	}

	public PropertyDAO getPropertyDAO() {
		return propertyDAO;
	}

	public StateDAO getStateDAO() {
		return stateDAO;
	}

	public TraderDAO getTraderDAO() {
		return traderDAO;
	}

	public AgentService getAgentService() {
		return agentService;
	}

	public BankAccountService getBankAccountService() {
		return bankAccountService;
	}

	public GoodTypeOwnershipService getGoodTypeOwnershipService() {
		return goodTypeOwnershipService;
	}

	public HardCashService getHardCashService() {
		return hardCashService;
	}

	public SettlementMarketService getMarketService() {
		return marketService;
	}

	public MarketOrderService getMarketOrderService() {
		return marketOrderService;
	}

	public PropertyService getPropertyService() {
		return propertyService;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public InputOutputModel getInputOutputModel() {
		return inputOutputModel;
	}

	public Log getLog() {
		return log;
	}

	public ModelRegistry getModelRegistry() {
		return modelRegistry;
	}

	public SimulationRunner getRunner() {
		return runner;
	}

	public SequenceNumberGeneratorImpl getSequenceNumberGenerator() {
		return sequenceNumberGenerator;
	}

	public ITimeSystem getTimeSystem() {
		return timeSystem;
	}

	public static void setInstance(ApplicationContext instance) {
		ApplicationContext.instance = instance;
	}

	public void setBankAccountDAO(BankAccountDAO bankAccountDAO) {
		this.bankAccountDAO = bankAccountDAO;
	}

	public void setCentralBankDAO(CentralBankDAO centralBankDAO) {
		this.centralBankDAO = centralBankDAO;
	}

	public void setCreditBankDAO(CreditBankDAO creditBankDAO) {
		this.creditBankDAO = creditBankDAO;
	}

	public void setGoodTypeOwnershipDAO(
			GoodTypeOwnershipDAO goodTypeOwnershipDAO) {
		this.goodTypeOwnershipDAO = goodTypeOwnershipDAO;
	}

	public void setHouseholdDAO(HouseholdDAO householdDAO) {
		this.householdDAO = householdDAO;
	}

	public void setFactoryDAO(FactoryDAO factoryDAO) {
		this.factoryDAO = factoryDAO;
	}

	public void setMarketOrderDAO(MarketOrderDAO marketOrderDAO) {
		this.marketOrderDAO = marketOrderDAO;
	}

	public void setPropertyDAO(PropertyDAO propertyDAO) {
		this.propertyDAO = propertyDAO;
	}

	public void setStateDAO(StateDAO stateDAO) {
		this.stateDAO = stateDAO;
	}

	public void setTraderDAO(TraderDAO traderDAO) {
		this.traderDAO = traderDAO;
	}

	public void setAgentService(AgentService agentService) {
		this.agentService = agentService;
	}

	public void setBankAccountService(BankAccountService bankAccountService) {
		this.bankAccountService = bankAccountService;
	}

	public void setGoodTypeOwnershipService(
			GoodTypeOwnershipService goodTypeOwnershipService) {
		this.goodTypeOwnershipService = goodTypeOwnershipService;
	}

	public void setHardCashService(HardCashService hardCashService) {
		this.hardCashService = hardCashService;
	}

	public void setMarketService(SettlementMarketService marketService) {
		this.marketService = marketService;
	}

	public void setMarketOrderService(MarketOrderService marketOrderService) {
		this.marketOrderService = marketOrderService;
	}

	public void setPropertyService(PropertyService propertyService) {
		this.propertyService = propertyService;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public void setInputOutputModel(InputOutputModel inputOutputModel) {
		this.inputOutputModel = inputOutputModel;
	}

	public void setLog(Log log) {
		this.log = log;
	}

	public void setModelRegistry(ModelRegistry modelRegistry) {
		this.modelRegistry = modelRegistry;
	}

	public void setRunner(SimulationRunner runner) {
		this.runner = runner;
	}

	public void setSequenceNumberGenerator(
			SequenceNumberGeneratorImpl sequenceNumberGenerator) {
		this.sequenceNumberGenerator = sequenceNumberGenerator;
	}

	public void setTimeSystem(ITimeSystem timeSystem) {
		this.timeSystem = timeSystem;
	}

}
