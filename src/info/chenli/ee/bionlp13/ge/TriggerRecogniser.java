package info.chenli.ee.bionlp13.ge;

import info.chenli.ee.corpora.Trigger;
import info.chenli.ee.searn.CSVotedPerceptron;
import info.chenli.ee.searn.StructuredInstance;
import info.chenli.ee.searn.Trainer;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.VotedPerceptron;
import weka.classifiers.meta.MultiClassClassifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

/**
 * 
 * @author Chen Li
 * 
 */
public class TriggerRecogniser extends TokenInstances {

	private final static Logger logger = Logger
			.getLogger(TriggerRecogniser.class.getName());

	private AbstractClassifier classifier;

	void train(File trainingDir, boolean useSearn) {

		TokenInstances ti = new TokenInstances();

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

			classifier = new MultiClassClassifier();

			try {
			    StringToWordVector filter = new StringToWordVector();
			    filter.setInputFormat(ti.getInstances());
			    Instances dataFiltered = Filter.useFilter(ti.getInstances(), filter);
				classifier.buildClassifier(dataFiltered);
			} catch (Exception e) {

				logger.log(Level.SEVERE, e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}

	}

	Map<Integer, Trigger> getTriggers(File file) {

		TriggerRecogniser tr = new TriggerRecogniser();
		tr.fetchInstances(file);

		try {
			// files have to be preprocessed (sentencised, tokenized).
			for (Instance instance : tr.getInstances()) {
				System.out.println(instance == null);
				System.out.println(instance.hasMissingValue());
				System.out.println(null == classifier);
				classifier.classifyInstance(instance);
				instance.value(classes);
			}

		} catch (Exception e) {

			logger.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}

		return null;
	}

	@Override
	public File getTaeDescriptor() {

		return new File("./desc/BioNLPSyntacticAnnotator.xml");
	}

	public static void main(String[] args) {

		TriggerRecogniser tr = new TriggerRecogniser();
		tr.train(new File(args[0]), false);
		tr.getTriggers(new File(args[1]));

	}
}
