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

package compecon.economy.security.equity.impl;

import javax.persistence.Entity;
import javax.persistence.Transient;

import compecon.economy.property.impl.PropertyIssuedImpl;
import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.security.equity.Share;

@Entity
public class ShareImpl extends PropertyIssuedImpl implements Share {

	@Transient
	protected BankAccountDelegate dividendBankAccountDelegate;

	@Override
	public BankAccountDelegate getDividendBankAccountDelegate() {
		return dividendBankAccountDelegate;
	}

	@Override
	@Transient
	public void resetOwner() {
		super.resetOwner();
		dividendBankAccountDelegate = null;
	}

	@Override
	public void setDividendBankAccountDelegate(
			final BankAccountDelegate dividendBankAccountDelegate) {
		this.dividendBankAccountDelegate = dividendBankAccountDelegate;
	}
}
