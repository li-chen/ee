package info.chenli.ee.bionlp13.ge;

import info.chenli.classifier.Instance;
import info.chenli.ee.corpora.Event;
import info.chenli.ee.corpora.Protein;
import info.chenli.ee.corpora.Sentence;
import info.chenli.ee.corpora.Token;
import info.chenli.ee.searn.StructuredInstance;
import info.chenli.ee.util.DependencyExtractor;

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

public class ThemeInstances extends AbstractInstances {

	private final static Logger logger = Logger.getLogger(ThemeInstances.class
			.getName());

	public ThemeInstances() {

		super("themes", Protein.type);

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

		return featuresString;
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

		// Currently, one sentence is considered as one structured instance.
		while (sentenceIter.hasNext()) {

			StructuredInstance si = new StructuredInstance();
			List<Instance> themeCandidates = new LinkedList<Instance>();
			si.setNodes(themeCandidates);

			Sentence sentence = (Sentence) sentenceIter.next();

			DependencyExtractor dependencyExtractor = new DependencyExtractor(
					JCasUtil.selectCovered(jcas, Token.class, sentence));

			List<Event> events = JCasUtil.selectCovered(jcas, Event.class,
					sentence);
			List<Protein> proteins = JCasUtil.selectCovered(jcas,
					Protein.class, sentence);

			for (Event event : events) {

				for (int i = 0; i < event.getThemes().size(); i++) {

					// check protein themes
					for (Protein protein : proteins) {

						boolean isTheme = event.getThemes(i).equals(
								protein.getId());

						themeCandidates.add(themeToInstance(jcas, protein,
								event.getTrigger(), dependencyExtractor,
								isTheme));
					}

					// check event themes
					for (Event themeEvent : events) {

						if (event != themeEvent) {

							boolean isTheme = event.getThemes(i).equals(
									themeEvent.getId());

							themeCandidates.add(themeToInstance(jcas,
									themeEvent.getTrigger(),
									event.getTrigger(), dependencyExtractor,
									isTheme));
						}
					}
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
