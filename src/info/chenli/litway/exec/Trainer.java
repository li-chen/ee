package info.chenli.litway.exec;

import info.chenli.litway.config.Configuration;

import java.io.File;
import java.util.logging.Logger;

public class Trainer {

	private final static Logger logger = Logger.getLogger(Trainer.class
			.getName());

	public void collectInstances() {

		Configuration conf = new Configuration("");

		TriggerInstances ti = new TriggerInstances();
		ti.getInstances(new File(""), "Trigger");

		for (String arg : conf.getArgTypes()) {
			ArgumentInstances ai = new ArgumentInstances();
			ai.getInstances(new File(""), arg);
		}

	}

	private void trainArgument(String arg) {
	}

}
