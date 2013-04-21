package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Accurary;
import info.chenli.classifier.Instance;
import info.chenli.classifier.InstanceDictionary;
import info.chenli.classifier.LibLinearFacade;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * 
 * @author Chen Li
 * 
 */
public class ThemeRecogniser extends LibLinearFacade {

	private final static Logger logger = Logger.getLogger(ThemeRecogniser.class
			.getName());
	private final String classifierName = "liblinear";

	public void train(File trainingSet, boolean useSearn) {

		if (useSearn) {

		} else {

			InstanceDictionary dict = new InstanceDictionary();

			ThemeInstances trainingInstances = new ThemeInstances();
			trainingInstances
					.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
			List<Instance> instances = trainingInstances
					.getInstances(trainingSet);

			dict.creatNumericDictionary(instances);
			dict.saveDictionary(new File("./model/themes.".concat(
					classifierName).concat(".dict")));

			trainingInstances.saveInstances(new File(
					"./model/instances.theme.txt"));
			trainingInstances.saveSvmLightInstances(new File(
					"./model/instances.theme.svm.txt"));

			train(dict.instancesToNumeric(instances));

			// System.out.println(accuracy(instances));

			ThemeInstances testInstances = new ThemeInstances();
			testInstances.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
			instances = testInstances.getInstances(new File(
					"./data/development/"));
			instances = dict.instancesToNumeric(instances);
			testInstances.saveSvmLightInstances(new File(
					"./model/instances.theme.svm.dev.txt"));

			// System.out.println(accuracy(instances));
		}

	}

	public static void main(String[] args) {

		ThemeRecogniser tr = new ThemeRecogniser();
		tr.loadModel(new File("./model/themes.".concat(tr.classifierName)
				.concat(".model")));

		InstanceDictionary dict = new InstanceDictionary();
		dict.loadDictionary(new File("./model/themes."
				.concat(tr.classifierName).concat(".dict")));

		ThemeInstances ti = new ThemeInstances();
		ti.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");

		List<Instance> instances = ti.getInstances(new File(args[0]));

		instances = dict.instancesToNumeric(instances);

		int total = 0, correct = 0;
		for (Instance instance : instances) {
			int prediction = tr.predict(instance);
			System.out.print(instance.getLabel() + ":" + prediction);
			for (String[] values : instance.getFeaturesString()) {
				for (String value : values) {
					System.out.print("\t" + value);
				}
			}
			System.out.println();
			for (int value : instance.getFeaturesNumeric()) {
				System.out.print("\t" + value);
			}
			System.out.println();
			if (prediction == instance.getLabel()) {
				correct++;
			}
			total++;
		}
		System.out.println(new Accurary(correct, total));
	}
}
