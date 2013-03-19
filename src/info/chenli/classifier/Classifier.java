package info.chenli.classifier;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.meta.MultiClassClassifier;
import weka.core.Instance;

public abstract class Classifier {

	private final static Logger logger = Logger.getLogger(Classifier.class
			.getName());

	private AbstractClassifier classifier = null;
	private String modelFileName = null;

	public abstract void train(File trainingSet);

	public abstract void classify(Instance instance);

	public void setClassifier(AbstractClassifier classifier) {
		this.classifier = classifier;
	}

	public AbstractClassifier getClassifier() {
		return this.classifier;
	}

	public void saveModel() {

		try {
			weka.core.SerializationHelper.write(this.getModelFileName(), classifier);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public void loadModel() {

		try {
			classifier = (MultiClassClassifier) weka.core.SerializationHelper
					.read(this.getModelFileName());

		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public String getModelFileName() {
		return modelFileName;
	}

	public void setModelFileName(String modelFileName) {
		this.modelFileName = modelFileName;
	}

}
