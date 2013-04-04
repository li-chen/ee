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

	private List<String> labelDict = new ArrayList<String>();
	private List<Map<String, Integer>> featureStringNumericDict = new ArrayList<Map<String, Integer>>();

	public List<String> getLabelDict() {
		return labelDict;
	}

	public List<Map<String, Integer>> getFeaturesDict() {
		return featureStringNumericDict;
	}

	public void creatNumericDictionary(List<Instance> instances) {

		int featureNum = instances.get(0).getFeaturesString().size();
		while (featureNum-- > 0) {
			featureStringNumericDict.add(new TreeMap<String, Integer>());
		}

		//
		// labels
		//
		for (Instance instance : instances) {

			if (!labelDict.contains(instance.getLabelString())) {
				labelDict.add(instance.getLabelString());
			}

		}

		//
		// features
		//
		int i = 0, index = 0;
		for (Map<String, Integer> aFeatureMap : featureStringNumericDict) {

			for (Instance instance : instances) {
				String featureStringValue = instance.getFeaturesString().get(i);
				if (!aFeatureMap.containsKey(featureStringValue)) {
					aFeatureMap.put(featureStringValue, index++);
				}
			}

			i++;
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

		for (Map<String, Integer> aFeatureMap : this.featureStringNumericDict) {

			for (String feature : aFeatureMap.keySet()) {
				sb.append(String.valueOf(aFeatureMap.get(feature)).concat(":")
						.concat(feature).concat("\t"));
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
			this.featureStringNumericDict = new ArrayList<Map<String, Integer>>();
			while ((line = br.readLine()) != null) {
				st = new StringTokenizer(line, "\t");

				Map<String, Integer> aFeatureMap = new TreeMap<String, Integer>();
				while (st.hasMoreElements()) {
					String feature = st.nextToken();
					int indexColon = feature.indexOf(":");
					aFeatureMap.put(feature.substring(indexColon + 1),
							Integer.parseInt(feature.substring(0, indexColon)));
				}

				featureStringNumericDict.add(aFeatureMap);
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

		Iterator<Map<String, Integer>> featuresDictIter = featureStringNumericDict
				.iterator();
		Iterator<String> featureStrIter = instance.getFeaturesString()
				.iterator();

		int[] featuresNumeric = new int[instance.getFeaturesString().size()];

		int i = 0;
		while (featureStrIter.hasNext()) {

			Map<String, Integer> aFeatureDict = featuresDictIter.next();
			String featureStr = featureStrIter.next();

			int numericValue = -1;
			if (aFeatureDict.containsKey(featureStr)) {
				numericValue = aFeatureDict.get(featureStr);
			}
			featuresNumeric[i++] = numericValue;

		}

		instance.setFeaturesNumeric(featuresNumeric);

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
			sb.append(instance.toString().concat("\n"));
		}

		FileUtil.saveFile(sb.toString(), file);
	}
}
