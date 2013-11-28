package compecon.engine.factory;

import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.trading.Trader;

public interface TraderFactory {

	public void deleteTrader(final Trader agent);

	public Trader newInstanceTrader(final Currency primaryCurrency);
}
