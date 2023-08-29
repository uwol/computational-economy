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

import io.github.uwol.compecon.economy.sectors.financial.CentralBank;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.economy.sectors.financial.impl.CentralBankImpl;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.factory.CentralBankFactory;

public class CentralBankImplFactoryImpl implements CentralBankFactory {

	@Override
	public void deleteCentralBank(final CentralBank agent) {
		ApplicationContext.getInstance().getCentralBankDAO().delete(agent);
	}

	@Override
	public CentralBank newInstanceCentralBank(final Currency currency) {
		assert (currency != null);

		final CentralBankImpl centralBank = new CentralBankImpl();

		centralBank.setId(ApplicationContext.getInstance().getSequenceNumberGenerator().getNextId());

		centralBank.setPrimaryCurrency(currency);
		ApplicationContext.getInstance().getCentralBankDAO().save(centralBank);
		centralBank.initialize();

		return centralBank;
	}
}
