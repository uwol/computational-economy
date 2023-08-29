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

import java.util.HashSet;
import java.util.Set;

import io.github.uwol.compecon.economy.sectors.financial.CreditBank;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.economy.sectors.financial.impl.CreditBankImpl;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.factory.CreditBankFactory;

public class CreditBankImplFactoryImpl implements CreditBankFactory {

	@Override
	public void deleteCreditBank(final CreditBank agent) {
		ApplicationContext.getInstance().getCreditBankDAO().delete(agent);
	}

	@Override
	public CreditBank newInstanceCreditBank(final Currency offeredCurrency) {
		final Set<Currency> offeredCurrencies = new HashSet<Currency>();
		offeredCurrencies.add(offeredCurrency);
		return newInstanceCreditBank(offeredCurrencies, offeredCurrency);
	}

	@Override
	public CreditBank newInstanceCreditBank(final Set<Currency> offeredCurrencies, final Currency primaryCurrency) {
		assert (primaryCurrency != null);
		assert (offeredCurrencies.contains(primaryCurrency));

		final CreditBankImpl creditBank = new CreditBankImpl();

		creditBank.setId(ApplicationContext.getInstance().getSequenceNumberGenerator().getNextId());

		creditBank.setPrimaryCurrency(primaryCurrency);
		ApplicationContext.getInstance().getCreditBankDAO().save(creditBank);
		creditBank.initialize();

		return creditBank;
	}

}
