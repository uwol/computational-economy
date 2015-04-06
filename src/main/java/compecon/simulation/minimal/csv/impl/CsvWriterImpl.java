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

package compecon.simulation.minimal.csv.impl;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

public abstract class CsvWriterImpl {

	protected final String COMMA_DELIMITER = ";";

	protected final String NEW_LINE_SEPARATOR = "\n";

	protected FileWriter writer;

	public CsvWriterImpl(final String csvFileName) {
		try {
			writer = new FileWriter(csvFileName);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			writer.flush();
			writer.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	protected void writeCsvLine(final String... values) {
		try {
			writer.append(StringUtils.join(values, COMMA_DELIMITER));
			writer.append(NEW_LINE_SEPARATOR);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
