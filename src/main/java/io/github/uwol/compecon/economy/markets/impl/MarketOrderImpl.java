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

package io.github.uwol.compecon.economy.markets.impl;

import io.github.uwol.compecon.economy.markets.MarketOrder;
import io.github.uwol.compecon.economy.markets.MarketParticipant;
import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.property.Property;
import io.github.uwol.compecon.economy.sectors.financial.BankAccountDelegate;
import io.github.uwol.compecon.economy.sectors.financial.Currency;

/**
 * http://en.wikipedia.org/wiki/Order_%28exchange%29
 */
public class MarketOrderImpl implements MarketOrder, Comparable<MarketOrder> {

	protected double amount;

	protected Currency commodityCurrency;

	protected BankAccountDelegate commodityCurrencyOfferorsBankAcountDelegate;

	protected Currency currency;

	protected GoodType goodType;

	protected int id;

	protected MarketParticipant offeror;

	protected BankAccountDelegate offerorsBankAcountDelegate;

	protected double pricePerUnit;

	protected Property property;

	protected ValidityPeriod validityPeriod;

	@Override
	public int compareTo(final MarketOrder marketOrder) {
		if (this == marketOrder) {
			return 0;
		}
		if (getPricePerUnit() > marketOrder.getPricePerUnit()) {
			return 1;
		}
		if (getPricePerUnit() < marketOrder.getPricePerUnit()) {
			return -1;
		}

		assert id != marketOrder.getId();

		// important, so that two market offers with same price can exists
		return id - marketOrder.getId();
	}

	@Override
	public void decrementAmount(final double amount) {
		this.amount -= amount;
	}

	@Override
	public double getAmount() {
		return amount;
	}

	@Override
	public Object getCommodity() {
		if (CommodityType.CURRENCY.equals(getCommodityType())) {
			return commodityCurrency;
		}
		if (CommodityType.PROPERTY.equals(getCommodityType())) {
			return property;
		}
		return goodType;
	}

	/**
	 * the currency to be traded as the commodity; not the currency of the offeror's
	 * bank account!
	 */
	@Override
	public Currency getCommodityCurrency() {
		return commodityCurrency;
	}

	@Override
	public BankAccountDelegate getCommodityCurrencyOfferorsBankAccountDelegate() {
		return commodityCurrencyOfferorsBankAcountDelegate;
	}

	@Override
	public CommodityType getCommodityType() {
		if (commodityCurrency != null) {
			return CommodityType.CURRENCY;
		}
		if (property != null) {
			return CommodityType.PROPERTY;
		}
		return CommodityType.GOODTYPE;
	}

	@Override
	public Currency getCurrency() {
		return currency;
	}

	@Override
	public GoodType getGoodType() {
		return goodType;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public MarketParticipant getOfferor() {
		return offeror;
	}

	@Override
	public BankAccountDelegate getOfferorsBankAcountDelegate() {
		return offerorsBankAcountDelegate;
	}

	@Override
	public double getPricePerUnit() {
		return pricePerUnit;
	}

	public double getPriceTotal() {
		return pricePerUnit * getAmount();
	}

	@Override
	public Property getProperty() {
		return property;
	}

	@Override
	public ValidityPeriod getValidityPeriod() {
		return validityPeriod;
	}

	public void setAmount(final double amount) {
		this.amount = amount;
	}

	/**
	 * @see #getCommodityCurrency()
	 */
	public void setCommodityCurrency(final Currency currency) {
		commodityCurrency = currency;
	}

	public void setCommodityCurrencyOfferorsBankAccountDelegate(final BankAccountDelegate bankAccountDelegate) {
		commodityCurrencyOfferorsBankAcountDelegate = bankAccountDelegate;
	}

	public void setCurrency(final Currency currency) {
		this.currency = currency;
	}

	public void setGoodType(final GoodType goodType) {
		this.goodType = goodType;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public void setOfferor(final MarketParticipant offeror) {
		this.offeror = offeror;
	}

	public void setOfferorsBankAcountDelegate(final BankAccountDelegate offerorsBankAcountDelegate) {
		this.offerorsBankAcountDelegate = offerorsBankAcountDelegate;
	}

	public void setPricePerUnit(final double pricePerUnit) {
		this.pricePerUnit = pricePerUnit;
	}

	public void setProperty(final Property property) {
		this.property = property;
	}

	public void setValidityPeriod(final ValidityPeriod validityPeriod) {
		this.validityPeriod = validityPeriod;
	}

	@Override
	public String toString() {
		return "id=[" + id + "], currency=[" + currency + "], amount=[" + amount + "], pricePerUnit=[" + pricePerUnit
				+ "], goodType=[" + goodType + "], commodityCurrency=[" + commodityCurrency + "], property=[" + property
				+ "]";
	}
}
