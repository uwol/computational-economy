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
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.nature.materia.GoodType;

@Entity
@Table(name = "GoodTypeMarketOffer")
@org.hibernate.annotations.Table(appliesTo = "GoodTypeMarketOffer", indexes = {
		@Index(name = "IDX_GTMO_CURRENCY", columnNames = "currency"),
		@Index(name = "IDX_GTMO_OFFERORSBANKACCOUNT", columnNames = "offerorsBankAcount_id"),
		@Index(name = "IDX_GTMO_MARGINALPRICE", columnNames = { "currency",
				"goodType", "pricePerUnit" }) })
public class GoodTypeMarketOffer extends MarketOffer {

	@Column(name = "amount")
	protected double amount;

	@Column
	@Enumerated(EnumType.STRING)
	protected GoodType goodType;

	// accessors

	public double getAmount() {
		return amount;
	}

	public GoodType getGoodType() {
		return goodType;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public void setGoodType(GoodType goodType) {
		this.goodType = goodType;
	}

	// transient

	@Transient
	public double getPriceTotal() {
		return this.pricePerUnit * this.getAmount();
	}

	@Transient
	public void decrementAmount(double amount) {
		this.amount -= amount;
	}

}
