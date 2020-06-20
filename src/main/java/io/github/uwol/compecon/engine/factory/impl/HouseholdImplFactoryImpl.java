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

package io.github.uwol.compecon.engine.factory.impl;

import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.economy.sectors.household.Household;
import io.github.uwol.compecon.economy.sectors.household.impl.HouseholdImpl;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.factory.HouseholdFactory;
import io.github.uwol.compecon.engine.util.HibernateUtil;
import io.github.uwol.compecon.math.intertemporal.impl.ModiglianiIntertemporalConsumptionFunction;

public class HouseholdImplFactoryImpl implements HouseholdFactory {

	@Override
	public void deleteHousehold(final Household agent) {
		ApplicationContext.getInstance().getHouseholdDAO().delete(agent);
		HibernateUtil.flushSession();
	}

	@Override
	public Household newInstanceHousehold(final Currency primaryCurrency, final int ageInDays) {
		assert (primaryCurrency != null);

		final HouseholdImpl household = new HouseholdImpl();

		if (!HibernateUtil.isActive()) {
			household.setId(ApplicationContext.getInstance().getSequenceNumberGenerator().getNextId());
		}

		household.setAgeInDays(ageInDays);
		household.setPrimaryCurrency(primaryCurrency);

		household.setUtilityFunction(
				ApplicationContext.getInstance().getInputOutputModel().getUtilityFunctionOfHousehold());

		// intertemporal preferences
		/*
		 * Map<Period, Double> intertemeporalPreferences = new HashMap<Period,
		 * Double>(); intertemeporalPreferences.put(Period.CURRENT, 0.5);
		 * intermeporalPreferences.put(Period.NEXT, 0.5);
		 * IntertemporalConsumptionFunction intertemporalConsumptionFunction = new
		 * CobbDouglasIntertemporalConsumptionFunction( intertemeporalPreferences);
		 */

		household.setIntertemporalConsumptionFunction(new ModiglianiIntertemporalConsumptionFunction());

		ApplicationContext.getInstance().getHouseholdDAO().save(household);
		household.initialize();
		HibernateUtil.flushSession();
		return household;
	}

}
