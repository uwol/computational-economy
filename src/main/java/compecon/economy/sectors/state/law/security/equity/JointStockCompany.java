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

package compecon.economy.sectors.state.law.security.equity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import compecon.economy.markets.SettlementMarket.ISettlementEvent;
import compecon.economy.sectors.Agent;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.economy.sectors.state.law.property.Property;
import compecon.economy.sectors.state.law.property.PropertyRegister;
import compecon.engine.MarketFactory;
import compecon.engine.PropertyFactory;
import compecon.engine.Simulation;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.HourType;
import compecon.engine.time.calendar.MonthType;
import compecon.engine.util.ConfigurationUtil;
import compecon.engine.util.MathUtil;
import compecon.materia.GoodType;

/**
 * Joint-stock companies are owned by agents and pay dividends to them every
 * period.
 */
@Entity
public abstract class JointStockCompany extends Agent {

	/**
	 * bank account for dividends to be payed to share holders
	 */
	@OneToOne
	@JoinColumn(name = "bankAccountDividends_id")
	@Index(name = "IDX_A_BA_DIVIDENDSS")
	protected BankAccount bankAccountDividends;

	@OneToMany
	@JoinTable(name = "JointStockCompany_IssuedShares")
	protected Set<Share> issuedShares = new HashSet<Share>();

	@Override
	public void initialize() {
		super.initialize();

		// offer shares at 00:00î
		// has to happen as an event, as at constructor time the transaction
		// bank account is null
		final ITimeSystemEvent offerSharesEvent = new OfferSharesEvent();
		this.timeSystemEvents.add(offerSharesEvent);
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(offerSharesEvent, -1, MonthType.EVERY, DayType.EVERY,
						HourType.HOUR_00);

		// pay dividend
		final ITimeSystemEvent payDividendEvent = new PayDividendEvent();
		this.timeSystemEvents.add(payDividendEvent);
		Simulation
				.getInstance()
				.getTimeSystem()
				.addEvent(payDividendEvent, -1, MonthType.EVERY, DayType.EVERY,
						HourType.EVERY);
	}

	@Transient
	public void deconstruct() {
		super.deconstruct();

		for (Share share : this.issuedShares) {
			PropertyFactory.deleteProperty(share);
		}

		this.bankAccountDividends = null;
	}

	/*
	 * accessors
	 */

	public BankAccount getBankAccountDividends() {
		return bankAccountDividends;
	}

	public Set<Share> getIssuedShares() {
		return issuedShares;
	}

	public void setBankAccountDividends(BankAccount bankAccountDividends) {
		this.bankAccountDividends = bankAccountDividends;
	}

	public void setIssuedShares(Set<Share> issuedShares) {
		this.issuedShares = issuedShares;
	}

	/*
	 * assertions
	 */

	@Transient
	public void assureBankAccountDividends() {
		if (this.isDeconstructed)
			return;

		this.assureBankCustomerAccount();

		// initialize bank account
		if (this.bankAccountDividends == null) {
			// overdraft not allowed
			this.bankAccountDividends = this.primaryBank.openBankAccount(this,
					this.primaryCurrency, false, "dividends",
					TermType.SHORT_TERM, MoneyType.DEPOSITS);
		}
	}

	/*
	 * business logic
	 */

	@Override
	@Transient
	protected BalanceSheet issueBalanceSheet() {
		this.assureBankAccountDividends();

		final BalanceSheet balanceSheet = super.issueBalanceSheet();

		// bank account for paying dividends
		balanceSheet.addBankAccountBalance(this.bankAccountDividends);

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
					JointStockCompany.this.bankAccountDividends,
					bankAccount.getBalance(), "converting profit to dividend");
		}
	}

	public class SettlementMarketEvent implements ISettlementEvent {
		@Override
		public void onEvent(GoodType goodType, double amount,
				double pricePerUnit, Currency currency) {
		}

		@Override
		public void onEvent(Currency commodityCurrency, double amount,
				double pricePerUnit, Currency currency) {
		}

		@Override
		public void onEvent(Property property, double pricePerUnit,
				Currency currency) {
		}
	}

	public class PayDividendEvent implements ITimeSystemEvent {

		@Override
		public void onEvent() {
			if (JointStockCompany.this.issuedShares.size() > 0) {
				JointStockCompany.this.assureBankAccountDividends();

				final double totalDividend = JointStockCompany.this.bankAccountDividends
						.getBalance();

				// dividend to be payed?
				if (totalDividend > 0.0) {
					double totalDividendPayed = 0.0;

					final Currency currency = JointStockCompany.this.bankAccountDividends
							.getCurrency();
					final double dividendPerOwner = totalDividend
							/ JointStockCompany.this.issuedShares.size();

					// pay dividend for each share
					for (Share share : JointStockCompany.this.issuedShares) {
						final Agent owner = PropertyRegister.getInstance()
								.getOwner(share);
						if (owner != null && owner != JointStockCompany.this) {
							if (owner instanceof IShareOwner) {
								final IShareOwner shareOwner = (IShareOwner) owner;
								shareOwner.assureDividendBankAccount();
								if (currency
										.equals(shareOwner
												.getDividendBankAccount()
												.getCurrency())) {
									final double dividend = Math
											.min(dividendPerOwner,
													JointStockCompany.this.bankAccountDividends
															.getBalance());
									JointStockCompany.this.bankAccountDividends
											.getManagingBank()
											.transferMoney(
													JointStockCompany.this.bankAccountDividends,
													shareOwner
															.getDividendBankAccount(),
													dividend, "dividend");
									shareOwner
											.onDividendTransfer(dividendPerOwner);
									totalDividendPayed += dividendPerOwner;
								}
							}
						}
					}
					if (getLog()
							.isAgentSelectedByClient(JointStockCompany.this)) {
						getLog().log(
								JointStockCompany.this,
								PayDividendEvent.class,
								"payed dividend of "
										+ Currency
												.formatMoneySum(totalDividendPayed)
										+ " "
										+ JointStockCompany.this.bankAccountTransactions
												.getCurrency().getIso4217Code());
					}
				}
			}
		}
	}

	public class OfferSharesEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			if (JointStockCompany.this.issuedShares.size() == 0) {
				JointStockCompany.this.assureBankAccountTransactions();

				// issue initial shares
				for (int i = 0; i < ConfigurationUtil.JointStockCompanyConfig
						.getInitialNumberOfShares(); i++) {
					final Share initialShare = PropertyFactory
							.newInstanceShare(JointStockCompany.this,
									JointStockCompany.this);
					JointStockCompany.this.issuedShares.add(initialShare);
					MarketFactory.getInstance().placeSettlementSellingOffer(
							initialShare, JointStockCompany.this,
							JointStockCompany.this.bankAccountTransactions,
							0.0, new SettlementMarketEvent());
				}
			}
		}
	}
}
