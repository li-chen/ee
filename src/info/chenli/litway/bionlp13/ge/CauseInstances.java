package info.chenli.litway.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.litway.corpora.Event;
import info.chenli.litway.corpora.Protein;
import info.chenli.litway.corpora.Sentence;
import info.chenli.litway.corpora.Token;
import info.chenli.litway.searn.StructuredInstance;
import info.chenli.litway.util.DependencyExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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

		super("causes", Protein.type);

	}

	@Override
	protected List<String> getFeaturesString() {

		featuresString = new ArrayList<String>();
		featuresString.add("text");
		featuresString.add("lemma");
		featuresString.add("pos");
		featuresString.add("leftToken");
		featuresString.add("rightToken");
		featuresString.add("eventType");
		featuresString.add("triggerText");
		featuresString.add("triggerTokenText");
		featuresString.add("triggerTokenLemma");
		featuresString.add("dependencyPathToTrigger");
		featuresString.add("themeText");
		featuresString.add("themeTokenText");
		featuresString.add("themeTokenLemma");
		featuresString.add("dependencyPathToTheme");

		return featuresString;
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

		// Currently, one sentence is considered as one structured instance.
		while (sentenceIter.hasNext()) {

			StructuredInstance si = new StructuredInstance();
			List<Instance> causeCandidates = new LinkedList<Instance>();
			si.setNodes(causeCandidates);

			Sentence sentence = (Sentence) sentenceIter.next();

			DependencyExtractor dependencyExtractor = new DependencyExtractor(
					JCasUtil.selectCovered(jcas, Token.class, sentence));

			List<Event> events = JCasUtil.selectCovered(jcas, Event.class,
					sentence);
			List<Protein> proteins = JCasUtil.selectCovered(jcas,
					Protein.class, sentence);

			for (Event event : events) {

				// an event may not have an cause.
				if (null == event.getCause() || event.getCause().equals("")) {

					continue;
				}

				// check protein themes
				for (Protein protein : proteins) {

					boolean isCause = event.getCause().equals(protein.getId());

					causeCandidates.add(causeToInstance(jcas, protein, event,
							dependencyExtractor, isCause));
				}

				// check event themes
				for (Event causeEvent : events) {

					boolean isCause = event.getCause().equals(
							causeEvent.getId());
					causeCandidates.add(causeToInstance(jcas,
							causeEvent.getTrigger(), event,
							dependencyExtractor, isCause));
				}
			}

			results.add(si);
		}

		return results;
	}

	public static void main(String[] args) {

		TokenInstances ti = new TokenInstances();
		ti.getInstances(new File(args[0]));
		System.out.println(ti.getInstances());
	}

}