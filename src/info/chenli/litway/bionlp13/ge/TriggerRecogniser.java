package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Accurary;
import info.chenli.classifier.Fscore;
import info.chenli.classifier.Instance;
import info.chenli.classifier.InstanceDictionary;
import info.chenli.classifier.PerceptronClassifier;
import info.chenli.litway.corpora.POS;
import info.chenli.litway.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Trigger POS statistics on training set: 1:CC, 1:CD, 2:DT, 8:IN, 290:JJ,
 * 7:JJR, 2:JJS, 995:NN, 36,:NNS, 4:RB, 3:RBR, 97:VB, 109:VBD, 99:VBG, 267:VBN,
 * 25:VBP, 78:VBZ, 1:WRB
 * 
 * @author Chen Li
 * 
 * 
 * 
 */
public class TriggerRecogniser extends PerceptronClassifier {

	private final static Logger logger = Logger
			.getLogger(TriggerRecogniser.class.getName());
	private static List<POS> consideredPOS = new ArrayList<POS>();
	static {
		consideredPOS.add(POS.JJ);
		consideredPOS.add(POS.JJR);
		consideredPOS.add(POS.JJS);
		consideredPOS.add(POS.NN);
		consideredPOS.add(POS.NNS);
		consideredPOS.add(POS.VB);
		consideredPOS.add(POS.VBD);
		consideredPOS.add(POS.VBG);
		consideredPOS.add(POS.VBN);
		consideredPOS.add(POS.VBP);
		consideredPOS.add(POS.VBZ);
	};

	public static boolean isConsidered(String pos) {

		POS aPos = null;
		try {
			aPos = POS.valueOf(pos);
		} catch (IllegalArgumentException e) {
			return false;
		}

		return isConsidered(aPos);
	}

	public static boolean isConsidered(POS pos) {

		for (POS aPos : consideredPOS) {
			if (aPos.equals(pos)) {
				return true;
			}
		}

		return false;
	}

	public void train(File trainingSet, boolean useSearn) {

		if (useSearn) {

		} else {

			TokenInstances trainingInstances = new TokenInstances();
			trainingInstances
					.setTaeDescriptor("/desc/TrainingSetAnnotator.xml");
			List<Instance> instances = trainingInstances
					.getInstances(trainingSet);

			// balance the instances
			// Collections.shuffle(instances);
			List<Instance> balancedInstances = new ArrayList<Instance>();

			int nonTriggerTokenNum = 0;
			Map<String, Integer> occurrenceMap = new TreeMap<String, Integer>();
			for (Instance instance : instances) {

				if (!isConsidered(instance.getFeaturesString().get(2))) {

					continue;

				} else if (!instance.getFeaturesString().get(0)
						.matches("^[a-zA-Z].+")) {

					continue;
				}
				if (instance.getLabelString().equals(
						String.valueOf(EventType.Non_trigger))) {
					nonTriggerTokenNum++;
				}
				if (!occurrenceMap.containsKey(instance.getLabelString())) {
					occurrenceMap.put(instance.getLabelString(), 0);
				}
				Integer occurrence = occurrenceMap.get(instance
						.getLabelString()) + 1;
				occurrenceMap.put(instance.getLabelString(), occurrence);
			}

			for (Instance instance : instances) {

				if (!isConsidered(instance.getFeaturesString().get(2))) {

					continue;

				} else if (!instance.getFeaturesString().get(0)
						.matches("^[a-zA-Z].+")) {

					continue;
				}

				int copyNumber = nonTriggerTokenNum
						/ occurrenceMap.get(instance.getLabelString());

				while (copyNumber-- > 0) {
					balancedInstances.add(instance);
				}
			}
			Collections.shuffle(balancedInstances);

			InstanceDictionary dict = new InstanceDictionary();
			dict.creatNumericDictionary(balancedInstances);
			dict.saveDictionary(new File("./model/triggers.dict"));

			// dict.writeInstancesToFile(balancedInstances, new File(
			// "./instances.csv"), false);
			// dict.writeInstancesToFile(balancedInstances, new File(
			// "./instances-original.csv"), true);

			// cross validation
			int step = balancedInstances.size() / 10;
			for (int i = 0; i < 10; i++) {

				List<Instance> subTestingInstances = balancedInstances.subList(
						step * i, step * (i + 1));
				List<Instance> subTrainingInstances = new ArrayList<Instance>();
				subTrainingInstances.addAll(balancedInstances.subList(0, step
						* i));
				subTrainingInstances.addAll(balancedInstances.subList(step
						* (i + 1), balancedInstances.size()));

				Collections.shuffle(subTrainingInstances);
				Collections.shuffle(subTestingInstances);

				train(subTrainingInstances, 100);

				test(subTestingInstances, dict);
			}

		}
	}

	private Fscore test(List<Instance> instances, InstanceDictionary dict) {

		int tp = 0, fp = 0, tn = 0, fn = 0, correct = 0, total = 0;
		StringBuffer tp_instances = new StringBuffer();
		StringBuffer fp_nonTrigger_instances = new StringBuffer();
		StringBuffer fp_trigger_instances = new StringBuffer();

		Collections.shuffle(instances);
		for (Instance instance : instances) {

			if (!isConsidered(instance.getFeaturesString().get(2))) {

				continue;

			} else if (!instance.getFeaturesString().get(0)
					.matches("^[a-zA-Z].+")) {

				continue;
			}

			double prediction = this.predict(instance);
			if (instance.getLabelString() == String
					.valueOf(EventType.Non_trigger)) {

				if (prediction != instance.getLabel()) {
					fp++;
					total++;
					fp_nonTrigger_instances.append(dict
							.getLabelString(prediction).concat("\t")
							.concat(instance.toString()).concat("\n"));
				}
			} else {
				if (prediction != instance.getLabel()) {
					fp++;
					fn++;
					fp_trigger_instances.append(dict.getLabelString(prediction)
							.concat("\t").concat(instance.toString())
							.concat("\n"));
				} else {
					correct++;
					tp++;
					tp_instances.append(dict.getLabelString(prediction)
							.concat("\t").concat(instance.toString())
							.concat("\n"));
				}
				if (prediction != dict.getLabelNumeric(String
						.valueOf(EventType.Non_trigger))) {
					total++;
				}
			}
		}

		FileUtil.saveFile(fp_nonTrigger_instances.toString(), new File(
				"./result/fp_nonTrigger.txt"));
		FileUtil.saveFile(fp_trigger_instances.toString(), new File(
				"./result/fp_trigger.txt"));
		FileUtil.saveFile(tp_instances.toString(), new File("./result/tp.txt"));

		Fscore fscore = new Fscore(tp, fp, tn, fn);
		System.out.println(fscore);
		System.out.println(new Accurary(correct, total));

		return fscore;
	}

	public static void main(String[] args) {

		TokenInstances trainingInstances = new TokenInstances();
		trainingInstances.setTaeDescriptor("/desc/TrainingSetAnnotator.xml");
		List<Instance> instances = trainingInstances.getInstances(new File(
				args[0]));

		// balance the instances
		List<Instance> balancedInstances = new ArrayList<Instance>();
		// // subsampling
		// int nonTriggerNumber = 0;
		// for (Instance instance : instances) {
		//
		// if (!isConsidered(instance.getFeaturesString().get(2))) {
		//
		// continue;
		//
		// } else if (!instance.getFeaturesString().get(0)
		// .matches("^[a-zA-Z].+")) {
		//
		// continue;
		// } else if (instance.getLabelString().equals(
		// String.valueOf(EventType.Non_trigger))) {
		//
		// if (nonTriggerNumber > 10000) {
		// continue;
		// }
		//
		// nonTriggerNumber++;
		// }
		// balancedInstances.add(instance);
		// }

		int nonTriggerTokenNum = 0;
		Map<String, Integer> occurrenceMap = new TreeMap<String, Integer>();
		for (Instance instance : instances) {

			if (!isConsidered(instance.getFeaturesString().get(2))) {

				continue;

			} else if (!instance.getFeaturesString().get(0)
					.matches("^[a-zA-Z].+")) {

				continue;
			}
			if (instance.getLabelString().equals(
					String.valueOf(EventType.Non_trigger))) {
				nonTriggerTokenNum++;
			}
			if (!occurrenceMap.containsKey(instance.getLabelString())) {
				occurrenceMap.put(instance.getLabelString(), 0);
			}
			Integer occurrence = occurrenceMap.get(instance.getLabelString()) + 1;
			occurrenceMap.put(instance.getLabelString(), occurrence);
		}

		for (Instance instance : instances) {

			if (!isConsidered(instance.getFeaturesString().get(2))) {

				continue;

			} else if (!instance.getFeaturesString().get(0)
					.matches("^[a-zA-Z].+")) {

				continue;
			}

			int copyNumber = nonTriggerTokenNum
					/ occurrenceMap.get(instance.getLabelString());

			while (copyNumber-- > 0) {
				balancedInstances.add(instance);
			}
		}
		Collections.shuffle(balancedInstances);

		InstanceDictionary dict = new InstanceDictionary();
		dict.creatNumericDictionary(balancedInstances);
		dict.saveDictionary(new File("./model/triggers.dict"));

		// 10-fold cross validation
		int fold = 10;
		int step = balancedInstances.size() / fold;
		for (int i = 0; i < fold; i++) {

			TriggerRecogniser tr = new TriggerRecogniser();

			List<Instance> subTestingInstances = balancedInstances.subList(step
					* i, step * (i + 1));
			List<Instance> subTrainingInstances = new ArrayList<Instance>();
			subTrainingInstances.addAll(balancedInstances.subList(0, step * i));
			subTrainingInstances.addAll(balancedInstances.subList(step
					* (i + 1), balancedInstances.size()));

			Collections.shuffle(subTrainingInstances);
			Collections.shuffle(subTestingInstances);

			tr.train(subTrainingInstances, 1000);

			tr.test(subTestingInstances, dict);
		}

	}
}
