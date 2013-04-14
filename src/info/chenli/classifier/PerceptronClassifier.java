package info.chenli.classifier;

import info.chenli.litway.util.MathUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PerceptronClassifier extends AbstractClassifier {

	private final static Logger logger = Logger
			.getLogger(PerceptronClassifier.class.getName());

	private Map<Integer, List<Integer>> weights = null;

	private void initWeights(List<Instance> instances) {

		if (null == weights || weights.size() == 0) {

			logger.info("Initial weight.");
			List<Integer> labels = new ArrayList<Integer>();

			int inputNeuronNumber = 0;

			for (Instance instance : instances) {
				if (!labels.contains(instance.getLabel())) {
					labels.add(instance.getLabel());
				}
				int lastFeatureIndex = instance.getFeaturesNumeric()[instance
						.getFeaturesNumeric().length - 1];
				if (lastFeatureIndex + 1 > inputNeuronNumber) {
					inputNeuronNumber = lastFeatureIndex + 1;
				}
			}

			weights = new HashMap<Integer, List<Integer>>();

			for (int label : labels) {

				int i = inputNeuronNumber;
				List<Integer> weightsOfALabel = new ArrayList<Integer>();
				while (i-- > 0) {
					weightsOfALabel.add(0);
				}

				weights.put(label, weightsOfALabel);
			}

			logger.info("Weights["
					.concat(String.valueOf(weights.size()))
					.concat("][")
					.concat(String.valueOf(weights.values().iterator().next()
							.size())).concat("] are set to 0."));
		}

	}

	@Override
	public void train(List<Instance> instances) {

		this.initWeights(instances);

		//
		// randomize order
		//

		logger.info("Start training.");
		for (Instance instance : instances) {

			int prediction = predict(instance.getFeaturesNumeric());

			if (prediction != instance.getLabel()) {

				try {

					this.weights.put(instance.getLabel(), MathUtil.add(
							this.weights.get(instance.getLabel()),
							instance.getFeaturesNumeric()));

					this.weights.put(prediction, MathUtil.subtract(
							this.weights.get(prediction),
							instance.getFeaturesNumeric()));

				} catch (IllegalArgumentException e) {

					logger.log(Level.SEVERE, e.getMessage(), e);
					throw new RuntimeException(e);
				}
			}
		}

		logger.info("End training.");

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
	public int predict(int[] featureSparseVector) {

		Iterator<Integer> weightsIter = weights.keySet().iterator();

		int prediction = weightsIter.next();
		int max = MathUtil.dot(weights.get(prediction), featureSparseVector);

		while (weightsIter.hasNext()) {

			int label = weightsIter.next();
			int newPrediction = MathUtil.dot(weights.get(label),
					featureSparseVector);

			if (newPrediction > max) {
				prediction = label;
				max = newPrediction;
			}
		}

		return prediction;
	}

	public Accurary accuracy(List<Instance> instances) {

		int correct = 0;
		int total = 0;
		for (Instance instance : instances) {
			double predicted_label = predict(instance.getFeaturesNumeric());

			if (predicted_label == instance.getLabel()) {
				correct++;
			}
			total++;
		}

		return new Accurary(correct, total);
	}

	@Override
	public String modelToString() {

		StringBuffer sb = new StringBuffer();

		for (Integer label : weights.keySet()) {

			sb.append(String.valueOf(label));

			int i = 0;
			for (Integer weight : weights.get(label)) {
				sb.append("\t")
				// .append(String.valueOf(i++)).append("-")
						.append(String.valueOf(weight));
			}

			sb.append("\n");
		}

		return sb.toString();
	}

	@Override
	public void loadModel(File modelFile) {

		try {

			BufferedReader br = new BufferedReader(new FileReader(modelFile));

			String line;
			this.weights = new TreeMap<Integer, List<Integer>>();

			while ((line = br.readLine()) != null) {

				StringTokenizer st = new StringTokenizer(line, "\t");
				int label = Integer.parseInt(st.nextToken());

				List<Integer> weightVector = new ArrayList<Integer>();
				while (st.hasMoreTokens()) {
					weightVector.add(Integer.parseInt(st.nextToken()));
				}
				weights.put(label, weightVector);
			}

			br.close();

		} catch (Exception e) {
			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		}
	}

}
