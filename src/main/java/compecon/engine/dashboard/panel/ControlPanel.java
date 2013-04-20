/*
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import compecon.Simulation;
import compecon.culture.sectors.financial.Currency;
import compecon.engine.AgentFactory;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.TimeSystem;
import compecon.nature.materia.GoodType;

public class ControlPanel extends JPanel {

	private final int SLIDER_MAX = 100;

	JButton singleStepButton;

	public ControlPanel() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

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
		add(millisecondsToSleepPerHourType);

		singleStepButton = new JButton("Step");
		singleStepButton.setEnabled(false);
		singleStepButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Simulation.setSingleStep();
			}
		});
		add(singleStepButton);

		add(new JSeparator(JSeparator.HORIZONTAL));

		JButton initFarmButton = new JButton("Init Farm");
		initFarmButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TimeSystem.getInstance().addExternalEvent(
						new ITimeSystemEvent() {
							@Override
							public void onEvent() {
								AgentFactory.newInstanceFarm();
							}
						});
			}
		});
		add(initFarmButton);

		JButton initFactoryButton = new JButton("Init Factory");
		initFactoryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TimeSystem.getInstance().addExternalEvent(
						new ITimeSystemEvent() {
							@Override
							public void onEvent() {
								AgentFactory
										.newInstanceFactory(GoodType.KILOWATT);
							}
						});
			}
		});
		add(initFactoryButton);

		JButton init100HouseholdsButton = new JButton("Init 100 Households");
		init100HouseholdsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TimeSystem.getInstance().addExternalEvent(
						new ITimeSystemEvent() {
							@Override
							public void onEvent() {
								for (int i = 0; i < 100; i++)
									AgentFactory.newInstanceHousehold();
							}
						});
			}
		});
		add(init100HouseholdsButton);

		JButton doDeficitSpendingButton = new JButton("Deficit spending");
		doDeficitSpendingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TimeSystem.getInstance().addExternalEvent(
						new ITimeSystemEvent() {
							@Override
							public void onEvent() {
								AgentFactory.getInstanceState(Currency.EURO)
										.doDeficitSpending();
							}
						});
			}
		});
		add(doDeficitSpendingButton);
	}
}
