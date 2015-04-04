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

package compecon.economy.agent;

import java.util.Set;

import compecon.economy.LifecycleEntity;
import compecon.economy.markets.MarketParticipant;
import compecon.economy.property.PropertyOwner;
import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.sectors.financial.BankCustomer;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.timesystem.TimeSystemEvent;

public interface Agent extends LifecycleEntity, PropertyOwner,
		MarketParticipant, BankCustomer {

	public BankAccountDelegate getBankAccountTransactionsDelegate();

	public int getId();

	public Currency getPrimaryCurrency();

	public Set<TimeSystemEvent> getTimeSystemEvents();

}
