package info.chenli.litway.bionlp13.ge;

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
public class ThemeRecogniser extends PerceptronClassifier {

	private final static Logger logger = Logger.getLogger(ThemeRecogniser.class
			.getName());

	public void train(File trainingSet, boolean useSearn) {

		if (useSearn) {

		} else {

			InstanceDictionary dict = new InstanceDictionary();

			ThemeInstances trainingInstances = new ThemeInstances();
			trainingInstances.setTaeDescriptor(new File(
					"./desc/TrainingSetAnnotator.xml"));
			List<Instance> instances = trainingInstances
					.getInstances(trainingSet);

			dict.creatDictionary(instances);
			dict.saveDictionary(new File("./model/themes.dict"));

			train(dict.instancesToNumeric(instances));

			System.out.println(accuracy(instances));

			ThemeInstances testInstances = new ThemeInstances();
			testInstances.setTaeDescriptor(new File(
					"./desc/TrainingSetAnnotator.xml"));
			instances = testInstances.getInstances(new File("./data/test/"));

			System.out.println(accuracy(dict.instancesToNumeric(instances)));
		}

	}

	public static void main(String[] args) {

		ThemeRecogniser tr = new ThemeRecogniser();
		tr.train(new File(args[0]), false);
		tr.saveModel(new File("./model/themes.perceptron.model"));

	}
}
