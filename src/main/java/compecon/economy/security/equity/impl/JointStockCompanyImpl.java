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
import compecon.economy.property.PropertyIssued;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
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

	/**
	 * bank account for dividends to be payed to share holders
	 */
	@OneToOne(targetEntity = BankAccountImpl.class)
	@JoinColumn(name = "bankAccountDividends_id")
	@Index(name = "IDX_A_BA_DIVIDENDSS")
	protected BankAccount bankAccountDividends;

	@Override
	public void initialize() {
		super.initialize();

		// offer shares at 00:00
		// has to happen as an event, as at constructor time the transaction
		// bank account is null
		final TimeSystemEvent offerSharesEvent = new OfferSharesEvent();
		this.timeSystemEvents.add(offerSharesEvent);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(offerSharesEvent, -1, MonthType.EVERY, DayType.EVERY,
						HourType.HOUR_00);

		// pay dividend
		final TimeSystemEvent payDividendEvent = new PayDividendEvent();
		this.timeSystemEvents.add(payDividendEvent);
		ApplicationContext
				.getInstance()
				.getTimeSystem()
				.addEvent(payDividendEvent, -1, MonthType.EVERY, DayType.EVERY,
						HourType.EVERY);
	}

	/*
	 * accessors
	 */

	public BankAccount getBankAccountDividends() {
		return bankAccountDividends;
	}

	public void setBankAccountDividends(BankAccount bankAccountDividends) {
		this.bankAccountDividends = bankAccountDividends;
	}

	/*
	 * assertions
	 */

	@Transient
	public void assureBankAccountDividends() {
		if (this.isDeconstructed)
			return;

		// initialize bank account
		if (this.bankAccountDividends == null) {
			// overdraft not allowed
			this.bankAccountDividends = this.getPrimaryBank().openBankAccount(
					this, this.primaryCurrency, false, "dividends",
					TermType.SHORT_TERM, MoneyType.DEPOSITS);
		}
	}

	/*
	 * business logic
	 */

	@Override
	@Transient
	protected BalanceSheetDTO issueBalanceSheet() {
		this.assureBankAccountDividends();

		final BalanceSheetDTO balanceSheet = super.issueBalanceSheet();

		// bank account for paying dividends
		balanceSheet.addBankAccountBalance(this.bankAccountDividends);

		// issuedCapital
		final List<PropertyIssued> propertiesIssued = ApplicationContext
				.getInstance()
				.getPropertyDAO()
				.findAllPropertiesIssuedByAgent(JointStockCompanyImpl.this,
						Share.class);
		for (PropertyIssued propertyIssued : propertiesIssued) {
			Share share = (Share) propertyIssued;
			balanceSheet.issuedCapital.add(share);
		}

		return balanceSheet;
	}

	@Override
	@Transient
	public void onBankCloseBankAccount(BankAccount bankAccount) {
		if (this.bankAccountDividends != null
				&& this.bankAccountDividends == bankAccount) {
			this.bankAccountDividends = null;
		}

		super.onBankCloseBankAccount(bankAccount);
	}

	@Override
	public void onMarketSettlement(GoodType goodType, double amount,
			double pricePerUnit, Currency currency) {
	}

	@Override
	public void onMarketSettlement(Currency commodityCurrency, double amount,
			double pricePerUnit, Currency currency) {
	}

	@Override
	public void onMarketSettlement(Property property, double pricePerUnit,
			Currency currency) {
	}

	/**
	 * Transfers money on account to dividend account, so that on the next
	 * dividend event the money is transfered to share holders.
	 * 
	 * @param bankAccount
	 */
	protected void transferBankAccountBalanceToDividendBankAccount(
			final BankAccount bankAccount) {
		this.assureBankAccountDividends();

		if (MathUtil.greater(bankAccount.getBalance(), 0.0)) {
			bankAccount.getManagingBank().transferMoney(bankAccount,
					JointStockCompanyImpl.this.bankAccountDividends,
					bankAccount.getBalance(), "converting profit to dividend");
		}
	}

	public class PayDividendEvent implements TimeSystemEvent {

		@Override
		public void onEvent() {
			final List<PropertyIssued> propertiesIssued = ApplicationContext
					.getInstance()
					.getPropertyDAO()
					.findAllPropertiesIssuedByAgent(JointStockCompanyImpl.this,
							Share.class);

			if (propertiesIssued.size() > 0) {
				JointStockCompanyImpl.this.assureBankAccountDividends();

				final double totalDividend = JointStockCompanyImpl.this.bankAccountDividends
						.getBalance();

				// dividend to be payed?
				if (totalDividend > 0.0) {
					double totalDividendPayed = 0.0;

					final Currency currency = JointStockCompanyImpl.this.bankAccountDividends
							.getCurrency();
					final double dividendPerOwner = totalDividend
							/ propertiesIssued.size();

					// pay dividend for each share
					for (PropertyIssued propertyIssued : propertiesIssued) {
						final Share share = (Share) propertyIssued;
						if (share.getOwner() != null
								&& share.getOwner() != JointStockCompanyImpl.this) {
							assert (share.getDividendBankAccountDelegate() != null);

							if (currency.equals(share
									.getDividendBankAccountDelegate()
									.getBankAccount().getCurrency())) {
								final double dividend = Math
										.min(dividendPerOwner,
												JointStockCompanyImpl.this.bankAccountDividends
														.getBalance());
								JointStockCompanyImpl.this.bankAccountDividends
										.getManagingBank()
										.transferMoney(
												JointStockCompanyImpl.this.bankAccountDividends,
												share.getDividendBankAccountDelegate()
														.getBankAccount(),
												dividend, "dividend");
								share.getDividendBankAccountDelegate()
										.onTransfer(dividendPerOwner);
								totalDividendPayed += dividendPerOwner;
							}
						}
					}
					if (getLog().isAgentSelectedByClient(
							JointStockCompanyImpl.this)) {
						getLog().log(
								JointStockCompanyImpl.this,
								PayDividendEvent.class,
								"payed dividend of "
										+ Currency
												.formatMoneySum(totalDividendPayed)
										+ " "
										+ JointStockCompanyImpl.this.bankAccountTransactions
												.getCurrency().getIso4217Code());
					}
				}
			}
		}
	}

	public class OfferSharesEvent implements TimeSystemEvent {

		@Override
		public void onEvent() {
			final List<PropertyIssued> sharesIssued = ApplicationContext
					.getInstance()
					.getPropertyDAO()
					.findAllPropertiesIssuedByAgent(JointStockCompanyImpl.this,
							Share.class);

			if (sharesIssued.size() == 0) {
				JointStockCompanyImpl.this.assureBankAccountTransactions();

				// issue initial shares
				for (int i = 0; i < ApplicationContext.getInstance()
						.getConfiguration().jointStockCompanyConfig
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
		}
	}
}
