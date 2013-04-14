package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.litway.corpora.Event;
import info.chenli.litway.corpora.Protein;
import info.chenli.litway.corpora.Sentence;
import info.chenli.litway.corpora.Token;
import info.chenli.litway.searn.StructuredInstance;
import info.chenli.litway.util.DependencyExtractor;
import info.chenli.litway.util.FileUtil;
import info.chenli.litway.util.Permutations;
import info.chenli.litway.util.StanfordDependencyReader;
import info.chenli.litway.util.UimaUtil;
import info.chenli.litway.util.StanfordDependencyReader.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

public class BindingInstances extends AbstractInstances {

	public BindingInstances() {

		super("Binding", new int[] { Event.type });
		// TODO Auto-generated constructor stub
	}

	@Override
	protected List<String> getLabelsString() {

		return null;
	}

	@Override
	protected List<StructuredInstance> getStructuredInstances(JCas jcas,
			FSIterator<Annotation> annoIter) {

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
			List<Instance> bindingEventCandidates = new LinkedList<Instance>();
			si.setNodes(bindingEventCandidates);

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

				if (event.getTrigger().getEventType()
						.equals(String.valueOf(EventType.Binding))) {

					bindingEventCandidates.addAll(bindingEventToInstance(jcas,
							event, proteins, dependencyExtractor));
				}
			}

			results.add(si);
		}

		return results;
	}

	private List<Instance> bindingEventToInstance(JCas jcas,
			Event bindingEvent, List<Protein> sentenceProteins,
			DependencyExtractor dependencyExtractor) {

		List<Instance> result = new ArrayList<Instance>();

		List<String> themes = new ArrayList<String>();
		for (int i = 0; i < bindingEvent.getThemes().size(); i++) {
			themes.add(bindingEvent.getThemes(i));
		}

		Permutations<String> themePerm = new Permutations<String>(themes);
		while (themePerm.hasNext()) {
			Instance instance = new Instance();
			List<String[]> featureString = new ArrayList<String[]>();
			instance.setFeaturesString(featureString);

			// featureString.add(e)
		}

		return null;
	}
}
