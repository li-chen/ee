package info.chenli.ee.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.classifier.InstanceDictionary;
import info.chenli.classifier.PerceptronClassifier;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * 
 * @author Chen Li
 * 
 */
public class TriggerRecogniser extends PerceptronClassifier {

	private final static Logger logger = Logger
			.getLogger(TriggerRecogniser.class.getName());

	public void train(File trainingSet, boolean useSearn) {

		if (useSearn) {

		} else {

			TokenInstances trainingInstances = new TokenInstances();
			trainingInstances.setTaeDescriptor(new File(
					"./desc/TrainingSetAnnotator.xml"));
			List<Instance> instances = trainingInstances
					.getInstances(trainingSet);

			InstanceDictionary dict = new InstanceDictionary();
			dict.creatDictionary(instances);
			dict.saveDictionary(new File("./model/triggers.dict"));

			train(dict.instancesToNumeric(instances));

			TokenInstances testInstances = new TokenInstances();
			testInstances.setTaeDescriptor(new File(
					"./desc/TrainingSetAnnotator.xml"));
			instances = testInstances.getInstances(new File("./data/test/"));
			System.out.println(accuracy(dict.instancesToNumeric(instances)));
		}

	}

	public static void main(String[] args) {

		TriggerRecogniser tr = new TriggerRecogniser();
		tr.train(new File(args[0]), false);

		tr.saveModel(new File("./model/triggers.perceptron.model"));

	}
}
