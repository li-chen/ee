package info.chenli.classifier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

/**
 * 
 * @author Chen Li
 * 
 */
public class LibLinearFacade extends AbstractClassifier {

	private final static Logger logger = Logger.getLogger(LibLinearFacade.class
			.getName());

	private Model model;

	public void train(List<Instance> instances) {

		Problem problem = new Problem();
		problem.l = instances.size(); // number of training examples

		int featureNum = 0;

		for (Instance instance : instances) {
			for (int index : instance.getFeaturesNumeric()) {
				if (index > featureNum) {
					featureNum = index;
				}
			}
		}
		problem.n = featureNum; // number of features
		problem.x = new Feature[instances.size()][]; // feature nodes
		problem.y = new double[instances.size()]; // target values
		int i = 0;
		for (Instance instance : instances) {
			problem.y[i] = instance.getLabel();
			// System.out.print(instance.getLabel());

			int previousIndex = 0;
			List<Feature> featureNodes = new ArrayList<Feature>();
			for (int index : instance.getFeaturesNumeric()) {
				if (index > previousIndex) {
					featureNodes.add(new FeatureNode(index, 1));
					// System.out.print("\t" + (index ));
				}
				previousIndex = index;
			}
			problem.x[i] = new FeatureNode[featureNodes.size()];
			problem.x[i] = featureNodes.toArray(problem.x[i]);
			i++;
			// System.out.println();
		}

		SolverType solver = SolverType.MCSVM_CS; // -s 4
		double C = 1.0; // cost of constraints violation
		double eps = 0.1; // stopping criteria

		Parameter parameter = new Parameter(solver, C, eps);
		// for (Feature[] feature : problem.x) {
		// for (Feature value : feature) {
		// System.out.print(value.getIndex() + "\t");
		// }
		// System.out.println();
		// }
		// System.out.println(problem == null);
		// System.out.println(parameter == null);
		model = Linear.train(problem, parameter);
		// Linear.crossValidation(problem, parameter, 10, problem.y);

	}

	@Override
	public int predict(int[] featureSparseVector) {

		if (featureSparseVector == null) {
			throw new IllegalArgumentException(
					"Empty sparse vector. This probably due to that the dictionary hasn't converted instances to numeric features yet.");
		}

		int n;
		int nr_feature = this.model.getNrFeature();
		if (this.model.getBias() >= 0) {
			n = nr_feature + 1;
		} else {
			n = nr_feature;
		}

		List<Feature> featureNodes = new ArrayList<Feature>();
		int previousIndex = 0;
		for (int index : featureSparseVector) {
			if (index > previousIndex) {
				featureNodes.add(new FeatureNode(index, 1));
			}
			previousIndex = index;
		}
		if (model.getBias() >= 0) {
			Feature node = new FeatureNode(n, model.getBias());
			featureNodes.add(node);
		}
		Feature[] instance = new FeatureNode[featureNodes.size()];
		instance = featureNodes.toArray(instance);
		return (int) Math.round(Linear.predict(this.model, instance));

	}

	@Override
	public String modelToString() {
		throw new UnsupportedOperationException("Unimplemented method.");
	}

	@Override
	public void saveModel(File modelFile) {

		try {
			model.save(modelFile);
		} catch (IOException e) {
			logger.severe("Error when saving mode: ".concat(modelFile
					.getAbsolutePath()));
			throw new RuntimeException(e);
		}
	}

	@Override
	public void loadModel(File modelFile) {

		try {
			this.model = Model.load(modelFile);
		} catch (IOException e) {
			logger.severe("Error when loading mode: ".concat(modelFile
					.getAbsolutePath()));
			throw new RuntimeException(e);
		}

	}

}