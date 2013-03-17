package info.chenli.classifier;

import info.chenli.ee.searn.Policy;
import info.chenli.ee.searn.State;
import info.chenli.ee.searn.StructuredInstance;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;

public abstract class Classifier {

	private final static Logger logger = Logger.getLogger(Classifier.class
			.getName());

	private AbstractClassifier classifier;

	public Classifier(AbstractClassifier classifier, Instances instances) {

		this.classifier = classifier;

		try {
			classifier.buildClassifier(instances);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "");
			throw new RuntimeException(e);
		}

	}

	public void setClassifier(AbstractClassifier classifier) {
		this.classifier = classifier;
	}

	public double predict(Instance instance) {

		try {
			return classifier.classifyInstance(instance);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "");
			throw new RuntimeException(e);
		}

	}

}
