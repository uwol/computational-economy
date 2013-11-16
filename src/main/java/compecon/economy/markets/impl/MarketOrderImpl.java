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

import compecon.economy.agent.Agent;
import compecon.economy.agent.impl.AgentImpl;
import compecon.economy.markets.MarketOrder;
import compecon.economy.property.Property;
import compecon.economy.property.impl.PropertyImpl;
import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.sectors.financial.Currency;
import compecon.materia.GoodType;

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

	@Enumerated(EnumType.STRING)
	@Column(name = "currency")
	@Index(name = "IDX_MO_CURRENCY")
	protected Currency currency;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	protected int id;

	@ManyToOne(targetEntity = AgentImpl.class)
	@JoinColumn(name = "offeror_id")
	protected Agent offeror;

	@Transient
	protected BankAccountDelegate offerorsBankAcountDelegate;

	@Column(name = "pricePerUnit")
	protected double pricePerUnit;

	@Column
	@Enumerated(EnumType.STRING)
	protected ValidityPeriod validityPeriod;

	// market offer type 1: market offer for good type

	@Column
	@Enumerated(EnumType.STRING)
	protected GoodType goodType;

	// market offer type 2: market offer for currency / money

	@Column(name = "commodityCurrency")
	@Enumerated(EnumType.STRING)
	protected Currency commodityCurrency;

	@Transient
	protected BankAccountDelegate commodityCurrencyOfferorsBankAcountDelegate;

	// market offer type 3: market offer for property (e.g. shares)

	@ManyToOne(targetEntity = PropertyImpl.class)
	@Index(name = "IDX_MO_PROPERTY")
	@JoinColumn(name = "property_id")
	protected Property property;

	// accessors

	public double getAmount() {
		return amount;
	}

	public Currency getCurrency() {
		return this.currency;
	}

	public int getId() {
		return id;
	}

	public Agent getOfferor() {
		return offeror;
	}

	/**
	 * the currency to be traded as the commodity; not the currency of the
	 * offeror's bank account!
	 */
	public Currency getCommodityCurrency() {
		return commodityCurrency;
	}

	@Transient
	public BankAccountDelegate getCommodityCurrencyOfferorsBankAccountDelegate() {
		return this.commodityCurrencyOfferorsBankAcountDelegate;
	}

	public GoodType getGoodType() {
		return goodType;
	}

	@Transient
	public BankAccountDelegate getOfferorsBankAcountDelegate() {
		return offerorsBankAcountDelegate;
	}

	public double getPricePerUnit() {
		return pricePerUnit;
	}

	public Property getProperty() {
		return property;
	}

	public ValidityPeriod getValidityPeriod() {
		return validityPeriod;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public void setCurrency(final Currency currency) {
		this.currency = currency;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setGoodType(GoodType goodType) {
		this.goodType = goodType;
	}

	public void setOfferor(Agent offeror) {
		this.offeror = offeror;
	}

	/**
	 * @see #getCommodityCurrency()
	 */
	public void setCommodityCurrency(Currency currency) {
		this.commodityCurrency = currency;
	}

	@Transient
	public void setCommodityCurrencyOfferorsBankAccountDelegate(
			BankAccountDelegate bankAccountDelegate) {
		this.commodityCurrencyOfferorsBankAcountDelegate = bankAccountDelegate;
	}

	@Transient
	public void setOfferorsBankAcountDelegate(
			BankAccountDelegate offerorsBankAcountDelegate) {
		this.offerorsBankAcountDelegate = offerorsBankAcountDelegate;
	}

	public void setPricePerUnit(double pricePerUnit) {
		this.pricePerUnit = pricePerUnit;
	}

	public void setProperty(Property property) {
		this.property = property;
	}

	public void setValidityPeriod(ValidityPeriod validityPeriod) {
		this.validityPeriod = validityPeriod;
	}

	/*
	 * business logic
	 */

	@Override
	@Transient
	public int compareTo(MarketOrder marketOrder) {
		if (this == marketOrder)
			return 0;
		if (this.getPricePerUnit() > marketOrder.getPricePerUnit())
			return 1;
		if (this.getPricePerUnit() < marketOrder.getPricePerUnit())
			return -1;
		// important, so that two market offers with same price can exists
		return this.hashCode() - marketOrder.hashCode();
	}

	@Transient
	public double getPriceTotal() {
		return this.pricePerUnit * this.getAmount();
	}

	@Transient
	public void decrementAmount(double amount) {
		this.amount -= amount;
	}

	@Transient
	public CommodityType getCommodityType() {
		if (this.commodityCurrency != null)
			return CommodityType.CURRENCY;
		if (this.property != null)
			return CommodityType.PROPERTY;
		return CommodityType.GOODTYPE;
	}

	@Transient
	public Object getCommodity() {
		if (CommodityType.CURRENCY.equals(getCommodityType()))
			return this.commodityCurrency;
		if (CommodityType.PROPERTY.equals(getCommodityType()))
			return this.property;
		return this.goodType;
	}
}
