/*
Copyright (C) 2015 u.wol@wwu.de

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

package compecon.engine.random.impl;

import java.util.Random;

import compecon.engine.random.RandomNumberGenerator;

public class StochasticNumberGeneratorImpl implements RandomNumberGenerator {

	protected Random random = new Random();

	@Override
	public int nextInt() {
		return random.nextInt();
	}

	@Override
	public int nextInt(final int bound) {
		return random.nextInt(bound);
	}
}
