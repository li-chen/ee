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

public class ThemeInstances extends AbstractInstances {

	private final static Logger logger = Logger.getLogger(ThemeInstances.class
			.getName());

	public ThemeInstances() {
		super(new int[] { Protein.type, Event.type });

	}

	@Override
	protected List<String> getLabelsString() {

		ArrayList<String> themeTypes = new ArrayList<String>();

		themeTypes.add("Theme");
		themeTypes.add("Non_theme");

		return themeTypes;

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
		/*String s = FileUtil.removeFileNameExtension(
				UimaUtil.getJCasFilePath(jcas));*/
		// Currently, one sentence is considered as one structured instance.
		while (sentenceIter.hasNext()) {

			StructuredInstance si = new StructuredInstance();
			List<Instance> themeCandidates = new LinkedList<Instance>();
			si.setNodes(themeCandidates);

			Sentence sentence = (Sentence) sentenceIter.next();
			Set<Pair> pairsOfSentence = pairsOfArticle.get(sentence.getId());

			DependencyExtractor dependencyExtractor = new DependencyExtractor(
					JCasUtil.selectCovered(jcas, Token.class, sentence),
					pairsOfSentence);

			List<Event> events = JCasUtil.selectCovered(jcas, Event.class,
					sentence);
			List<Protein> proteins = JCasUtil.selectCovered(jcas,
					Protein.class, sentence);
			if (proteins.size() < 1) {
				continue;
			}
			if (events.size() < 1) {
				continue;
			}
			for (Event event : events) {
				// check protein themes
				for (Protein protein : proteins) {
					boolean isTheme = false;
					for (int i = 0; i < event.getThemes().size(); i++) {
						isTheme = event.getThemes(i).equals(
													protein.getId());
						if (isTheme == true) {
							break;
						}
					}
					Instance instance = themeToInstance(jcas, sentence,
							protein, event.getTrigger(), pairsOfSentence,
							dependencyExtractor, isTheme);
					
					if ( instance != null) {
						themeCandidates.add(instance);
					}
				}

				// check event themes
				if (EventType.isComplexEvent(event.getTrigger().getEventType())) {
					for (Event themeEvent : events) {

						if (event != themeEvent) {

							boolean isTheme = event.getThemes(0).equals(
									themeEvent.getId());

							themeCandidates.add(themeToInstance(jcas, sentence,
									themeEvent.getTrigger(),
									event.getTrigger(), pairsOfSentence,
									dependencyExtractor, isTheme));
						}
					}
				}
			}

			results.add(si);
		}

		return results;
	}

	public static void main(String[] args) {

		ThemeInstances ti = new ThemeInstances();
		ti.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");

		List<Instance> instances = ti.getInstances(new File(args[0]));

		InstanceDictionary dict = new InstanceDictionary();
		dict.creatNumericDictionary(instances);
		String classifierName = "liblinear";
		dict.saveDictionary(new File("./model/themes.".concat(classifierName)
				.concat(".dict")));

		ti.saveInstances(new File("./model/instances.theme.txt"));
		ti.saveSvmLightInstances(new File("./model/instances.theme.svm.txt"));

		if (args.length == 2 && args[1].equals("dev")) {

			ThemeInstances testInstances = new ThemeInstances();
			testInstances.setTaeDescriptor("/desc/GeTrainingSetAnnotator.xml");
			List<Instance> tInstances = testInstances.getInstances(new File(
					"./data/development/"));

			tInstances = dict.instancesToNumeric(tInstances);

			testInstances.saveInstances(new File(
					"./model/instances.theme.dev.txt"));
			testInstances.saveSvmLightInstances(new File(
					"./model/instances.theme.svm.dev.txt"));
		}

	}
}
