package info.chenli.ee.bionlp13.ge;

import info.chenli.ee.corpora.Trigger;
import info.chenli.ee.searn.CSVotedPerceptron;
import info.chenli.ee.searn.Trainer;

import java.io.File;
import java.util.Map;

/**
 * 
 * @author Chen Li
 * 
 */
public class TriggerRecogniser {

	Map<Integer, Trigger> getTriggers(File trainingDir, File testDir) {

		Trainer trainer = new Trainer(new CSVotedPerceptron());
		TokenInstances ti = new TokenInstances();

		ti.fetchInstances(trainingDir);

		trainer.train(ti.getStructuredInstances());

		for (File file : testDir.listFiles()) {
			train
		}
	}

}
