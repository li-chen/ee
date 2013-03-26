package info.chenli.classifier;

import info.chenli.ee.util.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class InstanceDictionary {

	private final static Logger logger = Logger
			.getLogger(InstanceDictionary.class.getName());

	private List<String> labels = new ArrayList<String>();
	private List<List<String>> features = new ArrayList<List<String>>();

	public void creatDictionary(List<Instance> instances) {

		int featureNum = instances.get(0).getFeaturesString().size();
		while (featureNum-- > 0) {
			features.add(new ArrayList<String>());
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
			Iterator<List<String>> featuresIter = features.iterator();
			Iterator<String> featureStrIter = instance.getFeaturesString()
					.iterator();

			while (featuresIter.hasNext()) {

				List<String> feature = featuresIter.next();
				String featureStr = featureStrIter.next();

				if (!feature.contains(featureStr)) {
					feature.add(featureStr);
				}

			}

		}

	}

	public void saveDictionary(File file) {

		StringBuffer sb = new StringBuffer();
		int i = 0;
		for (String label : this.labels) {
			sb.append(String.valueOf(i++)).append(":").append(label)
					.append("\t");
		}
		sb.append("\n");

		for (List<String> featureVector : this.features) {

			i = 0;

			for (String feature : featureVector) {
				sb.append(String.valueOf(i++)).append(":").append(feature)
						.append("\t");
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
			this.features = new ArrayList<List<String>>();
			while ((line = br.readLine()) != null) {
				st = new StringTokenizer(line, "\t");

				List<String> featureVector = new ArrayList<String>();
				while (st.hasMoreElements()) {
					String feature = st.nextToken();
					featureVector
							.add(feature.substring(feature.indexOf(":") + 1));
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

		for (Instance instance : instances) {

			instanceToNumeric(instance);
		}

		return instances;
	}

	public Instance instanceToNumeric(Instance instance) {
		instance.setLabel(labels.indexOf(instance.getLabelString()));

		Iterator<List<String>> featuresIter = features.iterator();
		Iterator<String> featureStrIter = instance.getFeaturesString()
				.iterator();

		List<Double> featuresNumeric = new ArrayList<Double>();

		while (featureStrIter.hasNext()) {

			List<String> feature = featuresIter.next();
			String featureStr = featureStrIter.next();

			featuresNumeric.add((double) feature.indexOf(featureStr));
		}

		instance.setFeatures(featuresNumeric);
		return instance;
	}

	public double getLabelNumeric(String labelString) {

		return labelString.indexOf(labelString);

	}

	public String getLabelString(double label) {

		return labels.get((int) label);

	}
}
