package info.chenli.classifier;

import info.chenli.ee.util.MathUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PerceptronClassifier extends AbstractClassifier {

	private final static Logger logger = Logger
			.getLogger(PerceptronClassifier.class.getName());

	private Map<Double, List<Double>> weights = null;

	private void initWeights(List<Instance> trainingInstances) {

		int featureNum = trainingInstances.get(0).getFeatures().size();
		List<Double> labels = new ArrayList<Double>();

		for (Instance instance : trainingInstances) {
			if (!labels.contains(instance.getLabel())) {
				labels.add(instance.getLabel());
			}
		}

		if (null == weights || weights.size() == 0) {
			weights = new HashMap<Double, List<Double>>();

			for (Double label : labels) {

				List<Double> features = new ArrayList<Double>();
				for (int i = 0; i < featureNum; i++) {
					features.add(0.0);
				}
				weights.put(label, features);
			}
		}

	}

	@Override
	public void train(List<Instance> trainingInstances) {

		initWeights(trainingInstances);

		for (Instance instance : trainingInstances) {

			double prediction = predict(instance);

			if (prediction != instance.getLabel()) {

				try {

					this.weights.put(instance.getLabel(), MathUtil.add(
							this.weights.get(instance.getLabel()),
							MathUtil.multiply(instance.getFeatures(),
									instance.getLabel())));

					this.weights.put(prediction, MathUtil.subtract(this.weights
							.get(prediction), MathUtil.multiply(
							instance.getFeatures(), prediction)));

				} catch (IllegalArgumentException e) {

					logger.log(Level.SEVERE, e.getMessage(), e);
					throw new RuntimeException(e);
				}
			}
		}

	}

	public void printWeights() {

		for (double label : weights.keySet()) {

			System.out.print(label);

			for (double feature : weights.get(label)) {
				System.out.print("\t".concat(String.valueOf(feature)));
			}

			System.out.println();
		}

	}

	@Override
	public double predict(Instance instance) {

		double prediction = 0;
		double max = 0;

		for (double label : weights.keySet()) {

			List<Double> weight = weights.get(label);
			double newPrediction = MathUtil.dot(instance.getFeatures(), weight);

			if (newPrediction > max) {
				prediction = label;
				max = newPrediction;
			}
		}

		return prediction;
	}

	public double accuracy(Instance[] instances) {

		int correct = 0;
		int total = 0;
		for (int i = 0; i < instances.length; i++) {
			int predicted_label = (int) predict(instances[i]);
			if (predicted_label == (int) instances[i].getLabel())
				correct++;
			total++;
		}
		return (double) correct / total;
	}

	@Override
	public String modelToString() {

		StringBuffer sb = new StringBuffer();

		for (double label : weights.keySet()) {

			sb.append(String.valueOf(label));

			for (double weight : weights.get(label)) {
				sb.append("\t").append(String.valueOf(weight));
			}

			sb.append("\n");
		}

		return sb.toString();
	}

	public static void main(String[] args) throws IOException {
	}

}
