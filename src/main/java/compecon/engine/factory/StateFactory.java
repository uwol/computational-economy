package compecon.engine.factory;

import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.State;

public interface StateFactory {

	public void deleteState(final State agent);

	public State newInstanceState(final Currency currency);
}
