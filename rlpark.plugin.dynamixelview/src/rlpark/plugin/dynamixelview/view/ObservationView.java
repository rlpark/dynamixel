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

		public IntegerTextClient(String obsLabel, String textLabel) {
			super(textLabel);
			labelIndex = legend().indexOf(obsLabel);
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

	public Legend legend() {
		return instance.current().legend();
	}

	@Override
	protected ObsLayout getObservationLayout() {
		SensorGroup goalsGroup = new SensorGroup("Goals",
				startsWith(DynamixelLabels.Goal), 0, 1023);
		SensorGroup speedGroup = new SensorGroup("Speed",
				startsWith(DynamixelLabels.Speed), 0, 1023);
		SensorGroup loadGroup = new SensorGroup("Load",
				startsWith(DynamixelLabels.Load), -511, 511);
		SensorTextGroup infoGroup = createInfoGroup();
		return new ObsLayout(new ObsWidget[][] { { infoGroup, goalsGroup },
				{ speedGroup, loadGroup } });
	}

	@Override
	protected void setToolbar(IToolBarManager toolBarManager) {
		toolBarManager.add(terminateAction);
	}

	private SensorTextGroup createInfoGroup() {
		TextClient busVoltageTextClient = new TextClient("Motors:") {
			@Override
			public String currentText() {
				DynamixelRobot problem = instance.current();
				if (problem == null)
					return "";
				return String.valueOf(problem.nbMotors());
			}
		};
		TextClient loopTimeTextClient = new TextClient("Loop Time:") {
			@Override
			public String currentText() {
				Clock clock = instance.clock();
				if (clock == null)
					return "00ms";
				return Chrono.toPeriodString(clock.lastPeriodNano());
			}
		};
		return new SensorTextGroup("Info", busVoltageTextClient,
				loopTimeTextClient);
	}

	private int[] startsWith(String prefix) {
		List<Integer> indexes = new ArrayList<Integer>();
		for (Map.Entry<String, Integer> entry : legend().legend().entrySet())
			if (entry.getKey().startsWith(prefix))
				indexes.add(entry.getValue());
		Collections.sort(indexes);
		return Utils.asIntArray(indexes);
	}

	private void setViewTitle() {
		DynamixelRobot problem = instance.current();
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
	protected void setLayout() {
		super.setLayout();
		terminateAction.setEnabled(true);
		setViewTitle();
	}

	@Override
	protected boolean synchronize() {
		currentObservation = instance.current().lastReceivedObs();
		synchronize(currentObservation);
		return true;
	}

	@Override
	public void close() {
		instance.unset();
	}

	@Override
	protected void unsetLayout() {
		super.unsetLayout();
		terminateAction.setEnabled(false);
	}
}
