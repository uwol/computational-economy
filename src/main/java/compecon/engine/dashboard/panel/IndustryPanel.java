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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import compecon.economy.sectors.financial.Currency;
import compecon.economy.sectors.state.law.bookkeeping.BalanceSheet;
import compecon.engine.Simulation;
import compecon.engine.dashboard.panel.BalanceSheetPanel.BalanceSheetTableModel;
import compecon.materia.GoodType;
import compecon.materia.GoodType.Sector;
import compecon.materia.InputOutputModel;

public class IndustryPanel extends AbstractChartsPanel {

	public IndustryPanel() {
		this.setLayout(new BorderLayout());

		JTabbedPane jTabbedPaneCurrency = new JTabbedPane();

		for (Currency currency : Currency.values()) {
			JPanel panelForCurrency = new JPanel();
			jTabbedPaneCurrency.addTab(currency.getIso4217Code(),
					panelForCurrency);

			panelForCurrency.setLayout(new BorderLayout());

			JTabbedPane jTabbedPaneSector = new JTabbedPane();
			panelForCurrency.add(jTabbedPaneSector);

			// sector 1
			JPanel panelForSector1 = new JPanel();
			jTabbedPaneSector.addTab(Sector.PRIMARY + " Sector",
					panelForSector1);
			panelForSector1.setLayout(new GridLayout(0, 3));
			panelForSector1.setBackground(Color.lightGray);

			// sector 2
			JPanel panelForSector2 = new JPanel();
			jTabbedPaneSector.addTab(Sector.SECONDARY + " Sector",
					panelForSector2);
			panelForSector2.setLayout(new GridLayout(0, 3));
			panelForSector2.setBackground(Color.lightGray);

			// sector 3
			JPanel panelForSector3 = new JPanel();
			jTabbedPaneSector.addTab(Sector.TERTIARY + " Sector",
					panelForSector3);
			panelForSector3.setLayout(new GridLayout(0, 3));
			panelForSector3.setBackground(Color.lightGray);

			for (GoodType outputGoodType : GoodType.values()) {
				if (!outputGoodType.equals(GoodType.LABOURHOUR)) {
					Sector sector = outputGoodType.getSector();
					switch (sector) {
					case PRIMARY:
						panelForSector1.add(createProductionPanel(currency,
								outputGoodType));
						panelForSector1.add(createFactoryBalanceSheetPanel(
								currency, outputGoodType));
						break;
					case SECONDARY:
						panelForSector2.add(createProductionPanel(currency,
								outputGoodType));
						panelForSector2.add(createFactoryBalanceSheetPanel(
								currency, outputGoodType));
						break;
					case TERTIARY:
						panelForSector3.add(createProductionPanel(currency,
								outputGoodType));
						panelForSector3.add(createFactoryBalanceSheetPanel(
								currency, outputGoodType));
						break;
					}
				}
			}
		}

		add(jTabbedPaneCurrency, BorderLayout.CENTER);
	}

	protected ChartPanel createProductionPanel(Currency currency,
			GoodType outputGoodType) {
		TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

		timeSeriesCollection.addSeries(Simulation.getInstance()
				.getModelRegistry()
				.getGoodTypeProductionModel(currency, outputGoodType)
				.getOutputModel().getTimeSeries());
		for (GoodType inputGoodType : Simulation.getInstance()
				.getModelRegistry()
				.getGoodTypeProductionModel(currency, outputGoodType)
				.getInputGoodTypes()) {
			timeSeriesCollection.addSeries(Simulation.getInstance()
					.getModelRegistry()
					.getGoodTypeProductionModel(currency, outputGoodType)
					.getInputModel(inputGoodType).getTimeSeries());
		}

		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				outputGoodType.toString() + " Output", "Date", "Output",
				(XYDataset) timeSeriesCollection, true, true, false);
		configureChart(chart);
		chart.addSubtitle(new TextTitle("Inputs: "
				+ InputOutputModel.getProductionFunction(outputGoodType)
						.getInputGoodTypes().toString()));
		return new ChartPanel(chart);
	}

	protected JPanel createFactoryBalanceSheetPanel(final Currency currency,
			final GoodType goodType) {
		final BalanceSheetTableModel balanceSheetTableModel = new BalanceSheetTableModel(
				currency) {
			@Override
			protected BalanceSheet getModelData() {
				return Simulation.getInstance().getModelRegistry()
						.getBalanceSheetsModel(referenceCurrency)
						.getFactoryNationalAccountsBalanceSheet(goodType);
			}
		};
		return new BalanceSheetPanel(currency, balanceSheetTableModel,
				"Balance Sheet for " + currency.getIso4217Code() + " "
						+ goodType + " Factories");
	}
}
