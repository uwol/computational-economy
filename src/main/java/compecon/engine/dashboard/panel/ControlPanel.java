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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import compecon.economy.sectors.financial.Currency;
import compecon.engine.Simulation;
import compecon.engine.time.ITimeSystemEvent;

public class ControlPanel extends JPanel {

	private final int SLIDER_MAX = 100;

	protected final JLabel dateTimeLabel = new JLabel();

	public ControlPanel() {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		this.setPreferredSize(new Dimension(200, -1));

		this.add(createSpeedSliderPanel());

		this.add(new JSeparator(SwingConstants.HORIZONTAL));

		this.add(createEconomicSectorsPane());
	}

	protected JPanel createSpeedSliderPanel() {
		JPanel speedSliderPanel = new JPanel();

		final JButton dayStepButton = new JButton("+ 1 Day");
		final JButton hourStepButton = new JButton("+ 1 Hour");

		speedSliderPanel.add(this.dateTimeLabel);

		JSlider millisecondsToSleepPerHourType = new JSlider(
				JSlider.HORIZONTAL, 0, SLIDER_MAX, SLIDER_MAX);
		millisecondsToSleepPerHourType.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					double sliderValue = (int) source.getValue();
					double invertedSliderValue = SLIDER_MAX - sliderValue;
					double millisecondsToSleep = ((invertedSliderValue * invertedSliderValue) / (SLIDER_MAX * SLIDER_MAX)) * 3000.0 / 24.0;
					Simulation.getInstance().setMillisecondsToSleepPerHourType(
							(int) millisecondsToSleep);
					if (invertedSliderValue >= SLIDER_MAX - 10.0) {
						Simulation.getInstance().setPaused(true);
						dayStepButton.setEnabled(true);
						hourStepButton.setEnabled(true);
					} else {
						Simulation.getInstance().setPaused(false);
						dayStepButton.setEnabled(false);
						hourStepButton.setEnabled(false);
					}
				}
			}
		});
		// Turn on labels at major tick marks.
		millisecondsToSleepPerHourType.setMajorTickSpacing(10);
		millisecondsToSleepPerHourType.setPaintTicks(true);
		millisecondsToSleepPerHourType.setPaintLabels(true);
		speedSliderPanel.add(millisecondsToSleepPerHourType);

		dayStepButton.setEnabled(false);
		dayStepButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Simulation.getInstance().setSingleDayStep();
			}
		});
		speedSliderPanel.add(dayStepButton);

		hourStepButton.setEnabled(false);
		hourStepButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Simulation.getInstance().setSingleHourStep();
			}
		});
		speedSliderPanel.add(hourStepButton);

		return speedSliderPanel;
	}

	protected JTabbedPane createEconomicSectorsPane() {
		JTabbedPane economicSectorsPane = new JTabbedPane();

		for (final Currency currency : Currency.values()) {
			JPanel economicSectorPane = new JPanel();
			economicSectorsPane.addTab(currency.getIso4217Code(),
					economicSectorPane);
			economicSectorPane.setLayout(new BoxLayout(economicSectorPane,
					BoxLayout.PAGE_AXIS));

			/*
			 * init economic growth
			 */

			JButton initEconomicGrowthButton = new JButton("Economic growth");
			initEconomicGrowthButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Simulation.getInstance().getTimeSystem()
							.addExternalEvent(new ITimeSystemEvent() {
								@Override
								public void onEvent() {
									Simulation.getInstance().getModelRegistry()
											.getControlModel()
											.initEconomicGrowth(currency);
								}
							});
				}
			});
			economicSectorPane.add(initEconomicGrowthButton);

			/*
			 * deficit spending
			 */

			JButton doDeficitSpendingButton = new JButton("Deficit spending");
			doDeficitSpendingButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Simulation.getInstance().getTimeSystem()
							.addExternalEvent(new ITimeSystemEvent() {
								@Override
								public void onEvent() {
									Simulation.getInstance().getModelRegistry()
											.getControlModel()
											.deficitSpending(currency);
								}
							});
				}
			});
			economicSectorPane.add(doDeficitSpendingButton);

			/*
			 * init households
			 */

			JButton init100HouseholdsButton = new JButton("Init 100 Households");
			init100HouseholdsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Simulation.getInstance().getTimeSystem()
							.addExternalEvent(new ITimeSystemEvent() {
								@Override
								public void onEvent() {
									Simulation.getInstance().getModelRegistry()
											.getControlModel()
											.initHouseholds(currency);
								}
							});
				}
			});
			economicSectorPane.add(init100HouseholdsButton);
		}
		return economicSectorsPane;
	}

	public void refreshDateTime() {
		this.dateTimeLabel.setText(new SimpleDateFormat().format(Simulation
				.getInstance().getTimeSystem().getCurrentDate()));
	}
}
