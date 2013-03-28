package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Fscore;
import info.chenli.classifier.Instance;
import info.chenli.classifier.InstanceDictionary;
import info.chenli.classifier.PerceptronClassifier;

import java.io.File;
import java.util.Collections;
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
			trainingInstances
					.setTaeDescriptor("/desc/TrainingSetAnnotator.xml");
			List<Instance> instances = trainingInstances
					.getInstances(trainingSet);

			InstanceDictionary dict = new InstanceDictionary();
			dict.creatNumericDictionary(instances);
			dict.saveDictionary(new File("./model/triggers.dict"));

			Collections.shuffle(instances);
			train(instances, 15);

			TokenInstances testInstances = new TokenInstances();
			testInstances.setTaeDescriptor("/desc/TrainingSetAnnotator.xml");
			instances = testInstances.getInstances(new File(
			// "./data/test/PMC-2065877-01-Introduction.txt"));
					"./data/train"));

			instances = dict.instancesToNumeric(instances);
			int total = 0, correct = 0;
			for (Instance instance : instances) {
				double prediction = this.predict(instance);
				if (prediction != dict.getLabelNumeric(String
						.valueOf(EventType.Non_trigger))) {
					System.out.print(instance.getFeaturesString().get(0));
					System.out.print("\t".concat(dict
							.getLabelString(prediction)));
					if (prediction == instance.getLabel()) {
						correct++;
						System.out.print("\t".concat(String.valueOf(true)));
					}
					System.out.println();
				}
				if (!instance.getLabelString().equals(
						String.valueOf(EventType.Non_trigger))) {
					total++;
				}
			}
			System.out.println(String.valueOf(correct).concat("\t")
					.concat(String.valueOf(total)));
		}

	}

	public static void main(String[] args) {

		TriggerRecogniser tr = new TriggerRecogniser();
		tr.train(new File(args[0]), false);

		tr.saveModel(new File("./model/triggers.perceptron.model"));

	}
}
