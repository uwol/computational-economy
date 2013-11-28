package compecon.engine.factory;

import compecon.economy.agent.Agent;
import compecon.economy.sectors.financial.Bank;
import compecon.economy.sectors.financial.BankAccount;
import compecon.economy.sectors.financial.BankAccount.MoneyType;
import compecon.economy.sectors.financial.BankAccount.TermType;
import compecon.economy.sectors.financial.Currency;

public interface BankAccountFactory {

	public void deleteBankAccount(final BankAccount bankAccount);

	public void deleteAllBankAccounts(final Bank managingBank);

	public void deleteAllBankAccounts(final Bank managingBank, Agent owner);

	public BankAccount newInstanceBankAccount(final Agent owner,
			final Currency currency, boolean overdraftPossible,
			final Bank managingBank, final String name,
			final TermType termType, final MoneyType moneyType);
}
