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

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.economy.agent.impl.AgentImpl;
import compecon.economy.behaviour.PricingBehaviour;
import compecon.economy.bookkeeping.impl.BalanceSheetDTO;
import compecon.economy.materia.GoodType;
import compecon.economy.property.Property;
import compecon.economy.property.PropertyOwner;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.sectors.financial.CentralBank;
import compecon.economy.sectors.financial.CreditBank;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.financial.impl.BankAccountImpl;
import compecon.economy.sectors.household.Household;
import compecon.economy.sectors.state.State;
import compecon.economy.security.debt.FixedRateBond;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.timesystem.TimeSystemEvent;
import compecon.engine.timesystem.impl.DayType;
import compecon.engine.timesystem.impl.HourType;
import compecon.engine.timesystem.impl.MonthType;
import compecon.math.util.MathUtil;

@Entity
public class StateImpl extends AgentImpl implements State {

	public class GovernmentTransferEvent implements TimeSystemEvent {
		@Override
		public boolean isDeconstructed() {
			return StateImpl.this.isDeconstructed;
		}

		@Override
		public void onEvent() {
			transferBudgetToHouseholds();
		}

		@Transient
		private void transferBudgetToHouseholds() {
			assureBankAccountTransactions();

			final double budget = StateImpl.this.bankAccountTransactions
					.getBalance();

			if (MathUtil.greater(budget, 0.0)) {
				final List<Household> households = ApplicationContext
						.getInstance().getHouseholdDAO()
						.findAllByCurrency(StateImpl.this.primaryCurrency);

				if (households.size() > 0) {
					final double budgetPerHousehold = budget
							/ households.size();

					for (final Household household : households) {
						assert (!household.isDeconstructed());

						final BankAccount householdBankAccount = household
								.getBankAccountGovernmentTransfersDelegate()
								.getBankAccount();

						StateImpl.this.bankAccountTransactions
								.getManagingBank().transferMoney(
										StateImpl.this.bankAccountTransactions,
										householdBankAccount,
										budgetPerHousehold,
										"government transfer");

						household.getBankAccountGovernmentTransfersDelegate()
								.onTransfer(budgetPerHousehold);
					}
				}
			}
		}
	}

	/**
	 * bank account for financing the coupon of issued bonds
	 */
	@OneToOne(targetEntity = BankAccountImpl.class)
	@JoinColumn(name = "bankAccountCouponLoans_id")
	@Index(name = "IDX_A_BA_COUPONLOANS")
	protected BankAccount bankAccountCouponLoans;

	@Transient
	protected final BankAccountDelegate bankAccountCouponLoansDelegate = new BankAccountDelegate() {
		@Override
		public BankAccount getBankAccount() {
			StateImpl.this.assureBankAccountCouponLoans();
			return bankAccountCouponLoans;
		}

		@Override
		public void onTransfer(final double amount) {
		}
	};

	@Transient
	protected PricingBehaviour pricingBehaviour;

	@Transient
	public void assureBankAccountCouponLoans() {
		if (isDeconstructed) {
			return;
		}

		// initialize bank account
		if (bankAccountCouponLoans == null) {
			bankAccountCouponLoans = getPrimaryBank().openBankAccount(this,
					primaryCurrency, true, "loans", TermType.LONG_TERM,
					MoneyType.DEPOSITS);
		}
	}

	@Override
	public void deconstruct() {
		super.deconstruct();

		ApplicationContext.getInstance().getStateFactory().deleteState(this);
	}

	@Override
	@Transient
	public void doDeficitSpending() {
		assureBankAccountTransactions();

		for (final CreditBank creditBank : ApplicationContext.getInstance()
				.getAgentService().findCreditBanks(primaryCurrency)) {
			for (final BankAccount bankAccount : ApplicationContext
					.getInstance().getBankAccountDAO()
					.findAllBankAccountsManagedByBank(creditBank)) {
				if (bankAccount.getOwner() != this) {
					bankAccountTransactions.getManagingBank().transferMoney(
							bankAccountTransactions, bankAccount, 5000,
							"deficit spending");
				}
			}
		}
	}

	public BankAccount getBankAccountCouponLoans() {
		return bankAccountCouponLoans;
	}

	@Override
	@Transient
	public BankAccountDelegate getBankAccountCouponLoansDelegate() {
		return bankAccountCouponLoansDelegate;
	}

	@Override
	public void initialize() {
		super.initialize();

		/*
		 * has to happen every hour, so that not all money is spent on one
		 * distinct hour a day! else this would lead to significant distortions
		 * on markets, as the savings of the whole economy flow through the
		 * state via state bonds.
		 */
		final TimeSystemEvent governmentTransferEvent = new GovernmentTransferEvent();
		timeSystemEvents.add(governmentTransferEvent);

		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEventEvery(governmentTransferEvent, -1, MonthType.EVERY,
						DayType.EVERY, HourType.EVERY);

		final double initialInterestRate = ApplicationContext.getInstance()
				.getAgentService().findCentralBank(primaryCurrency)
				.getEffectiveKeyInterestRate() + 0.02;
		pricingBehaviour = ApplicationContext
				.getInstance()
				.getPricingBehaviourFactory()
				.newInstancePricingBehaviour(this, FixedRateBond.class,
						primaryCurrency, initialInterestRate);
	}

	@Override
	@Transient
	protected BalanceSheetDTO issueBalanceSheet() {
		final BalanceSheetDTO balanceSheet = super.issueBalanceSheet();

		// bank deposits
		balanceSheet.addBankAccountBalance(bankAccountCouponLoans);

		return balanceSheet;
	}

	@Override
	@Transient
	public FixedRateBond obtainBond(final double faceValue,
			final PropertyOwner buyer,
			final BankAccountDelegate buyerBankAccountDelegate) {
		assureBankAccountCouponLoans();

		assert (buyer == buyerBankAccountDelegate.getBankAccount().getOwner());

		// TODO alternative: price := this.pricingBehaviour.getCurrentPrice();
		final CentralBank centralBank = ApplicationContext.getInstance()
				.getAgentService().findCentralBank(primaryCurrency);
		final double coupon = centralBank.getEffectiveKeyInterestRate()
				+ ApplicationContext.getInstance().getConfiguration().stateConfig
						.getBondMargin();

		// coupons have to be payed from a separate bank account, so that bonds
		// can be re-bought with same face value after bond deconstruction
		final FixedRateBond fixedRateBond = ApplicationContext
				.getInstance()
				.getFixedRateBondFactory()
				.newInstanceFixedRateBond(this, this, primaryCurrency,
						getBankAccountTransactionsDelegate(),
						getBankAccountCouponLoansDelegate(), faceValue, coupon);

		// transfer money
		buyerBankAccountDelegate
				.getBankAccount()
				.getManagingBank()
				.transferMoney(buyerBankAccountDelegate.getBankAccount(),
						bankAccountTransactions, faceValue,
						"payment for " + fixedRateBond);

		// transfer bond
		ApplicationContext.getInstance().getPropertyService()
				.transferProperty(fixedRateBond, this, buyer);

		assert (fixedRateBond.getOwner() == buyerBankAccountDelegate
				.getBankAccount().getOwner());

		return fixedRateBond;
	}

	@Override
	@Transient
	public void onBankCloseBankAccount(final BankAccount bankAccount) {
		if (bankAccountCouponLoans != null
				&& bankAccountCouponLoans == bankAccount) {
			bankAccountCouponLoans = null;
		}

		super.onBankCloseBankAccount(bankAccount);
	}

	@Override
	public void onMarketSettlement(final Currency commodityCurrency,
			final double amount, final double pricePerUnit,
			final Currency currency) {
	}

	@Override
	public void onMarketSettlement(final GoodType goodType,
			final double amount, final double pricePerUnit,
			final Currency currency) {
	}

	@Override
	public void onMarketSettlement(final Property property,
			final double totalPrice, final Currency currency) {
		if (property instanceof FixedRateBond) {
			StateImpl.this.pricingBehaviour.registerSelling(
					((FixedRateBond) property).getFaceValue(), totalPrice);
		}
	}

	public void setBankAccountCouponLoans(
			final BankAccount bankAccountCouponLoans) {
		this.bankAccountCouponLoans = bankAccountCouponLoans;
	}

}
