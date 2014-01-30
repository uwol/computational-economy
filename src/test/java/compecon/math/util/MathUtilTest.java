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

package compecon.math.util;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import compecon.CompEconTestSupport;

public class MathUtilTest extends CompEconTestSupport {

	@Before
	public void setup() throws IOException {
		super.setUpApplicationContext(testConfigurationPropertiesFilename);
		super.setUpTestAgents();
	}

	@After
	public void tearDown() {
		super.tearDown();
	}

	@Test
	public void testCalculateMonthlyNominalInterestRate3() {
		final double effectiveInterestRate = 0.03;
		final double monthlyNominalInterestRate = MathUtil
				.calculateMonthlyNominalInterestRate(effectiveInterestRate);

		double effectiveInterestRateCheck = Math.pow(
				monthlyNominalInterestRate + 1.0, 12) - 1.0;

		Assert.assertEquals(effectiveInterestRate, effectiveInterestRateCheck,
				epsilon);
	}

	@Test
	public void testCalculateMonthlyNominalInterestRate10() {
		final double effectiveInterestRate = 0.1;
		final double monthlyNominalInterestRate = MathUtil
				.calculateMonthlyNominalInterestRate(effectiveInterestRate);

		double effectiveInterestRateCheck = Math.pow(
				monthlyNominalInterestRate + 1.0, 12) - 1.0;

		Assert.assertEquals(effectiveInterestRate, effectiveInterestRateCheck,
				epsilon);
	}
}
