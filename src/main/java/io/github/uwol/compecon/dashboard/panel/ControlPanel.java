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

package io.github.uwol.compecon.dashboard.panel;

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

import io.github.uwol.compecon.dashboard.model.ControlModel;
import io.github.uwol.compecon.economy.sectors.financial.Currency;
import io.github.uwol.compecon.engine.applicationcontext.ApplicationContext;
import io.github.uwol.compecon.engine.statistics.NotificationListenerModel.ModelListener;
import io.github.uwol.compecon.engine.timesystem.TimeSystemEvent;

public class ControlPanel extends JPanel implements ModelListener {

	protected final ControlModel controlModel = new ControlModel();

	protected final JLabel dateTimeLabel = new JLabel();

	private final int SLIDER_MAX = 100;

	public ControlPanel() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		setPreferredSize(new Dimension(200, -1));

		this.add(createSpeedSliderPanel());

		this.add(new JSeparator(SwingConstants.HORIZONTAL));

		this.add(createEconomicSectorsPane());

		ApplicationContext.getInstance().getModelRegistry()
				.getTimeSystemModel().registerListener(this);
	}

	protected JTabbedPane createEconomicSectorsPane() {
		final JTabbedPane economicSectorsPane = new JTabbedPane();

		for (final Currency currency : Currency.values()) {
			final JPanel economicSectorPane = new JPanel();
			economicSectorsPane.addTab(currency.getIso4217Code(),
					economicSectorPane);
			economicSectorPane.setLayout(new BoxLayout(economicSectorPane,
					BoxLayout.PAGE_AXIS));

			/*
			 * init economic growth
			 */

			final JButton initEconomicGrowthButton = new JButton(
					"Economic growth");
			initEconomicGrowthButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					ApplicationContext.getInstance().getTimeSystem()
							.addExternalEvent(new TimeSystemEvent() {
								@Override
								public boolean isDeconstructed() {
									return false;
								}

								@Override
								public void onEvent() {
									controlModel.initEconomicGrowth(currency);
								}
							});
				}
			});
			economicSectorPane.add(initEconomicGrowthButton);

			/*
			 * init economic contraction
			 */

			final JButton initEconomicContractionButton = new JButton(
					"Economic contraction");
			initEconomicContractionButton
					.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							ApplicationContext.getInstance().getTimeSystem()
									.addExternalEvent(new TimeSystemEvent() {
										@Override
										public boolean isDeconstructed() {
											return false;
										}

										@Override
										public void onEvent() {
											controlModel
													.initEconomicContraction(currency);
										}
									});
						}
					});
			economicSectorPane.add(initEconomicContractionButton);

			/*
			 * deficit spending
			 */

			final JButton doDeficitSpendingButton = new JButton(
					"Deficit spending");
			doDeficitSpendingButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					ApplicationContext.getInstance().getTimeSystem()
							.addExternalEvent(new TimeSystemEvent() {
								@Override
								public boolean isDeconstructed() {
									return false;
								}

								@Override
								public void onEvent() {
									controlModel.deficitSpending(currency);
								}
							});
				}
			});
			economicSectorPane.add(doDeficitSpendingButton);

			/*
			 * init households
			 */

			final JButton init100HouseholdsButton = new JButton(
					"Init 100 Households");
			init100HouseholdsButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					ApplicationContext.getInstance().getTimeSystem()
							.addExternalEvent(new TimeSystemEvent() {
								@Override
								public boolean isDeconstructed() {
									return false;
								}

								@Override
								public void onEvent() {
									controlModel.initHouseholds(currency);
								}
							});
				}
			});
			economicSectorPane.add(init100HouseholdsButton);
		}
		return economicSectorsPane;
	}

	protected JPanel createSpeedSliderPanel() {
		final JPanel speedSliderPanel = new JPanel();

		final JButton dayStepButton = new JButton("+ 1 Day");
		final JButton hourStepButton = new JButton("+ 1 Hour");

		speedSliderPanel.add(dateTimeLabel);

		final JSlider millisecondsToSleepPerHourType = new JSlider(
				JSlider.HORIZONTAL, 0, SLIDER_MAX, SLIDER_MAX);
		millisecondsToSleepPerHourType.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				final JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					final double sliderValue = source.getValue();
					final double invertedSliderValue = SLIDER_MAX - sliderValue;
					final double millisecondsToSleep = ((invertedSliderValue * invertedSliderValue) / (SLIDER_MAX * SLIDER_MAX)) * 3000.0 / 24.0;
					ApplicationContext
							.getInstance()
							.getSimulationRunner()
							.setMillisecondsToSleepPerHourType(
									(int) millisecondsToSleep);
					if (invertedSliderValue >= SLIDER_MAX - 10.0) {
						ApplicationContext.getInstance().getSimulationRunner()
								.setPaused(true);
						dayStepButton.setEnabled(true);
						hourStepButton.setEnabled(true);
					} else {
						ApplicationContext.getInstance().getSimulationRunner()
								.setPaused(false);
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
			@Override
			public void actionPerformed(final ActionEvent e) {
				ApplicationContext.getInstance().getSimulationRunner()
						.stepSingleDay();
			}
		});
		speedSliderPanel.add(dayStepButton);

		hourStepButton.setEnabled(false);
		hourStepButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				ApplicationContext.getInstance().getSimulationRunner()
						.stepSingleHour();
			}
		});
		speedSliderPanel.add(hourStepButton);

		return speedSliderPanel;
	}

	@Override
	public void notifyListener() {
		refreshDateTime();
	}

	private void refreshDateTime() {
		dateTimeLabel.setText(new SimpleDateFormat().format(ApplicationContext
				.getInstance().getTimeSystem().getCurrentDate()));
	}
}
