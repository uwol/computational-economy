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

package compecon.economy.sectors.state;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import compecon.economy.PricingBehaviour;
import compecon.economy.markets.SettlementMarket.ISettlementEvent;
import compecon.economy.sectors.Agent;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.economy.sectors.state.law.property.Property;
import compecon.economy.sectors.state.law.property.PropertyRegister;
import compecon.economy.sectors.state.law.security.debt.Bond;
import compecon.economy.sectors.state.law.security.debt.FixedRateBond;
import compecon.engine.AgentFactory;
import compecon.engine.MarketFactory;
import compecon.engine.PropertyFactory;
import compecon.engine.Simulation;
import compecon.engine.dao.DAOFactory;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.HourType;
import compecon.engine.time.calendar.MonthType;
import compecon.engine.util.MathUtil;
import compecon.materia.GoodType;
import compecon.math.price.IPriceFunction;
import compecon.math.utility.IUtilityFunction;

@Entity
public class State extends Agent {

	@OneToMany
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
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(balanceSheetPublicationEvent, -1, MonthType.EVERY,
						DayType.EVERY, BALANCE_SHEET_PUBLICATION_HOUR_TYPE);

		// offer bonds
		ITimeSystemEvent offerBondsEvent = new OfferBondsEvent();
		this.timeSystemEvents.add(offerBondsEvent);
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(offerBondsEvent, -1, MonthType.EVERY, DayType.EVERY,
						HourType.HOUR_12);

		double initialInterestRate = AgentFactory.getInstanceCentralBank(
				primaryCurrency).getEffectiveKeyInterestRate() + 0.02;
		this.pricingBehaviour = new PricingBehaviour(this, FixedRateBond.class,
				this.primaryCurrency, initialInterestRate);
	}

	@Transient
	public void deconstruct() {
		super.deconstruct();

		for (Bond bond : this.issuedBonds) {
			PropertyFactory.deleteProperty(bond);
		}
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

			BalanceSheet balanceSheet = State.this.issueBasicBalanceSheet();

			// --------------

			// list issued bonds on balance sheet
			Set<Bond> bondsToDelete = new HashSet<Bond>();
			for (Bond bond : State.this.issuedBonds) {
				if (bond.isDeconstructed()) {
					bondsToDelete.add(bond);
				} else if (!bond.getOwner().equals(State.this)) {
					balanceSheet.financialLiabilities += bond.getFaceValue();
				}
			}
			State.this.issuedBonds.removeAll(bondsToDelete);

			// publish
			getLog().agent_onPublishBalanceSheet(State.this, balanceSheet);
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

			// bonds that have not been sold by definition have to be issued AND
			// owned by this agent
			for (Property property : PropertyRegister.getInstance()
					.getProperties(State.this, FixedRateBond.class)) {
				FixedRateBond bond = (FixedRateBond) property;
				assert (bond.getOwner() == State.this);
				if (bond.getIssuerBankAccount().getOwner() == State.this) {
					bond.deconstruct();
					State.this.issuedBonds.remove(bond);
					PropertyFactory.deleteProperty(bond);
				}
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
						State.this, State.this.primaryCurrency,
						State.this.transactionsBankAccount, faceValuePerBond,
						State.this.pricingBehaviour.getCurrentPrice());
				State.this.issuedBonds.add(bond);
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
			Map<GoodType, IPriceFunction> priceFunctions = MarketFactory
					.getInstance().getFixedPriceFunctions(
							State.this.transactionsBankAccount.getCurrency(),
							State.this.utilityFunction.getInputGoodTypes());
			double budget = State.this.transactionsBankAccount.getBalance();
			if (MathUtil.greater(budget, 0)) {
				Map<GoodType, Double> optimalBundleOfGoods = State.this.utilityFunction
						.calculateUtilityMaximizingInputs(priceFunctions,
								budget);

				for (Entry<GoodType, Double> entry : optimalBundleOfGoods
						.entrySet()) {
					GoodType goodType = entry.getKey();
					double maxAmount = entry.getValue();
					MarketFactory.getInstance().buy(goodType, maxAmount,
							Double.NaN, Double.NaN, State.this,
							State.this.transactionsBankAccount);
				}
			}

			/*
			 * consume bought goods
			 */
			for (GoodType goodType : GoodType.values()) {
				PropertyRegister.getInstance().resetGoodTypeAmount(State.this,
						goodType);
			}
		}
	}
}
