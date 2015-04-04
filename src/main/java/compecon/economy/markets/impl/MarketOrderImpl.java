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

package compecon.economy.markets.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.economy.agent.impl.AgentImpl;
import compecon.economy.markets.MarketOrder;
import compecon.economy.markets.MarketParticipant;
import compecon.economy.materia.GoodType;
import compecon.economy.property.Property;
import compecon.economy.property.impl.PropertyImpl;
import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.sectors.financial.Currency;

/**
 * http://en.wikipedia.org/wiki/Order_%28exchange%29
 */
@Entity
@Table(name = "MarketOrder")
@org.hibernate.annotations.Table(appliesTo = "MarketOrder", indexes = {
		@Index(name = "IDX_MO_GP", columnNames = { "goodType", "pricePerUnit" }),
		@Index(name = "IDX_MO_CP", columnNames = { "commodityCurrency",
				"pricePerUnit" }) })
public class MarketOrderImpl implements MarketOrder, Comparable<MarketOrder> {

	@Column(name = "amount")
	protected double amount;

	@Column(name = "commodityCurrency")
	@Enumerated(EnumType.STRING)
	protected Currency commodityCurrency;

	@Transient
	protected BankAccountDelegate commodityCurrencyOfferorsBankAcountDelegate;

	@Enumerated(EnumType.STRING)
	@Column(name = "currency")
	@Index(name = "IDX_MO_CURRENCY")
	protected Currency currency;

	@Column
	@Enumerated(EnumType.STRING)
	protected GoodType goodType;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	protected int id;

	@ManyToOne(targetEntity = AgentImpl.class)
	@JoinColumn(name = "offeror_id")
	protected MarketParticipant offeror;

	// market offer type 1: market offer for good type

	@Transient
	protected BankAccountDelegate offerorsBankAcountDelegate;

	// market offer type 2: market offer for currency / money

	@Column(name = "pricePerUnit")
	protected double pricePerUnit;

	@ManyToOne(targetEntity = PropertyImpl.class)
	@Index(name = "IDX_MO_PROPERTY")
	@JoinColumn(name = "property_id")
	protected Property property;

	// market offer type 3: market offer for property (e.g. shares)

	@Column
	@Enumerated(EnumType.STRING)
	protected ValidityPeriod validityPeriod;

	// accessors

	@Override
	@Transient
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
		// important, so that two market offers with same price can exists
		return hashCode() - marketOrder.hashCode();
	}

	@Override
	@Transient
	public void decrementAmount(final double amount) {
		this.amount -= amount;
	}

	@Override
	public double getAmount() {
		return amount;
	}

	@Override
	@Transient
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
	 * the currency to be traded as the commodity; not the currency of the
	 * offeror's bank account!
	 */
	@Override
	public Currency getCommodityCurrency() {
		return commodityCurrency;
	}

	@Override
	@Transient
	public BankAccountDelegate getCommodityCurrencyOfferorsBankAccountDelegate() {
		return commodityCurrencyOfferorsBankAcountDelegate;
	}

	@Override
	@Transient
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

	public int getId() {
		return id;
	}

	@Override
	public MarketParticipant getOfferor() {
		return offeror;
	}

	@Override
	@Transient
	public BankAccountDelegate getOfferorsBankAcountDelegate() {
		return offerorsBankAcountDelegate;
	}

	@Override
	public double getPricePerUnit() {
		return pricePerUnit;
	}

	@Transient
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

	@Transient
	public void setCommodityCurrencyOfferorsBankAccountDelegate(
			final BankAccountDelegate bankAccountDelegate) {
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

	/*
	 * business logic
	 */

	public void setOfferor(final MarketParticipant offeror) {
		this.offeror = offeror;
	}

	@Transient
	public void setOfferorsBankAcountDelegate(
			final BankAccountDelegate offerorsBankAcountDelegate) {
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
		return "id=[" + id + "], currency=[" + currency + "], amount=["
				+ amount + "], pricePerUnit=[" + pricePerUnit + "], goodType=["
				+ goodType + "], commodityCurrency=[" + commodityCurrency
				+ "], property=[" + property + "]";
	}
}
