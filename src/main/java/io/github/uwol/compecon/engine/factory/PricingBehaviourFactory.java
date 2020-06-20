/*
Copyright (C) 2015 u.wol@wwu.de

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

package io.github.uwol.compecon.engine.factory;

import io.github.uwol.compecon.economy.agent.impl.AgentImpl;
import io.github.uwol.compecon.economy.behaviour.PricingBehaviour;
import io.github.uwol.compecon.economy.sectors.financial.Currency;

public interface PricingBehaviourFactory {

	public PricingBehaviour newInstancePricingBehaviour(final AgentImpl agent, final Object offeredObject,
			final Currency denominatedInCurrency, final double initialPrice);

	public PricingBehaviour newInstancePricingBehaviour(final AgentImpl agent, final Object offeredObject,
			final Currency denominatedInCurrency, final double initialPrice, final double priceChangeIncrement);
}
