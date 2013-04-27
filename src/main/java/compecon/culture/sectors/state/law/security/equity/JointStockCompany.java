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

package compecon.culture.sectors.state.law.security.equity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import compecon.culture.markets.SettlementMarket.ISettlementEvent;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.property.IProperty;
import compecon.culture.sectors.state.law.property.IPropertyOwner;
import compecon.culture.sectors.state.law.property.PropertyRegister;
import compecon.engine.Agent;
import compecon.engine.MarketFactory;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.HourType;
import compecon.engine.time.calendar.MonthType;
import compecon.nature.materia.GoodType;

/**
 * Joint-stock companies are owned by agents and pay dividends to them every
 * period.
 */
@Entity
public abstract class JointStockCompany extends Agent {

	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "JointStockCompany_IssuedShares")
	protected Set<Share> issuedShares = new HashSet<Share>();

	@Transient
	protected double MONEY_TO_RETAIN = 0;

	@Override
	public void initialize() {
		super.initialize();

		// offer shares at 00:00
		// has to happen as an event, as at constructor time the transaction
		// bank account is null
		ITimeSystemEvent offerSharesEvent = new OfferSharesEvent();
		this.timeSystemEvents.add(offerSharesEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				offerSharesEvent, -1, MonthType.EVERY, DayType.EVERY,
				HourType.HOUR_00);

		// pay dividend at 00:00
		ITimeSystemEvent payDividendEvent = new PayDividendEvent();
		this.timeSystemEvents.add(payDividendEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				payDividendEvent, -1, MonthType.EVERY, DayType.EVERY,
				HourType.HOUR_00);
	}

	/*
	 * Accessors
	 */

	public Set<Share> getIssuedShares() {
		return issuedShares;
	}

	public void setIssuedShares(Set<Share> issuedShares) {
		this.issuedShares = issuedShares;
	}

	/*
	 * Business logic
	 */

	@Transient
	protected abstract double calculateTotalDividend();

	@Transient
	protected void payDividend() {
		if (this.issuedShares.size() > 0) {
			double totalDividend = this.calculateTotalDividend();
			if (totalDividend > 0) {
				double dividendPerOwner = totalDividend
						/ this.issuedShares.size();

				Currency currency = this.transactionsBankAccount.getCurrency();

				for (Share share : this.issuedShares) {
					IPropertyOwner owner = PropertyRegister.getInstance()
							.getPropertyOwner(share);
					if (owner != null && owner instanceof IShareOwner) {
						this.transactionsBankAccount
								.getManagingBank()
								.transferMoney(
										this.transactionsBankAccount,
										((IShareOwner) owner)
												.getDividendBankAccount(currency),
										dividendPerOwner,
										this.bankPasswords
												.get(this.primaryBank),
										"dividend");
					}
				}
			}
		}
	}

	private class SettlementMarketEvent implements ISettlementEvent {
		@Override
		public void onEvent(GoodType goodType, double amount,
				double pricePerUnit) {
		}

		@Override
		public void onEvent(IProperty property, double amount,
				double pricePerUnit) {
		}
	}

	private class PayDividendEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			JointStockCompany.this.payDividend();
		}
	}

	private class OfferSharesEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			if (JointStockCompany.this.issuedShares.size() == 0) {
				// issue 10 shares
				for (int i = 0; i < 3; i++) {
					Share initialShare = new Share(JointStockCompany.this);
					PropertyRegister.getInstance().register(
							JointStockCompany.this, initialShare);
					JointStockCompany.this.issuedShares.add(initialShare);
					MarketFactory
							.getInstance(
									JointStockCompany.this.transactionsBankAccount
											.getCurrency())
							.placeSettlementSellingOffer(
									initialShare,
									JointStockCompany.this,
									JointStockCompany.this.transactionsBankAccount,
									1, 0, new SettlementMarketEvent());
				}
			}
		}
	}
}
