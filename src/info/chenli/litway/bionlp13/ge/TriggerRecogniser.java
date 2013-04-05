package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Accurary;
import info.chenli.classifier.Fscore;
import info.chenli.classifier.Instance;
import info.chenli.classifier.InstanceDictionary;
import info.chenli.classifier.PerceptronClassifier;
import info.chenli.litway.corpora.POS;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.Timer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

			int prediction = this.predict(instance.getFeaturesNumeric());
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
		logger.info(String.valueOf(instances.size()).concat(
				" instances are collected."));

		Collections.shuffle(instances);
		logger.info("Shuffle instances.");

		InstanceDictionary dict = new InstanceDictionary();
		dict.creatNumericDictionary(instances);
		logger.info("Create dictionary.");
		dict.saveDictionary(new File("./model/triggers.dict"));
		logger.info("Save dictionary.");

		// 10-fold cross validation
		int fold = 10;
		int step = instances.size() / fold;
		int i =0;

//		logger.info(String.valueOf(fold).concat(" fold cross validatation."));
//
//		for (int i = 0; i < fold; i++) {
//
//			logger.info(String.valueOf(i).concat(" fold cross validatation."));

			TriggerRecogniser tr = new TriggerRecogniser();

			List<Instance> subTestingInstances = instances.subList(step * i,
					step * (i + 1));
			List<Instance> subTrainingInstances = new ArrayList<Instance>();
			subTrainingInstances.addAll(instances.subList(0, step * i));
			subTrainingInstances.addAll(instances.subList(step * (i + 1),
					instances.size()));

			Collections.shuffle(subTrainingInstances);
			Collections.shuffle(subTestingInstances);

			Timer timer = new Timer();
			timer.start();

			tr.train(subTrainingInstances, 500);
			timer.stop();
//			logger.info(String.valueOf(i).concat(" fold training takes ")
//					.concat(String.valueOf(timer.getRunningTime())));

			tr.test(subTestingInstances, dict);
//		}

			tr.saveModel(new File("./model/triggers.perceptron.model"));
	}
}
