package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Accurary;
import info.chenli.classifier.Fscore;
import info.chenli.classifier.Instance;
import info.chenli.classifier.InstanceDictionary;
import info.chenli.classifier.LibLinearFacade;
import info.chenli.litway.corpora.POS;
import info.chenli.litway.corpora.Protein;
import info.chenli.litway.corpora.Sentence;
import info.chenli.litway.corpora.Token;
import info.chenli.litway.corpora.Trigger;
import info.chenli.litway.util.BioLemmatizerUtil;
import info.chenli.litway.util.DependencyExtractor;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.StanfordDependencyReader;
import info.chenli.litway.util.StanfordDependencyReader.Pair;
import info.chenli.litway.util.Stemmer;
import info.chenli.litway.util.Timer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import edu.ucdenver.ccp.nlp.biolemmatizer.BioLemmatizer;

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
public class TriggerRecogniser extends LibLinearFacade {

	private final static Logger logger = Logger
			.getLogger(TriggerRecogniser.class.getName());

	private final String classifierName = "liblinear";

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

	public void train(String trainingDir, int round) {
		//
		// collect all instances and fetch syntactical information
		//
		TokenInstances trainingInstances = new TokenInstances();
		trainingInstances.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
		List<Instance> instances = trainingInstances.getInstances(new File(
				trainingDir));
		logger.info(String.valueOf(instances.size()).concat(
				" instances are collected."));

		InstanceDictionary dict = new InstanceDictionary();
		dict.creatNumericDictionary(instances);
		dict.saveDictionary(new File("./model/triggers.".concat(classifierName)
				.concat(".dict")));
		logger.info("Save dictionary.");

		trainingInstances.saveInstances(new File(
				"./model/instances.trigger.txt"));
		trainingInstances.saveNumericInstances(new File(
				"./model/instances.trigger.num.txt"));
		trainingInstances.saveSvmLightInstances(new File(
				"./model/instances.trigger.svm.txt"));

		// development instances
		// TokenInstances devInstances = new TokenInstances();
		// devInstances.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
		// List<Instance> devInstancesList = devInstances.getInstances(new File(
		// "./data/development/"));
		// logger.info(String.valueOf(devInstancesList.size()).concat(
		// " instances are collected."));
		//
		// dict.instancesToNumeric(devInstancesList);
		//
		// devInstances.saveInstances(new File(
		// "./model/instances.trigger.dev.txt"));
		// devInstances.saveNumericInstances(new File(
		// "./model/instances.trigger.num.dev.txt"));
		// devInstances.saveSvmLightInstances(new File(
		// "./model/instances.trigger.svm.dev.txt"));
		//
		// System.out.print("Finish collecting events.");
		// System.exit(0);

		// Collections.shuffle(instances);
		// logger.info("Shuffle instances.");

		Timer timer = new Timer();
		timer.start();

		train(instances, round);
		timer.stop();
		logger.info("Training takes ".concat(String.valueOf(timer
				.getRunningTime())));

		saveModel(new File("./model/triggers.".concat(classifierName).concat(
				".model")));

	}

	public List<Trigger> predict(File file, InstanceDictionary dict,
			boolean printConfusionMatrix) {

		List<Trigger> triggers = new ArrayList<Trigger>();

		TokenInstances instancesGetter = new TokenInstances();
		instancesGetter.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
		if (printConfusionMatrix) {
			List<Instance> instances = dict.instancesToNumeric(instancesGetter
					.getInstances(file));
			Map<Integer, Map<Integer, Integer>> confusionMatrix = new TreeMap<Integer, Map<Integer, Integer>>();
			for (Instance instance : instances) {
				int prediction = instance.getFeaturesNumeric().length == 0 ? dict
						.getLabelNumeric(String.valueOf(EventType.Non_trigger))
						: this.predict(instance);

				// confusion matrix
				if (!confusionMatrix.containsKey(instance.getLabel())) {
					TreeMap<Integer, Integer> values = new TreeMap<Integer, Integer>();
					values.put(prediction, 1);
					confusionMatrix.put(instance.getLabel(), values);
				} else {
					Map<Integer, Integer> values = confusionMatrix.get(instance
							.getLabel());
					if (!values.containsKey(prediction)) {
						values.put(prediction, 1);
					} else {
						int count = values.get(prediction) + 1;
						values.put(prediction, count);
					}
				}
			}
			printConfusionMatrix(confusionMatrix, dict);
		} else {

			JCas jcas = instancesGetter.processSingleFile(file);
			Map<Integer, Set<Pair>> pairsOfArticle = StanfordDependencyReader
					.getPairs(new File(FileUtil.removeFileNameExtension(
							file.getAbsolutePath()).concat(".sdepcc")));

			FSIterator<Annotation> sentenceIter = jcas.getAnnotationIndex(
					Sentence.type).iterator();
			int proteinNum = jcas.getAnnotationIndex(Protein.type).size();
			while (sentenceIter.hasNext()) {

				Sentence sentence = (Sentence) sentenceIter.next();
				// List<Token> originalTokens = JCasUtil.selectCovered(jcas,
				// Token.class, sentence);
				List<Protein> sentenceProteins = JCasUtil.selectCovered(jcas,
						Protein.class, sentence);
				Set<Pair> pairsOfSentence = pairsOfArticle
						.get(sentence.getId());

				// instancesGetter.postProcessSentenceTokens(jcas,
				// originalTokens,
				// sentenceProteins, pairsOfSentence);
				List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class,
						sentence);
				DependencyExtractor dependencyExtractor = new DependencyExtractor(
						JCasUtil.selectCovered(jcas, Token.class, sentence),
						pairsOfSentence);
				for (Token token : tokens) {
					Instance instance = instancesGetter.tokenToInstance(jcas,
							token, null, tokens, sentenceProteins,
							pairsOfSentence, dependencyExtractor);
					instance = dict.instanceToNumeric(instance);
					int prediction = instance.getFeaturesNumeric().length == 0 ? dict
							.getLabelNumeric(String
									.valueOf(EventType.Non_trigger)) : this
							.predict(instance);

					if (token.getCoveredText().toLowerCase().indexOf("import") > -1) {
						System.out.println(instance.getLabel() + ":"
								+ instance.getLabelString() + "\t"
								+ token.getBegin() + ":" + token.getEnd());
						for (String[] feature : instance.getFeaturesString()) {
							for (String value : feature) {
								System.out.print("\t" + value);
							}
						}
						System.out.println();
						System.out.print(prediction);
						for (int value : instance.getFeaturesNumeric()) {
							System.out.print("\t" + value);
						}
						System.out.println();
					}
					if (prediction != dict.getLabelNumeric(String
							.valueOf(EventType.Non_trigger))) {
						Trigger trigger = new Trigger(jcas, token.getBegin(),
								token.getEnd());
						trigger.setEventType(dict.getLabelString(prediction));
						trigger.setId("T".concat(String.valueOf(++proteinNum)));
						triggers.add(trigger);
					}
				}
			}
		}
		return triggers;
	}

	private void printConfusionMatrix(
			Map<Integer, Map<Integer, Integer>> confusionMatrix,
			InstanceDictionary dict) {
		for (EventType goldType : EventType.values()) {
			System.out.print("\t".concat(String.valueOf(goldType)));
		}
		System.out.println();
		for (EventType goldType : EventType.values()) {
			System.out.print(String.valueOf(goldType));
			Map<Integer, Integer> predictions = confusionMatrix.get(dict
					.getLabelNumeric(String.valueOf(goldType)));
			for (EventType predictedType : EventType.values()) {
				System.out.print("\t");
				if (null != predictions) {
					System.out.print(String.valueOf(predictions.get(dict
							.getLabelNumeric(String.valueOf(predictedType)))));
				}
			}
			System.out.println();
		}
	}

	public void crossValidate(String dir) {
		//
		// collect all instances and fetch syntactical information
		//
		TokenInstances trainingInstances = new TokenInstances();
		trainingInstances.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
		List<Instance> instances = trainingInstances
				.getInstances(new File(dir));
		logger.info(String.valueOf(instances.size()).concat(
				" instances are collected."));

		Collections.shuffle(instances);
		trainingInstances.saveInstances(new File("./model/instances.csv"));
		InstanceDictionary dictAll = new InstanceDictionary();
		dictAll.creatNumericDictionary(instances);
		trainingInstances.saveNumericInstances(new File(
				"./model/instances.num.csv"));
		trainingInstances.saveSvmLightInstances(new File(
				"./model/instances.svm.csv"));
		logger.info("Shuffle instances.");

		//
		// n-fold cross validation
		//
		int fold = 10;
		int step = instances.size() / fold;

		logger.info(String.valueOf(fold).concat(" fold cross validatation."));

		double recallSum = 0, precisionSum = 0;
		for (int i = 0; i < fold; i++) {

			logger.info(String.valueOf(i).concat(" fold cross validatation."));

			List<Instance> subTestingInstances = instances.subList(step * i,
					step * (i + 1));
			List<Instance> subTrainingInstances = new ArrayList<Instance>();
			subTrainingInstances.addAll(instances.subList(0, step * i));
			subTrainingInstances.addAll(instances.subList(step * (i + 1),
					instances.size()));

			InstanceDictionary dict = new InstanceDictionary();
			dict.creatNumericDictionary(subTrainingInstances);
			logger.info("Create dictionary.");
			dict.saveDictionary(new File("./model/triggers." + i + ".dict"));
			logger.info("Save dictionary.");

			// Collections.shuffle(subTrainingInstances);
			Collections.shuffle(subTestingInstances);

			Timer timer = new Timer();
			timer.start();

			TriggerRecogniser tr = new TriggerRecogniser();
			// tr.train(subTrainingInstances, 50);
			tr.train(subTrainingInstances);
			timer.stop();
			logger.info(String.valueOf(i).concat(" fold training takes ")
					.concat(String.valueOf(timer.getRunningTime())));

			Fscore fscore = tr.test(subTestingInstances, dict, i);

			recallSum = recallSum + fscore.getRecall();
			precisionSum = precisionSum + fscore.getPrecision();

			tr.saveModel(new File("./model/triggers.".concat(classifierName)
					.concat("." + i + ".model")));
		}

		System.out.println(new Fscore(recallSum / fold, precisionSum / fold));

	}

	private Fscore test(List<Instance> instances, InstanceDictionary dict,
			int counter) {

		int tp = 0, fp = 0, tn = 0, fn = 0, correct = 0, total = 0;
		StringBuffer tp_instances = new StringBuffer();
		StringBuffer fp_nonTrigger_instances = new StringBuffer();
		StringBuffer fp_trigger_instances = new StringBuffer();

		// Collections.shuffle(instances);
		for (Instance instance : instances) {

			instance = dict.instanceToNumeric(instance);

			int prediction = this.predict(instance);
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
				"./result/fp_nonTrigger".concat(String.valueOf(counter))
						.concat(".txt")));
		FileUtil.saveFile(fp_trigger_instances.toString(),
				new File("./result/fp_trigger".concat(String.valueOf(counter))
						.concat(".txt")));
		FileUtil.saveFile(tp_instances.toString(), new File("./result/tp"
				.concat(String.valueOf(counter)).concat(".txt")));

		Fscore fscore = new Fscore(tp, fp, tn, fn);
		System.out.println(fscore);
		System.out.println(new Accurary(correct, total));

		return fscore;
	}

	public static void main(String[] args) {

		if (args.length == 0 || !args[0].equals("predict")
				&& !args[0].equals("train") && !args[0].equals("test")
				&& !args[0].equals("cross")) {
			throw new IllegalArgumentException(
					"The first argument has to be \"predict\" or \"train\". ");
		}

		File file = new File(args[1]);

		TriggerRecogniser tr = new TriggerRecogniser();

		if (args[0].equals("train")) {
			if (!file.isDirectory()) {
				throw new IllegalArgumentException(
						"The second argument has to be the training directory. ");
			}
			tr.train(args[1], 1);
		} else if (args[0].equals("predict")) {

			if (!file.isFile() && !file.isDirectory()) {
				throw new IllegalArgumentException(
						"The second argument has to be a file.");
			}

			tr.loadModel(new File("./model/triggers.".concat(tr.classifierName)
					.concat(".model")));
			InstanceDictionary dict = new InstanceDictionary();
			dict.loadDictionary(new File("./model/triggers.".concat(
					tr.classifierName).concat(".dict")));

			List<Trigger> triggers = tr.predict(file, dict, false);
			for (Trigger trigger : triggers) {
				System.out.println(trigger.getId().concat("\t")
						.concat(trigger.getEventType()).concat(" ")
						.concat(String.valueOf(trigger.getBegin())).concat(" ")
						.concat(String.valueOf(trigger.getEnd())).concat("\t")
						.concat(trigger.getCoveredText()));
			}

		} else if (args[0].equals("test")) {

			tr.loadModel(new File("./model/triggers.".concat(tr.classifierName)
					.concat(".model")));

			TokenInstances testInstances = new TokenInstances();
			testInstances.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
			List<Instance> instances = testInstances.getInstances(new File(
					"./data/development/"));
			logger.info(String.valueOf(instances.size()).concat(
					" instances are collected."));

			InstanceDictionary dict = new InstanceDictionary();
			dict.loadDictionary(new File("./model/triggers.".concat(
					tr.classifierName).concat(".dict")));
			dict.instancesToNumeric(instances);

			testInstances.saveInstances(new File(
					"./model/instances.trigger.dev.txt"));
			testInstances.saveNumericInstances(new File(
					"./model/instances.trigger.num.dev.txt"));
			testInstances.saveSvmLightInstances(new File(
					"./model/instances.trigger.svm.dev.txt"));

			System.out.print("Finish collecting events.");

			int total = 0, correct = 0;
			for (Instance instance : instances) {
				int prediction = tr.predict(instance);
				if (prediction == instance.getLabel()) {
					correct++;
				}
				total++;
			}
			System.out.println(new Accurary(correct, total));
		} else if (args[0].equals("cross")) {
			tr.crossValidate(args[1]);
		}
	}
}
