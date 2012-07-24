package rlpark.plugin.dynamixelview.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IToolBarManager;

import rlpark.plugin.dynamixel.data.DynamixelLabels;
import rlpark.plugin.dynamixel.robot.DynamixelRobot;
import rlpark.plugin.rltoys.envio.observations.Legend;
import rlpark.plugin.rltoys.utils.Utils;
import zephyr.plugin.core.api.synchronization.Chrono;
import zephyr.plugin.core.api.synchronization.Clock;
import zephyr.plugin.core.api.synchronization.Closeable;
import zephyr.plugin.core.internal.actions.TerminateAction;
import zephyr.plugin.core.internal.helpers.ClassViewProvider;
import zephyr.plugin.core.internal.observations.EnvironmentView;
import zephyr.plugin.core.internal.observations.ObsLayout;
import zephyr.plugin.core.internal.observations.ObsWidget;
import zephyr.plugin.core.internal.observations.SensorGroup;
import zephyr.plugin.core.internal.observations.SensorTextGroup;
import zephyr.plugin.core.internal.observations.SensorTextGroup.TextClient;

@SuppressWarnings({ "synthetic-access", "restriction" })
public class ObservationView extends EnvironmentView<DynamixelRobot> implements
		Closeable {
	static public class Provider extends ClassViewProvider {
		public Provider() {
			super(DynamixelRobot.class);
		}
	}

	protected class IntegerTextClient extends TextClient {
		private final int labelIndex;

		public IntegerTextClient(Legend legend, String obsLabel, String textLabel) {
			super(textLabel);
			labelIndex = legend.indexOf(obsLabel);
		}

		@Override
		public String currentText() {
			if (labelIndex < 0 || currentObservation == null)
				return "0000";
			return String.valueOf((int) currentObservation[labelIndex]);
		}
	}

	double[] currentObservation;
	private final TerminateAction terminateAction;

	public ObservationView() {
		terminateAction = new TerminateAction(this);
		terminateAction.setEnabled(false);
	}

	@Override
	protected ObsLayout getObservationLayout(Clock clock, DynamixelRobot robot) {
		Legend legend = robot.legend();
		SensorGroup goalsGroup = new SensorGroup("Goals",
				startsWith(legend, DynamixelLabels.Goal), 0, 1023);
		SensorGroup speedGroup = new SensorGroup("Speed",
				startsWith(legend, DynamixelLabels.Speed), 0, 1023);
		SensorGroup loadGroup = new SensorGroup("Load",
				startsWith(legend, DynamixelLabels.Load), -511, 511);
		SensorTextGroup infoGroup = createInfoGroup(clock, robot);
		return new ObsLayout(new ObsWidget[][] { { infoGroup, goalsGroup },
				{ speedGroup, loadGroup } });
	}

	@Override
	protected void setToolbar(IToolBarManager toolBarManager) {
		toolBarManager.add(terminateAction);
	}

	private SensorTextGroup createInfoGroup(final Clock clock, final DynamixelRobot robot) {
		TextClient busVoltageTextClient = new TextClient("Motors:") {
			@Override
			public String currentText() {
				return String.valueOf(robot.nbMotors());
			}
		};
		TextClient loopTimeTextClient = new TextClient("Loop Time:") {
			@Override
			public String currentText() {
				return Chrono.toPeriodString(clock.lastPeriodNano());
			}
		};
		return new SensorTextGroup("Info", busVoltageTextClient,
				loopTimeTextClient);
	}

	private int[] startsWith(Legend legend, String prefix) {
		List<Integer> indexes = new ArrayList<Integer>();
		for (Map.Entry<String, Integer> entry : legend.legend().entrySet())
			if (entry.getKey().startsWith(prefix))
				indexes.add(entry.getValue());
		Collections.sort(indexes);
		return Utils.asIntArray(indexes);
	}

	private void setViewTitle(DynamixelRobot problem) {
		if (problem == null)
			setViewName("Observation", "");
		String viewTitle = problem.getClass().getSimpleName();
		setViewName(viewTitle, "");
	}

	@Override
	public void dispose() {
		close();
		super.dispose();
	}

	@Override
	protected boolean isInstanceSupported(Object instance) {
		return DynamixelRobot.class.isInstance(instance);
	}

	@Override
	protected void setLayout(Clock clock, DynamixelRobot robot) {
		super.setLayout(clock, robot);
		terminateAction.setEnabled(true);
		setViewTitle(robot);
	}

	@Override
	protected boolean synchronize(DynamixelRobot robot) {
		currentObservation = robot.lastReceivedObs();
		synchronize(currentObservation);
		return true;
	}

	@Override
	protected void unsetLayout() {
		super.unsetLayout();
		terminateAction.setEnabled(false);
	}
}
