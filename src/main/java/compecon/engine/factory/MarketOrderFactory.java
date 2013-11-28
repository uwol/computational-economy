package compecon.engine.factory;

import compecon.economy.agent.Agent;
import compecon.economy.markets.MarketOrder;
import compecon.economy.materia.GoodType;
import compecon.economy.property.Property;
import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.sectors.financial.Currency;

public interface MarketOrderFactory {

	public MarketOrder newInstanceGoodTypeMarketOrder(final GoodType goodType,
			final Agent offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double amount, final double pricePerUnit);

	public MarketOrder newInstanceCurrencyMarketOrder(
			final Currency currencyToBeOffered,
			final Agent offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double amount,
			final double pricePerUnit,
			final BankAccountDelegate commodityCurrencyOfferorsBankAcountDelegate);

	public MarketOrder newInstancePropertyMarketOrder(final Property property,
			final Agent offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double pricePerUnit);
}
