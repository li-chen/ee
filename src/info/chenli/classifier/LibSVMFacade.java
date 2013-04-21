package info.chenli.classifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;

public class LibSVMFacade extends AbstractClassifier {

	private final static Logger logger = Logger.getLogger(LibSVMFacade.class
			.getName());

	private svm_model model;
	private int predict_probability = 1;

	@Override
	public void train(List<Instance> trainingInstances) {
		throw new UnsupportedOperationException("Unimplemented.");

	}

	@Override
	public int predict(int[] featureSparseVector) {

		int svm_type = svm.svm_get_svm_type(model);
		int nr_class = svm.svm_get_nr_class(model);
		double[] prob_estimates = null;

		if (predict_probability == 1) {
			if (svm.svm_check_probability_model(model) == 0) {
				logger.severe("Model does not support probabiliy estimates.");
				throw new RuntimeException(
						"Model does not support probabiliy estimates.");
			}
			if (svm_type == svm_parameter.EPSILON_SVR
					|| svm_type == svm_parameter.NU_SVR) {
				logger.info("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="
						+ svm.svm_get_svr_probability(model) + "\n");
			} else {
				int[] labels = new int[nr_class];
				svm.svm_get_labels(model, labels);
				prob_estimates = new double[nr_class];
				logger.info("labels");
				for (int j = 0; j < nr_class; j++) {
					logger.info(" " + labels[j]);
				}
			}
		} else {
			if (svm.svm_check_probability_model(model) != 0) {
				logger.info("Model supports probability estimates, but disabled in prediction.\n");
			}
		}

		svm_node[] instance = new svm_node[featureSparseVector.length];
		int i = 0;
		for (int index : featureSparseVector) {

			instance[i] = new svm_node();

			instance[i].index = index;
			instance[i].value = Double.valueOf(1).doubleValue();

			i++;
		}

		if (predict_probability == 1
				&& (svm_type == svm_parameter.C_SVC || svm_type == svm_parameter.NU_SVC)) {
			// It is adhoc to set probability integer during testing.
			return (int) svm.svm_predict_probability(model, instance,
					prob_estimates);
		} else {
			return (int) svm.svm_predict(model, instance);
		}
	}

	@Override
	public String modelToString() {
		throw new UnsupportedOperationException("Unimplemented.");
	}

	@Override
	public void loadModel(File modelFile) {

		try {
			model = svm.svm_load_model(new BufferedReader(new FileReader(
					modelFile)));

		} catch (FileNotFoundException e) {
			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		}

	}

}
