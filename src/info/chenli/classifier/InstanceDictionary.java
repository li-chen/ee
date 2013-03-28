package info.chenli.classifier;

import info.chenli.litway.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Logger;

public class InstanceDictionary {

	private final static Logger logger = Logger
			.getLogger(InstanceDictionary.class.getName());

	private List<String> labels = new ArrayList<String>();
	private List<Map<String, Double>> features = new ArrayList<Map<String, Double>>();

	public void creatNumericDictionary(List<Instance> instances) {

		List<Double> maxFeatureValues = new ArrayList<Double>();
		int featureNum = instances.get(0).getFeaturesString().size();
		while (featureNum-- > 0) {
			features.add(new TreeMap<String, Double>());
			maxFeatureValues.add(0.0);
		}

		for (Instance instance : instances) {

			//
			// labels
			//
			if (!labels.contains(instance.getLabelString())) {
				labels.add(instance.getLabelString());
			}

			//
			// features
			//
			Iterator<Map<String, Double>> featuresIter = features.iterator();
			Iterator<String> featureStrIter = instance.getFeaturesString()
					.iterator();
			List<Double> featuresNumeric = new ArrayList<Double>();

			int i = 0;
			while (featuresIter.hasNext()) {

				Map<String, Double> feature = featuresIter.next();
				String featureStr = featureStrIter.next();

				if (!feature.containsKey(featureStr)) {
					double currentValue = feature.keySet().size();
					feature.put(featureStr, currentValue);
					maxFeatureValues.set(i, currentValue);
				}

				featuresNumeric.add(feature.get(featureStr));

				i++;
			}
			instance.setFeatures(featuresNumeric);
		}

		// update dictionary to distributional value [0 - 1]
		int i = 0;
		for (Map<String, Double> featureVector : features) {
			Iterator<String> featureVectorIter = featureVector.keySet()
					.iterator();

			while (featureVectorIter.hasNext()) {

				String featureStr = featureVectorIter.next();

				featureVector.put(featureStr, featureVector.get(featureStr)
						/ maxFeatureValues.get(i));
			}
			i++;
		}

		// convert to distributional value [0 - 1]
		instancesToNumeric(instances);
	}

	public void saveDictionary(File file) {

		StringBuffer sb = new StringBuffer();
		int i = 0;
		for (String label : this.labels) {
			sb.append(String.valueOf(i++)).append(":").append(label)
					.append("\t");
		}
		sb.append("\n");

		for (Map<String, Double> featureVector : this.features) {

			for (String feature : featureVector.keySet()) {
				sb.append(featureVector.get(feature)).append(":")
						.append(feature).append("\t");
			}

			sb.append("\n");
		}

		FileUtil.saveFile(sb.toString(), file);
	}

	/**
	 * Load dictionary from a file. The loaded dictionary will overwrite the old
	 * one.
	 * 
	 * @param file
	 */
	public void loadDictionary(File file) {

		try {

			BufferedReader br = new BufferedReader(new FileReader(file));

			String line;

			//
			// load classes
			//
			line = br.readLine();
			this.labels = new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(line, "\t");
			while (st.hasMoreTokens()) {
				String label = st.nextToken();
				labels.add(label.substring(label.indexOf(":") + 1));
			}

			//
			// load features
			//
			this.features = new ArrayList<Map<String, Double>>();
			while ((line = br.readLine()) != null) {
				st = new StringTokenizer(line, "\t");

				Map<String, Double> featureVector = new TreeMap<String, Double>();
				while (st.hasMoreElements()) {
					String feature = st.nextToken();
					int indexColon = feature.indexOf(":");
					featureVector.put(feature.substring(indexColon + 1), Double
							.parseDouble(feature.substring(0, indexColon)));
				}

				features.add(featureVector);
			}

			br.close();

		} catch (Exception e) {

			logger.severe(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public List<Instance> instancesToNumeric(List<Instance> instances) {

		// set numeric values
		for (Instance instance : instances) {

			instanceToNumeric(instance);
		}

		return instances;
	}

	public Instance instanceToNumeric(Instance instance) {

		instance.setLabel(labels.indexOf(instance.getLabelString()));

		Iterator<Map<String, Double>> featuresIter = features.iterator();
		Iterator<String> featureStrIter = instance.getFeaturesString()
				.iterator();

		List<Double> featuresNumeric = new ArrayList<Double>();

		while (featureStrIter.hasNext()) {

			Map<String, Double> feature = featuresIter.next();
			String featureStr = featureStrIter.next();

			double numericFeature = feature.get(featureStr);
			featuresNumeric.add(numericFeature);

		}

		instance.setFeatures(featuresNumeric);
		return instance;
	}

	public double getLabelNumeric(String labelString) {

		return labels.indexOf(labelString);

	}

	public String getLabelString(double label) {

		return labels.get((int) label);

	}
}
