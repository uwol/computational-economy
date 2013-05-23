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

package compecon.culture.sectors.trading;

import javax.persistence.Entity;
import javax.persistence.Transient;

import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.security.equity.JointStockCompany;
import compecon.engine.MarketFactory;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.HourType;
import compecon.engine.time.calendar.MonthType;
import compecon.nature.materia.GoodType;

/**
 * Agent type Trader buys and sells goods
 */
@Entity
public class Trader extends JointStockCompany {

	@Transient
	protected final int MAX_CREDIT = 10000;

	@Override
	public void initialize() {
		super.initialize();

		// trading event every hour
		ITimeSystemEvent arbitrageTradingEvent = new ArbitrageTradingEvent();
		this.timeSystemEvents.add(arbitrageTradingEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				arbitrageTradingEvent, -1, MonthType.EVERY, DayType.EVERY,
				HourType.EVERY);

		// balance sheet publication
		ITimeSystemEvent balanceSheetPublicationEvent = new BalanceSheetPublicationEvent();
		this.timeSystemEvents.add(balanceSheetPublicationEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				balanceSheetPublicationEvent, -1, MonthType.EVERY,
				DayType.EVERY, BALANCE_SHEET_PUBLICATION_HOUR_TYPE);
	}

	protected class ArbitrageTradingEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			Trader.this.assertTransactionsBankAccount();

			for (GoodType goodType : GoodType.values()) {
				double localPrice = MarketFactory.getInstance()
						.getMarginalPrice(Trader.this.getPrimaryCurrency(),
								goodType);

				for (Currency foreignCurrency : Currency.values()) {
					if (foreignCurrency != Trader.this.getPrimaryCurrency()) {
						double foreignPriceInForeignCurrency = MarketFactory
								.getInstance().getMarginalPrice(
										foreignCurrency, goodType);
						/*
						 * double foreignPriceInLocalCurrency =
						 * foreignPriceInForeignCurrency exchangeRate;
						 * 
						 * 
						 * if(foreignPriceInLocalCurrency < localPrice){
						 * MarketFactory.getInstance().buy(goodType, currency,
						 * maxAmount, maxTotalPrice, maxPricePerUnit, buyer,
						 * buyersBankAccount, buyersBankAccountPassword) } else
						 * if(localPrice < foreignPriceInLocalCurrency){
						 * 
						 * }
						 */
					}
				}
			}
		}
	}

	protected class BalanceSheetPublicationEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			Trader.this.assertTransactionsBankAccount();
			/*
			 * Log.agent_onPublishBalanceSheet(Trader.this,
			 * Trader.this.issueBasicBalanceSheet());
			 */
		}
	}
}
