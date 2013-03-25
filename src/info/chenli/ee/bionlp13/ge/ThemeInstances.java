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
import java.util.Map;
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

					String themeId = event.getThemes(i);

					// check protein themes
					for (Protein protein : proteins) {

						themeCandidates.add(themeToInstance(jcas, protein,
								event.getTrigger(), themeId,
								dependencyExtractor));
					}

					// check event themes
					for (Event themeEvent : events) {
						themeCandidates.add(themeToInstance(jcas,
								themeEvent.getTrigger(), event.getTrigger(),
								themeId, dependencyExtractor));
					}
				}
			}

			results.add(si);
		}

		return results;
	}

	/**
	 * 
	 * @param jcas
	 * @param anno
	 * @param trigger
	 * @param themeId
	 * @param dependencyExtractor
	 * @return
	 */
	protected Instance themeToInstance(JCas jcas, Annotation anno, Event event,
			DependencyExtractor dependencyExtractor) {

		List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, anno);
		String tokenLemma = "", tokenPos = "";
		String leftToken = tokens.get(0).getCoveredText();
		String rightToken = tokens.get(tokens.size() - 1).getCoveredText();

		// Take the last non-digital token if protein is
		// multi-token.
		Token annoToken = null;
		for (Token token : tokens) {

			try {
				Double.parseDouble(token.getLemma());
			} catch (NumberFormatException e) {
				annoToken = token;
			}

			tokenLemma = tokenLemma.concat(token.getLemma()).concat("_");

			tokenPos = tokenPos.concat(token.getPos()).concat("_");
		}

		Instance instance = new Instance();
		List<String> featuresString = new ArrayList<String>();
		featuresString.add(anno.getCoveredText());
		values[1] = instances.attribute(0).addStringValue(tokenLemma);
		values[2] = instances.attribute(0).addStringValue(tokenPos);
		values[3] = instances.attribute(0).addStringValue(leftToken);
		values[4] = instances.attribute(0).addStringValue(rightToken);
		values[5] = instances.attribute(0).addStringValue(
				event.getTrigger().getEventType());
		values[6] = instances.attribute(0).addStringValue(
				event.getTrigger().getCoveredText());
		Token triggerToken = getTriggerToken(jcas, event.getTrigger());
		values[7] = instances.attribute(0).addStringValue(
				triggerToken.getCoveredText());
		values[8] = instances.attribute(0).addStringValue(
				triggerToken.getLemma());
		values[9] = instances.attribute(0).addStringValue(
				dependencyExtractor.getDijkstraShortestPath(annoToken,
						triggerToken));

		// protein that is theme
		Protein protein = null;
		if (anno instanceof Protein) {
			protein = (Protein) anno;
		}
		// TODO consider more themes. e.g. themes in binding.
		if (null != event.getThemes()
				&& event.getThemes().get(0).equals(protein.getId())) {

			values[10] = classes.indexOfValue("Theme");

		} else
		// protein that is not theme
		{
			values[10] = classes.indexOfValue("Non_theme");
		}

		return new DenseInstance(1.0, values);
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
