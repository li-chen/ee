package info.chenli.classifier;

import info.chenli.litway.util.FileUtil;

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

	private List<String> labelDict = new ArrayList<String>();
	private List<List<String>> featuresDict = new ArrayList<List<String>>();

	public List<String> getLabelDict() {
		return labelDict;
	}

	public List<List<String>> getFeaturesDict() {
		return featuresDict;
	}

	public void creatNumericDictionary(List<Instance> instances) {

		int featureNum = instances.get(0).getFeaturesString().size();
		while (featureNum-- > 0) {
			featuresDict.add(new ArrayList<String>());
		}

		for (Instance instance : instances) {

			//
			// labels
			//
			if (!labelDict.contains(instance.getLabelString())) {
				labelDict.add(instance.getLabelString());
			}

			//
			// features
			//
			Iterator<List<String>> featuresDictIter = featuresDict.iterator();
			Iterator<String> instanceFeatureStrIter = instance.getFeaturesString()
					.iterator();

			while (featuresDictIter.hasNext()) {

				List<String> feature = featuresDictIter.next();
				String featureStrValue = instanceFeatureStrIter.next();

				if (!feature.contains(featureStrValue)) {

					feature.add(featureStrValue);

				}
			}
		}

		instancesToNumeric(instances);
	}

	public void saveDictionary(File file) {

		StringBuffer sb = new StringBuffer();

		for (String label : this.labelDict) {
			sb.append(String.valueOf(labelDict.indexOf(label))).append(":")
					.append(label).append("\t");
		}
		sb.append("\n");

		for (List<String> featureVector : this.featuresDict) {

			for (String feature : featureVector) {
				sb.append(String.valueOf(featureVector.indexOf(feature))
						.concat(":").concat(feature).concat("\t"));
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
			this.labelDict = new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(line, "\t");
			while (st.hasMoreTokens()) {
				String label = st.nextToken();
				labelDict.set(Integer.parseInt(label.substring(0,
						label.indexOf(":"))), label.substring(label
						.indexOf(":") + 1));
			}

			//
			// load features
			//
			this.featuresDict = new ArrayList<List<String>>();
			while ((line = br.readLine()) != null) {
				st = new StringTokenizer(line, "\t");

				List<String> featureVector = new ArrayList<String>();
				while (st.hasMoreElements()) {
					String feature = st.nextToken();
					int indexColon = feature.indexOf(":");
					featureVector.set(
							Integer.parseInt(feature.substring(0, indexColon)),
							feature.substring(indexColon + 1));
				}

				featuresDict.add(featureVector);
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

		instance.setLabel(labelDict.indexOf(instance.getLabelString()));

		Iterator<List<String>> featuresDictIter = featuresDict.iterator();
		Iterator<String> featureStrIter = instance.getFeaturesString()
				.iterator();

		List<SparseVector> featuresNumeric = new ArrayList<SparseVector>();

		while (featureStrIter.hasNext()) {

			List<String> featureDict = featuresDictIter.next();
			String featureStr = featureStrIter.next();

			SparseVector numericFeature = new SparseVector();
			numericFeature.setLength(featureDict.size());
			if (featureDict.contains(featureStr)) {
				numericFeature.setPosition(featureDict.indexOf(featureStr));
				numericFeature.setValue(1);
			} else {
				numericFeature.setPosition(-1);
				numericFeature.setValue(0);
			}

			featuresNumeric.add(numericFeature);

		}

		instance.setFeatures(featuresNumeric);
		return instance;
	}

	public double getLabelNumeric(String labelString) {

		return labelDict.indexOf(labelString);

	}

	public String getLabelString(int label) {

		return labelDict.get(label);

	}

	public void writeInstancesToFile(List<Instance> instances, File file,
			boolean withOriginalValue) {
		StringBuffer sb = new StringBuffer();
		for (Instance instance : instances) {
			sb.append(String.valueOf(instance.getLabel()));

			Iterator<String> featureStrIter = instance.getFeaturesString()
					.iterator();
			for (SparseVector feature : instance.getFeatures()) {
				sb.append("\t".concat(String.valueOf(feature.getPosition())));
				if (withOriginalValue) {
					sb.append(":");
					sb.append(featureStrIter.next());
				}
			}
			sb.append("\n");
		}
		FileUtil.saveFile(sb.toString(), file);
	}
}
