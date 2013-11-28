package compecon.engine.factory;

import compecon.economy.sectors.financial.CentralBank;
import compecon.economy.sectors.financial.Currency;

public interface CentralBankFactory {

	public void deleteCentralBank(final CentralBank agent);

	public CentralBank newInstanceCentralBank(final Currency currency);
}
