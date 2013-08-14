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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import compecon.Simulation;
import compecon.culture.sectors.financial.Currency;
import compecon.engine.jmx.model.ModelRegistry;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.TimeSystem;

public class ControlPanel extends JPanel {

	private final int SLIDER_MAX = 100;

	JButton singleStepButton;

	JTabbedPane economicSectorsPane = new JTabbedPane();

	public ControlPanel() {

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		/*
		 * speed slider
		 */
		JSlider millisecondsToSleepPerHourType = new JSlider(
				JSlider.HORIZONTAL, 0, SLIDER_MAX, SLIDER_MAX);
		millisecondsToSleepPerHourType.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting()) {
					double sliderValue = (int) source.getValue();
					double invertedSliderValue = SLIDER_MAX - sliderValue;
					double millisecondsToSleep = ((invertedSliderValue * invertedSliderValue) / (SLIDER_MAX * SLIDER_MAX)) * 3000 / 24;
					Simulation
							.setMillisecondsToSleepPerHourType((int) millisecondsToSleep);
					if (invertedSliderValue >= SLIDER_MAX - 10) {
						Simulation.setPaused(true);
						singleStepButton.setEnabled(true);
					} else {
						Simulation.setPaused(false);
						singleStepButton.setEnabled(false);
					}
				}
			}
		});
		// Turn on labels at major tick marks.
		millisecondsToSleepPerHourType.setMajorTickSpacing(10);
		millisecondsToSleepPerHourType.setPaintTicks(true);
		millisecondsToSleepPerHourType.setPaintLabels(true);
		this.add(millisecondsToSleepPerHourType);

		singleStepButton = new JButton("Step");
		singleStepButton.setEnabled(false);
		singleStepButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Simulation.setSingleStep();
			}
		});
		this.add(singleStepButton);

		this.add(new JSeparator(SwingConstants.HORIZONTAL));

		/*
		 * tabbed panel for sectors
		 */

		this.add(this.economicSectorsPane);

		for (final Currency currency : Currency.values()) {
			JPanel economicSectorPane = new JPanel();
			this.economicSectorsPane.addTab(currency.getIso4217Code(),
					economicSectorPane);
			economicSectorPane.setLayout(new BoxLayout(economicSectorPane,
					BoxLayout.PAGE_AXIS));

			/*
			 * init economic growth
			 */

			JButton initEconomicGrowthButton = new JButton("Economic growth");
			initEconomicGrowthButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					TimeSystem.getInstance().addExternalEvent(
							new ITimeSystemEvent() {
								@Override
								public void onEvent() {
									ModelRegistry.getControlModel()
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
					TimeSystem.getInstance().addExternalEvent(
							new ITimeSystemEvent() {
								@Override
								public void onEvent() {
									ModelRegistry.getControlModel()
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
					TimeSystem.getInstance().addExternalEvent(
							new ITimeSystemEvent() {
								@Override
								public void onEvent() {
									ModelRegistry.getControlModel()
											.initHouseholds(currency);
								}
							});
				}
			});
			economicSectorPane.add(init100HouseholdsButton);

			/*
			 * init car factory
			 */

			JButton initCarFactoryButton = new JButton("Init car factory");
			initCarFactoryButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					TimeSystem.getInstance().addExternalEvent(
							new ITimeSystemEvent() {
								@Override
								public void onEvent() {
									ModelRegistry.getControlModel()
											.initCarFactory(currency);
								}
							});
				}
			});
			economicSectorPane.add(initCarFactoryButton);
		}
	}
}
