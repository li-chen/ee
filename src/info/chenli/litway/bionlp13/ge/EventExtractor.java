package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.classifier.InstanceDictionary;
import info.chenli.litway.corpora.Event;
import info.chenli.litway.corpora.Protein;
import info.chenli.litway.corpora.Sentence;
import info.chenli.litway.corpora.Token;
import info.chenli.litway.corpora.Trigger;
import info.chenli.litway.util.BioLemmatizerUtil;
import info.chenli.litway.util.Combinations;
import info.chenli.litway.util.DependencyExtractor;
import info.chenli.litway.util.FileFilterImpl;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.StanfordDependencyReader;
import info.chenli.litway.util.StanfordDependencyReader.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

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

	public void extract(File file) throws IOException {
		
		int[] perform = {0, 0, 0, 0};//tp, tn, fp, fn
		
		if (file.isDirectory()) {

			for (File f : file.listFiles(new FileFilterImpl(".txt"))) {
				
				/*perform = extractFromSingleFile(f, perform);
				
				for(int i:perform) {
					System.out.println(i);
				}*/
				
				extract(f);
			}
			
			

		} else if (file.isFile()) {

			
			logger.info("Extracting from ".concat(file.getName()));
			String newFileName = "./result/".concat(
					file.getName()
							.substring(0, file.getName().lastIndexOf(".")))
					.concat(".a2");
			FileUtil.saveFile(extractFromSingleFile(file, perform),
					new File(newFileName));
			logger.info("Result saved in ".concat(newFileName));
		}
	}

	/**
	 * Extract events from the given file.
	 * 
	 * @param file
	 * @throws IOException 
	 */
	public String extractFromSingleFile(File file, int[] perform) throws IOException {

		Map<Integer, List<Trigger>> triggers = new TreeMap<Integer, List<Trigger>>();
		Map<Integer, LinkedBlockingQueue<Event>> events = new TreeMap<Integer, LinkedBlockingQueue<Event>>();
		// Initialize the file
		JCas jcas = this.processSingleFile(file);
		int proteinNum = jcas.getAnnotationIndex(Protein.type).size();
		Map<Integer, Set<Pair>> pairsOfArticle = StanfordDependencyReader
				.getPairs(new File(FileUtil.removeFileNameExtension(
						file.getAbsolutePath()).concat(".sdepcc")));

		String[] labelDict = {	"Non_trigger", "Gene_expression", "Transcription", "Protein_catabolism", 
				"Localization", "Binding", "Protein_modification", "Phosphorylation", "Ubiquitination", "Acetylation"
				, "Deacetylation", "Regulation", "Positive_regulation", "Negative_regulation"};
		
		//
		// Initialize the classifiers
		//

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
		
		Map<String,double[]> word2vec = new HashMap<String,double[]>();
		word2vec = ReadWord2vec.word2vec();

		while (sentenceIter.hasNext()) {

			Sentence sentence = (Sentence) sentenceIter.next();
			Set<Pair> pairsOfSentence = pairsOfArticle.get(sentence.getId());

			// The queue where newly generated events are put
			LinkedBlockingQueue<Event> newEvents = new LinkedBlockingQueue<Event>();

			// protein
			List<Protein> sentenceProteins = JCasUtil.selectCovered(jcas,
					Protein.class, sentence);
			
			if (sentenceProteins.size() <= 0) {
				continue;
			}
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
	
				if(isProtein(token, sentenceProteins)) {
					continue;
				}
				
				token = containsProtein2(token, sentenceProteins);
				if(word2vec.containsKey(token.getLemma())
						|| word2vec.containsKey(token.getLemma().toUpperCase())) {
					Instance instance = new Instance();
					double[] fs = new double[200];
					//double[] fs1 = new double[200];
					//double[] fs2 = new double[200];
					if (word2vec.containsKey(token.getLemma())) {
						fs = word2vec.get(token.getLemma());
					}else {
						fs = word2vec.get(token.getLemma().toUpperCase());
					}
					
					//System.arraycopy(fs1, 0, fs, 0, 200);
					//Token leftToken = token.getLeftToken();
					/*if (leftToken != null) {
						leftToken = containsProtein(leftToken, sentenceProteins);
						if(isProtein(leftToken, sentenceProteins)) {
							String[] text = leftToken.getCoveredText().split("/");
							leftToken.setLemma(text[0].toUpperCase());
						}
						fs2 = word2vec.get(leftToken.getLemma());
						if (fs2 == null){
							fs2 = word2vec.get(leftToken.getLemma().toUpperCase());
						}
						if (fs2 != null){
							System.arraycopy(fs2, 0, fs, 200, 200);
						}
					}*/
					
					instance.setFeaturesNumericWord2vec(fs);
					int prediction = this.predict(instance, 
								"./trainfile/litway.instances.trigger.vector.model.txt");
					if (prediction != 1) {
						Trigger trigger = new Trigger(jcas, token.getBegin(),
								token.getEnd());
						trigger.setEventType(labelDict[prediction - 1]);
						trigger.setId("T".concat(String.valueOf(++proteinNum)));
						triggers.get(sentence.getId()).add(trigger);
					}
				}
			}

			//
			// theme assignment
			//

			// 1. iterate through all proteins

			if (null == events.get(sentence.getId())) {
				events.put(sentence.getId(), new LinkedBlockingQueue<Event>());
			}
			Map<String, Set<Event>> triggerEvents = new HashMap<String, Set<Event>>();
			for (Trigger trigger : triggers.get(sentence.getId())) {
				Set<Event> triggerEvent = new HashSet<Event>();
				if (EventType.isSimpleEvent(trigger.getEventType())) {

					for (Protein protein : sentenceProteins) {

						Instance proteinInstance = themeToInstance(jcas,
								sentence, protein, trigger, pairsOfSentence,
								dependencyExtractor, false);
						if ( proteinInstance != null) {
							double prediction = themeRecogniser.predict(themeDict
									.instanceToNumeric(proteinInstance)
									.getFeaturesNumeric());

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
								triggerEvent.add(event);
							}
						}
					}

				} else if (EventType.isBindingEvent(trigger.getEventType())) {

					List<Protein> themes = new ArrayList<Protein>();

					for (Protein protein : sentenceProteins) {

						Instance proteinInstance = themeToInstance(jcas,
								sentence, protein, trigger, pairsOfSentence,
								dependencyExtractor, false);
						
						if ( proteinInstance != null) {
							double prediction = themeRecogniser.predict(themeDict
									.instanceToNumeric(proteinInstance)
									.getFeaturesNumeric());
	
							if (prediction == themeDict.getLabelNumeric("Theme")) {
								themes.add(protein);
							}
						}

					}
					if (themes.size() > 0) {

						Event event = new Event(jcas);
						event.setTrigger(trigger);
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
												jcas, sentence, event,
												candidateThemes,
												dependencyExtractor));
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
							triggerEvent.add(newBindingEvent);
						}
					}

				} else if (EventType.isComplexEvent(trigger.getEventType())) {

					for (Protein protein : sentenceProteins) {

						Instance proteinInstance = themeDict
								.instanceToNumeric(themeToInstance(jcas,
										sentence, protein, trigger,
										pairsOfSentence, dependencyExtractor,
										false));
						if ( proteinInstance != null) {
							double prediction = themeRecogniser
									.predict(proteinInstance.getFeaturesNumeric());
	
							if (prediction == themeDict.getLabelNumeric("Theme")) {
	
								Event event = new Event(jcas);
								event.setId(String.valueOf(eventIndex++));
								event.setTrigger(trigger);
								StringArray themes = new StringArray(jcas, 1);
								themes.set(0, protein.getId());
								event.setThemes(themes);
								events.get(sentence.getId()).add(event);
								newEvents.add(event);
								triggerEvent.add(event);
							}
						}
					}
				}
				triggerEvents.put(trigger.getId(), triggerEvent);
			}
			LinkedBlockingQueue<Event> newEvents2 = new LinkedBlockingQueue<Event>();
			// 2. check all discovered events whether they can be themes
			for (Event themeEvent : newEvents) {
				// System.out.println("(" +
				// themeEvent.getTrigger().getEventType()
				// + ":" + themeEvent.getTrigger().getCoveredText() + ")"
				// + themeEvent.getTrigger().getBegin() + ":"
				// + themeEvent.getTrigger().getEnd());
				Set<Event> triggerEvent = triggerEvents.get(themeEvent.getTrigger().getId());
				loop : for (Trigger trigger : triggers.get(sentence.getId())) {
					
					Set<Event> triggerEvent2 = triggerEvents.get(trigger.getId());
					if (EventType.isRegulatoryEvent(trigger.getEventType())) {

						if (themeEvent.getTrigger().getBegin() == trigger
								.getBegin()) {
							continue;
						}
						
						if (EventType.isRegulatoryEvent(themeEvent.getTrigger().getEventType())) {
							for (Event event : triggerEvent) {
								if (event.getThemes(0).contains("E")) {
									//System.out.println(event.getThemes(0));
									for (Event event2 : triggerEvent2) {
										//System.out.println("E".concat(event2.getId()));
										if (event.getThemes(0).equalsIgnoreCase("E".concat(event2.getId()))) {
											continue loop;
										}
									}
								}
							}
						}
						
						Instance triggerTokenInstance = themeToInstance(jcas,
								sentence, themeEvent.getTrigger(), trigger,
								pairsOfSentence, dependencyExtractor, false);

						double prediction = themeRecogniser.predict(themeDict
								.instanceToNumeric(triggerTokenInstance)
								.getFeaturesNumeric());

						if (prediction == themeDict.getLabelNumeric("Theme")) {

							Event event = new Event(jcas);
							event.setId(String.valueOf(eventIndex++));
							event.setTrigger(trigger);
							StringArray themes = new StringArray(jcas, 1);
							themes.set(0, "E".concat(themeEvent.getId()));
							event.setThemes(themes);
							events.get(sentence.getId()).add(event);
							newEvents2.add(event);
							triggerEvent2.add(event);
							triggerEvents.put(trigger.getId(), triggerEvent2);
						}
					}
				}
				//System.out.println(resultToString(triggers, events));
			}
			
			for (Event themeEvent : newEvents2) {
				// System.out.println("(" +
				// themeEvent.getTrigger().getEventType()
				// + ":" + themeEvent.getTrigger().getCoveredText() + ")"
				// + themeEvent.getTrigger().getBegin() + ":"
				// + themeEvent.getTrigger().getEnd());
				Set<Event> triggerEvent = triggerEvents.get(themeEvent.getTrigger().getId());
				loop : for (Trigger trigger : triggers.get(sentence.getId())) {
					
					Set<Event> triggerEvent2 = triggerEvents.get(trigger.getId());
					if (EventType.isRegulatoryEvent(trigger.getEventType())) {

						if (themeEvent.getTrigger().getBegin() == trigger
								.getBegin()) {
							continue;
						}
						
						if (EventType.isRegulatoryEvent(themeEvent.getTrigger().getEventType())) {
							for (Event event : triggerEvent) {
								if (event.getThemes(0).contains("E")) {
									//System.out.println(event.getThemes(0));
									for (Event event2 : triggerEvent2) {
										//System.out.println("E".concat(event2.getId()));
										if (event.getThemes(0).equalsIgnoreCase("E".concat(event2.getId()))) {
											continue loop;
										}
									}
								}
							}
						}
						
						Instance triggerTokenInstance = themeToInstance(jcas,
								sentence, themeEvent.getTrigger(), trigger,
								pairsOfSentence, dependencyExtractor, false);

						double prediction = themeRecogniser.predict(themeDict
								.instanceToNumeric(triggerTokenInstance)
								.getFeaturesNumeric());

						if (prediction == themeDict.getLabelNumeric("Theme")) {

							Event event = new Event(jcas);
							event.setId(String.valueOf(eventIndex++));
							event.setTrigger(trigger);
							StringArray themes = new StringArray(jcas, 1);
							themes.set(0, "E".concat(themeEvent.getId()));
							event.setThemes(themes);
							events.get(sentence.getId()).add(event);
							//newEvents2.add(event);
							triggerEvent2.add(event);
							triggerEvents.put(trigger.getId(), triggerEvent2);
						}
					}
				}
				//System.out.println(resultToString(triggers, events));
			}
			//
			// cause
			//
			Map<String,Set<String>> equal = new HashMap<String,Set<String>>();
			for (Event event : newEvents) {
				Set<Event> triggerEvent = triggerEvents.get(event.getTrigger().getId());
				if (!EventType
						.isComplexEvent(event.getTrigger().getEventType())) {
					continue;
				}

				// protein
				loop : for (Protein protein : sentenceProteins) {
					for (Event event2 : triggerEvent) {
						if (event2.getThemes(0).equalsIgnoreCase(protein.getId())) {
							continue loop;
						}
					}

					Instance proteinInstance = causeToInstance(jcas, sentence,
							protein, event.getTrigger(), pairsOfSentence,
							dependencyExtractor, false,
							getThemeToken(jcas, event, sentence));
					
					if ( proteinInstance != null) {
						double prediction = causeRecogniser.predict(causeDict
								.instanceToNumeric(proteinInstance)
								.getFeaturesNumeric());
	
						if (prediction == causeDict.getLabelNumeric(String
								.valueOf("Cause"))) {
							if (event.getCause() != null) {
								Event event2 = new Event(jcas);
								event2.setId(String.valueOf(eventIndex++));
								event2.setTrigger(event.getTrigger());
								event2.setThemes(event.getThemes());
								event2.setCause(protein.getId());
								events.get(sentence.getId()).add(event2);
								//newEvents.add(event);
								triggerEvent.add(event2);
								triggerEvents.put(event.getTrigger().getId(), triggerEvent);
								Set<String> ss = new HashSet<String>();
								if (equal.containsKey("E".concat(event.getId()))) {
									ss = equal.get("E".concat(event.getId()));
								}	
								ss.add("E".concat(event2.getId()));
								equal.put("E".concat(event.getId()), ss);
							}else {
								event.setCause(protein.getId());
							}
						}
					}
				}


				// event
				loop : for (Event causeEvent : newEvents) {
					Set<Event> triggerEvent2 = triggerEvents.get(causeEvent.getTrigger().getId());
					if (causeEvent.getTrigger().getBegin() == event
							.getTrigger().getBegin()) {
						continue;
					}
					
					for (Event event2 : triggerEvent) {
						if (event2.getThemes(0).equalsIgnoreCase("E".concat(causeEvent.getId()))) {
							continue loop;
						}
					}
					
					if (EventType.isRegulatoryEvent(causeEvent.getTrigger().getEventType())) {
						for (Event event22 : triggerEvent) {
							for (Event event2 : triggerEvent2) {
								if (event22.getThemes(0).equalsIgnoreCase("E".concat(event2.getId()))
										|| event2.getThemes(0).equalsIgnoreCase("E".concat(event22.getId()))) {
									continue loop;
								}
							}
						}
					}
					
					if (EventType.isComplexEvent(causeEvent.getTrigger().getEventType())) {
						for (Event event2 : triggerEvent2) {
							if (event2.getCause() != null && event2.getCause().contains("E")) {
								for (Event event22 : triggerEvent) {
									if (event2.getCause().equalsIgnoreCase("E".concat(event22.getId()))) {
										continue loop;
									}
								}
							}
						}
					}
					
					Token themeToken = getThemeToken(jcas, event, sentence);
					Instance causeEventInstance = causeToInstance(jcas,
							sentence, causeEvent.getTrigger(),
							event.getTrigger(), pairsOfSentence,
							dependencyExtractor, false, themeToken);
					if ( causeEventInstance != null) {
						double prediction = causeRecogniser.predict(causeDict
								.instanceToNumeric(causeEventInstance)
								.getFeaturesNumeric());
						if (prediction == causeDict.getLabelNumeric(String
								.valueOf("Cause"))) {
							if (event.getCause() != null) {
								Event event2 = new Event(jcas);
								event2.setId(String.valueOf(eventIndex++));
								event2.setTrigger(event.getTrigger());
								event2.setThemes(event.getThemes());
								event2.setCause("E".concat(causeEvent.getId()));
								events.get(sentence.getId()).add(event2);
								//newEvents.add(event);
								triggerEvent.add(event2);
								triggerEvents.put(event.getTrigger().getId(), triggerEvent);
								Set<String> ss = new HashSet<String>();
								if (equal.containsKey("E".concat(event.getId()))) {
									ss = equal.get("E".concat(event.getId()));
								}	
								ss.add("E".concat(event2.getId()));
								equal.put("E".concat(event.getId()), ss);
							}else {
								event.setCause("E".concat(causeEvent.getId()));
							}
						}
					}
				}
				//System.out.println(resultToString(triggers, events));
			}
			
/*			for (Trigger trigger : triggers.get(sentence.getId())) {
				if (!EventType.isComplexEvent(trigger.getEventType())) {
					continue;
				}
				Set<Event> triggerEvent = triggerEvents.get(trigger.getId());
				for (Event event : triggerEvent) {
					if (equal.containsKey(event.getCause())) {
						Set<String> ss = equal.get(event.getCause());
						for (String s : ss) {
							Event event2 = new Event(jcas);
							event2.setId(String.valueOf(eventIndex++));
							event2.setTrigger(event.getTrigger());
							event2.setThemes(event.getThemes());
							event2.setCause(s);
							events.get(sentence.getId()).add(event2);
						}	
					}
				}
				if (!EventType.isRegulatoryEvent(trigger.getEventType())) {
					continue;
				}
				
				for (Event event : triggerEvent) {
					if (equal.containsKey(event.getThemes(0))) {
						Set<String> ss = equal.get(event.getThemes(0));
						for (String s : ss) {
							Event event2 = new Event(jcas);
							event2.setId(String.valueOf(eventIndex++));
							event2.setTrigger(event.getTrigger());
							StringArray themes = new StringArray(jcas, 1);
							themes.set(0, s);
							event2.setThemes(themes);
							event2.setCause(event.getCause());
							events.get(sentence.getId()).add(event2);
						}	
					}
				}
			}			*/
		}
		//System.out.println(resultToString(triggers, events));
		return resultToString(triggers, events);
		//return perform;
	}

	private String resultToString(Map<Integer, List<Trigger>> triggers,
			Map<Integer, LinkedBlockingQueue<Event>> events) {

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

		for (LinkedBlockingQueue<Event> sentenceEvents : events.values()) {
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

	public static void main(String[] args)  {

		EventExtractor ee = new EventExtractor();
		ee.setTaeDescriptor("/desc/TestSetAnnotator.xml");
		//ee.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
		File inputFile = new File(args[0]);
		//File inputFile = new File("/run/media/songrq/User/songrq/数据/BioNLP13/b");
		//File inputFile = new File("/run/media/songrq/User/songrq/数据/BioNLP13/BioNLP-ST-2013_GE_devel_data");

		try {
			ee.extract(inputFile);
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	
	private int predict(Instance instance,String modelFile) throws IOException
	{
		//int correct = 0, tp = 0, tn = 0, fp = 0, fn = 0;
		//int total = 0;
		//double error = 0;
		//float	p = 0, r = 0, f = 0;
		//double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
		svm_model model = svm.svm_load_model(modelFile);
		//int svm_type=svm.svm_get_svm_type(model);
		//int nr_class=svm.svm_get_nr_class(model);
		//double[] prob_estimates=null;

/*		if(predict_probability == 1)
		{
			if(svm_type == svm_parameter.EPSILON_SVR ||
			   svm_type == svm_parameter.NU_SVR)
			{
				svm_predict.info("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="+svm.svm_get_svr_probability(model)+"\n");
			}
			else
			{
				int[] labels=new int[nr_class];
				svm.svm_get_labels(model,labels);
				prob_estimates = new double[nr_class];
				output.writeBytes("labels");
				for(int j=0;j<nr_class;j++)
					output.writeBytes(" "+labels[j]);
				output.writeBytes("\n");
			}
		}
		while(true)
		{
			String line = input.readLine();
			if(line == null) break;

			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

			double target = atof(st.nextToken());
			int m = st.countTokens()/2;
			svm_node[] x = new svm_node[m];
			for(int j=0;j<m;j++)
			{
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}*/
			
			double[] ss = instance.getFeaturesNumericWord2vec();
			//double target = ss[0];
			svm_node[] x = new svm_node[ss.length];
			for(int j=0;j<ss.length;j++)
			{
				x[j] = new svm_node();
				x[j].index = j+1;
				x[j].value = ss[j];
			}

			int v;
/*			if (predict_probability==1 && (svm_type==svm_parameter.C_SVC || svm_type==svm_parameter.NU_SVC))
			{
				v = svm.svm_predict_probability(model,x,prob_estimates);
				output.writeBytes(v+" ");
				for(int j=0;j<nr_class;j++)
					output.writeBytes(prob_estimates[j]+" ");
				output.writeBytes("\n");
			}
			else
			{
				v = svm.svm_predict(model,x);
				output.writeBytes(v+"\n");
			}*/

			v = (int)svm.svm_predict(model,x);	
			return v;
/*			if(v == target) {
				++correct;
			}
			error += (v-target)*(v-target);
			sumv += v;
			sumy += target;
			sumvv += v*v;
			sumyy += target*target;
			sumvy += v*target;
			++total;
			if(target == 1) {
				if(v == target){
					tn++;
				}else {
					fp++;
				}
			}else {
				if(v == target){
					tp++;
				}else {
					fn++;
				}
			}
		}
		p = (float) tp / (tp + fp);
		r = (float) tp / (tp + fn);
		f = (float) 2 * p * r / (p + r); 
		System.out.println();
		System.out.println(new Accurary(correct, total));
		System.out.println("tp: " + tp + "   fp: " + fp + "   fn: " + fn);
		System.out.println("p: " + p + "   r: " + r + "   f: " + f);
		svm_predict.info("Accuracy = "+(double)correct/total*100+
				 "% ("+correct+"/"+total+") (classification)\n");*/
		
/*		if(svm_type == svm_parameter.EPSILON_SVR ||
		   svm_type == svm_parameter.NU_SVR)
		{
			svm_predict.info("Mean squared error = "+error/total+" (regression)\n");
			svm_predict.info("Squared correlation coefficient = "+
				 ((total*sumvy-sumv*sumy)*(total*sumvy-sumv*sumy))/
				 ((total*sumvv-sumv*sumv)*(total*sumyy-sumy*sumy))+
				 " (regression)\n");
		}else {
			svm_predict.info("Accuracy = "+(double)correct/total*100+
				 "% ("+correct+"/"+total+") (classification)\n");
		}
*/
	}	
	
	private boolean isProtein(Token token, List<Protein> proteinsOfSentence) {
		for (Protein protein : proteinsOfSentence) {
			if ((token.getBegin() >= protein.getBegin() && token.getEnd() <= protein
					.getEnd()) || (token.getBegin() <= protein.getBegin() && token.getEnd() >= protein
							.getEnd() && token.getCoveredText().contains("/"))) {
				return true;
			}
		}
		return false;
	}

	   private Token containsProtein2(Token token, List<Protein> proteinsOfSentence) {
	        for (Protein protein : proteinsOfSentence) {
	            if (((protein.getBegin() >= token.getBegin() && protein
	                            .getEnd() < token.getEnd()) ||
	                            (protein.getBegin() > token.getBegin() && protein
	                            .getEnd() <= token.getEnd()))
	                            && token.getCoveredText().indexOf("/") == -1) {
	                //String s = "";
	                if (protein.getBegin() > token.getBegin()) {
	                    int i = protein.getBegin();
	                    int j = protein.getEnd();
	                    protein.setBegin(token.getBegin());
	                    protein.setEnd(i);
	                    String text = protein.getCoveredText();
	                    if (text.charAt(text.length() - 1) == '-') {
	                    	//protein.setEnd(protein.getEnd() - 1);
	                    }
	                   // s = protein.getCoveredText().toLowerCase() + " ";
	                    token.setLemma(protein.getCoveredText().toLowerCase());
	                    //token.setSubStem("------");
	                    protein.setBegin(i);
	                    protein.setEnd(j);
	                }
	                //token.setSubLemma(s + protein.getCoveredText().toUpperCase());
	                //token.setSubLemma(s + "PROTEIN");
	                if (protein.getEnd() < token.getEnd()) {
	                	int i = token.getBegin();
	                    token.setBegin(protein.getEnd());
	                    if (token.getCoveredText().charAt(0) == '-') {
	                        token.setBegin(token.getBegin() + 1);
	                    }
	                    String text = token.getCoveredText();
	                    token.setBegin(i);
	                    String lemma = BioLemmatizerUtil.lemmatizeWord(text.toLowerCase(),
	                            token.getPos());
	                    /*Stemmer stem = new Stemmer();
	                    stem.add(text.toCharArray(), text.length());
	                    stem.stem();*/
	                    token.setLemma(lemma);
	                    //token.setSubStem("----");
	                }
	                return token;
	            }
	        }
	        return token;
	    }
	public static boolean isSimpleEvent(String eventType) {

		if (eventType.equalsIgnoreCase("Gene_expression")
				|| eventType.equalsIgnoreCase("Transcription")
				|| eventType.equalsIgnoreCase("Protein_catabolism")
				|| eventType.equalsIgnoreCase("Localization")) {
			return true;
		}

		return false;
	}

	public static boolean isRegulatoryEvent(String eventType) {

		if (eventType.equalsIgnoreCase("Regulation")
				|| eventType.equalsIgnoreCase("Positive_regulation")
				|| eventType.equalsIgnoreCase("Negative_regulation")) {

			return true;
		}

		return false;
	}

	public static boolean isComplexEvent(String eventType) {

		if (eventType.equalsIgnoreCase("Protein_modification")
				|| eventType.equalsIgnoreCase("Phosphorylation")
				|| eventType.equalsIgnoreCase("Ubiquitination")
				|| eventType.equalsIgnoreCase("Acetylation")
				|| eventType.equalsIgnoreCase("Deacetylation")
				|| eventType.equalsIgnoreCase("Regulation")
				|| eventType.equalsIgnoreCase("Positive_regulation")
				|| eventType.equalsIgnoreCase("Negative_regulation")) {

			return true;
		}

		return false;
	}

}
