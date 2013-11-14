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

import compecon.dashboard.Dashboard;
import compecon.economy.property.HardCashRegister;
import compecon.economy.property.PropertyRegister;
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
import compecon.engine.factory.MarketFactory;
import compecon.engine.factory.MarketOrderFactory;
import compecon.engine.factory.PropertyFactory;
import compecon.engine.log.Log;
import compecon.engine.runner.SimulationRunner;
import compecon.engine.statistics.ModelRegistry;
import compecon.engine.timesystem.ITimeSystem;
import compecon.materia.InputOutputModel;

public class ApplicationContext {

	protected static ApplicationContext instance;

	// system objects

	protected Configuration configuration;

	protected Dashboard dashboard;

	protected InputOutputModel inputOutputModel;

	protected Log log;

	protected ModelRegistry modelRegistry;

	protected SimulationRunner runner;

	protected SequenceNumberGeneratorImpl sequenceNumberGenerator;

	// economy objects

	protected PropertyRegister propertyRegister;

	protected HardCashRegister hardCashRegister;

	protected ITimeSystem timeSystem;

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

	// factories

	protected AgentFactory agentFactory;

	protected BankAccountFactory bankAccountFactory;

	protected MarketFactory marketFactory;

	protected MarketOrderFactory marketOrderFactory;

	protected PropertyFactory propertyFactory;

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

	/*
	 * accessors
	 */

	public Configuration getConfiguration() {
		return configuration;
	}

	public Dashboard getDashboard() {
		return dashboard;
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

	public SequenceNumberGeneratorImpl getSequenceNumberGenerator() {
		return sequenceNumberGenerator;
	}

	public PropertyRegister getPropertyRegister() {
		return propertyRegister;
	}

	public HardCashRegister getHardCashRegister() {
		return hardCashRegister;
	}

	public ITimeSystem getTimeSystem() {
		return timeSystem;
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

	public AgentFactory getAgentFactory() {
		return agentFactory;
	}

	public BankAccountFactory getBankAccountFactory() {
		return bankAccountFactory;
	}

	public MarketFactory getMarketFactory() {
		return marketFactory;
	}

	public MarketOrderFactory getMarketOrderFactory() {
		return marketOrderFactory;
	}

	public PropertyFactory getPropertyFactory() {
		return propertyFactory;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public void setDashboard(Dashboard dashboard) {
		this.dashboard = dashboard;
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

	public void setSequenceNumberGenerator(
			SequenceNumberGeneratorImpl sequenceNumberGenerator) {
		this.sequenceNumberGenerator = sequenceNumberGenerator;
	}

	public void setPropertyRegister(PropertyRegister propertyRegister) {
		this.propertyRegister = propertyRegister;
	}

	public void setHardCashRegister(HardCashRegister hardCashRegister) {
		this.hardCashRegister = hardCashRegister;
	}

	public void setTimeSystem(ITimeSystem timeSystem) {
		this.timeSystem = timeSystem;
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

	public void setAgentFactory(AgentFactory agentFactory) {
		this.agentFactory = agentFactory;
	}

	public void setBankAccountFactory(BankAccountFactory bankAccountFactory) {
		this.bankAccountFactory = bankAccountFactory;
	}

	public void setMarketFactory(MarketFactory marketFactory) {
		this.marketFactory = marketFactory;
	}

	public void setMarketOrderFactory(MarketOrderFactory marketOrderFactory) {
		this.marketOrderFactory = marketOrderFactory;
	}

	public void setPropertyFactory(PropertyFactory propertyFactory) {
		this.propertyFactory = propertyFactory;
	}

	public SimulationRunner getRunner() {
		return runner;
	}

	public void setRunner(SimulationRunner runner) {
		this.runner = runner;
	}

}
