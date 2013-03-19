package info.chenli.ee.bionlp13.ge;

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
import java.util.Map;
import java.util.logging.Logger;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;

public class CauseInstances extends
		info.chenli.ee.bionlp13.ge.AbstractInstances {

	private final static Logger logger = Logger.getLogger(CauseInstances.class
			.getName());

	public CauseInstances() {

		super("causes", Protein.type);

	}

	@Override
	protected void initAttributes() {

		Attribute textAttr = new Attribute("text", (ArrayList<String>) null);
		Attribute lemmaAttr = new Attribute("lemma", (ArrayList<String>) null);
		Attribute posAttr = new Attribute("pos", (ArrayList<String>) null);
		Attribute leftTokenAttr = new Attribute("leftToken",
				(ArrayList<String>) null);
		Attribute rightTokenAttr = new Attribute("rightToken",
				(ArrayList<String>) null);
		Attribute eventTypeAttr = new Attribute("eventType",
				(ArrayList<String>) null);
		Attribute triggerTextAttr = new Attribute("triggerText",
				(ArrayList<String>) null);
		Attribute triggerTokenTextAttr = new Attribute("triggerTokenText",
				(ArrayList<String>) null);
		Attribute triggerTokenLemmaAttr = new Attribute("triggerTokenLemma",
				(ArrayList<String>) null);
		Attribute dependencyPathToTriggerAttr = new Attribute(
				"dependencyPathToTrigger", (ArrayList<String>) null);
		Attribute themeTextAttr = new Attribute("themeText",
				(ArrayList<String>) null);
		Attribute themeTokenTextAttr = new Attribute("themeTokenText",
				(ArrayList<String>) null);
		Attribute themeTokenLemmaAttr = new Attribute("themeTokenLemma",
				(ArrayList<String>) null);
		Attribute dependencyPathToThemeAttr = new Attribute(
				"dependencyPathToTheme", (ArrayList<String>) null);

		attributes = new ArrayList<Attribute>();
		attributes.add(textAttr);
		attributes.add(lemmaAttr);
		attributes.add(posAttr);
		attributes.add(leftTokenAttr);
		attributes.add(rightTokenAttr);
		attributes.add(eventTypeAttr);
		attributes.add(triggerTextAttr);
		attributes.add(triggerTokenTextAttr);
		attributes.add(triggerTokenLemmaAttr);
		attributes.add(dependencyPathToTriggerAttr);
		attributes.add(themeTextAttr);
		attributes.add(themeTokenTextAttr);
		attributes.add(themeTokenLemmaAttr);
		attributes.add(dependencyPathToThemeAttr);

	}

	@Override
	protected Attribute getClasses() {

		ArrayList<String> themeTypes = new ArrayList<String>();

		themeTypes.add("Theme");
		themeTypes.add("Non_theme");

		return new Attribute("class", themeTypes);

	}

	@Override
	protected List<StructuredInstance> fetchStructuredInstances(JCas jcas,
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

					String themeId = event.getThemes(i);

					// check protein themes
					for (Protein protein : proteins) {

						themeCandidates.add(causeToInstance(jcas, protein, event, dependencyExtractor));
					}

					// check event themes
					for (Event themeEvent: events) {
						themeCandidates.add(causeToInstance(jcas, themeEvent.getTrigger(), event, dependencyExtractor));
					}
				}
			}

			results.add(si);
		}

		return results;
	}

	@Override
	public File getTaeDescriptor() {

		return new File("./desc/TrainingSetAnnotator.xml");
	}

	public static void main(String[] args) {

		TokenInstances ti = new TokenInstances();
		ti.fetchInstances(new File(args[0]));
		System.out.println(ti.getInstances());
	}

}
