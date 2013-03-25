package info.chenli.ee.bionlp13.ge;

import info.chenli.classifier.PerceptronClassifier;

import java.io.File;
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

			TokenInstances ti = new TokenInstances();
			ti.setTaeDescriptor(new File("./desc/BioNLPSyntacticAnnotator.xml"));

			train(ti.getInstances(trainingSet));
		}

	}

	public static void main(String[] args) {

		TriggerRecogniser tr = new TriggerRecogniser();
		tr.train(new File(args[0]), false);

		tr.saveModel(new File("./model/triggers.perceptron.model"));

	}
}
