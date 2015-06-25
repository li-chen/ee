package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.classifier.InstanceDictionary;
import info.chenli.litway.corpora.Event;
import info.chenli.litway.corpora.POS;
import info.chenli.litway.corpora.Protein;
import info.chenli.litway.corpora.Sentence;
import info.chenli.litway.corpora.Token;
import info.chenli.litway.corpora.Trigger;
import info.chenli.litway.searn.StructuredInstance;
import info.chenli.litway.util.Combinations;
import info.chenli.litway.util.DependencyExtractor;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.StanfordDependencyReader;
import info.chenli.litway.util.StanfordDependencyReader.Pair;
import info.chenli.litway.util.UimaUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

public class BindingInstances extends AbstractInstances {

	public BindingInstances() {

		super(new int[] { Event.type });
		// TODO Auto-generated constructor stub
	}

	@Override
	protected List<String> getLabelsString() {

		return null;
	}

	@Override
	protected List<StructuredInstance> getStructuredInstances(JCas jcas,
			FSIterator<Annotation> annoIter) {
		final String classifierName = "liblinear";
		
		boolean test = true;
		ArgumentRecogniser argumentRecogniser = new ArgumentRecogniser();
		argumentRecogniser.loadModel(new File("./model/arguments.".concat(
				classifierName).concat(".model")));
		InstanceDictionary argumentDict = new InstanceDictionary();
		argumentDict.loadDictionary(new File("./model/arguments.".concat(
				classifierName).concat(".dict")));
		
		List<StructuredInstance> results = new LinkedList<StructuredInstance>();

		AnnotationIndex<Annotation> sentenceIndex = jcas
				.getAnnotationIndex(Sentence.type);

		FSIterator<Annotation> sentenceIter = sentenceIndex.iterator();
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

		int sentenceid = 0;
		// Currently, one sentence is considered as one structured instance.
		while (sentenceIter.hasNext()) {
			sentenceid++;
			StructuredInstance si = new StructuredInstance();
			List<Instance> bindingEventCandidates = new LinkedList<Instance>();
			si.setNodes(bindingEventCandidates);

			Sentence sentence = (Sentence) sentenceIter.next();
			Set<Pair> pairsOfSentence = pairsOfArticle.get(sentence.getId());

			List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, sentence);
			DependencyExtractor dependencyExtractor = new DependencyExtractor(
					tokens, pairsOfSentence);

			List<Event> events = JCasUtil.selectCovered(jcas, Event.class,
					sentence);
			List<Trigger> triggers= JCasUtil.selectCovered(jcas, Trigger.class,
					sentence);
			List<Protein> proteins = JCasUtil.selectCovered(jcas,
					Protein.class, sentence);
			if (proteins.size() < 1) {
				continue;
			}
			if (triggers.size() < 1) {
				continue;
			}
			//binding trigger's event
			Map<String, Set<Event>> triggerEvevts = new TreeMap<String, Set<Event>>();
			for (Trigger trigger : triggers) {
				
				if (!trigger.getEventType().equals(String.valueOf(EventType.Binding))) {
					continue;
				}
				for (Event event : events) {
					if (event.getTrigger().getBegin() == trigger.getBegin()) {
						int themeToken = getThemeToken2(jcas, event, sentence);
						if (event.getThemes().size() != themeToken) {
							// There are cross sentence themes, which are not considered at the moment.
							continue;
						}
						
						Set<Event> triggerEvevt = new HashSet<Event>();
						if (triggerEvevts.containsKey(trigger.getId())) {
							triggerEvevt = triggerEvevts.get(trigger.getId());
						}
						triggerEvevt.add(event);
						triggerEvevts.put(trigger.getId(), triggerEvevt);
					}
				}
			}
			String[] bindingLemma = {"assembly", "recruitment", "ligand", "interact", "association", 
					"ligation", "binding", "interaction", "recover", "recognize", "bind", "recruit", 
					"dna-binding", "complex", "form", "immunoprecipitate", "heteromultimer"};
			//proteins that relation is and
			Set<String> andProtein = getAndProtein(jcas, proteins, dependencyExtractor);		
			//extract instances
			for (Trigger trigger : triggers) {
				//int triggerEventSize = 0;
				if (!trigger.getEventType().equals(String.valueOf(EventType.Binding))) {
					continue;
				}
				/*boolean bind =false;
				Token token = getTriggerToken(jcas, trigger);	
				for (int i=0; i<bindingLemma.length; i++) {
					if (bindingLemma[i].equals(token.getLemma())) {
						bind = true;
						break;
					}
				}
				if(!bind) {
					continue;
				}*/
				//the same protein, delete far protein
				Set<String> farProtein = getFarProtein( jcas, trigger, proteins, dependencyExtractor); 
				Set<String> notTheme = getNotProtein( jcas, trigger, proteins, dependencyExtractor);
				
				for (Protein protein : proteins) {
					Instance proteinInstance = argumentToInstance(jcas,
							sentence, protein, trigger, pairsOfSentence,
							dependencyExtractor, false, false, Stage.THEME);
					if ( proteinInstance != null) {
						double prediction = argumentRecogniser.predict(argumentDict
								.instanceToNumeric(proteinInstance)
								.getFeaturesNumeric(), proteinInstance);
						if (prediction != argumentDict.getLabelNumeric("Theme")) {
							notTheme.add(protein.getId());
						}
					}
				}

				Set<Event> triggerEvevt = triggerEvevts.get(trigger.getId());
				List<Protein> themeProteins = new LinkedList<Protein>();
				for (Protein protein : proteins) {
					if (!notTheme.contains(protein.getId()) && !farProtein.contains(protein.getId())) {
						themeProteins.add(protein);
					}
				}
				Combinations<Protein> combs = new Combinations<Protein>(
						themeProteins);
				
				loop2 : for (List<Protein> themes : combs.getCombinations()) {
					boolean truepositive = false;
					int equalNum = 0;
					if (triggerEvevts.containsKey(trigger.getId())) {
						loop : for (Event bindingEvent : triggerEvevt) {
							equalNum = 0;
							if (null != bindingEvent.getThemes()
									&& themes.size() == bindingEvent.getThemes().size()) {
								for (Protein protein : themes) {
									boolean foundTheProtein = false;
									for (int i = 0; i < bindingEvent.getThemes().size(); i++) {
										if (protein.getId().equals(bindingEvent.getThemes(i))) {
											equalNum++;
											if (equalNum == themes.size()) {
												truepositive = true;
												break loop;
											}
											foundTheProtein = true;
											break;
										}
									}
									if (foundTheProtein == false) {
										break;
									}
								}
							} 
						}
					}
					/*if (truepositive) {
						triggerEventSize++;
					}		*/	
					
					if (themes.size() > 2) {
						/*if (truepositive) {
							System.out.println("error");
						}			*/													
						continue;
					}
					
					for (Protein p : themes) {
						if (test && farProtein.contains(p.getId())) {
							/*if (truepositive) {
								System.out.println("farerror");
							}		*/								
							continue loop2;
						}
						if (test && notTheme.contains(p.getId())) {
							/*if (truepositive) {
								System.out.println("noterror");
							}	*/														
							continue loop2;
						}
					}
					int num = 0;
					if (themes.size() > 1) {
						for (Protein p : themes) {
							if (andProtein.contains(p.getId())) {
								num++;
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
									/*if (truepositive) {
										System.out.println("sameerror");
									} else {
										System.out.println("same");
									}*/
									continue loop2;
								}
							}
						}
					}
					
					if (test && num > 1) {
						/*if (truepositive) {
							System.out.println("anderror");
						}	*/				
						continue;
					}
					
					
					
					Instance instance = bindingEventToInstance(jcas,
							sentence, trigger, themes, dependencyExtractor, truepositive);
					instance.setSentenceId(sentenceid);
					instance.setFileId(trigger.getCoveredText() + "\t");
					for (Protein p : themes) {
						instance.setFileId(instance.getFileId() + p.getCoveredText() + "\t");
					}
					bindingEventCandidates.add(instance);
				}
				//System.out.println(triggerEventSize);
			}
			
			results.add(si);
		}

		return results;
	}

	public static void main(String[] args) {

		BindingInstances ti = new BindingInstances();
		ti.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");

		List<Instance> instances = ti.getInstances(
				new File("/media/songrq/soft/litway/数据/BioNLP13/b"));

/*		InstanceDictionary dict = new InstanceDictionary();
		dict.creatNumericDictionary(instances);
		String classifierName = "liblinear";

		ti.saveInstances(new File("./model/instances.binding.txt"));
		ti.saveSvmLightInstances(new File(
				"./model/instances.binding.svm.no_dum.txt"));
*/
		/*if (args.length == 2 && args[1].equals("dev")) {
			dict.saveDictionary(new File("./model/binding.".concat(
					classifierName).concat(".dict")));

			BindingInstances testInstances = new BindingInstances();
			testInstances.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
			instances = testInstances.getInstances(new File(
					"./data/development/"));

			instances = dict.instancesToNumeric(instances);

			ti.saveInstances(new File("./model/instances.binding.dev.txt"));
			testInstances.saveSvmLightInstances(new File(
					"./model/instances.binding.svm.dev.no_dum.txt"));
		}*/
	}
	protected int getThemeToken2(JCas jcas, Event event, Sentence sentence) {

		int tokenNum = 0;
		StringArray themes = event.getThemes();
		
		for (Protein protein : JCasUtil.selectCovered(jcas, Protein.class,
				sentence)) {
			for (int i=0; i<themes.size(); i++) {
				if (themes.get(i).equals(protein.getId())) {
					tokenNum++;
				}
			}
		}
		return tokenNum;
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

}
