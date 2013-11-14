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

package compecon.engine.factory.impl;

import compecon.economy.agent.Agent;
import compecon.economy.property.Property;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.security.debt.FixedRateBond;
import compecon.economy.security.debt.impl.FixedRateBondImpl;
import compecon.economy.security.equity.JointStockCompany;
import compecon.economy.security.equity.Share;
import compecon.economy.security.equity.impl.ShareImpl;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.factory.PropertyFactory;
import compecon.engine.util.HibernateUtil;

public class PropertyFactoryImpl implements PropertyFactory {

	public FixedRateBond newInstanceFixedRateBond(final Agent owner,
			final Currency currency,
			final BankAccount faceValueFromBankAccount,
			final BankAccount couponFromBankAccount, final double faceValue,
			final double coupon) {
		final FixedRateBondImpl fixedRateBond = new FixedRateBondImpl();
		fixedRateBond.setOwner(owner);
		fixedRateBond.setFaceValueFromBankAccount(faceValueFromBankAccount);
		fixedRateBond.setCouponFromBankAccount(couponFromBankAccount);
		fixedRateBond.setFaceValue(faceValue);
		fixedRateBond.setCoupon(coupon);
		fixedRateBond.setIssuedInCurrency(currency);
		fixedRateBond.initialize();
		ApplicationContext.getInstance().getPropertyDAO().save(fixedRateBond);
		HibernateUtil.flushSession();
		return fixedRateBond;
	}

	public Share newInstanceShare(final Agent owner,
			final JointStockCompany jointStockCompany) {
		final ShareImpl share = new ShareImpl();
		share.setJointStockCompany(jointStockCompany);
		share.setOwner(owner);
		share.initialize();
		ApplicationContext.getInstance().getPropertyDAO().save(share);
		HibernateUtil.flushSession();
		return share;
	}

	public void deleteProperty(final Property property) {
		ApplicationContext.getInstance().getPropertyDAO().delete(property);
	}
}
