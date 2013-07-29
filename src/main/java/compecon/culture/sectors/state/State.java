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

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import compecon.culture.PricingBehaviour;
import compecon.culture.markets.SettlementMarket.ISettlementEvent;
import compecon.culture.sectors.financial.BankAccount;
import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.state.law.property.Property;
import compecon.culture.sectors.state.law.property.PropertyRegister;
import compecon.culture.sectors.state.law.security.debt.Bond;
import compecon.culture.sectors.state.law.security.debt.FixedRateBond;
import compecon.engine.Agent;
import compecon.engine.AgentFactory;
import compecon.engine.MarketFactory;
import compecon.engine.PropertyFactory;
import compecon.engine.dao.DAOFactory;
import compecon.engine.jmx.Log;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.HourType;
import compecon.engine.time.calendar.MonthType;
import compecon.engine.util.MathUtil;
import compecon.nature.materia.GoodType;
import compecon.nature.math.utility.IUtilityFunction;

@Entity
public class State extends Agent {

	@OneToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "State_IssuedBonds", joinColumns = @JoinColumn(name = "state_id"), inverseJoinColumns = @JoinColumn(name = "bond_id"))
	protected Set<Bond> issuedBonds = new HashSet<Bond>();

	@Transient
	protected PricingBehaviour pricingBehaviour;

	@Transient
	protected IUtilityFunction utilityFunction;

	@Override
	public void initialize() {
		super.initialize();

		// balance sheet publication
		ITimeSystemEvent balanceSheetPublicationEvent = new BalanceSheetPublicationEvent();
		this.timeSystemEvents.add(balanceSheetPublicationEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				balanceSheetPublicationEvent, -1, MonthType.EVERY,
				DayType.EVERY, BALANCE_SHEET_PUBLICATION_HOUR_TYPE);

		// offer bonds
		ITimeSystemEvent offerBondsEvent = new OfferBondsEvent();
		this.timeSystemEvents.add(offerBondsEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(offerBondsEvent,
				-1, MonthType.EVERY, DayType.EVERY, HourType.HOUR_12);

		double initialInterestRate = AgentFactory.getInstanceCentralBank(
				primaryCurrency).getEffectiveKeyInterestRate() + 0.02;
		this.pricingBehaviour = new PricingBehaviour(this, FixedRateBond.class,
				this.primaryCurrency, initialInterestRate);
	}

	/*
	 * accessors
	 */

	public IUtilityFunction getUtilityFunction() {
		return utilityFunction;
	}

	public void setUtilityFunction(IUtilityFunction utilityFunction) {
		this.utilityFunction = utilityFunction;
	}

	/*
	 * Business logic
	 */

	@Transient
	public void doDeficitSpending() {
		this.assureTransactionsBankAccount();

		for (CreditBank creditBank : AgentFactory
				.getAllCreditBanks(this.primaryCurrency)) {
			for (BankAccount bankAccount : DAOFactory.getBankAccountDAO()
					.findAllBankAccountsManagedByBank(creditBank)) {
				if (bankAccount.getOwner() != this) {
					this.primaryBank.transferMoney(transactionsBankAccount,
							bankAccount, 5000,
							this.bankPasswords.get(this.primaryBank),
							"deficit spending");
				}
			}
		}
	}

	protected class SettlementMarketEvent implements ISettlementEvent {
		@Override
		public void onEvent(GoodType goodType, double amount,
				double pricePerUnit, Currency currency) {
		}

		@Override
		public void onEvent(Currency commodityCurrency, double amount,
				double pricePerUnit, Currency currency) {
		}

		@Override
		public void onEvent(Property property, double totalPrice,
				Currency currency) {
			if (property instanceof FixedRateBond) {
				State.this.pricingBehaviour.registerSelling(
						((FixedRateBond) property).getFaceValue(), totalPrice);
			}
		}
	}

	public class BalanceSheetPublicationEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			State.this.assureTransactionsBankAccount();

			Log.agent_onPublishBalanceSheet(State.this,
					State.this.issueBasicBalanceSheet());
		}
	}

	public class OfferBondsEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			State.this.assureTransactionsBankAccount();

			State.this.pricingBehaviour.nextPeriod();

			/*
			 * destroy bonds, that have been offered, but not sold in last
			 * periods
			 */
			MarketFactory.getInstance().removeAllSellingOffers(State.this,
					State.this.primaryCurrency, FixedRateBond.class);

			for (Property property : PropertyRegister.getInstance()
					.getProperties(State.this, FixedRateBond.class)) {
				PropertyRegister.getInstance().deregisterProperty(State.this,
						property);
				((FixedRateBond) property).deconstruct();
			}

			/*
			 * issue new bonds
			 */
			int numberOfBondsToIsue = 10;
			double totalFaceValueToBeOffered = Math
					.max(100, (int) State.this.pricingBehaviour
							.getLastSoldAmount() * 10.0);
			double faceValuePerBond = totalFaceValueToBeOffered
					/ (double) numberOfBondsToIsue;

			for (int i = 0; i < numberOfBondsToIsue; i++) {
				FixedRateBond bond = PropertyFactory.newInstanceFixedRateBond(
						State.this.primaryCurrency,
						State.this.transactionsBankAccount,
						State.this.bankPasswords
								.get(State.this.transactionsBankAccount
										.getManagingBank()), faceValuePerBond,
						State.this.pricingBehaviour.getCurrentPrice());
				PropertyRegister.getInstance().registerProperty(State.this,
						bond);
			}

			/*
			 * offer bonds
			 */
			for (Property property : PropertyRegister.getInstance()
					.getProperties(State.this, FixedRateBond.class)) {
				MarketFactory.getInstance().placeSettlementSellingOffer(
						(FixedRateBond) property, State.this,
						State.this.transactionsBankAccount,
						((FixedRateBond) property).getFaceValue(),
						new SettlementMarketEvent());
			}

			/*
			 * buy goods for sold bonds -> no hoarding of money
			 */
			Map<GoodType, Double> prices = MarketFactory.getInstance()
					.getMarginalPrices(
							State.this.transactionsBankAccount.getCurrency());
			double budget = State.this.transactionsBankAccount.getBalance();
			if (MathUtil.greater(budget, 0)) {
				Map<GoodType, Double> optimalBundleOfGoods = State.this.utilityFunction
						.calculateUtilityMaximizingInputsUnderBudgetRestriction(
								prices, budget);

				for (Entry<GoodType, Double> entry : optimalBundleOfGoods
						.entrySet()) {
					GoodType goodType = entry.getKey();
					double maxAmount = entry.getValue();
					double maxTotalPrice = State.this.transactionsBankAccount
							.getBalance();
					if (!Double.isInfinite(maxAmount)) {
						maxTotalPrice = Math
								.min(maxAmount * prices.get(goodType),
										State.this.transactionsBankAccount
												.getBalance());
					}
					MarketFactory.getInstance().buy(
							goodType,
							maxAmount,
							maxTotalPrice,
							prices.get(goodType),
							State.this,
							State.this.transactionsBankAccount,
							State.this.bankPasswords
									.get(State.this.transactionsBankAccount
											.getManagingBank()));
				}
			}
		}
	}
}
