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
			int attributeNum = instances.get(0).getFeaturesNumeric().length;

			for (Instance instance : instances) {
				if (!labels.contains(instance.getLabel())) {
					labels.add(instance.getLabel());
				}
				int lastFeatureIndex = instance.getFeaturesNumeric()[attributeNum - 1];
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

		logger.info("Start training.");
		for (Instance instance : instances) {

			int prediction = predict(instance.getFeaturesNumeric());

			if (prediction != instance.getLabel()) {

				try {

					// System.out.print("GS before:");
					// for (Integer weight :
					// this.weights.get(instance.getLabel())) {
					// System.out.print("\t".concat(String.valueOf(weight)));
					// }
					// System.out.println();
					this.weights.put(instance.getLabel(), MathUtil.add(
							this.weights.get(instance.getLabel()),
							instance.getFeaturesNumeric()));
					// System.out.print("GS after:");
					// for (Integer weight :
					// this.weights.get(instance.getLabel())) {
					// System.out.print("\t".concat(String.valueOf(weight)));
					// }
					// System.out.println();
					//
					// System.out.print("Pred before:");
					// for (Integer weight : this.weights.get(prediction)) {
					// System.out.print("\t".concat(String.valueOf(weight)));
					// }
					// System.out.println();
					this.weights.put(prediction, MathUtil.subtract(
							this.weights.get(prediction),
							instance.getFeaturesNumeric()));
					// System.out.print("Pred after:");
					// for (Integer weight : this.weights.get(prediction)) {
					// System.out.print("\t".concat(String.valueOf(weight)));
					// }
					// System.out.println();

				} catch (IllegalArgumentException e) {

					logger.log(Level.SEVERE, e.getMessage(), e);
					throw new RuntimeException(e);
				}
			}
			System.out.println();
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

	public static void main(String[] args) throws IOException {

		List<Instance> instances = new ArrayList<Instance>();

		// iris flower data set
		String[][] iris = { { "5.1", "3.5", "1.4", "0.2", "Iris-setosa" },
				{ "4.9", "3.0", "1.4", "0.2", "Iris-setosa" },
				{ "4.7", "3.2", "1.3", "0.2", "Iris-setosa" },
				{ "4.6", "3.1", "1.5", "0.2", "Iris-setosa" },
				{ "5.0", "3.6", "1.4", "0.2", "Iris-setosa" },
				{ "5.4", "3.9", "1.7", "0.4", "Iris-setosa" },
				{ "4.6", "3.4", "1.4", "0.3", "Iris-setosa" },
				{ "5.0", "3.4", "1.5", "0.2", "Iris-setosa" },
				{ "4.4", "2.9", "1.4", "0.2", "Iris-setosa" },
				{ "4.9", "3.1", "1.5", "0.1", "Iris-setosa" },
				{ "5.4", "3.7", "1.5", "0.2", "Iris-setosa" },
				{ "4.8", "3.4", "1.6", "0.2", "Iris-setosa" },
				{ "4.8", "3.0", "1.4", "0.1", "Iris-setosa" },
				{ "4.3", "3.0", "1.1", "0.1", "Iris-setosa" },
				{ "5.8", "4.0", "1.2", "0.2", "Iris-setosa" },
				{ "5.7", "4.4", "1.5", "0.4", "Iris-setosa" },
				{ "5.4", "3.9", "1.3", "0.4", "Iris-setosa" },
				{ "5.1", "3.5", "1.4", "0.3", "Iris-setosa" },
				{ "5.7", "3.8", "1.7", "0.3", "Iris-setosa" },
				{ "5.1", "3.8", "1.5", "0.3", "Iris-setosa" },
				{ "5.4", "3.4", "1.7", "0.2", "Iris-setosa" },
				{ "5.1", "3.7", "1.5", "0.4", "Iris-setosa" },
				{ "4.6", "3.6", "1.0", "0.2", "Iris-setosa" },
				{ "5.1", "3.3", "1.7", "0.5", "Iris-setosa" },
				{ "4.8", "3.4", "1.9", "0.2", "Iris-setosa" },
				{ "5.0", "3.0", "1.6", "0.2", "Iris-setosa" },
				{ "5.0", "3.4", "1.6", "0.4", "Iris-setosa" },
				{ "5.2", "3.5", "1.5", "0.2", "Iris-setosa" },
				{ "5.2", "3.4", "1.4", "0.2", "Iris-setosa" },
				{ "4.7", "3.2", "1.6", "0.2", "Iris-setosa" },
				{ "4.8", "3.1", "1.6", "0.2", "Iris-setosa" },
				{ "5.4", "3.4", "1.5", "0.4", "Iris-setosa" },
				{ "5.2", "4.1", "1.5", "0.1", "Iris-setosa" },
				{ "5.5", "4.2", "1.4", "0.2", "Iris-setosa" },
				{ "4.9", "3.1", "1.5", "0.1", "Iris-setosa" },
				{ "5.0", "3.2", "1.2", "0.2", "Iris-setosa" },
				{ "5.5", "3.5", "1.3", "0.2", "Iris-setosa" },
				{ "4.9", "3.1", "1.5", "0.1", "Iris-setosa" },
				{ "4.4", "3.0", "1.3", "0.2", "Iris-setosa" },
				{ "5.1", "3.4", "1.5", "0.2", "Iris-setosa" },
				{ "5.0", "3.5", "1.3", "0.3", "Iris-setosa" },
				{ "4.5", "2.3", "1.3", "0.3", "Iris-setosa" },
				{ "4.4", "3.2", "1.3", "0.2", "Iris-setosa" },
				{ "5.0", "3.5", "1.6", "0.6", "Iris-setosa" },
				{ "5.1", "3.8", "1.9", "0.4", "Iris-setosa" },
				{ "4.8", "3.0", "1.4", "0.3", "Iris-setosa" },
				{ "5.1", "3.8", "1.6", "0.2", "Iris-setosa" },
				{ "4.6", "3.2", "1.4", "0.2", "Iris-setosa" },
				{ "5.3", "3.7", "1.5", "0.2", "Iris-setosa" },
				{ "5.0", "3.3", "1.4", "0.2", "Iris-setosa" },
				{ "7.0", "3.2", "4.7", "1.4", "Iris-versicolor" },
				{ "6.4", "3.2", "4.5", "1.5", "Iris-versicolor" },
				{ "6.9", "3.1", "4.9", "1.5", "Iris-versicolor" },
				{ "5.5", "2.3", "4.0", "1.3", "Iris-versicolor" },
				{ "6.5", "2.8", "4.6", "1.5", "Iris-versicolor" },
				{ "5.7", "2.8", "4.5", "1.3", "Iris-versicolor" },
				{ "6.3", "3.3", "4.7", "1.6", "Iris-versicolor" },
				{ "4.9", "2.4", "3.3", "1.0", "Iris-versicolor" },
				{ "6.6", "2.9", "4.6", "1.3", "Iris-versicolor" },
				{ "5.2", "2.7", "3.9", "1.4", "Iris-versicolor" },
				{ "5.0", "2.0", "3.5", "1.0", "Iris-versicolor" },
				{ "5.9", "3.0", "4.2", "1.5", "Iris-versicolor" },
				{ "6.0", "2.2", "4.0", "1.0", "Iris-versicolor" },
				{ "6.1", "2.9", "4.7", "1.4", "Iris-versicolor" },
				{ "5.6", "2.9", "3.6", "1.3", "Iris-versicolor" },
				{ "6.7", "3.1", "4.4", "1.4", "Iris-versicolor" },
				{ "5.6", "3.0", "4.5", "1.5", "Iris-versicolor" },
				{ "5.8", "2.7", "4.1", "1.0", "Iris-versicolor" },
				{ "6.2", "2.2", "4.5", "1.5", "Iris-versicolor" },
				{ "5.6", "2.5", "3.9", "1.1", "Iris-versicolor" },
				{ "5.9", "3.2", "4.8", "1.8", "Iris-versicolor" },
				{ "6.1", "2.8", "4.0", "1.3", "Iris-versicolor" },
				{ "6.3", "2.5", "4.9", "1.5", "Iris-versicolor" },
				{ "6.1", "2.8", "4.7", "1.2", "Iris-versicolor" },
				{ "6.4", "2.9", "4.3", "1.3", "Iris-versicolor" },
				{ "6.6", "3.0", "4.4", "1.4", "Iris-versicolor" },
				{ "6.8", "2.8", "4.8", "1.4", "Iris-versicolor" },
				{ "6.7", "3.0", "5.0", "1.7", "Iris-versicolor" },
				{ "6.0", "2.9", "4.5", "1.5", "Iris-versicolor" },
				{ "5.7", "2.6", "3.5", "1.0", "Iris-versicolor" },
				{ "5.5", "2.4", "3.8", "1.1", "Iris-versicolor" },
				{ "5.5", "2.4", "3.7", "1.0", "Iris-versicolor" },
				{ "5.8", "2.7", "3.9", "1.2", "Iris-versicolor" },
				{ "6.0", "2.7", "5.1", "1.6", "Iris-versicolor" },
				{ "5.4", "3.0", "4.5", "1.5", "Iris-versicolor" },
				{ "6.0", "3.4", "4.5", "1.6", "Iris-versicolor" },
				{ "6.7", "3.1", "4.7", "1.5", "Iris-versicolor" },
				{ "6.3", "2.3", "4.4", "1.3", "Iris-versicolor" },
				{ "5.6", "3.0", "4.1", "1.3", "Iris-versicolor" },
				{ "5.5", "2.5", "4.0", "1.3", "Iris-versicolor" },
				{ "5.5", "2.6", "4.4", "1.2", "Iris-versicolor" },
				{ "6.1", "3.0", "4.6", "1.4", "Iris-versicolor" },
				{ "5.8", "2.6", "4.0", "1.2", "Iris-versicolor" },
				{ "5.0", "2.3", "3.3", "1.0", "Iris-versicolor" },
				{ "5.6", "2.7", "4.2", "1.3", "Iris-versicolor" },
				{ "5.7", "3.0", "4.2", "1.2", "Iris-versicolor" },
				{ "5.7", "2.9", "4.2", "1.3", "Iris-versicolor" },
				{ "6.2", "2.9", "4.3", "1.3", "Iris-versicolor" },
				{ "5.1", "2.5", "3.0", "1.1", "Iris-versicolor" },
				{ "5.7", "2.8", "4.1", "1.3", "Iris-versicolor" },
				{ "6.3", "3.3", "6.0", "2.5", "Iris-virginica" },
				{ "5.8", "2.7", "5.1", "1.9", "Iris-virginica" },
				{ "7.1", "3.0", "5.9", "2.1", "Iris-virginica" },
				{ "6.3", "2.9", "5.6", "1.8", "Iris-virginica" },
				{ "6.5", "3.0", "5.8", "2.2", "Iris-virginica" },
				{ "7.6", "3.0", "6.6", "2.1", "Iris-virginica" },
				{ "4.9", "2.5", "4.5", "1.7", "Iris-virginica" },
				{ "7.3", "2.9", "6.3", "1.8", "Iris-virginica" },
				{ "6.7", "2.5", "5.8", "1.8", "Iris-virginica" },
				{ "7.2", "3.6", "6.1", "2.5", "Iris-virginica" },
				{ "6.5", "3.2", "5.1", "2.0", "Iris-virginica" },
				{ "6.4", "2.7", "5.3", "1.9", "Iris-virginica" },
				{ "6.8", "3.0", "5.5", "2.1", "Iris-virginica" },
				{ "5.7", "2.5", "5.0", "2.0", "Iris-virginica" },
				{ "5.8", "2.8", "5.1", "2.4", "Iris-virginica" },
				{ "6.4", "3.2", "5.3", "2.3", "Iris-virginica" },
				{ "6.5", "3.0", "5.5", "1.8", "Iris-virginica" },
				{ "7.7", "3.8", "6.7", "2.2", "Iris-virginica" },
				{ "7.7", "2.6", "6.9", "2.3", "Iris-virginica" },
				{ "6.0", "2.2", "5.0", "1.5", "Iris-virginica" },
				{ "6.9", "3.2", "5.7", "2.3", "Iris-virginica" },
				{ "5.6", "2.8", "4.9", "2.0", "Iris-virginica" },
				{ "7.7", "2.8", "6.7", "2.0", "Iris-virginica" },
				{ "6.3", "2.7", "4.9", "1.8", "Iris-virginica" },
				{ "6.7", "3.3", "5.7", "2.1", "Iris-virginica" },
				{ "7.2", "3.2", "6.0", "1.8", "Iris-virginica" },
				{ "6.2", "2.8", "4.8", "1.8", "Iris-virginica" },
				{ "6.1", "3.0", "4.9", "1.8", "Iris-virginica" },
				{ "6.4", "2.8", "5.6", "2.1", "Iris-virginica" },
				{ "7.2", "3.0", "5.8", "1.6", "Iris-virginica" },
				{ "7.4", "2.8", "6.1", "1.9", "Iris-virginica" },
				{ "7.9", "3.8", "6.4", "2.0", "Iris-virginica" },
				{ "6.4", "2.8", "5.6", "2.2", "Iris-virginica" },
				{ "6.3", "2.8", "5.1", "1.5", "Iris-virginica" },
				{ "6.1", "2.6", "5.6", "1.4", "Iris-virginica" },
				{ "7.7", "3.0", "6.1", "2.3", "Iris-virginica" },
				{ "6.3", "3.4", "5.6", "2.4", "Iris-virginica" },
				{ "6.4", "3.1", "5.5", "1.8", "Iris-virginica" },
				{ "6.0", "3.0", "4.8", "1.8", "Iris-virginica" },
				{ "6.9", "3.1", "5.4", "2.1", "Iris-virginica" },
				{ "6.7", "3.1", "5.6", "2.4", "Iris-virginica" },
				{ "6.9", "3.1", "5.1", "2.3", "Iris-virginica" },
				{ "5.8", "2.7", "5.1", "1.9", "Iris-virginica" },
				{ "6.8", "3.2", "5.9", "2.3", "Iris-virginica" },
				{ "6.7", "3.3", "5.7", "2.5", "Iris-virginica" },
				{ "6.7", "3.0", "5.2", "2.3", "Iris-virginica" },
				{ "6.3", "2.5", "5.0", "1.9", "Iris-virginica" },
				{ "6.5", "3.0", "5.2", "2.0", "Iris-virginica" },
				{ "6.2", "3.4", "5.4", "2.3", "Iris-virginica" },
				{ "5.9", "3.0", "5.1", "1.8", "Iris-virginica" } };

		for (int i = 0; i < iris.length; i++) {
			Instance instance = new Instance();
			instance.setLabelString(iris[i][4]);

			List<String> features = new ArrayList<String>();

			for (int j = 0; j < 4; j++) {
				features.add(iris[i][j]);
			}

			instance.setFeaturesString(features);

			instances.add(instance);
		}

		Collections.shuffle(instances);
		InstanceDictionary dict = new InstanceDictionary();
		dict.creatNumericDictionary(instances);
		dict.saveDictionary(new File("./model/test.dict"));

		PerceptronClassifier classifier = new PerceptronClassifier();

		classifier.train(instances, 15);
		System.out.println(classifier.accuracy(instances));
		System.out.println(classifier.modelToString());

		// restaurants sample training data.

		// BufferedReader in = new BufferedReader(new FileReader(
		// "./test/restaurant_train.txt"));
		// String line = in.readLine();
		//
		// while (line != null) {
		//
		// Instance instance = new Instance();
		// int[] features = new int[9492];
		// String[] toks = line.split(" ");
		// instance.setLabel(Integer.parseInt(toks[0]));
		// for (int i = 1; i < toks.length; i++) {
		// int f = Integer.parseInt(toks[i].split(":")[0]);
		// int v = Integer.parseInt(toks[i].split(":")[1]);
		// features[f] = v;
		// }
		//
		// List<Integer> featuresDouble = new ArrayList<Integer>();
		// for (int feature : features) {
		// featuresDouble.add(feature);
		// }
		// instance.setFeatures(featuresDouble);
		// instances.add(instance);
		//
		// line = in.readLine();
		// }
		//
		// PerceptronClassifier classifier = new PerceptronClassifier();
		//
		// classifier.train(instances, 150);
		// System.out.println(classifier.accuracy(instances));
		//
		// // restaurants sample test data.
		// in = new BufferedReader(new
		// FileReader("./test/restaurant_test.txt"));
		// line = in.readLine();
		// instances = new ArrayList<Instance>();
		//
		// while (line != null) {
		//
		// Instance instance = new Instance();
		// double[] features = new double[9492];
		// String[] toks = line.split(" ");
		// instance.setLabel(Double.parseDouble(toks[0]));
		// for (int i = 1; i < toks.length; i++) {
		// int f = Integer.parseInt(toks[i].split(":")[0]);
		// double v = Double.parseDouble(toks[i].split(":")[1]);
		// features[f] = v;
		// }
		//
		// List<Double> featuresDouble = new ArrayList<Double>();
		// for (double feature : features) {
		// featuresDouble.add(feature);
		// }
		// instance.setFeatures(featuresDouble);
		// instances.add(instance);
		//
		// line = in.readLine();
		// }
		// System.out.println(classifier.accuracy(instances)); // Instance
		// testInstance = instances.get(0); // double prediction =
		// classifier.predict(testInstance); // System.out.print(prediction); //
		// System.out.print("\t"); //
		// System.out.println(testInstance.getLabel());

	}
}
