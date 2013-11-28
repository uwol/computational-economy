package compecon.engine.factory;

import compecon.economy.materia.GoodType;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.industry.Factory;

public interface FactoryFactory {

	public void deleteFactory(final Factory agent);

	public Factory newInstanceFactory(final GoodType goodType,
			final Currency primaryCurrency);
}
