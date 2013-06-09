package compecon.culture.sectors.financial;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import compecon.culture.sectors.household.Household;
import compecon.engine.AgentFactory;
import compecon.engine.dao.DAOFactory;
import compecon.engine.util.HibernateUtil;

public class BankTest {
	double epsilon = 0.0001;

	CentralBank centralBank_EUR;
	CreditBank creditBank1_EUR;
	CreditBank creditBank2_EUR;
	Household household1_EUR;
	Household household2_EUR;

	@Before
	public void setUp() {
		// init database connection

		HibernateUtil.openSession();

		centralBank_EUR = AgentFactory.getInstanceCentralBank(Currency.EURO);
		creditBank1_EUR = AgentFactory.newInstanceCreditBank(Currency.EURO);
		creditBank2_EUR = AgentFactory.newInstanceCreditBank(Currency.EURO);
		household1_EUR = AgentFactory.newInstanceHousehold(Currency.EURO);
		household2_EUR = AgentFactory.newInstanceHousehold(Currency.EURO);

		centralBank_EUR.assureTransactionsBankAccount();

		creditBank1_EUR.assureCentralBankAccount();
		creditBank1_EUR.assureTransactionsBankAccount();
		creditBank1_EUR.assureCurrencyTradeBankAccounts();

		creditBank2_EUR.assureCentralBankAccount();
		creditBank2_EUR.assureTransactionsBankAccount();
		creditBank2_EUR.assureCurrencyTradeBankAccounts();

		household1_EUR.assureTransactionsBankAccount();
		household2_EUR.assureTransactionsBankAccount();

		HibernateUtil.flushSession();
	}

	@After
	public void tearDown() {
		household1_EUR.deconstruct();
		household2_EUR.deconstruct();
		creditBank1_EUR.deconstruct();
		creditBank2_EUR.deconstruct();
		centralBank_EUR.deconstruct();

		HibernateUtil.flushSession();

		// close database conenction
		HibernateUtil.closeSession();
	}

	@Test
	public void transferMoney() {
		assertEquals(0.0, household1_EUR.getTransactionsBankAccount()
				.getBalance(), epsilon);
		Bank source = household1_EUR.getTransactionsBankAccount()
				.getManagingBank();
		Bank target = household2_EUR.getTransactionsBankAccount()
				.getManagingBank();
		for (int i = 1; i < 1000; i++) {
			source.transferMoney(household1_EUR.getTransactionsBankAccount(),
					household2_EUR.getTransactionsBankAccount(), 10,
					household1_EUR.getBankPasswords().get(source),
					"Transaction" + i);
			assertEquals(-10.0 * i, household1_EUR.getTransactionsBankAccount()
					.getBalance(), epsilon);
			assertEquals(10.0 * i, household2_EUR.getTransactionsBankAccount()
					.getBalance(), epsilon);
		}
	}

	@Test
	public void findAllBankAccountsManagedByBank() {
		int size1 = DAOFactory.getBankAccountDAO()
				.findAllBankAccountsManagedByBank(creditBank1_EUR).size();
		int size2 = DAOFactory.getBankAccountDAO()
				.findAllBankAccountsManagedByBank(creditBank2_EUR).size();

		assertEquals(6, size1 + size2);
	}
}
