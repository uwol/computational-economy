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
import compecon.culture.sectors.state.law.property.Property;
import compecon.culture.sectors.state.law.property.PropertyRegister;
import compecon.engine.Agent;
import compecon.engine.MarketFactory;
import compecon.engine.PropertyFactory;
import compecon.engine.jmx.Log;
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

	@Transient
	protected final int INITIAL_NUMBER_OF_SHARES = 100;

	@Override
	public void initialize() {
		super.initialize();

		// offer shares at 00:00î
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

	public int getInitialNumberOfShares() {
		return INITIAL_NUMBER_OF_SHARES;
	}

	public void setIssuedShares(Set<Share> issuedShares) {
		this.issuedShares = issuedShares;
	}

	/*
	 * Business logic
	 */

	@Transient
	protected double calculateTotalDividend() {
		this.assureTransactionsBankAccount();

		return Math.max(0.0, this.transactionsBankAccount.getBalance()
				- MONEY_TO_RETAIN);
	}

	public class SettlementMarketEvent implements ISettlementEvent {
		@Override
		public void onEvent(GoodType goodType, double amount,
				double pricePerUnit, Currency currency) {
		}

		@Override
		public void onEvent(Currency commodityCurrency, double amount,
				double pricePerUnit, Currency currency) {
		}

		@Override
		public void onEvent(Property property, double pricePerUnit,
				Currency currency) {
		}
	}

	public class PayDividendEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			if (JointStockCompany.this.issuedShares.size() > 0) {
				JointStockCompany.this.assureTransactionsBankAccount();

				double totalDividend = JointStockCompany.this
						.calculateTotalDividend();

				// dividend to be payed?
				if (totalDividend > 0) {
					double totalDividendPayed = 0;

					Currency currency = JointStockCompany.this.transactionsBankAccount
							.getCurrency();
					double dividendPerOwner = totalDividend
							/ JointStockCompany.this.issuedShares.size();

					// pay dividend for each share
					for (Share share : JointStockCompany.this.issuedShares) {
						Agent owner = PropertyRegister.getInstance().getOwner(
								share);
						if (owner != null && owner != JointStockCompany.this) {
							if (owner instanceof IShareOwner) {
								IShareOwner shareOwner = (IShareOwner) owner;
								shareOwner.assureDividendBankAccount();
								if (currency
										.equals(shareOwner
												.getDividendBankAccount()
												.getCurrency())) {
									JointStockCompany.this.transactionsBankAccount
											.getManagingBank()
											.transferMoney(
													JointStockCompany.this.transactionsBankAccount,
													((IShareOwner) owner)
															.getDividendBankAccount(),
													dividendPerOwner,
													JointStockCompany.this.bankPasswords
															.get(JointStockCompany.this.primaryBank),
													"dividend");
									((IShareOwner) owner)
											.onDividendTransfer(dividendPerOwner);
									totalDividendPayed += dividendPerOwner;
								}
							}
						}
					}
					if (Log.isAgentSelectedByClient(JointStockCompany.this)) {
						Log.log(JointStockCompany.this,
								PayDividendEvent.class,
								"payed dividend of "
										+ Currency.round(totalDividendPayed)
										+ " "
										+ JointStockCompany.this.transactionsBankAccount
												.getCurrency().getIso4217Code());
					}
				}
			}
		}
	}

	public class OfferSharesEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			if (JointStockCompany.this.issuedShares.size() == 0) {
				JointStockCompany.this.assureTransactionsBankAccount();

				// issue initial shares
				for (int i = 0; i < INITIAL_NUMBER_OF_SHARES; i++) {
					Share initialShare = PropertyFactory.newInstanceShare(
							JointStockCompany.this, JointStockCompany.this);
					JointStockCompany.this.issuedShares.add(initialShare);
					MarketFactory.getInstance().placeSettlementSellingOffer(
							initialShare, JointStockCompany.this,
							JointStockCompany.this.transactionsBankAccount, 0,
							new SettlementMarketEvent());
				}
			}
		}
	}
}
