package compecon.engine.factory.impl;

import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.State;
import compecon.economy.sectors.state.impl.StateImpl;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.factory.StateFactory;
import compecon.engine.util.HibernateUtil;

public class StateImplFactoryImpl implements StateFactory {

	@Override
	public void deleteState(final State agent) {
		ApplicationContext.getInstance().getStateDAO().delete((State) agent);
		HibernateUtil.flushSession();
	}

	@Override
	public State newInstanceState(final Currency currency) {
		StateImpl state = new StateImpl();
		if (!HibernateUtil.isActive()) {
			state.setId(ApplicationContext.getInstance()
					.getSequenceNumberGenerator().getNextId());
		}

		state.setUtilityFunction(ApplicationContext.getInstance()
				.getInputOutputModel().getUtilityFunctionOfState());

		state.setPrimaryCurrency(currency);
		ApplicationContext.getInstance().getStateDAO().save(state);
		state.initialize();
		HibernateUtil.flushSession();
		return state;
	}

}
