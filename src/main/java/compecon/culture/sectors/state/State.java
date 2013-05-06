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

package compecon.culture.sectors.state;

import javax.persistence.Entity;
import javax.persistence.Transient;

import compecon.culture.sectors.financial.CreditBank;
import compecon.engine.Agent;
import compecon.engine.AgentFactory;
import compecon.engine.Log;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.MonthType;

@Entity
public class State extends Agent {

	@Override
	public void initialize() {
		super.initialize();

		// balance sheet publication
		ITimeSystemEvent balanceSheetPublicationEvent = new BalanceSheetPublicationEvent();
		this.timeSystemEvents.add(balanceSheetPublicationEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				balanceSheetPublicationEvent, -1, MonthType.EVERY,
				DayType.EVERY, BALANCE_SHEET_PUBLICATION_HOUR_TYPE);
	}

	/*
	 * Accessors
	 */

	/*
	 * Business logic
	 */

	@Transient
	public void doDeficitSpending() {
		this.assertTransactionsBankAccount();
		for (CreditBank creditBank : AgentFactory
				.getAllCreditBanks(this.primaryCurrency)) {
			for (Agent agent : creditBank.getCustomers()) {
				if (agent != this) {
					this.primaryBank.transferMoney(transactionsBankAccount,
							agent.getTransactionsBankAccount(), 100,
							this.bankPasswords.get(this.primaryBank),
							"deficit spending");
				}
			}
		}
	}

	protected class BalanceSheetPublicationEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			State.this.assertTransactionsBankAccount();
			Log.agent_onPublishBalanceSheet(State.this,
					State.this.issueBasicBalanceSheet());
		}
	}
}
