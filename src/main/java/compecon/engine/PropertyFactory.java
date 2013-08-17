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

package compecon.engine;

import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.law.property.Property;
import compecon.economy.sectors.state.law.security.debt.FixedRateBond;
import compecon.economy.sectors.state.law.security.equity.JointStockCompany;
import compecon.economy.sectors.state.law.security.equity.Share;
import compecon.engine.dao.DAOFactory;
import compecon.engine.util.HibernateUtil;

public class PropertyFactory {
	public static FixedRateBond newInstanceFixedRateBond(Agent owner,
			Currency currency, BankAccount issuerBankAccount,
			String issuerBankAccountPassword, double faceValue, double coupon) {
		FixedRateBond fixedRateBond = new FixedRateBond();
		fixedRateBond.setOwner(owner);
		fixedRateBond.setIssuerBankAccount(issuerBankAccount);
		fixedRateBond.setIssuerBankAccountPassword(issuerBankAccountPassword);
		fixedRateBond.setFaceValue(faceValue);
		fixedRateBond.setCoupon(coupon);
		fixedRateBond.setIssuedInCurrency(currency);
		fixedRateBond.initialize();
		DAOFactory.getPropertyDAO().save(fixedRateBond);
		HibernateUtil.flushSession();
		return fixedRateBond;
	}

	public static Share newInstanceShare(Agent owner,
			JointStockCompany jointStockCompany) {
		Share share = new Share();
		share.setJointStockCompany(jointStockCompany);
		share.setOwner(owner);
		share.initialize();
		DAOFactory.getPropertyDAO().save(share);
		HibernateUtil.flushSession();
		return share;
	}

	public static void deleteProperty(Property property) {
		DAOFactory.getPropertyDAO().delete(property);
	}
}
