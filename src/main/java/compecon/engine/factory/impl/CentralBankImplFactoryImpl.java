package compecon.engine.factory.impl;

import compecon.economy.sectors.financial.CentralBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.financial.impl.CentralBankImpl;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.factory.CentralBankFactory;
import compecon.engine.util.HibernateUtil;

public class CentralBankImplFactoryImpl implements CentralBankFactory {

	@Override
	public void deleteCentralBank(final CentralBank agent) {
		ApplicationContext.getInstance().getCentralBankDAO()
				.delete((CentralBank) agent);
		HibernateUtil.flushSession();
	}

	@Override
	public CentralBank newInstanceCentralBank(Currency currency) {
		CentralBankImpl centralBank = new CentralBankImpl();
		if (!HibernateUtil.isActive())
			centralBank.setId(ApplicationContext.getInstance()
					.getSequenceNumberGenerator().getNextId());
		centralBank.setPrimaryCurrency(currency);
		ApplicationContext.getInstance().getCentralBankDAO().save(centralBank);
		centralBank.initialize();
		HibernateUtil.flushSession();
		return centralBank;
	}

}
