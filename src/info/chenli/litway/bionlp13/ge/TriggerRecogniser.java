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
import info.chenli.litway.util.UimaUtil;
import info.chenli.litway.util.StanfordDependencyReader.Pair;
import info.chenli.litway.util.Stemmer;
import info.chenli.litway.util.Timer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

/*		trainingInstances.saveInstances(new File(
				"./model/instances.trigger.txt"));
		trainingInstances.saveNumericInstances(new File(
				"./model/instances.trigger.num.txt"));
		trainingInstances.saveSvmLightInstances(new File(
				"./model/instances.trigger.svm.txt"));
*/
		Timer timer = new Timer();
		timer.start();

		train(instances, round);
		timer.stop();
		logger.info("Training takes ".concat(String.valueOf(timer
				.getRunningTime())));

		saveModel(new File("./model/triggers.".concat(classifierName).concat(
				".model")));

	}
	
	public void train2(String trainingDir, int round) {
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
		dict.saveDictionary(new File("./model/triggers.train.devel.".concat(classifierName)
				.concat(".dict")));
		logger.info("Save dictionary.");

/*		trainingInstances.saveInstances(new File(
				"./model/instances.trigger.txt"));
		trainingInstances.saveNumericInstances(new File(
				"./model/instances.trigger.num.txt"));
		trainingInstances.saveSvmLightInstances(new File(
				"./model/instances.trigger.svm.txt"));
*/
		Timer timer = new Timer();
		timer.start();

		train(instances, round);
		timer.stop();
		logger.info("Training takes ".concat(String.valueOf(timer
				.getRunningTime())));

		saveModel(new File("./model/triggers.train.devel.".concat(classifierName).concat(
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
			Map<Integer, Set<Pair>> pairsOfArticle = new HashMap<Integer, Set<Pair>>();
			if (new File(FileUtil.removeFileNameExtension(
					UimaUtil.getJCasFilePath(jcas)).concat(".sdepcc")).exists()) {
				pairsOfArticle = StanfordDependencyReader
						.getPairs(new File(FileUtil.removeFileNameExtension(
								UimaUtil.getJCasFilePath(jcas)).concat(".sdepcc")));
			} else {
				pairsOfArticle = StanfordDependencyReader
						.getPairs(new File(FileUtil.removeFileNameExtension(
								UimaUtil.getJCasFilePath(jcas)).concat(".sd")));
			}

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
				File word2vecFile = new File("/home/songrq/word2vec/data/word2vec100");
				Map<String,double[]> word2vec = ReadWord2vec.word2vec(word2vecFile); 

				for (Token token : tokens) {
					Instance instance = instancesGetter.tokenToInstance(jcas,
							token, null, tokens, sentenceProteins,
							pairsOfSentence, dependencyExtractor, word2vec);
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
/*		StringBuffer tp_instances = new StringBuffer();
		StringBuffer fp_nonTrigger_instances = new StringBuffer();
		StringBuffer fp_trigger_instances = new StringBuffer();*/
		// Collections.shuffle(instances);
		for (Instance instance : instances) {
			instance = dict.instanceToNumeric(instance);
			int prediction = this.predict(instance);
			if (instance.getLabelString() == String
					.valueOf(EventType.Non_trigger)) {

				if (prediction != instance.getLabel()) {
					fp++;
					total++;
					
					/*fp_nonTrigger_instances.append(String.valueOf(instance.getSentenceId())
							.concat("\t").concat(String.valueOf(instance.getTokenId()))
							.concat("\t").concat(dict.getLabelString(prediction))
							.concat("\t").concat(instance.getLabelString())
							.concat("\t").concat(instance.getFeaturesString().get(0)[0])
							.concat("\n"));*/
				} else {
					tn++;
				}
			} else {
				if (prediction != instance.getLabel()) {
					//fp++;
					fn++;
					/*fp_trigger_instances.append(String.valueOf(instance.getSentenceId())
							.concat("\t").concat(String.valueOf(instance.getTokenId()))
							.concat("\t").concat(dict.getLabelString(prediction))
							.concat("\t").concat(instance.getLabelString())
							.concat("\t").concat(instance.getFeaturesString().get(0)[0])
							//.concat("\t").concat(instance.getFeaturesString().get(1)[0])
							.concat("\n"));*/
				} else {
					correct++;//鎻愬彇鍑虹殑trigger鏈夊嚑涓纭殑
					tp++;
					/*tp_instances.append(String.valueOf(instance.getSentenceId())
							.concat("\t").concat(String.valueOf(instance.getTokenId()))
							.concat("\t").concat(dict.getLabelString(prediction))
							.concat("\t").concat(instance.getLabelString())
							.concat("\t").concat(instance.getFeaturesString().get(0)[0])
							//.concat("\t").concat(instance.getFeaturesString().get(1)[0])
							.concat("\n"));*/
				}
				
				if (prediction != dict.getLabelNumeric(String
						.valueOf(EventType.Non_trigger))) {
					total++;//鎻愬彇鍑哄灏戜釜trigger
				}
			}
		}

/*		FileUtil.saveFile(fp_nonTrigger_instances.toString(), new File(
				"./result/fp_nonTrigger".concat(String.valueOf(counter))
						.concat(".txt")));
		FileUtil.saveFile(fp_trigger_instances.toString(),
				new File("./result/fp_trigger".concat(String.valueOf(counter))
						.concat(".txt")));
		FileUtil.saveFile(tp_instances.toString(), new File("./result/tp"
				.concat(String.valueOf(counter)).concat(".txt")));
*/
		Fscore fscore = new Fscore(tp, fp, tn, fn);
		System.out.println(fscore);
		System.out.println(new Accurary(correct, total));

		return fscore;
	}
	/**
	 * train 閽堝train锛岃緭鍑簃odel锛宨nstance 
	 * predict 閽堝test锛岃緭鍑簍rigger
	 * test 閽堝development锛岃緭鍑篿nstance锛孉ccuracy
	 * cross 閽堝train锛屼氦鍙夐獙璇�
	 * @param args
	 */
	public static void main(String[] args) {


		TriggerRecogniser tr = new TriggerRecogniser();
		//tr.train("/media/songrq/soft/litway/数据/"
		//			+ "BioNLP13/BioNLP-ST-2013_GE_train_data_yuanShuJu", 1);
		tr.train2(args[0], 1);

		
		//tr.train2("/media/songrq/soft/litway/数据/"
		//		+ "BioNLP13/BioNLP-ST-2013_GE_train_devel_data_yuanShuJu", 1);
////////////////////////////////////////////////////////////////////////////////////////
		
		
		/*tr.loadModel(new File("./model/triggers.".concat(tr.classifierName)
				.concat(".model")));

		TokenInstances testInstances = new TokenInstances();
		testInstances.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
		List<Instance> instances = testInstances.getInstances(new File("/media/songrq/soft/litway/数据/"
						+ "BioNLP13/BioNLP-ST-2013_GE_devel_data_yuanShuJu"));
		logger.info(String.valueOf(instances.size()).concat(
				" instances are collected."));
		
		InstanceDictionary dict = new InstanceDictionary();
		dict.loadDictionary(new File("./model/triggers.".concat(
				tr.classifierName).concat(".dict")));
		tr.test(instances, dict, 1);
		dict.instancesToNumeric(instances);

		//testInstances.saveInstances(new File(
		//		"./model/instances.trigger.dev.txt"));
		//testInstances.saveNumericInstances(new File(
		//		"./model/instances.trigger.num.dev.txt"));
		//testInstances.saveSvmLightInstances(new File(
		//		"./model/instances.trigger.svm.dev.txt"));

		System.out.print("Finish collecting events.");
		
		int total = 0, correct = 0, tp, tn = 0, n = 0, fn, fp;
		float p, r, f;
		int  total14[] = new int [14];
		int  correct14[] = new int [14];
		String ss[] = {"Non_trigger", "Gene_expression", "Transcription", "Protein_catabolism", "Localization", "Binding", "Protein_modification", "Phosphorylation", "Ubiquitination", "Acetylation", "Deacetylation", "Regulation", "Positive_regulation", "Negative_regulation"};
		for (Instance instance : instances) {
			for (int i=0; i<14; i++) {
				if (instance.getLabelString().equalsIgnoreCase(ss[i])) {
					total14[i]++;
				}
			}
			int prediction = tr.predict(instance);
			if (prediction == instance.getLabel()) {
				for (int i=0; i<14; i++) {
					if (instance.getLabelString().equalsIgnoreCase(ss[i])) {
						correct14[i]++;
					}
				}
				if (instance.getLabelString() == String
						.valueOf(EventType.Non_trigger)){
					tn++;
				}
				correct++;
			}
			
			if (instance.getLabelString() == String
					.valueOf(EventType.Non_trigger)){
				n++;
			}
			total++;
		}
		
		fp = n - tn;
		tp = correct - tn;
		fn = total - n - tp;
		p = (float) tp / (tp + fp);
		r = (float) tp / (tp + fn);
		f = (float) 2 * p * r / (p + r);
		
		System.out.println(new Accurary(correct, total));
		System.out.println("tp: " + tp + "   fp: " + fp + "   fn: " + fn);
		System.out.println("p: " + p + "   r: " + r + "   f: " + f);
		for (int i=0; i<14; i++) {
			System.out.print(ss[i]);
			System.out.print("\t\t");
			System.out.print(total14[i]);
			System.out.print("\t\t");
			System.out.print(correct14[i]);
			System.out.print("\t\t");
			System.out.print((float)correct14[i]/total14[i]);
			System.out.print("\n");
		}*/
	}
}
