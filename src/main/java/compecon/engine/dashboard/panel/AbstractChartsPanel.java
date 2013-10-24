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

package compecon.engine.dashboard.panel;

import java.awt.Color;
import java.text.SimpleDateFormat;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

import compecon.economy.PricingBehaviour.PricingBehaviourNewPriceDecisionCause;
import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.engine.Simulation;
import compecon.engine.dashboard.panel.BalanceSheetPanel.BalanceSheetTableModel;
import compecon.materia.GoodType;

public abstract class AbstractChartsPanel extends JPanel {

	protected static void configureChart(JFreeChart chart) {
		chart.setBackgroundPaint(Color.white);
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

		DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
		NumberAxis valueAxis = (NumberAxis) plot.getRangeAxis();

		dateAxis.setDateFormatOverride(new SimpleDateFormat("dd-MMM"));
		valueAxis.setAutoRangeIncludesZero(true);
		valueAxis.setUpperMargin(0.15);
		valueAxis.setLowerMargin(0.15);
	}

	protected JPanel createHouseholdBalanceSheetPanel(final Currency currency) {
		final BalanceSheetTableModel balanceSheetTableModel = new BalanceSheetTableModel(
				currency) {
			@Override
			protected BalanceSheet getBalanceSheet() {
				return Simulation.getInstance().getModelRegistry()
						.getNationalEconomyModel(currency).balanceSheetsModel
						.getHouseholdNationalAccountsBalanceSheet();
			}
		};
		return new BalanceSheetPanel(currency, balanceSheetTableModel,
				"Balance Sheet for " + currency.getIso4217Code()
						+ " Households");
	}

	protected JPanel createFactoryBalanceSheetPanel(final Currency currency,
			final GoodType goodType) {
		final BalanceSheetTableModel balanceSheetTableModel = new BalanceSheetTableModel(
				currency) {
			@Override
			protected BalanceSheet getBalanceSheet() {
				return Simulation.getInstance().getModelRegistry()
						.getNationalEconomyModel(currency).balanceSheetsModel
						.getFactoryNationalAccountsBalanceSheet(goodType);
			}
		};
		return new BalanceSheetPanel(currency, balanceSheetTableModel,
				"Balance Sheet for " + currency.getIso4217Code() + " "
						+ goodType + " Factories");
	}

	protected JPanel createFactoryBalanceSheetPanel(final Currency currency) {
		final BalanceSheetTableModel balanceSheetTableModel = new BalanceSheetTableModel(
				currency) {
			@Override
			protected BalanceSheet getBalanceSheet() {
				return Simulation.getInstance().getModelRegistry()
						.getNationalEconomyModel(currency).balanceSheetsModel
						.getFactoryNationalAccountsBalanceSheet();
			}
		};
		return new BalanceSheetPanel(currency, balanceSheetTableModel,
				"Balance Sheet for " + currency.getIso4217Code() + " Factories");
	}

	protected JPanel createCreditBankBalanceSheetPanel(final Currency currency) {
		final BalanceSheetTableModel balanceSheetTableModel = new BalanceSheetTableModel(
				currency) {
			@Override
			protected BalanceSheet getBalanceSheet() {
				return Simulation.getInstance().getModelRegistry()
						.getNationalEconomyModel(currency).balanceSheetsModel
						.getCreditBankNationalAccountsBalanceSheet();
			}
		};
		return new BalanceSheetPanel(currency, balanceSheetTableModel,
				"Balance Sheet for " + currency.getIso4217Code()
						+ " Credit Banks");
	}

	protected JPanel createCentralBankBalanceSheetPanel(final Currency currency) {
		final BalanceSheetTableModel balanceSheetTableModel = new BalanceSheetTableModel(
				currency) {
			@Override
			protected BalanceSheet getBalanceSheet() {
				return Simulation.getInstance().getModelRegistry()
						.getNationalEconomyModel(currency).balanceSheetsModel
						.getCentralBankNationalAccountsBalanceSheet();
			}
		};
		return new BalanceSheetPanel(currency, balanceSheetTableModel,
				"Balance Sheet for " + currency.getIso4217Code()
						+ " Central Bank");
	}

	protected JPanel createTraderBalanceSheetPanel(final Currency currency) {
		final BalanceSheetTableModel balanceSheetTableModel = new BalanceSheetTableModel(
				currency) {
			@Override
			protected BalanceSheet getBalanceSheet() {
				return Simulation.getInstance().getModelRegistry()
						.getNationalEconomyModel(currency).balanceSheetsModel
						.getTraderNationalAccountsBalanceSheet();
			}
		};
		return new BalanceSheetPanel(currency, balanceSheetTableModel,
				"Balance Sheet for " + currency.getIso4217Code() + " Traders");
	}

	protected JPanel createStateBalanceSheetPanel(final Currency currency) {
		final BalanceSheetTableModel balanceSheetTableModel = new BalanceSheetTableModel(
				currency) {
			@Override
			protected BalanceSheet getBalanceSheet() {
				return Simulation.getInstance().getModelRegistry()
						.getNationalEconomyModel(currency).balanceSheetsModel
						.getStateNationalAccountsBalanceSheet();
			}
		};
		return new BalanceSheetPanel(currency, balanceSheetTableModel,
				"Balance Sheet for " + currency.getIso4217Code() + " State");
	}

	protected ChartPanel createPricingBehaviourMechanicsPanel(
			Currency currency, GoodType goodType) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		for (PricingBehaviourNewPriceDecisionCause decisionCause : PricingBehaviourNewPriceDecisionCause
				.values()) {
			timeSeriesCollection
					.addSeries(Simulation.getInstance().getModelRegistry()
							.getNationalEconomyModel(currency).pricingBehaviourModels
							.get(goodType).pricingBehaviourPriceDecisionCauseModels
							.get(decisionCause).getTimeSeries());
		}

		timeSeriesCollection
				.addSeries(Simulation.getInstance().getModelRegistry()
						.getNationalEconomyModel(currency).pricingBehaviourModels
						.get(goodType).pricingBehaviourAveragePriceDecisionCauseModel
						.getTimeSeries());

		JFreeChart chart = ChartFactory.createTimeSeriesChart(goodType
				+ " Pricing Behaviour Mechanics", "Date", "Budget Spent",
				(XYDataset) timeSeriesCollection, true, true, false);
		configureChart(chart);
		return new ChartPanel(chart);
	}
}
