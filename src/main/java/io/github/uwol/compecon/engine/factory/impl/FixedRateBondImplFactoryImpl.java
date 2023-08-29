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

import io.github.uwol.compecon.economy.agent.Agent;
import io.github.uwol.compecon.economy.sectors.financial.BankAccountDelegate;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.economy.security.debt.FixedRateBond;
import io.github.uwol.compecon.economy.security.debt.impl.FixedRateBondImpl;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.factory.FixedRateBondFactory;

public class FixedRateBondImplFactoryImpl implements FixedRateBondFactory {

	@Override
	public FixedRateBond newInstanceFixedRateBond(final Agent owner, final Agent issuer, final Currency currency,
			final BankAccountDelegate faceValueFromBankAccountDelegate,
			final BankAccountDelegate couponFromBankAccountDelegate, final double faceValue, final double coupon) {
		assert (owner != null);
		assert (issuer != null);
		assert (currency != null);
		assert (faceValueFromBankAccountDelegate != null);
		assert (couponFromBankAccountDelegate != null);
		assert (faceValue > 0);

		final FixedRateBondImpl fixedRateBond = new FixedRateBondImpl();

		fixedRateBond.setId(ApplicationContext.getInstance().getSequenceNumberGenerator().getNextId());

		fixedRateBond.setOwner(owner);
		fixedRateBond.setIssuer(issuer);
		fixedRateBond.setFaceValueFromBankAccountDelegate(faceValueFromBankAccountDelegate);
		fixedRateBond.setCouponFromBankAccountDelegate(couponFromBankAccountDelegate);
		fixedRateBond.setFaceValue(faceValue);
		fixedRateBond.setCoupon(coupon);
		fixedRateBond.setIssuedInCurrency(currency);
		fixedRateBond.initialize();
		ApplicationContext.getInstance().getPropertyDAO().save(fixedRateBond);

		return fixedRateBond;
	}
}
