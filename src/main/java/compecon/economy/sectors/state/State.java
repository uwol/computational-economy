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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.economy.PricingBehaviour;
import compecon.economy.markets.SettlementMarket.ISettlementEvent;
import compecon.economy.sectors.Agent;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
import compecon.economy.sectors.financial.CentralBank;
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
import compecon.engine.util.ConfigurationUtil;
import compecon.engine.util.MathUtil;
import compecon.materia.GoodType;
import compecon.math.price.IPriceFunction;
import compecon.math.utility.IUtilityFunction;

@Entity
public class State extends Agent {

	/**
	 * bank account for financing the coupon of issued bonds
	 */
	@OneToOne
	@JoinColumn(name = "bankAccountCouponLoans_id")
	@Index(name = "IDX_A_BA_COUPONLOANS")
	protected BankAccount bankAccountCouponLoans;

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

		// offer bonds
		final ITimeSystemEvent offerBondsEvent = new OfferBondsEvent();
		this.timeSystemEvents.add(offerBondsEvent);
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(offerBondsEvent, -1, MonthType.EVERY, DayType.EVERY,
						HourType.HOUR_12);

		/*
		 * buy and consume goods: has to happen every hour, so that not all
		 * money is spent on one distinct hour a day! else this would lead to
		 * significant distortions on markets, as the savings of the whole
		 * economy flow through the state via state bonds
		 */
		final ITimeSystemEvent buyAndConsumeGoodsEvent = new BuyAndConsumeGoodsEvent();
		this.timeSystemEvents.add(buyAndConsumeGoodsEvent);
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEventEvery(buyAndConsumeGoodsEvent, -1, MonthType.EVERY,
						DayType.EVERY, HourType.EVERY);

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

	public BankAccount getBankAccountCouponLoans() {
		return bankAccountCouponLoans;
	}

	public Set<Bond> getIssuedBonds() {
		return issuedBonds;
	}

	public IUtilityFunction getUtilityFunction() {
		return utilityFunction;
	}

	public void setBankAccountCouponLoans(BankAccount bankAccountCouponLoans) {
		this.bankAccountCouponLoans = bankAccountCouponLoans;
	}

	public void setIssuedBonds(Set<Bond> issuedBonds) {
		this.issuedBonds = issuedBonds;
	}

	public void setUtilityFunction(IUtilityFunction utilityFunction) {
		this.utilityFunction = utilityFunction;
	}

	/*
	 * assertions
	 */

	@Transient
	public void assureBankAccountCouponLoans() {
		if (this.isDeconstructed)
			return;

		this.assureBankCustomerAccount();

		// initialize bank account
		if (this.bankAccountCouponLoans == null) {
			this.bankAccountCouponLoans = this.primaryBank.openBankAccount(
					this, this.primaryCurrency, true, "loans",
					TermType.LONG_TERM, MoneyType.DEPOSITS);
		}
	}

	/*
	 * business logic
	 */

	@Transient
	public void doDeficitSpending() {
		this.assureBankAccountTransactions();

		for (CreditBank creditBank : AgentFactory
				.getAllCreditBanks(this.primaryCurrency)) {
			for (BankAccount bankAccount : DAOFactory.getBankAccountDAO()
					.findAllBankAccountsManagedByBank(creditBank)) {
				if (bankAccount.getOwner() != this) {
					this.primaryBank.transferMoney(bankAccountTransactions,
							bankAccount, 5000, "deficit spending");
				}
			}
		}
	}

	@Override
	@Transient
	protected BalanceSheet issueBalanceSheet() {
		final BalanceSheet balanceSheet = super.issueBalanceSheet();

		// bank deposits
		balanceSheet.addBankAccountBalance(this.bankAccountCouponLoans);

		// list issued bonds on balance sheet
		for (Bond bond : this.issuedBonds) {
			if (!bond.isDeconstructed() && !bond.getOwner().equals(this)) {
				balanceSheet.financialLiabilities += bond.getFaceValue();
			}
		}

		// remove deconstructed bonds
		final Set<Bond> bondsToDelete = new HashSet<Bond>();
		for (Bond bond : this.issuedBonds) {
			if (bond.isDeconstructed()) {
				bondsToDelete.add(bond);
			}
		}
		this.issuedBonds.removeAll(bondsToDelete);

		return balanceSheet;
	}

	@Transient
	private FixedRateBond issueNewFixedRateBond(final double faceValue) {
		this.assureBankAccountCouponLoans();

		// TODO alternative: price := this.pricingBehaviour.getCurrentPrice();
		final CentralBank centralBank = DAOFactory.getCentralBankDAO()
				.findByCurrency(this.primaryCurrency);
		final double coupon = centralBank.getEffectiveKeyInterestRate()
				+ ConfigurationUtil.StateConfig.getBondMargin();
		// coupons have to be payed from a separate bank account, so that bonds
		// can be re-bought with same face value after bond deconstruction
		final FixedRateBond bond = PropertyFactory.newInstanceFixedRateBond(
				this, this.primaryCurrency, this.bankAccountTransactions,
				this.bankAccountCouponLoans, faceValue, coupon);
		this.issuedBonds.add(bond);
		return bond;
	}

	@Transient
	public FixedRateBond obtainBond(final double faceValue,
			final BankAccount buyerBankAccount) {
		this.assureBankAccountCouponLoans();

		final FixedRateBond fixedRateBond = issueNewFixedRateBond(faceValue);
		buyerBankAccount.getManagingBank().transferMoney(buyerBankAccount,
				this.bankAccountTransactions, faceValue,
				"payment for " + fixedRateBond);
		PropertyRegister.getInstance().transferProperty(this,
				buyerBankAccount.getOwner(), fixedRateBond);
		return fixedRateBond;
	}

	@Override
	@Transient
	public void onBankCloseBankAccount(BankAccount bankAccount) {
		if (this.bankAccountCouponLoans != null
				&& this.bankAccountCouponLoans == bankAccount) {
			this.bankAccountCouponLoans = null;
		}

		super.onBankCloseBankAccount(bankAccount);
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

	public class OfferBondsEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			State.this.pricingBehaviour.nextPeriod();

			destroyUnsoldBonds();
			issueNewBonds();
			offerBonds();
		}

		private void destroyUnsoldBonds() {
			/*
			 * destroy bonds, that have been offered, but not sold in last
			 * periods
			 */
			MarketFactory.getInstance().removeAllSellingOffers(State.this,
					State.this.primaryCurrency, FixedRateBond.class);

			// by definition bonds that have not been sold have owned AND to be
			// issued by this agent
			for (Property property : PropertyRegister.getInstance()
					.getProperties(State.this, FixedRateBond.class)) {
				assert (property instanceof FixedRateBond);
				FixedRateBond bond = (FixedRateBond) property;

				assert (bond.getOwner() == State.this);

				// if the bond is issued by this state -> it is an unsold bond
				if (bond.getIssuer() == State.this) {
					bond.deconstruct();
				}
			}
		}

		private void issueNewBonds() {
			/*
			 * issue new bonds
			 */
			final int numberOfBondsToIssue = 10;
			final double totalFaceValueToBeOffered = Math.max(100,
					(int) State.this.pricingBehaviour.getLastSoldAmount()
							* (double) numberOfBondsToIssue);
			final double faceValuePerBond = totalFaceValueToBeOffered
					/ (double) numberOfBondsToIssue;

			for (int i = 0; i < numberOfBondsToIssue; i++) {
				State.this.issueNewFixedRateBond(faceValuePerBond);
			}
		}

		private void offerBonds() {
			/*
			 * offer bonds
			 */
			for (Property property : PropertyRegister.getInstance()
					.getProperties(State.this, FixedRateBond.class)) {
				MarketFactory.getInstance().placeSettlementSellingOffer(
						(FixedRateBond) property, State.this,
						State.this.bankAccountTransactions,
						((FixedRateBond) property).getFaceValue(),
						new SettlementMarketEvent());
			}
		}
	}

	public class BuyAndConsumeGoodsEvent implements ITimeSystemEvent {

		@Override
		public void onEvent() {
			buyAndConsumeGoods();
		}

		@Transient
		private double buyAndConsumeGoods() {
			State.this.assureBankAccountTransactions();

			/*
			 * buy goods for sold bonds -> prevent hoarding of money
			 */
			Map<GoodType, IPriceFunction> priceFunctions = MarketFactory
					.getInstance().getMarketPriceFunctions(
							State.this.bankAccountTransactions.getCurrency(),
							State.this.utilityFunction.getInputGoodTypes());
			final double budget = State.this.bankAccountTransactions
					.getBalance();
			if (MathUtil.greater(budget, 0.0)) {
				getLog().setAgentCurrentlyActive(State.this);
				Map<GoodType, Double> optimalBundleOfGoods = State.this.utilityFunction
						.calculateUtilityMaximizingInputs(priceFunctions,
								budget);

				for (Entry<GoodType, Double> entry : optimalBundleOfGoods
						.entrySet()) {
					GoodType goodType = entry.getKey();
					double maxAmount = entry.getValue();
					MarketFactory.getInstance().buy(goodType, maxAmount,
							Double.NaN, Double.NaN, State.this,
							State.this.bankAccountTransactions);
				}
			}

			/*
			 * consume bought goods
			 */
			final Map<GoodType, Double> effectiveConsumptionGoodsBundle = new HashMap<GoodType, Double>();
			for (GoodType goodType : State.this.utilityFunction
					.getInputGoodTypes()) {
				double amountToConsume = PropertyRegister.getInstance()
						.getBalance(State.this, goodType);
				effectiveConsumptionGoodsBundle.put(goodType, amountToConsume);
				PropertyRegister.getInstance().decrementGoodTypeAmount(
						State.this, goodType, amountToConsume);
			}
			double utility = State.this.utilityFunction
					.calculateUtility(effectiveConsumptionGoodsBundle);
			getLog().state_onUtility(State.this,
					State.this.bankAccountTransactions.getCurrency(),
					effectiveConsumptionGoodsBundle, utility);
			return utility;
		}
	}

}
