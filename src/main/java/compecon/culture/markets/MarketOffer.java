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

import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.property.IProperty;
import compecon.engine.Agent;

public class MarketOffer implements Comparable<MarketOffer> {
	protected static int lastId = 0;

	protected int id = 0;

	protected Agent offeror;
	protected Currency currency;
	protected BankAccount offerorsBankAcount;
	protected double amount;
	protected double pricePerUnit;
	protected IProperty property;

	public MarketOffer(Agent offeror, Currency currency,
			BankAccount offerorsBankAcount, IProperty property,
			double pricePerUnit, double amount) {

		this.id = lastId++;

		this.offeror = offeror;
		this.currency = currency;
		this.offerorsBankAcount = offerorsBankAcount;
		this.pricePerUnit = pricePerUnit;
		this.amount = amount;
		this.property = property;
	}

	public int getId() {
		return this.id;
	}

	public Agent getOfferor() {
		return this.offeror;
	}

	public Currency getCurrency() {
		return this.currency;
	}

	public BankAccount getOfferorsBankAcount() {
		return this.offerorsBankAcount;
	}

	public double getAmount() {
		return this.amount;
	}

	public double getPriceTotal() {
		return this.pricePerUnit * this.getAmount();
	}

	public IProperty getProperty() {
		return this.property;
	}

	public double getPricePerUnit() {
		return this.pricePerUnit;
	}

	public void decrementAmount(double amount) {
		this.amount -= amount;
	}

	@Override
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
