package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.classifier.InstanceDictionary;
import info.chenli.litway.corpora.Event;
import info.chenli.litway.corpora.Protein;
import info.chenli.litway.corpora.Sentence;
import info.chenli.litway.corpora.Token;
import info.chenli.litway.searn.StructuredInstance;
import info.chenli.litway.util.DependencyExtractor;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.StanfordDependencyReader;
import info.chenli.litway.util.StanfordDependencyReader.Pair;
import info.chenli.litway.util.UimaUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

public class CauseInstances extends AbstractInstances {

	private final static Logger logger = Logger.getLogger(CauseInstances.class
			.getName());

	public CauseInstances() {

		super(new int[] { Protein.type });

	}

	@Override
	protected List<String> getLabelsString() {

		ArrayList<String> causeTypes = new ArrayList<String>();

		causeTypes.add("Cause");
		causeTypes.add("Non_cause");

		return causeTypes;

	}

	@Override
	protected List<StructuredInstance> getStructuredInstances(JCas jcas,
			FSIterator<Annotation> tokenIter) {

		List<StructuredInstance> results = new LinkedList<StructuredInstance>();

		AnnotationIndex<Annotation> sentenceIndex = jcas
				.getAnnotationIndex(Sentence.type);

		FSIterator<Annotation> sentenceIter = sentenceIndex.iterator();
		Map<Integer, Set<Pair>> pairsOfArticle = StanfordDependencyReader
				.getPairs(new File(FileUtil.removeFileNameExtension(
						UimaUtil.getJCasFilePath(jcas)).concat(".sdepcc")));

		// Currently, one sentence is considered as one structured instance.
		while (sentenceIter.hasNext()) {

			StructuredInstance si = new StructuredInstance();
			List<Instance> causeCandidates = new LinkedList<Instance>();
			si.setNodes(causeCandidates);

			Sentence sentence = (Sentence) sentenceIter.next();
			Set<Pair> pairsOfSentence = pairsOfArticle.get(sentence.getId());

			DependencyExtractor dependencyExtractor = new DependencyExtractor(
					JCasUtil.selectCovered(jcas, Token.class, sentence),
					pairsOfSentence);

			List<Event> events = JCasUtil.selectCovered(jcas, Event.class,
					sentence);
			List<Protein> proteins = JCasUtil.selectCovered(jcas,
					Protein.class, sentence);

			for (Event event : events) {

				String themeId = event.getThemes(0);

				// find the theme token
				Token themeToken = null;
				if (themeId.startsWith("T")) {
					themeToken = getProteinToken(jcas, proteins, themeId);
				} else if (themeId.startsWith("E")) {
					for (int i = 0; i < events.size(); i++) {
						Event anEvent = events.get(i);
						if (anEvent.getId().equals(themeId)) {
							themeToken = getTriggerToken(jcas,
									event.getTrigger());
							break;
						}
					}

				}

				if (null == themeToken) {

					// There are cross sentence themes, which are not considered
					// at the moment.
					// throw new
					// RuntimeException("An event must have a theme.");
				}

				// check protein causes
				for (Protein protein : proteins) {

					boolean isCause = event.getCause() == null ? false : event
							.getCause().equals(protein.getId());

					causeCandidates.add(causeToInstance(jcas, sentence,
							protein, event.getTrigger(), pairsOfSentence,
							dependencyExtractor, isCause, themeToken));
				}

				// check event causes
				for (Event causeEvent : events) {

					boolean isCause = event.getCause() == null ? false : event
							.getCause().equals(causeEvent.getId());

					causeCandidates.add(causeToInstance(jcas, sentence,
							causeEvent.getTrigger(), event.getTrigger(),
							pairsOfSentence, dependencyExtractor, isCause,
							themeToken));
				}
			}

			results.add(si);
		}

		return results;
	}

	public static void main(String[] args) {

		CauseInstances ci = new CauseInstances();
		ci.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");

		List<Instance> instances = ci.getInstances(new File(args[0]));

		InstanceDictionary dict = new InstanceDictionary();
		dict.creatNumericDictionary(instances);
		String classifierName = "liblinear";

		ci.saveInstances(new File("./model/instances.cause.txt"));
		ci.saveSvmLightInstances(new File(
				"./model/instances.cause.svm.no_dum.txt"));

		if (args.length == 2 && args[1].equals("dev")) {
			dict.saveDictionary(new File("./model/causes.".concat(
					classifierName).concat(".dict")));

			CauseInstances testInstances = new CauseInstances();
			testInstances.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
			List<Instance> tInstances = testInstances.getInstances(new File(
					"./data/development/"));

			tInstances = dict.instancesToNumeric(tInstances);

			testInstances.saveInstances(new File("./model/instances.cause.dev.txt"));
			testInstances.saveSvmLightInstances(new File(
					"./model/instances.cause.svm.dev.no_dum.txt"));
		}
	}

}
