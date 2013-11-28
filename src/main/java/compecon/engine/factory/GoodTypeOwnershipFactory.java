package compecon.engine.factory;

import compecon.economy.agent.Agent;
import compecon.economy.property.GoodTypeOwnership;

public interface GoodTypeOwnershipFactory {

	public void deleteGoodTypeOwnership(
			final GoodTypeOwnership goodTypeOwnership);

	public GoodTypeOwnership newInstanceGoodTypeOwnership(final Agent owner);
}
