package compecon.engine.factory;

import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;

public interface HouseholdFactory {

	public void deleteHousehold(final Household agent);

	public Household newInstanceHousehold(final Currency primaryCurrency,
			final int ageInDays);
}
