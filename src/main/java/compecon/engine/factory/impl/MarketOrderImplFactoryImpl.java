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

package compecon.engine.factory.impl;

import compecon.economy.markets.MarketOrder;
import compecon.economy.markets.MarketParticipant;
import compecon.economy.markets.impl.MarketOrderImpl;
import compecon.economy.materia.GoodType;
import compecon.economy.property.Property;
import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.factory.MarketOrderFactory;
import compecon.engine.util.HibernateUtil;

public class MarketOrderImplFactoryImpl implements MarketOrderFactory {

	@Override
	public void deleteAllSellingOrders(final MarketParticipant offeror) {
		ApplicationContext.getInstance().getMarketOrderDAO()
				.deleteAllSellingOrders(offeror);
		HibernateUtil.flushSession();
	}

	@Override
	public void deleteAllSellingOrders(final MarketParticipant offeror,
			final Currency denominatedInCurrency,
			final Class<? extends Property> propertyClass) {
		ApplicationContext
				.getInstance()
				.getMarketOrderDAO()
				.deleteAllSellingOrders(offeror, denominatedInCurrency,
						propertyClass);
		HibernateUtil.flushSession();
	}

	@Override
	public void deleteAllSellingOrders(final MarketParticipant offeror,
			final Currency denominatedInCurrency,
			final Currency commodityCurrency) {
		ApplicationContext
				.getInstance()
				.getMarketOrderDAO()
				.deleteAllSellingOrders(offeror, denominatedInCurrency,
						commodityCurrency);
		HibernateUtil.flushSession();
	}

	@Override
	public void deleteAllSellingOrders(final MarketParticipant offeror,
			final Currency denominatedInCurrency, final GoodType goodType) {
		ApplicationContext
				.getInstance()
				.getMarketOrderDAO()
				.deleteAllSellingOrders(offeror, denominatedInCurrency,
						goodType);
		HibernateUtil.flushSession();
	}

	@Override
	public void deleteSellingOrder(final MarketOrder marketOrder) {
		ApplicationContext.getInstance().getMarketOrderDAO()
				.delete(marketOrder);
		HibernateUtil.flushSession();
	}

	@Override
	public MarketOrderImpl newInstanceCurrencyMarketOrder(
			final Currency currencyToBeOffered,
			final MarketParticipant offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double amount,
			final double pricePerUnit,
			final BankAccountDelegate commodityCurrencyOfferorsBankAcountDelegate) {
		assert (currencyToBeOffered != null);
		assert (offeror != null);
		assert (offerorsBankAcountDelegate != null);
		assert (commodityCurrencyOfferorsBankAcountDelegate != null);

		final MarketOrderImpl marketOrder = new MarketOrderImpl();
		if (!HibernateUtil.isActive()) {
			marketOrder.setId(ApplicationContext.getInstance()
					.getSequenceNumberGenerator().getNextId());
		}
		marketOrder.setCurrency(offerorsBankAcountDelegate.getBankAccount()
				.getCurrency());
		marketOrder.setCommodityCurrency(currencyToBeOffered);
		marketOrder.setOfferor(offeror);
		marketOrder.setOfferorsBankAcountDelegate(offerorsBankAcountDelegate);
		marketOrder.setAmount(amount);
		marketOrder.setPricePerUnit(pricePerUnit);
		marketOrder
				.setCommodityCurrencyOfferorsBankAccountDelegate(commodityCurrencyOfferorsBankAcountDelegate);

		ApplicationContext.getInstance().getMarketOrderDAO().save(marketOrder);
		HibernateUtil.flushSession();
		return marketOrder;
	}

	@Override
	public MarketOrderImpl newInstanceGoodTypeMarketOrder(
			final GoodType goodType, final MarketParticipant offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double amount, final double pricePerUnit) {
		assert (goodType != null);
		assert (offeror != null);
		assert (offerorsBankAcountDelegate != null);

		final MarketOrderImpl marketOrder = new MarketOrderImpl();
		if (!HibernateUtil.isActive()) {
			marketOrder.setId(ApplicationContext.getInstance()
					.getSequenceNumberGenerator().getNextId());
		}
		marketOrder.setCurrency(offerorsBankAcountDelegate.getBankAccount()
				.getCurrency());
		marketOrder.setGoodType(goodType);
		marketOrder.setOfferor(offeror);
		marketOrder.setOfferorsBankAcountDelegate(offerorsBankAcountDelegate);
		marketOrder.setAmount(amount);
		marketOrder.setPricePerUnit(pricePerUnit);

		ApplicationContext.getInstance().getMarketOrderDAO().save(marketOrder);
		HibernateUtil.flushSession();
		return marketOrder;
	}

	@Override
	public MarketOrderImpl newInstancePropertyMarketOrder(
			final Property property, final MarketParticipant offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double pricePerUnit) {
		assert (property != null);
		assert (offeror != null);
		assert (offerorsBankAcountDelegate != null);

		final MarketOrderImpl marketOrder = new MarketOrderImpl();
		if (!HibernateUtil.isActive()) {
			marketOrder.setId(ApplicationContext.getInstance()
					.getSequenceNumberGenerator().getNextId());
		}
		marketOrder.setCurrency(offerorsBankAcountDelegate.getBankAccount()
				.getCurrency());
		marketOrder.setProperty(property);
		marketOrder.setOfferor(offeror);
		marketOrder.setOfferorsBankAcountDelegate(offerorsBankAcountDelegate);
		marketOrder.setAmount(1);
		marketOrder.setPricePerUnit(pricePerUnit);

		ApplicationContext.getInstance().getMarketOrderDAO().save(marketOrder);
		HibernateUtil.flushSession();
		return marketOrder;
	}

}
