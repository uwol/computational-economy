package compecon.engine.factory.impl;

import compecon.economy.agent.Agent;
import compecon.economy.sectors.financial.Bank;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.financial.impl.BankAccountImpl;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.factory.BankAccountFactory;
import compecon.engine.util.HibernateUtil;

public class BankAccountImplFactoryImpl implements BankAccountFactory {

	public void deleteBankAccount(final BankAccount bankAccount) {
		ApplicationContext.getInstance().getBankAccountDAO()
				.delete(bankAccount);
		HibernateUtil.flushSession();
	}

	@Override
	public void deleteAllBankAccounts(final Bank managingBank) {
		ApplicationContext.getInstance().getBankAccountDAO()
				.deleteAllBankAccounts(managingBank);
		HibernateUtil.flushSession();
	}

	@Override
	public void deleteAllBankAccounts(final Bank managingBank, final Agent owner) {
		ApplicationContext.getInstance().getBankAccountDAO()
				.deleteAllBankAccounts(managingBank, owner);
		HibernateUtil.flushSession();
	}

	public BankAccount newInstanceBankAccount(final Agent owner,
			final Currency currency, boolean overdraftPossible,
			final Bank managingBank, final String name,
			final TermType termType, final MoneyType moneyType) {
		BankAccountImpl bankAccount = new BankAccountImpl();
		if (!HibernateUtil.isActive())
			bankAccount.setId(ApplicationContext.getInstance()
					.getSequenceNumberGenerator().getNextId());

		bankAccount.setOwner(owner);
		bankAccount.setOverdraftPossible(overdraftPossible);
		bankAccount.setCurrency(currency);
		bankAccount.setManagingBank(managingBank);
		bankAccount.setName(name);
		bankAccount.setTermType(termType);
		bankAccount.setMoneyType(moneyType);

		ApplicationContext.getInstance().getBankAccountDAO().save(bankAccount);
		HibernateUtil.flushSession();
		return bankAccount;
	}
}
