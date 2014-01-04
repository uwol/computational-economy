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

import compecon.economy.sectors.financial.CentralBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.financial.impl.CentralBankImpl;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.factory.CentralBankFactory;
import compecon.engine.util.HibernateUtil;

public class CentralBankImplFactoryImpl implements CentralBankFactory {

	@Override
	public void deleteCentralBank(final CentralBank agent) {
		ApplicationContext.getInstance().getCentralBankDAO()
				.delete((CentralBank) agent);
		HibernateUtil.flushSession();
	}

	@Override
	public CentralBank newInstanceCentralBank(Currency currency) {
		assert (currency != null);

		final CentralBankImpl centralBank = new CentralBankImpl();
		if (!HibernateUtil.isActive())
			centralBank.setId(ApplicationContext.getInstance()
					.getSequenceNumberGenerator().getNextId());
		centralBank.setPrimaryCurrency(currency);
		ApplicationContext.getInstance().getCentralBankDAO().save(centralBank);
		centralBank.initialize();
		HibernateUtil.flushSession();
		return centralBank;
	}
}
