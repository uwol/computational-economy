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

package io.github.uwol.compecon.economy.security.equity.impl;

import io.github.uwol.compecon.economy.property.impl.PropertyIssuedImpl;
import io.github.uwol.compecon.economy.sectors.financial.BankAccountDelegate;
import io.github.uwol.compecon.economy.security.equity.Share;

public class ShareImpl extends PropertyIssuedImpl implements Share {

	protected BankAccountDelegate dividendBankAccountDelegate;

	@Override
	public BankAccountDelegate getDividendBankAccountDelegate() {
		return dividendBankAccountDelegate;
	}

	@Override
	public void resetOwner() {
		super.resetOwner();
		dividendBankAccountDelegate = null;
	}

	@Override
	public void setDividendBankAccountDelegate(final BankAccountDelegate dividendBankAccountDelegate) {
		this.dividendBankAccountDelegate = dividendBankAccountDelegate;
	}
}
