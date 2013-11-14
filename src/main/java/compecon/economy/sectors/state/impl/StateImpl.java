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

package compecon.economy.sectors.state.impl;

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

import compecon.economy.agent.impl.AgentImpl;
import compecon.economy.behaviour.PricingBehaviour;
import compecon.economy.behaviour.impl.PricingBehaviourImpl;
import compecon.economy.bookkeeping.impl.BalanceSheetDTO;
import compecon.economy.markets.SettlementMarket.ISettlementEvent;
import compecon.economy.property.Property;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
import compecon.economy.sectors.financial.CentralBank;
import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.financial.impl.BankAccountImpl;
import compecon.economy.sectors.state.State;
import compecon.economy.security.debt.Bond;
import compecon.economy.security.debt.FixedRateBond;
import compecon.economy.security.debt.impl.BondImpl;
import compecon.economy.security.debt.impl.FixedRateBondImpl;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.timesystem.ITimeSystemEvent;
import compecon.engine.timesystem.impl.DayType;
import compecon.engine.timesystem.impl.HourType;
import compecon.engine.timesystem.impl.MonthType;
import compecon.engine.util.MathUtil;
import compecon.materia.GoodType;
import compecon.math.price.PriceFunction;
import compecon.math.utility.UtilityFunction;

@Entity
public class StateImpl extends AgentImpl implements State {

	/**
	 * bank account for financing the coupon of issued bonds
	 */
	@OneToOne(targetEntity = BankAccountImpl.class)
	@JoinColumn(name = "bankAccountCouponLoans_id")
	@Index(name = "IDX_A_BA_COUPONLOANS")
	protected BankAccount bankAccountCouponLoans;

	@OneToMany(targetEntity = BondImpl.class)
	@JoinTable(name = "State_IssuedBonds", joinColumns = @JoinColumn(name = "state_id"), inverseJoinColumns = @JoinColumn(name = "bond_id"))
	protected Set<Bond> issuedBonds = new HashSet<Bond>();

	@Transient
	protected PricingBehaviour pricingBehaviour;

	@Transient
	protected UtilityFunction utilityFunction;

	@Override
	public void initialize() {
		super.initialize();

		// offer bonds
		final ITimeSystemEvent offerBondsEvent = new OfferBondsEvent();
		this.timeSystemEvents.add(offerBondsEvent);
		ApplicationContext
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
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEventEvery(buyAndConsumeGoodsEvent, -1, MonthType.EVERY,
						DayType.EVERY, HourType.EVERY);

		double initialInterestRate = ApplicationContext.getInstance()
				.getAgentFactory().getInstanceCentralBank(primaryCurrency)
				.getEffectiveKeyInterestRate() + 0.02;
		this.pricingBehaviour = new PricingBehaviourImpl(this,
				FixedRateBond.class, this.primaryCurrency, initialInterestRate);
	}

	@Transient
	public void deconstruct() {
		super.deconstruct();

		for (Bond bond : this.issuedBonds) {
			ApplicationContext.getInstance().getPropertyFactory()
					.deleteProperty(bond);
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

	public UtilityFunction getUtilityFunction() {
		return utilityFunction;
	}

	public void setBankAccountCouponLoans(BankAccount bankAccountCouponLoans) {
		this.bankAccountCouponLoans = bankAccountCouponLoans;
	}

	public void setIssuedBonds(Set<Bond> issuedBonds) {
		this.issuedBonds = issuedBonds;
	}

	public void setUtilityFunction(UtilityFunction utilityFunction) {
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

		for (CreditBank creditBank : ApplicationContext.getInstance()
				.getAgentFactory().getAllCreditBanks(this.primaryCurrency)) {
			for (BankAccount bankAccount : ApplicationContext.getInstance()
					.getBankAccountDAO()
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
	protected BalanceSheetDTO issueBalanceSheet() {
		final BalanceSheetDTO balanceSheet = super.issueBalanceSheet();

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
		final CentralBank centralBank = ApplicationContext.getInstance()
				.getCentralBankDAO().findByCurrency(this.primaryCurrency);
		final double coupon = centralBank.getEffectiveKeyInterestRate()
				+ ApplicationContext.getInstance().getConfiguration().stateConfig
						.getBondMargin();
		// coupons have to be payed from a separate bank account, so that bonds
		// can be re-bought with same face value after bond deconstruction
		final FixedRateBond bond = ApplicationContext
				.getInstance()
				.getPropertyFactory()
				.newInstanceFixedRateBond(this, this.primaryCurrency,
						this.bankAccountTransactions,
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
		ApplicationContext
				.getInstance()
				.getPropertyRegister()
				.transferProperty(this, buyerBankAccount.getOwner(),
						fixedRateBond);
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
				StateImpl.this.pricingBehaviour.registerSelling(
						((FixedRateBond) property).getFaceValue(), totalPrice);
			}
		}
	}

	public class OfferBondsEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			StateImpl.this.pricingBehaviour.nextPeriod();

			destroyUnsoldBonds();
			issueNewBonds();
			offerBonds();
		}

		private void destroyUnsoldBonds() {
			/*
			 * destroy bonds, that have been offered, but not sold in last
			 * periods
			 */
			// FIXME FixedRateBondImpl -> FixedRateBond
			ApplicationContext
					.getInstance()
					.getMarketFactory()
					.getMarket()
					.removeAllSellingOffers(StateImpl.this,
							StateImpl.this.primaryCurrency,
							FixedRateBondImpl.class);

			// by definition bonds that have not been sold have owned AND to be
			// issued by this agent
			for (Property property : ApplicationContext.getInstance()
					.getPropertyRegister()
					.getProperties(StateImpl.this, FixedRateBond.class)) {
				assert (property instanceof FixedRateBond);
				FixedRateBond bond = (FixedRateBond) property;

				assert (bond.getOwner() == StateImpl.this);

				// if the bond is issued by this state -> it is an unsold bond
				if (bond.getIssuer() == StateImpl.this) {
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
					(int) StateImpl.this.pricingBehaviour.getLastSoldAmount()
							* (double) numberOfBondsToIssue);
			final double faceValuePerBond = totalFaceValueToBeOffered
					/ (double) numberOfBondsToIssue;

			for (int i = 0; i < numberOfBondsToIssue; i++) {
				StateImpl.this.issueNewFixedRateBond(faceValuePerBond);
			}
		}

		private void offerBonds() {
			/*
			 * offer bonds
			 */
			for (Property property : ApplicationContext.getInstance()
					.getPropertyRegister()
					.getProperties(StateImpl.this, FixedRateBond.class)) {
				ApplicationContext
						.getInstance()
						.getMarketFactory()
						.getMarket()
						.placeSettlementSellingOffer((FixedRateBond) property,
								StateImpl.this,
								StateImpl.this.bankAccountTransactions,
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
			StateImpl.this.assureBankAccountTransactions();

			/*
			 * buy goods for sold bonds -> prevent hoarding of money
			 */
			Map<GoodType, PriceFunction> priceFunctions = ApplicationContext
					.getInstance()
					.getMarketFactory()
					.getMarket()
					.getMarketPriceFunctions(
							StateImpl.this.bankAccountTransactions
									.getCurrency(),
							StateImpl.this.utilityFunction.getInputGoodTypes());
			final double budget = StateImpl.this.bankAccountTransactions
					.getBalance();
			if (MathUtil.greater(budget, 0.0)) {
				getLog().setAgentCurrentlyActive(StateImpl.this);
				Map<GoodType, Double> optimalBundleOfGoods = StateImpl.this.utilityFunction
						.calculateUtilityMaximizingInputs(priceFunctions,
								budget);

				for (Entry<GoodType, Double> entry : optimalBundleOfGoods
						.entrySet()) {
					GoodType goodType = entry.getKey();
					double maxAmount = entry.getValue();
					ApplicationContext
							.getInstance()
							.getMarketFactory()
							.getMarket()
							.buy(goodType, maxAmount, Double.NaN, Double.NaN,
									StateImpl.this,
									StateImpl.this.bankAccountTransactions);
				}
			}

			/*
			 * consume bought goods
			 */
			final Map<GoodType, Double> effectiveConsumptionGoodsBundle = new HashMap<GoodType, Double>();
			for (GoodType goodType : StateImpl.this.utilityFunction
					.getInputGoodTypes()) {
				double amountToConsume = ApplicationContext.getInstance()
						.getPropertyRegister()
						.getBalance(StateImpl.this, goodType);
				effectiveConsumptionGoodsBundle.put(goodType, amountToConsume);
				ApplicationContext
						.getInstance()
						.getPropertyRegister()
						.decrementGoodTypeAmount(StateImpl.this, goodType,
								amountToConsume);
			}
			double utility = StateImpl.this.utilityFunction
					.calculateUtility(effectiveConsumptionGoodsBundle);
			getLog().state_onUtility(StateImpl.this,
					StateImpl.this.bankAccountTransactions.getCurrency(),
					effectiveConsumptionGoodsBundle, utility);
			return utility;
		}
	}

}
