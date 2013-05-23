/*
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

package compecon.culture.markets.ordertypes;

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

import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.property.Property;
import compecon.engine.Agent;
import compecon.nature.materia.GoodType;

@Entity
@Table(name = "MarketOrder")
@org.hibernate.annotations.Table(appliesTo = "MarketOrder", indexes = {
		@Index(name = "IDX_MO_CURRENCY", columnNames = "currency"),
		@Index(name = "IDX_MO_OFFERORSBANKACCOUNT", columnNames = "offerorsBankAcount_id"),
		@Index(name = "IDX_MO_CP_MARGINALPRICE", columnNames = { "currency",
				"pricePerUnit" }),
		@Index(name = "IDX_MO_CGP_MARGINALPRICE", columnNames = { "currency",
				"goodType", "pricePerUnit" }) })
public class MarketOrder implements Comparable<MarketOrder> {

	@Column(name = "amount")
	protected double amount;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	protected int id;

	@ManyToOne
	@JoinColumn(name = "offeror_id")
	protected Agent offeror;

	@Column(name = "currency")
	@Enumerated(EnumType.STRING)
	protected Currency currency;

	@Column
	@Enumerated(EnumType.STRING)
	protected GoodType goodType;

	@ManyToOne
	@JoinColumn(name = "offerorsBankAcount_id")
	protected BankAccount offerorsBankAcount;

	@Column(name = "pricePerUnit")
	protected double pricePerUnit;

	@ManyToOne
	@Index(name = "IDX_MO_PROPERTY")
	@JoinColumn(name = "property_id")
	protected Property property;

	@Column
	@Enumerated(EnumType.STRING)
	protected ValidityPeriod validityPeriod;

	// accessors

	public double getAmount() {
		return amount;
	}

	public int getId() {
		return id;
	}

	public Agent getOfferor() {
		return offeror;
	}

	public Currency getCurrency() {
		return currency;
	}

	public GoodType getGoodType() {
		return goodType;
	}

	public BankAccount getOfferorsBankAcount() {
		return offerorsBankAcount;
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

	public void setId(int id) {
		this.id = id;
	}

	public void setGoodType(GoodType goodType) {
		this.goodType = goodType;
	}

	public void setOfferor(Agent offeror) {
		this.offeror = offeror;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public void setOfferorsBankAcount(BankAccount offerorsBankAcount) {
		this.offerorsBankAcount = offerorsBankAcount;
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

	// transient

	@Override
	@Transient
	public int compareTo(MarketOrder marketOffer) {
		if (this == marketOffer)
			return 0;
		if (this.getPricePerUnit() > marketOffer.getPricePerUnit())
			return 1;
		if (this.getPricePerUnit() < marketOffer.getPricePerUnit())
			return -1;
		// important, so that two market offers with same price can exists
		return this.hashCode() - marketOffer.hashCode();
	}

	@Transient
	public double getPriceTotal() {
		return this.pricePerUnit * this.getAmount();
	}

	@Transient
	public void decrementAmount(double amount) {
		this.amount -= amount;
	}

	public enum ValidityPeriod {
		GoodForDay, GoodTillDate, GoodTillCancelled
	}

}
