package compecon.engine.factory.impl;

import java.util.HashSet;
import java.util.Set;

import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.financial.impl.CreditBankImpl;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.factory.CreditBankFactory;
import compecon.engine.util.HibernateUtil;

public class CreditBankImplFactoryImpl implements CreditBankFactory {

	@Override
	public void deleteCreditBank(final CreditBank agent) {
		ApplicationContext.getInstance().getCreditBankDAO()
				.delete((CreditBank) agent);
		HibernateUtil.flushSession();
	}

	@Override
	public CreditBank newInstanceCreditBank(final Currency offeredCurrency) {
		final Set<Currency> offeredCurrencies = new HashSet<Currency>();
		offeredCurrencies.add(offeredCurrency);
		return newInstanceCreditBank(offeredCurrencies, offeredCurrency);
	}

	@Override
	public CreditBank newInstanceCreditBank(
			final Set<Currency> offeredCurrencies,
			final Currency primaryCurrency) {
		assert (offeredCurrencies.contains(primaryCurrency));

		final CreditBankImpl creditBank = new CreditBankImpl();
		if (!HibernateUtil.isActive())
			creditBank.setId(ApplicationContext.getInstance()
					.getSequenceNumberGenerator().getNextId());
		creditBank.setPrimaryCurrency(primaryCurrency);
		ApplicationContext.getInstance().getCreditBankDAO().save(creditBank);
		creditBank.initialize();
		HibernateUtil.flushSession();
		return creditBank;
	}

}
