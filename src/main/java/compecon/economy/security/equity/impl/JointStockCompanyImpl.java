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

package compecon.economy.security.equity.impl;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.economy.agent.impl.AgentImpl;
import compecon.economy.bookkeeping.impl.BalanceSheetDTO;
import compecon.economy.materia.GoodType;
import compecon.economy.property.Property;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.financial.impl.BankAccountImpl;
import compecon.economy.security.equity.JointStockCompany;
import compecon.economy.security.equity.Share;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.timesystem.TimeSystemEvent;
import compecon.engine.timesystem.impl.DayType;
import compecon.engine.timesystem.impl.HourType;
import compecon.engine.timesystem.impl.MonthType;
import compecon.math.util.MathUtil;

/**
 * Joint-stock companies are owned by agents and pay dividends to them every
 * period.
 */
@Entity
public abstract class JointStockCompanyImpl extends AgentImpl implements
		JointStockCompany {

	public class PayDividendEvent implements TimeSystemEvent {
		@Override
		public boolean isDeconstructed() {
			return JointStockCompanyImpl.this.isDeconstructed;
		}

		@Override
		public void onEvent() {
			final List<Property> propertiesIssued = ApplicationContext
					.getInstance()
					.getPropertyService()
					.findAllPropertiesIssuedByAgent(JointStockCompanyImpl.this,
							Share.class);

			if (propertiesIssued.size() == 0) {
				issueShares();
			} else {
				assureBankAccountDividends();

				final double totalDividend = bankAccountDividends.getBalance();

				// dividend to be payed?
				if (MathUtil.greater(totalDividend, 0.0)) {
					double totalDividendPayed = 0.0;

					final Currency currency = bankAccountDividends
							.getCurrency();
					final double dividendPerShare = totalDividend
							/ propertiesIssued.size();

					// pay dividend for each share
					for (final Property propertyIssued : propertiesIssued) {
						final Share share = (Share) propertyIssued;

						if (share.getOwner() != null
								&& share.getOwner() != JointStockCompanyImpl.this) {
							assert (share.getDividendBankAccountDelegate() != null);

							if (currency.equals(share
									.getDividendBankAccountDelegate()
									.getBankAccount().getCurrency())) {
								final double dividend = Math.min(
										dividendPerShare,
										bankAccountDividends.getBalance());
								bankAccountDividends
										.getManagingBank()
										.transferMoney(
												bankAccountDividends,
												share.getDividendBankAccountDelegate()
														.getBankAccount(),
												dividend, "dividend");
								share.getDividendBankAccountDelegate()
										.onTransfer(dividendPerShare);
								totalDividendPayed += dividendPerShare;
							}
						}
					}

					if (getLog().isAgentSelectedByClient(
							JointStockCompanyImpl.this)) {
						getLog().log(
								JointStockCompanyImpl.this,
								PayDividendEvent.class,
								"payed dividend of %s %s",
								Currency.formatMoneySum(totalDividendPayed),
								JointStockCompanyImpl.this.bankAccountTransactions
										.getCurrency());
					}
				}
			}
		}
	}

	/**
	 * bank account for dividends to be payed to share holders
	 */
	@OneToOne(targetEntity = BankAccountImpl.class)
	@JoinColumn(name = "bankAccountDividends_id")
	@Index(name = "IDX_A_BA_DIVIDENDS")
	protected BankAccount bankAccountDividends;

	@Transient
	protected final BankAccountDelegate bankAccountDividendsDelegate = new BankAccountDelegate() {
		@Override
		public BankAccount getBankAccount() {
			JointStockCompanyImpl.this.assureBankAccountDividends();
			return bankAccountDividends;
		}

		@Override
		public void onTransfer(final double amount) {
		}
	};

	@Transient
	public void assureBankAccountDividends() {
		if (isDeconstructed) {
			return;
		}

		// initialize bank account
		if (bankAccountDividends == null) {
			// overdraft not allowed
			bankAccountDividends = getPrimaryBank().openBankAccount(this,
					primaryCurrency, false, "dividends", TermType.SHORT_TERM,
					MoneyType.DEPOSITS);
		}
	}

	public BankAccount getBankAccountDividends() {
		return bankAccountDividends;
	}

	/*
	 * assertions
	 */

	@Transient
	public BankAccountDelegate getBankAccountDividendsDelegate() {
		return bankAccountDividendsDelegate;
	}

	/*
	 * business logic
	 */

	@Override
	public void initialize() {
		super.initialize();

		// pay dividend; every hour, so that no money is hoarded
		final TimeSystemEvent payDividendEvent = new PayDividendEvent();
		timeSystemEvents.add(payDividendEvent);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(payDividendEvent, -1, MonthType.EVERY, DayType.EVERY,
						HourType.EVERY);
	}

	@Override
	@Transient
	protected BalanceSheetDTO issueBalanceSheet() {
		assureBankAccountDividends();

		final BalanceSheetDTO balanceSheet = super.issueBalanceSheet();

		// bank account for paying dividends
		balanceSheet.addBankAccountBalance(bankAccountDividends);

		return balanceSheet;
	}

	@Override
	@Transient
	public void issueShares() {
		// issue initial shares
		for (int i = 0; i < ApplicationContext.getInstance().getConfiguration().jointStockCompanyConfig
				.getInitialNumberOfShares(); i++) {
			final Share initialShare = ApplicationContext
					.getInstance()
					.getShareFactory()
					.newInstanceShare(JointStockCompanyImpl.this,
							JointStockCompanyImpl.this);
			ApplicationContext
					.getInstance()
					.getMarketService()
					.placeSellingOffer(initialShare,
							JointStockCompanyImpl.this,
							getBankAccountTransactionsDelegate(), 0.0);
		}
	}

	@Override
	@Transient
	public void onBankCloseBankAccount(final BankAccount bankAccount) {
		if (bankAccountDividends != null && bankAccountDividends == bankAccount) {
			bankAccountDividends = null;
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
			final double pricePerUnit, final Currency currency) {
	}

	public void setBankAccountDividends(final BankAccount bankAccountDividends) {
		this.bankAccountDividends = bankAccountDividends;
	}

	/**
	 * Transfers money on account to dividend account, so that on the next
	 * dividend event the money is transfered to share holders.
	 *
	 * @param bankAccount
	 */
	protected void transferBankAccountBalanceToDividendBankAccount(
			final BankAccount bankAccount) {
		assureBankAccountDividends();

		if (MathUtil.greater(bankAccount.getBalance(), 0.0)) {
			bankAccount.getManagingBank().transferMoney(bankAccount,
					JointStockCompanyImpl.this.bankAccountDividends,
					bankAccount.getBalance(), "converting profit to dividend");
		}
	}
}
