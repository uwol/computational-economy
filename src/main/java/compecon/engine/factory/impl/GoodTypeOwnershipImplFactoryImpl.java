package compecon.engine.factory.impl;

import compecon.economy.agent.Agent;
import compecon.economy.property.GoodTypeOwnership;
import compecon.economy.property.impl.GoodTypeOwnershipImpl;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.factory.GoodTypeOwnershipFactory;
import compecon.engine.util.HibernateUtil;

public class GoodTypeOwnershipImplFactoryImpl implements
		GoodTypeOwnershipFactory {

	@Override
	public GoodTypeOwnership newInstanceGoodTypeOwnership(Agent owner) {
		GoodTypeOwnershipImpl goodTypeOwnership = new GoodTypeOwnershipImpl();
		goodTypeOwnership.setAgent(owner);
		ApplicationContext.getInstance().getGoodTypeOwnershipDAO()
				.save(goodTypeOwnership);
		HibernateUtil.flushSession();
		return goodTypeOwnership;
	}

	@Override
	public void deleteGoodTypeOwnership(GoodTypeOwnership goodTypeOwnership) {
		ApplicationContext.getInstance().getGoodTypeOwnershipDAO()
				.delete(goodTypeOwnership);
		HibernateUtil.flushSession();
	}
}
