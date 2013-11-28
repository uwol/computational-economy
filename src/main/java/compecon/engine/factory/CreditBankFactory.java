package compecon.engine.factory;

import java.util.Set;

import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;

public interface CreditBankFactory {

	public void deleteCreditBank(final CreditBank agent);

	public CreditBank newInstanceCreditBank(final Currency offeredCurrency);

	public CreditBank newInstanceCreditBank(
			final Set<Currency> offeredCurrencies,
			final Currency primaryCurrency);
}
