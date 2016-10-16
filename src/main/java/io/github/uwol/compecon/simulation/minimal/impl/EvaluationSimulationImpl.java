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

package io.github.uwol.compecon.simulation.minimal.impl;

import java.io.IOException;
import java.util.GregorianCalendar;

import io.github.uwol.compecon.economy.materia.GoodType;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContextFactory;
import io.github.uwol.compecon.engine.statistics.PricesModel;
import io.github.uwol.compecon.engine.statistics.ModelRegistry.NationalEconomyModel.IndustryModel;
import io.github.uwol.compecon.engine.statistics.ModelRegistry.NationalEconomyModel.PricingBehaviourModel;
import io.github.uwol.compecon.engine.statistics.ModelRegistry.NationalEconomyModel.UtilityModel;
import io.github.uwol.compecon.engine.statistics.timeseries.PeriodDataAccumulatorTimeSeriesModel;
import io.github.uwol.compecon.engine.timesystem.impl.DayType;
import io.github.uwol.compecon.engine.timesystem.impl.HourType;
import io.github.uwol.compecon.engine.timesystem.impl.MonthType;
import io.github.uwol.compecon.engine.util.HibernateUtil;
import io.github.uwol.compecon.simulation.minimal.csv.impl.M1CsvWriterImpl;
import io.github.uwol.compecon.simulation.minimal.csv.impl.OutputCsvWriterImpl;
import io.github.uwol.compecon.simulation.minimal.csv.impl.PriceCsvWriterImpl;
import io.github.uwol.compecon.simulation.minimal.csv.impl.SoldCsvWriterImpl;
import io.github.uwol.compecon.simulation.minimal.csv.impl.UtilityCsvWriterImpl;

public class EvaluationSimulationImpl {

	public static void main(final String[] args) throws IOException {
		runSimulationIteration(1, 1);
		runSimulationIteration(2, 1);
	}

	protected static M1CsvWriterImpl registerM1FileWriter(final int scenario,
			final int iteration) {
		final PeriodDataAccumulatorTimeSeriesModel m1Model = ApplicationContext
				.getInstance().getModelRegistry()
				.getNationalEconomyModel(Currency.EURO).moneySupplyM1Model;
		final String csvFileName = String.format("csv/%s_%s_m1.csv", scenario,
				iteration);
		final M1CsvWriterImpl m1FileWriter = new M1CsvWriterImpl(csvFileName,
				m1Model);

		m1Model.getTimeSeries().addChangeListener(m1FileWriter);

		return m1FileWriter;
	}

	protected static OutputCsvWriterImpl registerOutputFileWriter(
			final int scenario, final int iteration, final GoodType goodType) {
		final IndustryModel industryModel = ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(Currency.EURO).industryModels
				.get(goodType);
		final String csvFileName = String.format("csv/%s_%s_%s_output.csv",
				scenario, iteration, goodType);
		final OutputCsvWriterImpl outputFileWriter = new OutputCsvWriterImpl(
				csvFileName, industryModel, goodType);

		industryModel.outputModel.getTimeSeries().addChangeListener(
				outputFileWriter);

		return outputFileWriter;
	}

	protected static PriceCsvWriterImpl registerPriceFileWriter(
			final int scenario, final int iteration, final GoodType goodType) {
		final PricesModel pricesModel = ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(Currency.EURO).pricesModel;
		final String csvFileName = String.format("csv/%s_%s_%s_prices.csv",
				scenario, iteration, goodType);
		final PriceCsvWriterImpl priceFileWriter = new PriceCsvWriterImpl(
				csvFileName, pricesModel, goodType);

		pricesModel.registerListener(priceFileWriter);

		return priceFileWriter;
	}

	protected static SoldCsvWriterImpl registerSoldFileWriter(
			final int scenario, final int iteration, final GoodType goodType) {
		final PricingBehaviourModel pricingBehaviourModel = ApplicationContext
				.getInstance().getModelRegistry()
				.getNationalEconomyModel(Currency.EURO)
				.getPricingBehaviourModel(goodType);
		final String csvFileName = String.format("csv/%s_%s_%s_sold.csv",
				scenario, iteration, goodType);
		final SoldCsvWriterImpl soldFileWriter = new SoldCsvWriterImpl(
				csvFileName, pricingBehaviourModel, goodType);

		pricingBehaviourModel.soldModel.getTimeSeries().addChangeListener(
				soldFileWriter);

		return soldFileWriter;
	}

	protected static UtilityCsvWriterImpl registerUtilityFileWriter(
			final int scenario, final int iteration) {
		final UtilityModel utilityModel = ApplicationContext.getInstance()
				.getModelRegistry().getNationalEconomyModel(Currency.EURO).householdsModel.utilityModel;
		final String csvFileName = String.format("csv/%s_%s_utility.csv",
				scenario, iteration);
		final UtilityCsvWriterImpl utilityFileWriter = new UtilityCsvWriterImpl(
				csvFileName, utilityModel);

		utilityModel.utilityOutputModel.getTimeSeries().addChangeListener(
				utilityFileWriter);

		return utilityFileWriter;
	}

	protected static void runSimulationIteration(final int scenario,
			final int iteration) throws IOException {
		System.out.println("running scenario " + scenario + ", iteration "
				+ iteration);

		/*
		 * setup
		 */
		final String configurationPropertiesFilename = System.getProperty(
				"configuration.properties", "minimal.configuration.properties");

		if (HibernateUtil.isActive()) {
			ApplicationContextFactory
					.configureHibernateApplicationContext(configurationPropertiesFilename);
		} else {
			ApplicationContextFactory
					.configureInMemoryApplicationContext(configurationPropertiesFilename);
		}

		/*
		 * register model listeners
		 */
		final PriceCsvWriterImpl coalPriceWriter = registerPriceFileWriter(
				scenario, iteration, GoodType.COAL);
		final PriceCsvWriterImpl wheatPriceWriter = registerPriceFileWriter(
				scenario, iteration, GoodType.WHEAT);
		final PriceCsvWriterImpl labourHourPriceWriter = registerPriceFileWriter(
				scenario, iteration, GoodType.LABOURHOUR);

		final OutputCsvWriterImpl coalOutputWriter = registerOutputFileWriter(
				scenario, iteration, GoodType.COAL);
		final OutputCsvWriterImpl wheatOutputWriter = registerOutputFileWriter(
				scenario, iteration, GoodType.WHEAT);

		final SoldCsvWriterImpl coalSoldWriter = registerSoldFileWriter(
				scenario, iteration, GoodType.COAL);
		final SoldCsvWriterImpl wheatSoldWriter = registerSoldFileWriter(
				scenario, iteration, GoodType.WHEAT);
		final SoldCsvWriterImpl labourHourSoldWriter = registerSoldFileWriter(
				scenario, iteration, GoodType.LABOURHOUR);

		final UtilityCsvWriterImpl utilityWriter = registerUtilityFileWriter(
				scenario, iteration);

		final M1CsvWriterImpl m1Writer = registerM1FileWriter(scenario,
				iteration);

		/*
		 * register exogenous shock
		 */
		if (scenario == 2) {
			ApplicationContext
					.getInstance()
					.getTimeSystem()
					.addEvent(new ExogenousShockEvent(), 2002, MonthType.EVERY,
							DayType.DAY_01, HourType.HOUR_01);
		}

		/*
		 * run simulation
		 */
		ApplicationContext.getInstance().getAgentFactory()
				.constructAgentsFromConfiguration();
		ApplicationContext.getInstance().getSimulationRunner()
				.run(new GregorianCalendar(2003, 12, 31).getTime());
		ApplicationContext.getInstance().getAgentFactory().deconstructAgents();

		/*
		 * close writers
		 */
		coalPriceWriter.close();
		wheatPriceWriter.close();
		labourHourPriceWriter.close();

		coalSoldWriter.close();
		wheatSoldWriter.close();
		labourHourSoldWriter.close();

		coalOutputWriter.close();
		wheatOutputWriter.close();

		m1Writer.close();
		utilityWriter.close();

		/*
		 * reset application context
		 */
		ApplicationContext.getInstance().reset();
	}
}
