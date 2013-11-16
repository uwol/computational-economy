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
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.economy.agent.impl.AgentImpl;
import compecon.economy.behaviour.PricingBehaviour;
import compecon.economy.behaviour.impl.PricingBehaviourImpl;
import compecon.economy.bookkeeping.impl.BalanceSheetDTO;
import compecon.economy.property.Property;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.sectors.financial.CentralBank;
import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.financial.impl.BankAccountImpl;
import compecon.economy.sectors.state.State;
import compecon.economy.security.debt.Bond;
import compecon.economy.security.debt.FixedRateBond;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.service.SettlementMarketService.SettlementEvent;
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

	@Transient
	protected PricingBehaviour pricingBehaviour;

	@Transient
	protected UtilityFunction utilityFunction;

	@Override
	public void initialize() {
		super.initialize();

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
				.getAgentService().getInstanceCentralBank(primaryCurrency)
				.getEffectiveKeyInterestRate() + 0.02;
		this.pricingBehaviour = new PricingBehaviourImpl(this,
				FixedRateBond.class, this.primaryCurrency, initialInterestRate);
	}

	/*
	 * accessors
	 */

	public BankAccount getBankAccountCouponLoans() {
		return bankAccountCouponLoans;
	}

	public UtilityFunction getUtilityFunction() {
		return utilityFunction;
	}

	public void setBankAccountCouponLoans(BankAccount bankAccountCouponLoans) {
		this.bankAccountCouponLoans = bankAccountCouponLoans;
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

		// initialize bank account
		if (this.bankAccountCouponLoans == null) {
			this.bankAccountCouponLoans = this.getPrimaryBank()
					.openBankAccount(this, this.primaryCurrency, true, "loans",
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
				.getAgentService().getAllCreditBanks(this.primaryCurrency)) {
			for (BankAccount bankAccount : ApplicationContext.getInstance()
					.getBankAccountDAO()
					.findAllBankAccountsManagedByBank(creditBank)) {
				if (bankAccount.getOwner() != this) {
					this.bankAccountTransactions.getManagingBank()
							.transferMoney(this.bankAccountTransactions,
									bankAccount, 5000, "deficit spending");
				}
			}
		}
	}

	@Transient
	public BankAccountDelegate getBankAccountCouponLoansDelegate() {
		final BankAccountDelegate delegate = new BankAccountDelegate() {
			@Override
			public BankAccount getBankAccount() {
				StateImpl.this.assureBankAccountCouponLoans();
				return StateImpl.this.bankAccountCouponLoans;
			}
		};
		return delegate;
	}

	@Override
	@Transient
	protected BalanceSheetDTO issueBalanceSheet() {
		final BalanceSheetDTO balanceSheet = super.issueBalanceSheet();

		// bank deposits
		balanceSheet.addBankAccountBalance(this.bankAccountCouponLoans);

		// list issued bonds on balance sheet
		for (Property property : ApplicationContext.getInstance()
				.getPropertyDAO().findAllPropertiesOfAgent(this, Bond.class)) {
			Bond bond = (Bond) property;
			if (!bond.isDeconstructed() && !bond.getOwner().equals(this)) {
				balanceSheet.financialLiabilities += bond.getFaceValue();
			}
		}

		return balanceSheet;
	}

	@Transient
	public FixedRateBond obtainBond(final double faceValue,
			final BankAccountDelegate buyerBankAccountDelegate) {
		this.assureBankAccountCouponLoans();

		// TODO alternative: price := this.pricingBehaviour.getCurrentPrice();
		final CentralBank centralBank = ApplicationContext.getInstance()
				.getAgentService().getInstanceCentralBank(this.primaryCurrency);
		final double coupon = centralBank.getEffectiveKeyInterestRate()
				+ ApplicationContext.getInstance().getConfiguration().stateConfig
						.getBondMargin();

		// coupons have to be payed from a separate bank account, so that bonds
		// can be re-bought with same face value after bond deconstruction
		final FixedRateBond fixedRateBond = ApplicationContext
				.getInstance()
				.getPropertyService()
				.newInstanceFixedRateBond(this, this, this.primaryCurrency,
						getBankAccountTransactionsDelegate(),
						getBankAccountCouponLoansDelegate(), faceValue, coupon);

		// transfer
		buyerBankAccountDelegate
				.getBankAccount()
				.getManagingBank()
				.transferMoney(buyerBankAccountDelegate.getBankAccount(),
						this.bankAccountTransactions, faceValue,
						"payment for " + fixedRateBond);
		ApplicationContext
				.getInstance()
				.getPropertyService()
				.transferProperty(this,
						buyerBankAccountDelegate.getBankAccount().getOwner(),
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

	protected class SettlementMarketEvent implements SettlementEvent {
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
					.getMarketService()
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
							.getMarketService()
							.buy(goodType, maxAmount, Double.NaN, Double.NaN,
									StateImpl.this,
									getBankAccountTransactionsDelegate());
				}
			}

			/*
			 * consume bought goods
			 */
			final Map<GoodType, Double> effectiveConsumptionGoodsBundle = new HashMap<GoodType, Double>();
			for (GoodType goodType : StateImpl.this.utilityFunction
					.getInputGoodTypes()) {
				double amountToConsume = ApplicationContext.getInstance()
						.getPropertyService()
						.getBalance(StateImpl.this, goodType);
				effectiveConsumptionGoodsBundle.put(goodType, amountToConsume);
				ApplicationContext
						.getInstance()
						.getPropertyService()
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
