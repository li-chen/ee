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
public class CauseRecogniser extends PerceptronClassifier {

	private final static Logger logger = Logger.getLogger(CauseRecogniser.class
			.getName());

	public void train(File trainingSet, boolean useSearn) {

		if (useSearn) {

		} else {

			InstanceDictionary dict = new InstanceDictionary();

			CauseInstances trainingInstances = new CauseInstances();
			trainingInstances.setTaeDescriptor(new File(
					"./desc/TrainingSetAnnotator.xml"));
			List<Instance> instances = trainingInstances
					.getInstances(trainingSet);

			dict.creatDictionary(instances);
			dict.saveDictionary(new File("./model/causes.dict"));

			this.train(dict.instancesToNumeric(instances));

			System.out.println(this.accuracy(instances));

			CauseInstances testInstances = new CauseInstances();
			testInstances.setTaeDescriptor(new File(
					"./desc/TrainingSetAnnotator.xml"));
			instances = testInstances.getInstances(new File("./data/test/"));

			System.out.println(this.accuracy(dict.instancesToNumeric(instances)));
		}

	}

	public static void main(String[] args) {

		CauseRecogniser tr = new CauseRecogniser();
		tr.train(new File(args[0]), false);
		tr.saveModel(new File("./model/causes.perceptron.model"));

	}
}
