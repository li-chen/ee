package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.classifier.InstanceDictionary;
import info.chenli.classifier.LibLinearFacade;
import info.chenli.litway.util.Timer;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class BindingRecogniser extends LibLinearFacade {

	private final static Logger logger = Logger
			.getLogger(BindingRecogniser.class.getName());
	private String classifierName = "liblinear";

	public void train(String trainingDir, int round) {
		//
		// collect all instances and fetch syntactical information
		//
		BindingInstances trainingInstances = new BindingInstances();
		trainingInstances.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
		List<Instance> instances = trainingInstances.getInstances(new File(
				trainingDir));
		logger.info(String.valueOf(instances.size()).concat(
				" instances are collected."));

		InstanceDictionary dict = new InstanceDictionary();
		dict.creatNumericDictionary(instances);
		dict.saveDictionary(new File("./model/bindings.".concat(classifierName)
				.concat(".dict")));
		logger.info("Save dictionary.");

		// save instances
		trainingInstances.saveInstances(new File(
				"./model/instances.binding.txt"));
		trainingInstances.saveSvmLightInstances(new File(
				"./model/instances.binding.svm.txt"));

		// shuffle
		Collections.shuffle(instances);
		logger.info("Shuffle instances.");

		Timer timer = new Timer();
		timer.start();

		train(instances, round);
		timer.stop();
		logger.info("Training takes ".concat(String.valueOf(timer
				.getRunningTime())));

		saveModel(new File("./model/bindings.".concat(classifierName).concat(
				".model")));

	}

	public static void main(String[] args) {

		BindingRecogniser br = new BindingRecogniser();
		br.train(args[0], 1);
	}
}