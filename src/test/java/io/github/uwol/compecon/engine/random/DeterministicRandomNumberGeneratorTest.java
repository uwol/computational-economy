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

package io.github.uwol.compecon.engine.random;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.github.uwol.compecon.CompEconTestSupport;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.random.RandomNumberGenerator;
import io.github.uwol.compecon.engine.random.impl.DeterministicNumberGeneratorImpl;

public class DeterministicRandomNumberGeneratorTest extends CompEconTestSupport {

	@Before
	public void setup() throws IOException {
		super.setUpApplicationContext(testConfigurationPropertiesFilename);

		ApplicationContext.getInstance().setRandomNumberGenerator(
				new DeterministicNumberGeneratorImpl());
	}

	@Override
	@After
	public void tearDown() {
		super.tearDown();
	}

	@Test
	public void testGetAgentMethods() {
		final RandomNumberGenerator randomNumberGenerator = ApplicationContext
				.getInstance().getRandomNumberGenerator();

		final int nextInt = randomNumberGenerator.nextInt();
		final int nextInt2 = randomNumberGenerator.nextInt();
		final int nextInt3 = randomNumberGenerator.nextInt();

		Assert.assertEquals(-1193959466, nextInt);
		Assert.assertEquals(-1139614796, nextInt2);
		Assert.assertEquals(837415749, nextInt3);
	}
}
