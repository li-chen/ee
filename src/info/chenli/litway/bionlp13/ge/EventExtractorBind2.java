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
import info.chenli.litway.util.UimaUtil;
import info.chenli.litway.util.StanfordDependencyReader.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;


public class EventExtractorBind2 extends TokenInstances {

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

		boolean test = true;
		
		File word2vecFile = new File("./word2vec/word2vec100");
		//File word2vecFile = new File("/home/songrq/word2vec/data/word2vec100");
		
		Map<String,double[]> word2vec = ReadWord2vec.word2vec(word2vecFile); 

		Map<Integer, List<Trigger>> triggers = new TreeMap<Integer, List<Trigger>>();
		Map<Integer, List<Event>> events = new TreeMap<Integer, List<Event>>();
		// Initialize the file
		JCas jcas = this.processSingleFile(file);
		int proteinNum = 0;
		FSIterator<Annotation> proteinIter = jcas.getAnnotationIndex(
				Protein.type).iterator();
		while(proteinIter.hasNext()) {
			Protein protein = (Protein) proteinIter.next();
			String s = protein.getId().replace('T', '0');
			proteinNum = proteinNum < Integer.valueOf(s) ? Integer.valueOf(s) : proteinNum;
		}
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


		
		//
		// Initialize the classifiers
		//
		
		// trigger
		TriggerRecogniser triggerRecogniser = new TriggerRecogniser();
		InstanceDictionary triggerDict = new InstanceDictionary();
		triggerRecogniser.loadModel(new File("./model/triggers.".concat(
				classifierName).concat(".model")));
		//String triggerModel = "./model/triggers.model";
		triggerDict.loadDictionary(new File("./model/triggers.".concat(
				classifierName).concat(".dict")));
		if (test) {
			triggerRecogniser.loadModel(new File("./model/triggers.train.devel.".concat(
					classifierName).concat(".model")));
			triggerDict.loadDictionary(new File("./model/triggers.train.devel.".concat(
					classifierName).concat(".dict")));

		}else {
			triggerRecogniser.loadModel(new File("./model/triggers.".concat(
					classifierName).concat(".model")));
			triggerDict.loadDictionary(new File("./model/triggers.".concat(
					classifierName).concat(".dict")));
		}
		// argument
		ArgumentRecogniser argumentRecogniser = new ArgumentRecogniser();
		InstanceDictionary argumentDict = new InstanceDictionary();
		if (test) {
			argumentRecogniser.loadModel(new File("./model/arguments.train.devel.".concat(
					classifierName).concat(".model")));
			argumentDict.loadDictionary(new File("./model/arguments.train.devel.".concat(
					classifierName).concat(".dict")));
		}else {
			argumentRecogniser.loadModel(new File("./model/arguments.".concat(
					classifierName).concat(".model")));
			argumentDict.loadDictionary(new File("./model/arguments.".concat(
					classifierName).concat(".dict")));
		}
		/*ArgumentRecogniser reguArgumentRecogniser = new ArgumentRecogniser();
		reguArgumentRecogniser.loadModel(new File("./model/reguArguments.".concat(
				classifierName).concat(".model")));
		InstanceDictionary reguArgumentDict = new InstanceDictionary();
		reguArgumentDict.loadDictionary(new File("./model/reguArguments.".concat(
				classifierName).concat(".dict")));*/
		/*TriggerArgumentRecogniser triggerArgumentRecogniser = new TriggerArgumentRecogniser();
		triggerArgumentRecogniser.loadModel(new File("./model/triggerArguments.".concat(
				classifierName).concat(".model")));
		InstanceDictionary triggerArgumentDict = new InstanceDictionary();
		triggerArgumentDict.loadDictionary(new File("./model/triggerArguments.".concat(
				classifierName).concat(".dict")));*/
		
		// binding
		BindingRecogniser bindingRecogniser = new BindingRecogniser();
		InstanceDictionary bindingDict = new InstanceDictionary();
		if (test) {
			bindingRecogniser.loadModel(new File("./model/bindings.train.devel.".concat(
					classifierName).concat(".model")));
			bindingDict.loadDictionary(new File("./model/bindings.train.devel.".concat(
					classifierName).concat(".dict")));
		}else {
			bindingRecogniser.loadModel(new File("./model/bindings.".concat(
					classifierName).concat(".model")));
			bindingDict.loadDictionary(new File("./model/bindings.".concat(
					classifierName).concat(".dict")));
		}
		
		
		// Initialize the iterator and counter
		FSIterator<Annotation> sentenceIter = jcas.getAnnotationIndex(
				Sentence.type).iterator();
		int eventIndex = 1;

		while (sentenceIter.hasNext()) {

			Sentence sentence = (Sentence) sentenceIter.next();
			Set<Pair> pairsOfSentence = pairsOfArticle.get(sentence.getId());

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
			
			Map<String, Trigger> triggerId = new HashMap<String, Trigger>();

			for (Token token : tokens) {
				if(isProtein(token, sentenceProteins)) {
					continue;
				}

				int tokenBegin = token.getBegin();
				int tokenEnd = token.getEnd();
				token = containsProtein(token, sentenceProteins);
				/*if (shouldDelete(jcas, token, sentenceProteins)) {
					continue;
				}*/
				
				Instance tokenInstance = tokenToInstance(jcas, token, null,
						tokens, sentenceProteins, pairsOfSentence,
						dependencyExtractor, word2vec);
				if (tokenInstance != null) {
					tokenInstance = triggerDict.instanceToNumeric(tokenInstance);
					int prediction = triggerRecogniser.predict(tokenInstance);
					//int[] featureSparseVector = tokenInstance.getFeaturesNumeric();

					//int prediction = this.predict2(featureSparseVector, tokenInstance, triggerModel);
	
					/*String temp = shouldChange(jcas, token, sentenceProteins);
					if (!temp.equals("Non_trigger")) {
						prediction = triggerDict.getLabelNumeric(temp);
					}*/
					if (prediction != triggerDict.getLabelNumeric(String
							.valueOf(EventType.Non_trigger))) {
	
						Trigger trigger = new Trigger(jcas, token.getBegin(),
								token.getEnd());
						trigger.setEventType(triggerDict.getLabelString(prediction));
						trigger.setId("T".concat(String.valueOf(++proteinNum)));
						triggers.get(sentence.getId()).add(trigger);
						triggerId.put(trigger.getId(), trigger);
					}
				}
					
				token.setBegin(tokenBegin);
				token.setEnd(tokenEnd);
			}

			//
			// argument assignment
			//
			
			// 1. iterate through all proteins
			if (null == events.get(sentence.getId())) {
				events.put(sentence.getId(), new LinkedList<Event>());
			}			
			Set<String> sameToken = new HashSet<String>();
			
			Map<String, List<Argument>> eventArg = new HashMap<String, List<Argument>>();
			Map<String, Set<Event>> triggerEvents = new HashMap<String, Set<Event>>();
			Map<String, Set<String>> triggerCauses = new HashMap<String, Set<String>>();
			
			Set<String> andProtein = getAndProtein(jcas, sentenceProteins, dependencyExtractor);
			
			for (Trigger trigger : triggers.get(sentence.getId())) {
				for (Protein protein : sentenceProteins) {
					Token triggerToken = getTriggerToken(jcas, trigger);
					Token proteinToken = getToken(jcas, protein);
					boolean areSameTokens = (proteinToken.getId() == triggerToken.getId());
					if (areSameTokens) {
						sameToken.add(protein.getId());
					}
				}
			}
			for (Trigger trigger : triggers.get(sentence.getId())) {
				Set<Event> triggerEvent = new HashSet<Event>();
				Set<String> triggerCause = new HashSet<String>();
				
				if (EventType.isSimpleEvent(trigger.getEventType())) {
					for (Protein protein : sentenceProteins) {
						Token triggerToken = getTriggerToken(jcas, trigger);
						Token proteinToken = getToken(jcas, protein);
						boolean areSameTokens = (proteinToken.getId() == triggerToken.getId());
						Instance proteinInstance = argumentToInstance(jcas,
								sentence, protein, trigger, pairsOfSentence,
								dependencyExtractor, false, false, Stage.THEME);
						if ( proteinInstance != null) {
							double prediction = argumentRecogniser.predict(argumentDict
									.instanceToNumeric(proteinInstance)
									.getFeaturesNumeric(), proteinInstance);
							if (areSameTokens) {
								prediction = argumentDict.getLabelNumeric("Theme");
							}
							if (prediction == argumentDict.getLabelNumeric("Theme")) {	
								Event event = new Event(jcas);
								event.setId(String.valueOf(eventIndex++));
								event.setTrigger(trigger);
								StringArray themes = new StringArray(jcas, 1);
								themes.set(0, protein.getId());
								event.setThemes(themes);
								events.get(sentence.getId()).add(event);
								triggerEvent.add(event);
								
								Argument arg = new Argument();
								arg.setId(protein.getId());
								arg.setRelation("Theme");
								List<Argument> args = new LinkedList<Argument>();
								args.add(arg);
								eventArg.put("E".concat(event.getId()), args);
							}
						}
					}
				} else if (EventType.isBindingEvent(trigger.getEventType())) {
					List<List<Protein>> predictedThemesComb = new ArrayList<List<Protein>>();
					Set<String> farProtein = getFarProtein( jcas, trigger, sentenceProteins, dependencyExtractor); 
					Set<String> notTheme = getNotProtein( jcas, trigger, sentenceProteins, dependencyExtractor);
					for (Protein protein : sentenceProteins) {
						Token triggerToken = getTriggerToken(jcas, trigger);
						Token proteinToken = getToken(jcas, protein);
						boolean areSameTokens = (proteinToken.getId() == triggerToken.getId());

						Instance proteinInstance = argumentToInstance(jcas,
								sentence, protein, trigger, pairsOfSentence,
								dependencyExtractor, false, false, Stage.THEME);
						if ( proteinInstance != null) {
							double prediction = argumentRecogniser.predict(argumentDict
									.instanceToNumeric(proteinInstance)
									.getFeaturesNumeric(), proteinInstance);
							if (areSameTokens) {
								prediction = argumentDict.getLabelNumeric("Theme");
							}
							if (prediction != argumentDict.getLabelNumeric("Theme")) {
								notTheme.add(protein.getId());
							}
						}
					}
					List<Protein> proteins = new LinkedList<Protein>();
					for (Protein protein : sentenceProteins) {
						if (!notTheme.contains(protein.getId()) && !farProtein.contains(protein.getId())) {
							proteins.add(protein);
						}
					}
					if (proteins.size() == 1) {
						/*Instance bindingInstance = bindingEventToInstance(jcas,
								sentence, trigger, proteins, dependencyExtractor, false);
						double prediction = bindingRecogniser.predict(bindingDict
								.instanceToNumeric(bindingInstance)
								.getFeaturesNumeric());
						Token triggerToken = getTriggerToken(jcas, trigger);
						Token proteinToken = getToken(jcas, proteins.get(0));
						boolean areSameTokens = (proteinToken.getId() == triggerToken.getId());
						if (areSameTokens) {
							prediction = bindingDict.getLabelNumeric("Binding");
						}
						if (prediction == bindingDict
								.getLabelNumeric("Binding")) {*/
							predictedThemesComb.add(proteins);
						//}
					} else if (proteins.size() > 1) {
						Combinations<Protein> combs = new Combinations<Protein>(
								proteins);
						loop : for (List<Protein> themes : combs.getCombinations()) {
							boolean truepositive = false;
							if (themes.size() != 2) {
								continue;
							}
							/*for (Protein p : themes) {
								if (farProtein.contains(p.getId())) {
									continue loop;
								}
								if (notTheme.contains(p.getId())) {
									continue loop;
								}
							}*/
							int num = 0;
							for (Protein p : themes) {
								if (andProtein.contains(p.getId())) {
									num++;
									if (num > 1) {
										List<Protein> theme = new LinkedList<Protein>();
										theme.add(themes.get(0));
										/*Instance bindingInstance = bindingEventToInstance(jcas,
												sentence, trigger, theme, dependencyExtractor, truepositive);
										double prediction = bindingRecogniser.predict(bindingDict
												.instanceToNumeric(bindingInstance)
												.getFeaturesNumeric());
										if (prediction == bindingDict
												.getLabelNumeric("Binding")) {*/
											predictedThemesComb.add(theme);
										//}
										
										theme.remove(0);
										theme.add(themes.get(1));
										/*bindingInstance = bindingEventToInstance(jcas,
												sentence, trigger, theme, dependencyExtractor, truepositive);
										prediction = bindingRecogniser.predict(bindingDict
												.instanceToNumeric(bindingInstance)
												.getFeaturesNumeric());
										if (prediction == bindingDict
												.getLabelNumeric("Binding")) {*/
											predictedThemesComb.add(theme);
										//}
										
										continue loop;
									}
								}
							}
							for(Protein p : themes) {
								for(Protein p2 : themes) {
									if (p.getId().equalsIgnoreCase(p2.getId())) {
										continue;
									}
									if (p.getCoveredText().equalsIgnoreCase(p2.getCoveredText())
											|| p.getCoveredText().contains(p2.getCoveredText())
											|| p2.getCoveredText().contains(p.getCoveredText())) {
										List<Protein> theme = new LinkedList<Protein>();
										theme.add(themes.get(0));
										/*Instance bindingInstance = bindingEventToInstance(jcas,
												sentence, trigger, theme, dependencyExtractor, truepositive);
										double prediction = bindingRecogniser.predict(bindingDict
												.instanceToNumeric(bindingInstance)
												.getFeaturesNumeric());
										if (prediction == bindingDict
												.getLabelNumeric("Binding")) {*/
											predictedThemesComb.add(theme);
										//}
										
										theme.remove(0);
										theme.add(themes.get(1));
										/*bindingInstance = bindingEventToInstance(jcas,
												sentence, trigger, theme, dependencyExtractor, truepositive);
										prediction = bindingRecogniser.predict(bindingDict
												.instanceToNumeric(bindingInstance)
												.getFeaturesNumeric());
										if (prediction == bindingDict
												.getLabelNumeric("Binding")) {*/
											predictedThemesComb.add(theme);
										//}
										continue loop;
									}
								}
							}
							Instance instance = bindingEventToInstance(jcas,
									sentence, trigger, themes, dependencyExtractor, truepositive);
							double prediction = bindingRecogniser.predict(bindingDict
									.instanceToNumeric(instance)
									.getFeaturesNumeric(), instance);
							if (prediction == bindingDict
									.getLabelNumeric("Binding")) {
								List<Protein> theme0 = new LinkedList<Protein>();
								theme0.add(themes.get(0));
								List<Protein> theme1 = new LinkedList<Protein>();
								theme1.add(themes.get(1));
								Instance bindInstance0 = bindingEventToInstance(jcas,
										sentence, trigger, theme0, dependencyExtractor, truepositive);
								Instance bindInstance1 = bindingEventToInstance(jcas,
										sentence, trigger, theme1, dependencyExtractor, truepositive);

								double prediction0 = bindingRecogniser.predict(bindingDict
										.instanceToNumeric(bindInstance0)
										.getFeaturesNumeric(), bindInstance0);
								double prediction1 = bindingRecogniser.predict(bindingDict
										.instanceToNumeric(bindInstance1)
										.getFeaturesNumeric(), bindInstance1);
								double predictionValue1 = bindingRecogniser.predict_values(bindingDict
											.instanceToNumeric(bindingEventToInstance(jcas,
													sentence, trigger, theme1, dependencyExtractor, truepositive))
											.getFeaturesNumeric());
								double predictionValue0 = bindingRecogniser.predict_values(bindingDict
											.instanceToNumeric(bindingEventToInstance(jcas,
													sentence, trigger, theme0, dependencyExtractor, truepositive))
											.getFeaturesNumeric());
								
								if (prediction0 == bindingDict.getLabelNumeric("Binding")
										&& prediction1 == bindingDict.getLabelNumeric("Binding")) {
									prediction = bindingRecogniser.predict_values(bindingDict
											.instanceToNumeric(bindingEventToInstance(jcas,
													sentence, trigger, themes, dependencyExtractor, truepositive))
											.getFeaturesNumeric());
									if (predictionValue1 > prediction && predictionValue0 > prediction) {
										predictedThemesComb.add(theme0);
										predictedThemesComb.add(theme1);
									} /*else if (prediction0 < prediction && prediction1 < prediction) {
										predictedThemesComb.add(themes);
									}*/ else {
										predictedThemesComb.add(themes);
									}
								} else {
									predictedThemesComb.add(themes);
								}
							} else {
								List<Protein> theme = new LinkedList<Protein>();
								theme.add(themes.get(0));
								/*Instance bindingInstance = bindingEventToInstance(jcas,
										sentence, trigger, theme, dependencyExtractor, truepositive);
								double prediction0 = bindingRecogniser.predict(bindingDict
										.instanceToNumeric(bindingInstance)
										.getFeaturesNumeric());
								if (prediction0 == bindingDict
										.getLabelNumeric("Binding")) {*/
									predictedThemesComb.add(theme);
								//}
								
								theme.remove(0);
								theme.add(themes.get(1));
								/*bindingInstance = bindingEventToInstance(jcas,
										sentence, trigger, theme, dependencyExtractor, truepositive);
								prediction0 = bindingRecogniser.predict(bindingDict
										.instanceToNumeric(bindingInstance)
										.getFeaturesNumeric());
								if (prediction0 == bindingDict
										.getLabelNumeric("Binding")) {*/
									predictedThemesComb.add(theme);
								//}
							}
						}
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
						if (checkedThemesComb.size() > andProtein.size() + 2
								&& predictedThemes.size() == 2
								&& checkedThemesComb.size() >= 10) {
							break;
						}
						Event newBindingEvent = new Event(jcas);
						newBindingEvent.setTrigger(trigger);
						newBindingEvent.setId(String.valueOf(eventIndex++));
						StringArray eventThemes = new StringArray(jcas,
								predictedThemes.size());
						List<Argument> args = new LinkedList<Argument>();
						for (Protein theme : predictedThemes) {
							eventThemes.set(predictedThemes.indexOf(theme),
									theme.getId());
							Argument arg = new Argument();
							arg.setId(theme.getId());
							arg.setRelation("Theme");
							args.add(arg);
						}
						eventArg.put("E".concat(newBindingEvent.getId()), args);
						
						newBindingEvent.setThemes(eventThemes);
						events.get(sentence.getId()).add(newBindingEvent);
						triggerEvent.add(newBindingEvent);
					}
				} else if (EventType.isComplexEvent(trigger.getEventType())
						&& !EventType.isRegulatoryEvent(trigger.getEventType())) {

					for (Protein protein : sentenceProteins) {
						Token triggerToken = getTriggerToken(jcas, trigger);
						Token proteinToken = getToken(jcas, protein);
						boolean areSameTokens = (proteinToken.getId() == triggerToken.getId());

						Instance proteinInstance = argumentDict
								.instanceToNumeric(argumentToInstance(jcas,
										sentence, protein, trigger,
										pairsOfSentence, dependencyExtractor,
										false, false, Stage.THEME));
						if ( proteinInstance != null) {
							double prediction = argumentRecogniser
									.predict(proteinInstance.getFeaturesNumeric(), proteinInstance);
							if (areSameTokens) {
								prediction = argumentDict.getLabelNumeric("Theme");
							}
							if (prediction == argumentDict.getLabelNumeric("Theme")) {
	
								Event event = new Event(jcas);
								event.setId(String.valueOf(eventIndex++));
								event.setTrigger(trigger);
								StringArray themes = new StringArray(jcas, 1);
								themes.set(0, protein.getId());
								event.setThemes(themes);
								events.get(sentence.getId()).add(event);
								triggerEvent.add(event);
								
								Argument arg = new Argument();
								arg.setId(protein.getId());
								arg.setRelation("Theme");
								List<Argument> args = new LinkedList<Argument>();
								args.add(arg);
								eventArg.put("E".concat(event.getId()), args);
							}else if(prediction == argumentDict.getLabelNumeric("Cause")) {
								if (sameToken.contains(protein.getId())) {
									continue;
								}
								triggerCause.add(protein.getId());
							}
						}
					}
				} else if (EventType.isRegulatoryEvent(trigger.getEventType())) {

					for (Protein protein : sentenceProteins) {
						Token triggerToken = getTriggerToken(jcas, trigger);
						Token proteinToken = getToken(jcas, protein);
						boolean areSameTokens = (proteinToken.getId() == triggerToken.getId());
						if (!areSameTokens && sameToken.contains(protein.getId())) {
							continue;
						}

						Instance proteinInstance = argumentDict
								.instanceToNumeric(argumentToInstance(jcas,
										sentence, protein, trigger,
										pairsOfSentence, dependencyExtractor,
										false, false, Stage.THEME));
						if ( proteinInstance != null) {
							double prediction = argumentRecogniser
									.predict(proteinInstance.getFeaturesNumeric(), proteinInstance);

							if (prediction == argumentDict.getLabelNumeric("Theme")) {
								Event event = new Event(jcas);
								event.setId(String.valueOf(eventIndex++));
								event.setTrigger(trigger);
								StringArray themes = new StringArray(jcas, 1);
								themes.set(0, protein.getId());
								event.setThemes(themes);
								events.get(sentence.getId()).add(event);
								triggerEvent.add(event);
								
								Argument arg = new Argument();
								arg.setId(protein.getId());
								arg.setRelation("Theme");
								List<Argument> args = new LinkedList<Argument>();
								args.add(arg);
								eventArg.put("E".concat(event.getId()), args);

							}else if(prediction == argumentDict.getLabelNumeric("Cause")) {
								triggerCause.add(protein.getId());
							}
						}
					}
				}
				triggerEvents.put(trigger.getId(), triggerEvent);
				triggerCauses.put(trigger.getId(), triggerCause);
			}
			
			
			// 2. check all discovered events whether they can be arguments
			Map<String, Set<Argument>> arguments = new TreeMap<String, Set<Argument>>();
			for (Trigger argumentTrigger : triggers.get(sentence.getId())) {
				loop : for (Trigger trigger : triggers.get(sentence.getId())) {
					if (!EventType.isComplexEvent(trigger.getEventType())) {
						continue;
					}
					
					if (argumentTrigger.getBegin() == trigger.getBegin()) {
						continue;
					}
					
					Instance triggerTokenInstance = argumentToInstance(jcas,
							sentence, argumentTrigger, trigger,
							pairsOfSentence, dependencyExtractor, false, false, Stage.THEME);
	
					double prediction = argumentRecogniser.predict(argumentDict
							.instanceToNumeric(triggerTokenInstance)
							.getFeaturesNumeric(), triggerTokenInstance);
					
					if (prediction == argumentDict.getLabelNumeric("Non_Argument")) {
						continue;
					}
					
					if (arguments.containsKey(argumentTrigger.getId())) {
						Set<Argument> removeArg = new HashSet<Argument>(arguments.get(argumentTrigger.getId()));
						for (Argument arg : arguments.get(argumentTrigger.getId())) {
							if (arg.getId().equals(trigger.getId())) {
								double prediction0 = argumentRecogniser.predict_values(argumentDict
										.instanceToNumeric(argumentToInstance(jcas,
												sentence, argumentTrigger, trigger,
												pairsOfSentence, dependencyExtractor, false, false, Stage.THEME))
										.getFeaturesNumeric());
								double prediction1 = argumentRecogniser.predict_values(argumentDict
										.instanceToNumeric(argumentToInstance(jcas,
												sentence, trigger, argumentTrigger,
												pairsOfSentence, dependencyExtractor, false, false, Stage.THEME))
										.getFeaturesNumeric());
								if (prediction0 < prediction1) {
									continue loop;
								}else {
									removeArg.remove(arg);
								}
							}
						}
						arguments.put(argumentTrigger.getId(), removeArg);
					}
										
					Set<String> tris = arguments.keySet();
					if (prediction == argumentDict.getLabelNumeric("Cause")) {
						Set<Argument> args = new HashSet<Argument>();
						if (tris.contains(trigger.getId())) {
							args = arguments.get(trigger.getId());
						}
						
						Argument arg = new Argument();
						arg.setId(argumentTrigger.getId());
						arg.setRelation("Cause");
						args.add(arg);
						arguments.put(trigger.getId(), args)	;				
					}else if (prediction == argumentDict.getLabelNumeric("Theme")) {

						if (EventType.isRegulatoryEvent(trigger.getEventType())) {

							Set<Argument> args = new HashSet<Argument>();
							if (tris.contains(trigger.getId())) {
								args = arguments.get(trigger.getId());
							}
							
							Argument arg = new Argument();
							arg.setId(argumentTrigger.getId());
							arg.setRelation("Theme");
							args.add(arg);
							arguments.put(trigger.getId(), args)	;				
						}
					}
				}
			}
			//
			//new event
			//theme
			Map<String, Set<Event>> newtriggerEvents = new HashMap<String, Set<Event>>();
			Set<String> tIds = arguments.keySet();
			for (String tId : tIds) {
				Set<Argument> args = arguments.get(tId);
				Set<Event> triggerEvent = new HashSet<Event>();
				for (Argument arg : args) {
					if (arg.getRelation().equalsIgnoreCase("Theme")
							&& triggerEvents.containsKey(arg.getId())) {
						Set<Event> evens = triggerEvents.get(arg.getId());
						for (Event eve : evens) {
							Event event = new Event(jcas);
							event.setId(String.valueOf(eventIndex++));
							event.setTrigger(triggerId.get(tId));
							StringArray themes = new StringArray(jcas, 1);
							themes.set(0, "E".concat(eve.getId()));
							event.setThemes(themes);
							events.get(sentence.getId()).add(event);
							triggerEvent.add(event);
							newtriggerEvents.put(tId, triggerEvent);
							
							Argument argu = new Argument();
							argu.setId("E".concat(eve.getId()));
							argu.setRelation("Theme");
							List<Argument> argus = new LinkedList<Argument>();
							argus.add(argu);
							eventArg.put("E".concat(event.getId()), argus);

						}
					}
				}
			}
			Map<String, Set<Event>> newtriggerEvents2 = new HashMap<String, Set<Event>>();
			for (String tId : tIds) {
				Set<Argument> args = arguments.get(tId);
				Set<Event> triggerEvent = new HashSet<Event>();
				for (Argument arg : args) {
					if (arg.getRelation().equalsIgnoreCase("Theme")
							&& newtriggerEvents.containsKey(arg.getId())) {
						Set<Event> evens = newtriggerEvents.get(arg.getId());
						for (Event eve : evens) {
							Event event = new Event(jcas);
							event.setId(String.valueOf(eventIndex++));
							event.setTrigger(triggerId.get(tId));
							StringArray themes = new StringArray(jcas, 1);
							themes.set(0, "E".concat(eve.getId()));
							event.setThemes(themes);
							events.get(sentence.getId()).add(event);
							triggerEvent.add(event);
							newtriggerEvents2.put(tId, triggerEvent);
							
							Argument argu = new Argument();
							argu.setId("E".concat(eve.getId()));
							argu.setRelation("Theme");
							List<Argument> argus = new LinkedList<Argument>();
							argus.add(argu);
							eventArg.put("E".concat(event.getId()), argus);

						}
					}
				}
			}			
			//cause
			Map<String,Set<String>> equal = new HashMap<String,Set<String>>();
			Map<String, Set<Event>> newtriggerEvents3 = new HashMap<String, Set<Event>>();
			for (String tId : tIds) {
				Set<Argument> args = arguments.get(tId);
				Set<Event> triggerEvent = new HashSet<Event>();
				for (Argument arg : args) {
					if (arg.getRelation().equalsIgnoreCase("Cause")
							&& triggerEvents.containsKey(arg.getId())) {
						Set<Event> evens = triggerEvents.get(tId);//trigger event
						if (newtriggerEvents.containsKey(tId)) {
							evens.addAll(newtriggerEvents.get(tId));
						}
						if (newtriggerEvents2.containsKey(tId)) {
							evens.addAll(newtriggerEvents2.get(tId));
						}
						Set<Event> causeEvens = triggerEvents.get(arg.getId());//cause event
						for (Event eve : evens) {
							for (Event causeEve : causeEvens) {
								if (eve.getCause() != null) {
									Event event = new Event(jcas);
									event.setId(String.valueOf(eventIndex++));
									event.setTrigger(eve.getTrigger());
									event.setThemes(eve.getThemes());
									event.setCause("E".concat(causeEve.getId()));
									events.get(sentence.getId()).add(event);
									triggerEvent.add(event);
									newtriggerEvents3.put(tId, triggerEvent);
									
									List<Argument> argus = new LinkedList<Argument>();
									Argument argu = new Argument();
									argu.setId("E".concat(causeEve.getId()));
									argu.setRelation("Cause");
									argus.add(argu);
									argu.setId(eve.getThemes(0));
									argu.setRelation("Theme");
									argus.add(argu);
									eventArg.put("E".concat(event.getId()), argus);

									Set<String> ss = new HashSet<String>();
									if (equal.containsKey("E".concat(eve.getId()))) {
										ss = equal.get("E".concat(eve.getId()));
									}	
									ss.add("E".concat(event.getId()));
									equal.put("E".concat(eve.getId()), ss);
								}else {
									
									List<Argument> argus = eventArg.get("E".concat(eve.getId()));
									Argument argu = new Argument();
									argu.setId("E".concat(causeEve.getId()));
									argu.setRelation("Cause");
									argus.add(argu);
									eventArg.put("E".concat(eve.getId()), argus);

									eve.setCause("E".concat(causeEve.getId()));
								}
							}
						}
					}
				}
			}
			for (String tId : tIds) {
				Set<Argument> args = arguments.get(tId);
				Set<Event> triggerEvent = new HashSet<Event>();
				for (Argument arg : args) {
					if (arg.getRelation().equalsIgnoreCase("Cause")
							&& newtriggerEvents.containsKey(arg.getId())) {
						Set<Event> evens = triggerEvents.get(tId);//trigger event
						if (newtriggerEvents.containsKey(tId)) {
							evens.addAll(newtriggerEvents.get(tId));
						}
						if (newtriggerEvents2.containsKey(tId)) {
							evens.addAll(newtriggerEvents2.get(tId));
						}
						Set<Event> causeEvens = newtriggerEvents.get(arg.getId());//new cause event
						for (Event eve : evens) {
							for (Event causeEve : causeEvens) {
								if (eve.getCause() != null) {
									Event event = new Event(jcas);
									event.setId(String.valueOf(eventIndex++));
									event.setTrigger(eve.getTrigger());
									event.setThemes(eve.getThemes());
									event.setCause("E".concat(causeEve.getId()));
									events.get(sentence.getId()).add(event);
									triggerEvent.add(event);
									newtriggerEvents3.put(tId, triggerEvent);
									
									List<Argument> argus = new LinkedList<Argument>();
									Argument argu = new Argument();
									argu.setId("E".concat(causeEve.getId()));
									argu.setRelation("Cause");
									argus.add(argu);
									argu.setId(eve.getThemes(0));
									argu.setRelation("Theme");
									argus.add(argu);
									eventArg.put("E".concat(event.getId()), argus);

									Set<String> ss = new HashSet<String>();
									if (equal.containsKey("E".concat(eve.getId()))) {
										ss = equal.get("E".concat(eve.getId()));
									}	
									ss.add("E".concat(event.getId()));
									equal.put("E".concat(eve.getId()), ss);
								}else {
									
									List<Argument> argus = eventArg.get("E".concat(eve.getId()));
									Argument argu = new Argument();
									argu.setId("E".concat(causeEve.getId()));
									argu.setRelation("Cause");
									argus.add(argu);
									eventArg.put("E".concat(eve.getId()), argus);

									eve.setCause("E".concat(causeEve.getId()));
								}
							}
						}
					}
				}
			}
			
			// protein cause
			for (Trigger trigger : triggers.get(sentence.getId())) {
				if (!EventType
						.isComplexEvent(trigger.getEventType())) {
					continue;
				}
				Set<Event> triggerEvent2 = new HashSet<Event>();

				Set<Event> triggerEvent = triggerEvents.get(trigger.getId());
				if (newtriggerEvents.containsKey(trigger.getId())) {
					triggerEvent.addAll(newtriggerEvents.get(trigger.getId()));
				}
				if (newtriggerEvents2.containsKey(trigger.getId())) {
					triggerEvent.addAll(newtriggerEvents2.get(trigger.getId()));
				}

				for (Event event : triggerEvent) {
					if (triggerCauses.containsKey(trigger.getId())) {
						Set<String> triggerCause = triggerCauses.get(trigger.getId());
						for (String proCause : triggerCause) {
							if (event.getCause() != null) {
								Event event2 = new Event(jcas);
								event2.setId(String.valueOf(eventIndex++));
								event2.setTrigger(event.getTrigger());
								event2.setThemes(event.getThemes());
								event2.setCause(proCause);
								events.get(sentence.getId()).add(event2);
								triggerEvent2.add(event2);
								newtriggerEvents3.put(trigger.getId(), triggerEvent2);
								
								List<Argument> argus = new LinkedList<Argument>();
								Argument argu = new Argument();
								argu.setId(proCause);
								argu.setRelation("Cause");
								argus.add(argu);
								argu.setId(event.getThemes(0));
								argu.setRelation("Theme");
								argus.add(argu);
								eventArg.put("E".concat(event2.getId()), argus);

								Set<String> ss = new HashSet<String>();
								if (equal.containsKey("E".concat(event.getId()))) {
									ss = equal.get("E".concat(event.getId()));
								}	
								ss.add("E".concat(event2.getId()));
								equal.put("E".concat(event.getId()), ss);
							}else {

								List<Argument> argus = eventArg.get("E".concat(event.getId()));
								Argument argu = new Argument();
								argu.setId(proCause);
								argu.setRelation("Cause");
								argus.add(argu);
								eventArg.put("E".concat(event.getId()), argus);

								event.setCause(proCause);
							}
						}
					}
				}
			}
			//only event cause is different
			for (Trigger trigger : triggers.get(sentence.getId())) {

				if (!EventType.isComplexEvent(trigger.getEventType())) {
					continue;
				}
				Set<Event> triggerEvent2 = new HashSet<Event>();
				Set<Event> triggerEvent = triggerEvents.get(trigger.getId());
				if (newtriggerEvents.containsKey(trigger.getId())) {
					triggerEvent.addAll(newtriggerEvents.get(trigger.getId()));
				}
				if (newtriggerEvents2.containsKey(trigger.getId())) {
					triggerEvent.addAll(newtriggerEvents2.get(trigger.getId()));
				}
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
							triggerEvent2.add(event2);
							newtriggerEvents3.put(trigger.getId(), triggerEvent2);

							List<Argument> argus = new LinkedList<Argument>();
							Argument argu = new Argument();
							argu.setId(s);
							argu.setRelation("Cause");
							argus.add(argu);
							argu.setId(event.getThemes(0));
							argu.setRelation("Theme");
							argus.add(argu);
							eventArg.put("E".concat(event2.getId()), argus);

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
							
							List<Argument> argus = new LinkedList<Argument>();
							Argument argu = new Argument();
							argu.setId(event.getCause());
							argu.setRelation("Cause");
							argus.add(argu);
							argu.setId(s);
							argu.setRelation("Theme");
							argus.add(argu);
							eventArg.put("E".concat(event2.getId()), argus);

						}	
					}
				}
			}		
			//修剪
			List<String> removeEvent = new LinkedList<String>();
			for (Trigger trigger : triggers.get(sentence.getId())) {
				if (!EventType.isComplexEvent(trigger.getEventType())) {
					continue;
				}
				Set<Event> triggerEvent = triggerEvents.get(trigger.getId());
				if (newtriggerEvents.containsKey(trigger.getId())) {
					triggerEvent.addAll(newtriggerEvents.get(trigger.getId()));
				}
				if (newtriggerEvents2.containsKey(trigger.getId())) {
					triggerEvent.addAll(newtriggerEvents2.get(trigger.getId()));
				}
				if (newtriggerEvents3.containsKey(trigger.getId())) {
					triggerEvent.addAll(newtriggerEvents3.get(trigger.getId()));
				}
				for (Event event : triggerEvent) {
					if (null != event.getCause() && event.getCause().contains("E")) {
						List<Argument> args = eventArg.get(event.getCause());
						for (Argument arg : args) {
							if (arg.getId().contains("E")) {
								List<Argument> args2 = eventArg.get(arg.getId());
								for (Argument arg2 : args2) {
									for (Event event2 : triggerEvent) {
										if (event2.getId().equals(event.getId())) {
											continue;
										}
										List<Argument> args3 = eventArg.get("E".concat(event2.getId()));
										for (Argument arg3 : args3) {
											if (arg3.getId().contains("T") && arg3.getId().equals(arg2.getId())) {
												double prediction0 = 0;
												double prediction1 = 0;
												for (Event e : events.get(sentence.getId())) {
													if (e.getId().equals(event.getCause().replace("E", ""))) {
														prediction0 = argumentRecogniser.predict_values(argumentDict
														.instanceToNumeric(argumentToInstance(jcas,
																sentence, e.getTrigger(), trigger,
																pairsOfSentence, dependencyExtractor, false, false, Stage.THEME))
														.getFeaturesNumeric());
													}
												}
												
												if (EventType.isBindingEvent(event2.getTrigger().getEventType())) {
													List<Protein> themes = new LinkedList<Protein>();
													for (Protein protein : sentenceProteins) {
														for (int i=0; i<event2.getThemes().size(); i++) {
															if (protein.getId().equals(event2.getThemes(i))) {
																themes.add(protein);
															}
														}														
													}
													prediction1 = bindingRecogniser.predict_values(bindingDict
															.instanceToNumeric(bindingEventToInstance(jcas,
																	sentence, event2.getTrigger(), themes, dependencyExtractor, false))
															.getFeaturesNumeric());
												} else {
													for (Protein protein : sentenceProteins) {
														if (protein.getId().equals(event2.getThemes(0))) {
															prediction1 = argumentRecogniser.predict_values(argumentDict
																	.instanceToNumeric(argumentToInstance(jcas,
																			sentence, protein, event2.getTrigger(),
																			pairsOfSentence, dependencyExtractor, false, false, Stage.THEME))
																	.getFeaturesNumeric());
														}			
														break;
													}													
												}
												if (prediction0 < prediction1) {
													removeEvent.add(event.getId());
												}else {
													removeEvent.add(event2.getId());
												}												
											}
										}
									}
								}
							}else {
								for (Event event2 : triggerEvent) {
									if (event2.getId().equals(event.getId())) {
										continue;
									}
									List<Argument> args2 = eventArg.get("E".concat(event2.getId()));
									for (Argument arg2 : args2) {
										if (arg2.getId().contains("T") && arg2.getId().equals(arg.getId())) {
											double prediction0 = 0;
											double prediction1 = 0;
											for (Event e : events.get(sentence.getId())) {
												if (e.getId().equals(event.getCause().replace("E", ""))) {
													prediction0 = argumentRecogniser.predict_values(argumentDict
													.instanceToNumeric(argumentToInstance(jcas,
															sentence, e.getTrigger(), trigger,
															pairsOfSentence, dependencyExtractor, false, false, Stage.THEME))
													.getFeaturesNumeric());
												}
											}
											
											if (EventType.isBindingEvent(event2.getTrigger().getEventType())) {
												List<Protein> themes = new LinkedList<Protein>();
												for (Protein protein : sentenceProteins) {
													for (int i=0; i<event2.getThemes().size(); i++) {
														if (protein.getId().equals(event2.getThemes(i))) {
															themes.add(protein);
														}
													}														
												}
												prediction1 = bindingRecogniser.predict_values(bindingDict
														.instanceToNumeric(bindingEventToInstance(jcas,
																sentence, event2.getTrigger(), themes, dependencyExtractor, false))
														.getFeaturesNumeric());
											} else {
												for (Protein protein : sentenceProteins) {
													if (protein.getId().equals(event2.getThemes(0))) {
														prediction1 = argumentRecogniser.predict_values(argumentDict
																.instanceToNumeric(argumentToInstance(jcas,
																		sentence, protein, event2.getTrigger(),
																		pairsOfSentence, dependencyExtractor, false, false, Stage.THEME))
																.getFeaturesNumeric());
													}	
													break;
												}													
											}
											if (prediction0 < prediction1) {
												removeEvent.add(event.getId());
											}else {
												removeEvent.add(event2.getId());
											}
										}
									}
								}
							}
						}
					}
				}
				if (!EventType.isRegulatoryEvent(trigger.getEventType())) {
					continue;
				}
				for (Event event : triggerEvent) {
					if (event.getThemes(0).contains("E")) {
						List<Argument> args = eventArg.get(event.getThemes(0));
						for (Argument arg : args) {
							if (arg.getId().contains("E")) {
								List<Argument> args2 = eventArg.get(arg.getId());
								for (Argument arg2 : args2) {
									for (Event event2 : triggerEvent) {
										if (event2.getId().equals(event.getId())) {
											continue;
										}
										List<Argument> args3 = eventArg.get("E".concat(event2.getId()));
										for (Argument arg3 : args3) {
											if (arg3.getId().contains("T") && arg3.getId().equals(arg2.getId())) {
												double prediction0 = 0;
												double prediction1 = 0;
												for (Event e : events.get(sentence.getId())) {
													if (e.getId().equals(event.getThemes(0).replace("E", ""))) {
														prediction0 = argumentRecogniser.predict_values(argumentDict
														.instanceToNumeric(argumentToInstance(jcas,
																sentence, e.getTrigger(), trigger,
																pairsOfSentence, dependencyExtractor, false, false, Stage.THEME))
														.getFeaturesNumeric());
													}
												}
												
												if (EventType.isBindingEvent(event2.getTrigger().getEventType())) {
													List<Protein> themes = new LinkedList<Protein>();
													for (Protein protein : sentenceProteins) {
														for (int i=0; i<event2.getThemes().size(); i++) {
															if (protein.getId().equals(event2.getThemes(i))) {
																themes.add(protein);
															}
														}														
													}
													prediction1 = bindingRecogniser.predict_values(bindingDict
															.instanceToNumeric(bindingEventToInstance(jcas,
																	sentence, event2.getTrigger(), themes, dependencyExtractor, false))
															.getFeaturesNumeric());
												} else {
													for (Protein protein : sentenceProteins) {
														if (protein.getId().equals(event2.getThemes(0))) {
															prediction1 = argumentRecogniser.predict_values(argumentDict
																	.instanceToNumeric(argumentToInstance(jcas,
																			sentence, protein, event2.getTrigger(),
																			pairsOfSentence, dependencyExtractor, false, false, Stage.THEME))
																	.getFeaturesNumeric());
														}	
														break;
													}													
												}
												if (prediction0 < prediction1) {
													removeEvent.add(event.getId());
												}else {
													removeEvent.add(event2.getId());
												}
											}else if(arg3.getId().contains("E")) {
												List<Argument> args4 = eventArg.get(arg3.getId());
												for (Argument arg4 : args4) {
													if (arg4.getId().contains("T") && arg4.getId().equals(arg2.getId())) {
														double prediction0 = 0;
														double prediction1 = 0;
														for (Event e : events.get(sentence.getId())) {
															if (e.getId().equals(event.getThemes(0).replace("E", ""))) {
																prediction0 = argumentRecogniser.predict_values(argumentDict
																.instanceToNumeric(argumentToInstance(jcas,
																		sentence, e.getTrigger(), trigger,
																		pairsOfSentence, dependencyExtractor, false, false, Stage.THEME))
																.getFeaturesNumeric());
															}
														}
														
														if (EventType.isBindingEvent(event2.getTrigger().getEventType())) {
															List<Protein> themes = new LinkedList<Protein>();
															for (Protein protein : sentenceProteins) {
																for (int i=0; i<event2.getThemes().size(); i++) {
																	if (protein.getId().equals(event2.getThemes(i))) {
																		themes.add(protein);
																	}
																}														
															}
															prediction1 = bindingRecogniser.predict_values(bindingDict
																	.instanceToNumeric(bindingEventToInstance(jcas,
																			sentence, event2.getTrigger(), themes, dependencyExtractor, false))
																	.getFeaturesNumeric());
														} else {
															for (Protein protein : sentenceProteins) {
																if (protein.getId().equals(event2.getThemes(0))) {
																	prediction1 = argumentRecogniser.predict_values(argumentDict
																			.instanceToNumeric(argumentToInstance(jcas,
																					sentence, protein, event2.getTrigger(),
																					pairsOfSentence, dependencyExtractor, false, false, Stage.THEME))
																			.getFeaturesNumeric());
																}	
																break;
															}													
														}
														if (prediction0 < prediction1) {
															removeEvent.add(event.getId());
														}else {
															removeEvent.add(event2.getId());
														}
													}
												}
											}
										}
									}
								}
							}else {
								for (Event event2 : triggerEvent) {
									if (event2.getId().equals(event.getId())) {
										continue;
									}
									List<Argument> args2 = eventArg.get("E".concat(event2.getId()));
									for (Argument arg2 : args2) {
										if (arg2.getId().contains("T") && arg2.getId().equals(arg.getId())) {
											double prediction0 = 0;
											double prediction1 = 0;
											for (Event e : events.get(sentence.getId())) {
												if (e.getId().equals(event.getThemes(0).replace("E", ""))) {
													prediction0 = argumentRecogniser.predict_values(argumentDict
													.instanceToNumeric(argumentToInstance(jcas,
															sentence, e.getTrigger(), trigger,
															pairsOfSentence, dependencyExtractor, false, false, Stage.THEME))
													.getFeaturesNumeric());
												}
											}
											
											if (EventType.isBindingEvent(event2.getTrigger().getEventType())) {
												List<Protein> themes = new LinkedList<Protein>();
												for (Protein protein : sentenceProteins) {
													for (int i=0; i<event2.getThemes().size(); i++) {
														if (protein.getId().equals(event2.getThemes(i))) {
															themes.add(protein);
														}
													}														
												}
												prediction1 = bindingRecogniser.predict_values(bindingDict
														.instanceToNumeric(bindingEventToInstance(jcas,
																sentence, event2.getTrigger(), themes, dependencyExtractor, false))
														.getFeaturesNumeric());
											} else {
												for (Protein protein : sentenceProteins) {
													if (protein.getId().equals(event2.getThemes(0))) {
														prediction1 = argumentRecogniser.predict_values(argumentDict
																.instanceToNumeric(argumentToInstance(jcas,
																		sentence, protein, event2.getTrigger(),
																		pairsOfSentence, dependencyExtractor, false, false, Stage.THEME))
																.getFeaturesNumeric());
													}	
													break;
												}													
											}
											if (prediction0 < prediction1) {
												removeEvent.add(event.getId());
											}else {
												removeEvent.add(event2.getId());
											}
										}else if(arg2.getId().contains("E")) {
											List<Argument> args4 = eventArg.get(arg2.getId());
											for (Argument arg4 : args4) {
												if (arg4.getId().contains("T") && arg4.getId().equals(arg.getId())) {
													double prediction0 = 0;
													double prediction1 = 0;
													for (Event e : events.get(sentence.getId())) {
														if (e.getId().equals(event.getThemes(0).replace("E", ""))) {
															prediction0 = argumentRecogniser.predict_values(argumentDict
															.instanceToNumeric(argumentToInstance(jcas,
																	sentence, e.getTrigger(), trigger,
																	pairsOfSentence, dependencyExtractor, false, false, Stage.THEME))
															.getFeaturesNumeric());
														}
													}
													
													if (EventType.isBindingEvent(event2.getTrigger().getEventType())) {
														List<Protein> themes = new LinkedList<Protein>();
														for (Protein protein : sentenceProteins) {
															for (int i=0; i<event2.getThemes().size(); i++) {
																if (protein.getId().equals(event2.getThemes(i))) {
																	themes.add(protein);
																}
															}														
														}
														prediction1 = bindingRecogniser.predict_values(bindingDict
																.instanceToNumeric(bindingEventToInstance(jcas,
																		sentence, event2.getTrigger(), themes, dependencyExtractor, false))
																.getFeaturesNumeric());
													} else {
														for (Protein protein : sentenceProteins) {
															if (protein.getId().equals(event2.getThemes(0))) {
																prediction1 = argumentRecogniser.predict_values(argumentDict
																		.instanceToNumeric(argumentToInstance(jcas,
																				sentence, protein, event2.getTrigger(),
																				pairsOfSentence, dependencyExtractor, false, false, Stage.THEME))
																		.getFeaturesNumeric());
															}	
															break;
														}													
													}
													if (prediction0 < prediction1) {
														removeEvent.add(event.getId());
													}else {
														removeEvent.add(event2.getId());
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			
			List<String> removeEvent2 = new LinkedList<String>();
			for (Event event : events.get(sentence.getId())) {
				if (!EventType.isComplexEvent(event.getTrigger().getEventType())) {
					continue;
				}				
				if (null != event.getCause() && removeEvent.contains(event.getCause().replace("E", ""))) {
					event.setCause("");
				}
				if (removeEvent.contains(event.getThemes(0).replace("E", ""))) {
					removeEvent2.add(event.getId());
				}
			}
			for (Event event : events.get(sentence.getId())) {
				if (!EventType.isComplexEvent(event.getTrigger().getEventType())) {
					continue;
				}				
				if (null != event.getCause() && removeEvent2.contains(event.getCause().replace("E", ""))) {
					event.setCause("");
				}
				if (removeEvent2.contains(event.getThemes(0).replace("E", ""))) {
					removeEvent.add(event.getId());
				}
			}
			List<Event> eventscopy = new LinkedList<Event>();
			for (Event event : events.get(sentence.getId())) {
				if (!removeEvent.contains(event.getId()) && !removeEvent2.contains(event.getId())) {
					eventscopy.add(event);
				}
			}
			events.put(sentence.getId(), eventscopy);
		}
		return resultToString(triggers, events);
		//return perform;
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

	public static void main(String[] args)  {

		EventExtractorBind2 ee = new EventExtractorBind2();
		ee.setTaeDescriptor("/desc/TestSetAnnotator.xml");
		//ee.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
		//File inputFile = new File(args[0]);
		
		//File inputFile = new File("/media/songrq/soft/litway/数据/"
		//		+ "BioNLP13/BioNLP-ST-2013_GE_devel_data_yuanShuJu");
		
		//File inputFile = new File("/media/songrq/soft/litway/数据/"
		//		+ "BioNLP13/BioNLP-ST-2013_GE_test_data_yuanShuJu");
		
		File inputFile = new File(args[0]);
		
		//File inputFile = new File("/media/songrq/soft/litway/数据/"
		//		+ "BioNLP13/b");
		try {
			ee.extract(inputFile);
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
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
	public static final class Argument {

		private String argId;
		private String relation;

		public String getId() {
			return argId;
		}

		public void setId(String argId) {
			this.argId = argId;
		}

		public String getRelation() {
			return relation;
		}

		public void setRelation(String relation) {
			this.relation = relation;
		}
	}

	protected Token getToken(JCas jcas, Annotation annotation) {
		
		List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class,
				annotation);
		// if protein/trigger is within a token
		if (tokens.size() == 0) {
			FSIterator<Annotation> iter = jcas.getAnnotationIndex(
					Token.type).iterator();
			tokens = new ArrayList<Token>();
			while (iter.hasNext()) {
				Token token = (Token) iter.next();
				if (token.getBegin() <= annotation.getBegin()
						&& token.getEnd() >= annotation.getEnd()) {
					tokens.add(token);
					break;
				}
			}
		}
		/*if (tokens.size() == 0) {
			System.out.println(annotation.getCoveredText());
		}*/
		Token token = tokens.get(0);						
		for (Token aToken : tokens) {

			try {
				Double.parseDouble(aToken.getLemma());
				break;
			} catch (NumberFormatException e) {
				if (aToken.getCoveredText().equals(")")) {
					break;
				}
				token = aToken;
			}
		}
		return token;
	}
	
	protected Set<String> getNotProtein(JCas jcas, Trigger trigger, List<Protein> sentenceProteins, DependencyExtractor dependencyExtractor) {
		
		//delete protein whose token is too far from triggertoken
		Set<String> notTheme = new HashSet<String>();
		for (Protein protein : sentenceProteins) {
			Token triggerToken = getTriggerToken(jcas, trigger);														
			Token token = getToken(jcas, protein);				
			int pathLength = dependencyExtractor.getDijkstraShortestPathLength(
					triggerToken, token);
			//int distance = token.getId() > triggerToken.getId() ? token.getId() - triggerToken.getId()
			//		: triggerToken.getId() - token.getId();
			if (pathLength > 6) {
				notTheme.add(protein.getId());
			}
			/*if (distance > 10) {
				notTheme.add(protein.getId());
			}*/
		}
		return notTheme;
	}
	
	protected Set<String> getFarProtein(JCas jcas, Trigger trigger, List<Protein> sentenceProteins, DependencyExtractor dependencyExtractor) {
		
		//the same protein, delete far protein
		Set<String> farProtein = new HashSet<String>();
		if (sentenceProteins.size() > 1) {
			for(Protein p : sentenceProteins) {
				for(Protein p2 : sentenceProteins) {
					if (p.getId().equalsIgnoreCase(p2.getId())) {
						continue;
					}
					if (p.getCoveredText().equalsIgnoreCase(p2.getCoveredText())
							|| p.getCoveredText().contains(p2.getCoveredText())
							|| p2.getCoveredText().contains(p.getCoveredText())
							) {
						
						Token triggerToken = getTriggerToken(jcas, trigger);														
						Token token = getToken(jcas, p);										
						Token token2 = getToken(jcas, p2);
						
						int distance = token.getId() > triggerToken.getId() ? token.getId() - triggerToken.getId()
								: triggerToken.getId() - token.getId();
						int distance2 = token2.getId() > triggerToken.getId() ? token2.getId() - triggerToken.getId()
								: triggerToken.getId() - token2.getId();
						int pathLength = dependencyExtractor.getDijkstraShortestPathLength(
								triggerToken, token);
						int pathLength2 = dependencyExtractor.getDijkstraShortestPathLength(
								triggerToken, token2);
						if (pathLength > pathLength2) {
							farProtein.add(p.getId());
						}else if (pathLength < pathLength2) {
							farProtein.add(p2.getId());
						}else if (pathLength == pathLength2 && distance > distance2) {
							farProtein.add(p.getId());
						}else if (pathLength == pathLength2 && distance < distance2) {
							farProtein.add(p2.getId());
						}
					}
				}
			}
		}
		return farProtein;
	}
	
	protected Set<String> getAndProtein(JCas jcas, List<Protein> sentenceProteins, DependencyExtractor dependencyExtractor) {
			
		Set<String> andProtein = new HashSet<String>();
		if (sentenceProteins.size() > 1) {
			for (Protein protein : sentenceProteins) {
				for (Protein protein2 : sentenceProteins) {
					if (protein.getId().equalsIgnoreCase(protein2.getId())) {
						continue;
					}
					Token token2 = getToken(jcas, protein2);														
					Token token = getToken(jcas, protein);	
					String dependencyPath = dependencyExtractor.getShortestPath(
							token, token2, Stage.BINDING);
					if (dependencyPath != null
							&& (dependencyPath.equalsIgnoreCase("conj_and") 
							|| dependencyPath.equalsIgnoreCase("-conj_and")
							||dependencyPath.equalsIgnoreCase("conj_or") 
							|| dependencyPath.equalsIgnoreCase("-conj_or")
							||dependencyPath.equalsIgnoreCase("abbrev") //缩写,equlv
							|| dependencyPath.equalsIgnoreCase("-abbrev")
							//||dependencyPath.equalsIgnoreCase("appos") 
							//|| dependencyPath.equalsIgnoreCase("-appos")
							)) {
						andProtein.add(protein.getId());
						andProtein.add(protein2.getId());
					}
					/*if (token2.getId() == token.getId()) {
						//andProtein.add(protein.getId());
						//andProtein.add(protein2.getId());
					}*/
					/*List<Token> between = new LinkedList<Token>();
					for (Token token : tokens) {
						if (protein2.getBegin() >= protein.getEnd()) {
							if (token.getBegin() >= protein.getEnd()
									&& token.getEnd() <= protein2.getBegin()) {
							between.add(token);
							}
						}else if(protein.getBegin() >= protein2.getEnd()) {
							if (token.getBegin() >= protein2.getEnd()
									&& token.getEnd() <= protein.getBegin()) {
							between.add(token);
							}
						}
					}
					boolean isAnd = true;
					for (Token token : between) {
						if (!token.getCoveredText().equalsIgnoreCase(",")
								&& !token.getCoveredText().equalsIgnoreCase("and")
								&& !token.getCoveredText().equalsIgnoreCase("or")
								) {
							isAnd = false;
							break;
						}
					}
					if (isAnd) {
						andProtein.add(protein.getId());
						andProtein.add(protein2.getId());
					}*/
				}
			}
		}
		return andProtein;
	}
	
	protected boolean shouldDelete(JCas jcas, Token token, List<Protein> sentenceProteins) {
		
		//delete entity
		if (token.getLemma().equals("mrna")
					&& token.getLeftToken() != null
					&& token.getLeftToken().getLeftToken() != null
					&& (token.getLeftToken().getLeftToken().getLemma().equals("induction") 
								|| token.getLeftToken().getLeftToken().getLemma().equals("express")
								|| token.getLeftToken().getLeftToken().getLemma().equals("expression"))) {
			for (Protein protein : sentenceProteins) {
				Token proteinToken = getToken(jcas, protein);
				Token token2 = token.getLeftToken();
				if (token2.getId() == proteinToken.getId()) {
					return true;//transcriotion: induction/express/expression PROTEIN mRNA
				}
			}
		}
		if (token.getLemma().equals("mrna")
				&& token.getLeftToken() != null
				&& token.getLeftToken().getLeftToken() != null
				&& token.getLeftToken().getLeftToken().getLemma().equals("of")
				&& token.getLeftToken().getLeftToken().getLeftToken() != null
				&& token.getLeftToken().getLeftToken().getLeftToken().getLemma().equals("level")
				&& token.getLeftToken().getLeftToken().getLeftToken().getLeftToken() != null
				&& token.getLeftToken().getLeftToken().getLeftToken().getLeftToken().getLemma().equals("higher")
				&& token.getLeftToken().getLeftToken().getLeftToken().getLeftToken().getLeftToken() != null
				&& token.getLeftToken().getLeftToken().getLeftToken().getLeftToken().getLeftToken().getLemma().equals("significantly")
				&& token.getLeftToken().getLeftToken().getLeftToken().getLeftToken().getLeftToken().getLeftToken() != null
				&& token.getLeftToken().getLeftToken().getLeftToken().getLeftToken().getLeftToken().getLeftToken().getLemma().equals("express")
				) {
			for (Protein protein : sentenceProteins) {
				Token proteinToken = getToken(jcas, protein);
				Token token2 = token.getLeftToken();
				if (token2.getId() == proteinToken.getId()) {
					return true;//transcriotion: expressed significantly higher levels of PROTEIN mRNA
				}
			}
		}
		if (token.getLemma().equals("mrna")
				&& token.getLeftToken() != null
				&& token.getLeftToken().getLeftToken() != null
				&& token.getLeftToken().getLeftToken().getLemma().equals("of")
				&& token.getLeftToken().getLeftToken().getLeftToken() != null
				&& token.getLeftToken().getLeftToken().getLeftToken().getLemma().equals("level")
				&& token.getLeftToken().getLeftToken().getLeftToken().getLeftToken() != null
				&& token.getLeftToken().getLeftToken().getLeftToken().getLeftToken().getLemma().equals("higher")
				&& token.getLeftToken().getLeftToken().getLeftToken().getLeftToken().getLeftToken() != null
				&& token.getLeftToken().getLeftToken().getLeftToken().getLeftToken().getLeftToken().getLemma().equals("express")
				) {
			for (Protein protein : sentenceProteins) {
				Token proteinToken = getToken(jcas, protein);
				Token token2 = token.getLeftToken();
				if (token2.getId() == proteinToken.getId()) {
					return true;//transcriotion: expressed higher levels of PROTEIN mRNA
				}
			}
		}
		if (token.getLemma().equals("mrna")
				&& token.getLeftToken() != null
				&& token.getLeftToken().getLeftToken() != null
				&& token.getLeftToken().getLeftToken().getLemma().equals("of") 
				&& token.getLeftToken().getLeftToken().getLeftToken() != null
				&& (token.getLeftToken().getLeftToken().getLeftToken().getLemma().equals("induction") 
							|| token.getLeftToken().getLeftToken().getLeftToken().getLemma().equals("expression")
							|| token.getLeftToken().getLeftToken().getLeftToken().getLemma().equals("induction"))) {
			for (Protein protein : sentenceProteins) {
				Token proteinToken = getToken(jcas, protein);
				Token token2 = token.getLeftToken();
				if (token2.getId() == proteinToken.getId()) {
					return true;//transcriotion: induction/express/expression of PROTEIN mRNA
				}
			}
		}
		if (token.getLemma().equals("transcript")
				&& token.getLeftToken() != null
				&& token.getLeftToken().getLeftToken() != null
				&& token.getLeftToken().getLeftToken().getLemma().equals("of")
				&& token.getLeftToken().getLeftToken().getLeftToken() != null
				&& (token.getLeftToken().getLeftToken().getLeftToken().getLemma().equals("production")
						|| token.getLeftToken().getLeftToken().getLeftToken().getLemma().equals("detect"))) {
			for (Protein protein : sentenceProteins) {
				Token proteinToken = getToken(jcas, protein);
				Token token2 = token.getLeftToken();
				if (token2.getId() == proteinToken.getId()) {
					return true;//transcriotion: production/detect of PROTEIN transcript
				}
			}
		}
		if (token.getLemma().equals("transcript")
				&& token.getLeftToken() != null
				&& token.getLeftToken().getLeftToken() != null
				&& (token.getLeftToken().getLeftToken().getLemma().equals("production")
						|| token.getLeftToken().getLeftToken().getLemma().equals("detect"))) {
			for (Protein protein : sentenceProteins) {
				Token proteinToken = getToken(jcas, protein);
				Token token2 = token.getLeftToken();
				if (token2.getId() == proteinToken.getId()) {
					return true;//transcriotion: production/detect PROTEIN transcript
				}
			}
		}
		if (token.getLemma().equals("mrna")
					&& token.getRightToken() != null
					&& (token.getRightToken().getLemma().equals("production") 
							|| token.getRightToken().getLemma().equals("synthesis")
							|| token.getRightToken().getLemma().equals("synthesize"))) {
			return true;//transcriotion: mRNA production/synthesis/synthesize
		}
		if (token.getLemma().equals("mrna")
					&& token.getRightToken() != null
					&& token.getRightToken().getLemma().equals("be")
					&& token.getRightToken().getRightToken() != null
					&& (token.getRightToken().getRightToken().getLemma().equals("express") 
							|| token.getRightToken().getRightToken().getLemma().equals("present"))) {
			return true;//transcriotion: mRNA be express/present
		}
		if (token.getLemma().equals("mrna")
				&& token.getRightToken() != null
				&& token.getRightToken().getLemma().equals("be")
				&& token.getRightToken().getRightToken() != null
				&& token.getRightToken().getRightToken().getLemma().equals("also")
				&& token.getRightToken().getRightToken().getRightToken() != null
				&& (token.getRightToken().getRightToken().getRightToken().getLemma().equals("express") 
						|| token.getRightToken().getRightToken().getRightToken().getLemma().equals("present"))) {
		return true;//transcriotion: mRNA be also express/present
		}
		if (token.getLemma().equals("transcription") 
				&& token.getRightToken() != null
				&& (token.getRightToken().getLemma().equals("factor")
						|| token.getRightToken().getLemma().equals("start"))) {
			return true;//transcription factor/start: entity,transcription
		}
		if (token.getLemma().equals("binding") 
				&& token.getRightToken() != null
				&& (token.getRightToken().getLemma().equals("site")
						|| token.getRightToken().getLemma().equals("domain")
						|| token.getRightToken().getLemma().equals("sequence"))) {
			return true;//binding site/domain/sequence: entity,binding
		}
		if (token.getLemma().equals("localisation") 
				&& token.getRightToken() != null
				&& token.getRightToken().getLemma().equals("sequence")
				&& token.getLeftToken() != null
				&& token.getLeftToken().getLemma().equals("nuclear")) {
			return true;//nuclear localisation sequence: entity,localisation
		}

		if (token.getLemma().equals("activation") 
				&& token.getRightToken() != null
				&& token.getRightToken().getLemma().equals("region")) {
			return true;//activation region : entity,Positive_regulation
		}
		if (token.getLemma().equals("located") 
				&& token.getLeftToken() != null
				&& token.getLeftToken().getLemma().equals("site")) {
			return true;//site located ,localisation
		}
		if (token.getLemma().equals("modification") 
				&& token.getLeftToken() != null
				&& !token.getLeftToken().getSubLemma().equals("translational")) {
			return true;//post-translational modification:	Protein_modification
		}
		return false;
	}

	protected String shouldChange(JCas jcas, Token token, List<Protein> sentenceProteins) {
		
		//multi-token trigger
		if (token.getLemma().equals("level") 
				&& token.getLeftToken() != null
				&& token.getLeftToken().getLemma().equals("protein")) {
			return "Gene-expression";//gene-expression:protein level
		}
		if (token.getLemma().equals("expression") 
				&& token.getLeftToken() != null
				&& (token.getLeftToken().getLemma().equals("mrna")
						|| token.getLeftToken().getLemma().equals("rna")
						|| (token.getLeftToken().getLemma().equals(")")
								&& token.getLeftToken().getLeftToken() != null
								&& token.getLeftToken().getLeftToken().getLemma().equals("mrna")))) {
			return "Transcriotion";//transcriotion: mrna/rna expression
		}
		if (token.getLemma().equals("transcriptional") 
				&& token.getRightToken() != null
				&& (token.getRightToken().getLemma().equals("level")
						|| token.getRightToken().getLemma().equals("activity")
						|| token.getRightToken().getLemma().equals("activation")
						|| token.getRightToken().getLemma().equals("regulation")
						|| token.getRightToken().getLemma().equals("elongation"))) {
			return "Transcriotion";//transcriotion: transcriptional level/activity/activation/regulation/elongation
		}
		/*if (token.getLemma().equals("level") 
				&& token.getLeftToken() != null
				&& (token.getLeftToken().getLemma().equals("mrna")
						|| token.getLeftToken().getLemma().equals("transcript")
						|| token.getLeftToken().getLemma().equals("transcription"))) {
			return 3;//transcriotion: mrna/transcript level
		}*/
		if ((token.getLemma().equals("express") 
				|| token.getLemma().equals("expression")
				|| token.getLemma().equals("induction"))
					&& token.getRightToken() != null
					&& token.getRightToken().getRightToken() != null
					&& token.getRightToken().getRightToken().getLemma().equals("mrna")) {
			for (Protein protein : sentenceProteins) {
				Token proteinToken = getToken(jcas, protein);
				Token token2 = token.getRightToken();
				if (token2.getId() == proteinToken.getId()) {
					return "Transcriotion";//transcriotion: induction/express/expression PROTEIN mRNA
				}
			}
		}
		if ((token.getLemma().equals("express") 
				|| token.getLemma().equals("expression")
				|| token.getLemma().equals("induction"))
					&& token.getRightToken() != null
					&& token.getRightToken().getLemma().equals("of")
					&& token.getRightToken().getRightToken() != null
					&& token.getRightToken().getRightToken().getRightToken() != null
					&& token.getRightToken().getRightToken().getRightToken().getLemma().equals("mrna")) {
			for (Protein protein : sentenceProteins) {
				Token proteinToken = getToken(jcas, protein);
				Token token2 = token.getRightToken().getRightToken();
				if (token2.getId() == proteinToken.getId()) {
					return "Transcriotion";//transcriotion: induction/express/expression of PROTEIN mRNA
				}
			}
		}
		if (token.getLemma().equals("express")
					&& token.getRightToken() != null
					&& token.getRightToken().getLemma().equals("significantly")
					&& token.getRightToken().getRightToken() != null
					&& token.getRightToken().getRightToken().getLemma().equals("higher")
					&& token.getRightToken().getRightToken().getRightToken() != null
					&& token.getRightToken().getRightToken().getRightToken().getLemma().equals("level")
					&& token.getRightToken().getRightToken().getRightToken().getRightToken() != null
					&& token.getRightToken().getRightToken().getRightToken().getRightToken().getLemma().equals("of")
					&& token.getRightToken().getRightToken().getRightToken().getRightToken().getRightToken() != null
					&& token.getRightToken().getRightToken().getRightToken().getRightToken().getRightToken().getRightToken() != null
					&& token.getRightToken().getRightToken().getRightToken().getRightToken().getRightToken().getRightToken().getLemma().equals("mrna")
					) {
			for (Protein protein : sentenceProteins) {
				Token proteinToken = getToken(jcas, protein);
				Token token2 = token.getRightToken().getRightToken().getRightToken().getRightToken().getRightToken();
				if (token2.getId() == proteinToken.getId()) {
					return "Transcriotion";//transcriotion: expressed significantly higher levels of PROTEIN mRNA
				}
			}
		}
		if (token.getLemma().equals("express")
				&& token.getRightToken() != null
				&& token.getRightToken().getLemma().equals("higher")
				&& token.getRightToken().getRightToken() != null
				&& token.getRightToken().getRightToken().getLemma().equals("level")
				&& token.getRightToken().getRightToken().getRightToken() != null
				&& token.getRightToken().getRightToken().getRightToken().getLemma().equals("of")
				&& token.getRightToken().getRightToken().getRightToken().getRightToken() != null
				&& token.getRightToken().getRightToken().getRightToken().getRightToken().getRightToken() != null
				&& token.getRightToken().getRightToken().getRightToken().getRightToken().getRightToken().getLemma().equals("mrna")
				) {
			for (Protein protein : sentenceProteins) {
				Token proteinToken = getToken(jcas, protein);
				Token token2 = token.getRightToken().getRightToken().getRightToken().getRightToken();
				if (token2.getId() == proteinToken.getId()) {
					return "Transcriotion";//transcriotion: expressed higher levels of PROTEIN mRNA
				}
			}
		}
		if ((token.getLemma().equals("production") 
				|| token.getLemma().equals("detect"))
					&& token.getRightToken() != null
					&& token.getRightToken().getLemma().equals("of")
					&& token.getRightToken().getRightToken() != null
					&& token.getRightToken().getRightToken().getRightToken() != null
					&& token.getRightToken().getRightToken().getRightToken().getLemma().equals("transcript")) {
			for (Protein protein : sentenceProteins) {
				Token proteinToken = getToken(jcas, protein);
				Token token2 = token.getRightToken().getRightToken();
				if (token2.getId() == proteinToken.getId()) {
					return "Transcriotion";//transcriotion: production/detect of PROTEIN transcript
				}
			}
		}
		if ((token.getLemma().equals("production") 
				|| token.getLemma().equals("detect"))
					&& token.getRightToken() != null
					&& token.getRightToken().getRightToken() != null
					&& token.getRightToken().getRightToken().getLemma().equals("transcript")) {
			for (Protein protein : sentenceProteins) {
				Token proteinToken = getToken(jcas, protein);
				Token token2 = token.getRightToken();
				if (token2.getId() == proteinToken.getId()) {
					return "Transcriotion";//transcriotion: production/detect PROTEIN transcript
				}
			}
		}
		if ((token.getLemma().equals("production") 
				|| token.getLemma().equals("synthesis")
				|| token.getLemma().equals("synthesize"))
					&& token.getLeftToken() != null
					&& token.getLeftToken().getLemma().equals("mrna")) {
			return "Transcriotion";//transcriotion: mRNA production/synthesis/synthesize
		}
		if ((token.getLemma().equals("express") 
				|| token.getLemma().equals("present"))
					&& token.getLeftToken() != null
					&& token.getLeftToken().getLemma().equals("be")
					&& token.getLeftToken().getLeftToken() != null
					&& token.getLeftToken().getLeftToken().getLemma().equals("mrna")) {
			return "Transcriotion";//transcriotion: mRNA be express/present
		}
		if ((token.getLemma().equals("express") 
				|| token.getLemma().equals("present"))
					&& token.getLeftToken() != null
					&& token.getLeftToken().getLemma().equals("also")
					&& token.getLeftToken().getLeftToken() != null
					&& token.getLeftToken().getLeftToken().getLemma().equals("be")
					&& token.getLeftToken().getLeftToken().getLeftToken() != null
					&& token.getLeftToken().getLeftToken().getLeftToken().getLemma().equals("mrna")) {
			return "Transcriotion";//transcriotion: mRNA be also express/present
		}
		if ((token.getLemma().equals("expression") || token.getLemma().equals("detect") || token.getLemma().equals("exclusion"))
					&& ((token.getLeftToken() != null
								&& (token.getLeftToken().getLemma().equals("nuclear") 
										|| token.getLeftToken().getLemma().equals("cytoplasmic")))
							|| (token.getLeftToken() != null
									&& token.getLeftToken().getLeftToken() != null
									&& (token.getLeftToken().getLeftToken().getLemma().equals("nuclear")
											|| token.getLeftToken().getLeftToken().getLemma().equals("cytoplasmic"))))) {
			return "Localization";//Localization: nuclear/cytoplasmic (PROTEIN) expression/detect/exclusion
		}
		if (token.getLemma().equals("located") 
				&& token.getRightToken() != null
				&& token.getRightToken().getLemma().equals("in") 
				&& token.getRightToken().getRightToken() != null
				&& ((token.getRightToken().getRightToken().getLemma().equals("nuclear")
							|| token.getRightToken().getRightToken().getLemma().equals("cytoplasmic"))
							||  (token.getRightToken().getRightToken().getRightToken() != null
									&&(token.getRightToken().getRightToken().getRightToken().getLemma().equals("nuclear")
											|| token.getRightToken().getRightToken().getRightToken().getLemma().equals("cytoplasmic"))))) {
		return "Localization";//Localization: located in  (PROTEIN) nuclear/cytoplasmic
		}
		if (token.getLemma().equals("detectable") 
				&& token.getRightToken() != null
				&& ((token.getRightToken().getLemma().equals("nuclear")
						 	|| token.getRightToken().getLemma().equals("cytoplasmic"))
						|| (token.getRightToken().getRightToken() != null
								&& (token.getRightToken().getLemma().equals("nuclear")
									 	|| token.getRightToken().getLemma().equals("cytoplasmic"))))) {
		return "Localization";//Localization: detectable (PROTEIN) nuclear/cytoplasmic
		}
		if (token.getLemma().equals("detectable") 
				&& token.getLeftToken() != null
				&& token.getLeftToken().getLemma().equals("be")
				&& token.getLeftToken().getLeftToken() != null
				&& ((token.getLeftToken().getLeftToken().getLemma().equals("nuclear")
							|| token.getLeftToken().getLeftToken().getLemma().equals("cytoplasmic"))
						|| (token.getLeftToken().getLeftToken().getLeftToken() != null
								&& (token.getLeftToken().getLeftToken().getLeftToken().getLemma().equals("nuclear")
										|| token.getLeftToken().getLeftToken().getLeftToken().getLemma().equals("cytoplasmic"))))) {
		return "Localization";//Localization:  nuclear/cytoplasmic (PROTEIN) be detectable
		}
		if (token.getLemma().equals("complex") 
				&& ((token.getLeftToken() != null
							&& token.getLeftToken().getLemma().equals("form"))
						||(token.getLeftToken() != null
							&& token.getLeftToken().getLeftToken() != null
							&& token.getLeftToken().getLeftToken().getLemma().equals("form")))) {
			return "Binding";//binding: forms a complex; form complexes
		}
		if (token.getLemma().equals("form") 
				&& ((token.getRightToken() != null
							&& (token.getRightToken().getLemma().equals("complex")
									|| token.getRightToken().getLemma().equals("heteromultimer")))
						||(token.getRightToken() != null
							&& token.getRightToken().getRightToken() != null
							&& token.getRightToken().getRightToken().getLemma().equals("complex")))) {
			return "Binding";//binding: forms a complex; form complexes; form heteromultimers
		}
		if (token.getLemma().equals("immunoprecipitate") 
				&& token.getRightToken() != null
				&& token.getRightToken().getLemma().equals("together")) {
			return "Binding";//immunoprecipitated together		Binding
		}
		if (token.getLemma().equals("heteromultimer") 
				&& token.getLeftToken() != null
				&& token.getLeftToken().getLemma().equals("form")) {
			return "Binding";//form heteromultimers		Binding
		}
		if (token.getLemma().equals("mechanism") 
				&& token.getLeftToken() != null
				&& token.getLeftToken().getSubLemma().equals("translational")) {
			return "Protein_modification";//post-translational mechanisms:	Protein_modification
		}		
		if (token.getLemma().equals("activity") 
				&& token.getLeftToken() != null
				&& token.getLeftToken().getCoveredText().contains("ubiquitin")) {
			return "Ubiquitination";//linear-ubiquitin-chain-ligase activity	: Ubiquitination
		}
		if (token.getLemma().equals("downstream") 
				&& token.getLeftToken() != null
				&& token.getLeftToken().getLemma().equals("function")) {
			return "Regulation";//regulation: function downstream
		}
		if (token.getLemma().equals("function") 
				&& token.getRightToken() != null
				&& token.getRightToken().getLemma().equals("downstream")) {
			return "Regulation";//regulation: function downstream
		}
		if (token.getLemma().equals("downstream") 
				&& token.getRightToken() != null
				&& token.getRightToken().getLemma().equals("target")) {
			return "Regulation";//regulation:  downstream target
		}
		if (token.getLemma().equals("control") 
				&& token.getLeftToken() != null
				&& token.getLeftToken().getLemma().equals("the")
				&& token.getLeftToken().getLeftToken() != null
				&& token.getLeftToken().getLeftToken().getLemma().equals("under")) {
			return "Regulation";//regulation:  under the control
		}
		if (token.getLemma().equals("specificity") 
				&& token.getLeftToken() != null
				&& token.getLeftToken().getCoveredText().contains("substrate")) {
			return "Regulation";//substrate specificity	:	Regulation
		}
		if (token.getLemma().equals("role")
				&& ((token.getLeftToken() != null
							&& token.getLeftToken().getLeftToken() != null
							&& token.getLeftToken().getLeftToken().getLemma().equals("play"))
					|| (token.getLeftToken() != null
							&& token.getLeftToken().getLeftToken() != null
							&& token.getLeftToken().getLeftToken().getLeftToken() != null
							&& token.getLeftToken().getLeftToken().getLeftToken().getLemma().equals("play"))
					|| (token.getLeftToken() != null
							&& token.getLeftToken().getLemma().equals("dual")))) {
			return "Regulation";//regulation: dual role; play an important role; plays a role
		}
		if (token.getLemma().equals("level") 
				&& token.getLeftToken() != null
				&& (token.getLeftToken().getLemma().equals("high")
						|| token.getLeftToken().getLemma().equals("higher"))) {
			return "Positive_regulation";//positive-regulation:high/higher level
		}
		if (token.getLemma().equals("presence") 
				&& token.getLeftToken() != null
				&& token.getLeftToken().getLemma().equals("the")
				&& token.getLeftToken().getLeftToken() != null
				&& token.getLeftToken().getLeftToken().getLemma().equals("in")
				&& token.getRightToken() != null
				&& token.getRightToken().getLemma().equals("of")) {
			return "Positive_regulation";//positive-regulation: in the presence of
		}
		if (token.getLemma().equals("response") 
				&& token.getLeftToken() != null
				&& token.getLeftToken().getLemma().equals("in")
				&& token.getRightToken() != null
				&& token.getRightToken().getLemma().equals("to")) {
			return "Positive_regulation";//positive-regulation:in response to
		}
		if (token.getLemma().equals("regulated") 
				&& token.getLeftToken() != null
				&& token.getLeftToken().getLemma().equals("up")) {
			return "Positive_regulation";//positive_regulation: up regulated
		}
		if (token.getLemma().equals("capacity") 
				&& token.getLeftToken() != null
				&& token.getLeftToken().getLemma().equals("suppressive")) {
			return "Negative_regulation";//suppressive capacity	Negative_regulation
		}
		if (token.getLemma().equals("effect") 
				&& token.getLeftToken() != null
				&& token.getLeftToken().getLemma().equals("inhibitory")) {
			return "Negative_regulation";//inhibitory effect	Negative_regulation
		}
		if (token.getLemma().equals("knock") 
				&& token.getRightToken() != null
				&& token.getRightToken().getLemma().equals("down")) {
			return "Negative_regulation";//knocked down		Negative_regulation
		}
		if (token.getLemma().equals("regulate") 
				&& token.getLeftToken() != null
				&& token.getLeftToken().getLemma().equals("negatively")) {
			return "Negative_regulation";//Negative_regulation: negatively regulate
		}
		return "Non_trigger";
	}

	private int predict2(int[] featureSparseVector, Instance instance,String modelFile) throws IOException
	{
		svm_model model = svm.svm_load_model(modelFile);
		int n = 0;
		svm_node[] x = new svm_node[n];
		
		double[] fs = instance.getFeaturesNumericWord2vec();
		if (null != fs) {
			n = featureSparseVector.length + fs.length;
		} else {
			n = featureSparseVector.length;
		}
		int num = 0;
		if (null != fs) {
			for(int j=0; j<fs.length; j++)
			{
				x[j] = new svm_node();
				x[j].index = j+1;
				x[j].value = fs[j];
			}
			num = fs.length;
		}
		int previousIndex = 0;
		for (int index : featureSparseVector) {
			if (index > previousIndex) {
				if (null != fs) {
					x[num] = new svm_node();
					x[num].index = fs.length + index;
					x[num].value = 1;
				}else {
					x[num] = new svm_node();
					x[num].index = index;
					x[num].value = 1;
				}
			}
			num++;
			previousIndex = index;
		}
		
		return (int)svm.svm_predict(model,x);
	}	

}
