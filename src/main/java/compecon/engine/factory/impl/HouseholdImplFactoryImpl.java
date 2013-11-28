package compecon.engine.factory.impl;

import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.household.Household;
import compecon.economy.sectors.household.impl.HouseholdImpl;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.factory.HouseholdFactory;
import compecon.engine.util.HibernateUtil;
import compecon.math.intertemporal.impl.ModiglianiIntertemporalConsumptionFunction;

public class HouseholdImplFactoryImpl implements HouseholdFactory {

	@Override
	public void deleteHousehold(final Household agent) {
		ApplicationContext.getInstance().getHouseholdDAO()
				.delete((Household) agent);
		HibernateUtil.flushSession();
	}

	@Override
	public Household newInstanceHousehold(final Currency primaryCurrency,
			final int ageInDays) {
		HouseholdImpl household = new HouseholdImpl();
		if (!HibernateUtil.isActive())
			household.setId(ApplicationContext.getInstance()
					.getSequenceNumberGenerator().getNextId());
		household.setAgeInDays(ageInDays);
		household.setPrimaryCurrency(primaryCurrency);

		household.setUtilityFunction(ApplicationContext.getInstance()
				.getInputOutputModel().getUtilityFunctionOfHousehold());

		// intertemporal preferences
		/*
		 * Map<Period, Double> intertemeporalPreferences = new HashMap<Period,
		 * Double>(); intertemeporalPreferences.put(Period.CURRENT, 0.5);
		 * intermeporalPreferences.put(Period.NEXT, 0.5);
		 * IntertemporalConsumptionFunction intertemporalConsumptionFunction =
		 * new CobbDouglasIntertemporalConsumptionFunction(
		 * intertemeporalPreferences);
		 */

		household
				.setIntertemporalConsumptionFunction(new ModiglianiIntertemporalConsumptionFunction());

		ApplicationContext.getInstance().getHouseholdDAO().save(household);
		household.initialize();
		HibernateUtil.flushSession();
		return household;
	}

}
