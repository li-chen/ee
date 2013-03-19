package info.chenli.ee.bionlp13.ge;

import info.chenli.classifier.Classifier;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.classifiers.meta.MultiClassClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * 
 * @author Chen Li
 * 
 */
public class TriggerRecogniser extends Classifier {

	private final static Logger logger = Logger
			.getLogger(TriggerRecogniser.class.getName());

	@Override
	public void train(File trainingSet) {

		train(trainingSet, false);

	}

	public void train(File trainingDir, boolean useSearn) {

		TokenInstances ti = new TokenInstances();
		ti.setTaeDescriptor(new File("./desc/BioNLPSyntacticAnnotator.xml"));

		// prepare instances
		ti.fetchInstances(trainingDir);

		if (useSearn) {
			// initial the SEARN trainer
			// Trainer trainer = new Trainer(new CSVotedPerceptron());
			//
			// // train
			// trainer.train(ti.getStructuredInstances());
			//
			// // save the model to a physical file for later use
			// classifier = trainer.getModel();
		} else {

			this.setClassifier(new MultiClassClassifier());

			try {
				StringToWordVector filter = new StringToWordVector();
				filter.setInputFormat(ti.getInstances());
				Instances dataFiltered = Filter.useFilter(ti.getInstances(),
						filter);
				getClassifier().buildClassifier(dataFiltered);
			} catch (Exception e) {

				logger.log(Level.SEVERE, e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}

	}

	@Override
	public void classify(Instance instance) {

		if (null == this.getClassifier()) {
			loadModel();
		}

		try {

			this.getClassifier().classifyInstance(instance);

		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}

	}

	public static void main(String[] args) {

		TriggerRecogniser tr = new TriggerRecogniser();
		tr.train(new File(args[0]), false);
		tr.setModelFileName("./model/triggers.multiclassifier.model");
		tr.saveModel();

	}
}
