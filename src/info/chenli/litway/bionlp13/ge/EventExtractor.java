package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.classifier.InstanceDictionary;
import info.chenli.litway.corpora.Event;
import info.chenli.litway.corpora.Protein;
import info.chenli.litway.corpora.Sentence;
import info.chenli.litway.corpora.Token;
import info.chenli.litway.corpora.Trigger;
import info.chenli.litway.util.Combinations;
import info.chenli.litway.util.DependencyExtractor;
import info.chenli.litway.util.FileFilterImpl;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.StanfordDependencyReader;
import info.chenli.litway.util.StanfordDependencyReader.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

public class EventExtractor extends TokenInstances {

	private final static Logger logger = Logger.getLogger(EventExtractor.class
			.getName());

	private String classifierName = "liblinear";

	public void train(File dir) {

		if (!dir.isDirectory()) {
			logger.info(dir.getAbsolutePath().concat(" is not a directory."));
		}

		//
		// train trigger
		//

		//
		// train theme
		//

		//
		// train cause
		//
	}

	public void extract(File file) {

		if (file.isDirectory()) {

			for (File f : file.listFiles(new FileFilterImpl(".txt"))) {
				extract(f);
			}

		} else if (file.isFile()) {

			logger.info("Extracting from ".concat(file.getName()));
			String newFileName = "./result/".concat(
					file.getName()
							.substring(0, file.getName().lastIndexOf(".")))
					.concat(".a2");
			FileUtil.saveFile(extractFromSingleFile(file),
					new File(newFileName));
			logger.info("Result saved in ".concat(newFileName));
		}
	}

	/**
	 * Extract events from the given file.
	 * 
	 * @param file
	 */
	public String extractFromSingleFile(File file) {

		Map<Integer, List<Trigger>> triggers = new TreeMap<Integer, List<Trigger>>();
		Map<Integer, List<Event>> events = new TreeMap<Integer, List<Event>>();
		// Initialize the file
		JCas jcas = this.processSingleFile(file);
		int proteinNum = jcas.getAnnotationIndex(Protein.type).size();
		Map<Integer, Set<Pair>> pairsOfArticle = StanfordDependencyReader
				.getPairs(new File(FileUtil.removeFileNameExtension(
						file.getAbsolutePath()).concat(".sdepcc")));
		
		File word2vecFile = new File("/home/songrq/word2vec/data/word2vec100");
		Map<String,double[]> word2vec = ReadWord2vec.word2vec(word2vecFile); 

		//
		// Initialize the classifiers
		//

		// trigger
		TriggerRecogniser triggerRecogniser = new TriggerRecogniser();
		triggerRecogniser.loadModel(new File("./model/triggers.".concat(
				classifierName).concat(".model")));
		InstanceDictionary triggerDict = new InstanceDictionary();
		triggerDict.loadDictionary(new File("./model/triggers.".concat(
				classifierName).concat(".dict")));

		// theme
		ThemeRecogniser themeRecogniser = new ThemeRecogniser();
		themeRecogniser.loadModel(new File("./model/themes.".concat(
				classifierName).concat(".model")));
		InstanceDictionary themeDict = new InstanceDictionary();
		themeDict.loadDictionary(new File("./model/themes.".concat(
				classifierName).concat(".dict")));

		// cause
		CauseRecogniser causeRecogniser = new CauseRecogniser();
		causeRecogniser.loadModel(new File("./model/causes.".concat(
				classifierName).concat(".model")));
		InstanceDictionary causeDict = new InstanceDictionary();
		causeDict.loadDictionary(new File("./model/causes.".concat(
				classifierName).concat(".dict")));

		// binding
		BindingRecogniser bindingRecogniser = new BindingRecogniser();
		bindingRecogniser.loadModel(new File("./model/bindings.".concat(
				classifierName).concat(".model")));
		InstanceDictionary bindingDict = new InstanceDictionary();
		bindingDict.loadDictionary(new File("./model/bindings.".concat(
				classifierName).concat(".dict")));

		// Initialize the iterator and counter
		FSIterator<Annotation> sentenceIter = jcas.getAnnotationIndex(
				Sentence.type).iterator();
		int eventIndex = 1;

		while (sentenceIter.hasNext()) {

			Sentence sentence = (Sentence) sentenceIter.next();
			// protein
			List<Protein> sentenceProteins = JCasUtil.selectCovered(jcas,
					Protein.class, sentence);
			if (sentenceProteins.size() <= 0) {
				continue;
			}
			
			Set<Pair> pairsOfSentence = pairsOfArticle.get(sentence.getId());

			// The queue where newly generated events are put
			LinkedBlockingQueue<Event> newEvents = new LinkedBlockingQueue<Event>();
	
			//
			// trigger detection
			//
			List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class,
					sentence);
			DependencyExtractor dependencyExtractor = new DependencyExtractor(
					tokens, pairsOfSentence);

			if (null == triggers.get(sentence.getId())) {
				triggers.put(sentence.getId(), new ArrayList<Trigger>());
			}

			for (Token token : tokens) {

				/*if (!POS.isPos(token.getPos())) {
					continue triggerDetectionLoop;
				}
				for (Protein protein : sentenceProteins) {
					if ((token.getBegin() >= protein.getBegin() && token
							.getBegin() <= protein.getEnd())
							|| (token.getEnd() >= protein.getBegin() && token
									.getEnd() <= protein.getEnd())) {
						continue triggerDetectionLoop;
					}
				}*/
				Instance tokenInstance = tokenToInstance(jcas, token, null,
						tokens, sentenceProteins, pairsOfSentence,
						dependencyExtractor, word2vec);
				// set the token filter here
				// if (!TriggerRecogniser.isConsidered(tokenInstance
				// .getFeaturesString().get(2))) {
				// continue;
				// }
				if (tokenInstance != null) {
					int prediction = triggerRecogniser.predict(triggerDict
							.instanceToNumeric(tokenInstance));
	
					if (prediction != triggerDict.getLabelNumeric(String
							.valueOf(EventType.Non_trigger))) {
	
						Trigger trigger = new Trigger(jcas, token.getBegin(),
								token.getEnd());
						trigger.setEventType(triggerDict.getLabelString(prediction));
						trigger.setId("T".concat(String.valueOf(++proteinNum)));
						triggers.get(sentence.getId()).add(trigger);
					}
				}
			}

			//
			// theme assignment
			//

			// 1. iterate through all proteins
			if (null == triggers.get(sentence.getId())) {
				continue;
			}
			if (null == events.get(sentence.getId())) {
				events.put(sentence.getId(), new ArrayList<Event>());
			}

			for (Trigger trigger : triggers.get(sentence.getId())) {

				if (EventType.isSimpleEvent(trigger.getEventType())) {

					for (Protein protein : sentenceProteins) {

						Instance proteinInstance = themeToInstance(jcas,
								sentence, protein, trigger, pairsOfSentence,
								dependencyExtractor, false);
						double prediction = themeRecogniser.predict(themeDict
								.instanceToNumeric(proteinInstance)
								.getFeaturesNumeric(), proteinInstance);

//						if (trigger.getEventType().equals(
//								String.valueOf(EventType.Localization))
//								&& protein.getCoveredText().toLowerCase()
//										.indexOf("phosp") > -1) {
//							System.out.println(proteinInstance.getLabel() + ":"
//									+ proteinInstance.getLabelString() + "("
//									+ trigger.getCoveredText() + ")" + "\t"
//									+ protein.getBegin() + ":"
//									+ protein.getEnd() + "("
//									+ protein.getCoveredText() + ")");
//							for (String[] feature : proteinInstance
//									.getFeaturesString()) {
//								for (String value : feature) {
//									System.out.print("\t" + value);
//								}
//							}
//							System.out.println();
//							System.out.print(prediction);
//							for (int value : proteinInstance
//									.getFeaturesNumeric()) {
//								System.out.print("\t" + value);
//							}
//							System.out.println();
//						}

						if (prediction == themeDict.getLabelNumeric("Theme")) {

							Event event = new Event(jcas);
							event.setId(String.valueOf(eventIndex++));
							event.setTrigger(trigger);
							StringArray themes = new StringArray(jcas, 1);
							themes.set(0, protein.getId());
							event.setThemes(themes);
							events.get(sentence.getId()).add(event);
							newEvents.add(event);
						}
					}

				} else if (EventType.isBindingEvent(trigger.getEventType())) {

					List<Protein> themes = new ArrayList<Protein>();

					for (Protein protein : sentenceProteins) {

						Instance proteinInstance = themeToInstance(jcas,
								sentence, protein, trigger, pairsOfSentence,
								dependencyExtractor, false);
						double prediction = themeRecogniser.predict(themeDict
								.instanceToNumeric(proteinInstance)
								.getFeaturesNumeric(), proteinInstance);

						if (prediction == themeDict.getLabelNumeric("Theme")) {
							themes.add(protein);
						}

					}
					if (themes.size() > 0) {

						// event.setId(String.valueOf(eventIndex++));

						List<List<Protein>> predictedThemesComb = new ArrayList<List<Protein>>();
						if (themes.size() > 1) {

							Combinations<Protein> combs = new Combinations<Protein>(
									themes);

							for (List<Protein> candidateThemes : combs
									.getCombinations()) {
								if (candidateThemes.size() > 3) {
									continue;
								}
								Instance bindingInstance = bindingDict
										.instanceToNumeric(bindingEventToInstance(
												jcas, sentence, trigger,
												candidateThemes,
												dependencyExtractor, false));
								if (bindingRecogniser.predict(bindingInstance) == bindingDict
										.getLabelNumeric("Binding")) {
									predictedThemesComb.add(candidateThemes);
								}
							}

						} else
						// when there is only one theme
						{
							List<Protein> candidateThemes = new ArrayList<Protein>();
							candidateThemes.add(themes.get(0));
							predictedThemesComb.add(candidateThemes);
						}

						// clean the themes which are fully covered by another
						List<List<Protein>> checkedThemesComb = new ArrayList<List<Protein>>();
						checkingTheme: for (List<Protein> beingCheckedThemes : predictedThemesComb) {
							if (checkedThemesComb.contains(beingCheckedThemes)) {
								continue;
							} else {

								List<List<Protein>> copy = new ArrayList<List<Protein>>(
										checkedThemesComb);
								for (List<Protein> checkedThemes : copy) {

									if (checkedThemes
											.containsAll(beingCheckedThemes)) {
										continue checkingTheme;
									} else if (beingCheckedThemes
											.containsAll(checkedThemes)) {
										checkedThemesComb.remove(checkedThemes);
									}
								}
								checkedThemesComb.add(beingCheckedThemes);
							}
						}

						for (List<Protein> predictedThemes : checkedThemesComb) {

							Event newBindingEvent = new Event(jcas);
							newBindingEvent.setTrigger(trigger);
							newBindingEvent.setId(String.valueOf(eventIndex++));

							StringArray eventThemes = new StringArray(jcas,
									predictedThemes.size());

							for (Protein theme : predictedThemes) {
								eventThemes.set(predictedThemes.indexOf(theme),
										theme.getId());
							}
							newBindingEvent.setThemes(eventThemes);

							events.get(sentence.getId()).add(newBindingEvent);
							newEvents.add(newBindingEvent);
						}
					}

				} else if (EventType.isComplexEvent(trigger.getEventType())) {

					for (Protein protein : sentenceProteins) {

						Instance proteinInstance = themeDict
								.instanceToNumeric(themeToInstance(jcas,
										sentence, protein, trigger,
										pairsOfSentence, dependencyExtractor,
										false));

						double prediction = themeRecogniser
								.predict(proteinInstance.getFeaturesNumeric(), proteinInstance);

						if (prediction == themeDict.getLabelNumeric("Theme")) {

							Event event = new Event(jcas);
							event.setId(String.valueOf(eventIndex++));
							event.setTrigger(trigger);
							StringArray themes = new StringArray(jcas, 1);
							themes.set(0, protein.getId());
							event.setThemes(themes);
							events.get(sentence.getId()).add(event);
							newEvents.add(event);
						}
					}
				}
			}

			// 2. check all discovered events whether they can be themes

			for (Event themeEvent : newEvents) {
				// System.out.println("(" +
				// themeEvent.getTrigger().getEventType()
				// + ":" + themeEvent.getTrigger().getCoveredText() + ")"
				// + themeEvent.getTrigger().getBegin() + ":"
				// + themeEvent.getTrigger().getEnd());

				for (Trigger trigger : triggers.get(sentence.getId())) {

					if (EventType.isRegulatoryEvent(trigger.getEventType())) {

						if (themeEvent.getTrigger().getBegin() == trigger
								.getBegin()) {
							continue;
						}

						Instance triggerTokenInstance = themeToInstance(jcas,
								sentence, themeEvent.getTrigger(), trigger,
								pairsOfSentence, dependencyExtractor, false);

						double prediction = themeRecogniser.predict(themeDict
								.instanceToNumeric(triggerTokenInstance)
								.getFeaturesNumeric(), triggerTokenInstance);

						if (prediction == themeDict.getLabelNumeric("Theme")) {

							Event event = new Event(jcas);
							event.setId(String.valueOf(eventIndex++));
							event.setTrigger(trigger);
							StringArray themes = new StringArray(jcas, 1);
							themes.set(0, "E".concat(themeEvent.getId()));
							event.setThemes(themes);
							events.get(sentence.getId()).add(event);
							newEvents.add(event);
						}

					}
				}
			}

			//
			// cause
			//
			for (Event event : events.get(sentence.getId())) {

				if (!EventType
						.isComplexEvent(event.getTrigger().getEventType())) {
					continue;
				}

				// protein
				for (Protein protein : sentenceProteins) {

					Instance proteinInstance = causeToInstance(jcas, sentence,
							protein, event.getTrigger(), pairsOfSentence,
							dependencyExtractor, false,
							getThemeToken(jcas, event, sentence));
					double prediction = causeRecogniser.predict(causeDict
							.instanceToNumeric(proteinInstance)
							.getFeaturesNumeric(), proteinInstance);

					if (prediction == causeDict.getLabelNumeric(String
							.valueOf("Cause"))) {
						event.setCause(protein.getId());
					}
				}

				if (!EventType.isRegulatoryEvent(event.getTrigger()
						.getEventType())) {
					continue;
				}

				// event
				for (Event causeEvent : events.get(sentence.getId())) {

					if (causeEvent.getTrigger().getBegin() == event
							.getTrigger().getBegin()) {
						continue;
					}

					Token themeToken = getThemeToken(jcas, event, sentence);
					Instance causeEventInstance = causeToInstance(jcas,
							sentence, causeEvent.getTrigger(),
							event.getTrigger(), pairsOfSentence,
							dependencyExtractor, false, themeToken);
					double prediction = causeRecogniser.predict(causeDict
							.instanceToNumeric(causeEventInstance)
							.getFeaturesNumeric(), causeEventInstance);
					// if (event
					// .getTrigger()
					// .getEventType()
					// .equals(String
					// .valueOf(EventType.Positive_regulation))
					// && causeEvent.getTrigger().getCoveredText()
					// .toLowerCase().indexOf("phosp") > -1) {
					// System.out.println(causeEventInstance.getLabel() + ":"
					// + causeEventInstance.getLabelString() + "("
					// + event.getTrigger().getCoveredText() + ")"
					// + "\t" + causeEvent.getTrigger().getBegin()
					// + ":" + causeEvent.getTrigger().getEnd() + "("
					// + causeEvent.getTrigger().getCoveredText()
					// + ")");
					// for (String[] feature : causeEventInstance
					// .getFeaturesString()) {
					// for (String value : feature) {
					// System.out.print("\t" + value);
					// }
					// }
					// System.out.println();
					// System.out.print(prediction);
					// for (int value : causeEventInstance
					// .getFeaturesNumeric()) {
					// System.out.print("\t" + value);
					// }
					// System.out.println();
					// }

					if (prediction == causeDict.getLabelNumeric(String
							.valueOf("Cause"))) {
						event.setCause("E".concat(causeEvent.getId()));
					}
				}
			}
		}

		return resultToString(triggers, events);
	}

	private String resultToString(Map<Integer, List<Trigger>> triggers,
			Map<Integer, List<Event>> events) {

		StringBuffer sb = new StringBuffer();

		// print triggers
		for (List<Trigger> sentenceTriggers : triggers.values()) {
			for (Trigger trigger : sentenceTriggers) {
				sb.append(trigger.getId().concat("\t")
						.concat(trigger.getEventType()).concat(" ")
						.concat(String.valueOf(trigger.getBegin())).concat(" ")
						.concat(String.valueOf(trigger.getEnd())).concat("\t")
						.concat(trigger.getCoveredText()).concat("\n"));
			}
		}

		for (List<Event> sentenceEvents : events.values()) {
			for (Event event : sentenceEvents) {
				sb.append("E").append(event.getId()).append("\t")
						.append(event.getTrigger().getEventType()).append(":")
						.append(event.getTrigger().getId());

				for (int j = 0; j < event.getThemes().size(); j++) {
					sb.append(" Theme").append(j == 0 ? "" : j + 1).append(":")
							.append(event.getThemes(j));
				}

				if (null != event.getCause() && "" != event.getCause()) {
					sb.append(" Cause:").append(event.getCause());
				}
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	public static void main(String[] args) {

		EventExtractor ee = new EventExtractor();
		ee.setTaeDescriptor("/desc/TestSetAnnotator.xml");

		File inputFile = new File(args[0]);
		ee.extract(inputFile);

		// if (args.length == 2 && args[1].equals("cross")) {
		// File[] files = inputFile.listFiles(new FileFilter() {
		//
		// @Override
		// public boolean accept(File pathname) {
		// if (pathname.getName().startsWith("PMC-")
		// && pathname.getName().endsWith(".txt")) {
		// return true;
		// }
		// return false;
		// }
		// });
		// List<File> fileList = Arrays.asList(files);
		//
		// int fold = 10;
		// for () {}
		// }
	}
}
