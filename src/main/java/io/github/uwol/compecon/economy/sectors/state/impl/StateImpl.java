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

package io.github.uwol.compecon.economy.sectors.state.impl;

import java.util.List;

import io.github.uwol.compecon.economy.agent.impl.AgentImpl;
import io.github.uwol.compecon.economy.behaviour.PricingBehaviour;
import io.github.uwol.compecon.economy.bookkeeping.impl.BalanceSheetDTO;
import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.property.Property;
import io.github.uwol.compecon.economy.property.PropertyOwner;
import io.github.uwol.compecon.economy.sectors.financial.BankAccount;
import io.github.uwol.compecon.economy.sectors.financial.BankAccount.MoneyType;
import io.github.uwol.compecon.economy.sectors.financial.BankAccount.TermType;
import io.github.uwol.compecon.economy.sectors.financial.BankAccountDelegate;
import io.github.uwol.compecon.economy.sectors.financial.CentralBank;
import io.github.uwol.compecon.economy.sectors.financial.CreditBank;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.economy.sectors.household.Household;
import io.github.uwol.compecon.economy.sectors.state.State;
import io.github.uwol.compecon.economy.security.debt.FixedRateBond;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.timesystem.TimeSystemEvent;
import io.github.uwol.compecon.engine.timesystem.impl.DayType;
import io.github.uwol.compecon.engine.timesystem.impl.HourType;
import io.github.uwol.compecon.engine.timesystem.impl.MonthType;
import io.github.uwol.compecon.math.util.MathUtil;

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

		private void transferBudgetToHouseholds() {
			assureBankAccountTransactions();

			final double budget = StateImpl.this.bankAccountTransactions.getBalance();

			if (MathUtil.greater(budget, 0.0)) {
				final List<Household> households = ApplicationContext.getInstance().getHouseholdDAO()
						.findAllByCurrency(StateImpl.this.primaryCurrency);

				if (households.size() > 0) {
					final double budgetPerHousehold = budget / households.size();

					for (final Household household : households) {
						assert (!household.isDeconstructed());

						final BankAccount householdBankAccount = household.getBankAccountGovernmentTransfersDelegate()
								.getBankAccount();

						StateImpl.this.bankAccountTransactions.getManagingBank().transferMoney(
								StateImpl.this.bankAccountTransactions, householdBankAccount, budgetPerHousehold,
								"government transfer");

						household.getBankAccountGovernmentTransfersDelegate().onTransfer(budgetPerHousehold);
					}
				}
			}
		}
	}

	/**
	 * bank account for financing the coupon of issued bonds
	 */
	protected BankAccount bankAccountCouponLoans;

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

	protected PricingBehaviour pricingBehaviour;

	public void assureBankAccountCouponLoans() {
		if (isDeconstructed) {
			return;
		}

		// initialize bank account
		if (bankAccountCouponLoans == null) {
			bankAccountCouponLoans = getPrimaryBank().openBankAccount(this, primaryCurrency, true, "loans",
					TermType.LONG_TERM, MoneyType.DEPOSITS);
		}
	}

	@Override
	public void deconstruct() {
		super.deconstruct();

		ApplicationContext.getInstance().getStateFactory().deleteState(this);
	}

	@Override
	public void doDeficitSpending() {
		assureBankAccountTransactions();

		for (final CreditBank creditBank : ApplicationContext.getInstance().getAgentService()
				.findCreditBanks(primaryCurrency)) {
			for (final BankAccount bankAccount : ApplicationContext.getInstance().getBankAccountDAO()
					.findAllBankAccountsManagedByBank(creditBank)) {
				if (bankAccount.getOwner() != this) {
					bankAccountTransactions.getManagingBank().transferMoney(bankAccountTransactions, bankAccount, 5000,
							"deficit spending");
				}
			}
		}
	}

	public BankAccount getBankAccountCouponLoans() {
		return bankAccountCouponLoans;
	}

	@Override
	public BankAccountDelegate getBankAccountCouponLoansDelegate() {
		return bankAccountCouponLoansDelegate;
	}

	@Override
	public void initialize() {
		super.initialize();

		/*
		 * has to happen every hour, so that not all money is spent on one distinct hour
		 * a day! else this would lead to significant distortions on markets, as the
		 * savings of the whole economy flow through the state via state bonds.
		 */
		final TimeSystemEvent governmentTransferEvent = new GovernmentTransferEvent();
		timeSystemEvents.add(governmentTransferEvent);

		ApplicationContext.getInstance().getTimeSystem().addEventEvery(governmentTransferEvent, -1, MonthType.EVERY,
				DayType.EVERY, HourType.EVERY);

		final double initialInterestRate = ApplicationContext.getInstance().getAgentService()
				.findCentralBank(primaryCurrency).getEffectiveKeyInterestRate() + 0.02;
		pricingBehaviour = ApplicationContext.getInstance().getPricingBehaviourFactory()
				.newInstancePricingBehaviour(this, FixedRateBond.class, primaryCurrency, initialInterestRate);
	}

	@Override
	protected BalanceSheetDTO issueBalanceSheet() {
		final BalanceSheetDTO balanceSheet = super.issueBalanceSheet();

		// bank deposits
		balanceSheet.addBankAccountBalance(bankAccountCouponLoans);

		return balanceSheet;
	}

	@Override
	public FixedRateBond obtainBond(final double faceValue, final PropertyOwner buyer,
			final BankAccountDelegate buyerBankAccountDelegate) {
		assureBankAccountCouponLoans();

		assert (buyer == buyerBankAccountDelegate.getBankAccount().getOwner());

		// TODO alternative: price := this.pricingBehaviour.getCurrentPrice();
		final CentralBank centralBank = ApplicationContext.getInstance().getAgentService()
				.findCentralBank(primaryCurrency);
		final double coupon = centralBank.getEffectiveKeyInterestRate()
				+ ApplicationContext.getInstance().getConfiguration().stateConfig.getBondMargin();

		// coupons have to be payed from a separate bank account, so that bonds
		// can be re-bought with same face value after bond deconstruction
		final FixedRateBond fixedRateBond = ApplicationContext.getInstance().getFixedRateBondFactory()
				.newInstanceFixedRateBond(this, this, primaryCurrency, getBankAccountTransactionsDelegate(),
						getBankAccountCouponLoansDelegate(), faceValue, coupon);

		// transfer money
		buyerBankAccountDelegate.getBankAccount().getManagingBank().transferMoney(
				buyerBankAccountDelegate.getBankAccount(), bankAccountTransactions, faceValue,
				"payment for " + fixedRateBond);

		// transfer bond
		ApplicationContext.getInstance().getPropertyService().transferProperty(fixedRateBond, this, buyer);

		assert (fixedRateBond.getOwner() == buyerBankAccountDelegate.getBankAccount().getOwner());

		return fixedRateBond;
	}

	@Override
	public void onBankCloseBankAccount(final BankAccount bankAccount) {
		if (bankAccountCouponLoans != null && bankAccountCouponLoans == bankAccount) {
			bankAccountCouponLoans = null;
		}

		super.onBankCloseBankAccount(bankAccount);
	}

	@Override
	public void onMarketSettlement(final Currency commodityCurrency, final double amount, final double pricePerUnit,
			final Currency currency) {
	}

	@Override
	public void onMarketSettlement(final GoodType goodType, final double amount, final double pricePerUnit,
			final Currency currency) {
	}

	@Override
	public void onMarketSettlement(final Property property, final double totalPrice, final Currency currency) {
		if (property instanceof FixedRateBond) {
			StateImpl.this.pricingBehaviour.registerSelling(((FixedRateBond) property).getFaceValue(), totalPrice);
		}
	}

	public void setBankAccountCouponLoans(final BankAccount bankAccountCouponLoans) {
		this.bankAccountCouponLoans = bankAccountCouponLoans;
	}

}
