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
import java.util.logging.Logger;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

public class ArgumentInstances extends AbstractInstances {

	private final static Logger logger = Logger.getLogger(ArgumentInstances.class
			.getName());

	public ArgumentInstances() {
		super(new int[] { Protein.type, Event.type });

	}

	@Override
	protected List<String> getLabelsString() {

		ArrayList<String> argumentTypes = new ArrayList<String>();

		argumentTypes.add("Theme");
		argumentTypes.add("Cause");
		argumentTypes.add("Non_Argument");

		return argumentTypes;

	}

	@Override
	protected List<StructuredInstance> getStructuredInstances(JCas jcas,
			FSIterator<Annotation> tokenIter) {

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

		/*String s = FileUtil.removeFileNameExtension(
				UimaUtil.getJCasFilePath(jcas));*/
		// Currently, one sentence is considered as one structured instance.
		while (sentenceIter.hasNext()) {

			StructuredInstance si = new StructuredInstance();
			List<Instance> argumentCandidates = new LinkedList<Instance>();
			si.setNodes(argumentCandidates);

			Sentence sentence = (Sentence) sentenceIter.next();
			Set<Pair> pairsOfSentence = pairsOfArticle.get(sentence.getId());

			DependencyExtractor dependencyExtractor = new DependencyExtractor(
					JCasUtil.selectCovered(jcas, Token.class, sentence),
					pairsOfSentence);

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
			Map<String, Set<Event>> triggerEvevts = new TreeMap<String, Set<Event>>();
			for (Trigger trigger : triggers) {
				for (Event event : events) {
					if (event.getTrigger().getBegin() == trigger.getBegin()) {
						Token themeToken = getThemeToken(jcas, event, sentence);
						if (null == themeToken) {
							// There are cross sentence themes, which are not considered
							// at the moment.
							//logger.warning(fileName.concat(": An event must have a theme. It may be caused by cross-sentence event."));
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
			
			for (Trigger trigger : triggers) {
				/*if (!EventType.isBindingEvent(trigger.getEventType())) {
					continue;
				}*/
				// check protein arguments
				for (Protein protein : proteins) {
					boolean isTheme = false, isCause = false;
					if (triggerEvevts.containsKey(trigger.getId())) {
						loop : for (Event event : triggerEvevts.get(trigger.getId())) {
							for (int i = 0; i < event.getThemes().size(); i++) {
								isTheme = event.getThemes(i).equals(
															protein.getId());
								if (isTheme == true) {
									break loop;
								}
							}
						}
					
						if (!isTheme
								&& EventType.isComplexEvent(trigger.getEventType())) {
							
							for (Event event : triggerEvevts.get(trigger.getId())) {
								if (null != event.getCause()) {
									isCause = event.getCause().equals(
																protein.getId());
									if (isCause == true) {
										break;
									}
								}
							}							
						}
					}
/*					Token triggerToken = getToken(jcas, trigger);														
					Token token = getToken(jcas, protein);				
					int pathLength = dependencyExtractor.getDijkstraShortestPathLength(
							triggerToken, token);
					int distance = token.getId() > triggerToken.getId() ? token.getId() - triggerToken.getId()
							: triggerToken.getId() - token.getId();
					if (pathLength > 6) {
						if (isTheme || isCause) {
							System.out.println("error");
						}
						//continue;
					}
					if (distance > 10) {
						//notTheme.add(protein.getId());
					}*/
					
					Instance instance = argumentToInstance(jcas, sentence,
							protein, trigger, pairsOfSentence,
							dependencyExtractor, isTheme, isCause, Stage.THEME);
					if ( instance != null) {
						argumentCandidates.add(instance);
					}
				}

				// check event arguments
				if (EventType.isComplexEvent(trigger.getEventType())) {
					for (Trigger argumentTrigger : triggers) {	
						if (argumentTrigger.getBegin() == trigger.getBegin()) {
							continue;
						}
						
						boolean isTheme =false, isCause =false;
						if (triggerEvevts.containsKey(trigger.getId()) 
								&& triggerEvevts.containsKey(argumentTrigger.getId())) {
							if (EventType.isRegulatoryEvent(trigger.getEventType())) {
								loop : for (Event event : triggerEvevts.get(trigger.getId())) {
									for (Event themeEvent : triggerEvevts.get(argumentTrigger.getId())) {
										if (event.getThemes(0).equalsIgnoreCase(themeEvent.getId())) {
											isTheme = true;
											break loop;
										}
									}
								}
							}
						
							if (!isTheme) {
								loop : for (Event event : triggerEvevts.get(trigger.getId())) {
									for (Event themeEvent : triggerEvevts.get(argumentTrigger.getId())) {
										if (null != event.getCause()
												&& event.getCause().equalsIgnoreCase(themeEvent.getId())) {
											isCause = true;
											break loop;
										}
									}
								}
							}
						}
						
						/*Token triggerToken = getToken(jcas, trigger);														
						Token token = getToken(jcas, argumentTrigger);				
						int pathLength = dependencyExtractor.getDijkstraShortestPathLength(
								triggerToken, token);
						int distance = token.getId() > triggerToken.getId() ? token.getId() - triggerToken.getId()
								: triggerToken.getId() - token.getId();
						if (pathLength > 6) {
							if (isTheme || isCause) {
								System.out.println("error");
							}
							//continue;
						}
						if (distance > 10) {
							//notTheme.add(protein.getId());
						}*/
						
						argumentCandidates.add(argumentToInstance(jcas, sentence,
								argumentTrigger, trigger, pairsOfSentence,
								dependencyExtractor, isTheme, isCause, Stage.THEME));
					}
				}
			}
			results.add(si);
		}

		return results;
	}

	public static void main(String[] args) {

		ArgumentInstances ti = new ArgumentInstances();
		ti.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");

		List<Instance> instances = ti.getInstances(new File(args[0]));

		InstanceDictionary dict = new InstanceDictionary();
		dict.creatNumericDictionary(instances);
		String classifierName = "liblinear";
		dict.saveDictionary(new File("./model/arguments.".concat(classifierName)
				.concat(".dict")));

		ti.saveInstances(new File("./model/instances.arguments.txt"));
		ti.saveSvmLightInstances(new File("./model/instances.arguments.svm.txt"));

		if (args.length == 2 && args[1].equals("dev")) {

			ArgumentInstances testInstances = new ArgumentInstances();
			testInstances.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
			List<Instance> tInstances = testInstances.getInstances(new File(
					"./data/development/"));

			tInstances = dict.instancesToNumeric(tInstances);

			testInstances.saveInstances(new File(
					"./model/instances.arguments.dev.txt"));
			testInstances.saveSvmLightInstances(new File(
					"./model/instances.arguments.svm.dev.txt"));
		}

	}
}
