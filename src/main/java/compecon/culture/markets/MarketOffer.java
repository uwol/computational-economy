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

package compecon.culture.markets;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.Currency;
import compecon.engine.Agent;

@MappedSuperclass
public abstract class MarketOffer implements Comparable<MarketOffer> {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	protected int id;

	@ManyToOne
	@JoinColumn(name = "offeror_id")
	protected Agent offeror;

	@Column(name = "primaryCurrency")
	@Enumerated(EnumType.STRING)
	@Index(name = "IDX_PRIMARYCURRENCY")
	protected Currency currency;

	@ManyToOne
	@JoinColumn(name = "offerorsBankAcount_id")
	@Index(name = "IDX_OFFERORSBANKACCOUNT_ID")
	protected BankAccount offerorsBankAcount;

	@Column(name = "pricePerUnit")
	protected double pricePerUnit;

	// accessors

	public int getId() {
		return id;
	}

	public Agent getOfferor() {
		return offeror;
	}

	public Currency getCurrency() {
		return currency;
	}

	public BankAccount getOfferorsBankAcount() {
		return offerorsBankAcount;
	}

	public double getPricePerUnit() {
		return pricePerUnit;
	}

	public void setId(int id) {
		this.id = id;
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

	// transient

	@Override
	@Transient
	public int compareTo(MarketOffer marketOffer) {
		if (this == marketOffer)
			return 0;
		if (this.getPricePerUnit() > marketOffer.getPricePerUnit())
			return 1;
		if (this.getPricePerUnit() < marketOffer.getPricePerUnit())
			return -1;
		// important, so that two market offers with same price can exists
		return this.hashCode() - marketOffer.hashCode();
	}
}
