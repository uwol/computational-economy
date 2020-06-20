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

package io.github.uwol.compecon.engine.service.impl;

import java.util.List;

import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.sectors.financial.CentralBank;
import io.github.uwol.compecon.economy.sectors.financial.CreditBank;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.economy.sectors.household.Household;
import io.github.uwol.compecon.economy.sectors.industry.Factory;
import io.github.uwol.compecon.economy.sectors.state.State;
import io.github.uwol.compecon.economy.sectors.trading.Trader;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.service.AgentService;

public class AgentServiceImpl implements AgentService {

	@Override
	public CentralBank findCentralBank(final Currency currency) {
		final CentralBank centralBank = ApplicationContext.getInstance().getCentralBankDAO().findByCurrency(currency);
		if (centralBank == null) {
			return ApplicationContext.getInstance().getCentralBankFactory().newInstanceCentralBank(currency);
		}
		return centralBank;
	}

	@Override
	public List<CreditBank> findCreditBanks(final Currency currency) {
		return ApplicationContext.getInstance().getCreditBankDAO().findAllByCurrency(currency);
	}

	@Override
	public List<Factory> findFactories(final Currency currency) {
		return ApplicationContext.getInstance().getFactoryDAO().findAllByCurrency(currency);
	}

	@Override
	public List<Factory> findFactories(final Currency currency, final GoodType producedGoodType) {
		return ApplicationContext.getInstance().getFactoryDAO().findAllByCurrencyAndProducedGoodType(currency,
				producedGoodType);
	}

	@Override
	public List<Household> findHouseholds(final Currency currency) {
		return ApplicationContext.getInstance().getHouseholdDAO().findAllByCurrency(currency);
	}

	@Override
	public CreditBank findRandomCreditBank(final Currency currency) {
		return ApplicationContext.getInstance().getCreditBankDAO().findRandom(currency);
	}

	@Override
	public Factory findRandomFactory() {
		return ApplicationContext.getInstance().getFactoryDAO().findRandom();
	}

	@Override
	public State findState(final Currency currency) {
		final State state = ApplicationContext.getInstance().getStateDAO().findByCurrency(currency);
		if (state == null) {
			return ApplicationContext.getInstance().getStateFactory().newInstanceState(currency);
		}
		return state;
	}

	@Override
	public List<Trader> findTraders(final Currency currency) {
		return ApplicationContext.getInstance().getTraderDAO().findAllByCurrency(currency);
	}
}
