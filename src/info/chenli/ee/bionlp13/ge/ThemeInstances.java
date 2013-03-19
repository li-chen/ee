package info.chenli.ee.bionlp13.ge;

import info.chenli.ee.corpora.Event;
import info.chenli.ee.corpora.Protein;
import info.chenli.ee.corpora.Sentence;
import info.chenli.ee.corpora.Token;
import info.chenli.ee.searn.StructuredInstance;

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

public class ThemeInstances extends
		info.chenli.ee.bionlp13.ge.AbstractInstances {

	private final static Logger logger = Logger.getLogger(ThemeInstances.class
			.getName());

	public ThemeInstances() {

		super("themes", Protein.type);

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
		Attribute triggerLemmaAttr = new Attribute("triggerLemma",
				(ArrayList<String>) null);
		Attribute dependencyPathToTriggerAttr = new Attribute(
				"dependencyPathToTrigger", (ArrayList<String>) null);

		attributes = new ArrayList<Attribute>();
		attributes.add(textAttr);
		attributes.add(lemmaAttr);
		attributes.add(posAttr);
		attributes.add(leftTokenAttr);
		attributes.add(rightTokenAttr);
		attributes.add(eventTypeAttr);
		attributes.add(triggerTextAttr);
		attributes.add(triggerLemmaAttr);
		attributes.add(dependencyPathToTriggerAttr);

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
			LinkedList<Instance> themes = new LinkedList<Instance>();
			si.setNodes(themes);

			Sentence sentence = (Sentence) sentenceIter.next();

			List<Event> events = JCasUtil.selectCovered(jcas, Event.class,
					sentence);
			List<Protein> proteins = JCasUtil.selectCovered(jcas,
					Protein.class, sentence);

			for (Event event : events) {

				for (int i = 0; i < event.getThemes().size(); i++) {

					String themeId = event.getThemes(i);

					// check protein themes
					for (Protein protein : proteins) {

						if (themeId.equals(protein.getId())) {

							double[] values = new double[instances
									.numAttributes()];
							values[0] = instances.attribute(0).addStringValue(
									protein.getCoveredText());

							List<Token> tokens = JCasUtil.selectCovered(jcas,
									Token.class, protein);
							String tokenLemma = "", tokenPos = "";
							String leftToken = tokens.get(0).getCoveredText();
							String rightToken = tokens.get(tokens.size() - 1)
									.getCoveredText();

							for (Token token : tokens) {

								tokenLemma = tokenLemma
										.concat(token.getLemma()).concat("_");

								tokenPos = tokenPos.concat(token.getPos())
										.concat("_");
							}

							values[1] = instances.attribute(0).addStringValue(
									tokenLemma);
							values[2] = instances.attribute(0).addStringValue(
									tokenPos);
							values[3] = instances.attribute(0).addStringValue(
									leftToken);
							values[4] = instances.attribute(0).addStringValue(
									rightToken);
							values[5] = instances.attribute(0).addStringValue(
									event.getTrigger().getEventType());
							values[6] = instances.attribute(0).addStringValue(
									event.getTrigger().getCoveredText());
							values[7] = instances.attribute(0).addStringValue(
									tokenLemma);
							values[8] = instances.attribute(0).addStringValue(
									tokenLemma);
						}
					}

					// check event themes
				}
			}

			results.add(si);
		}

		return results;
	}

	/**
	 * 
	 * @param token
	 * @param triggerTokens
	 *            Used for extracting instances from training set. null when
	 *            being used for testing set.
	 * @return
	 */
	protected Instance tokenToInstance(Token token,
			Map<Integer, String> triggerTokens) {

		// attributes.add(textAttr);
		// attributes.add(lemmaAttr);
		// attributes.add(posAttr);
		// attributes.add(leftTokenAttr);
		// attributes.add(rightTokenAttr);
		// attributes.add(eventTypeAttr);
		// attributes.add(triggerTextAttr);
		// attributes.add(triggerLemmaAttr);
		// attributes.add(dependencyPathToTriggerAttr);

		double[] values = new double[instances.numAttributes()];

		values[0] = instances.attribute(0).addStringValue(
				token.getCoveredText());
		values[1] = instances.attribute(1).addStringValue(token.getLemma());
		values[2] = instances.attribute(2).addStringValue(token.getPos());
		values[3] = instances.attribute(3).addStringValue(
				null == token.getLeftToken() ? "" : token.getLeftToken()
						.getCoveredText());
		values[4] = instances.attribute(4).addStringValue(
				null == token.getRightToken() ? "" : token.getRightToken()
						.getCoveredText());
		if (null != triggerTokens) {
			String eventType = triggerTokens.containsKey(token.getId()) ? triggerTokens
					.get(token.getId()) : String.valueOf(EventType.Non_trigger);
			values[5] = classes.indexOfValue(eventType);
		}

		Instance instance = new DenseInstance(1.0, values);

		if (null == triggerTokens) {
			instance.setClassMissing();
		}

		return instance;
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