package info.chenli.classifier;

import info.chenli.ee.util.FileUtil;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractClassifier {

	private final static Logger logger = Logger
			.getLogger(AbstractClassifier.class.getName());

	private String modelStr = null;

	public void train(List<Instance> trainingInstances, int trainingRound) {

		while (trainingRound-- > 0) {

			train(trainingInstances);
		}
	}

	public abstract void train(List<Instance> trainingInstances);

	/**
	 * Classify the instance and assign a label the instance.
	 * 
	 * @param instance
	 * @return The predicted label.
	 */
	public abstract double predict(Instance instance);

	public abstract String modelToString();

	public abstract void loadModel(File modelFile);

	public void saveModel(File modelFile) {

		modelStr = modelToString();

		logger.info("The model is saved to ".concat(modelFile.getName()));
		FileUtil.saveFile(modelStr, modelFile);
	}

}
